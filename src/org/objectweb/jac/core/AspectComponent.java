/*
  Copyright (C) 2001-2004 Renaud Pawlak <renaud@aopsys.com>

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

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.rtti.*;
import org.objectweb.jac.util.WeakHashSet;
import org.objectweb.jac.util.WrappedThrowableException;

/**
 * This class is the root class for the aspect components defined by a
 * Jac application programmers.
 * 
 * <p>When programming a new aspect, the programmer must extends this
 * class and define new pointcuts to link a set of base method
 * invocation to a wrapping method. If necessary, the programmer can
 * also redefine the methods of the interface
 * <code>BaseProgramListener</code> to be able to react on several
 * events happenning in the base programm such as object
 * (de)serialization, object instantiation, object naming, object
 * cloning, or wrappers execution.
 *
 * @author <a href="mailto:pawlak@cnam.fr">Renaud Pawlak</a>
 *
 * @see Pointcut */

public class AspectComponent 
    implements Serializable, BaseProgramListener 
{
    static Logger logger = Logger.getLogger("aspects");
    static Logger loggerConf = Logger.getLogger("aspects.config");
    static Logger loggerWrap = Logger.getLogger("wrappers");
    static Logger loggerWuni = Logger.getLogger("wuni");
    static Logger loggerPerf = Logger.getLogger("perf");

    protected static final boolean SHARED = false;
    protected static final boolean NOT_SHARED = true;

    protected static final ClassRepository cr =  ClassRepository.get();

    /**
     * Returns true if the method is defined in the aspect component
     * class.
     *
     * @param methodName the method to test
     * @return true if aspect component method */

    public static boolean defines(String methodName) {
        if (ClassRepository.getDirectMethodAccess(AspectComponent.class, 
                                                  methodName )[0] != null)
            return true;
        return false;
    }

    /**
     * A common configuration method that defines a timer and its
     * callback.
     *
     * <p>The method <code>mi</code> is invoked every
     * <code>period</code> ms. The method is defined in the aspect
     * component.
     *
     * @param period the timer's period (in ms)
     * @param callback the method that is called every period 
     * @param args the arguments the callback is called with */

    public void defineTimer(final long period, final MethodItem callback, 
                            final Object[] args) {
        final AspectComponent ac=this;
        new Thread() {
                public void run() {
                    while(true) {
                        try {
                            sleep(period);
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                        callback.invoke(ac,args);
                    }
                }
            }.start(); 
    }

    /**
     * Tell if the aspect component is already woven to the base
     * program.<br>
     *
     * @see #weave() */

    //   public boolean woven = false;

    /** 
     * Memorize how many calls have been performed on
     * <code>start_weaving_type.start_weaving_method</code>. The
     * weaving will start when this number reaches
     * <code>start_weaving_count</code>.<br>
     *
     * @see #weave() */

    public int firstCall = -1;

    /** 
     * The type where the weaving has to start.
     *
     * @see #weave() */
   
    public Class startWeavingType;

    /** 
     * The method where the weaving has to start.<br>
     *
     * @see #weave() 
     */
    public String startWeavingMethod;

    /** 
     * The number of calls of
     * <code>startWeavingType.startWeavingMethod</code> where the
     * weaving has to start.<br>
     *
     * @see #weave() 
     */
    public int startWeavingCount;

    /**
     * The number of instance of <code>startWeavingType</code> where
     * the weaving has to start.<br>
     *
     * @see #weave() 
     */
    public int startWeavingCCount;

    /** 
     * The wrappers handled by this aspect component.<br>
     *
     * @see #getWrappers() 
     * @see #addWrapper(Wrapper) 
     */
    protected transient WeakHashSet wrappers;

    /**
     * Tells if this aspect component is a system listener one so that
     * it can receive events from the org.objectweb.jac.core objects. 
     */
    protected boolean systemListener = false;

    /**
     * Sets this aspect component to be a system or non-system
     * listener.
     *
     * @param systemListener if true, the component can receive events
     * from the JAC core objects (e.g. whenUsingNewInstance) ; if
     * false, these events are filtered 
     */
    public void setSystemListener(boolean systemListener) {
        this.systemListener = systemListener;
    }

    /**
     * Tells if this aspect component is a system listener.
     *
     * @return true if system listener
     * @see #setSystemListener(boolean) 
     */
    public boolean isSystemListener() {
        return systemListener;
    }

    /**
     * The default constructor of the aspect component. By default,
     * there is no start weaving type or method so that the
     * <code>weave()</code> is called as the aspect component is
     * registering to the aspect component manager<br>. 
     *
     * @see #weave()
     * @see ACManager#register(String,Object) 
     */
    public AspectComponent() {   
        logger.debug("New AC: "+this);
        wrappers = new WeakHashSet();
        firstCall = -1;
        startWeavingType = null;
        startWeavingMethod = null;
        startWeavingCount = 0;
        startWeavingCCount = 0;
        init();
        //      Collaboration.get().setCurAC(ACManager.get().getName(this));
    }

    /**
     * Initializes the aspect component so that its state is resetted
     * to the default state.<br> 
     */
    public void init() {      
        //woven = false;
        loggerWrap.debug("clearing wrappers (was "+wrappers+")");
        wrappers.clear();
    }

    protected String application=null;

    /**
     * Gets the application that declares this aspect component.
     * 
     * @return the application's name
     * @see Application
     * @see ApplicationRepository 
     */
    public String getApplication() {
        return application;
    }

    /**
     * Gets the application that declares this aspect component.
     *
     * <p>This method is invoked by the system and should not be
     * directly used.
     * 
     * @param application the application's name
     * @see Application
     * @see ApplicationRepository */

    public void setApplication(String application) {
        this.application = application;
    }

    /**
     * Returns the wrappers handled by this aspect component. When you
     * have a reference on a wrapper, you can also know which aspect
     * component is currently handling it by using
     * <code>Wrapper.getAspectComponent()</code>.<br>
     *
     * @return a vector containing the handdled wrappers
     * @see #addWrapper(Wrapper)
     * @see Wrapper#getAspectComponent() */
   
    public Collection getWrappers() {
        return wrappers;
    }
   
    /**
     * Add a wrapper to the wrappers handled by this aspect
     * component.
     * 
     * <p>The programmer should not call this method explicitly unless
     * s/he really hnows what s/he is doing.  In fact, this method is
     * automatically upcalled when the aspect component wraps a base
     * program object (see <code>Wrappee.wrap()</code>).<br>
     *
     * @param wrapper the wrapper to add to the handdled wrappers
     * @see #getWrappers()
     * @see Wrapping#wrap(Wrappee,Wrapper,AbstractMethodItem) 
     */

    public void addWrapper(Wrapper wrapper) {
        wrappers.add(wrapper);
    }

    /**
     *  The programmer should define this method to wrap all the
     *  currently instantiated Jac objects so that it plugs a given
     *  aspect. Since the objects might allready be wrapped by other
     *  weavers, the programmer should be careful that it does not wrap
     *  the same object with the same wrapper several times. This can
     *  be done in the weave function by using the
     *  <code>Wrappee.isExtendedBy()</code> method. When a new AC is
     *  registered the weave method is automatically called. In the
     *  jac.prop file, the time when it is called can be parametrized
     *  with the org.objectweb.jac.startWeavingPlaces property.
     *
     *  <p>This method is only called once. For the objects that are
     *  created after the weaving, the programmer must define the
     *  <code>whenUsingNewInstance</code> method to wrap them.
     *
     *  <p>The default implementation of the weave method is the
     *  following. In most of the case, it will work if the
     *  <code>whenUsingNewInstance()</code> is correctly defined (thus,
     *  the programer do not have to redefine the weave method).
     *
     *  <ul><pre>
     *  for (int i=0; i < JacObject.objectCount(); i++) {
     *     simulateUsingNewInstance ( (Wrappee)JacObject.getObject(i) );
     *  }
     *  </pre></ul>
     *
     *  <b>IMPORTANT NOTE</b>: this method is not deprecated but the
     *  programmer should not overload it since it is much cleaner to
     *  define the <code>whenUsingNewInstance()</code> to implement the
     *  weaving.<br>
     *
     * @see #whenUsingNewInstance(Interaction)
     * @see #simulateUsingNewInstance(Wrappee)
     * @see Wrapping#isExtendedBy(Wrappee,ClassItem,Class) 
     */

    public void weave() {
        Iterator it = ObjectRepository.getMemoryObjects().iterator();
        while (it.hasNext()) {
            simulateUsingNewInstance((Wrappee)it.next());
        }
    }

    /**
     * This method is called when a new instance (that has not been
     * wrapped by the aspect component yet) is used by a peer
     * object.
     * 
     * <p>By default, this method check out all the pointcuts that have
     * been defined within this aspect component and accordingly wraps
     * the wrappee.
     * 
     * <p>However, for performance or flexibility reasons, the
     * programmer can also define it from scratch so that the aspects
     * can dynamically apply while the base program creates new base
     * objects. The aspect component is notified only once for each new
     * instance.
     * 
     * <p>Here is a typical implementation of this method for a
     * many-to-one wrappees-wrapper relationship:
     *
     * <ul><pre>
     * public class MyAC extends AspectComponent {
     *   public MyWrapper myWrapper = null;
     *   public void whenUsingNewInstance() {
     *     // creates the sole instance of MyWrapper
     *     if ( myWrapper == null ) myWrapper = new MyWrapper();
     *     // make sure we do not wrap an object several times 
     *     // (this is usually not useful!!)
     *     if ( wrappee().isExtendedBy( MyWrapper.class ) ) return;
     *     // Do not wrap system or aspect objects
     *     if ( wrappee().getClass().getName().startsWith( "org.objectweb.jac.core." ) ||
     *          wrappee().getClass().getName().startsWith( "org.objectweb.jac.aspects." ) ) 
     *       return;
     *     // wrap it...
     *     wrappee().wrapAll( myWrapper, "myWrappingMethod" );
     *   }
     * }
     * </pre></ul>
     *
     * <p>The same but with one-to-one wrappees-wrappers
     * relationship:
     *
     * <ul><pre>
     * public class MyAC extends AspectComponent {
     *   public void whenUsingNewInstance() {
     *     // make sure we do not wrap an object several times 
     *     // (this is usually not useful!!)
     *     if ( wrappee().isExtendedBy( MyWrapper.class ) ) return;
     *     // Do not wrap system or aspect objects
     *     if ( wrappee().getClass().getName().startsWith( "org.objectweb.jac.core." ) ||
     *          wrappee().getClass().getName().startsWith( "org.objectweb.jac.aspects." ) ) 
     *       return;
     *     // one wrapper for each new instance
     *     MyWrapper myWrapper = new MyWrapper();
     *     // wrap it...
     *     wrappee().wrapAll( myWrapper, "myWrappingMethod" );
     *   }
     * }
     * </pre></ul>
     *
     * <b>NOTE</b>: this method is not upcalled until the aspect component
     * is woven.<br>
     * 
     * @see #weave() 
     */
    public void whenUsingNewInstance(Interaction interaction) {
        //if(treatedInstances.contains(wrappee())) return;
        //treatedInstances.add(wrappee());
        ClassItem cli = interaction.getClassItem();
        //Log.trace("wuni",this+": "+method());
        AbstractMethodItem method = interaction.method;

        loggerWuni.debug("whenUsingNewInstance("+interaction+"); pointcuts: "+pointcuts.size());
        int i = 0;
        Iterator it = pointcuts.iterator();
        while (it.hasNext()) {
            loggerWuni.debug("  pointcut "+i++);
            ((Pointcut)it.next()).applyTo(interaction.wrappee, cli);
        }
    }

    public void whenUsingNewClass(ClassItem cli) {
        Iterator it = pointcuts.iterator();
        int i = 0;
        loggerWuni.debug(this+".whenUsingNewClass"+cli);
        while (it.hasNext()) {
            ((Pointcut)it.next()).applyTo(null, cli);
        }
    }

    /**
     * This method simulates an instance first use so that the system
     * upcalls <code>whenUsingNewInstance()</code> with a fake
     * collaboration point (<code>method()</code> is null and
     * <code>args</code> is null too).
     *
     * @param wrappee the wrappe on which the call is simulated.
     * @see #whenUsingNewInstance(Interaction) */

    protected void simulateUsingNewInstance(Wrappee wrappee) {
        /** simulates a new interaction */
        //      Log.trace("core","simulateUsingNewInstance "+this);
        Collaboration collab = Collaboration.get();
        //collab.newInteraction();
        whenUsingNewInstance(new Interaction(null,wrappee,null,null));
        /** end of simulated interaction */
        //collab.endOfInteraction();
    }

    /**
     * This method is called when a new instance is created.
     * 
     * <p>Since the instance is not initialized yet, the aspect
     * component must not wrap it at this stage. To wrap a new
     * instance, the aspect component must use the
     * <code>whenUsingNewInstance()</code> method. This method is not
     * upcalled when the instantiation is remotly performed (from a
     * deployer site), see the <code>whenRemoteInstantiation()</code>
     * method.<br>
     *
     * @param newInstance the newly created instance
     * @see #whenUsingNewInstance(Interaction)
     * @see #whenRemoteInstantiation(Wrappee,String) */

    //public void whenNewInstance( Wrappee newInstance ) {}

    /**
     * This method is upcalled when a new object is instanciated from a
     * remote site.
     *
     * <p>The name that is passed is the name of the remote
     * reference that has been used to create the object. Typically, it
     * is the name that will be used by the naming aspect to name the
     * new object.
     * 
     * @param newInstance the instance that have been created by a
     * remote host
     * @param name the name of the new instance
     * @see org.objectweb.jac.aspects.naming.NamingAC#whenRemoteInstantiation(Wrappee,String) 
     */
    public void whenRemoteInstantiation(Wrappee newInstance,
                                        String name) {}

    /**
     * This method unwraps all the instances that have been wrapped by
     * the aspect component.
     * 
     * <p>This is done in a generic way that is not very efficient. To
     * optimize this process, the user may overload the
     * <code>unweave()</code> method to change its default
     * behavior.<br>
     *
     * @see #unweave() 
     */
    protected final void unwrapAll() {
        loggerWrap.debug("unwrap all");
        loggerWrap.debug(wrappers.size()+" wrappers: " + wrappers);
        Iterator it = ObjectRepository.getMemoryObjects().iterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof Wrappee) {
                Wrappee wrappee = (Wrappee)o;
                ClassItem wrappeeClass = cr.getClass(wrappee);
                loggerWrap.debug("testing wrappee " + wrappee);
                Wrapping.unwrap(wrappee,wrappeeClass,wrappers);
                Wrapping.unwrap(null,wrappeeClass,wrappers);
            }
        }
        wrappers.clear();
    }

    /**
     * The programmer should overload this method to unwrap all the
     * currently wrapped Jac objects so that it unplugs a given aspect.
     * 
     * <p>By default, calls the <code>unwrapAll</code> method but the
     * programmer can implement the unveaving process in a more
     * efficient way.<br>
     *
     * @see #unwrapAll() */

    public void unweave() {
        logger.info("--- unweaving "+this+" ---");
		long start = System.currentTimeMillis();        
        unwrapAll();
        pointcuts.clear();
        loggerPerf.info("unweaved "+this+" in "+(System.currentTimeMillis()-start)+"ms");
    }

    /**
     * This method is automatically called when a Jac Object is
     * cloned. */

    public void whenClone(Wrappee cloned, Wrappee clone) {}

    /**
     * This method is called when a JAC object is serialized and can
     * parametrize the serialization by filling the <code>finalObject</code>
     * parameter.
     *
     * <p><b>IMPORTANT</b>: this method is upcalled only if the
     * serialization is done with a
     * <code>JacObjectOutputStream</code>. To ensure the correct use of
     * this class, only use <code>JacObject.serialize()</code> to
     * serialize an object.</p>
     *
     * @param orgObject the object being serialized
     * @param finalObject the corresponding serialized structure
     * @return the object being serialized (usually orgObject, but not
     * necessarily).
     * @see SerializedJacObject
     * @see JacObjectOutputStream */

    public Wrappee whenSerialized(Wrappee orgObject, 
                                  SerializedJacObject finalObject) {
        return orgObject;
    }

    /**
     * This method is called when a base object is deserialized and can
     * parametrize the deserialization by reading the
     * SerializedJacObject instance to get some extra infos on the
     * aspects.
     *
     * <p><b>IMPORTANT</b>: this method is upcalled only if the
     * deserialization is done with a
     * <code>JacObjectInputStream</code>. To ensure the correct use of
     * this class, only use <code>JacObject.deserialize()</code> to
     * deserialize an object.
     *
     * @param orgObject the corresponding serialized structure
     * @param finalObject the object being deserialized
     * @return the object being deserialized (usually finalObject but
     * not necessarily)
     * @see SerializedJacObject
     * @see JacObjectInputStream */

    public Wrappee whenDeserialized(SerializedJacObject orgObject, 
                                    Wrappee finalObject) {
        return finalObject;
    }


    public void onExit() {}

    /**
     * This method is called when a wrapper is going to be applied to a
     * wrappee.
     *
     * <p>By overloading this method, the aspect component can skip the
     * wrapper, for instance if the current collaboration shows that
     * the wrapper has allready been applied.<br>
     *
     * <p>If the method returns true, then the wrapper is run, else it
     * is skipped. Default: return true.<br>
     *
     * @param wrapper the wrapper that is going to be runned
     * @param wrappingMethod the name of the may-be runned wrapping
     * method
     * @return a boolean that tells if the wrapper has to be runned
     * (true) or not (false)
     * @see Wrappee
     * @see Wrapping#wrap(Wrappee,Wrapper,AbstractMethodItem)
     * @see Wrapper
     * @see Wrapper#proceed(Invocation) 
     */
    public boolean beforeRunningWrapper(Wrapper wrapper, 
                                        String wrappingMethod) {
        return true;
    }
   
    /**
     * This method is called after the application of the
     * wrapper.
     *
     * <p>Typically, the aspect component can set an attribute to
     * the collaboration that indicates that the wrapper has been
     * applied (and that can be used by the beforeRunningWrapper
     * method).
     * 
     * @param wrapper the wrapper that has just been runned
     * @param wrappingMethod the name of the runned wrapping method
     * @see Wrappee
     * @see Wrapping#wrap(Wrappee,Wrapper,AbstractMethodItem)
     * @see Wrapper
     * @see Wrapper#proceed(Invocation) 
     */
    public void afterRunningWrapper(Wrapper wrapper, 
                                    String wrappingMethod) {}

    /**
     * This method is called after a new wrapper wraps a wrappee.
     */

    public void afterWrap(Wrappee wrappee, Wrapper wrapper,
                          String[] wrapping_methods,
                          String[][] wrapped_methods) {}
   
    /**
     * This method is called when a program gets a set of object from
     * the object repository.
     *
     * <p>At this step, the aspect can change the returned list so that
     * the program will see the right set of objects (for instance a
     * security aspect may filter this set so that the user will not
     * see the objects s/he is not allowed to see or a persistance
     * aspect may force the load of a set of objects from the storage
     * and add them to the list).
     *
     * @param objects the returned list (can be modified)
     * @param cl the class that was used by the program to filter its
     * instances (can be null for all the objects)
     * @see ObjectRepository */

    public void whenGetObjects(Collection objects, ClassItem cl) {}

    public String whenNameObject(Object object,String name) { 
        return name;
    }

    public void getNameCounters(Map counters) {
    }

    public void updateNameCounters(Map counters) {
    }

    /**
     * This method is upcalled by JAC when an object was seeked into
     * the name repository and was not found.
     *
     * <p>The reason of this miss can be multiple. For instance, the
     * persistence aspect may have not already load the object from the
     * storage, or the distribution aspect may need to bind to a remote
     * object. Thus, this method allows the aspects to resolve the
     * object.
     *
     * <p>By default, this method does nothing.
     *
     * <p>The final choosen name is a contextual attribute called
     * FOUND_OBJECT.
     *
     * @param name the name of the object 
     * @see BaseProgramListener#FOUND_OBJECT
     */
   
    public void whenObjectMiss(String name) {}

    public void whenDeleted(Wrappee object) {}

    public void whenFree(Wrappee object) {}

    public void afterApplicationStarted() {}

    public void whenCloseDisplay(Display display) {}

    public void whenTopologyChanged() {}

    /**
     * Called when the aspect's configuration is reloaded
     */
    public void whenReload() {}
    public void beforeReload() {}

    /**
     * This method should be defined by the programmer when specific
     * actions are needed once the aspect component has be
     * configured. */

    public void whenConfigured() {}

    /**
     * To overload in order to perform some treatements before the
     * configuration. */
    public void beforeConfiguration() throws Exception {
    }

    public String getName() {
        return ACManager.getACM().getName(this);
    }

    /**
     * This method is upcalled by the system when the aspect is
     * actually registered in the AC manager.
     *
     * <p>Here it does nothing. */
    public void doRegister() {
    }

    /**
     * This method is upcalled by the system when the aspect is
     * actually registered in the AC manager.
     *
     * <p>Here it does nothing. */
    public void doUnregister() {
    }

    /** Store the pointcuts. */
    Vector pointcuts = new Vector();

    /**
     * Defines and adds a new method pointcut.
     *
     * <p>For more details on how to use pointcuts, see the
     * <code>MethodPointcut</code> class.
     *
     * @param wrappeeExpr the wrappee definition that matches the names
     * as defined by the naming aspect
     * @param wrappeeClassExpr the wrappee class expression (matches
     * the fully qualified class name)
     * @param wrappeeMethodExpr the wrappee method expression (matches
     * the full method name as defined by the rtti)
     * @param wrappingClassName the name of the wrapper class
     * @param one2one true if each new wrapped instance corresponds to
     * one different wrapper
     * @see MethodPointcut 
     */
    public Pointcut pointcut(String wrappeeExpr, 
                             String wrappeeClassExpr, 
                             String wrappeeMethodExpr,
                             String wrappingClassName,
                             String exceptionHandler,
                             boolean one2one) {

        MethodPointcut pc = new MethodPointcut( this,
                                                wrappeeExpr, 
                                                wrappeeClassExpr, 
                                                wrappeeMethodExpr,
                                                null,
                                                wrappingClassName,
                                                null,
                                                null,
                                                "ALL",
                                                exceptionHandler,
                                                one2one );
        pointcuts.add(pc);
        return pc;
    }

    /**
     * Defines and adds a new method pointcut.
     *
     * <p>For more details on how to use pointcuts, see the
     * <code>MethodPointcut</code> class.
     *
     * @param wrappeeExpr the wrappee definition that matches the names
     * as defined by the naming aspect
     * @param wrappeeClassExpr the wrappee class expression (matches
     * the fully qualified class name)
     * @param wrappeeMethodExpr the wrappee method expression (matches
     * the full method name as defined by the rtti)
     * @param wrappingClassName the name of the wrapper class
     * @param initParameters the initialization parameters of the
     * wrapper (passed to the wrappers constructor that matches the
     * parameters types)
     * @param one2One true if each new wrapped instance corresponds to
     * one different wrapper
     * @see MethodPointcut 
     */
    public Pointcut pointcut(String wrappeeExpr, 
                             String wrappeeClassExpr, 
                             String wrappeeMethodExpr,
                             String wrappingClassName,
                             Object[] initParameters,
                             String exceptionHandler,
                             boolean one2One) {

        MethodPointcut pc = new MethodPointcut( this,
                                                wrappeeExpr, 
                                                wrappeeClassExpr, 
                                                wrappeeMethodExpr,
                                                null,
                                                wrappingClassName,
                                                null,
                                                initParameters,
                                                "ALL",
                                                exceptionHandler,
                                                one2One );
        pointcuts.add(pc);
        return pc;
    }

    /**
     * Defines and adds a new localized method pointcut.
     *
     * <p>For more details on how to use pointcuts, see the
     * <code>MethodPointcut</code> class.
     *
     * @param wrappeeExpr the wrappee definition that matches the names
     * as defined by the naming aspect
     * @param wrappeeClassExpr the wrappee class expression (matches
     * the fully qualified class name)
     * @param wrappeeMethodExpr the wrappee method expression (matches
     * the full method name as defined by the rtti)
     * @param wrappingClassName the name of the wrapper class
     * @param hostExpr a regular expression that macthes the hosts
     * where the pointcut has to be applied (if null or empty string,
     * default is ".*" which means that the pointcut will be applied on
     * all the hosts)
     * @param one2One true if each new wrapped instance corresponds to
     * one different wrapper
     * @see MethodPointcut */

    public Pointcut pointcut(String wrappeeExpr, 
                             String wrappeeClassExpr, 
                             String wrappeeMethodExpr,
                             String wrappingClassName,
                             String hostExpr,
                             String exceptionHandler,
                             boolean one2One) {

        MethodPointcut pc = new MethodPointcut(this,
                                               wrappeeExpr, 
                                               wrappeeClassExpr, 
                                               wrappeeMethodExpr,
                                               null,
                                               wrappingClassName,
                                               null,
                                               null,
                                               hostExpr,
                                               exceptionHandler,
                                               one2One);
        pointcuts.add(pc);
        return pc;
    }

    /**
     * Defines and adds a new localized method pointcut.
     *
     * <p>For more details on how to use pointcuts, see the
     * <code>MethodPointcut</code> class.
     *
     * @param wrappeeExpr the wrappee definition that matches the names
     * as defined by the naming aspect
     * @param wrappeeClassExpr the wrappee class expression (matches
     * the fully qualified class name)
     * @param wrappeeMethodExpr the wrappee method expression (matches
     * the full method name as defined by the rtti)
     * @param wrappingClassName the name of the wrapper class
     * @param initParameters the initialization parameters of the
     * wrapper (passed to the wrappers constructor that matches the
     * parameters types)
     * @param hostExpr a regular expression that macthes the hosts
     * where the pointcut has to be applied (if null or empty string,
     * default is ".*" which means that the pointcut will be applied on
     * all the hosts)
     * @param one2One true if each new wrapped instance corresponds to
     * one different wrapper
     * @see MethodPointcut 
     */
    public Pointcut pointcut(String wrappeeExpr, 
                             String wrappeeClassExpr, 
                             String wrappeeMethodExpr,
                             String wrappingClassName,
                             Object[] initParameters,
                             String hostExpr,
                             String exceptionHandler,
                             boolean one2One) {
      
        MethodPointcut pc = new MethodPointcut(this,
                                               wrappeeExpr, 
                                               wrappeeClassExpr, 
                                               wrappeeMethodExpr,
                                               null,
                                               wrappingClassName,
                                               null,
                                               initParameters,
                                               hostExpr,
                                               exceptionHandler,
                                               one2One);
        pointcuts.add(pc);
        return pc;
    }

    /**
     * Defines and adds a new method pointcut.
     *
     * <p>For more details on how to use pointcuts, see the
     * <code>MethodPointcut</code> class.
     *
     * @param wrappeeExpr the wrappee definition that matches the names
     * as defined by the naming aspect
     * @param wrappeeClassExpr the wrappee class expression (matches
     * the fully qualified class name)
     * @param wrappeeMethodExpr the wrappee method expression (matches
     * the full method name as defined by the rtti)
     * @param wrapper the wrapper that contains the wrapping method,
     * cannot be null
     *
     * @see MethodPointcut 
     */
    public Pointcut pointcut(String wrappeeExpr, 
                             String wrappeeClassExpr, 
                             String wrappeeMethodExpr,
                             Wrapper wrapper,
                             String exceptionHandler) {

        MethodPointcut pc = new MethodPointcut(this,
                                               wrappeeExpr, 
                                               wrappeeClassExpr, 
                                               wrappeeMethodExpr,
                                               wrapper,
                                               wrapper.getClass().getName(),
                                               null,
                                               null,
                                               "ALL",
                                               exceptionHandler,
                                               false);
        pointcuts.add(pc);
        return pc;
    }

    /**
     * Defines and adds a new localized method pointcut.
     *
     * <p>For more details on how to use pointcuts, see the
     * <code>MethodPointcut</code> class.
     *
     * @param wrappeeExpr the wrappee definition that matches the names
     * as defined by the naming aspect
     * @param wrappeeClassExpr the wrappee class expression (matches
     * the fully qualified class name)
     * @param wrappeeMethodExpr the wrappee method expression (matches
     * the full method name as defined by the rtti)
     * @param wrapper the wrapper that contains the wrapping method,
     * cannot be null
     * @param hostExpr a regular expression that macthes the hosts
     * where the pointcut has to be applied (if null or empty string,
     * default is ".*" which means that the pointcut will be applied on
     * all the hosts)
     *
     * @see MethodPointcut 
     */
    public Pointcut pointcut(String wrappeeExpr, 
                             String wrappeeClassExpr, 
                             String wrappeeMethodExpr,
                             Wrapper wrapper,
                             String hostExpr,
                             String exceptionHandler) {

        MethodPointcut pc = new MethodPointcut(this,
                                               wrappeeExpr, 
                                               wrappeeClassExpr, 
                                               wrappeeMethodExpr,
                                               wrapper,
                                               wrapper.getClass().getName(),
                                               null,
                                               null,
                                               hostExpr,
                                               exceptionHandler,
                                               false);
        pointcuts.add(pc);
        return pc;
    }

    /**
     * Defines and adds a new pointcut that upcalls an aspect method
     * when the matching object is created.
     *
     * <p>For more details on how to use pointcuts, see the
     * <code>MethodPointcut</code> class.
     *
     * @param wrappeeExpr the wrappee definition that matches the names
     * as defined by the naming aspect
     * @param wrappeeClassExpr the wrappee class expression (matches
     * the fully qualified class name)
     * @param methodName a method of the aspect component to call
     * @param methodArgs the arguments of this methods when called
     * @see MethodPointcut */

    public Pointcut pointcut(String wrappeeExpr, 
                             String wrappeeClassExpr, 
                             String methodName,
                             Object[] methodArgs,
                             String exceptionHandler) {

        MethodPointcut pc = new MethodPointcut(this,
                                               wrappeeExpr, 
                                               wrappeeClassExpr, 
                                               null,
                                               null,
                                               null,
                                               methodName,
                                               methodArgs,
                                               "ALL",
                                               exceptionHandler,
                                               false);
        pointcuts.add(pc);
        return pc;
    }

    /**
     * Defines and adds a new localized pointcut that upcalls an aspect
     * method when the matching object is created.
     *
     * <p>For more details on how to use pointcuts, see the
     * <code>MethodPointcut</code> class.</p>
     *
     * @param wrappeeExpr the wrappee definition that matches the names
     * as defined by the naming aspect
     * @param wrappeeClassExpr the wrappee class expression (matches
     * the fully qualified class name)
     * @param methodName a method of the aspect component to call
     * @param methodArgs the arguments of this methods when called
     * @see MethodPointcut */

    public Pointcut pointcut(String wrappeeExpr, 
                             String wrappeeClassExpr, 
                             String methodName,
                             Object[] methodArgs,
                             String hostExpr,
                             String exceptionHandler) {

        MethodPointcut pc = new MethodPointcut(this,
                                               wrappeeExpr, 
                                               wrappeeClassExpr, 
                                               null,
                                               null,
                                               null,
                                               methodName,
                                               methodArgs,
                                               hostExpr,
                                               exceptionHandler,
                                               false);
        pointcuts.add(pc);
        return pc;
    }
 
    /**
     * Generic config method to set an attribute on a class
     * @param cli the class to set an attribute on
     * @param name name of the attribute
     * @param value string value of the attribute
     */
    public void setAttribute(ClassItem cli, String name, String value) {
        cli.setAttribute(name,value);
    }

    /**
     * Generic config method to set an attribute on a class
     * @param field the field to set an attribute on
     * @param name name of the attribute
     * @param value string value of the attribute
     */
    public void setAttribute(FieldItem field, String name, String value) {
        field.setAttribute(name,value);
    }


    /**
     * Generic config method to set an attribute on a method
     * @param method the method to set an attribute on
     * @param name name of the attribute
     * @param value string value of the attribute
     */
    public void setAttribute(AbstractMethodItem method, 
                             String name, String value) {
        method.setAttribute(name,value);
    }

    // following methods implement the CollaborationParticipant interface

    public final void attrdef(String name, Object value) {
        Collaboration.get().addAttribute( name, value );
    }

    public final Object attr( String name ) {
        return Collaboration.get().getAttribute( name );
    }

    /** Block keywords which can be used instead of "block" */
    protected String[] blockKeywords = new String[0];

    public Set getBlockKeywords() {
        return new HashSet(Arrays.asList(blockKeywords));
    }

    /** Returns defaults configuration files that must be loaded before
        the user's configuration */
    public String[] getDefaultConfigs() {
        return new String[0];
    }

    /**
     * Returns all the configuration methods of the aspect
     * @return Collection of MethodItem
     */
    public Collection getConfigurationMethods() {
        Vector result = new Vector();
        Iterator ms = cr.getClass(getClass()).getAllMethods().iterator();

        while(ms.hasNext()){
            MethodItem methodItem=(MethodItem)ms.next();
            if (isConfigurationMethod(methodItem.getActualMethod())){
                result.add(methodItem);
            }
        }
        return result;
    }

    /**
     * Returns all the name of the configuration methods of the aspect
     * @return Collection of String
     */
    public Collection getConfigurationMethodsName() {
        Vector result = new Vector();
        Method[] ms = getClass().getMethods();
        for(int i=0;i<ms.length;i++) {
            if (isConfigurationMethod(ms[i])) {
                result.add(ms[i].getName());
            }
        }
        return result;
    }


    /**
     * Returns all the configuration methods of the aspect whose first
     * parameter is compatible with a given type.
     * @param firstParamType the type the first parameter must be compatible with
     * @return a collection of MethodItem with at least one parameter,
     * and whose first parameter can be of type firstParamType or a
     * subclass of firstParamType */
    public List getConfigurationMethodsName(Class firstParamType) {
        Vector result = new Vector();
        Iterator ms = cr.getClass(getClass())
            .getAllMethods().iterator();
        // We use hash set to remove duplicate entries quickly
        HashSet names = new HashSet();

        while (ms.hasNext()) {
            MethodItem methodItem = (MethodItem)ms.next();
            if (isConfigurationMethod(methodItem.getActualMethod())) {
                Class[] paramTypes = methodItem.getParameterTypes();
                if (paramTypes.length>0 &&
                    (paramTypes[0].isAssignableFrom(firstParamType) || 
                     firstParamType.isAssignableFrom(paramTypes[0])) &&
                    !names.contains(methodItem.getName())) {
                    result.add(methodItem.getName());
                    names.add(methodItem.getName());
                }
            }
        }
        return result;
    }


    /**
     * Tells wether a method is configuration method of this aspect
     * @param method
     * @return true if the method was a configuration method.
     */
    public boolean isConfigurationMethod(Method method) {
        Class cl = method.getDeclaringClass();
        if (AspectComponent.class.isAssignableFrom(cl)) {
            Class[] interfs=cl.getInterfaces();
            for(int i=0;i<interfs.length;i++) {
                if (!interfs[i].getName().endsWith("Conf")) 
                    continue;
                Method[] ms = interfs[i].getMethods();
                for(int j=0; j<ms.length; j++) {
                    if (ms[j].getName().equals(method.getName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }   

    protected ConfigMethod currentConfigMethod;
    protected Imports currentImports;

    /**
     * Configures this aspect component with a given configuration file. 
     *
     * @param name name of the aspect component (informative)
     * @param filePath path to a resource of file containing the configuration
     */
    public void configure(String name, String filePath) {
        long start = System.currentTimeMillis();
        if (filePath == null) {
            return;
        }
        loggerConf.info("configuring aspect " + name + " with file "+filePath); 
        List configMethods = null;
        try {
            // Naming.PARSER_NAME ("parserimpl#0") is the only
            // instance of ACParser, if we are on the master site, it
            // is automatically instantiated by the binding aspect, if
            // not, the binding aspect creates a stub for
            // parserimpl#0@mastersite
            Parser acp = (Parser)NameRepository.get().getObject(Naming.PARSER_NAME);
            configMethods = acp.parse(filePath,this.getClass().getName(),
                                      this.getBlockKeywords());
        } catch (FileNotFoundException e) {
            logger.warn("cannot find config file "+filePath+" for "+name+
                        "("+e+"), using serialized infos.");
        } catch (Exception e) {
            logger.error("configure "+name+","+filePath,e);
            return;
        }
        if (configMethods==null)
            return;
        currentImports = new Imports();
        for (int i=0; i<configMethods.size(); i++) {
            MethodItem method = null;
            Object statement = configMethods.get(i);
            if (statement instanceof ImportStatement) {
                ImportStatement imp = (ImportStatement)statement;
                currentImports.add(imp.getExpr());
                continue;
            }
            ConfigMethod cm = (ConfigMethod)statement;
            currentConfigMethod = cm;
            if (cm==null) {
                loggerConf.warn("skipping error");
                continue;
            }
            try {
                loggerConf.debug(
                    "invoking configuration method '"+cm.getMethod()+
                    "' on "+this.getClass()+" with "+Arrays.asList(cm.getArgs()));
                if (cm.getMethod().equals("")) 
                    continue;
                MethodItem[] methodItems =
                    cr.getClass(this.getClass()).
                    getMethods(cm.getMethod());
                if (methodItems==null || methodItems.length==0) {
                    loggerConf.warn("No such configuration method "+cm.getMethod()+
                                    " in AC "+this.getClass());
                }
                loggerConf.debug(
                    "matching methods : "+Arrays.asList(methodItems));

                Object[] paramValues = null;
                //determine which method to call based on number of parameters
                // Warning, ClassItem+String can become a MemberItem
                Vector exceptions = new Vector();
                for(int j=0; j<methodItems.length && method==null; j++) {
                    paramValues = cm.getArgs();
                    //If the ConfigMethod has the same number of param as MethodItem
                    if (methodItems[j].getParameterTypes().length==paramValues.length) {
                        method = methodItems[j];
                        loggerConf.debug("selecting method "+method);

                        // Try to convert the parameters
                        // if it fails, try the next method
                        paramValues = cm.getArgs();
                        try {
                            Class[] paramTypes = method.getParameterTypes();
                            for(int k=0; k<paramTypes.length; k++) {
                                paramValues[k] = 
                                    ACConfiguration.convertValue(
                                        paramValues[k], 
                                        paramTypes[k], 
                                        currentImports);
                            }
                            break;
                        } catch(Exception e) {
                            exceptions.add(e);
                            method = null;
                        }
                    }
                    //If method==null try to translate MemberItem parameters
                    //into ClassItem+String
                    if (method==null && 
                        (methodItems[j].getParameterCount()>0)) {
                        for (int p=0; p<methodItems[j].getParameterCount(); p++) {
                            if ((MemberItem.class.isAssignableFrom(
                                methodItems[j].getParameterTypes()[p])) 
                                && (methodItems[j].getParameterTypes().length
                                    == paramValues.length-1)) 
                            {
                                method = methodItems[j];
                                loggerConf.debug(
                                    "selecting method "+method+
                                    " with MemberItem=ClassItem+String for param #"+p);
                                paramValues = cm.getArgs();
                                Object paramValuesTranslated[] = new Object[paramValues.length-1];
                                try {
                                    Class[] paramTypes = method.getParameterTypes();
                                    ClassItem classItem = 
                                        (ClassItem)ACConfiguration.convertValue(
                                            paramValues[p],
                                            ClassItem.class, 
                                            currentImports);
                                    paramValuesTranslated[p] =
                                        classItem.getMember(
                                            (String)ACConfiguration.convertValue(
                                                paramValues[p+1],
                                                String.class,
                                                currentImports));
                                    if (!paramTypes[p].isAssignableFrom(
                                            paramValuesTranslated[p].getClass())) 
                                    {
                                        throw new Exception(
                                            "Error translating parameter: "+
                                            paramValuesTranslated[0].getClass().getName()+
                                            " can't be assigned to "+paramTypes[p].getName());
                                    }
                                    for(int k=p+1; k<paramTypes.length; k++){
                                        paramValuesTranslated[k] = 
                                            ACConfiguration.convertValue(
                                                paramValues[k+1], 
                                                paramTypes[k],
                                                currentImports);
                                    }

                                    for(int k=0; k<p; k++){
                                        paramValuesTranslated[k] = 
                                            ACConfiguration.convertValue(
                                                paramValues[k], 
                                                paramTypes[k],
                                                currentImports);
                                    }
                                    paramValues = paramValuesTranslated;
                                    loggerConf.debug("Parameter transformation successful");
                                    break;
                                } catch (Exception e) {
                                    loggerConf.debug("Exception in parameter transformation: "+e);
                                    exceptions.add(e);
                                    method = null;
                                }
                            }
                        }
                    }
               
                }
                if (method == null) {
                    if (exceptions.isEmpty()) {
                        throw new Exception("Wrong number of parameters for method "+
                                            cm.getMethod());
                    } else {
                        throw (Exception)exceptions.get(0);
                        //new Exception(exceptions.toString());
                    }
                }
            
                loggerConf.debug(
                    filePath+": line "+cm.getLineNumber() +
                    ", invoke configuration method '" + 
                    (method!=null?method.toString():cm.getMethod()) +
                    "' on " +  this.getClass() + 
                    " with " + Arrays.asList(cm.getArgs()));
                method.invoke(this,paramValues);

            } catch (InvocationTargetException e) {
                Throwable target = e.getTargetException();
                loggerConf.warn(cm.getLineNumber() +
                                ", failed to invoke configuration method '"+
                                (method!=null?method.getFullName():cm.getMethod())+
                                "' on "+this.getClass() + " with "+
                                Arrays.asList(cm.getArgs()),target);
                loggerConf.info("Stack trace follows",e);
            } catch (Throwable e) {
                if (e instanceof WrappedThrowableException)
                    e = ((WrappedThrowableException)e).getWrappedThrowable();
                loggerConf.warn(cm.getLineNumber() +
                                ", failed to invoke configuration method '"+ 
                                (method!=null?method.getFullName():cm.getMethod())+
                                "' on "+this.getClass()+
                                " with "+Arrays.asList(cm.getArgs())+" : "+e);
                loggerConf.info("Stack trace follows",e);
            } finally {
                currentConfigMethod = null;
            }
        }
        loggerPerf.debug(
            "aspect configuration file "+filePath+" read in "+
            (System.currentTimeMillis()-start)+"ms");
    }

    public void beforeWrappeeInit(Wrappee wrappee) {};

    public void afterWrappeeInit(Wrappee wrappee) {};

    /**
     * Issue a warning containing the file, line number and
     * configuration method name (if available)
     *
     * @param message the warning message to print 
     * @see #error(String)
     */
    protected void warning(String message) {
        if (currentConfigMethod!=null)
            loggerConf.warn(
                currentConfigMethod.getLineNumber()+":"+
                currentConfigMethod.getMethod()+": "+message);
        else
            loggerConf.warn(message);
    }

    /**
     * Issue an error containing the file, line number and
     * configuration method name (if available)
     *
     * @param message the warning message to print 
     * @see #warning(String)
     */
    protected void error(String message) {
        if (currentConfigMethod!=null)
            loggerConf.error(
                currentConfigMethod.getLineNumber()+":"+
                currentConfigMethod.getMethod()+": "+message);
        else
            logger.error(message);
    }

    /**
     * Returns a named aspect component in the same application
     */
    protected AspectComponent getAC(String name) {
        return ACManager.getACM().getACFromFullName(application+"."+name);
    }
}
