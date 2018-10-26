/*
  Copyright (C) 2001-2003 Renaud Pawlak <renaud@aopsys.com>

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 2 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  USA */

package org.objectweb.jac.core;

import gnu.regexp.RE;
import gnu.regexp.REException;
import gnu.regexp.RESyntax;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.rtti.AbstractMethodItem;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.core.rtti.CollectionItem;
import org.objectweb.jac.core.rtti.ConstructorItem;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.MethodItem;
import org.objectweb.jac.util.ExtArrays;
import org.objectweb.jac.util.Strings;

/**
 * This class can be used by JAC aspect components to easily define a
 * set of method points on the base program that the aspects will use to
 * modify its behavior.
 *
 * <p>A method pointcut is defined through four pointcut
 * expressions. For the moment, these pointcut expressions are a
 * simple extension of regular expressions -- in an EMACS_LISP syntax
 * (see the GNU-regexp tutorial) that can be combined with the
 * <code>&&</code> operator (in this case all the regexps must match)
 * or the <code>||</code> operator (in this case, only one regexp must
 * match). Before a regexp, you can use the <code>!</code> (not)
 * operator to inverse the matching.
 *
 * <p>Depending on the pointcut expression, you can also
 * use keywords that will be dynamically interpreted.  We intend to
 * provide a more complete and suited desciption language. Note that
 * for the moment, the tricky cases that cannot be handled by these
 * expressions can be treated by programming the
 * <code>AspectComponent.whenUsingNewInstance</code> method.
 *
 * <ul><li><i>The host expression</i>: filters the components that
 * belong or not to the pointcut on a location basis. To be activated,
 * the name of the host where the pointcut is currently applied must
 * match this expression.</li>
 *
 * <li><i>The wrappee expression</i>: filters the components that
 * belong or not to the pointcut on a per-component basis. This
 * expression assumes that the component is named (that the naming
 * aspect is woven). If you need to modify all the components of the
 * same class equaly, then this expression should be <code>".*"</code>
 * and the <i>wrappee-class expression</i> should be used.</li>
 *
 * <li><i>The wrappee-class expression</i>: filters the components
 * that belong or not to the pointcut on a per-class basis. For
 * instance, <code>"A || B"</code> says that only the classes that are
 * named A or B belong to the pointcut; <code>packagename.* &&
 * classname</code> matches the class that belong to
 * <code>packagename</code> and that are named
 * <code>classname</code>.</li>
 *
 * <li><i>The wrappee-method expression</i>: for all the components
 * that match the two previous expressions, defines which methods must
 * be modified by the pointcut. For instance,
 * <code>"set.*(int):void"</code> matches all the methods that have
 * name that begins with "set", that take only one integer argument,
 * and that return nothing. The regular expression can contain several
 * builtin keywords to automatically match methods with special
 * semantics (for instance, <code>ALL</code>: all the methods of the
 * class, <code>MODIFIERS</code>: all the state modifiers,
 * <code>ACCESSORS</code>: all the state setters,
 * <code>GETTERS(fielname1,fieldname2)</code>: the getter for the
 * given field, <code>SETTERS(...)</code>: the setter for the given
 * field), <code>WRITERS(...)</code>: all the methods which modify the
 * given field. For instance, <code>FIELDSETTERS && !.*(int).*</code>
 * matches all the field setters, excluding the ones for the integer
 * fields.</li></ul>
 *
 * <p>For more informations on pointcut expressions, please see the <a
 * href="doc/tutorial.html#3.2.5>programmer's guide</a>.
 *
 * <p>A pointcut is also related to a wrapping class and a wrapping
 * method (and a precise wrapper, instance of the wrapping class, can
 * be specified if it is known). This couple implements the aspect
 * code in all the points that matches the over-depicted pointcut
 * expression.
 *
 * <p>Finally, when a pointcut is applied to the matching elements,
 * wrappers (instances of the wrapping class) are created (except if
 * the intialization wrapper is not null). In this case, the
 * <code>one2one</code> flag tells if one new wrapper instance nust be
 * created for each base component or if one unique wrapper instance
 * must be used for all the components.
 *
 * @author <a href="http://cedric.cnam.fr/~pawlak/index-english.html">Renaud Pawlak</a>
 * @see AspectComponent */

public class MethodPointcut extends Pointcut {
    static Logger logger = Logger.getLogger("pointcut");
    static Logger loggerName = Logger.getLogger("pointcut.name");
    static Logger loggerHost = Logger.getLogger("pointcut.host");
    static Logger loggerPath = Logger.getLogger("pointcut.path");
    static Logger loggerKeywords = Logger.getLogger("pointcut.keywords");
    static Logger loggerCreate = Logger.getLogger("pointcut.create");
    static Logger loggerWrappers = Logger.getLogger("wappers");

