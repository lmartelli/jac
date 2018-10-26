/*
  Copyright (C) 2001-2003 Renaud Pawlak, Lionel Seinturier, Fabrice
  Legond-Aubry.

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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.rtti.ClassInfo;
import org.objectweb.jac.core.rtti.LoadtimeRTTI;
import org.objectweb.jac.util.ExtArrays;
import org.objectweb.jac.util.Streams;
import org.objectweb.jac.util.Strings;

/**
 * The JAC specific class loader for JAC objects.
 *
 * <p>This loader does the following things :</p>
 * <ul>
 *   <li>load a set of Properties from all the found "jac.prop" files
 *   <li>get from them the choosen wrappeeTranslator
 *   <li>instanciate the choosen wrappeTranslator
 *   <li>replace the system class loader.
 *   <li>then for each loaded class it can delegates the translation of
 *    JAC objects to the choosen <code>WrappeeTranslator</code>.
 * </ul>
 * @see org.objectweb.jac.core.WrappeeTranslator */
public class JacLoader extends ClassLoader {
    static Logger logger = Logger.getLogger("loader");
    static Logger loggerRes = Logger.getLogger("loader.resource");

    /** 
     * Stores the bytecode of the loaded and translated classes.
     * 
     * @see isLoaded(String)
     * @see getLoadedBytecode(String) */
    private Hashtable classes = new Hashtable();
    
    /** If true, caches the tranlated classes on disk. */
    private boolean write = false;
    
    /** If true, clears the disk cache. */
    private boolean clean = false;

    private ClassLoader parentLoader = ClassLoader.getSystemClassLoader();

    private WrappeeTranslator wt = null;
    private String bytecodeModifier;
    
    /* Packages whose loading we delegate */
    private String[] ignoredPackages = {
        "java.", "javax.", "sun.", "org.xml.sax.", "org.w3c.dom.", "org.apache.log4j"
    };

    /* Classes whose loading we delegate. Those classes are thus shared
       by all Jac loaders, and untranslated */
    private HashSet ignoredClasses = new HashSet();


    private static ClassLoader otherClassLoader;

    LoadtimeRTTI rtti;

    /**
     * Create a JacLoader.
     *
     * @param write if true, caches the tranlated classes on disk
     * @param clean if true, clears the disk cache
     * @param otherClassLoader if not null, indicates another
     * ClassLoader where to search for classes not in original
     * ClassPath
     */
    public JacLoader(boolean write,
                     boolean clean,
                     ClassLoader otherClassLoader) 
        throws Exception
    {
        logger.info("building JacLoader...");
        //super (ClassLoader.getSystemClassLoader());
        this.write = write;
        this.clean = clean;
        if (otherClassLoader != null)
            JacLoader.otherClassLoader = otherClassLoader;

        ignoredClasses.add("org.objectweb.jac.util.Log");
        ignoredClasses.add("org.objectweb.jac.core.Jac");
        ignoredClasses.add("org.objectweb.jac.core.rtti.LoadtimeRTTI");
        ignoredClasses.add("org.objectweb.jac.core.rtti.ClassInfo");
        ignoredClasses.add("org.objectweb.jac.core.rtti.MethodInfo");
        ignoredClasses.add("org.objectweb.jac.core.rtti.InvokeInfo");
        // load the properties
        JacPropLoader.loadProps();
        logger.info("JacPropLoader = "+
                  Strings.hex(JacPropLoader.class));
			
        // get the bytecodeModifier string
        bytecodeModifier = org.objectweb.jac.core.JacPropLoader.bytecodeModifier;
        logger.info("jacloader: Selected bytecode modifier = '" +
                  bytecodeModifier +"'");
      
        // instanciate the bytecode translator 
        Class c = Class.forName("org.objectweb.jac.core.translators.WrappeeTranslator_"+
                                bytecodeModifier);
        Class repositoryClass = loadClass("org.objectweb.jac.core.rtti.ClassRepository",true);
        rtti = (LoadtimeRTTI)
            repositoryClass.getMethod("get",ExtArrays.emptyClassArray).invoke(null,ExtArrays.emptyObjectArray);
        wt = (WrappeeTranslator)c.getConstructor(
            new Class[] {LoadtimeRTTI.class}).newInstance(
                new Object[] {rtti});
      
        logger.info("Instanciated bytecode modifier is "+wt);
    }


    /**
     * Create a JacLoader. Same as
     * <code>JacLoader(boolean,boolean,null)</code>.
     *
     * @param write if true, caches the tranlated classes on disk
     * @param clean if true, clears the disk cache
     */
    public JacLoader(boolean write,
                     boolean clean) 
        throws Exception
    {
        this(write, clean, null);
    }


    public void setWrappeeTranslator(WrappeeTranslator wt) {
        this.wt = wt;
    }

