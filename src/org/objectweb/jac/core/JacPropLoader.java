/*
  Copyright (C) 2001 Renaud Pawlak, Laurent Martelli, Lionel
  Seinturier, Fabrice Legond-Aubry.

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

import gnu.regexp.RE;
import gnu.regexp.REException;
import java.util.*;
import org.apache.log4j.Logger;
import org.objectweb.jac.util.Strings;

/**
 * This class is used to load all the properties necessary to the
 * execution of JAC. This class is called by the <code>Jac</code>,
 * <code>JacLoader</code> and <code>JacObject</code> and
 * <code>CompositionAspect</code> classes.
 *
 * @see JacLoader
 * @see Jac
 * @author Renaud Pawlak
 * @author Lionel Seinturier
 * @author Laurent Martelli */

public abstract class JacPropLoader extends JacPropTools {
    static Logger logger = Logger.getLogger("props");

    /**************************************************************
     * Static members.
     **************************************************************/

    /** The JAC property file name. */
    public final static String propFileName = "jac.prop";
    
    /** The name of the property in the the jac.prop file. */
    public final static String toNotAdaptProp = "jac.toNotAdapt";
    
    /** The name of the property in the the jac.prop file. */
    public final static String toAdaptProp = "jac.toAdapt";

    /** The name of the property in the the jac.prop file. */
    public final static String wrappableMethodsProp = "jac.wrappableMethods";

    /** property name for classes whose fields must not be translated */
    public final static String dontTranslateFieldsProp = "jac.dontTranslateField";
    
    /** The name of the property in the the jac.prop file. */
    public final static String toWrapProp = "jac.toWrap";
	
	/** The name of the property that defines the initial global
		topology in the jac.prop file. */
    public final static String topologyProp = "jac.topology";
       
    /**
     * The jac.prop property string to declare initially woven aspect
     * components. */
    public final static String acsProp = "jac.acs";
    
    /**
     * The jac.prop property string to declare when the initially woven
     * aspect components are woven. */
    public final static String startWeavingPlacesProp = "jac.startWeavingPlaces";
    
    /**
     * The jac.prop property string to set the composition aspect class
     * name. */
    public final static String compositionAspectProp = "jac.comp.compositionAspect";
    

    /** The name of the wrapping order property in the prop file. */
    public final static String wrappingOrderProp = "jac.comp.wrappingOrder";

    /** The name of the incompatible property in the prop file. */
    public final static String incompatibleACsProp = "jac.comp.imcompatibleACs";

    /** The name of the dependent property in the prop file. */
    public final static String dependentACsProp = "jac.comp.dependentACs";

    /** The name of the property in the the jac.prop file. */
    public final static String bytecodeModifierProp = "jac.bytecodeModifier";

    /** Property key for the remote reference class. */
    public static final String remRefClassProp = "jac.remoteRefClass";
	
    /** Default remote reference class. */
    public static final String remRefDefaultClassName = "org.objectweb.jac.core.dist.rmi.RMIRemoteRef";

    /** Property key for the class providing a naming service. */
    public static final String namingClassProp = "org.objectweb.jac.core.dist.namingClass";

    /** Default class providing a naming service. */
    public static final String namingClassDefaultName = "org.objectweb.jac.core.dist.rmi.RMINaming";

    /** The properties loaded from the jac.prop file. */
    public static Properties props = new Properties();
   
    /** Store the packages (set of classes) translated to be
        wrappable. */
    public static HashSet packagesToAdapt = new HashSet();
   
    /** Store the packages (set of classes) translated to be not
        wrappable. */
    public static HashSet packagesToNotAdapt = new HashSet();

    /** Store the methods that are wrappable (per class) */
    public static Hashtable wrappableMethods = new Hashtable();

    /** Store the classes whose fields must not be translated */
    public static HashSet dontTranslateFields = new HashSet();
   
    public static HashSet packagesToWrap = new HashSet();
   
    public static String compositionAspect = null;
   
    /** The name of the bytecode modifier package */
    public static String bytecodeModifier = null;
   
    public static String remoteRefClassName = null;
   
    public static String namingClassName = null;
   
    /** Stores all the declared aspect components. */
    public static Hashtable declaredACs = new Hashtable();
   
    /** Store the default wrapping order. */
    public static  Vector wrappingOrder = new Vector();
   
    /** Store the exclusive aspect component pairs. */
    public static  Vector incompatibleACs = new Vector();
   
    /** Store the dependent aspect component pairs. */
    public static Vector dependentACs = new Vector();
   
    public static int acs_count=0;

    static HashSet packagesToAdaptRE = new HashSet();

    static HashSet packagesToNotAdaptRE = new HashSet();

