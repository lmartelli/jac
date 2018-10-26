/*
  Copyright (C) 2001-2004 Laurent Martelli <laurent@aopsys.com>

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

package org.objectweb.jac.aspects.persistence;

import java.lang.IndexOutOfBoundsException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.CollectionItem;
import org.objectweb.jac.core.rtti.FieldItem;

/**
 * The Storage interface defines methods to store and retrieve objects
 * fields to and from a media (it could be a database, simples files
 * ...)  
 */

public interface Storage {
    
    /**
     * Returns the id of the storage
     */
    String getId();

    /**
     * Sets the id of the storage
     */
    void setId(String id);

    /**
     * Create an object in the storage.
     *
     * @param className a <code>String</code> value
     * @return the OID of new object
     * @exception Exception if an error occurs
     */
    OID createObject(String className) throws Exception;

    /**
     * Delete an object from the storage.
     *
     * @param oid the ID of the object to delete
     * @exception Exception if an error occurs
     */
    void deleteObject(OID oid) throws Exception;

    /**
     * Initialize the value of an object's field. The field must not
     * have already been set.
     *
     * @param oid the ID of the object that will contain the field
     * @param field the field to set
     * @param value the value of the field
     * @exception Exception if an error occurs
     */
    void setField(OID oid, FieldItem field, Object value) 
        throws Exception;

    /**
     * Update a field value.
     *
     * @param oid the ID of the object that already contains the field
     * @param field the field to update
     * @param value the new value of the field
     * @exception Exception if an error occurs
     */
    void updateField(OID oid, FieldItem field, Object value) 
        throws Exception;

    /**
     * Get the value of a field.
     *
     * @param oid the ID of the object that contains the field
     * @param field the field to retrieve
     * @return the stored value of the field, or null if there's no
     * stored value for that field
     * @exception Exception if an error occurs */
    Object getField(OID oid, FieldItem field)
        throws Exception;
   
    /**
     * Get the values of several fields.
     *
     * @param oid the ID of object to retrieve
     * @param cl the class of the object
     * @param fields the fields to retrieve
     * @return the value of the fields
     * @exception Exception if an error occurs
     */
    StorageField[] getFields(OID oid, ClassItem cl, FieldItem[] fields) 
        throws Exception;

    // Collection functions

    /**
     * Get the ID of a collection
     *
     * @param oid the oid of he object that owns the collection
     * @param collection the collection
     * @return the id of the collection
     * @exception Exception if an error occurs
     */

    OID getCollectionID(OID oid, CollectionItem collection) 
        throws Exception;

    // List functions

    /**
     * Get a List from the storage.
     *
     * @param oid the ID of the object that contains the vector
     * @param collection the collection to retrieve
     * @return the List
     * @exception Exception if an error occurs
     */
    List getList(OID oid, CollectionItem collection)
        throws Exception, IndexOutOfBoundsException;

    /**
     * Get a List from the storage. 
     *
     * @param cid the ID of the List 
     * @return the List
     * @exception Exception if an error occurs
     */
    List getList(OID cid)
        throws Exception;

    /**
     * Get an element from a list
     *
     * @param cid the ID of the List
     * @param index the index of the element
     * @return the element
     * @exception Exception if an error occurs
     */
    Object getListItem(OID cid, long index)
        throws Exception;
   
    /**
     * Get the number of objects contained in a list
     *
     * @param cid the ID of the list
     * @return the number of objects contained in the list
     * @exception Exception if an error occurs
     */
    long getListSize(OID cid) throws Exception;
   
    /**
     * Say if a List contains an object.
     *
     * @param cid the ID of the List
     * @param value the object to look for in the list
     * @return wether the List contains the value
     * @exception Exception if an error occurs
     */
    boolean listContains(OID cid, Object value) 
        throws Exception;


    /**
     * Insert a value into an existing List.
     *
     * @param cid the ID of the List
     * @param position the position where to insert the value
     * @param value the value to insert
     * @exception Exception if an error occurs
     */
    void addToList(OID cid, long position, Object value)
        throws Exception;

