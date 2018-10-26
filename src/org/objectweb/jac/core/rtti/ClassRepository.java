/*
  Copyright (C) 2001-2003 Renaud Pawlak <renaud@aopsys.com>, 
                          Laurent Martelli <laurent@aopsys.com>
  
  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 2 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA */

package org.objectweb.jac.core.rtti;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.objectweb.jac.util.Classes;
import org.objectweb.jac.util.Strings;

/**
 * This class defines the class repository of the rtti aspect.<p>
 *
 * It contains class items that are field and method items
 * agregates.<p>
 *
 * @see ClassItem
 * @see MethodItem
 * @see FieldItem
 * @see CollectionItem
 *
 * @author Renaud Pawlak
 * @author Laurent Martelli
 */

public class ClassRepository implements LoadtimeRTTI {
    static Logger logger = Logger.getLogger("rtti.repository");
    static Logger loggerlt = Logger.getLogger("rtti.lt");

    Map ltClassInfos = new Hashtable();

    public ClassInfo getClassInfo(String className) {
        ClassInfo classinfo=(ClassInfo)ltClassInfos.get(className);
        if (classinfo==null) {
            classinfo=new ClassInfo();
            ltClassInfos.put(className,classinfo);
        }
        return classinfo;
    }

    public void setClassInfo(String className, ClassInfo classInfo) {
        ltClassInfos.put(className,classInfo);
    }

    public MethodInfo getMethodInfo(String className, String method) {
        return getClassInfo(className).getMethodInfo(method);
    }

    /**
     * Adds a modified field for a given method.
     *
     * <p>This method is automatically called at load-time by the JAC
     * classloader through bytecode analysis.
     *
     * @param methodSign the method signature of the form
     * <code>packagepath.classname.methodname</code>
     * @param fieldName the name of the field that is modified by the
     * method */

    public void addltModifiedField(String className, String methodSign, 
                                   String fieldName) {
        getClassInfo(className).addModifiedField(methodSign,fieldName);
    }

    public void addltSetField(String className, String methodSign, 
                              String fieldName) {
        getClassInfo(className).addSetField(methodSign,fieldName);
    }

    /**
     * Adds an accessed field for a given method.
     *
     * <p>This method is automatically called at load-time by the JAC
     * classloader through bytecode analysis.
     *
     * @param methodSign the method signature of the form
     * <code>packagepath.classname.methodname</code>
     * @param fieldName the name of the field that is accessed by the
     * method */

    public void addltAccessedField(String className, String methodSign, 
                                   String fieldName) {
        getClassInfo(className).addAccessedField(methodSign,fieldName);
    }

    public void addltReturnedField(String className, String methodSign, 
                                   String fieldName) {
        getClassInfo(className).setReturnedField(methodSign,fieldName);
    }

    public void setltIsGetter(String className, String methodSign, 
                              boolean isGetter) {
        getClassInfo(className).setIsGetter(methodSign,isGetter);
    }

    public void addltAddedCollection(String className, String methodSign, 
                                     String fieldName) {
        getClassInfo(className).addAddedCollection(methodSign,fieldName);
    }

    public void addltRemovedCollection(String className, String methodSign, 
                                       String fieldName) {
        getClassInfo(className).addRemovedCollection(methodSign,fieldName);
    }

    public void addltModifiedCollection(String className, String methodSign, 
                                        String fieldName) {
        getClassInfo(className).addModifiedCollection(methodSign,fieldName);
    }

    public void setCollectionIndexArgument(String className, String methodSign, 
                                           int argument) {
        getClassInfo(className).setCollectionIndexArgument(methodSign,argument);
    }

    public void setCollectionItemArgument(String className, String methodSign, 
                                          int argument) {
        getClassInfo(className).setCollectionItemArgument(methodSign,argument);
    }

    public void addInvokedMethod(String className, String methodSign, 
                                 InvokeInfo invokeInfo) {
        getClassInfo(className).addInvokedMethod(methodSign,invokeInfo);
    }

    public void setCallSuper(String className, String method) {
        getMethodInfo(className,method).callSuper = true;
    }

    /**
     * Get the sole instance of class repository.<p>
     * 
     * @return the class repository */
   
    public static ClassRepository get() {
        if ( classRepository == null ) {
            classRepository = new ClassRepository();
        }
        return classRepository;
    }
   
    /**
     * Store the sole instance of class repository. */

    protected static transient ClassRepository classRepository = null;

