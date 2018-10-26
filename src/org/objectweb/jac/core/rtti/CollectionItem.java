/*
  Copyright (C) 2003 Renaud Pawlak <renaud@aopsys.com>, 
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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.objectweb.jac.util.WrappedThrowableException;
import java.util.ArrayList;

/**
 * This class defines a meta item that corresponds to a
 * <code>java.lang.reflect.Field</code> meta element that is of an
 * array, a collection, or a map type.<p>
 *
 * @see java.util.Collection
 * @see java.util.Map
 * @see java.util.Arrays
 * @see java.lang.reflect.Field
 *
 * @author Renaud Pawlak
 * @author Laurent Martelli
 */

public class CollectionItem extends FieldItem {

    static Logger logger = Logger.getLogger("rtti.collection");
 
    protected ClassItem componentType;

    /**
     * Default contructor to create a new collection item object.<p>
     *
     * @param delegate the <code>java.lang.reflect.Field</code> actual
     * meta item */

    /**
     * Creates a CollectionItem for a real field
     * @param delegate the field the collection corresponds to
     * @param parent the class the collection belongs to
     */
    public CollectionItem(Field delegate, ClassItem parent) 
        throws InvalidDelegateException 
    {
        super(delegate,parent);
        type = delegate.getType();
        if (!RttiAC.isCollectionType(type)) {
            throw new InvalidDelegateException(delegate,"not a collection type");
        }  
    }

    /**
     * Creates a CollectionItem for an expression field.
     * @param name the expression
     * @param path the list of FieldItem the expression is made of
     * @param parent the class the collection belongs to
     */
    public CollectionItem(String name, List path, ClassItem parent) 
        throws InvalidDelegateException 
    {
        super(name,path,parent);
        if (!RttiAC.isCollectionType(type)) {
            throw new InvalidDelegateException(parent.getName()+"."+name,"Not a collection type");
        }  
    }

    /**
     * Creates a CollectionItem for a calculated field.
     * @param name the name of the collection
     * @param getter the getter of the calculated collection
     * @param parent the class the collection belongs to
     */
    public CollectionItem(String name, MethodItem getter, ClassItem parent) {
        super(name,getter,parent);
    }

    MethodItem[] addingMethods = MethodItem.emptyArray;
   
    /**
     * Gets the method items that access this collection for adding.<p>
     *
     * @return value of addingMethods.  
     * @see #hasAdder()
     */

    public MethodItem[] getAddingMethods() {
        ((ClassItem)parent).buildFieldInfo();
        return addingMethods;
    }

    /**
     * Returns true if the collection has at least one adding method.
     * @see #getAddingMethods()
     */
    public boolean hasAdder() {
        ((ClassItem)parent).buildFieldInfo();
        return addingMethods.length>0;
    }
   
    MethodItem adder;
    /**
     * Returns <em>the</em> adder of the collection, or null if it has
     * no adder.  
     */
    public MethodItem getAdder() {
        ((ClassItem)parent).buildFieldInfo();
        if (adder!=null) 
            return adder;
        else if (addingMethods.length>0)
            return addingMethods[0];
        else if (isExpression)
            return ((CollectionItem)getPathTop()).getAdder();
        else
            return null;
    }
    /**
     * Sets <em>the</em> adder of the collection.  
     * @param adder the adder
     */
    public void setAdder(MethodItem adder) {
        this.adder = adder;
    }

    /**
     * Sets the methods that access this collection for adding.<p>
     *
     * @param addingMethods value to assign to addingMethods.
     */

    public void setAddingMethods( MethodItem[] addingMethods ) {
        this.addingMethods = addingMethods;
    }

    /**
     * Adds a new adding method for this field.<p>
     *
     * @param addingMethod the method to add
     */

    public void addAddingMethod(MethodItem addingMethod) {
        if (addingMethods.length == 0) {
            addingMethods = new MethodItem[] { addingMethod };
        } else {
            MethodItem[] tmp = new MethodItem[addingMethods.length + 1];
            System.arraycopy(addingMethods, 0, tmp, 0, addingMethods.length);
            tmp[addingMethods.length] = addingMethod;
            addingMethods = tmp;
            if (getLongName().startsWith("org.objectweb.jac."))
                logger.debug("Adders for "+getLongName()+": "+Arrays.asList(tmp));
            else
                logger.warn("Adders for "+getLongName()+": "+Arrays.asList(tmp));
        }
    }