    Vector wrappeeExprs = new Vector();
    Vector wrappeeRegexps = new Vector();
    Vector wrappeeClassExprs = new Vector();
    Vector wrappeeClassRegexps = new Vector();
    Vector wrappeeMethodExprs = new Vector();
    Vector wrappeeMethodRegexps = new Vector();
    Vector hostExprs = new Vector();
    Vector hostRegexps = new Vector();
    Vector iwrappeeExprs = new Vector();
    Vector iwrappeeClassExprs = new Vector();
    Vector iwrappeeMethodExprs = new Vector();
    Vector ihostExprs = new Vector();
    String wrappeeExpr;
    String wrappeeClassExpr;
    String wrappeeMethodExpr;
    String hostExpr;
    String wrappingClassName;
    String methodName;
    Object[] methodArgs;
    String exceptionHandler;
    boolean one2one = true;
    boolean allInstances = false;
    boolean allHosts = false;
    boolean allClasses = false;
    AspectComponent aspectComponent = null;

    /** Class of the wrapper */
    ClassItem wrapperClass;
    /** The wrapping method */
    //MethodItem wrappingMethod;

    static String[] wrappeeKeywords = new String[] {
        "ALL"
    };
    static String[] classKeywords = new String[] {
        "ALL",
        "COLLECTIONS"
    };
    static String[] methodKeywords = new String[] {
        "ALL",
        "STATICS",
        "CONSTRUCTORS",
        "MODIFIERS",
        "REFACCESSORS",
        "COLACCESSORS",
        "ACCESSORS",
        "COLSETTERS",
        "FIELDSETTERS",
        "REFSETTERS",
        "SETTERS", /* args=a list of fields or tags */
        "WRITERS", /* args=a list of fields or tags */
        "COLGETTERS",
        "FIELDGETTERS",
        "REFGETTERS",
        "GETTERS", /* args=a list of fields or tags */
        "ADDERS", /* no args | args=list of collection or tags */
        "REMOVERS" /* no args | args=list of collection or tags */
    };
    static String[] hostKeywords = new String[] {
        "ALL"
    };

    /**
     * Returns a readable description of the pointcut. */

    public String toString() {
        return "pointcut {"+wrappingClassName+","+
            methodName+
            "}->{"+wrappeeExpr+","+wrappeeClassExpr+","+
            wrappeeMethodExpr+","+hostExpr+"}";
    }

    Wrapper commonWrapper = null;
    Wrapper initWrapper = null;

    /**
     * Instantiates a new pointcut with the given characterisitics.
     *
     * @param aspectComponent the aspect component this pointcut
     * belongs to
     * @param wrappeeExpr the wrappee definition that matches the names
     * as defined by the naming aspect
     * @param wrappeeClassExpr the wrappee class expression (matches
     * the fully qualified class name)
     * @param wrappeeMethodExpr the wrappee method expression (matches
     * the full method name as defined by the rtti)
     * @param initWrapper the instance of the wrapper used by this
     * pointcut, if null a new one is automatically created depending
     * on the one2one flag
     * @param wrappingClassName the name of the wrapper class
     * @param methodName the name of the aspect component
     * method to upcall when a mathing object is used
     * @param methodArgs the argument values for this method
     * method to upcall when a matching object is used
     * @param hostExpr a regular expression that macthes the hosts
     * where the pointcut has to be applied (if null or empty string,
     * default is ".*" which means that the pointcut will be applied on
     * all the hosts)
     * @param one2one true if each new wrapped instance corresponds to
     * one different wrapper (it has no effect if
     * <code>initWrapper</code> is not null 
     */
    public MethodPointcut(AspectComponent aspectComponent, 
                          String wrappeeExpr, 
                          String wrappeeClassExpr, 
                          String wrappeeMethodExpr,
                          Wrapper initWrapper,
                          String wrappingClassName,
                          String methodName,
                          Object[] methodArgs,
                          String hostExpr,
                          String exceptionHandler,
                          boolean one2one) {

        this.aspectComponent = aspectComponent;
        this.wrappeeExpr = wrappeeExpr;
        this.wrappeeClassExpr = wrappeeClassExpr;
        this.wrappeeMethodExpr = wrappeeMethodExpr;
        this.hostExpr = hostExpr;
        this.initWrapper = initWrapper;
        this.wrappingClassName = wrappingClassName;
        this.methodName = methodName;
        this.methodArgs = methodArgs;
        this.exceptionHandler = exceptionHandler;
        this.one2one = one2one;

        parseExpr("wrappee class expression", null, null,
                  wrappeeClassExpr, classKeywords, 
                  wrappeeClassExprs, iwrappeeClassExprs);
        wrappeeClassRegexps = buildRegexps(wrappeeClassExprs);
      
        parseExpr("host expression", null, null,
                  hostExpr, hostKeywords,
                  hostExprs, ihostExprs);
        hostRegexps = buildRegexps(hostExprs);

        if (wrappeeExpr.equals("ALL") || wrappeeExpr.equals(".*")) {
            allInstances = true;
        }
        if (hostExpr.equals("ALL") || hostExpr.equals(".*")) {
            allHosts = true;
        }
        if (wrappeeClassExpr.equals("ALL") || wrappeeClassExpr.equals(".*")) {
            allClasses = true;
        }

        if (!allInstances) {
            parseExpr("wrappee expression", null, null,
                      wrappeeExpr, wrappeeKeywords,
                      wrappeeExprs, iwrappeeExprs);

            wrappeeRegexps = buildRegexps(wrappeeExprs);
        }

        loggerCreate.debug(aspectComponent+" new pointcut "+this);
    }

