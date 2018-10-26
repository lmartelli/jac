/*
  Copyright (C) 2001-2003 Renaud Pawlak, Lionel Seinturier.

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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import org.aopalliance.intercept.ConstructorInterceptor;
import org.aopalliance.intercept.Interceptor;
import org.aopalliance.intercept.MethodInterceptor;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.rtti.AbstractMethodItem;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.core.rtti.ConstructorItem;
import org.objectweb.jac.core.rtti.MethodItem;
import org.objectweb.jac.core.rtti.MixinMethodItem;
import org.objectweb.jac.core.rtti.NamingConventions;
import org.objectweb.jac.util.Strings;
import org.objectweb.jac.util.WrappedThrowableException;

/**
 * This class provides a set of useful static methods that allow the
 * wrapping of wrappable objects (wrappee) by some wrapper objects.
 *
 * @author <a href="mailto:pawlak@cnam.fr">Renaud Pawlak</a>
 * @author <a href="mailto:Lionel.Seinturier@lip6.fr">Lionel Seinturier</a> */

public class Wrapping {
    static Logger logger = Logger.getLogger("wrapping");
    static Logger loggerExc = Logger.getLogger("exceptions");
    static Logger loggerRole = Logger.getLogger("rolemethod");
    static Logger loggerWuni = Logger.getLogger("wuni");

    /** The local Aspect Component manager for this container (on
        optimization purpose). */
    /** The wrapping chains for the base methods
        (wrappingChains[object]->hastable[method]->vector). */
    static transient Map wrappingChains = new java.util.WeakHashMap();
    /** The wrapping chains for the static base methods
        (staticWrappingChains[method]->vector). */
    static transient Map staticWrappingChains = new Hashtable();

    /** The exceptions handlers for the base methods. */
    static transient Map catchingChains = new java.util.WeakHashMap();

    /** The exceptions handlers for the static base methods. */
    static transient Map staticCatchingChains = new Hashtable();

    /** The wrappers for the base object. */
    static transient Map wrappers = new java.util.WeakHashMap();

    /** The wrappers for the base classes. */
    static transient Map staticWrappers = new Hashtable();

    /** The wrapper classes for the base object. */
    static transient Map wrapperClasses = new java.util.WeakHashMap();

    /** The wrapper classes for the base classes. */
    static transient Map staticWrapperClasses = new Hashtable();

    public static final Object[] emptyArray = new Object[0];

    /**
     * Returns the wrapping chain that wraps the given method.
     *
     * @param wrappee the wrappee (if null, the given method is
     * nesserally a static method)
     * @param method the wrapped method (can be static, then wrappee is
     * null) 
     */
    public static WrappingChain getWrappingChain(
        Wrappee wrappee,
        AbstractMethodItem method) 
    {
        //         System.out.println("getting wrapping chain for "+method);

        if (method.isStatic()) {
            WrappingChain result =
                (WrappingChain) staticWrappingChains.get(method);
            if (result == null) {
                result = new WrappingChain();
                staticWrappingChains.put(method, result);
            }
            //System.out.println("return "+result);
            return result;
        } else {
            Map wrappeeChains = (Map) wrappingChains.get(wrappee);
            if (wrappeeChains == null) {
                wrappeeChains = new Hashtable();
                wrappingChains.put(wrappee, wrappeeChains);
            }
            WrappingChain result = (WrappingChain) wrappeeChains.get(method);
            if (result == null) {
                result = new WrappingChain();
                wrappeeChains.put(method, result);
            }
            return result;
        }
    }

    /**
     * Returns the catching chain that wraps the given method.
     *
     * @param wrappee the wrappee (if null, the given method is
     * nesserally a static method)
     * @param method the wrapped method (can be static, then wrappee is
     * null) 
     */
    public static List getCatchingChain(
        Wrappee wrappee,
        AbstractMethodItem method) 
    {
        if (method.isStatic()) {
            List chain = (List) staticCatchingChains.get(method);
            if (chain == null) {
                chain = new Vector();
                staticCatchingChains.put(method, chain);
            }
            return chain;
        } else {
            Map wrappeeChain = (Map) catchingChains.get(wrappee);
            if (wrappeeChain == null) {
                wrappeeChain = new Hashtable();
                catchingChains.put(wrappee, wrappeeChain);
            }
            List chain = (List) wrappeeChain.get(method);
            if (chain == null) {
                chain = new Vector();
                wrappeeChain.put(method, chain);
            }
            return chain;
        }
    }

    /**
     * Returns the wrappers that wrap the given wrappee or wrappee's
     * class.
     *
     * @param wrappee the wrappee (can be null if wrappeeClass is not
     * null)
     * @param wrappeeClass the wrappee's class (can be null if wrappee
     * is not null) 
     */
    public static List getWrappers(Wrappee wrappee, ClassItem wrappeeClass) {
        if (wrappee == null) {
            List wrappers = (List) staticWrappers.get(wrappeeClass);
            if (wrappers == null) {
                wrappers = new Vector();
                staticWrappers.put(wrappeeClass, wrappers);
            }
            return wrappers;
        } else {
            List result = (List) wrappers.get(wrappee);
            if (result == null) {
                result = new Vector();
                wrappers.put(wrappee, result);
            }
            return result;
        }
    }

