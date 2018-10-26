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


import java.io.Serializable;
import java.lang.reflect.Array;
import java.net.URL;
import org.apache.log4j.Logger;
import org.objectweb.jac.aspects.gui.Length;
import org.objectweb.jac.core.rtti.*;
import org.objectweb.jac.util.Classes;
import org.objectweb.jac.util.Strings;

/**
 * This class defines aspect component configurations so that the
 * programmer or the system administrator will be able to configure
 * the available aspects for a given application.
 *
 * <p>The default available aspects in the system are the ones that
 * are declared to the AC manager in the <code>jac.prop</code> (see
 * the <code>org.objectweb.jac.acs</code> property).
 *
 * @see ApplicationRepository
 * @see Application
 * @see Parser
 * @see ACManager
 */

public class ACConfiguration implements Serializable {
    static Logger logger = Logger.getLogger("jac");
    static Logger loggerAspects = Logger.getLogger("aspects");
    static Logger loggerConf = Logger.getLogger("aspects.config");
    static Logger loggerPerf = Logger.getLogger("perf");

    /** The application this configuration belongs to. */
    protected Application application;
    /** The name of the configured AC. */
    protected String name;
    /** The aspect component that corresponds to this configuration
        once instantiated (null before). This is a local aspect
        component. */
    transient AspectComponent instance = null;
   
    /** The configuration file's URL. */
    protected URL filePath;

    String acPath = null;

    /**
     * Sets the URL of the configuration file that defines the
     * configuration operations.
     *
     * @param filePath a valid file path
     * @see #getURL() */

    public void setURL(URL filePath) {
        this.filePath = filePath;
    }

    /**
     * The getter of the configuration file's URL.
     *
     * @return the URL
     * @see #setURL(URL) */

    public URL getURL() {
        return filePath;
    }

    /**
     * This flag tells if the aspect that is configured by the current
     * configuration will be woven on demand (by the administrator or
     * by a configuration program) or if the aspect will be
     * automatically woven and restored by the system.
     *
     * <p>For instance, a persistence aspect should always have this
     * configuration flag to false whilst a debugging aspect should
     * most of the time be woven on demand (when debugging is
     * needed). */

    protected boolean weaveOnDemand = true;
   
    /**
     * Gets the <code>weaveOnDemand</code> flag value.
     * @return the flag value */

    public boolean getWeaveOnDemand() {
        return weaveOnDemand;
    }
   
    /**
     * Sets the <code>weaveOnDemand</code> flag value.
     * @param b the new flag value */

    public void setWeaveOnDemand(boolean b) {
        weaveOnDemand = b;
    }

    /**
     * Creates a new aspect component configuration.
     *
     * @param application the application this configuration belongs to 
     * @param name the name of the AC as defined in the declared ACs of
     * the AC manager, or the of the aspect component's class
     * @param filePath the path of the configuration file; it can be
     * absolute but, if relative, it is automatically concatened to the
     * application's path
     * @param weaveNow a true value means that the aspect that
     * configured by this configuration will be automatically woven at
     * the application's start, a false value means that the user will
     * have to weave it with a program (or with the administration
     * GUI); default is true */