    /**
     * Add a value at the end of a list.
     *
     * @param cid the ID of the List
     * @param value the value to add
     * @exception Exception if an error occurs
     */
    void addToList(OID cid, Object value)
        throws Exception;

    /**
     * Set the value of a list element.
     *
     * @param cid the ID of the List
     * @param index the index of the element to set
     * @param value the value
     * @exception Exception if an error occurs
     */
    void setListItem(OID cid, long index, Object value)
        throws Exception;

    /**
     * Remove a value from an existing list.
     *
     * @param cid the ID the List
     * @param position the position of the element to remove
     * @exception Exception if an error occurs
     */
    void removeFromList(OID cid, long position)
        throws Exception;

    /**
     * Remove the first value from an existing list.
     *
     * @param cid the ID the List
     * @param value the value to remove
     * @exception Exception if an error occurs
     */
    void removeFromList(OID cid, Object value)
        throws Exception;

    /**
     * Remove all objects from a list.
     *
     * @param cid the ID of the list to clear
     * @exception Exception if an error occurs
     */
    void clearList(OID cid) throws Exception;

    /**
     * Get the smallest index of an element in a List.
     *
     * @param cid the ID of the List
     * @param value the value
     * @return the index of value
     * @exception Exception if an error occurs
     */
    long getIndexInList(OID cid, Object value)
        throws Exception;

    /**
     * Get the highest index of an element in a List.
     *
     * @param cid the ID of the List
     * @param value the value
     * @return the index of value
     * @exception Exception if an error occurs
     */
    long getLastIndexInList(OID cid, Object value)
        throws Exception;

    // Set functions

    /**
     * Get the elements of a Set.
     *
     * @param oid the ID of the object that contains the Set
     * @param collection the collection
     * @return the elements of the Set
     * @exception Exception if an error occurs
     */
    List getSet(OID oid, CollectionItem collection) 
        throws Exception ;

    /**
     * Get the elements of a Set.
     *
     * @param cid the ID of the Set
     * @return the elements of the Set
     * @exception Exception if an error occurs
     */
    List getSet(OID cid) 
        throws Exception ;

   
    /**
     * Get the number of objects contained in a set
     *
     * @param cid the ID of the list
     * @return the number of objects contained in the set
     * @exception Exception if an error occurs
     */
    long getSetSize(OID cid) throws Exception;

    /**
     * Add an object to a Set.
     *
     * @param cid the ID of the Set
     * @param value the value to add
     * @return true if the set did not already contain the object, false otherwise.
     * @exception Exception if an error occurs
     */
    boolean addToSet(OID cid, Object value) 
        throws Exception ;

    /**
     * Remove an element from a Set.
     *
     * @param cid the ID of the Set
     * @param value the value to add
     * @return wether the set did contain the object
     * @exception Exception if an error occurs
     */
    boolean removeFromSet(OID cid, Object value) 
        throws Exception ;

    /**
     * Remove all objects from a set.
     *
     * @param cid the ID of the set to clear
     * @exception Exception if an error occurs
     */
    void clearSet(OID cid) 
        throws Exception;

    /**
     * Say if a set contains an object.
     *
     * @param cid the ID of the Set
     * @param value the value
     * @return wether the Set contains the value
     * @exception Exception if an error occurs
     */
    boolean setContains(OID cid, Object value) 
        throws Exception;

    // Map functions

    /**
     * Describe <code>getMap</code> method here.
     *
     * @param oid an <code>OID</code> value
     * @param collection a <code>CollectionItem</code> value
     * @return a <code>Map</code> value
     * @exception Exception if an error occurs
     */
    Map getMap(OID oid, CollectionItem collection) 
        throws Exception;

    /**
     * Get the elements of a Map.
     *
     * @param cid the ID of the Set
     * @return the Map
     * @exception Exception if an error occurs
     */
    Map getMap(OID cid) 
        throws Exception;
   
    /**
     * Get the number of objects contained in a map
     *
     * @param cid the ID of the list
     * @return the number of objects contained in the map
     * @exception Exception if an error occurs
     */
    long getMapSize(OID cid) throws Exception;