    /**
     * Returns the classes of the wrappers that wrap the given wrappee
     * or wrappee's class.
     *
     * @param wrappee the wrappee (can be null if wrappeeClass is not
     * null)
     * @param wrappeeClass the wrappee's class (can be null if wrappee
     * is not null) 
     */
    public static List getWrapperClasses(
        Wrappee wrappee,
        ClassItem wrappeeClass) 
    {
        if (wrappee == null) {
            List result = (List) staticWrapperClasses.get(wrappeeClass);
            if (result == null) {
                result = new Vector();
                staticWrapperClasses.put(wrappeeClass, result);
            }
            return result;
        } else {
            List result = (List) wrapperClasses.get(wrappee);
            if (result == null) {
                result = new Vector();
                wrapperClasses.put(wrappee, result);
            }
            return result;
        }
    }

    /**
     * Adds a wrapper to the current wrappee.
     *
     * <p>Any method of this wrapper can then be used on the wrappee
     * with <code>invokeRoleMethod</code>.
     *
     * <p>To precise which method of this wrapper should actually wrap
     * the current wrappee methods, use the <code>wrap</code> methods.
     *
     * @param wrappee the wrappee (can be null if wrappeeClass is not
     * null, then wraps all the instances of the class in a static
     * mode)
     * @param wrappeeClass the wrappee's class (can be null if wrappee
     * is not null)
     * @param wrapper the new wrapper */

    public static void wrap(
        Wrappee wrappee,
        ClassItem wrappeeClass,
        Wrapper wrapper) 
    {
        logger.debug("wrapping " + wrappee + "(" + wrappeeClass + ") with " + wrapper);
        List wrappers = getWrappers(wrappee, wrappeeClass);
        if (wrappers.contains(wrapper)) {
            logger.debug("  ignoring already present wrapper");
            return;
        }

        wrappers.add(wrapper);
        getWrapperClasses(wrappee, wrappeeClass).add(wrapper.getClass());

        /*
        String ac = (String) Collaboration.get().getCurAC();
        if ( ac != null ) {
           AspectComponent temp_ac = (AspectComponent) 
              ACManager.get().getObject( ac );
           if ( temp_ac != null ) { 
              temp_ac.addWrapper( wrapper );
              wrapper.setAspectComponent( ac );
           }
        }
        */
    }

    static private HashMap wrappableMethods = new HashMap();

    /**
     * Tells if a given method is wrappable.
     *
     * @param method the method to check */

    public static boolean isWrappable(AbstractMethodItem method) {
        // We use a cache because it is too slow otherwise
        Boolean ret = (Boolean) wrappableMethods.get(method);
        if (ret == null) {
            try {
                //            getClass().getField( "_" + methodName + "_method_name" );
                ((ClassItem) method.getParent()).getActualClass().getField(
                    "__JAC_TRANSLATED");
            } catch (Exception e) {
                //Log.trace("wrappable","* isWrappable("+method+") -> false");
                wrappableMethods.put(method, Boolean.FALSE);
                return false;
            }
            //Log.trace("wrappable","* isWrappable("+method+") -> true");
            wrappableMethods.put(method, new Boolean(true));
            return true;
        } else {
            //Log.trace("wrappable","isWrappable("+method+") -> "+ret);
            return ret.booleanValue();
        }
    }

    public static String printWrappingChain(Interceptor[] wrappingChain) {
        String result = "[";
        for (int i = 0; i < wrappingChain.length; i++) {
            if (i != 0) {
                result += ",";
            }
            result += wrappingChain[i].toString();
        }

        result += "]";
        return result;
    }

    static CompositionAspect ca = null;

    /**
     * Wrap a method of an object.
     *
     * @param wrappee the object to wrap
     * @param wrapper the wrapper
     * @param wrappedMethod the method of wrappee to wrap 
     */
    public static void wrap(
        Wrappee wrappee,
        Wrapper wrapper,
        AbstractMethodItem wrappedMethod) 
    {
        if (wrapMethod(wrappee,wrapper,wrappedMethod)) {
            // Add to list of wrappers
            Wrapping.wrap(wrappee, wrappedMethod.getClassItem(), wrapper);
        }
    }

