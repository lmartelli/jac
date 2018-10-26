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

import java.lang.NoSuchMethodException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.Wrappee;
import org.objectweb.jac.util.Strings;
import org.objectweb.jac.util.WrappedThrowableException;

/**
 * This class defines a meta item that corresponds to the
 * <code>java.lang.reflect.Method</code> meta element.
 *
 * <p>In addition to the <code>java.lang.reflect</code> classical
 * features, this RTTI method element is able to tell if a method is a
 * setter, a getter, or more generally speaking, a state modifier for
 * a given field of the object it belongs to. And, if this field is a
 * collection (an array, a list or a map), then this meta element is
 * able to tell if the method it represents adds or removes elements
 * to or from this collection.
 *
 * <p>It also provides to introspection features for references on Jac
 * objects.
 *
 * <p>For the moment, default meta informations are setted by the
 * <code>ClassRepository</code> class using some naming
 * conventions. In a close future, these informations will be deduced
 * from the class bytecodes analysis at load-time.
 *
 * @see java.lang.reflect.Method
 * @see #getWrittenFields()
 * @see #getAccessedFields()
 * @see #getAddedCollections()
 * @see #getRemovedCollections()
 * @see #isModifier()
 * @see #hasAccessedReferences()
 *
 * @author Renaud Pawlak
 * @author Laurent Martelli
 */

public class MethodItem extends AbstractMethodItem {
    static Logger logger = Logger.getLogger("rtti.method");

    /**
     * Transforms a method items array into a methods array containing
     * the <code>java.lang.reflect</code> methods wrapped by the method
     * items.<p>
     *
     * @param methodItems the method items
     * @return the actual methods in <code>java.lang.reflect</code>
     */

    public static Method[] toMethods( MethodItem[] methodItems ) {
        Method[] res = new Method[methodItems.length];
        for ( int i = 0; i < methodItems.length; i++ ) {
            if ( methodItems[i] == null ) {
                res[i] = null;
            } else {
                res[i] = methodItems[i].getActualMethod();
            }
        }
        return res;
    }

    /**
     * Default contructor to create a new method item object.<p>
     *
     * @param delegate the <code>java.lang.reflect.Method</code> actual
     * meta item */

    public MethodItem(Method delegate, ClassItem parent) 
        throws InvalidDelegateException 
    {
        super(delegate,parent);
        Class cl = delegate.getDeclaringClass();
        if (Wrappee.class.isAssignableFrom(cl)) {
            String orgName = "_org_"+delegate.getName()+
                (isStatic?"":"_"+Strings.getShortClassName(cl));
            try {
                orgMethod = 
                    cl.getDeclaredMethod(orgName,delegate.getParameterTypes());
                orgMethod.setAccessible(true);
            } catch(NoSuchMethodException e) {
                //Log.warning("No _org_ method "+orgName+" found for "+delegate);
            }
        }
    }

    Method orgMethod;

    public final Method getOrgMethod() {
        return orgMethod;
    }

    /**
     * Tells if this method accesses any references of the class it
     * belongs to.
     *
     * @return true if one or more references are accessed */
   
    public final boolean hasAccessedReferences() {
        ((ClassItem)parent).buildFieldInfo();
        return numAccessedReferences>0;
    }

    FieldItem returnedField;
    /**
     * Returns the field item corresponding to the field value returned
     * by this method, if any.
     * @see #setReturnedField(FieldItem)
     */
    public final FieldItem getReturnedField() {
        return returnedField;
    }
    /**
     * Sets the field returned by the method.
     * @see #getReturnedField()
     */
    public void setReturnedField(FieldItem returnedField) {
        if (this.returnedField!=null &&
            !(returnedField.isCalculated() && this.returnedField.isCalculated())) {
            logger.warn("overriding returned field "+this.returnedField.getName()+
                        " for "+getParent().getName()+"."+getName()+
                        " with "+returnedField.getName());
        }
        this.returnedField = returnedField;
    }