    /** Stores the Jac objects root class. */
    //public static Class jacObjectClass;
    /** Stores the Wrappee class. */
    public static Class wrappeeClass;
    static {
        try {
            //jacObjectClass = Class.forName( "org.objectweb.jac.core.JacObject" );
            wrappeeClass = Class.forName("org.objectweb.jac.core.Wrappee");
        } catch(Exception e) {
            e.printStackTrace();
            System.exit( -1 );
        }
    }

    /**
     * Store direct access to the methods of all the classes by name on
     * optimization purpose. */
    transient static Hashtable directMethodAccess = new Hashtable();

    /**
     * Store direct access to the fields of all the classes by name on
     * optimization purpose. */
    transient static Hashtable directFieldAccess = new Hashtable();

    /**
     * Fill the direct access to methods hashtable for a given class.
     *
     * <p>This is used to optimize the method calls when you only have
     * the method name.
     *
     * @param cl the class to treat.
     *
     * @see #getDirectMethodAccess(Class,String) */

    public static Hashtable fillDirectMethodAccess(final Class cl) {
        if( cl == null ) return null;
        if ( directMethodAccess == null ) directMethodAccess = new Hashtable();
        Hashtable methods = new Hashtable();
        final Method[] meths = cl.getMethods();
        //      AccessibleObject.setAccessible( meths, true );
        for (int i=meths.length-1; i>=0; i--) {
            meths[i].setAccessible(true);
            if ( methods.containsKey(meths[i].getName()) ) {
                Method[] oldms = (Method[]) methods.get( meths[i].getName() );
                Method[] newms = new Method[oldms.length+1];
                System.arraycopy( oldms, 0, newms, 0, oldms.length );
                newms[oldms.length] = meths[i];
                methods.remove( meths[i].getName() );
                methods.put( meths[i].getName(), newms );
            } else {
                methods.put( meths[i].getName(), new Method[] { meths[i] } );
            }
        }
        directMethodAccess.put(cl,methods);
        return methods;
    }

    /**
     * Fill the direct access to fields hashtable for a given class.
     *
     * <p>This is used to optimize the access of protected and private
     * fields.
     *
     * @param cl the class to treat. */

    public static void fillDirectFieldAccess(final Class cl) {
        if (cl==null || cl==Object.class) 
            return;
        if (directFieldAccess == null )
            directFieldAccess = new Hashtable();
        if (directFieldAccess.containsKey(cl)) 
            return;
        Hashtable fields = new Hashtable();
        Class superClass = cl.getSuperclass();
        if (superClass!=Object.class && superClass!=null) {
            fillDirectFieldAccess(superClass);
            Map inheritedFields = (Map)directFieldAccess.get(superClass);
            if (inheritedFields!=null)
                fields.putAll(inheritedFields);
        }
        final Field[] fs = cl.getDeclaredFields();
        //      AccessibleObject.setAccessible( fs, true );
        for (int i = 0; i<fs.length; i++) {
            if (!isJacField(fs[i].getName()) && !isSystemField(fs[i].getName())) {
                fs[i].setAccessible(true);
                fields.put(fs[i].getName(),fs[i]);
            }
        }
        directFieldAccess.put(cl,fields);
    }

    /**
     * Returns a Hashtable that maps fields with their names.
     *
     * <p>For efficiency, the programmer should use this method instead
     * of using the Java refection API to retrives values of protected
     * or private fields within a class and all its superclasses.
     * 
     * @param cl the class
     * @return a Map (String(field name) -> Field)
     * 
     * @see #fillDirectFieldAccess(Class) */

    public static Hashtable getDirectFieldAccess(Class cl) {
        fillDirectFieldAccess(cl);
        return ((Hashtable)directFieldAccess.get(cl));
    }

    /**
     * Call a directly acceded method.
     *
     * @param cl the class that supports the method
     * @param methodName the method to call
     * @param o the object to call the method on
     * @param params the arguments
     *
     * @see #getDirectMethodAccess(Class,String)
     */
    public static Object invokeDirect(Class cl, 
                                      String methodName, 
                                      Object o, 
                                      Object[] params) 
        throws java.lang.NoSuchMethodException, 
        InvocationTargetException, IllegalAccessException
    {      
        Method[] directMethods = getDirectMethodAccess(cl,methodName);
        logger.debug("direct invocation of "+methodName+" => "+
                     java.util.Arrays.asList(directMethods));
        boolean ok = false;
        boolean lengthOk = false;
        Object ret = null;
        for (int i=0; i<directMethods.length; i++) {
            if (directMethods[i].getParameterTypes().length==params.length) {
                lengthOk = true;
                try {
                    ret = directMethods[i].invoke(o, params);
                    ok = true;
                } catch (IllegalArgumentException e1) {
                }
            }
        }
        if (!ok) {
            if (lengthOk) {
                logger.error("No such method "+cl.getName()+"."+methodName+Arrays.asList(params));
            } else {
                logger.error("Wrong number of parameters for "+cl.getName()+"."+methodName+Arrays.asList(params));
            }
            throw new java.lang.NoSuchMethodException();
        }
        return ret;
    }

