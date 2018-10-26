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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.core.rtti.CollectionItem;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.RttiAC;
import org.objectweb.jac.util.ExtArrays;
import org.objectweb.jac.util.Predicate;
import org.objectweb.jac.util.Strings;

/**
 * This class allows the JAC applications to access objects that are
 * managed within the current VM or that can be defined outside of it
 * by the currently woven aspects.
 *
 * <p>By default, the object repository methods only return the JAC
 * objects that are accessible through the
 * <code>JacObject.getObject</code> methods. However, since it upcalls
 * the <code>ACManager.whenGetObjects()</code> method, all the aspect
 * components can change the returned object set.
 *
 * <p>As typical examples:
 *
 * <ul><li>the persistence aspects may load objects that correspond to
 * the get filter so that the application can see the objects stored
 * in the database
 *
 * <li>the owning aspect can remove some objects from the returned
 * collection if the current user is not allowed to access them
 *
 * <li>the distribution aspect can initiate a distributed seek over
 * all the containers of the topology to add some objects that
 * correspond to the filter but that may have not been declared yet in
 * this VM</ul>
 *
 * @see ACManager#whenGetObjects(Collection,ClassItem)
 * @see AspectComponent#whenGetObjects(Collection,ClassItem)
 */

public class ObjectRepository {
    static Logger logger = Logger.getLogger("repository");
    static Logger loggerGet = Logger.getLogger("repository.getobjects");

    /** Memorize the JAC object instances of the system. */
    transient static org.objectweb.jac.util.WeakHashMap instances = new org.objectweb.jac.util.WeakHashMap();

    /** Fast access for the JAC objects. Integer -> Object */
    transient static java.util.WeakHashMap reverseInstances = new java.util.WeakHashMap();

    /** The number of created JAC objects. */
    transient static int instancecount = 0;

    public static void register(Object wrappee) {
        // This test is not useful anymore since register should be
        // called only once
        //if(instances.contains(wrappee)) return;
        // do not register collections
        if (wrappee instanceof org.objectweb.jac.lib.java.util.Vector || 
            wrappee instanceof org.objectweb.jac.lib.java.util.HashSet || 
            wrappee instanceof org.objectweb.jac.lib.java.util.Hashtable ||
            wrappee instanceof org.objectweb.jac.lib.java.util.HashMap) {
            return;
        }
        //System.out.println("registering "+wrappee);
        Integer inst = new Integer(instancecount++);
        instances.put(inst,wrappee);
        logger.debug(wrappee+"->"+inst);
        reverseInstances.put(wrappee,inst);
    }

    /**
     * This method deletes the given JAC object by removing it from
     * the different collections it has been inserted into by the
     * system.
     *
     * <p>The JAVA GC, will then be able to free it from memory on its
     * next run. 
     */
    public static void delete(Wrappee object) {
        logger.debug("delete "+object);
        // remove from system list
        instances.remove(reverseInstances.get(object));
        reverseInstances.remove(object);
        // remove from name repository
        NameRepository.get().unregisterObject(object);
        ACManager.getACM().whenDeleted(object);
    }

    public static void free(Wrappee object) {
        // remove from system list
        instances.remove(object);
        reverseInstances.remove(object);
        // remove from name repository
        NameRepository.get().unregisterObject(object);
        ACManager.getACM().whenFree(object);
    }

    /**
     * This method returns the nth object that has been created
     * in the Jac system.
     *
     * @param nth  the object index
     * @return     the requested object
     */
    public static Object getMemoryObject(int nth) {
        return instances.get(new Integer(nth));
    }


    /**
     * This method returns the index of anobject that has been created
     * in the Jac system.
     *
     * @param obj  the object
     * @return     the index of the object
     */
    public static int getMemoryObjectIndex(Object obj) {
        return ((Integer)reverseInstances.get(obj)).intValue();
    }


    /**
     * This method returns the object counter of the Jac system (last
     * created object is <code>getObject(objectCount()-1)</code>).
     *
     * @return  the number of created objects
     */
    public static int memoryObjectCount() {
        return instances.size(); //instancecount;
    }
    
    
    /**
     * This method returns all the JAC objects that are in the current
     * JVM memory (use <code>getObjects()</code> to get all the objects
     * handled by the woven aspects -- such as distribution or
     * persistence).
     *
     * @return all the JAC objects in memory 
     *
     * @see #getObjects()
     * @see #getObjects(ClassItem)
     * @see #getMemoryObjects(ClassItem)
     */
    public static Collection getMemoryObjects() {
        return instances.values();
    }


    /**
     * This method returns all the Jac objects of a given type as an
     * array.
     *
     * @param type the type to get
     * @return all the Jac objects of a given type 
     *
     * @see #getObjects()
     * @see #getObjects(ClassItem)
     * @see #getMemoryObjects(ClassItem)
     */
    public static Object[] getMemoryObjects(String type) {
        ClassItem cl = null;
        try {
            cl = ClassRepository.get().getClass(type);
        } catch( Exception e ) { 
            e.printStackTrace(); 
            return ExtArrays.emptyObjectArray;
        }
        return getMemoryObjects(cl);
    }


    /**
     * This method returns all the JAC objects of a given type as an
     * array.
     *
     * @param cl the type to get
     * @return all the JAC objects of a given type 
     *
     * @see #getObjects()
     * @see #getObjects(ClassItem)
     * @see #getMemoryObjects()
     */
    public static Object[] getMemoryObjects(ClassItem cl) {
        Vector objects = new Vector();

        Class type = cl.getActualClass();
        Iterator it = getMemoryObjects().iterator();
        while(it.hasNext()) {
            Object cur = it.next();
            if (type.isAssignableFrom(cur.getClass())) {
                objects.add(cur);
            }
        }

        logger.debug("getMemoryObjects("+cl+") -> "+objects);
        return objects.toArray();
    }