    FieldItem setField;
    /**
     * Returns <em>the</em> field set by this method, if any. In other
     * words, the field for which the method is <em>the</em> setter. A
     * method is <em>the</em>setter of a field if it assigns this field
     * directly with one of its argument.
     * @see #setReturnedField(FieldItem) */
    public final FieldItem getSetField() {
        return setField;
    }
    /**
     * Sets <em>the</em> field set by the method. This method should
     * not be called more than once for a gievn field.
     * @see #getSetField() */
    public void setSetField(FieldItem setField) {
        if (this.setField!=null && this.setField!=setField) {
            logger.warn("overriding set field "+this.setField.getName()+
                        " for "+getParent().getName()+"."+getName()+
                        " with "+setField.getName());
        }
        this.setField = setField;
    }

    /** Store the collections that are added by this method.<p> */
    CollectionItem[] addedCollections = null;
   

    /**
     * Get the value of the collections that are added by this
     * method.<p>
     *
     * @return value of addedCollections.  */

    public final CollectionItem[] getAddedCollections() {
        ((ClassItem)parent).buildFieldInfo();
        return addedCollections;
    }

    /**
     * Returns true if the method has at least one added collection
     */
    public final boolean hasAddedCollections() {
        ((ClassItem)parent).buildFieldInfo();
        return addedCollections!=null && addedCollections.length>0;
    }

    /**
     * Sets the value of the collections that are added by this method.<p>
     *
     * @param addedCollections value to assign to addedCollections
     * @see #addAddedCollection(CollectionItem)
     */

    public final void setAddedCollections(CollectionItem[] addedCollections) {
        this.addedCollections = addedCollections;
    }

    /**
     * Adds a new added collection for this method.<p>
     *
     * @param addedCollection the collection to add
     * @see #removeAddedCollection(CollectionItem)
     */

    public final void addAddedCollection(CollectionItem addedCollection) {
        if (addedCollections == null) {
            addedCollections = new CollectionItem[] { addedCollection };
        } else {
            CollectionItem[] tmp = new CollectionItem[addedCollections.length + 1];
            System.arraycopy(addedCollections, 0, tmp, 0, addedCollections.length);
            tmp[addedCollections.length] = addedCollection;
            addedCollections = tmp;
        }
    }

    /**
     * Removes an existing added collection for this method.<p>
     *
     * @param collection the collection to add
     * @see #addAddedCollection(CollectionItem) 
     */
    public final void removeAddedCollection(CollectionItem collection) {
        if (addedCollections != null) {
            Vector v = new Vector(Arrays.asList(addedCollections));
            v.remove(collection);
            addedCollections = new CollectionItem[v.size()];
            System.arraycopy(v.toArray(),0,addedCollections,0,v.size());
        }
    }

    /**
     * Gets the method represented by this method item.<p>
     *
     * @return the actual method
     */

    public final Method getActualMethod() {
        return (Method)delegate;
    }

    public final String getName() {
        return ((Method)delegate).getName();
    }

    public final Class getType() {
        return ((Method)delegate).getReturnType();
    }

    public Class[] getParameterTypes() {
        return ((Method)delegate).getParameterTypes();
    }

    int collectionIndexArgument = -1;
    /**
     * Returns the number of the argument which is the index of a
     * modified collection if any, -1 otherwise.
     */
    public int getCollectionIndexArgument() {
        return collectionIndexArgument;
    }
    /**
     * Sets collectionIndexArgument
     * @see #getCollectionIndexArgument()
     */
    public void setCollectionIndexArgument(int collectionIndexArgument) {
        this.collectionIndexArgument = collectionIndexArgument;
    }

    int collectionItemArgument = -1;
    /**
     * Returns the number of the argument which is the item that will be added
     * to a collection if any, -1 otherwise.
     */
    public int getCollectionItemArgument() {
        return collectionItemArgument;
    }
    /**
     * Sets collectionItemArgument
     * @see #getCollectionItemArgument()
     */
    public void setCollectionItemArgument(int collectionItemArgument) {
        this.collectionItemArgument = collectionItemArgument;
    }
   
    public void addAccessedField(FieldItem accessedField) {
        super.addAccessedField(accessedField);
        if (getType()!=void.class)
            accessedField.addDependentMethod(this);
    }