    /**
     * Returns the actual collection item. In the case of an expression
     * field, this is the last element of the path, otherwise it is the
     * field itself.  
     */
    public CollectionItem getCollection() {
        if (isExpression) {
            return (CollectionItem)getPathTop();
        } else {
            return this;
        }
    }

    /**
     * Returns the type of the objects contained in this collection.
     * @return the type of objects in this collection or null if it is
     * undefined.
     */
    public ClassItem getComponentType() {
        ((ClassItem)parent).buildFieldInfo();
        if (componentType!=null)
            return componentType;
        if (isArray()) {
            componentType = ClassRepository.get().getClass(getType().getComponentType());
        } else {

            if (isExpression) {
                componentType = ((CollectionItem)getPathTop()).getComponentType();
            } else {
                MethodItem adder = getAdder();
                if (adder!=null && adder.getParameterCount()>0) {
                    if (isMap()) {
                        if (isIndex())
                            componentType = adder.getParameterTypeItem(
                                adder.getParameterCount()-1);
                        else {
                            componentType = 
                                ClassRepository.get().getClass(
                                    "java.util.Map$Entry");
                        }
                    } else {
                        if (adder.getParameterCount()==1)
                            componentType = adder.getParameterTypeItem(0);
                        else if (adder.getParameterCount()==2) {
                            int itemArg = adder.getCollectionItemArgument();
                            if (itemArg!=-1)
                                componentType = adder.getParameterTypeItem(itemArg);
                        } 
                        if (componentType==null)
                            logger.warn(
                                "Cannot determine component type of "+getName()+
                                " from adder "+adder.getFullName());
                    }
                }
            }
        }

        if (componentType==null) {
            if (!isMap() || isIndex())
                logger.warn("Component type of "+this+" is null");
            if (adder==null && !isCalculated())
                logger.warn("no adder fo "+this+" "+getParent());
        }
        return componentType;
    }

    /**
     * Sets the component type of the collection.
     * @param componentType the component type
     */
    public void setComponentType(ClassItem componentType) {
        this.componentType = componentType;
    }

    public boolean isAddingMethod(MethodItem method) {
        ((ClassItem)parent).buildFieldInfo();
        return Arrays.asList(addingMethods).contains(method);
    }

    /**
     * Adds an item to the collection, using the adder method if it has one.
     * @param substance object on which to add
     * @param value object to add to the collection
     */
    public final void addThroughAdder(Object substance, Object value) {
        logger.debug("addThroughAdder "+substance+"."+getName()+","+value);
        MethodItem adder = getAdder();
        if (adder!=null) {
            try {
                //Log.trace("rtti.field",this+": invoking "+adders[i]);
                adder.invoke(substance,new Object[] { value });
                return;
            } catch (WrappedThrowableException e) {
                Throwable target = e.getWrappedThrowable();
                if (target instanceof InvocationTargetException)
                    throw e;
                else
                    logger.error("addThroughAdder "+substance+"."+getName()+","+value,e);
            } 
        }

        logger.warn("No adder for collection " + this);
        add(substance,value,null);
    }


    public final void putThroughAdder(Object substance, Object value, Object key) {
        logger.debug("putThroughAdder "+substance+"."+getName()+","+key+"->"+value);
        MethodItem adder = getAdder();
        if (adder!=null) {
            try {
                //Log.trace("rtti.field",this+": invoking "+adders[i]);
                if (adder.getParameterCount()==2)
                    adder.invoke(substance,new Object[] { key, value });
                else if (adder.getParameterCount()==1)
                    adder.invoke(substance,new Object[] { value });               
                else
                    throw new RuntimeException("putThroughAdder("+substance+","+value+","+key+
                                               ") :Wrong number off parameters for adder "+adder);
                return;
            } catch (WrappedThrowableException e) {
                Throwable target = e.getWrappedThrowable();
                if (target instanceof InvocationTargetException)
                    throw e;
                else
                    logger.error("putThroughAdder "+substance+"."+getName()+","+value,e);
            } 
        }

        logger.warn("No adder for collection " + this);
        add(substance,value,key);
    }

    MethodItem[] removingMethods = MethodItem.emptyArray;
   
    /**
     * Gets the methods that access this collection for removing.<p>
     *
     * @return value of removingMethods.
     */

    public MethodItem[] getRemovingMethods() {
        ((ClassItem)parent).buildFieldInfo();
        return removingMethods;
    }
   
    /**
     * Returns true if the collection has at least one removing method.
     * @see #getRemovingMethods()
     */
    public boolean hasRemover() {
        ((ClassItem)parent).buildFieldInfo();
        return removingMethods.length>0;
    }