    private void addIgnoredPkgs(String[] ignoredPackages) {
        String[] new_p = new String[ignoredPackages.length + 
                                   this.ignoredPackages.length];
      
        System.arraycopy(this.ignoredPackages, 0, new_p, 0, 
                         this.ignoredPackages.length);
        System.arraycopy(ignoredPackages, 0, new_p, this.ignoredPackages.length,
                         ignoredPackages.length);
        this.ignoredPackages = new_p;
    }

    /**
     * Tells wether to defer loading of a class to the parent ClassLoader
     * @param classname name of the class
     * @return true if we should defer the loading of that class
     */ 
    public boolean deferClass(String classname) {
        if (ignoredClasses.contains(classname))
            return true;
        for(int i=0; i<ignoredPackages.length; i++) {
            if (classname.startsWith(ignoredPackages[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tells wether a defered class's bytecode should be analyzed
     * @param classname the name of the class
     */
    protected boolean analyzeClass(String classname) {
        return false;
    }

    protected Class loadClass(String class_name, boolean resolve) 
        throws ClassNotFoundException
    {
        logger.debug("loadClass("+class_name+","+resolve+")");
        Class cl = null;

        cl = findLoadedClass(class_name);
        if (cl!=null) {
            logger.debug("already loaded "+class_name);
            return cl;
        }
        cl = (Class)classes.get(class_name);
        if (cl==null) {
            if (deferClass(class_name) && !JacPropLoader.adaptClass(class_name)) {
                logger.debug("defer "+class_name);
                byte[] bytes = null;
                if (analyzeClass(class_name)) {
                    try {
                        logger.debug("fill RTTI for "+class_name);                   
                        bytes = wt.fillClassRTTI(class_name);
                    } catch (Exception e) {
                        logger.error("Failed to fill RTTI for "+class_name,e);
                        // TODO: we should throw another exception
                        throw new ClassNotFoundException(
                            "jacloader: cannot find class "+
                            class_name+" on disk: "+e);
                    }
                }
                cl = parentLoader.loadClass(class_name);
            }

            if (cl == null) {
                byte[] bytes;
                String resourcePath = "/" + class_name.replace('.', '/') + ".class";
                logger.debug("resourcePath = "+resourcePath);
                bytes = loadResource(resourcePath);

                if (wt!=null && classIsToBeAdapted(class_name)) {
                    logger.info("adapting "+class_name);
                    String baseName = 
                        Jac.getJacRoot()+"classes_"+bytecodeModifier+"/"+
                        class_name.replace('.','/');
                    File cacheFile = new File(baseName+".class");
                    MessageDigest digest;
                    boolean useCache = false;
                    File md5File =  new File(baseName+".md5");
                    File rttiFile =  new File(baseName+".rtti");
                    byte[] orig_md5 = null;
                    try {
                        digest = MessageDigest.getInstance("MD5");
                        digest.update(bytes);
                        orig_md5 = digest.digest();
                        if (md5File.exists()) {
                            byte[] stored_md5 = loadFile(md5File);
                            useCache = Arrays.equals(orig_md5,stored_md5);
                        }
                    } catch (NoSuchAlgorithmException e) {
                        logger.warn("Could not get an MD5 digest: "+e);
                    }
                    boolean loaded = false;
                    if (useCache && cacheFile.exists() && rttiFile.exists()) {
                        logger.info("loading class from cache "+cacheFile);
                        bytes = loadFile(cacheFile);
                        try {
                            FileInputStream rttiStream = new FileInputStream(rttiFile);
                            ObjectInputStream objStream = new ObjectInputStream(rttiStream);
                            ClassInfo classInfo = (ClassInfo)objStream.readObject();
                            rttiStream.close();
                            objStream.close();
                            rtti.setClassInfo(class_name,classInfo);
                            loaded = true;
                        } catch (InvalidClassException e) {
                            // ignored
                            logger.info("Rtti format must have changed: "+rttiFile);
                        } catch (Exception e) {
                            logger.error("Failed to read rtti cache file "+rttiFile+": "+e);
                        }
                    }
                    if (!loaded) {
                        try {
                            //bytes = wt.translateClass(defineClass(class_name, bytes, 0, bytes.length));
                            bytes = wt.translateClass(class_name);
                        } catch (Exception e) {
                            logger.error("Failed to translate class "+class_name,e);
                            throw new ClassNotFoundException(
                                "jacloader: cannot find class "+
                                class_name+" on disk: "+e);
                        }
                        if (bytes!=null && write) {
                            logger.info("writing "+cacheFile);
                            try {
                                cacheFile.getParentFile().mkdirs();
                                FileOutputStream out = new FileOutputStream(cacheFile);
                                try {
                                    out.write(bytes);
                                } finally {
                                    out.close();
                                }
                            } catch (Exception e) {
                                logger.warn("Failed to write class file "+cacheFile+": "+e);
                            }
                            if (orig_md5!=null) {
                                logger.info("writing "+md5File);
                                try {
                                    md5File.getParentFile().mkdirs();
                                    FileOutputStream out = new FileOutputStream(md5File);
                                    try {
                                        out.write(orig_md5);
                                    } finally {
                                        out.close();
                                    }
                                } catch (Exception e) {
                                    logger.warn("Failed to write class file "+md5File+": "+e);
                                }
                            }
                            try {
                                FileOutputStream rttiStream = new FileOutputStream(rttiFile);
                                ObjectOutputStream objStream = new ObjectOutputStream(rttiStream);
                                objStream.writeObject(rtti.getClassInfo(class_name));
                                objStream.flush();
                                rttiStream.close();
                                objStream.close();
                            } catch (Exception e) {
                                logger.error("Failed to write rtti cache file "+rttiFile+": "+e);
                            }
                        }
                    }
                }
                logger.debug("defineClass "+class_name);
                if (bytes==null) {
                    logger.debug("defer "+class_name);
                    try {
                        if (otherClassLoader!=null)
                            cl = otherClassLoader.loadClass(class_name);
                        else
                            cl = parentLoader.loadClass(class_name);
                    } catch(Exception e) {
                        logger.error("Failed to load class "+class_name,e);
                    }
                } else {
                    try {
                        cl = defineClass(class_name, bytes, 0, bytes.length);
                    } catch(Exception e1) {
                        logger.error("Failed to define class "+class_name,e1);
                        // we are not authorized to load this class, we defer
                        logger.debug("defer "+class_name);
                        bytes = null;
                        if (analyzeClass(class_name))
                            try {
                                logger.debug("fill RTTI for "+class_name);                   
                                bytes = wt.fillClassRTTI(class_name);
                            } catch (Exception e) {
                                logger.error("Failed to fill RTTI for "+class_name,e);
                                throw new ClassNotFoundException(
                                    "jacloader: cannot find class "+
                                    class_name+" on disk: "+e);
                            }
                        cl = parentLoader.loadClass(class_name);
                    }
                    //cl = defineClass(class_name, bytes, 0, bytes.length);
                }
            }
      
            if (resolve)
                resolveClass(cl);
        }

        if (cl.getClassLoader()!=null)
            logger.info(class_name+"@"+Integer.toHexString(cl.hashCode())+
                        " ["+cl.getClassLoader()+"]");

        logger.debug("----------");
        classes.put(class_name, cl);

        return cl;
    }

    public InputStream getResourceAsStream(String name)
    {
        loggerRes.debug("getResourceAsStream: "+name);

        if ((name == null) || (name.length() == 0))
            return null;

        InputStream result = null;
      
        result = super.getResourceAsStream(name);
        if ((result == null) && (otherClassLoader != null)) {
            loggerRes.debug("  resource not found with system classloader, trying with: "+otherClassLoader);
            result = otherClassLoader.getResourceAsStream(name);
        }
        return result;
    }

    public URL getResource(String name)
    {
        loggerRes.debug("getResource: "+name+" (parent="+getParent()+")");

        URL result = null;

        //result = super.getResource(name);
        if (getParent() != null) {
            result = getParent().getResource(name);
        } 
        if (result == null) {
            result = findResource(name);
        }

        // if not in usual CLASSPATH, search in additionnal CLASSPATH
        if ((result == null) && (otherClassLoader != null)) {
            loggerRes.debug("  resource not found with "+getParent()+", trying with: "+otherClassLoader);
            if (otherClassLoader instanceof URLClassLoader)
                loggerRes.debug("  classpath="+Arrays.asList(((URLClassLoader)otherClassLoader).getURLs()));
            result = otherClassLoader.getResource(name);
        }

        return result;
    }

    byte[]  loadResource(String resourcePath) throws ClassNotFoundException {
        InputStream in = null;

        logger.debug( "loadResource: " + resourcePath);

        in = super.getClass().getResourceAsStream(resourcePath);

        // if not in usual CLASSPATH, search in additionnal CLASSPATH
        if ((in == null) && (otherClassLoader != null))
        {
            logger.debug(
                      "loadResource: searching in otherClassLoader ...");
            in = otherClassLoader.getResourceAsStream(resourcePath.substring(1));
        }

        if (in == null) 
        {
            throw new ClassNotFoundException("jacloader: Can not find resource "+resourcePath);
        }
        try {
            return Streams.readStream(in);
        } catch (IOException e) {
            throw new ClassNotFoundException(
                "jacloader: failed to load resource "+resourcePath+" : "+e);
        }
      
    }

    byte[]  loadFile(File file) throws ClassNotFoundException {
        try {
            InputStream in = new FileInputStream(file);
            if (in == null) 
                throw new ClassNotFoundException("jacloader: Can not find resource "+file);
            return Streams.readStream(in);
        } catch (IOException e) {
            throw new ClassNotFoundException(
                "jacloader: failed to load resource "+file+" : "+e);
        }
      
    }

    /**
     * Tell wether a class is to be adapted or not. All data
     * are extracted from the jac.prop files located a various
     * place.
     * 
     *
     * @see org.objectweb.jac.core.JacPropLoader
     * @see org.objectweb.jac.core.JacPropTools
     * @param name a <code>String</code> value
     * @return a <code>boolean</code> value (true if to be adpated).
     *
     */
    public static boolean classIsToBeAdapted(String name) {
        //      System.out.println("classIsToBeAdapted "+name+"? "+Strings.hex(JacPropLoader.class));
        int index = name.lastIndexOf(".");
        String packagename = "";
        if (index!=-1)
            packagename = name.substring(0,index);
        Iterator it;
        if (JacPropLoader.adaptClass(name))
            return true;

        // do not translate classes that follow
        if ( name.endsWith(".Run") || 
             name.endsWith(".Main") ||
             name.endsWith("AC") ||
             name.endsWith("Wrapper") ||
             (name.indexOf('$') != -1) ||
             name.endsWith("Exception") ||
             name.startsWith("org.objectweb.jac.core") ||
             name.startsWith("org.objectweb.jac.util") ||
             name.startsWith("org.objectweb.jac.aspects"))
            return false;
      
        if (JacPropLoader.doNotAdaptClass(name))
            return false;

        return true; 
    }

    /**
     * display some information about a class<br>
     * Only use for debug purpose to verify that the class
     * has been patched correctly.
     *
     * @see java.lang.ClassLoader
     * @param name the class to display
     *
     */
    public static void displayClassInfo(String name)
    {
        System.out.println ("jacloader: Displaying data on class " + name);
        Class cl = null;
        try {
            cl = Class.forName (name);
        }
        catch (Exception e)
        {
            System.out.println ("\tClass is not available from the loader.");
            return;
        }
        Field[] fl = cl.getDeclaredFields();
        for (int i=0; i<fl.length; i++)
        {
            System.out.println ("\tField "+i+" : name("+
                                fl[i].getName()+"), type("+
                                fl[i].getType().getName()
                                +"), attr("+fl[i].getModifiers()+")");
        }
        Method[] fm = cl.getDeclaredMethods();
        for (int i=0; i<fm.length; i++)
        {
            System.out.println ("\tMethod "+i+" ("+
                                fm[i].getName()+"), return type is "+
                                fm[i].getReturnType().getName());
            Class[] clexcep = fm[i].getExceptionTypes();
            for (int j=0; j<clexcep.length;j++)
                System.out.println ("\t\tThrow Exception : "+clexcep[j].getName());
            Class[] clpar = fm[i].getParameterTypes();
            for (int j=0; j<clpar.length;j++)
                System.out.println ("\t\tParameter number "+j+" is a "+clpar[j].getName());	
        }
    }
   

    /**
     * Returns true if the class is already loaded.
     * 
     * @param classname the class name to test
     * @return true if already loaded
     */
    public boolean isLoaded (String classname) {
        return (classes.containsKey(classname));
    }
	
    /**
     * Loads a class and calls <code>main()</code> in that class.
     * 
     * <p>This method was extracted "as is" from javassist 1.0 from
     * Shigeru Chiba.
     *
     * <p><a href="www.csg.is.titech.ac.jp/~chiba/javassist/">Javassist
     * Homepage</a>
     *
     * @param classname         the loaded class.
     * @param args parameters passed to <code>main()</code>.  */
	
    public void run(String classname, String[] args) throws Throwable {
        Class c = this.loadClass(classname);
        ClassLoader saved = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this); 
            c.getDeclaredMethod("main", new Class[] { String[].class })
                .invoke(null, new Object[] { args });
        }
        catch (java.lang.reflect.InvocationTargetException e) {
            throw e.getTargetException();
        }
        finally {
            Thread.currentThread().setContextClassLoader(saved); 
        }
    }

    /**
     * Use JacPropLoader to read some JAC properties
     * @param props the properties to read
     */
    public void readProperties(Properties props) {
        JacPropLoader.addProps(props);
    }

    /**
     * Usage: java org.objectweb.jac.core.JacLoader [class]
     */
    public static void main(String[] args) throws Exception {
        JacLoader loader = new JacLoader(true,true, otherClassLoader);
        for (int i=0; i<args.length; i++) {
            JacPropLoader.packagesToAdapt.add(args[i]);
            loader.loadClass(args[i]);
        }
    }
}