    /**
     * Invokes this method on the given object and with the given
     * parameters values.
     *
     * @param object a class this method belongs to intance
     * @param parameters the values of the parameters to invoke this
     * method with 
     */
    public Object invoke(Object object, Object[] parameters) 
    {
        logger.debug(toString()+" invoke("+object+","+Arrays.asList(parameters)+")");
        if (!isStatic() && object==null)
            throw new NullPointerException(
                "Invoking instance method "+
                parent.getName()+"."+this+" on null");
        try {
            return ((Method)delegate).invoke(object,parameters);
        } catch (IllegalArgumentException e) {
            Class cl = (Class)getParent().getDelegate();
            if (!cl.isAssignableFrom(object.getClass())) {
                throw new IllegalArgumentException(
                    getLongName()+": called object "+Strings.hex(object)+
                    " is not an instance of "+cl.getName());
            }
            if (parameters.length == getParameterCount()) {
                checkParameters(parameters);
            }
            throw e;
        } catch (Exception e) {
            logger.info("caught exception "+e);
            throw new WrappedThrowableException(e);
        }
    }

    /**
     * Checks the type of parameters
     *
     * @param parameters parameters to check
     */    
    void checkParameters(Object[] parameters) {
        Class[] types = getParameterTypes();
        for (int i=0; i<types.length; i++) {
            if (!types[i].isAssignableFrom(parameters[i].getClass()))
                throw new IllegalArgumentException(
                    getLongName()+", argument n°"+(i+1)+":"+
                    parameters[i].getClass().getName()+
                    " cannot be converted to "+types[i].getName());
        }
    }

    /**
     * Invokes this static method with the given parameters values.
     *
     * @param parameters the values of the parameters to invoke this
     * method with */

    public final Object invokeStatic(Object[] parameters) 
    {
        if (!isStatic())
            throw new RuntimeException("Cannot invokeStatic a non static method: "+getLongName());
        //      logger.debug(toString()+" invoke("+object+","+Arrays.asList(parameters)+")");
        try {
            return ((Method)delegate).invoke(null,parameters);
        } catch (IllegalArgumentException e) {
            if (parameters.length == getParameterCount()) {
                checkParameters(parameters);
            }
            throw e;
        } catch (Exception e) {
            logger.info("Caught exception in "+getFullName(),e);
            throw new WrappedThrowableException(e);
        }
    }

    /**
     * Invokes a method with uninitialized parameters.
     *
     * <p>The parameters array is initialized before the invocation
     * with default values.
     *
     * @param object the target object
     * @param parameters the maybe initialized values of the parameters
     * to invoke this method with (primitive parameters can be null) */

    public final Object invokeWithInit(Object object,
                                       Object[] parameters)
        throws IllegalAccessException, InvocationTargetException
    {
      
        Class[] paramTypes = getParameterTypes();
        for (int i=0; i< parameters.length; i++) {
            if (parameters[i]==null) {
                if (paramTypes[i] == float.class) {
                    parameters[i] = new Float(0.0);
                } else if (paramTypes[i] == long.class) {
                    parameters[i] = new Long(0);
                } else if (paramTypes[i] == double.class) {
                    parameters[i] = new Double(0.0);
                } else if (paramTypes[i] == byte.class) {
                    parameters[i] = new Byte((byte)0);
                } else if (paramTypes[i] == char.class) {
                    parameters[i] = new Character(' ');
                } else if (paramTypes[i] == short.class) {
                    parameters[i] = new Short((short)0);
                } else if (paramTypes[i] == int.class) {
                    parameters[i] = new Integer(0);
                } else if (paramTypes[i] == boolean.class) {
                    parameters[i] = Boolean.FALSE;
                }
            }
        }
        return ((Method)delegate).invoke(object,parameters);
    } 

    /** Tells wether the method returns the value of a field */
    public final boolean isGetter() {
        ((ClassItem)parent).buildFieldInfo();
        return returnedField!=null;
    }

    public final boolean isCollectionGetter() {
        ((ClassItem)parent).buildFieldInfo();
        return returnedField!=null && (returnedField instanceof CollectionItem);      
    }

    /** Tells wether the method is <em>the</em> setter of a field */
    public final boolean isSetter() {
        ((ClassItem)parent).buildFieldInfo();
        return setField!=null;
    }

    /** Tells wether the method is an adder of a collection */
    public final boolean isAdder() {
        ((ClassItem)parent).buildFieldInfo();
        return addedCollections!=null && addedCollections.length>0;
    }


    /** Stores the collections that are removed by this method.<p> */
    CollectionItem[] removedCollections = null;
   