    /**
     * Build a vector of regular expressions
     * @param patterns a collection of strings
     * @return a vector of the size of patterns filled with RE.
     */
    Vector buildRegexps(Vector patterns) {
        Vector result = new Vector(patterns.size());
        Iterator i = patterns.iterator();
        while (i.hasNext()) {
            String pattern = (String)i.next();
            try {
                result.add(buildRegexp(pattern));
            } catch(REException e) {
                logger.error("invalid regexp \""+pattern+"\":"+e);
            }
        }
        return result;
    }

    public static RE buildRegexp(String pattern) throws REException {
        return new RE(Strings.replace(pattern,"$","\\$"),0,
                      RESyntax.RE_SYNTAX_EMACS);        
    }

    /**
     * Instanciate the wrapper, and initialize wrapperClass
     * @parapm wrappee unused
     */
    Wrapper buildWrapper(Wrappee wrappee) {
        try {
            if (wrapperClass==null) {
                wrapperClass = ClassRepository.get().getClass(wrappingClassName);
            }
            if (methodArgs!=null) {
                if (wrapperClass.isInner())
                    return (Wrapper)wrapperClass.newInstance(
                        ExtArrays.add(0,aspectComponent,methodArgs));
                else
                    return (Wrapper)wrapperClass.newInstance(
                        ExtArrays.add(0,aspectComponent,methodArgs));
            } else {
                if (wrapperClass.isInner())
                    return (Wrapper)wrapperClass.newInstance(
                        new Object[] {aspectComponent,aspectComponent});
                else 
                    return (Wrapper)wrapperClass.newInstance(
                        new Object[] {aspectComponent});
            }
        } catch(Exception e) {
            logger.error("buildWrapper failed for "+wrappee,e);
        }
        return null;
    }

    /**
     * Applies this pointcut to the given wrappee.
     *
     * <p>The pointcut application consists in wrapping the wrappee
     * accordingly to the pointcut description. Note that in JAC, the
     * pointcut is usually applied on a per-component basis, and when
     * the component (wrappee) is used for the first time.
     *
     * @param wrappee the component the pointcut is applied to
     * @param cl class  the pointcut is applied to
     */
  
    public synchronized void applyTo(Wrappee wrappee, ClassItem cl) {
        
        // REGRESSION: CANNOT HANDLE CLONES FOR THE MOMENT
        //if( wrappee!=null && wrappee.isCloned() ) {
        //   Log.trace("pointcut","do not apply aspects on clones");
        //   return;
        //}
        
        logger.info("apply "+this+" on "+wrappee+" - "+cl);
        

        if (!isClassMatching(wrappee,cl)) { return; }
        Logger classLogger = Logger.getLogger("pointcut."+cl);
        if (classLogger.isDebugEnabled())
            classLogger.debug("class is matching"); 
        
        if (!isHostMatching(wrappee,cl)) { return; }
        if (classLogger.isDebugEnabled())
            classLogger.debug("host is matching");       
        
        if (!isNameMatching(wrappee,cl)) return;
        if (classLogger.isDebugEnabled())
            classLogger.debug("name is matching"); 
        
        // upcalls the method if exist
        if (methodName!=null) {
            try {
                classLogger.debug(
                    "Upcalling "+aspectComponent.getClass().getName()+
                    "."+methodName+"("+Arrays.asList(methodArgs)+")");
                Object[] args = ExtArrays.add(0,wrappee,methodArgs);
                ClassRepository.get().getClass(aspectComponent.getClass())
                    .invoke(aspectComponent, methodName, args); 
            } catch(Exception e) {
                logger.error("Upcalling failed",e);
            }
        }

        // stops if no wrapping infos if given
        if (initWrapper==null 
            && wrappingClassName==null) 
            return;
        Collection methodsToWrap = 
            wrappee!=null ? getMatchingMethodsFor(wrappee,cl) : getMatchingStaticMethodsFor(cl);
        Wrapper wrapper = null;
        if (initWrapper!=null) {
            wrapper = commonWrapper = initWrapper;
            wrapperClass = ClassRepository.get().getClass(wrapper);
        } else {
            if (one2one) {
                wrapper = buildWrapper(wrappee);
            } else {
                if (commonWrapper==null)
                    commonWrapper = buildWrapper(wrappee);
                wrapper = commonWrapper;
            }
        }
      
        //if (wrappingMethod==null) {
        //   wrappingMethod = wrapperClass.getMethod(wrappingMethodName);
        //}

        if (methodsToWrap!=null && methodsToWrap.size()>0) {
            classLogger.debug(
                "applying "+wrappingClassName+
                " on "+cl.getName()+" ("+
                NameRepository.get().getName(wrappee)+")"); 
            classLogger.debug("methods to wrap="+methodsToWrap); 
        }

        loggerWrappers.debug("new pointcut: wrapper="+wrapper+" methods to wrap="+methodsToWrap);

        // wrap the methods
        //Log.trace("pointcut.wrap","exception handlers for "+wrapper.getClass()+": "+eh); 
        Iterator it = methodsToWrap.iterator();
        boolean wrapped = false;
        while (it.hasNext()) {
            AbstractMethodItem method = (AbstractMethodItem)it.next();
            classLogger.debug(
                "Wrapping "+method.getLongName()+" with "+wrappingClassName+
                " on "+wrappee+" - "+cl.getName());
            //wrapped = wrapped || Wrapping.wrapMethod(wrappee,wrapper,method);
            if (Wrapping.wrapMethod(wrappee,wrapper,method) && !wrapped) {
                // postponing this at after the loop seems to have
                // strange effects on static methods wrapping
                Wrapping.wrap(wrappee,cl,wrapper);
                wrapped = true;
            }

            // install exeption handler if needed
            if (exceptionHandler!=null) {
                Wrapping.addExceptionHandler(wrappee,wrapper,
                                             exceptionHandler,method);
            }
        }
        loggerWrappers.debug("wrapped = "+wrapped);
        if (methodsToWrap.size()==0 && one2one) {
            Wrapping.wrap(wrappee,cl,wrapper);
        }
    }