    /**
     * This method returns all the JAC objects that match any of the
     * given types.
     *
     * @param types the types to get
     * @return all the JAC objects that match any of the given types 
     */   
    public static Object[] getMemoryObjects(String[] types) {
        Vector objects = new Vector();
        for (int i=0; i<types.length; i++) {
            try {
                objects.addAll( 
                    Arrays.asList(
                        getMemoryObjects(ClassRepository.get().getClass(types[i]))) );
            } catch( Exception e ) { 
                e.printStackTrace(); 
            }
        }       
        return objects.toArray();
    }

    /**
     * This method returns all the Jac objects that match any of the
     * given types.
     *
     * @param types the types to get
     * @return all the Jac objects that match any of the given types 
     */   
    public static Object[] getMemoryObjects(ClassItem[] types) {
        Vector objects = new Vector();
        for (int i=0; i<types.length; i++) {
            objects.addAll( Arrays.asList(getMemoryObjects(types[i])) );
        }       
        return objects.toArray();
    }

    /**
     * Gets all the instantiated JAC objects on the current VM and on
     * all the external objects sources known by the aspects (maybe
     * resticed by some aspects).
     *
     * @return a collection of acessible objects
     *
     * @see #getObjects(ClassItem) 
     * @see #getMemoryObjects() 
     */
    public static Collection getObjects() {
        HashSet objects = new HashSet(instances.values());
        ((ACManager)ACManager.get()).whenGetObjects(objects,null);
        return objects;
    }

    /**
     * Gets all the JAC objects instances of a given class on the
     * current VM and on all the external objects sources known by the
     * aspects (maybe resticed by some aspects).
     *
     * @return a collection of acessible instances of <code>cl</code>
     *
     * @see #getObjects(Class) 
     */
    public static Collection getObjects(ClassItem cl) {
        loggerGet.debug("getObjects "+cl);
        String repName = (String)cl.getAttribute(RttiAC.REPOSITORY_NAME);
        CollectionItem repCollection  = 
            (CollectionItem)cl.getAttribute(RttiAC.REPOSITORY_COLLECTION);
        Object repository = null;
        if (repName!=null) {
            repository = NameRepository.get().getObject(repName);
            if (repository==null)
                loggerGet.error(cl+": no such repository object "+repName);
        }
        if (repository!=null  && repCollection!=null) {
            loggerGet.debug("Using repository "+repName+"."+repCollection.getName());
            return FieldItem.getPathLeaves(repCollection,repository);
        } else {
            List objects = new Vector(Arrays.asList(getMemoryObjects(cl)));
            ((ACManager)ACManager.get()).whenGetObjects(objects,cl);
            return objects;
        }
    }

    /**
     * Gets all the JAC objects instances of a given class on the
     * current VM and on all the external objects sources known by the
     * aspects (maybe resticed by some aspects).
     *
     * @return a collection of acessible instances of <code>cl</code>
     * @see #getObjects(ClassItem) */
    public static Collection getObjects(Class cl) {
        return getObjects(ClassRepository.get().getClass(cl));
    }

    /**
     * Get all instances of a class whose field relation contains the
     * given value. If a repository has been defined for the class,
     * only objects belonging to the repository are returned.
     *
     * @param cl the class
     * @param relation the relation
     * @param value the value that the relation must contain 
     *
     * @see org.objectweb.jac.core.rtti.RttiConf#defineRepository(ClassItem,String,CollectionItem)
     */
    public static Collection getObjectsWhere(ClassItem cl, 
                                             FieldItem relation, Object value) {
        loggerGet.debug("getObjectsWhere "+cl+","+relation+","+value);
        Collection objects = getObjects(cl);
        Vector result = new Vector();
        FieldItem field = relation.getField();
        Iterator it = objects.iterator();
        while (it.hasNext()) {
            Object object = it.next();
            for (Iterator j=relation.getSubstances(object).iterator();
                 j.hasNext();) {
                Object substance = j.next();
                if (field instanceof CollectionItem) {
                    if (((CollectionItem)field).
                        getActualCollectionThroughAccessor(substance).contains(value)) {
                        result.add(object);
                        break;
                    }
                } else if (field.isReference()) {
                    if (field.getThroughAccessor(substance)==value) {
                        result.add(object);
                        break;
                    }
                } else {
                    Object testedValue = field.getThroughAccessor(substance);
                    if ((value==null && testedValue==null) || (value!=null && value.equals(testedValue))) {
                        result.add(object);
                        break;
                    }
                }
            }
        }
        return result;
    }


    /**
     * Get all instances of class cl which match a predicate
     * @param cl the class
     * @param filter the predicate to be matched
     * @return a collection whose all items are such that filter(item)==true
     */
    public static Collection getObjectsWhere(ClassItem cl, Predicate filter) {
        loggerGet.debug("getObjectsWhere "+cl+","+filter.getClass().getName());
        Collection objects = getObjects(cl);
        Vector result = new Vector();
        Iterator it = objects.iterator();
        while (it.hasNext()) {
            Object object = it.next();
            if (filter.apply(object)) {
                result.add(object);
            }
        }
        return result;
    }

    public void dump() {
    }      

}