    /**
     * Gets the value of the collections that are removed by this
     * method.<p>
     *
     * @return value of removedCollections.  */

    public final CollectionItem[] getRemovedCollections() {
        ((ClassItem)parent).buildFieldInfo();
        return removedCollections;
    }

    public final CollectionItem getRemovedCollection() {
        CollectionItem[] colls = getRemovedCollections();
        return ((colls != null) ? colls[0] : null);
    }
   
    /**
     * Returns true if the method has at least one removed collection
     */

    public final boolean hasRemovedCollections() {
        ((ClassItem)parent).buildFieldInfo();
        return removedCollections!=null && removedCollections.length>0;
    }

    /**
     * Sets the value of the collections that are removed by this method.<p>
     *
     * @param removedCollections value to assign to removedCollections
     * @see #addRemovedCollection(CollectionItem)
     */

    public final void setRemovedCollections(CollectionItem[] removedCollections) {
        this.removedCollections = removedCollections;
    }

    /**
     * Adds a new removed collection for this method.<p>
     *
     * @param removedCollection the collection to add
     * @see #setRemovedCollections(CollectionItem[])
     * @see #removeRemovedCollection(CollectionItem)
     */

    public final void addRemovedCollection(CollectionItem removedCollection) {
        if (removedCollections == null) {
            removedCollections = new CollectionItem[] { removedCollection };
        } else {
            CollectionItem[] tmp = new CollectionItem[removedCollections.length+1];
            System.arraycopy(removedCollections, 0, tmp, 0, 
                             removedCollections.length);
            tmp[removedCollections.length] = removedCollection;
            removedCollections = tmp;
        }
    }

    /**
     * Removes an existing removed collection for this method.<p>
     *
     * @param collection the collection to remove
     * @see #addRemovedCollection(CollectionItem)
     */
    public final void removeRemovedCollection(CollectionItem collection) {
        if (removedCollections != null) {
            Vector v = new Vector(Arrays.asList(removedCollections));
            v.remove(collection);
            removedCollections = new CollectionItem[v.size()];
            System.arraycopy(v.toArray(),0,removedCollections,0,v.size());
        }
    }

    /** Tells wether the method is a remover of a collection */
    public final boolean isRemover() {
        ((ClassItem)parent).buildFieldInfo();
        return removedCollections!=null && removedCollections.length>0;
    }

    public final boolean isAccessor() {
        ((ClassItem)parent).buildFieldInfo();
        return accessedFields!=null && accessedFields.length>0;
    }

    public final boolean isWriter() {
        ((ClassItem)parent).buildFieldInfo();
        return hasWrittenFields();
    }

    public final boolean isCollectionAccessor() {
        ((ClassItem)parent).buildFieldInfo();
        if (accessedFields!=null) {
            for (int i=0; i<accessedFields.length; i++) {
                if (accessedFields[i] instanceof CollectionItem) {
                    return true;
                }
            }
        }
        return false;
    }

    public final boolean isReferenceAccessor() {
        ((ClassItem)parent).buildFieldInfo();
        if (accessedFields!=null) {
            for (int i=0; i<accessedFields.length; i++) {
                if (accessedFields[i].isReference()) {
                    return true;
                }
            }
        }
        return false;
    }

    public final boolean isCollectionSetter() {
        ((ClassItem)parent).buildFieldInfo();
        return setField instanceof CollectionItem;
    }

    public final boolean isFieldSetter() {
        ((ClassItem)parent).buildFieldInfo();
        return setField!=null && setField.isPrimitive();
    }

    public final boolean isReferenceSetter() {
        ((ClassItem)parent).buildFieldInfo();
        return setField!=null && setField.isReference();
    }

    public final boolean isFieldGetter() {
        ((ClassItem)parent).buildFieldInfo();
        return !(returnedField instanceof CollectionItem) 
            && returnedField.isPrimitive();
    }

    public final boolean isReferenceGetter() {
        ((ClassItem)parent).buildFieldInfo();
        return returnedField!=null && returnedField.isReference();
    }

    /** Tells wether the method was introduced by JAC */
    public final boolean isJacMethod() {
        return ClassRepository.isJacMethod(getName());
    }

    public final static MethodItem[] emptyArray = new MethodItem[0];
}// MethodItem