    /* Cache of matching methods (ClassItem -> Vector of AbstractMethodItem)*/
    Hashtable cache = new Hashtable();

    /* Cache of matching static methods (ClassItem -> Vector of AbstractMethodItem)*/
    Hashtable staticsCache = new Hashtable();

    /**
     * Gets the methods of the wrappee that are modified by this
     * pointcut.
     *
     * @param wrappee the component to test
     * @param cli the class of the wrappee
     * @return a vector containing the matching method items 
     */
    protected Collection getMatchingMethodsFor(Wrappee wrappee, ClassItem cli) {
        //Log.trace("pointcut.match."+cli,"getting matching methods for "+
        //          cli.getName()+"("+wrappee+") "+wrappeeMethodExpr); 
        String name = null;
        if (wrappee!=null) {
            name = cli.getName();
        }
        Collection result = (Collection)cache.get(name);
        if (result==null) {
            result = parseMethodExpr(wrappee,cli,wrappeeMethodExpr);
            cache.put(name,result);
            //Log.trace("pointcut.match."+cli,wrappeeMethodExpr+" -> "+result);
        } else {
            //Log.trace("pointcut.match."+cli,2,"methods cache hit for "+
            //          cli.getName()+"("+wrappee+")"); 
        }
        return result;
    }

    protected Collection getMatchingStaticMethodsFor(ClassItem cli) {
        //Log.trace("pointcut.match."+cli,
        //          "getting static matching methods for "+cli.getName()); 
        String name = cli.getName();
        Collection result = (Collection)staticsCache.get(name);
        if (result==null) {
            //Log.trace("pointcut.match."+cli,"method expr="+wrappeeMethodExpr);
            result = parseMethodExpr(null,cli,wrappeeMethodExpr);
            staticsCache.put(name,result);
        } else {
            //Log.trace("pointcut.match."+cli,2,
            //          "methods cache hit for "+cli.getName()); 
        }
        return result;
    }

    /**
     * @param wrappee the object the pointcut applies to
     * @param cli the class the pointcut applies to (in case
     * wrappee==null, for static methods)
     * @param expr the pointcut expression
     * @return A set of method matching the pointcut for the wrappee or class 
     */
    public Collection parseMethodExpr(Wrappee wrappee, ClassItem cli, String expr) {
        //Log.trace("pointcut.parse","parseMethodExpr "+expr+" for "+cli);
        String[] exprs = Strings.split(expr,"&&");
        Collection result = new HashSet();

        if (wrappee==null) {
            result.addAll(cli.getAllStaticMethods());
        } else {
            result.addAll(cli.getAllInstanceMethods());
            result.addAll(cli.getConstructors());
        }
        for (int i=0; i<exprs.length; i++) {
            String curExpr;
            boolean inv = false;
            exprs[i] = exprs[i].trim();
            if (exprs[i].charAt(0)=='!') {
                inv = true;
                curExpr = exprs[i].substring(1).trim();
            } else {
                curExpr = exprs[i];
            }

            String[] subExprs = Strings.split(curExpr,"||");
            HashSet subExprResult = new HashSet();
            for(int j=0; j<subExprs.length; j++) {
                String curSubExpr  = subExprs[j].trim();
                filterMethodKeywords(wrappee,cli,curSubExpr,inv,result,subExprResult);
            }
            //System.out.println((inv?"!":"")+curExpr+" -> "+subExprResult);
            result = subExprResult;
        }
        return result;
    }