    MethodItem remover;
    /**
     * Returns <em>the</em> remover of the collection, or null if it has
     * no remover.  
     */
    public MethodItem getRemover() {
        ((ClassItem)parent).buildFieldInfo();
        if (remover!=null) 
            return remover;
        else if (removingMethods.length>0)
            return removingMethods[0];
        else if (isExpression)
            return ((CollectionItem)getPathTop()).getRemover();
        else
            return null;
    }

    /**
     * Sets <em>the</em> remover of the collection.  
     * @param remover the remover
     */
    public void setRemover(MethodItem remover) {
        this.remover = remover;
    }

    /**
     * Sets the methods that access this collection for removing.<p>
     *
     * @param removingMethods value to assign to removingMethods.
     */

    public void setRemovingMethods( MethodItem[] removingMethods ) {
        this.removingMethods = removingMethods;
    }

    /**
     * Adds a new removing method for this field.<p>
     *
     * @param removingMethod the method to add
     */

    public void addRemovingMethod( MethodItem removingMethod ) {
        if (removingMethods == null) {
            removingMethods = new MethodItem[] { removingMethod };
        } else {
            MethodItem[] tmp = new MethodItem[removingMethods.length + 1];
            System.arraycopy(removingMethods, 0, tmp, 0, removingMethods.length);
            tmp[removingMethods.length] = removingMethod;
            removingMethods = tmp;
        }
    }

    public boolean isRemovingMethod(MethodItem method) {
        ((ClassItem)parent).buildFieldInfo();
        return Arrays.asList(removingMethods).contains(method);
    }

    /**
     * Clears all the methods that has been set to be removers or
     * adders for this collection item.
     *
     * @see #addAddingMethod(MethodItem)
     * @see #setAddingMethods(MethodItem[])
     * @see MethodItem#removeAddedCollection(CollectionItem)
     * @see #addRemovingMethod(MethodItem)
     * @see #setRemovingMethods(MethodItem[])
     * @see MethodItem#removeRemovedCollection(CollectionItem) */

    public void clearMethods() {
        super.clearMethods();
        if (removingMethods != null) {
            for (int i=0; i<removingMethods.length; i++) {
                removingMethods[i].removeRemovedCollection(this);
            }
            removingMethods = null;
        }
        if (addingMethods != null) {
            for (int i=0; i<addingMethods.length; i++) {
                addingMethods[i].removeAddedCollection(this);
            }
            addingMethods = null;
        }
    }

    /**
     * Clears the collection on the given object.<p>
     *
     * For maps and collections, it delegates to the <code>clear</code>
     * method. For array, it resets the adding index so that added
     * elements will crush existing ones.<p>
     *
     * @param substance the object where to clean the collection
     */

    public void clear(Object substance) {
        logger.debug(this+".clear("+substance+")");
        Field f = (Field) delegate;

        try {
            Object collection = f.get(substance);
            logger.debug("collection="+System.identityHashCode(collection));
            if (collection!=null) {
                logger.debug("type="+collection.getClass().getName());
                if (collection instanceof Collection) { 
                    ((Collection)collection).clear();
                } else if (collection instanceof Map) { 
                    ((Map)collection).clear();
                } else if (getType().isArray()) {
                    f.set(substance, Array.newInstance(getType().getComponentType(),0));
                }
            }
        } catch (Exception e) { 
            logger.error("clear failed for "+this+" on "+substance,e);
        }
    }

    /**
     * Tells if this collection is actually an array.<p>
     *
     * @return true if an array
     */

    public boolean isArray() {
        return getType()!=null && getType().isArray();
    }

    /**
     * Gets the collection object represented by this collection
     * item.
     *
     * <p>It returns a <code>java.util.Collection</code> representation of
     * the actual collection (i.e.  either a <code>Collection</code>, a
     * <code>Map</code>, or an array).
     *
     * <p>The programmer should rather use the methods that gets the
     * actual collection by using the accessor for this collection if
     * any since it allows possible aspects applications if needed.
     *
     * @return the actual object collection representation
     * @see #getActualCollectionThroughAccessor(Object)
     * @see #toCollection(Object) */

    public Collection getActualCollection(Object substance) {
        Object value = null;

        try {
            value = ((Field)delegate).get(substance);
        } catch (Exception e) {
            logger.error("getActualCollection("+getName()+") failed",e);
            return null;
        }
        return toCollection(value);
    }

