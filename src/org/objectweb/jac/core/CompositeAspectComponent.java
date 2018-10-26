/*
  Copyright (C) 2003 Renaud Pawlak <renaud@aopsys.com>

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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.ACManager;

/**
 * This class is the root class for the composite aspect components
 * defined by JAC aspect programmers.
 *
 * <p>A composite aspect is a kind of facade that factorizes and
 * simplifies the use of a set of sub-aspects (called chidren
 * aspects). For instance, the user and authentication aspects of JAC
 * can be factorized in on unique aspect in order to simplify the use
 * (factorization) and to eliminate useless dependencies.
 *
 * @author <a href="mailto:renaud@aopsys.com">Renaud Pawlak</a> */

public class CompositeAspectComponent extends AspectComponent {
    static Logger logger = Logger.getLogger("aspects.composite");

    Map childClasses=new HashMap();
    Map children=new HashMap();
    Map defaultConfigs=new HashMap();

    /**
     * The default constructor.
     *
     * <p>Should be redefined be the programmer in order to declare the child aspects.
     *
     * @see #addChild(String,Class) */
    public CompositeAspectComponent () {   
        super();
    }

    /**
     * Invoke this method in the constructor in order to disable the
     * application of the default configuration for a given child
     * aspect component.
     * 
     * <p>Note that the default configs are defined by the
     * <code>getDefaultConfig</code> method. By default, all the
     * children's default configs are applied. */
    protected void disableDefaultConfigs(String childName) {
        defaultConfigs.put(childName,Boolean.FALSE);
    }

    /**
     * Declares a new child for this composite (should be invoked from
     * the constructor).
     *
     * @param name the child's name (can be any unique id)
     * @param acClass the aspect component's class of the child */
    protected void addChild(String name,Class acClass) {
        logger.debug("addChild("+name+
                  ","+acClass+")"+" to composite "+getName());
        childClasses.put(name,acClass);
    }

    /**
     * Removes a child for this composite (do not use unless you know
     * what you are doing).
     *
     * @param name the child's name as given in the {@link
     * #addChild(String,Class)} method */
    protected void removeChild(String name) { 
        logger.debug("removeChild("+name+
                  ") from composite "+getName()+")");
        childClasses.remove(name); }

    /**
     * Returns a child aspect component from its name.
     *
     * <p>This method should be used in configuration methods of the
     * composite to delegate the configuration to the child
     * aspects. */
    protected AspectComponent getChild(String name) {
        logger.debug("getChild("+name+
                  ") in composite "+getName());
        return (AspectComponent)children.get(name);
    }

    /**
     * Returns the internal name of a child (applicationName.name).
     *
     * @param name the child's name */ 
    protected String getChildActualName(String name) {
        logger.debug("getChildActualName("+name+
                  ") in composite "+getName());
        return getChild(name).getApplication()+"."+name;
    }

    /**
     * Returns the internal name of a child, as it is registered in
     * the JAC ACManager.
     *
     * <p>Note that this method will return null if the child is not
     * yet registered in the AC manager. This is the case at
     * configuration time. Thus, {@link #getChildActualName(String)}
     * should be prefered.
     *
     * @param name the child's name */ 
    protected String getChildRegisteredName(String name) {
        logger.debug("getChildActualName("+name+
                  ") in composite "+getName());
        return ACManager.getACM().getName(getChild(name));
    }

    /**
     * This method is upcalled by the system when the composite aspect
     * is about to be configured.
     *
     * <p>This method instantiates all the declared children (with
     * {@link #addChild(String,Class)} method. */
    public void beforeConfiguration() throws Exception {
        logger.debug("beforeConfiguration()"+
                  " for composite "+getName());
        Iterator it=childClasses.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry entry=(Map.Entry)it.next();
            Class acClass=(Class)entry.getValue();
            AspectComponent instance = (AspectComponent) acClass.newInstance();
            instance.setApplication(getApplication());
            children.put(entry.getKey(),instance);
            // apply the default configurations if needed
            String[] defaults = instance.getDefaultConfigs();
            for (int i=0; i<defaults.length; i++) {
                instance.configure((String)entry.getKey(),defaults[i]);
            }
        }

    }

    /**
     * This method is upcalled by the system when the composite aspect
     * is actually registered in the AC manager.
     *
     * <p>It symply registers all the children with the right
     * names. */
    public final void doRegister() {
        logger.debug("registerChildren()"+
                     " for composite "+getName());
        Iterator it=children.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry entry=(Map.Entry)it.next();
            AspectComponent ac=(AspectComponent)entry.getValue();
            ACManager.get().register( ac.getApplication() + 
                                      "." + entry.getKey(), ac );
        }	
        logger.debug("end registerChildren() => ACM dump: "+
                     ACManager.get().getPrintableString());

    }


    /**
     * This method is upcalled by the system when the composite aspect
     * is actually unregistered in the AC manager.
     *
     * <p>It symply unregisters all the children. */
    public final void doUnregister() {
        logger.debug("unregisterChildren()"+
                     " for composite "+getName());
        Iterator it=children.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry entry=(Map.Entry)it.next();
            AspectComponent ac=(AspectComponent)entry.getValue();
            ACManager.get().unregister( ac.getApplication() + 
                                        "." + entry.getKey() );
        }	
    }

}