    /**
     * Adds methods from source that match an expression to a collection
     *
     * @param wrappee object to match with
     * @param cli class to macth with, used if wrappee==null
     * @param expr the expression to match
     * @param inv wether to keep or reject matching methods
     * @param source method items to chose from
     * @param dest collection to add the matching methods to
     */
    protected void filterMethodKeywords(Object wrappee, ClassItem cli, 
                                        String expr, boolean inv,
                                        Collection source, Collection dest) {

        //System.out.println("EXPR="+(inv?"!":"")+expr+", CLI="+cli);
        String keyword = null;
        for (int i=0; i<methodKeywords.length && keyword==null; i++) {
            if (expr.startsWith(methodKeywords[i])) {
                keyword = methodKeywords[i];
                //System.out.println("   KEYWORD="+keyword);
                List parameters = null;
            
                Iterator it = source.iterator();
                boolean add = false;
                while (it.hasNext()) {
                    AbstractMethodItem method = (AbstractMethodItem)it.next();
                    //System.out.println("      TESTING="+method);
                    add = false;
                    if (keyword.equals("ALL")) {
                        add = !inv;
                    } else if (keyword.equals("MODIFIERS")) {
                        if (parameters==null)
                            parameters = parseParameters(expr.substring(keyword.length()),cli);
                        add = (isWriter(method,parameters) || 
                               isAdder(method,parameters) ||
                               isRemover(method,parameters) || 
                               isCollectionModifier(method,parameters)) 
                            ^ inv;
                    } else if (keyword.equals("ACCESSORS")) {
                        add = method.isAccessor() ^ inv;
                    } else if (keyword.equals("REMOVERS")) {
                        if (parameters==null)
                            parameters = parseParameters(expr.substring(keyword.length()),cli);
                        add = isRemover(method,parameters) ^ inv;
                    } else if (keyword.equals("ADDERS")) {
                        if (parameters==null)
                            parameters = parseParameters(expr.substring(keyword.length()),cli);
                        add = isAdder(method,parameters) ^ inv;
                    } else if (keyword.equals("SETTERS")) {
                        if (parameters==null)
                            parameters = parseParameters(expr.substring(keyword.length()),cli);
                        add = isSetter(method,parameters) ^ inv;
                    } else if (keyword.equals("STATICS")) {
                        add = method.isStatic() ^ inv;
                    } else if (keyword.equals("CONSTRUCTORS")) {
                        add = (method instanceof ConstructorItem) ^ inv;
                    } else if (keyword.equals("COLGETTERS")) {
                        add = method.isCollectionGetter() ^ inv;
                    } else if (keyword.equals("COLACCESSORS")) {
                        if (parameters==null)
                            parameters = parseParameters(expr.substring(keyword.length()),cli);
                        add = isCollectionAccessor(method,parameters) ^ inv;
                    } else if (keyword.equals("FIELDGETTERS")) {
                        add = method.isFieldGetter() ^ inv;
                    } else if (keyword.equals("REFGETTERS")) {
                        add = method.isReferenceGetter() ^ inv;
                    } else if (keyword.equals("REFACCESSORS")) {
                        if (parameters==null)
                            parameters = parseParameters(expr.substring(keyword.length()),cli);
                        add = isReferenceAccessor(method,parameters) ^ inv;
                    } else if (keyword.equals("COLSETTERS")) {
                        add = method.isCollectionSetter() ^ inv;
                    } else if (keyword.equals("FIELDSETTERS")) {
                        add = method.isFieldSetter() ^ inv;
                    } else if (keyword.equals("REFSETTERS")) {
                        add = method.isReferenceSetter() ^ inv;
                    } else if (keyword.equals("WRITERS")) {
                        if (parameters==null)
                            parameters = parseParameters(expr.substring(keyword.length()),cli);
                        add = isWriter(method,parameters) ^ inv;
                    } else if (keyword.equals("GETTERS")) {
                        if (parameters==null)
                            parameters = parseParameters(expr.substring(keyword.length()),cli);
                        add = isGetter(method,parameters) ^ inv;
                    }
                    if (add) {
                        dest.add(method.getConcreteMethod());
                        it.remove();
                    }
                }
            }
        }

        // if no keyword was found, use regular expression matching
        if (keyword==null) {
            try {
                /*
                  System.out.println("regexp matching for "+
                  (inv?"!":"")+expr+" -> "+
                  wrappingMethodName+" on "+wrappee);
                  System.out.println("Methods = "+source);
                */
                RE re = new RE(Strings.replace(expr,"$","\\$"),0,
                               RESyntax.RE_SYNTAX_EMACS);
                Iterator it = source.iterator();
                while (it.hasNext()) {
                    AbstractMethodItem method = (AbstractMethodItem)it.next();
                    if (re.isMatch(method.getFullName()) ^ inv) {
                        dest.add(method);
                        //System.out.println("         -> ADDED "+method);
                    }
                }
                //System.out.println("Matching methods = "+dest);
            } catch (Exception e) {
                logger.error("filterMethodKeywords"+e);
            }
        }

        //Log.trace("pointcut."+cli.getName(),expr+" MATCH "+result);
    }

    /**
     * Tells wether a method is an adder of one a set of collections
     * @param method the method to test
     * @param collections the collection items. If null, it matches any
     * collection.  
     * @return true if method is an adder of one of the collections
     */
    static boolean isAdder(AbstractMethodItem method, Collection collections) {
        if (!method.isAdder()) {
            return false;
        } else {
            if (collections==null) {
                return true;
            } else {
                CollectionItem[] added = method.getAddedCollections();
                if (added!=null) {
                    for (int i=0; i<added.length; i++) {
                        if (collections.contains(added[i]))
                            return true;
                    }
                }
                return false;
            }
        }
    }