    /**
     * Gets the collection object represented by this collection item
     * by using the accessor defined in the class containing this
     * collection if any.
     *
     * <p>Use this method to be sure that all the aspects will be
     * applied to the substance when the collection is retrieved.
     *
     * <p>It returns a <code>java.util.Collection</code> representation of
     * the actual collection (i.e.  either a <code>Collection</code>, a
     * <code>Map</code>, or an array).
     *
     * @return the actual object collection representation
     * @see #getActualCollection(Object)
     * @see #toCollection(Object) 
     */
    public Collection getActualCollectionThroughAccessor(Object substance) {
        Object value = getThroughAccessor(substance);
        return value!=null ? toCollection(value) : new ArrayList();
    }

    /**
     * A useful method to convert any kind of collection to a
     * <code>java.util.Collection</code> compatible instance.
     *
     * <p>Supported converted value types are
     * <code>java.util.Collection</code>, <code>java.util.Map</code>,
     * and Java arrays.
     *
     * @param value an object of one of the supported types
     * @return the collection compliant converted value */

    public Collection toCollection(Object value) {
        if (value == null)  return null;

        if (value instanceof Collection) { 
            return (Collection)value;
        } else if (value instanceof Map) { 
            if (isIndex()) 
                return ((Map)value).values();
            else
                return ((Map)value).entrySet();
        } else if (value.getClass().isArray()) {
            if (value.getClass().getComponentType()==Object.class) {
                return Arrays.asList( (Object[]) value );
            } else {
                int length = Array.getLength(value);
                Vector result = new Vector(length);
                for (int i=0; i<length; i++) {
                    result.add(Array.get(value,i));
                }
                return result;
            }
        }
        return null;
    }

    /**
     * Adds an item to the collection.<p>
     *
     * The <code>extraInfos</code> param must represent a key if the
     * collection is an hastable. If the collection is a
     * <code>java.util</code> collection or if it is an array, the
     * extra informations can be null (in this case, the new item is
     * added at the end of the collection), and they can be not
     * null. In this case, it must be an <code>Integer</code> instance
     * that represents the index of the new item within the
     * collection (must be a list in this case).<p>
     *
     * @param substance the collection where to add the new item
     * @param newItem the item to add
     * @param extraInfos this optional parameter must be used when the
     * collection need some other information when adding a item to it
     * as, for instance the key for a map or the index for a list 
     */
    public void add(Object substance, Object newItem, Object extraInfos) {

        Field f = (Field) delegate;

        try {
            if (isSet() || isList()) { 
                // Set and List
                if ( extraInfos == null ) {
                    ((Collection)f.get(substance)).add(newItem);
                } else {
                    ((List)f.get(substance)).add( 
                        ((Integer)extraInfos).intValue(), newItem);
                }
            } else if (isMap()) { 
                // Map
                ((Map)f.get(substance)).put(extraInfos, newItem);
            } else if (isArray()) {
                // Arrays
                // THIS IS BUGGED !!!
                if (extraInfos == null) {
                    Object oldArray = f.get(substance);
                    int index = 0;
                    if (oldArray != null) {
                        index = Array.getLength(oldArray);
                    }
                    Object newArray;
                    f.set( substance, 
                           newArray = Array.newInstance( 
                               getType().getComponentType(), index+1 ) );

                    if (oldArray != null) {
                        for(int i=0; i<index; i++) {
                            Array.set(newArray, i, Array.get(oldArray, i));
                        }
                    }
               
                    Array.set(newArray, index, newItem);

                } else {
                    ((Object[])f.get(substance))[((Integer)extraInfos).intValue()] = newItem;
                    //arrayAddingIndex = ((Integer)extraInfos).intValue() + 1;
                }
            }
        } catch ( Exception e ) { 
            logger.error("add "+substance+"."+getName()+" "+newItem+","+extraInfos,e);
        }
    }

    /**
     * Removes an item from the collection, using the remover method if it has one.
     * @param substance object on which to remove
     * @param item object to remove from the collection
     */
    public void removeThroughRemover(Object substance, Object item) {
        ((ClassItem)parent).buildFieldInfo();
        MethodItem remover = getRemover();
        if (remover!=null) {
            remover.invoke(substance,new Object[] {item});
        } else {
            logger.warn("No remover for collection "+this);
            remove(substance,item,null);
        }
    }