    /**
     * Return an array of methods that correspond to the given class
     * and given method name.
     *
     * <p>For efficiency, the programmer should use this method instead
     * of using the Java refection API.
     * 
     * @param cl the class where the method is supposed to be
     * @param name the method name
     * @return an array containing the matching methods (homonyms), a
     * one array element with <code>null</code> inside if none method
     * matched (this ugly result was introduced for backward
     * compatibility)
     * 
     * @see #fillDirectMethodAccess(Class) 
     */
    public static Method[] getDirectMethodAccess(Class cl, String name) {
        Hashtable methods = (Hashtable)directMethodAccess.get(cl);
        if (methods==null)
            methods = fillDirectMethodAccess(cl);
        Method[] ret = (Method[]) methods.get(name);
        if (ret == null ) {
            return new Method[] { };
        } else {
            return ret;
        }
    }

    /**
     * Returns true is this class defautlt constructor (the one with no
     * parameter) has been added by JAC at class load-time.
     *
     * @param cl the class to test
     * @return true if added, false if programmer defined 
     */
    public static boolean isDefaultConstructorAdded(Class cl) {
        try {
            cl.getField("__JAC_ADDED_DEFAULT_CONSTRUCTOR");
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Return true if the method is added by JAC at class load-time.
     *
     * @param methodName the method to check
     * @return true if added at load-time 
     */
    public static boolean isJacMethod (String methodName) {
        if (methodName.startsWith("_"))
            return true;
        return false;
    }

    /**
     * Return true if the field is added by JAC at class load-time. 
     * @param fieldName name of the field
     */
    public static boolean isJacField (String fieldName) {
        return 
            fieldName.startsWith("__JAC_") || 
            fieldName.equals("JUST4LOG_OPTIMIZED");
    }

    /**
     * Tells if the field is a "system" field, such as a field added by
     * the compiler for class objects.
     * @param fieldName name of the field 
     */
    public static boolean isSystemField(String fieldName) {
        return fieldName.indexOf('$')!=-1;
    }

    /**
     * Gets the method names of a class
     *
     * @param cl the class
     * @return the method names as an array of strings
     */   
    public static String[] getMethodsName(Class cl) {
   
        String[] methodNames = null;
        Vector tmp = new Vector();
      
        try {
      
            Method[] methods = cl.getMethods();

            for (int i=0; i<methods.length; i++) {
                if ( ! ( Modifier.isStatic(methods[i].getModifiers()) ||
                         isJacMethod(methods[i].getName()) ) ) {
                    tmp.add (methods[i].getName());
                }
            }

            methodNames = new String[tmp.size()];

            for (int i=0 ; i<tmp.size(); i++) {
                methodNames[i] = (String)tmp.get(i);
            }

        } catch(Exception e) { 
            logger.error("getMethodsName "+cl.getName()+" failed",e); 
        }
      
        return methodNames;
    }

    /**
     * Get modifiers names.
     *
     * @param cl the class
     * @return the modifiers method names as an array of strings
     */
    public static String[] getModifiersNames(Class cl) {
   
        ClassItem cli = ClassRepository.get().getClass(cl);
        Collection modifiers = cli.getAllModifiers();
      
        String[] methodNames = new String[modifiers.size()];

        Iterator it = modifiers.iterator();
        for( int i=0; i<methodNames.length; i++ ) {
            methodNames[i]=((MethodItem)it.next()).getName();
        }
        return methodNames;
    }

    /**
     * Get getter names.
     *
     * @param cl the class
     * @return the modifiers names as an array of strings
     */
    public static String[] getGettersNames(Class cl) {
   
        ClassItem cli = ClassRepository.get().getClass(cl);
        Collection getters = cli.getAllGetters();
      
        String[] methodNames = new String[getters.size()];

        Iterator it = getters.iterator();
        for( int i=0; i<methodNames.length; i++ ) {
            methodNames[i]=((MethodItem)it.next()).getName();
        }
        return methodNames;
    }

    /**
     * Adds a set of written fields to a method.<p>
     *
     * This method shortcuts the writting of RTTI aspects for
     * programs. Equivalent effects can be achieved by using the RTTI
     * aspect API.<p>
     *
     * @param cl the involved class
     * @param methodName the method name
     * @param fieldNames the set of fields that are written by this
     * method
     * @see MethodItem#addWrittenField(FieldItem)
     */
    public static void addWrittenFields(Class cl, 
                                        String methodName, 
                                        String[] fieldNames)
    {
        ClassItem cli = ((ClassRepository)ClassRepository.get()).getClass(cl);
        MethodItem[] mis = cli.getMethods(methodName);

        if ( mis != null ) {
            for ( int i = 0; i < mis.length; i++ ) {
                for ( int j = 0; j < fieldNames.length; j++ ) {
                    FieldItem fi = cli.getField( fieldNames[j] );
                    if ( fi != null ) {
                        mis[i].addWrittenField(fi);
                    }
                }
            }
        }
      
    }  

    /**
     * Adds a set of accessed fields to a method.<p>
     *
     * This method shortcuts the writting of RTTI aspects for
     * programs. Equivalent effects can be achieved by using the RTTI
     * aspect API.<p>
     *
     * @param cl the involved class
     * @param methodName the method name
     * @param fieldNames the set of fields that are accessed by this
     * method
     * @see MethodItem#addAccessedField(FieldItem)
     */
    public static void addAccessedFields(Class cl, 
                                         String methodName, 
                                         String[] fieldNames) {
      
        ClassItem cli = ClassRepository.get().getClass(cl);
        MethodItem[] mis = cli.getMethods(methodName);

        if ( mis != null ) {
            for ( int i = 0; i < mis.length; i++ ) {
                for ( int j = 0; j < fieldNames.length; j++ ) {
                    FieldItem fi = cli.getField( fieldNames[j] );
                    if ( fi != null ) {
                        mis[i].addAccessedField(fi);
                    }
                }
            }
        }
      
    }  

    /**
     * Adds a set of added collections to a method.<p>
     *
     * This method shortcuts the writting of RTTI aspects for
     * programs. Equivalent effects can be achieved by using the RTTI
     * aspect API.<p>
     *
     * @param cl the involved class
     * @param methodName the method name
     * @param collectionNames the set of collections that are added by this
     * method
     * @see MethodItem#addAddedCollection(CollectionItem) 
     */
    public static void addAddedCollections(Class cl, 
                                           String methodName, 
                                           String[] collectionNames) {
      
        ClassItem cli = ClassRepository.get().getClass(cl);
        MethodItem[] mis = cli.getMethods(methodName);

        if ( mis != null ) {
            for ( int i = 0; i < mis.length; i++ ) {
                for ( int j = 0; j < collectionNames.length; j++ ) {
                    CollectionItem ci = (CollectionItem)cli.getField(collectionNames[j]);
                    if ( ci != null ) {
                        mis[i].addAddedCollection(ci);
                    }
                }
            }
        }
      
    }  

    /**
     * Adds a set of removed collections to a method.<p>
     *
     * This method shortcuts the writting of RTTI aspects for
     * programs. Equivalent effects can be achieved by using the RTTI
     * aspect API.<p>
     *
     * @param cl the involved class
     * @param methodName the method name
     * @param collectionNames the set of collections that are removed by this
     * method
     * @see MethodItem#addRemovedCollection(CollectionItem) 
     */
    public static void addRemovedCollections(Class cl, 
                                             String methodName, 
                                             String[] collectionNames) {
      
        ClassItem cli = ClassRepository.get().getClass(cl);
        MethodItem[] mis = cli.getMethods(methodName);

        if (mis != null) {
            for (int i=0; i<mis.length; i++) {
                for (int j=0; j<collectionNames.length; j++) {
                    CollectionItem ci = (CollectionItem)cli.getField(collectionNames[j]);
                    if (ci != null) {
                        mis[i].addRemovedCollection(ci);
                    }
                }
            }
        }
      
    }

    /**
     * The default constructor will set the classRepository field to
     * the right value (singleton pattern). */

    public ClassRepository () {
        classRepository = this;
    }

    /**
     * This method returns an existing class from its name.
     *
     * <p>If the class is not registered yet, the class repository
     * automatically builds default runtime informations (using naming
     * conventions), and seamlessly registers the new
     * <code>ClassItem</code> instance into the class repository.
     *
     * <p>Note: in case of manual class registrations, a class must be
     * registered with its full name to avoid name conflicts.<p>
     *
     * @param name the name of the class to get
     * @return the class if exist, null otherwise 
     *
     * @see #getVirtualClass(String)
     */
    public ClassItem getClass(String name)
    {
        String wrappedName = Classes.getPrimitiveTypeWrapper(name);
        MetaItem res = (MetaItem)getObject(wrappedName);
        if (res == null) {
            try {
                res = getClass(Class.forName(name));
            } catch (ClassNotFoundException e) {
                throw new NoSuchClassException(name);
            }
        }
        if (res!=null && res instanceof ClassItem)
            return (ClassItem)res;
        else
            throw new NoSuchClassException(name);
    }

    /**
     * Returns a ClassItem or a VirtualClassItem from its name. It
     * first tries to find a ClassItem, and if it fails, it returns a
     * VirtualClassItem.
     *
     * @param name the name of the class to find
     * @return a ClassItem or a VirtualClassItem 
     *
     * @see #getClass(String)
     * @see #getVirtualClassStrict(String)
     */

    public MetaItem getVirtualClass(String name) 
    {
        MetaItem ret;
        try {
            ret = getClass(name);
        } catch (NoSuchClassException e) {
            ret = (MetaItem)getObject(name);
            if (ret == null)
                throw e;
        }
        return ret;
    }

    /**
     * Returns a VirtualClassItem from its name.
     * @param name the name of the class to find
     * @return the VirtualClassItem with the requested name
     * @see #getClass(String)
     * @see #getVirtualClass(String)
     */
    public VirtualClassItem getVirtualClassStrict(String name) 
    {
        MetaItem ret;
        ret = (MetaItem)getObject(name);
        if (ret == null || !(ret instanceof VirtualClassItem))
            throw new NoSuchClassException(name);
        return (VirtualClassItem)ret;
    }

    /**
     * This method returns the class item that corresponds to the given
     * object class.<p>
     *
     * @param object the object to get the class item
     * @return the class item
     * @see #getClass(String) */

    public ClassItem getClass(Object object) {
        return getNonPrimitiveClass(object.getClass().getName());
    }
    /**
     * This method returns the class item that corresponds to the given
     * class.<p>
     *
     * @param cl the class to get the class item of
     * @return the class item
     * @see #getClass(String) 
     */
    public ClassItem getClass(Class cl) {
        String wrappedName = Classes.getPrimitiveTypeWrapper(cl.getName());
        MetaItem res = (MetaItem)getObject(wrappedName);
        if (res == null) {
            res = buildDefaultRTTI(cl);
            if (res != null) 
                register(wrappedName, res);
        }
        if (res!=null && res instanceof ClassItem)
            return (ClassItem)res;
        else
            throw new NoSuchClassException(cl.getName());
        /*      return getClass(cl.getName()); */
    }

    public ClassItem getNonPrimitiveClass(String className) {
        try {
            // !!! Class.forName() can trigger the instantiation of
            // !!! the ClassItem
            Class cl = Class.forName(className);
            MetaItem res = (MetaItem)getObject(className);
            if (res == null) {
                try {
                    res = buildDefaultRTTI(cl);
                } catch(Exception e) {
                    throw new NoSuchClassException(className);
                }
                if (res != null) {
                    register(className, res);
                    return (ClassItem)res;
                }
            } else {
                return (ClassItem)res;
            }
            throw new NoSuchClassException(className);
        } catch(ClassNotFoundException e) {
            throw new NoSuchClassException(className);
        }
    }

    String ignoreFields = null;
    public void ignoreFields(String packageExpr) {
        ignoreFields = packageExpr;
    }

    /**
     * This method builds the default RTTI for a given class name and
     * returns a corresponding class item.<p>
     *
     * @param cl the class to build
     * @return the corresponding class item, null if the given name is
     * not a class name 
     */
    public ClassItem buildDefaultRTTI(Class cl) {
        ClassItem cli = null;
        logger.debug( ">>> Constructing RTTI infos for class "+cl.getName()+Strings.hash(cl));
        try {

            cli = new ClassItem(cl);

            // methods
            Method[] meths = cl.getMethods();
            //Log.trace("rtti.methods","adding methods for "+cli);
            for(int i=0; i<meths.length; i++) {
                if (meths[i].getDeclaringClass() == Object.class 
                    /*|| meths[i].getName().startsWith("_")*/ ) continue;
                Logger.getLogger("rtti."+cl.getName()).debug("    adding method "+meths[i]);
                MethodItem mi = new MethodItem(meths[i],cli);
                cli.addMethod(mi);
            }

            // constructors
            Constructor[] constructors = cl.getConstructors();
            for(int i=0; i<constructors.length; i++) {
                if ( ! ( isDefaultConstructorAdded(cl) && 
                         constructors[i].getParameterTypes().length == 0 ) ) {
                    cli.addConstructor(new ConstructorItem(constructors[i],cli));
                }
            }

        } catch (InvalidDelegateException e) {
            logger.error("buildDefaultRTTI("+cl.getName()+"): "+e);
            return null;
        } catch (NoClassDefFoundError e) {
            logger.error("buildDefaultRTTI("+cl.getName()+")",e);
            throw e;
        }
        logger.debug( ">>> DONE Constructing RTTI infos for class "+cl.getName()+Strings.hash(cl)+
                      " -> "+cli+Strings.hash(cli));
        return cli;
    }

    public void buildDefaultFieldRTTI(ClassItem cli) {
        loggerlt.debug("constructing fields info for "+cli.getName());
        Class cl = cli.getActualClass();
        Collection fields = getDirectFieldAccess(cl).values();
        Iterator it = fields.iterator();
        while (it.hasNext()) {
            Field f = (Field) it.next();
            //logger.debug( "constructing " + f );
            Class t = f.getType();
            FieldItem fi = null;
            MethodItem mis[] = null;
            Method m = null;
            try {
                if (RttiAC.isCollectionType(t))
                {
                    logger.debug(t.getName()+" -> "+f.getName()+ " is a collection");
                    fi = new CollectionItem(f,cli);
                } else {
                    logger.debug(f.getName()+ " is a field");
                    fi = new FieldItem(f,cli);
                }
            } catch (InvalidDelegateException e) {
                e.printStackTrace();
            }
            cli.addField(fi);
        }
        Logger logger = Logger.getLogger("rtti."+cli.getName());
        logger.debug("extracting bytecoded info for "+cli.getName());
        logger.debug("methods "+cli.getAllMethods());
        Iterator methods = cli.getAllMethods().iterator();
        while (methods.hasNext()) {
            MethodItem method = (MethodItem)methods.next();
            try {
                logger.debug("    "+method.getFullName());
                AbstractMethodItem realMethod = method.getConcreteMethod();
                String key = realMethod.getOwningClass().getName()+"."+realMethod.getFullName();
                ClassInfo classinfo = getClassInfo(realMethod.getOwningClass().getName());
                MethodInfo methodinfo = classinfo.getMethodInfo(key);
                if (methodinfo.accessedFields.size()>0) {
                    logger.debug("        accessed fields: "+methodinfo.accessedFields);
                    it = methodinfo.accessedFields.iterator();
                    while (it.hasNext()) {
                        String fieldName = (String)it.next();
                        FieldItem fi = cli.getField(fieldName);
                        method.addAccessedField(fi);
                        fi.addAccessingMethod(method);
                    }
                }
                if (methodinfo.returnedField!=null && methodinfo.isGetter) {
                    logger.debug("        returned field: "+
                                 methodinfo.returnedField);
                    FieldItem fi = cli.getField(methodinfo.returnedField);
                    method.setReturnedField(fi);
                    fi.setGetter(method);
                }
                if (methodinfo.collectionIndexArgument!=-1) {
                    logger.debug("        collectionIndexArgument : "+
                                 methodinfo.collectionIndexArgument);
                    method.setCollectionIndexArgument(methodinfo.collectionIndexArgument);
                }
                if (methodinfo.collectionItemArgument!=-1) {
                    logger.debug("        collectionItemArgument : "+
                                 methodinfo.collectionItemArgument);
                    method.setCollectionItemArgument(methodinfo.collectionItemArgument);
                }
                if (methodinfo.modifiedFields.size()>0) {
                    logger.debug("        modified fields: "+methodinfo.modifiedFields);
                    it = methodinfo.modifiedFields.iterator();
                    while(it.hasNext()) {
                        String fieldName = (String)it.next();
                        try {
                            FieldItem fi = cli.getField(fieldName);
                            method.addWrittenField(fi);
                            fi.addWritingMethod(method);
                        } catch (NoSuchFieldException e) {
                            // On Windows, JDK 1.4.0_02, static private fields
                            // used for <MyClass>.class are not found here
                        }
                    }
                }
                if (methodinfo.setFields.size()==1) {
                    logger.debug("         set fields: "+methodinfo.setFields);
                    it = methodinfo.setFields.iterator();
                    while(it.hasNext()) {
                        String fieldName = (String)it.next();
                        FieldItem fi = cli.getField(fieldName);
                        method.setSetField(fi);
                        fi.setSetter(method);
                    }
                } else if (methodinfo.setFields.size()>1) {
                    logger.warn(
                        cli.getName()+" sets more than one field: "+
                        methodinfo.setFields);
                }
                if (methodinfo.addedCollections.size()>0) {
                    logger.debug("        added collections: "+
                                 methodinfo.addedCollections);
                    it = methodinfo.addedCollections.iterator();
                    while(it.hasNext()) {
                        String fieldName = (String)it.next();
                        CollectionItem ci = cli.getCollection(fieldName);
                        method.addAddedCollection(ci);
                        ci.addAddingMethod(method);
                    }
                }
                if (methodinfo.removedCollections.size()>0) {
                    logger.debug("        removed collections: "+
                                 methodinfo.removedCollections);
                    it = methodinfo.removedCollections.iterator();
                    while(it.hasNext()) {
                        String fieldName = (String)it.next();
                        CollectionItem ci = cli.getCollection(fieldName);
                        method.addRemovedCollection(ci);
                        ci.addRemovingMethod(method);
                    }
                }
                if (methodinfo.modifiedCollections.size()>0) {
                    logger.debug("        modified collections: "+
                                 methodinfo.modifiedCollections);
                    it = methodinfo.modifiedCollections.iterator();
                    while(it.hasNext()) {
                        String fieldName = (String)it.next();
                        CollectionItem ci = cli.getCollection(fieldName);
                        method.addModifiedCollection(ci);
                        ci.addWritingMethod(method);
                    }
                }
                if (method.getType()!=void.class &&
                    methodinfo.invokedMethods.size()>0) {
                    logger.debug("        invoked methods: "+methodinfo.invokedMethods);
                    it = methodinfo.invokedMethods.iterator();
                    while(it.hasNext()) {
                        InvokeInfo invokeInfo = (InvokeInfo)it.next();
                        ClassItem invokedClass = getClass(invokeInfo.className);
                        try {
                            MethodItem invokedMethod =
                                invokedClass.getMethod(invokeInfo.method);
                            if (invokedMethod.getType()!=void.class)
                                invokedMethod.addDependentMethod(method);
                        } catch(Exception e) {
                        }
                    }               
                }
                if (methodinfo.callSuper) {
                    ClassItem superClass = cli.getSuperclass();
                    logger.debug("        calls super");
                    if (superClass!=null) {
                        try {
                            MethodItem superMethod = 
                                superClass.getMethod(method.getFullName());
                            if (superMethod.isAdder()) {
                                CollectionItem[] addedCollections = 
                                    superMethod.getAddedCollections();
                                logger.debug("        added collections: "+
                                             Arrays.asList(addedCollections));
                                for (int i=0; i<addedCollections.length; i++) {
                                    CollectionItem collection = 
                                        cli.getCollection(addedCollections[i].getName());
                                    method.addAddedCollection(collection);
                                    collection.addAddingMethod(method);
                                }
                            }
                            if (superMethod.isRemover()) {
                                CollectionItem[] removedCollections = 
                                    superMethod.getRemovedCollections();
                                logger.debug("        removed collections: "+
                                             Arrays.asList(removedCollections));
                                for (int i=0; i<removedCollections.length; i++) {
                                    CollectionItem collection = 
                                        cli.getCollection(removedCollections[i].getName());
                                    method.addRemovedCollection(collection);
                                    collection.addRemovingMethod(method);
                                }
                            }
                            if (superMethod.hasModifiedCollections()) {
                                CollectionItem[] modifiedCollections = 
                                    method.getModifiedCollections();
                                logger.debug("        modified collections: "+
                                             Arrays.asList(modifiedCollections));
                                for (int i=0; i<modifiedCollections.length; i++) {
                                    CollectionItem collection = 
                                        cli.getCollection(modifiedCollections[i].getName());
                                    method.addModifiedCollection(collection);
                                    collection.addWritingMethod(method);
                                }
                            }
                            if (superMethod.isSetter()) {
                                FieldItem field = 
                                    cli.getField(superMethod.getSetField().getName());
                                method.setSetField(field);
                                field.setSetter(method);
                                logger.debug("        set field: "+field);
                            }
                            if (superMethod.hasWrittenFields()) {
                                FieldItem[] writtenFields = 
                                    superMethod.getWrittenFields();
                                for (int i=0; i<writtenFields.length; i++) {
                                    FieldItem field = cli.getField(writtenFields[i].getName());
                                    method.addWrittenField(field);
                                    field.addWritingMethod(method);                        
                                }
                            }
                        } catch (NoSuchMethodException e) {
                            // The superclass' method may be
                            // protected, and we shouldn't issue a
                            // warning in this case
                            if (!superClass.hasMethod(method.getName(),method.getParameterTypes()))
                                logger.warn(
                                    "Method "+method.getFullName()+
                                    " calls super but super class "+superClass.getName()+
                                    " does not have a public method with that name");
                        }
                    } else {
                        logger.warn("Method "+method.getFullName()+
                                    " calls super but class does not have a super class");
                    }
                }
            } catch (Exception e) {
                logger.error("Failed RTTI build for "+method,e);
            }
        }

        // Inherited mixin methods
        ClassItem superClass = cli.getSuperclass();
        if (superClass!=null) {
            Iterator i = superClass.getMixinMethods().iterator();
            while (i.hasNext()) {
                MixinMethodItem method = (MixinMethodItem)i.next();
                try {
                    cli.addMethod(new MixinMethodItem((Method)method.getDelegate(),cli));
                    logger.debug("Adding inherited mixin method "+method.getFullName());
                } catch(InvalidDelegateException e) {
                    logger.error("Failed to add inherited mixin method "+
                                 method.getFullName(),e);
                }
            }
        }
    }

    /**
     * Link class items to their names */
    public Hashtable objects = new Hashtable();

    /**
     * Reverse hashtable to find an class item from its name */
    public Hashtable names = new Hashtable();

    /**
     * Register a new class into the class repository.
     *
     * @param logicalName the key that allows to find the class
     * @param object the class to register
     * @return true if the class registered, false if already
     * registered
     *
     * @see #unregister(String) 
     */
    public void register(String logicalName, Object object) {
        Object o = objects.get(logicalName);
        boolean result = true;
        if (o != null && !(object instanceof VirtualClassItem)) {
            throw new RuntimeException("Class "+logicalName+" is already registered: "+o);
        }
        objects.put(logicalName, object);      
        names.put(object, logicalName);
    }

    /**
     * Unregister a class from the repository.
     *
     * @param logicalName the class name
     *
     * @see #register(String,Object) 
     */
    public void unregister(String logicalName) {
        if (objects.get(logicalName) == null) {
            return;
        }
        names.remove(objects.get(logicalName));
        objects.remove(logicalName);
    }

    /**
     * Returns true if a class is registered with this name.
     *
     * @param logicalName the key that allows to find the class
     *
     * @see #register(String,Object) 
     */
    public boolean isRegistered(String logicalName) {
        if (names.contains(logicalName)) {
            return true;
        }
        return false;
    }

    /**
     * Return all the registered classes as an array.
     *
     * <p>Reverse operation is <code>getNames()</code>.
     *
     * @return the registered classes in this repository
     *
     * @see #register(String,Object)
     * @see #getNames() 
     */
    public Object[] getClasses() {
        return objects.values().toArray();
    }

    /**
     * Return the names of the registered classes as an array.    
     *
     * @return the registered classes names in this repository
     *
     * @see #register(String,Object) 
     */
    public Object[] getNames() {
        return names.values().toArray();
    }

    /**
     * Return a registered classes for a given logical name.
     * 
     * <p>Return <code>null</code> if the name does not correspond to
     * any registered class or if <code>logicalName</code> is null.
     *
     * <p>Reverse operation is <code>getName(Object)</code>.
     *
     * @param logicalName the key that allows to find the class
     * @return the corresponding object, null if not registered
     *
     * @see #register(String,Object)
     * @see #getName(Object) 
     */
    public Object getObject(String logicalName) {
        if (logicalName == null) return null;
        return objects.get(logicalName);
    }

    /**
     * Returns the name of a registered class. Null if not
     * registered.
     *
     * <p>Reverse operation is <code>getObject(String)</code>.
     *
     * @param object the class to get the name of
     * @return the class name, null if not registered
     *
     * @see #register(String,Object) 
     * @see #getObject(String)
     */
    public String getName (Object object) {
        if (object == null) {
            return null;
        }
        return (String)names.get(object);
    }

    /**
     * Returns a field whose owning class is the class of an object
     *
     * @param substance the object whose class to user
     * @param field the field to return
     */
    public FieldItem getActualField(Object substance, FieldItem field) {
        return getClass(substance).getField(field.getName());
    }

    /**
     * Dump all the registered classes in this class repository. 
     */
    public void dump() {
        Enumeration keys = objects.keys();
      
        System.out.println ( getClass().getName() + " dump:");
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            System.out.println(" - " + key + " : " + objects.get( key ) );
        }
    }

    /* instantiate an object from a string */
    public static Object instantiate(Class type, String value) {
        if (type==int.class) {
            type = Integer.class;
        } else if (type==float.class) {
            type = Float.class;
        } else if (type==double.class) {
            type = Double.class;
        } else if (type==boolean.class) {
            type = Boolean.class;
        } else if (type==byte.class) {
            type = Byte.class;
        } else if (type==char.class) {
            type = Character.class;
        } else if (type==long.class) {
            type = Long.class;
        } else if (type==short.class) {
            type = Short.class;
        }
        try {
            Constructor constructor = type.getConstructor(new Class[] {String.class});
            return constructor.newInstance(new Object[] {value});
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
