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

  You should have received a copy of the GNU Lesser General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA */

package org.objectweb.jac.util;

import java.util.*;
import org.apache.log4j.Logger;

/**
 * @author <a href="http://cedric.cnam.fr/~pawlak/index-english.html">Renaud Pawlak</a>
 */

/**
 * This class can be subclassed to create specific repositories.
 * 
 * <p>A repository class should be a singleton (a sole instance
 * class). Thus, the repository subclasses should define a static
 * field and a static method 'get' that returns the unique repository
 * for the class and that creates it if it does not exist. 
 */
public class Repository {
    static Logger logger = Logger.getLogger("repository");

    /**
     * Get the sole repository instance for this class. Creates it if
     * it does not exist yet.
     *
     * <p>NOTE: this method MUST be defined by all subclasses.
     */
    public static Repository get () {
        if (repository == null) 
            repository = new Repository();
        return repository;
    }
   
    /**
     * Store the sole instance of repository.
     *
     * <p>NOTE: this field MUST be defined by all subclasses.
     * 
     * @see #get()
     */
    protected static Repository repository = null;

    /**
     * Link JAC objects to the names  (String -> Object) */
    public Map objects;

    /**
     * Reverse hashtable to find an objet from its name  (Object -> String)*/
    public Map names;

    public Repository() {
        init();
    }

    protected void init() {
        objects = new Hashtable();
        names = new Hashtable();
    }

    /**
     * Register a new object into the repository.
     *
     * @param logicalName the key that allows to find the object
     * @param object the object to register
     * @return true if the object registered, false if already
     * registered
     *
     * @see #unregister(String) 
     */
    public boolean register(String logicalName, Object object) {
        logger.debug("register("+logicalName+","+object.getClass().getName()+")");
        Object old = objects.put(logicalName, object);
        if (old!=null) {
            logger.warn("overriding "+logicalName+" -> "+old+" with "+object);
            names.remove(old);
        }
        names.put(object, logicalName);
        return true;
    }

    /**
     * Unregister a JacObject from the repository.
     *
     * @param logicalName the key that allows to find the object
     *
     * @see #register(String,Object)
     * @see #unregisterObject(Object) 
     */
    public void unregister(String logicalName) {
        logger.debug("unregister("+logicalName+")");
        Object object = objects.remove(logicalName);
        if (object != null) {
            names.remove(object);
        }
    }

    /**
     * Unregister a JacObject from the repository.
     *
     * @param object the object to unregister
     *
     * @see #register(String,Object)
     * @see #unregister(String) 
     */
    public void unregisterObject(Object object) {
        logger.debug("unregisterObject("+object+")");
        Object logicalName = names.remove(object);
        if (logicalName != null) {
            objects.remove(logicalName);
        }
    }

    /**
     * Returns true if an object is registered with this name.
     *
     * @param logicalName the key that allows to find the object
     *
     * @see #register(String,Object) */

    public boolean isRegistered(String logicalName) {
        if (objects.containsKey(logicalName)) {
            return true;
        }
        return false;
    }

    /**
     * Return all the registered objects as an array.
     *
     * <p>Reverse operation is <code>getNames()</code>.
     *
     * @return the registered objects in this repository
     *
     * @see #register(String,Object)
     * @see #getNames() 
     */
    public Object[] getObjects() {
        return objects.values().toArray();
    }

    /**
     * Return the names of the registered objects as an array.    
     *
     * <p>Reverse operation is <code>getObjects()</code>.
     *
     * @return the registered object names in this repository
     *
     * @see #register(String,Object)
     * @see #getObjects() 
     */
    public String[] getNames() {
        return (String[])names.values().toArray(ExtArrays.emptyStringArray);
    }

    /**
     * Return a registered object for a given logical name.
     * 
     * <p>Return <code>null</code> if the name does not correspond to
     * any registered object or if <code>logicalName</code> is null.
     *
     * <p>Reverse operation is <code>getName(Object)</code>.
     *
     * @param logicalName the key that allows to find the object
     * @return the corresponding object, null if not registered
     *
     * @see #register(String,Object)
     * @see #getName(Object) 
     */
    public Object getObject(String logicalName) {
        if (logicalName == null) 
            return null;
        Object ret = objects.get(logicalName);
        logger.debug("getObject("+logicalName+") -> "+
                     (ret==null?"null":ret.getClass().getName()));
        return ret;
    }

    /**
     * Returns the name of a registered object. Null if not
     * registered.
     *
     * <p>Reverse operation is <code>getObject(String)</code>.
     *
     * @param object the object to get the name of
     * @return the object name, null if not registered
     *
     * @see #register(String,Object) 
     * @see #getObject(String)
     */
    public String getName(Object object) {
        if (object == null) {
            return null;
        }
        return (String)names.get(object);
    }

    /**
     * Dump all the registered objects. 
     */
    public void dump() {            
        System.out.println(this + " dump:");
        System.out.println(getPrintableString());
    }

    public String getPrintableString() {
        return ""+objects;
    }

}