    /**
     * Wrap a method of an object. Only updates the wrapping chain of
     * the method.
     *
     * @param wrappee the object to wrap
     * @param wrapper the wrapper
     * @param wrappedMethod the method of wrappee to wrap 
     *
     * @return true if the method was actually wrapped
     */
    public static boolean wrapMethod(
        Wrappee wrappee,
        Wrapper wrapper,
        AbstractMethodItem wrappedMethod) 
    {
        if (isWrappable(wrappedMethod)) {
            logger.debug(wrappedMethod + Strings.hash(wrappedMethod)+" is wrapped by " + wrapper);

            WrappingChain wrappingChain =
                getWrappingChain(wrappee, wrappedMethod.getConcreteMethod());
            logger.debug("  "+wrappedMethod.getConcreteMethod()+" -> "+wrappingChain+
                         Strings.hash(wrappingChain));

            if (wrappingChain.contains(wrapper)) {
                logger.debug("  skipping "+wrapper);
                return false;
            }

            int rank = 0;

            ACManager acm = ACManager.getACM();
            if (acm != null) {
                if (ca == null)
                    ca = (CompositionAspect) acm.objects.get(
                        "JAC_composition_aspect");
                if (ca != null) {
                    rank = ca.getWeaveTimeRank(wrappingChain, wrapper);
                    //Log.trace("wrap.wrap","found rank="+rank+"/"+wrappingChain.size());
                } else {
                    /*
                    if( wrappingChain.size() > 0 ) {
                    Log.warning( "no composition aspect found when wrapping "+
                                 wrappedMethod + " with " + 
                                 wrapper.getClass().getName() );
                    }
                    */
                }
            }

            wrappingChain.add(rank, wrapper);
            return true;
            //System.out.println("WrappingChain = "+printWrappingChain(wrappingChain));
        } else {
            //logger.debug(wrappedMethod+" is not wrappable");
            return false;
        }

    }

    /**
     * A nicer way to write <code>wrap</code> when several base methods
     * need to be wrapped.
     *
     * @param wrappee the wrappee (can be null if the wrappeed methods
     * are static)
     * @param wrapper the wrapper where the wrapping method is
     * implemented
     * @param wrappedMethods the names of the wrapped methods 
     */
    public static void wrap(
        Wrappee wrappee,
        Wrapper wrapper,
        AbstractMethodItem[] wrappedMethods) 
    {
        if (wrappedMethods == null)
            return;
        for (int i = 0; i < wrappedMethods.length; i++) {
            if (wrappedMethods[i] != null) {
                wrap(wrappee, wrapper, wrappedMethods[i]);
            }
        }
    }

    /**
     * A string based version of wrap (for simplification purpose).
     *
     * @param wrappee the wrappee (cannot be null, this version only
     * works for non-static methods)
     * @param wrapper the wrapper where the wrapping method is
     * implemented
     * @param wrappedMethods the names of the wrapped methods
     *
     * @see #wrap(Wrappee,Wrapper,AbstractMethodItem[]) 
     */
    public static void wrap(
        Wrappee wrappee,
        Wrapper wrapper,
        String[] wrappedMethods) 
    {
        ClassItem cli = ClassRepository.get().getClass(wrappee);
        if (wrappedMethods == null)
            return;
        for (int i = 0; i < wrappedMethods.length; i++) {
            if (wrappedMethods[i] != null) {
                try {
                    MethodItem[] methods = cli.getMethods(wrappedMethods[i]);
                    for (int j = 0; j < methods.length; j++) {
                        wrap(wrappee, wrapper, methods[j]);
                    }
                } catch (org.objectweb.jac.core.rtti.NoSuchMethodException e) {
                    logger.error("wrap "+wrappee+","+wrapper+","+Arrays.asList(wrappedMethods)+": "+e);
                }
            }
        }
    }

    /**
     * Wraps all the wrappable (public) methods of the current wrappee.
     *
     * @param wrappee the wrappee (can be null, then only wrap static
     * methods)
     * @param wrappeeClass the wrappee's class (can be null if wrappee
     * is not null)
     * @param wrapper the wrapper
     */
    public static void wrapAll(
        Wrappee wrappee,
        ClassItem wrappeeClass,
        Wrapper wrapper) 
    {
        Collection methods = null;
        if (wrappeeClass != null) {
            methods = wrappeeClass.getAllMethods();
        } else {
            methods =
                ClassRepository
                    .get()
                    .getClass(wrappee.getClass())
                    .getAllMethods();
        }
        Iterator i = methods.iterator();
        while (i.hasNext()) {
            wrap(wrappee, wrapper, (AbstractMethodItem) i.next());
        }
    }