    public ACConfiguration(Application application, String name, 
                           String filePath, boolean weaveNow) {
        this.name = name;
        this.application = application;
        this.weaveOnDemand = ! weaveNow;
        if (filePath!=null) {
            try {
                if (filePath.startsWith("file:")) {
                    filePath = filePath.substring(5);
                }
                this.filePath = new URL("file:"+filePath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // If name is not the name of a registered AC, 
        // It must be the class name of the AC
        ACManager acm = ACManager.getACM();
        if (!acm.isACDeclared(name)) {
            acm.declareAC(name,name);
        }
    }

    /**
     * Gets the name of the configured AC as defined in the declared
     * ACs of the AC manager.
     *
     * @return the AC name
     * @see #setName(String)
     * @see ACManager */

    public String getName() {
        return name;
    }

    /**
     * The aspect name setter. Must be declared in the
     * <code>jac.prop</code>.
     *
     * @param name the aspect name 
     * @see ACManager */

    public void setName(String name) { 
        this.name = name;
    }
   
    /**
     * Gets the aspect component instance that corresponds to this
     * configuration.
     *
     * @return the AC instance */

    public AspectComponent getInstance() {
        return instance;
    }

    /**
     * Gets the owning application.
     *
     * @return the application that owns this configuration */

    public Application getApplication() {
        return application;
    }

    /**
     * Return the class item for the configured aspect component.
     *
     * @return the AC type */

    public ClassItem getAspectClass() {
        String acPath = ((ACManager)ACManager.get()).getACPathFromName(name);
        return ClassRepository.get().getClass(acPath);
    }

    /**
     * Instantiates a new aspect component.
     *
     * @return the new aspect component, null if something went wrong
     */

    protected AspectComponent instantiate() {
        loggerAspects.debug("instantiating "+name);
        ACManager acm = (ACManager)ACManager.get(); 
        //      String oldAC = Collaboration.get().getCurAC();
        //      Collaboration.get().setCurAC(
        //         getApplication().getName() + "." + name );
        try {
            instance = (AspectComponent) acm.getObject(
                application.getName()+"."+name);
            if (instance == null) {
                if (acPath == null)
                    acPath = acm.getACPathFromName(name);
                loggerAspects.debug(name + " : " + acPath);
                if( acPath == null ) return null;
                Class cl = Class.forName( acPath );
                loggerAspects.debug("instantiating "+cl);
                instance = (AspectComponent) cl.newInstance();
                instance.setApplication(getApplication().getName());
            }
            return instance;
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            //         Collaboration.get().setCurAC(oldAC);
        }
    }

    /**
     * Configures the aspect component.
     *
     * <p>This method takes the aspect component instance that corresponds
     * to this configuration, parse the configuration file, and calls
     * all the configuration operations that are defined in this file.
     *
     * @see Parser
     * @see #getInstance() */


    protected void configure() {
        logger.info("--- configuring "+name+" aspect ---");
        long start = System.currentTimeMillis();
        String[] defaults = instance.getDefaultConfigs();
        for (int i=0; i<defaults.length; i++) {
            instance.configure(name,defaults[i]);
        }
        instance.configure(name,filePath.getFile());
        instance.whenConfigured();
        loggerPerf.info("aspect "+name+" configured in "+
                        (System.currentTimeMillis()-start)+"ms");
    }

    public static Object convertArray(Object[] array, Class componentType, Imports imports) 
        throws Exception 
    {
        Object result  = Array.newInstance(componentType, array.length);
        for (int i=0; i<array.length; i++) {
            Array.set(result,i,convertValue(array[i],componentType,imports));
        }
        return result;
    }

    public static Object convertValue(Object object, Class type) 
        throws Exception 
    {
        return convertValue(object,type,null);
    }

    public static Object convertValue(Object object, Class type, Imports imports) 
        throws Exception 
    {
        Object result = object;
        if (object != null && object.getClass() != type) {
            try {
                if (type.isArray()) {
                    result = convertArray((Object[])object,type.getComponentType(),imports);
                } else if (type==double.class || type==Double.class) {
                    result = new Double((String)object);
                } else if (type==int.class || type==Integer.class) {
                    result = new Integer((String)object);
                } else if (type==long.class || type==Long.class) {
                    result = new Long((String)object);
                } else if (type==float.class || type==Float.class) {
                    result = new Float((String)object);
                } else if (type==boolean.class || type==Boolean.class) {
                    result = Boolean.valueOf((String)object);
                } else if (type==short.class || type==Short.class) {
                    result = new Short((String)object);
                } else if (type==Class.class) {
                    result = Class.forName((String)object);
                } else if (type==ClassItem.class) {
                    result = getClass((String)object,imports);
                } else if (type==VirtualClassItem.class) { 
                    result = ClassRepository.get().getVirtualClassStrict((String)object);
                } else if  (AbstractMethodItem.class.isAssignableFrom(type)) {
                    String str = (String)object;
                    int index = str.indexOf("(");
                    try {
                        ClassItem classItem;
                        if (index==-1) {
                            classItem = getClass(str,imports);
                            result = classItem.getConstructor("");
                        } else {
                            classItem = getClass(
                                str.substring(0,index),imports);

                            // resolve parameter types
                            String[] paramTypes = 
                                Strings.split(str.substring(index+1,str.length()-1), ",");
                            resolveTypes(paramTypes,imports);
                            String fullName = str.substring(0,index+1)+Strings.join(paramTypes,",")+")";
                            result = classItem.getConstructor(fullName.substring(index));
                        }
                    } catch (NoSuchClassException e) {
                        if (index!=-1) {
                            // resolve parameter types
                            String[] paramTypes = 
                                Strings.split(str.substring(index+1,str.length()-1), ",");
                            resolveTypes(paramTypes,imports);
                            str = str.substring(0,index+1)+Strings.join(paramTypes,",")+")";
                            index = str.lastIndexOf(".",index);
                        } else {
                            index = str.lastIndexOf(".");
                        }
                        if (index!=-1) {
                            try {
                                ClassItem classItem = getClass(
                                    str.substring(0,index),imports);
                                result = classItem.getAbstractMethod(
                                    str.substring(index+1));
                            } catch (NoSuchClassException e2) {
                                throw new Exception("Failed to convert "+str+
                                                    " into a "+type.getName());
                            }
                        } else {
                            throw new Exception("Failed to convert "+str+
                                                " into a "+type.getName());
                        }
                    }
                } else if (FieldItem.class.isAssignableFrom(type)) {
                    String str = (String)object;
                    loggerConf.debug("Trying to convert "+str+" into a FieldItem");
                    int index = str.length();
                    result = null; 
                    while (index!=-1 && result==null) {
                        index = str.lastIndexOf(".",index-1);
                        if (index!=-1) {
                            try {
                                loggerConf.debug(
                                    "  Trying class="+str.substring(0,index)+
                                    " and field="+str.substring(index+1));
                                ClassItem classItem = getClass(
                                    str.substring(0,index),imports);
                                result = classItem.getField(str.substring(index+1));
                                loggerConf.debug("    -> "+result);
                            } catch (NoSuchClassException e) {
                                loggerConf.info(
                                    "  Failed conversion of "+object+
                                    " to FieldItem with class="+str.substring(0,index)+
                                    " and field="+str.substring(index+1));
                            }
                        }
                    }
                    if (index==-1 || result==null) {
                        throw new Exception("Failed to convert "+str+
                                            " into a "+type.getName());
                    }
                } else if (MemberItem.class.isAssignableFrom(type)) {
                    String str = (String)object;
                    int index = -1;
                    int paren = str.indexOf("(");
                    if (paren==-1)
                        index = str.length();
                    else 
                        index = paren;

                    result = null; 
                    while (index!=-1 && result==null) {
                        index = str.lastIndexOf(".",index-1);
                        if (index!=-1) {
                            try {
                                ClassItem classItem = getClass(
                                    str.substring(0,index),imports);
                                result = classItem.getMember(str.substring(index+1));
                            } catch (NoSuchClassException e) {
                            }
                        }
                    }

                    if (index==-1 || result==null) {
                        throw new Exception("Failed to convert "+str+
                                            " into a "+type.getName());
                    }
                } else if (type == Length.class) {
                    return new Length((String)object);
                } else {
                    throw new Exception("Don't know how to convert "+object+" into a "+type);
                }
            } catch (Exception e) {
                loggerConf.info("Failed to convert "+object+" into "+type.getName(),e);
                throw new Exception(
                    "Failed to convert "+object+" into "+type.getName()+" : "+e);
            }
        }
        loggerConf.debug("Converted "+object+" into "+type+": "+result);
        return result;
    }

    protected static void resolveTypes(String[] types, Imports imports) {
        for (int i=0; i<types.length; i++) {
            String type = types[i];
            boolean isArray = false;
            if (type.endsWith("[]")) {
                type = type.substring(0,type.length()-2); 
            }
            try {
                if (isArray)
                    types[i] = 
                        imports.getClass(
                            types[i].substring(0,types[i].length()-2)).getName()+"[]";
                else
                    types[i] = imports.getClass(types[i]).getName();
            } catch (NoSuchClassException nse) {
                if (!Classes.isPrimitiveType(type))
                    throw nse;
            }
        }
    }

    protected static ClassItem getClass(String name, Imports imports) {
        if (imports!=null) {
            try {
                return imports.getClass(name);
            } catch (NoSuchClassException e) {
                return ClassRepository.get().getClass(name);
            }
        } else {
            return ClassRepository.get().getClass(name);
        }
    }

    /**
     * Instantiates, configures, and weaves the aspect component that
     * corresponds to this configuration.
     *
     * @see #instantiate()
     * @see #configure() 
     */
    public void weave() {
        //acs.put( acName, ac );
        instantiate();
        if (instance == null) {
            logger.error("could not instantiate aspect "+name);
            return;
        }
        try {
            instance.beforeConfiguration();
            configure();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        loggerAspects.debug(
            "registering " + instance + 
            " on application " + getApplication() );
        // This line is useful for composite aspects
        instance.doRegister();
        ACManager.get().register( getApplication().getName() + 
                                  "." + name, instance );
    }

    /**
     * Unweaves the aspect component that corresponds to this
     * configuration.
     */
    public void unweave() {
        // This line is useful for composite aspects
        instance.doUnregister();
        ((ACManager)ACManager.get()).unregister( application.getName() + "." + name );
        instance = null;
    }   

    /**
     * Returns a string representation of this configuration.
     * @return a string 
     */
    public String toString() {
        return "AC " + name + " configuration";   
    }

}