    /**
     * Put an element in a Map.
     *
     * @param cid the ID of the Map
     * @param key the key
     * @param value the value
     * @return the previous value associated with the key
     * @exception Exception if an error occurs
     */
    Object putInMap(OID cid, Object key, Object value) 
        throws Exception;

    /**
     * Get the value associated to a key from a Map.
     *
     * @param cid the ID of the Map
     * @param key the key
     * @return the value associated with the key
     * @exception Exception if an error occurs
     */
    Object getFromMap(OID cid, Object key) 
        throws Exception;

    /**
     * Says if a Map contains a key.
     *
     * @param cid the ID of the Map
     * @param key the key to search
     * @return wether the Map contains the key
     * @exception Exception if an error occurs
     */
    boolean mapContainsKey(OID cid, Object key)
        throws Exception;

    /**
     * Says if a Map contains a value.
     *
     * @param cid the ID of the Map
     * @param value the value to search
     * @return wether the Map contains the value
     * @exception Exception if an error occurs
     */
    boolean mapContainsValue(OID cid, Object value)
        throws Exception;

    /**
     * Remove a key from a Map.
     *
     * @param cid the ID the Map
     * @param key the key to remove
     * @return the previous value associated to the key, or null
     * @exception Exception if an error occurs
     */
    Object removeFromMap(OID cid, Object key)
        throws Exception;

    /**
     * Remove all objects from a set.
     *
     * @param cid the ID of the set to clear
     * @exception Exception if an error occurs
     */
    void clearMap(OID cid) 
        throws Exception;

    /**
     * Remove a field from an existing object.
     *
     * @param oid the ID of the object that contains the field
     * @param field the ID of the field to remove
     * @param value <b>Deprecated</b> 
     */
    void removeField(OID oid, FieldItem field, Object value) 
        throws Exception;

    /**
     * Generate a new name for an instance.
     *
     * @param className the className of the instance for which to generate a name
     * @return the generated name, null if failure
     */
    String newName(String className) throws Exception;

    /**
     * Gets the name counters used to generate new names.
     * @return a Map associating counter names to a Long value. The
     * value is the next value to be used.
     * @see #updateNameCounters(Map) 
     */
    Map getNameCounters() throws Exception;

    /**
     * Sets the name counters used to generate new names.
     * @param counters a Map associating counter names to a Long
     * value. The value is the next value to be used. A counter is
     * updated only if the suplied value is greater than the current
     * value.
     * @see #getNameCounters()
     */
    void updateNameCounters(Map counters) throws Exception;

    /**
     * Get the ID of an object from its name.
     *
     * @param name the candidate object name
     * @return null if not found 
     */
    OID getOIDFromName(String name) throws Exception;

    /**
     * Get the name of an object from its oid.
     *
     * @param oid the candidate object oid 
     */
    String getNameFromOID(OID oid) throws Exception;

    /**
     * Bind an existing object to a logical name to that it can be
     * found later on.<p>
     *
     * This method allows the user to create persistence roots.<p>
     *
     * @param oid an existing object ID
     * @param name the name that is given to it 
     */
    void bindOIDToName(OID oid,String name) throws Exception;

    /**
     * Delete a name from the storage.
     * @param name the name to remove
     */   
    void deleteName(String name) throws Exception;

    /**
     * Get the class ID of a given object.<p>
     *
     * @param oid the object's class ID
     * @exception Exception if an error occurs
     */
    String getClassID(OID oid) throws Exception;

    /**
     * Get OIDs of all root objects.
     *
     * @return the root objects
     * @exception Exception if an error occurs
     */
    Collection getRootObjects() throws Exception;

    /**
     * Get all instances of a class, or all objects if cl == null.
     *
     * @param cl the class
     * @return the instances
     */
    Collection getObjects(ClassItem cl) throws Exception;

    /**
     * Closes the storage.
     */
    void close();

    /**
     * Starts a transaction
     */
    void startTransaction() throws Exception;

    /**
     * Commit started transaction
     */
    void commit() throws Exception;

    /**
     * Rollback started transaction
     */
    void rollback() throws Exception;
}