    /**
     * Wraps all the writer wrappable (public) methods of the current
     * wrappee.
     *
     * @param wrappee the wrappee (cannot be null, this version only
     * works for non-static methods)
     * @param wrapper the wrapper
     */
    public static void wrapModifiers(Wrappee wrappee, Wrapper wrapper) {
        try {
            Collection methods =
                ClassRepository.get().getClass(wrappee).getAllModifiers();
            Iterator i = methods.iterator();
            while (i.hasNext()) {
                MethodItem method = (MethodItem) i.next();
                wrap(wrappee, wrapper, method);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Definitively removes a wrapper.
     *
     * <p>When this method is called, none of the methods of the
     * wrapper can be used as role methods anymore.
     *
     * <p>This method before calls the <code>unwrapAll</code> to ensure
     * that none of the current wrappee methods are yet wrapped.
     *
     * @param wrappee the wrappee (can be null, then only wrap static
     * methods)
     * @param wrappeeClass the wrappee's class (can be null if wrappee
     * is not null)
     * @param wrapper the wrapper to remove
     *
     * @see #unwrapAll(Wrappee,ClassItem,Wrapper)
     * @see #invokeRoleMethod(Wrappee,String,Object[]) 
     */
    public static void unwrap(
        Wrappee wrappee,
        ClassItem wrappeeClass,
        Wrapper wrapper) 
    {
        List wrappers = getWrappers(wrappee, wrappeeClass);
        if (!wrappers.contains(wrapper))
            return;
        logger.debug("unwrapping " + wrappee + " with " + wrapper);
        wrappers.remove(wrapper);
        getWrapperClasses(wrappee, wrappeeClass).remove(wrapper.getClass());
        unwrapAll(wrappee, wrappeeClass, wrapper);
    }

    /**
     * Removes all wrappers that belong to list of wrappers
     *
     * @param wrappee the wrappee to unwrap (may be null for static methods)
     * @param wrappeeClass the class of the wrappee to unwrap
     * @param acWrappers the wrappers to remove (the wrappers of an aspect component)
     */
    public static void unwrap(
        Wrappee wrappee,
        ClassItem wrappeeClass,
        Collection acWrappers) 
    {
        List wrappers = getWrappers(wrappee, wrappeeClass);
        // Since acWrappers.size() >> wrappers.size(), we iterate on wappers
        Iterator it = wrappers.iterator();
        while(it.hasNext()) {
            Wrapper wrapper = (Wrapper)it.next();
            if (acWrappers.contains(wrapper)) {
                logger.debug("unwrapping " + wrappee + " with " + wrapper);
                it.remove();
                getWrapperClasses(wrappee, wrappeeClass).remove(wrapper.getClass());
                unwrapAll(wrappee, wrappeeClass, wrapper);
            } else {
                logger.debug("leaving "+wrapper);
            }
        }
    }

    /**
     * Unwraps a single method.
     *
     * <p>The wrapper must implement <code>wrappingMethod</code>. If
     * the wrapped method was not actually wrapped at the time this
     * method is called, then this code has no effect.
     *
     * <p>To definitively remove the wrapper so that none of its method
     * will not be considered as role methods anymore, use the
     * <code>unwrap(Wrappee,ClassItem,Wrapper)</code> method.
     *
     * @param wrappee the wrappee (can be null, then the wrappedMethod
     * must be static)
     * @param wrapper the wrapper
     * @param wrappedMethod the name of the method to unwrap
     *
     * @see #unwrap(Wrappee,ClassItem,Wrapper) 
     */
    public static void unwrap(
        Wrappee wrappee,
        Wrapper wrapper,
        AbstractMethodItem wrappedMethod) 
    {
        logger.debug("unwrapping " + (wrappee != null ? wrappee.getClass().getName() : "-")+
                     "."+wrappedMethod+"("+Strings.hex(wrappedMethod)+"-"+
                     Strings.hex(wrappedMethod.getClassItem())+")"+
                     " with "+wrapper+"???");
        WrappingChain wrappingChain = getWrappingChain(wrappee, wrappedMethod.getConcreteMethod());
        for (int i=0; i<wrappingChain.size(); i++) {
            if (wrappingChain.get(i) == wrapper) {
                logger.debug("unwrapping "+wrappedMethod+"("+Strings.hex(wrappedMethod)+")"+
                             " with "+wrapper);
                wrappingChain.remove(i);
            }
        }
    }

    public static void unwrapAll(
        Wrappee wrappee,
        ClassItem wrappeeClass,
        Wrapper wrapper) 
    {
        logger.debug("unwrapAll "+wrappeeClass+"-"+Strings.hex(wrappeeClass)+
                     "-"+wrappeeClass.getClass().getClassLoader());
        Collection methods = wrappeeClass.getAllMethods();
        Iterator i = methods.iterator();
        while (i.hasNext()) {
            AbstractMethodItem m = (AbstractMethodItem)i.next();
            if (!(m instanceof MixinMethodItem))
                unwrap(wrappee, wrapper, m);
        }
    }

    /**
     * Tells wether a wrapper wraps a wrappee or a class
     */
    public static boolean isWrappedBy(
        Wrappee wrappee,
        ClassItem wrappeeClass,
        Wrapper wrapper) 
    {
        return getWrappers(wrappee, wrappeeClass).contains(wrapper);
    }

    /**
     * Returns true if the wrappee or the wrappeeClass is wrapped by a
     * wrapper class.
     */
    public static boolean isWrappedBy(
        Wrappee wrappee,
        ClassItem wrappeeClass,
        Class wrapperClass) 
    {
        return getWrapperClasses(wrappee, wrappeeClass).contains(wrapperClass);
    }

    /**
     * Tells wether a wrappee has a wrapper whose class can be
     * assigned to a given wrapper class.
     */
    public static boolean isExtendedBy(
        Wrappee wrappee,
        ClassItem wrappeeClass,
        Class wrapperClass) 
    {
        Iterator i = getWrapperClasses(wrappee, wrappeeClass).iterator();
        while (i.hasNext()) {
            Class cl = (Class) i.next();
            if (wrapperClass.isAssignableFrom(cl))
                return true;
        }
        return false;
    }

    public static void addExceptionHandler(
        Wrappee wrappee,
        Wrapper wrapper,
        String method,
        AbstractMethodItem listenedMethod) 
    {
        Vector catchingChain =
            (Vector) getCatchingChain(wrappee, listenedMethod);

        Object[] catchingMethod = new Object[2];
        catchingMethod[0] = wrapper;
        Method[] methods =
            ClassRepository.getDirectMethodAccess(wrapper.getClass(), method);
        if (methods.length > 0) {
            catchingMethod[1] = methods[0];
            catchingChain.add(catchingMethod);
        } else {
            throw new NoSuchMethodError(
                "No such method "
                    + method
                    + " in class "
                    + wrapper.getClass().getName());
        }
    }

    public static void addExceptionHandler(
        Wrappee wrappee,
        Wrapper wrapper,
        String method) 
    {
        Collection meths =
            ClassRepository.get().getClass(wrappee.getClass()).getAllMethods();
        Iterator i = meths.iterator();
        while (i.hasNext()) {
            addExceptionHandler(
                wrappee,
                wrapper,
                method,
                (AbstractMethodItem) i.next());
        }
    }

    /**
     * Invokes a role method on the wrappee. The first wrapper which
     * defines a role method with that name is used, so in order to
     * avoid ambiguity, it is preferable to use
     * invokeRoleMethod(Wrappee,Class,String,Object[])
     * 
     * @param wrappee the wrappee (must be wrapped by a wrapper that
     * supports the role method)
     * @param methodName the name of the role method to invoke.
     * @param parameters the parameters.
     * @return the returned object
     * 
     * @see #invokeRoleMethod(Wrappee,Class,String,Object[]) 
     */
    public static Object invokeRoleMethod(
        Wrappee wrappee,
        String methodName,
        Object[] parameters) 
    {
        Iterator wrappers = getWrappers(wrappee, null).iterator();
        Object ret = null;
        MethodItem method = null;
        Object wrapper = null;

        // Seek the role method
        while (wrappers.hasNext()) {
            wrapper = wrappers.next();
            try {
                method =
                    (MethodItem) ClassRepository.get().getClass(
                        wrapper).getAbstractMethods(
                        methodName)[0];
            } catch (org.objectweb.jac.core.rtti.NoSuchMethodException e) {
            }
            if (method != null)
                break;
        }
        if (method == null) {
            logger.warn(
                "no such role method " + methodName
                    + " found on " + wrappee.getClass());
            return ret;
        }

        try {

            // Starts a new interaction
            //Collaboration.get().newInteraction();
            Object[] actualParameters;
            if (method.getParameterTypes().length > 0
                && method.getParameterTypes()[0] == Wrappee.class) {
                actualParameters =
                    new Object[method.getParameterTypes().length];
                actualParameters[0] = wrappee;
                System.arraycopy(
                    parameters,
                    0,
                    actualParameters,
                    1,
                    actualParameters.length - 1);
            } else {
                actualParameters = parameters;
            }
            // invoke...
            ret = method.invoke(wrapper, actualParameters);

        } finally {
            //Collaboration.get().endOfInteraction();
        }
        return ret;
    }

    /**
     * Invokes a role method on the wrappee.
     *
     * @param wrappee the wrappee (must be wrapped by a wrapper that
     * supports the role method).
     * @param wrapperClass the class of the role method to invoke.
     * @param methodName the name of the role method to invoke.
     * @param parameters the parameters.
     * @return the returned object
     */
    public static Object invokeRoleMethod(
        Wrappee wrappee,
        Class wrapperClass,
        String methodName,
        Object[] parameters) 
    {
        return invokeRoleMethod(
            wrappee,
            null,
            wrapperClass,
            methodName,
            parameters);
    }

    /**
     * Invokes a role method on the wrappee.
     *
     * @param wrappee the wrappee (must be wrapped by a wrapper that
     * supports the role method).
     * @param wrappeeClass the class of the wrapper (for static methods)
     * @param wrapperClass the class of the role method to invoke.
     * @param methodName the name of the role method to invoke.
     * @param parameters the parameters.
     * @return the returned object
     */

    public static Object invokeRoleMethod(
        Wrappee wrappee,
        ClassItem wrappeeClass,
        Class wrapperClass,
        String methodName,
        Object[] parameters) 
    {
        loggerRole.debug("invokeRoleMethod "    + wrapperClass + "." + methodName
                         + " on " + wrappee + "(" + wrappeeClass    + ")");
        if (wrappee == null)
            ACManager.getACM().whenUsingNewClass(wrappeeClass);

        Iterator wrappers = getWrappers(wrappee, wrappeeClass).iterator();
        Object ret = null;
        MethodItem method = null;
        Object wrapper = null;

        loggerRole.debug("wrappers for "    + wrappee + "(" + wrappeeClass + ") : "
                         + getWrappers(wrappee, wrappeeClass));

        // Seek the role method
        while (wrappers.hasNext()) {
            wrapper = wrappers.next();
            if (wrapperClass.isAssignableFrom(wrapper.getClass())) {
                try {
                    method =
                        (MethodItem) ClassRepository.get().getClass(
                            wrapper).getAbstractMethods(
                            methodName)[0];
                } catch (org.objectweb.jac.core.rtti.NoSuchMethodException e) {
                    logger.warn(
                        "no such role method " + methodName
                            + " found on " + wrappee.getClass());
                    return ret;
                }
            }
            if (method != null)
                break;
        }

        if (method == null) {
            logger.warn(
                "no such role method "+ methodName + 
                " found on " + wrappee.getClass());
            return ret;
        }
        try {

            // Starts a new interaction
            //Collaboration.get().newInteraction();
            Object[] actualParameters;
            Class parameterTypes[] = method.getParameterTypes();
            if (parameterTypes.length > 0
                && parameterTypes[0] == Wrappee.class) {
                actualParameters = new Object[parameterTypes.length];
                actualParameters[0] = wrappee;
                System.arraycopy(
                    parameters,
                    0,
                    actualParameters,
                    1,
                    actualParameters.length - 1);
            } else {
                actualParameters = parameters;
            }

            // invoke...
            ret = method.invoke(wrapper, actualParameters);

        } finally {
            //Collaboration.get().endOfInteraction();
        }
        return ret;
    }

    /**
     * This method can be used to shortcut the wrapping chain and
     * directly call the original method.
     * 
     * <p><b>NOTE</b>: avoid doing this unless you really know what you
     * are doing. It is not clean to shortcut the whole wrapping
     * chain. Instead, use the <code>invoke</code> method or, if you
     * really need to,
     * <code>AspectComponent.before/afterRunningWrapper()</code> to
     * skip some wrappers.
     *
     * @param wrappee the object that supports the method
     * @param name the name of the method to call
     * @param parameters the argument values
     * @return the called method return value as an object
     *
     * @see AspectComponent#beforeRunningWrapper(Wrapper,String)
     * @see AspectComponent#afterRunningWrapper(Wrapper,String)
     **/

    public static Object invokeOrg(
        Wrappee wrappee,
        String name,
        Object[] parameters) 
    {
        Object ret = null;

        Method[] methods = wrappee.getClass().getMethods();
        boolean ok = false;
        boolean found = false;
        String orgName =
            "_org_"
                + name
                + "_"
                + NamingConventions.getShortClassName(wrappee.getClass());
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getParameterTypes().length == parameters.length
                && methods[i].getName().equals(orgName)) {
                found = true;
                try {
                    ok = false;
                    ret = methods[i].invoke(wrappee, parameters);
                    ok = true;
                } catch (IllegalArgumentException e) {
                } catch (InvocationTargetException e) {
                    Throwable t = e.getTargetException();
                    if (t instanceof RuntimeException) {
                        throw (RuntimeException) t;
                    }
                    throw new RuntimeException(
                        "invokeOrg("
                            + wrappee + ","
                            + name + ","
                            + Arrays.asList(parameters) + ") failed: " + e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(
                        "invokeOrg("
                            + wrappee + ","
                            + name + ","
                            + Arrays.asList(parameters) + ") failed: " + e);
                }
                if (ok)
                    break;
            }
        }
        // if there was no "_org_" prefixed method, try to call the method directly
        if (!found) {
            logger.warn(
                "original method "
                    + orgName
                    + " was not found in "
                    + wrappee.getClass().getName());
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals(name)
                    && methods[i].getParameterTypes().length
                        == parameters.length) {
                    try {
                        ok = false;
                        ret = methods[i].invoke(wrappee, parameters);
                        ok = true;
                    } catch (IllegalArgumentException e) {
                    } catch (InvocationTargetException e) {
                        Throwable t = e.getTargetException();
                        if (t instanceof RuntimeException) {
                            throw (RuntimeException) t;
                        }
                        throw new RuntimeException(
                            "invokeOrg("
                                + wrappee + ","
                                + name + ","
                                + Arrays.asList(parameters) + ") failed: " + e);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(
                            "invokeOrg("
                                + wrappee + ","
                                + name + ","
                                + Arrays.asList(parameters) + ") failed: " + e);
                    }
                    if (ok)
                        break;
                }
            }
        }
        if (!ok) {
            throw new IllegalArgumentException(
                "No such original method has been found: "
                    + wrappee.getClass().getName()
                    + "."
                    + name);
        }