    /**
     * Tells wether a method is a remover of one a set of collections
     * @param method the method to test
     * @param collections the collection items. If null, it matches any
     * collection.  
     * @return true if method is a remover of one of the collections
     */
    static boolean isRemover(AbstractMethodItem method, Collection collections) {
        if (!method.isRemover()) {
            return false;
        } else {
            if (collections==null) {
                return true;
            } else {
                CollectionItem[] removed = method.getRemovedCollections();
                if (removed!=null) {
                    for (int i=0; i<removed.length; i++) {
                        if (collections.contains(removed[i]))
                            return true;
                    }
                }
                return false;
            }
        }
    }


    /**
     * Tells wether a method is a writer of one a set of fields
     * @param method the method to test
     * @param fields the field items. If null, it matches any
     * field  
     * @return true if method is a writer of one of the fields
     */
    static boolean isWriter(AbstractMethodItem method, Collection fields) {
        if (!method.isWriter()) {
            return false;
        } else {
            if (fields==null) {
                return true;
            } else {
                FieldItem[] written = method.getWrittenFields();
                if (written!=null) {
                    for (int i=0; i<written.length; i++) {
                        if (fields.contains(written[i]))
                            return true;
                    }
                }
                return false;
            }
        }
    }   

    /**
     * Tells wether a method is the setter of one of a set of fields
     * @param method the method to test
     * @param fields the field items. If null, it matches any
     * field  
     * @return true if method is the setter of one of the fields
     */
    static boolean isSetter(AbstractMethodItem method, Collection fields) {
        FieldItem setField = method.getSetField();
        return (fields==null && setField!=null) || 
            (fields!=null && fields.contains(setField));
    }   


    /**
     * Tells wether a method is the getter of one of a set of fields
     * @param method the method to test
     * @param fields the field items. If null, it matches any
     * field  
     * @return true if method is the getter of one of the fields
     */
    static boolean isGetter(AbstractMethodItem method, Collection fields) {
        if (method instanceof MethodItem) {
            FieldItem setField = ((MethodItem)method).getReturnedField();
            return (fields==null && setField!=null) || 
                (fields!=null && fields.contains(setField));
        } else {
            return false;
        }
    }   

    /**
     * Tells wether a method is a refaccessor of one a set of references
     * @param method the method to test
     * @param collections the reference items. If null, it matches any
     * reference.  
     * @return true if method is a reference accessor of one of the references
     */
    static boolean isReferenceAccessor(AbstractMethodItem method, Collection references) {
        if (!method.isReferenceAccessor()) {
            return false;
        } else {
            if (references==null) {
                return true;
            } else {
                FieldItem[] refs = method.getAccessedReferences();
                if (refs!=null) {
                    for (int i=0; i<refs.length; i++) {
                        if (references.contains(refs[i]))
                            return true;
                    }
                }
                return false;
            }
        }
    }

    static boolean isCollectionAccessor(AbstractMethodItem method, Collection collections) {
        if (!method.isCollectionAccessor()) {
            return false;
        } else {
            if (collections==null) {
                return true;
            } else {
                CollectionItem[] accessedCollections = method.getAccessedCollections();
                if (accessedCollections!=null) {
                    for (int i=0; i<accessedCollections.length; i++) {
                        if (collections.contains(accessedCollections[i]))
                            return true;
                    }
                }
                return false;
            }
        }
    }

    static boolean isCollectionModifier(AbstractMethodItem method, Collection collections) {
        if (!method.hasModifiedCollections()) {
            return false;
        } else {
            if (collections==null) {
                return true;
            } else {
                CollectionItem[] modifiedCollections = method.getModifiedCollections();
                if (modifiedCollections!=null) {
                    for (int i=0; i<modifiedCollections.length; i++) {
                        if (collections.contains(modifiedCollections[i]))
                            return true;
                    }
                }
                return false;
            }
        }
    }

    Hashtable classCache = new Hashtable();

    /**
     * Tests if the given component class is modified (in a way or
     * another) by this pointcut.
     *
     * @param wrappee the component to test
     * @return true if the class matches */