    /**
     * Removes an item from the collection.<p>
     *
     * The <code>extraInfos</code> param must represent a key if the
     * collection is an hastable. If the collection is a
     * <code>java.util</code> collection or if it is an array, the
     * extra informations can be null (in this case, the new item is
     * added at the end of the collection), and they can be not
     * null. In this case, it must be an <code>Integer</code> instance
     * that represents the index of the new item within the
     * collection (must be a list in this case).<p>
     *
     * @param substance the collection where to remove the new item
     * @param item the item to remove (can be null if extra infos are
     * not null)
     * @param extraInfos this optional parameter must be used when the
     * collection need some other information when adding a item to it
     * as, for instance the key for a map, or the index in a list */

    public void remove(Object substance, Object item, Object extraInfos) {
        Field f = (Field)delegate;

        try {      
      
            if (Collection.class.isAssignableFrom(getType())) { 
                // Set and List
                if (extraInfos == null) {
                    ((Collection)f.get(substance)).remove(item);
                } else {
                    int index = ((Integer)extraInfos).intValue();
                    ((List)f.get(substance)).remove(index);
                }
            } else if (Map.class.isAssignableFrom(getType())) {
                // Map
                ((Map)f.get(substance)).remove(extraInfos);
            } else if (getType().isArray()) {
                // Arrays
                // THIS IS BUGGED !!!
                if (extraInfos == null) {
                    Object oldArray = f.get(substance);
                    int index = 0;
                    if (oldArray != null) {
                        index = Array.getLength(oldArray);
                    }
                    Object newArray;
                    f.set( substance, 
                           newArray = Array.newInstance( 
                               getType().getComponentType(), index-1 ) );

                    if (oldArray != null) {
                        for( int i=0; i<index; i++) {
                            Array.set(newArray, i, Array.get(oldArray,i));
                        }
                    }
               
                    //Array.set( newArray, index, newItem );

                } else {
                    //((Object[])f.get( substance ))[((Integer)extraInfos).intValue()] = item;
                    //arrayAddingIndex = ((Integer)extraInfos).intValue() + 1;
                }
            }  
        } catch (Exception e) { 
            logger.error("remove "+substance+"."+getName()+" "+item+","+extraInfos,e);
        }
      
    }

    /**
     * Tells wether an object's collection contains an given object
     * @param substance
     * @param object the object to search
     * @return true if substance.collection.contains(object)
     */
    public boolean contains(Object substance, Object object) {
        if (!isArray()) {
            Collection collection = (Collection)getThroughAccessor(substance);
            return collection.contains(object);
        } else {
            throw new RuntimeException(
                "CollectionItem.contains(substance,object) is not implemented for arrays");
        }
    }

    public Object getMap(Object substance, Object key) {
        if (!isMap()) {
            throw new RuntimeException("Cannot call getMap() on "+this);
        }
        try {
            return getMap(substance).get(key);
        } catch(IllegalAccessException e) {
            logger.error("getMap "+substance+"."+getName()+" "+key,e); 
        }
        return null;
    }

    protected Map getMap(Object substance) throws IllegalAccessException {
        return (Map)((Field)delegate).get(substance);
    }

    /**
     * Tells if this collection item is compliant with a
     * <code>java.util.List</code> interface.
     *
     * @return true if compliant */

    public boolean isList() {
        return List.class.isAssignableFrom(getType());
    }
   
    /**
     * Tells if this collection item is compliant with a
     * <code>java.util.Map</code> interface.
     *
     * @return true if compliant */

    public boolean isMap() {
        return Map.class.isAssignableFrom(getType());
    }
   
    public boolean isIndex() {
        return RttiAC.isIndex(this);
    }

    /**
     * Tells if this collection item is compliant with a
     * <code>java.util.Set</code> interface.
     *
     * @return true if compliant */

    public boolean isSet() {
        return java.util.Set.class.isAssignableFrom(getType());
    }
   
    /**
     * Always returns false for collections (maybe not very
     * semantically clear).
     *
     * @return false */

    public boolean isPrimitive() {
        return false;
    }

    /**
     * Always returns false for collections (maybe not very
     * semantically clear).
     *
     * @return false */

    public boolean isReference() {
        return false;
    }

    public FieldItem clone(ClassItem parent) {
        CollectionItem clone = null;
        try {
            if (isCalculated)
                clone = new CollectionItem(name,getter,parent);
            else 
                clone = new CollectionItem((Field)delegate,parent);
            clone.setAdder(adder);
            clone.setRemover(remover);
        } catch(Exception e) {
            logger.error("Failed to clone collection "+this);
        }
        return clone;      
    }

    public static final CollectionItem[] emptyArray = new CollectionItem[0];
}