    /**
     * Add some properties.
     * @param ps the properties to add
     * @return true is ps!=null, false otherwise
     */
    public static boolean addProps(Properties ps) {
        String tmp;
        if (ps==null)
            return false;
        for (Enumeration e = ps.propertyNames() ; e.hasMoreElements() ;) {
            String key=(String)e.nextElement();
            String value=ps.getProperty(key);
            if (value!=null)
                props.setProperty(key,value);
        }

        // Get list of packages that must not be adpated
        fillSetProps(packagesToNotAdapt, ps, toNotAdaptProp, false);
        Iterator i = packagesToNotAdapt.iterator();
        packagesToNotAdaptRE.clear();
        while (i.hasNext()) {
            String pkg = (String)i.next();
            try {
                packagesToNotAdaptRE.add(new RE("^"+Strings.replace(Strings.replace(Strings.replace(pkg,"$","\\$"),".","\\."),"*",".*")+"$"));
            } catch(REException e) {
                logger.warn("RE exception: "+e);
            }
        }
      
        // Get list of packages that must be adpated
        fillSetProps(packagesToAdapt, ps, toAdaptProp, false);
        i = packagesToAdapt.iterator();
        packagesToAdaptRE.clear();
        while (i.hasNext()) {
            String pkg = (String)i.next();
            try {
                packagesToAdaptRE.add(new RE("^"+Strings.replace(Strings.replace(Strings.replace(pkg,"$","\\$"),".","\\."),"*",".*")+"$"));
            } catch(REException e) {
                logger.warn("RE exception: "+e);
            }
        }
      
        // Get list of packages that must be wrapped
        fillSetProps(packagesToWrap, ps, toWrapProp, true);
      
        // Get list of methods that must be wrappable
        fillMapProps(wrappableMethods, ps, wrappableMethodsProp, 0, false);

        // Get classes whose collection fields must not be translated
        fillSetProps(dontTranslateFields,ps,dontTranslateFieldsProp,false);

        // Get list of aspects that must be weaved
        fillMapProps(declaredACs, ps, acsProp, 1, false);
      
        // Get the order of aspects wrapping 
        fillListStringProps(wrappingOrder, ps, wrappingOrderProp, false);
      
        fillListStringProps(dependentACs, ps, dependentACsProp, false);
      
        fillListStringProps(incompatibleACs, ps, incompatibleACsProp, false);
      
        tmp=fillStringProp(ps, compositionAspectProp);
        if (tmp!=null) compositionAspect = tmp;
      
      
        //Get the bytecode modifier name
        tmp= fillStringProp (ps, bytecodeModifierProp);
        if (tmp!=null) bytecodeModifier = tmp;
      
      
        //Get the 
        tmp= fillStringProp (ps, remRefClassProp);
        if (tmp!=null) bytecodeModifier = tmp;
      
        //Get the 
        tmp= fillStringProp (ps, namingClassProp);
        if (tmp!=null) bytecodeModifier = tmp;
      
        return true;
    }

    public static void loadProps(boolean d) {
        loadProps();
    }

    /**
     * Try to load the properties and set all the internal hash tables
     * in order to be used by the JAC core objects. No parameters. No
     * returns.
     */
    public static void loadProps() {
        boolean propsInMemory = false;
        //props = new Properties();
      
        // FOR TEST PURPOSE ONLY
        /*
          String myr=System.getProperty ("JAC_ROOT","vide");
          Properties p=System.getProperties();
          String elt;
          for (Enumeration e = p.propertyNames() ; e.hasMoreElements() ;) {
          elt=(String)e.nextElement();
          System.out.println ("key '"+ elt + "' has value "+p.getProperty(elt));
	
          }
        */
      
        // Try to read a jac.prop in JAC_ROOT
        //System.getProperty("user.dir");
        //File f = new File(".");
        //loadDirectory = f.getAbsolutePath();
        if (!Strings.isEmpty(Jac.getJacRoot()))
            propsInMemory |= addProps(getPropsFrom(Jac.getJacRoot(), propFileName));
      
        String directory;
        directory = System.getProperty("JAC_ROOT",null);
        if (directory != null)
            propsInMemory |= addProps(getPropsFrom(directory, propFileName));
      
      
        // Try to read a jac.prop in $HOME
        directory = System.getProperty("user.home",null);
        if  (directory!=null)
            propsInMemory|=addProps(getPropsFrom(directory, propFileName));
      
        // Try to read a jac.prop file from the current directory	
        propsInMemory |= addProps(getPropsFrom("./", propFileName));
      
        //Stop everything if not, at least, one jac property file is found 
        //Call System.exit();
        if (!propsInMemory)
        {
            logger.warn("Can not find the '"+propFileName+"' file.");
        }
      
        acs_count=declaredACs.size();
        if (bytecodeModifier == null)
            bytecodeModifier = "BCEL";
      
        if (remoteRefClassName == null)
            remoteRefClassName = remRefDefaultClassName;
      
        if (namingClassName == null)
            namingClassName = namingClassDefaultName;
    }

    /**
     * Returns true if the fields of the specified class must be translated
     */
    public static boolean translateFields(String className) {
        Iterator i = dontTranslateFields.iterator();
        while (i.hasNext()) {
            try {
                RE dontTranslate = new RE((String)i.next());
                logger.debug("Testing "+dontTranslate);
                if (dontTranslate.isMatch(className)) {
                    logger.debug("Do not translate fields for "+className);
                    return false;
                }
            } catch (REException e) {
                logger.warn("RE exception: "+e);
            }
        }
        logger.debug("Translate fields for "+className);
        return true;
    }

    /**
     * Returns true if the specified class matches the toAdapt property.
     */
    public static boolean adaptClass(String className) {
        logger.debug("Adapt class "+className+" ???");
        Iterator i = packagesToAdaptRE.iterator();
        while (i.hasNext()) {
            RE regexp = (RE)i.next();
            logger.debug("Testing "+regexp);
            if (regexp.isMatch(className)) {
                logger.debug("Adapt "+className);
                return true;
            }
        }
        logger.debug("Do not force adaptation of "+className);
        return false;
    }

    /**
     * Returns true if the specified class matches the toNotAdapt property.
     */
    public static boolean doNotAdaptClass(String className) {
        logger.debug("Do not adapt class "+className+" ???");
        Iterator i = packagesToNotAdaptRE.iterator();
        while (i.hasNext()) {
            RE regexp = (RE)i.next();
            logger.debug("Testing "+regexp);
            if (regexp.isMatch(className)) {
                logger.debug("Do not adapt "+className);
                return true;
            }
        }
        logger.debug("Adaptation not disabled for "+className);
        return false;
    }
}
