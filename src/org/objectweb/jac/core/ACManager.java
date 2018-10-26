/*
  Copyright (C) 2001-2003 Renaud Pawlak <renaud@aopsys.com>

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 2 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  USA */

package org.objectweb.jac.core;

import java.util.*;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.rtti.*;
import org.objectweb.jac.util.*;

/**
 * @author <a href="mailto:pawlak@cnam.fr">Renaud Pawlak</a>
 */

/**
 * This class manages all the aspect components that are present in
 * the JAC system.
 *
 * <p>When a new aspect component is registered, it is
 * woven regarding its weaving properties. Once woven, the aspect
 * component manager dispatches all the events of the
 * <code>BaseProgramListener</code> to it.
 *
 * @see AspectComponent
 * @see BaseProgramListener */

public class ACManager extends OrderedRepository 
   implements BaseProgramListener 
{
    static Logger logger = Logger.getLogger("jac");
    static Logger loggerAspects = Logger.getLogger("aspects");
    static Logger loggerACM = Logger.getLogger("acm");
    static Logger loggerWuni = Logger.getLogger("wuni");

    /**
    * A internally-used flag to bootstrap the AC manager. */

    public boolean registering = false;

    /**
    * Get the sole instance of Aspect Component Manager for this
    * JAC container.
    * 
    * <p>If this instance does not exist yet, then it is created.
    *
    * <p>This method returns a <code>Repository</code> so that the
    * result must be casted to used specific method of
    * <code>ACManager</code>.
    *
    * @return the aspect component manager of the local container
    * @see org.objectweb.jac.util.Repository */
   
    public static Repository get() {
        if ( acManager == null ) {
            acManager = new ACManager();
        }
        return acManager;
    }

    public static ACManager getACM() {
        return acManager;
    }

    public AspectComponent[] getAspectComponents() {
        return (AspectComponent[])objects.values().toArray(new AspectComponent[0]);
    }

    /** Stores the sole instance of Aspect Component Manager. */
    protected static ACManager acManager = null;

    /** Stores all the declared aspect components. */
    protected Hashtable declaredACs = (Hashtable) JacPropLoader.declaredACs;

    static {
        Runtime.getRuntime().addShutdownHook(
            new Thread() {
                    public void run() {
                        if (acManager!=null) {
                            logger.info("JAC system shutdown: notifying all ACs...");
                            acManager.onExit();
                        }
                        logger.info("Bye bye.");
                    }
                }
        );
        //GarbageCollector.init();
    }

    /**
    * Declares a new aspect component.
    *
    * @param name the name of the aspect component
    * @param path its path (must be accessible from the current
    * classpath) */

    public void declareAC(String name, String path) { 
        loggerAspects.debug("declaring "+path+" as "+name);
        declaredACs.put(name, path);
    }

    public boolean isACDeclared(String name) {
        if (name==null) return false;
        return (declaredACs.get(name) != null);
    }

    /**
    * Gets a declared aspect component path from its name.
    *
    * @param acName the name of the AC
    * @return the corresponding path, null if not declared */

    public String getACPathFromName(String acName) {
        return (String)declaredACs.get(acName);
    }

    /**
    * Gets the declared aspect components.
    *
    * @return the set of the declared aspect components names */

    public Set getDeclaredACs() {
        return declaredACs.keySet();
    }

    /**
    * Registers a declared aspect as is (not linked to an application
    * and with no configuration).
    *
    * @param name the aspect name */

    public AspectComponent registerDeclaredAC(String name) {
        AspectComponent ac = null;
        try {
            if ( !acManager.isRegistered(name) ) {
                String className = getACPathFromName(name);
                loggerAspects.debug("registering the "+name+" ac");
                acManager.register(
                    name, ac = (AspectComponent) Class.forName(className).newInstance());
            } else {
                loggerAspects.debug(name+" is already registered");
            }            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ac;
    }

    /**
     * Called by the JAC launcher to create and declare the aspect components
     * defined in jac.prop. */

    public static void main(String[] args) throws Throwable {

        ACManager acManager = (ACManager)get();

        loggerAspects.debug("initializing the AC manager");

        try {

            // Create the Composition Aspect 
            //String prop1 = JacObject.props.getProperty(compositionAspectProp);
            AspectComponent ac = null;
            try {
                loggerAspects.debug("compositionAspect = "+
                           JacPropLoader.compositionAspect);
                loggerAspects.debug("JacPropLoader = "+
                           Strings.hex(JacPropLoader.class));
                JacPropLoader.loadProps();
                ac = (AspectComponent) Class.forName(
                    JacPropLoader.compositionAspect ).newInstance();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
            acManager.register("JAC_composition_aspect", ac);

            /** Creating the needed aspects. */
            
            acManager.registerDeclaredAC("naming");
            acManager.registerDeclaredAC("binding");
            acManager.registerDeclaredAC("rtti");
            //         acManager.registerDeclaredAC( "deployment" );

        } catch (Exception e) {
            e.printStackTrace();
        }
              
    }

    /**
     * Returns an AC of a given name for the current application.
     * @param name aspect's name
     */
    public AspectComponent getAC(String name) {
        String appName = Collaboration.get().getCurApp();
        if (appName==null) {
            loggerAspects.error("getAC("+name+") cannot work because curApp==null");
        }
        return (AspectComponent)getObject(appName+"."+name);
    }

    /**
    * Returns an AC of a given name
    * @param fullName aspect's full name (<application_name>.<aspect_name>)
    */
    public AspectComponent getACFromFullName(String fullName) {
        return (AspectComponent)getObject(fullName);
    }

    /**
    * The default constructor will set the acManager field to the
    * right value and bind the JAC internal MOP to it so that it will
    * be notified of the base program events.
    *
    * @see BaseProgramListener */

    public ACManager () {
    }

    /**
    * Dispatch this event to all the registered Aspect Components.
    *
    * @param newInstance the remotly created instance
    * @param name the original remote name 
    * @see AspectComponent#whenRemoteInstantiation(Wrappee,String)
    * @see BaseProgramListener#whenRemoteInstantiation(Wrappee,String) */

    public final void whenRemoteInstantiation(Wrappee newInstance, 
                                              String name) {
        System.out.println("------ REMINST "+name+"-> "+newInstance);
        Iterator it = orderedObjects.iterator();
        while (it.hasNext()) {
            AspectComponent curac = (AspectComponent)it.next();
            curac.whenRemoteInstantiation(newInstance, name);
        }
    }

    /**
    * Dispatch this event to all the registered Aspect Components that
    * are woven.
    * 
    * @see AspectComponent#whenUsingNewInstance(Interaction)
    * @see BaseProgramListener#whenUsingNewInstance(Interaction) 
    */

    public final void whenUsingNewInstance(Interaction interaction) {
        loggerWuni.debug("whenUsingNewInstance "+Strings.hex(interaction.wrappee));
        //      Collaboration collab = Collaboration.get();
        //      String old_ac = (String)collab.getCurAC();
        try {
            ClassItem cli = interaction.getClassItem();
            boolean isJacClass = cli.getName().startsWith("org.objectweb.jac.core");
            Vector toNotify = (Vector)orderedObjects.clone();
            for (int i=0; i<toNotify.size(); i++) { 
                AspectComponent curac = (AspectComponent)toNotify.get(i);
                loggerAspects.debug("curac = "+names.get(curac));
                //            collab.setCurAC((String)names.get(curac));
                if ((!isJacClass) || curac.isSystemListener()) {
                    loggerWuni.debug("  wuni "+curac.getName());
                    curac.whenUsingNewInstance(interaction);
                }
            }
        } catch (Exception e) {
            loggerWuni.error("ACManager.whenUsingNewInstance "+interaction,e);
         
        } /*
            finally {
            collab.setCurAC(old_ac);
            }
          */
    }

    HashSet initializedClasses = new HashSet();
    public final synchronized void whenUsingNewClass(ClassItem cl) {
        if (!initializedClasses.contains(cl)) {
            loggerAspects.debug("whenUsingNewClass "+cl);
            try {
                Vector toNotify = (Vector)orderedObjects.clone();
                for (int i=0; i<toNotify.size(); i++) { 
                    AspectComponent curac = (AspectComponent)toNotify.get(i);
                    loggerAspects.debug("curac = "+names.get(curac));
                    curac.whenUsingNewClass(cl);
                }
            } catch ( Exception e ) {
                e.printStackTrace();
            }
            initializedClasses.add(cl);
        }
    }

    /**
    * Dispatch this event to all the woven Aspect Components.
    *
    * <p>Note: the current collaboration contains an attribute called
    * "orgObject" and that is a reference to the object that is
    * currently serialized.
    *
    * @param finalObject the corresponding serialized structure.
    * @see AspectComponent#whenSerialized(Wrappee,SerializedJacObject)
    * @see BaseProgramListener#whenSerialized(Wrappee,SerializedJacObject)
    */

    public final Wrappee whenSerialized(Wrappee orgObject, 
                                        SerializedJacObject finalObject) {
        Iterator it = orderedObjects.iterator();
        while (it.hasNext()) {
            AspectComponent curac = (AspectComponent)it.next();
            orgObject = curac.whenSerialized(orgObject,finalObject);
        }
        return orgObject;
    }

    /**
    * Dispatch this event to all the woven Aspect Components.
    *
    * <p>Note: the current collaboration contains an attribute called
    * "finalObject" and that is a reference to the object that is
    * will be finally deserialized.
    *
    * @param orgObject the corresponding serialized structure.
    * @see AspectComponent#whenDeserialized(SerializedJacObject,Wrappee)
    * @see BaseProgramListener#whenDeserialized(SerializedJacObject,Wrappee) 
    */

    public final Wrappee whenDeserialized(SerializedJacObject orgObject,
                                          Wrappee finalObject) {
        Iterator it = orderedObjects.iterator();
        while (it.hasNext()) {
            AspectComponent curac = (AspectComponent)it.next();
            finalObject = curac.whenDeserialized(orgObject,finalObject);
        }
        return finalObject;
    }

    /** 
    * Apply all the woven aspect components to the given wrappee.
    *
    * @param wrappee the wrappee that have to be treated
    */

    public final void simulateUsingNewInstance(Wrappee wrappee) {
        if (wrappee==null) 
            return;

        Iterator it=((Vector)orderedObjects.clone()).iterator();
        while(it.hasNext()) {
            AspectComponent curac = (AspectComponent)it.next();
            curac.simulateUsingNewInstance(wrappee);
        }
    }

    /**
    * Dispatch this event to all the registered Aspect Components that
    * are woven.
    * 
    * @param cloned the wrappee that is being cloned
    * @param clone the new clone of cloned
    * @see AspectComponent#whenClone(Wrappee,Wrappee)
    * @see BaseProgramListener#whenClone(Wrappee,Wrappee) */

    public final void whenClone(Wrappee cloned, Wrappee clone) {
        Object[] acs = orderedObjects.toArray();
        for ( int i=0; i<acs.length; i++) {
            AspectComponent curac = (AspectComponent)acs[i];
            curac.whenClone(cloned, clone);
        }
    }

    public final void whenDeleted(Wrappee object) {
        Object[] acs = orderedObjects.toArray();
        for (int i=0; i<acs.length; i++) {
            AspectComponent curac = (AspectComponent)acs[i];
            curac.whenDeleted(object);
        }
    }

    public final void whenFree(Wrappee object) {
        Object[] acs = orderedObjects.toArray();
        for (int i=0; i<acs.length; i++) {
            AspectComponent curac = (AspectComponent)acs[i];
            curac.whenFree(object);
        }
    }

    /**
    * Dispatch this event to all the registered Aspect Components that
    * are woven.
    * 
    * @see AspectComponent#afterApplicationStarted() */

    public final void afterApplicationStarted() {
        Object[] acs = orderedObjects.toArray();
        for ( int i = 0; i < acs.length; i++ ) {
            AspectComponent curac = (AspectComponent)acs[i];
            curac.afterApplicationStarted();
        }
    }

    public final void onExit() {
        Object[] acs = orderedObjects.toArray();
        for ( int i = 0; i < acs.length; i++ ) {
            AspectComponent curac = (AspectComponent)acs[i];
            curac.onExit();
        }
    }


    /**
    * Dispatch this event to all the registered Aspect Components that
    * are woven.
    * 
    * @see AspectComponent#whenTopologyChanged() */

    public final void whenTopologyChanged() {
        Object[] acs = orderedObjects.toArray();
        for ( int i = 0; i < acs.length; i++ ) {
            AspectComponent curac = (AspectComponent)acs[i];
            curac.whenTopologyChanged();
        }
    }
   
    /**
    * Dispatch this event to all the registered Aspect Components that
    * are woven.
    * 
    * @see AspectComponent#whenCloseDisplay(Display) */

    public final void whenCloseDisplay(Display display) {
        Object[] acs = orderedObjects.toArray();
        for ( int i = 0; i < acs.length; i++ ) {
            AspectComponent curac = (AspectComponent)acs[i];
            curac.whenCloseDisplay(display);
        }
    }

    /**
    * Forward this event to the aspect component that owns the
    * wrapper.
    *
    * @param wrapper the wrapper that is going to be runned
    * @param wrappingMethod the name of the may-be runned wrapping
    * @see AspectComponent#beforeRunningWrapper(Wrapper,String)
    * @see BaseProgramListener#beforeRunningWrapper(Wrapper,String) */

    public final boolean beforeRunningWrapper(Wrapper wrapper,
                                              String wrappingMethod) {
        boolean ret = true;
        AspectComponent ac = wrapper.getAspectComponent();      
      
        if (ac != null) {
            if (!ac.beforeRunningWrapper(wrapper,wrappingMethod)) {
                ret = false;
            }
        }
        return ret;
    }

    /**
    * Upcall the beforeRunningWrapper method of all the aspect
    * component that owns the wrapper.
    *
    * @param wrapper the wrapper that has just been runned
    * @param wrappingMethod the name of the runned wrapping method
    * @see AspectComponent#afterRunningWrapper(Wrapper,String)
    * @see BaseProgramListener#afterRunningWrapper(Wrapper,String) */

    public final void afterRunningWrapper(Wrapper wrapper,
                                          String wrappingMethod) {
        AspectComponent ac = wrapper.getAspectComponent();
        if (ac != null) {
            ac.afterRunningWrapper(wrapper, wrappingMethod);
        }
    }

    /**
    * Upcall the beforeRunningWrapper method of all the aspect
    * component that owns the wrapper.
    *
    * @see AspectComponent#afterWrap(Wrappee,Wrapper,String[],String[][]) */

    public final void afterWrap(Wrappee wrappee, Wrapper wrapper, 
                                String[] wrapping_methods, 
                                String[][] wrapped_methods) {
        Object[] acs = orderedObjects.toArray();
        for (int i=0; i<acs.length; i++) {
            AspectComponent curac = (AspectComponent)acs[i];
            curac.afterWrap(wrappee, wrapper, wrapping_methods, wrapped_methods);
        }
    }

    public void whenGetObjects(Collection objects, ClassItem cl)
    {
        Iterator it = orderedObjects.iterator();
        while (it.hasNext()) {
            AspectComponent curac = (AspectComponent)it.next();
            try {
                curac.whenGetObjects(objects,cl);
            } catch (Exception e) {
                loggerAspects.error("whenGetObjects failed on "+curac,e);
            }
        }
    }

    public String whenNameObject(Object object, String name)
    {
        Iterator it = orderedObjects.iterator();
        while (it.hasNext()) {
            AspectComponent curac = (AspectComponent)it.next();
            name = curac.whenNameObject(object,name);
        }
        return name;
    }

    public void getNameCounters(Map counters) {
        Iterator it = orderedObjects.iterator();
        while (it.hasNext()) {
            AspectComponent curac = (AspectComponent)it.next();
            curac.getNameCounters(counters);
        }
    }

    /**
     * Update name counters 
     */
    public void updateNameCounters(Map counters) {
        Iterator it = orderedObjects.iterator();
        while (it.hasNext()) {
            AspectComponent curac = (AspectComponent)it.next();
            curac.updateNameCounters(counters);
        }
    }

    public void whenObjectMiss(String name)
    {
        loggerACM.debug("whenObjectMiss("+name+")");
        Iterator it = orderedObjects.iterator();
        while (it.hasNext()) {
            AspectComponent curac = (AspectComponent)it.next();
            logger.debug("whenObjectMiss("+curac+")");
            curac.whenObjectMiss (name);
        }
    }

    /**
    * Register a new aspect component.
    *
    * <p>When a new aspect component is registered, the
    * <code>allWoven</code> flag is set to false. And the
    * <code>checkWeaving</code> method will be called for each base
    * method call until all the aspect components are woven.
    *
    * @param name the aspect component key for the AC manager
    * @param aspectComponent the registered aspect component 
    * @see AspectComponent#weave()
    */

    public final boolean register(String name, Object aspectComponent) {
        loggerAspects.debug("registering AC "+name);
        Object o = names.get(name);
        if (o != null) {
            // reject if already registered
            return false;
        }
        //registering = true;
        AspectComponent ac = (AspectComponent) aspectComponent;
        //ac.init();
      
        //AspectComponent compAC = (AspectComponent) names.get( 
        //   "JAC_composition_aspect" );
      
        boolean ret = super.register(name, ac);

        loggerAspects.debug("memory objects = "+ObjectRepository.getMemoryObjects());
        Iterator it = ObjectRepository.getMemoryObjects().iterator();
        while( it.hasNext() ) {
            Wrappee cur = (Wrappee) it.next();
            String className = cur.getClass().getName();
            if ( ! ( className.equals("org.objectweb.jac.core.NameRepository") ||
                     className.equals("org.objectweb.jac.core.ACManager") || 
                     className.equals("org.objectweb.jac.core.dist.Topology") ) ) {
                if ( (! className.startsWith("org.objectweb.jac.core")) || ac.isSystemListener() ) {
                    loggerAspects.debug("simulateUsingNewInstance on "+cur.getClass());
                    ac.simulateUsingNewInstance(cur);
                }
            }
        }
        //nonWovenACs.add ( ac );
        //allWoven = false;
        //registering = false;
        return ret;
    }

    /**
    * A resister method that can be used by a UI.
    *
    * <p>This method creates a new aspect component with the name of
    * the given class and registers it. It will be woven immediatly.
    *
    * @param name the aspect component key for the AC manager
    * @param className the aspect component class name
    * @see ACManager#register(String,Object)
    * @see AspectComponent#weave() */

    public final boolean registerWithName(String name, String className) {
        boolean ret = false;
        try {
            ret = register(name, Class.forName(className).newInstance());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
    * Register (if not allready registered) and weaves a new Aspect
    * Component.
    *
    * @param ac the aspect component to weave
    * @see ACManager#register(String,Object)
    * @see AspectComponent#weave() */

    public final void weave(AspectComponent ac) {
        if (names.get(ac) == null) {
            register(ac.toString(),ac);
        }
    }   

    /**
    * Unregister a given aspect component (it is then unwoven). 
    *
    * @param name the aspect component key for the AC manager
    * @see #register(String,Object)
    * @see AspectComponent#unweave() */
   
    public final void unregister(String name) {
        AspectComponent ac = (AspectComponent)objects.get(name);
        if (ac == null) {
            loggerAspects.debug("'"+name+"' aspect component not found. ("+names+")");
            return;
        }
        ac.unweave();
        super.unregister(name);
    }

    // following methods implement the CollaborationParticipant interface

    public final void attrdef( String name, Object value ) {
        Collaboration.get().addAttribute( name, value );
    }
    
    public final Object attr(String name) {
        return Collaboration.get().getAttribute(name);
    }

    /**
    * Reload an aspect for an application
    * @param application the application's name
    * @param aspect the aspect's name
    */
    public void reloadAspect(String application, String aspect) {
        String fullName = application+"."+aspect;
        logger.info("Reloading "+fullName); 
        AspectComponent ac = getACFromFullName(fullName);
        if (ac != null) {
            ApplicationRepository.get().unextend(application,aspect);
            ac.beforeReload();
            ApplicationRepository.get().extend(application,aspect);
            ac.whenReload();
        }
    }

    /**
    * Reload an aspect for the current application
    * @param aspect the aspect's name
    */
    public void reloadAspect(String aspect) throws Exception {
        String application = Collaboration.get().getCurApp();
        if (application==null) {
            throw new Exception("No current application in context");
        }
        reloadAspect(application,aspect);
    }

      /*
   public static Collection getWeavedAspects() {
      String application = Collaboration.get().getCurApp();
      if (application==null) {
         throw new Exception("No current application in context");
      }
      AspectComponent[] aspects = getACM().getAspectComponents();
      Vector weavedAspects = new Vector(aspects.length);
      for (int i=0; i<aspects.length; i++) {
         if (application.equals(aspects[i].getApplication())
             && aspects[i].)
      }
      return weavedAspects;
   }
      */

    public final void beforeWrappeeInit(Wrappee wrappee) {
        Object[] acs = orderedObjects.toArray();
        for (int i = 0; i < acs.length; i++) {
            AspectComponent curac = (AspectComponent)acs[i];
            curac.beforeWrappeeInit(wrappee);
        }
    }

    public final void afterWrappeeInit(Wrappee wrappee) {
        Object[] acs = orderedObjects.toArray();
        for (int i = 0; i < acs.length; i++) {
            AspectComponent curac = (AspectComponent)acs[i];
            curac.afterWrappeeInit(wrappee);
        }
    }

}