        return ret;
    }


    public static Object invokeOrg(
        Wrappee wrappee,
        MethodItem method,
        Object[] parameters) 
    {
        try {
            return method.getOrgMethod().invoke(wrappee, parameters);
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            }
            throw new RuntimeException(
                "invokeOrg("
                + wrappee + "," + method.getName() + "," + Arrays.asList(parameters)
                + ") failed: " + e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(
                "invokeOrg("
                + wrappee + "," + method.getName() + "," + Arrays.asList(parameters)
                + ") failed: " + e);
        }
    }

    public static Object clone(Object org) {
        return org;
    }

    // -------
    // Internal JAC method used to initialize or execute wrapping chains

    /**
     * This method factorizes the common code that is used when the
     * next wrapper is called.
     *
     * @param interaction the method call being intercepted
     */
    public static Object nextWrapper(Interaction interaction)
    //      throws Throwable
    {
        Logger methodLogger = Logger.getLogger("wrapping." + interaction.method.getName());
        if (methodLogger.isDebugEnabled())
            methodLogger.debug(
                "nextWrapper "
                + interaction.method.getParent()+"."+interaction.method
                + ", rank=" + interaction.rank
                + ", isConstructor=" + (interaction.method instanceof ConstructorItem));
        Object ret = null;
        //boolean start_rank = interaction.rank == 0;
        Method m = null; // invoked method (wrapping method or _org_)

        //Collaboration collaboration = Collaboration.get();
        int rank = interaction.rank;
        Wrappee wrappee = interaction.wrappee;
        AbstractMethodItem method = interaction.method;
        String forcedName = null;

        try {

            if (rank == 0) {
                //collaboration.newInteraction();

                //Log.trace("jac", "done.");
                // here, the attribute is persistent for optimization purpose
                // we save it to restore it at the end

                //oldApplicationName = collaboration.getCurApp();
                if (method instanceof ConstructorItem) {
                    // only call whenUsingNewInstance once for each method
                    if (((ClassItem) method.getParent()).getActualClass()
                        == wrappee.getClass()) {
                        ObjectRepository.register(wrappee);
                        if (ACManager.getACM() != null) {
                            loggerWuni.debug("calling WUNI on " + Strings.hex(wrappee));
                            ACManager.getACM().whenUsingNewInstance(
                                interaction);
                            interaction.wrappingChain =
                                getWrappingChain(wrappee, method).chain;
                        }
                        // <HACK>
                    } else {
                        // We have to save the forced name if there's one,
                        // or it could be used for some object created by
                        // the super constructor (like collections's vector)
                        Collaboration collab = Collaboration.get();
                        forcedName = (String) collab.getAttribute(Naming.FORCE_NAME);
                        if (forcedName != null) {
                            collab.removeAttribute(Naming.FORCE_NAME);
                        }
                    }
                    // </HACK>
                } else if (method.isStatic() && ACManager.getACM() != null) {
                    //wrappingChain=getWrappingChain(wrappee,method);
                    loggerWuni.debug("calling WUNI for static method "
                                     + method.getParent() + "." + method);
                    ACManager.getACM().whenUsingNewClass(
                        interaction.getClassItem());
                    interaction.wrappingChain =
                        getWrappingChain(wrappee, method).chain;
                }
            }

            if (methodLogger.isDebugEnabled())
                methodLogger.debug("wrapping chain: "
                                   + printWrappingChain(interaction.wrappingChain));
            //System.out.println("===>"+interaction.wrappingChain);

            if (rank < interaction.wrappingChain.length) {
                Interceptor to_invoke =
                    interaction.wrappingChain[rank];
                if (method instanceof ConstructorItem){
                    ret = ((ConstructorInterceptor)to_invoke).construct(interaction);
                } else {
                    ret = ((MethodInterceptor)to_invoke).invoke(interaction);
                }
                //Log.trace("jac", "wrapping method returns " + ret );
            } else {
                //Log.trace("jac", "calling org " + method );
                //               collaboration.setCurAC(null);
                if (method instanceof ConstructorItem || method.isStatic()) {
                    try {
                        ret =
                            ((ClassItem) method.getParent())
                                .getActualClass()
                                .getMethod(
                                    "_org_" + method.getName(),
                                    method.getParameterTypes())
                                .invoke(wrappee, interaction.args);
                    } catch (java.lang.NoSuchMethodException e) {
                        throw new RuntimeException(
                            "Failed to invoke org method "
                                + "_org_" + method.getName()
                                + " on " + wrappee
                                + ": " + e);
                    }
                } else {
                    m = ((MethodItem) method).getOrgMethod();
                    if (m != null) {
                        ret = m.invoke(wrappee, interaction.args);
                    } else {
                        try {
                            m =
                                ClassRepository.getDirectMethodAccess(
                                    wrappee.getClass(),
                                    "_org_" + method.getName())[0];
                            ret = m.invoke(wrappee, interaction.args);
                        } catch (IllegalArgumentException ill) {
                            try {
                                ret =
                                    ClassRepository.invokeDirect(
                                        wrappee.getClass(),
                                        "_org_" + method.getName(),
                                        wrappee,
                                        interaction.args);
                            } catch (java.lang.NoSuchMethodException e) {
                                throw new RuntimeException(
                                    "Failed to invoke org method "
                                        + "_org_" + method.getName()
                                        + " on " + wrappee
                                        + ": " + e);
                            }
                        }
                    }
                }

                //Log.trace("jac", "end of calling org " + method + " ret="+ret);

            }

        } catch (Throwable t) {
            if(t instanceof InvocationTargetException)
                t=((InvocationTargetException)t).getTargetException();
            loggerExc.info(interaction.wrappee+ "."+ method
                      + "(" + m + ") catching " + t);
            WrappedThrowableException wrapped = null;
            if (t instanceof WrappedThrowableException) {
                wrapped = (WrappedThrowableException) t;
                t = ((WrappedThrowableException) t).getWrappedThrowable();
            }
            // loggerExc.info("wrapped exception is "+t);
            /*
            if (start_rank) {
               collaboration.endOfInteraction();            
            }            
            */

            boolean caught = false;

            List catchingChain = getCatchingChain(wrappee, method);

            if (catchingChain != null) {
                Iterator it = catchingChain.iterator();
                loggerExc.info("trying to find catcher...");
                while (it.hasNext()) {
                    try {
                        Object[] wm = (Object[]) it.next();
                        loggerExc.info("trying " + wm[1]
                                  + " on " + wm[0]
                                  + " (" + t + ")"
                                  + " ->" + t.getClass());
                        ((Method) wm[1]).invoke(wm[0], new Object[] { t });
                        loggerExc.info("caught.");
                        caught = true;
                        // several handlers on the same exception can cause trouble!!!
                        // e.g.: InputWrapper.inputCanceled + TransactionWrapper.rollback!
                        // => handlers should be correctly ordered!
                        break;
                    } catch (Exception e1) {
                        if ((e1 instanceof InvocationTargetException)) {
                            //logger.warn("oops! Exception handler generates an exception");
                            throw new WrappedThrowableException(
                                ((InvocationTargetException) e1)
                                    .getTargetException());
                        }
                    }
                }
            }

            if (!caught) {
                loggerExc.info("forwarding exception " + t);
                loggerExc.debug("forwarding exception",t);
                if (wrapped != null) {
                    throw wrapped;
                } else {
                    throw new WrappedThrowableException(t);
                }
            } else {
                loggerExc.info("silent exception");
            }
        }

        if (forcedName != null) {
            Collaboration.get().addAttribute(Naming.FORCE_NAME, forcedName);
        }
        return ret;
    }

    public static Object methodNextWrapper(Interaction interaction) {
        //Collaboration collaboration = Collaboration.get();
        Logger methodLogger = Logger.getLogger("wrapping." + interaction.method.getName());
        if (methodLogger.isDebugEnabled())
            methodLogger.debug("methodNextWrapper " + interaction.method.getLongName());
        try {
            if (interaction.wrappingChain.length > 0) {
                return (
                    (MethodInterceptor) interaction.wrappingChain[0]).invoke(
                    interaction);
            } else {
                if (methodLogger.isDebugEnabled())
                    methodLogger.debug(
                        "invoke org method "
                        + ((MethodItem) interaction.method).getOrgMethod());
                return ((MethodItem) interaction.method).getOrgMethod().invoke(
                    interaction.wrappee,
                    interaction.args);
            }
        } catch (Throwable t) {
            if(t instanceof InvocationTargetException)
                t=((InvocationTargetException)t).getTargetException();
            loggerExc.info("Catching " + t + " (stack trace follows)",t);
            WrappedThrowableException wrapped = null;
            if (t instanceof WrappedThrowableException) {
                wrapped = (WrappedThrowableException) t;
                t = ((WrappedThrowableException) t).getWrappedThrowable();
            }
            boolean caught = false;

            List catchingChain =
                getCatchingChain(interaction.wrappee, interaction.method);

            if (catchingChain != null) {
                Iterator it = catchingChain.iterator();
                while (it.hasNext()) {
                    try {
                        Object[] wm = (Object[]) it.next();
                        ((Method) wm[1]).invoke(wm[0], new Object[] { t });
                        caught = true;
                        // several handlers on the same exception can cause trouble!!!
                        // e.g.: InputWrapper.inputCanceled + TransactionWrapper.rollback!
                        // => handlers should be correctly ordered!
                        break;
                    } catch (Exception e1) {
                        if ((e1 instanceof InvocationTargetException)) {
                            //logger.warn("oops! Exception handler generates an exception");
                            throw new WrappedThrowableException(
                                ((InvocationTargetException) e1)
                                    .getTargetException());
                        }
                    }
                }
            }

            if (!caught) {
                loggerExc.info("forwarding exception " + t);
                loggerExc.debug("forwarding exception",t);
                if (wrapped != null) {
                    throw wrapped;
                } else {
                    throw new WrappedThrowableException(t);
                }
            }

            return null;
        }

    }
}