    public boolean isClassMatching(Wrappee wrappee, ClassItem cl) {
        if (allClasses)
            return true;
        /*
          Log.trace("pointcut.class",2,
          "getting matching class for "+
          cl+"("+NameRepository.get().getName(wrappee)+") for "+
          wrappeeClassExpr+"/"+wrappingClassName+"."+wrappingMethodName);
        */
        String className = null;
        if (cl==null) {
            cl = ClassRepository.get().getClass(wrappee);
        }
        className = cl.getName();

        Boolean match = (Boolean)classCache.get(className);
        if (match==null) {
            Iterator it = wrappeeClassRegexps.iterator();
            Iterator iti = iwrappeeClassExprs.iterator();
            try {
                match = Boolean.TRUE;
                while (it.hasNext()) {
                    RE regexp = (RE)it.next();
                    boolean inv = ((Boolean) iti.next()).booleanValue();
                    /*
                      Log.trace("pointcut.class",2,
                      "isClassMatching: comparing "+className+" with "+
                      regexp+" (inv="+inv+")");
                    */
                    if (cl.isSubClassOf(regexp) ^ inv) {
                        /*
                          Log.trace("pointcut.class","Class "+className+" does not match "+
                          (inv?"":"!")+regexp);
                        */
                        match = Boolean.FALSE;
                        break;
                    }
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
            classCache.put(className,match);
            /*
              if (match.booleanValue())
              Log.trace("pointcut.class","Class "+className+" matches "+
              wrappeeClassExprs+iwrappeeClassExprs);
            */
        } else {
            /*
              Log.trace("pointcut.class",2,"class cache hit for "+
              cl+"("+NameRepository.get().getName(wrappee)+") -> "+match); 
            */
        }
        return match.booleanValue();
    }

    /**
     * Tests if the given component is modified (in a way or
     * another) by this pointcut.
     *
     * <p>Contrary to the class-based matching, this matching works on
     * a per-component basis and not on a per-class basis. This is
     * posible by using the naming aspect of the system (assuming it is
     * there). If the naming aspect appears not to be woven, then all
     * the components should mathes here.
     *
     * @param wrappee the component to test
     * @return true if the name matches 
     */
    public boolean isNameMatching(Wrappee wrappee, ClassItem cl) {
        if (allInstances) return true;
        if (wrappee==null) return true;
        String name = NameRepository.get().getName(wrappee);
        if (name == null) {
            //logger.info("Name "+name+" does not match "+wrappeeExprs);
            return false;
        }
        return isNameMatching(wrappee,name);
    }

    public boolean isNameMatching(Wrappee wrappee,String name) {
        loggerName.debug("isNameMatching "+name+","+wrappee);
        Iterator it = wrappeeRegexps.iterator();
        Iterator it2 = wrappeeExprs.iterator();
        Iterator iti = iwrappeeExprs.iterator();
        try {
            while (it.hasNext()) {
                String s = (String)it2.next();
                if (isPathExpression(s)) {
                    boolean result = isInPath(wrappee,s);
                    /*
                      if (result)
                      logger.info("Name "+name+" matches "+wrappeeExprs);
                      else
                      logger.info("Name "+name+" does not match "+wrappeeExprs);
                    */
                    return result;
                }
                RE regexp = (RE)it.next();
                boolean inv = ((Boolean)iti.next()).booleanValue();
                /*
                  Log.trace("wrap","isNameMatching: comparing "+name+" with "+
                  regexp+" (inv="+inv);
                */
                if (regexp.isMatch(name) ^ inv) {
                    /*
                      logger.info("Name "+name+" does not match "+
                      (inv?"":"!")+s);
                    */
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * Tells if this expression is a path expression (of the form o/r/o...).
     *
     * @param expr the expression to check
     * @return true is a path expression */

    public static boolean isPathExpression(String expr) {
        if (expr.indexOf('/') == -1) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Tells if this object is reachable for the given object path
     * expression.
     *
     * @param candidate the candidate object
     * @param pathExpr the path expression
     * @return boolean true if reachable */

    public static boolean isInPath(Object candidate, String pathExpr) {
        StringTokenizer st = new StringTokenizer(pathExpr,"/");
        String root = st.nextToken();
        Collection accessible = NameRepository.getObjects(root);
        try {
            while (st.hasMoreTokens()) {
                loggerPath.debug("intermediate accessibles are "+accessible);
                String relExpr = st.nextToken();
                accessible = getRelatedObjects(accessible, relExpr);
                String filterExpr = st.nextToken();
                accessible = filterObjects(accessible, filterExpr);
            }
        } catch( Exception e ) {
            loggerPath.error("malformed path expression: "+pathExpr,e);
            return false;
        }
        loggerPath.debug("checking if "+candidate+
                         " matches the path expression "+pathExpr+
                         ", accessible objects are: "+accessible);
        return accessible.contains( candidate );
    }

    /**
     * Gets all the objects that are related to a set of objects through
     * relations that match the given expression.
     *
     * @param initial the initial objects
     * @param relationExpr the expression that matches the relations
     * @return a set of accessible objects */

    public static Collection getRelatedObjects(Collection initial,
                                               String relationExpr) {
        loggerPath.debug("getRelatedObjects "+relationExpr);
        Iterator it = initial.iterator();
        ClassRepository cr = ClassRepository.get();
        Vector res = new Vector();
        while( it.hasNext() ) {
            Object o = it.next();
            ClassItem cli = cr.getClass(o.getClass());
            loggerPath.debug("getting matching relations");
            Collection rels = cli.getMatchingRelations(relationExpr);
            loggerPath.debug("matching relations are "+rels);
            Iterator itRels = rels.iterator();
            while( itRels.hasNext() ) {
                FieldItem fi = (FieldItem) itRels.next();
                if( fi.isReference() ) {
                    Object ref = fi.get(o);
                    if( ref != null ) {
                        loggerPath.debug("adding referenced object "+ref);
                        res.add(ref);
                    }
                } else if (fi instanceof CollectionItem) {
                    Collection cref = ((CollectionItem)fi).getActualCollection(o);
                    if (cref != null) {
                        loggerPath.debug("adding objects in collection");
                        res.addAll(cref);
                    }
                }
                loggerPath.debug("performing next relation");
            }
        }
        return res;
    }

    /**
     * Filters a collection of objects regarding an expression.
     *
     * @param initial the collection to filter
     * @param filter the filtering expression
     * @return a filtered collection that contains only the objects
     * that match the expression */

    public static Collection filterObjects(Collection initial,
                                           String filter) {
        loggerPath.debug("filterObjects "+initial+"/"+filter);
        NameRepository nr = (NameRepository)NameRepository.get();
        Vector res = new Vector();
        try {
            // path expression allows the user to denote objects
            // with their index in the collection
            Integer index = new Integer(filter);
            res.add(((Vector)initial).get(index.intValue()));
            return res;
        } catch (Exception e) {}

        if (initial==null) 
            return res;
        Iterator it = initial.iterator();
        try {
            RE regexp = new RE(filter, 0, RESyntax.RE_SYNTAX_EMACS);
            while(it.hasNext()) {
                Object o = it.next();
                String name = nr.getName(o);
                if (name!=null && regexp.isMatch(name)) {
                    res.add(o);
                }
            }
        } catch( Exception e ) {
            e.printStackTrace();
        }
        return res;
    }

    Hashtable hostCache = new Hashtable();

    /**
     * Tests if the given component is modified (in a way or
     * another) by this pointcut.
     *
     * <p>Contrary to the class-based matching, this matching works on
     * a per-component basis and not on a per-class basis. This is
     * posible by using the naming aspect of the system (assuming it is
     * there). If the naming aspect appears not to be woven, then all
     * the components should mathes here.
     *
     * @param wrappee the component to test
     * @param cl the class, in case of static method
     * @return true if the name matches 
     */
    public boolean isHostMatching(Wrappee wrappee, ClassItem cl) {
        if (allHosts) { return true; }
        String name = "";
        try {
            Class distd = Class.forName("org.objectweb.jac.core.dist.Distd");
            name = (String)distd.getMethod("getLocalContainerName",ExtArrays.emptyClassArray)
                .invoke(null,ExtArrays.emptyObjectArray);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        if (name == null) {
            loggerHost.debug("Host "+name+" does not match "+
                      hostExprs+ihostExprs);
            return false;
        }

        Boolean match = (Boolean)hostCache.get(name);
        if (match==null) {
            Iterator it = hostRegexps.iterator();
            Iterator iti = ihostExprs.iterator();
            match = Boolean.TRUE;
            try {
                while(it.hasNext()) {
                    RE regexp = (RE)it.next();
                    boolean inv = ((Boolean) iti.next()).booleanValue();
                    loggerHost.debug("isHostMatching: comparing "+name+" with "+
                              regexp+" (inv="+inv);
                    if (regexp.isMatch(name) ^ inv) {
                        loggerHost.debug("Host "+name+" does not match "+
                                         (inv?"":"!")+regexp);
                        match = Boolean.FALSE;
                        break;
                    }
                }
            } catch( Exception e ) {
                e.printStackTrace();
            }
            hostCache.put(name,match);
        }
        loggerHost.debug("Host \""+name+"\" is "+(match.booleanValue()?"":" not ")+
                         "matching "+hostExprs+ihostExprs);
        return match.booleanValue();
    }

    /**
     * Tests if the given method item is modified (in a way or
     * another) by this pointcut.
     *
     * @param method the method to test
     * @return true if the method matches 
     */
    public boolean isMethodMatching(AbstractMethodItem method) {
        Iterator it = wrappeeMethodRegexps.iterator();
        Iterator iti = iwrappeeMethodExprs.iterator();
        String name = method.getFullName();

        while (it.hasNext()) {
            RE regexp = (RE)it.next(); 
            boolean inv = ((Boolean)iti.next()).booleanValue();
            if (regexp.isMatch(name) ^ inv) {
                return false;
            }
        }
        return true;
    }

    FieldItem parameterToField(ClassItem cli, Object parameter) {
        if (parameter instanceof FieldItem)
            return (FieldItem)parameter;
        else if (parameter instanceof String) {
            return cli.getField((String)parameter);
        }
        else {
            logger.warn("Unknown parameter type "+
                        parameter.getClass().getName());
            return null;
        }
    }

    boolean isNoneParameter(Object parameter) {
        if (parameter instanceof String && 
            "#NONE#".equals((String)parameter)) {
            return true;
        }
        return false;
    }

    protected String parseKeyword(Wrappee wrappee, ClassItem cli, 
                                  String keywordExpr, 
                                  List parameters) {

        loggerKeywords.debug("parseKeyword("+wrappee+","+cli+","+
                  keywordExpr+","+parameters+")");
        String result = "";

        // replace attributes (<>) with the actual member values
        parameters = replaceTags(parameters,cli);

        if (keywordExpr.equals("ALL")) {
            loggerKeywords.debug("found ALL keyword");
            result = ".*";

        } else if (keywordExpr.equals("COLLECTIONS")) {
            loggerKeywords.debug("found COLLECTIONS keyword");
            result = "org.objectweb.jac.lib.java.util.*";

        } 

        loggerKeywords.debug("parsed keyword "+keywordExpr+" => "+result);

        return result;

    }

    static String quoteString(String string) {
        return Strings.replace(string,"[]","\\[\\]");
    }

}
