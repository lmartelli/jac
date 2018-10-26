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

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Vector;

/**
 * This class defines a meta item that corresponds to the
 * <code>java.lang.reflect.Method</code> and to the
 * <code>java.lang.reflect.Constructor</code> meta elements.
 *
 * <p>It appears that methods and contructors can be seen as
 * semantically similar (a constructor is nothing else that a special
 * static method that returns an object that is of the owner class
 * type). Thus, we decide to wrap those two meta item into a unique
 * one (on contrary to the Java choice)<p>
 *
 * @author Laurent Martelli
 * @author Renaud Pawlak
 */

public abstract class AbstractMethodItem extends MemberItem {
   
    /**
     * Default contructor to create a new abstract method item.<p>
     *
     * @param delegate the <code>java.lang.reflect</code> actual
     * meta item */

    public AbstractMethodItem(Object delegate, ClassItem parent)
        throws InvalidDelegateException 
    {
        super(delegate,parent);
        isStatic = Modifier.isStatic(((Member)delegate).getModifiers());
    }

    /**
     * If the field does not have a value for the request attribute,
     * tries on the superclass.
     */
    public final Object getAttribute(String name) {
        Object value = super.getAttribute(name);
        if (value==null) {
            ClassItem parent = ((ClassItem)getParent()).getSuperclass();
            if (parent!=null) {
                try {
                    AbstractMethodItem parentMethod = parent.getAbstractMethod(getFullName());
                    value = parentMethod.getAttribute(name);
                } catch (NoSuchMethodException e) {
                    ClassItem[] interfaces = parent.getInterfaceItems();
                    for (int i=0; i<interfaces.length && value==null;i++) {
                        if (parent.hasMethod(getFullName())) {
                            AbstractMethodItem parentMethod = parent.getAbstractMethod(getFullName());
                            value = parentMethod.getAttribute(name);
                        }      
                    }
                }
            }
        }
        return value;
    }

    /**
     * Gets the parameter types of this abstract method item.<p>
     *
     * @return the actual method parameter types
     */
    public abstract Class[] getParameterTypes();
    
    public void setParameter(Object[] params, int i, Object value) {
        params[i] = value;
    }

    public Object getParameter(Object[] params, int i) {
        return params[i];
    }

    /**
     * Get the ClassItem of the type of a parameter of the method.
     * @param n the number of the parameter
     * @return the ClassItem of the type of the parameter
     */
    public ClassItem getParameterTypeItem(int n) {
        return ClassRepository.get().getClass(getParameterTypes()[n]);
    }

    /**
     * Gets the number of parameters
     *
     * @return the number of parameters.
     */
    public int getParameterCount() {
        return getParameterTypes().length;
    }

    boolean isStatic = false;

    public final boolean isStatic() {
        return isStatic;
    }
   
    private String fullName;
    /**
     * Return the full method name, ie with parameter types.
     *
     * @return The full method name. For instance myMethod(java.lang.String,int)
     * @see #getCompactFullName()
     */
    public String getFullName() {
        if (fullName==null) {
            fullName = getFullName(getName(),getParameterTypes());
        }
        return fullName;
    }

    String realFullName;
    /**
     * Return the full method name, ie with parameter types.
     *
     * @return The full method name. For instance myMethod(java.lang.String,int)
     * @see #getCompactFullName()
     */
    public String getRealFullName() {
        if (realFullName==null) {
            realFullName = getFullName(getName(),((Method)delegate).getParameterTypes());
        }
        return realFullName;
    }

    public static String getFullName(String name, Class[] pts) {
        StringBuffer ret = new StringBuffer(name.length()+2+pts.length*15);
        ret.append(name);
        ret.append('(');
        for (int i=0; i<pts.length; i++) {
            ret.append(NamingConventions.getStandardClassName(pts[i]));
            if (i < pts.length-1) 
                ret.append(',');
        }
        ret.append(')');
        return ret.toString();
    }

    /**
     * Return the full method name, ie with short parameter types.
     *
     * @return The full method name. For instance "myMethod(String,int)"
     * @see #getFullName()
     */
    public String getCompactFullName() {
        //.substring(0, getName().lastIndexOf('.'))
        Class[] pts = getParameterTypes();
        StringBuffer ret = new StringBuffer(getName().length()+2+pts.length*15);
        ret.append(getName());
        ret.append('(');
        for (int i=0; i<pts.length; i++) {
            ret.append(NamingConventions.getShortClassName(pts[i]));
            if (i < pts.length-1) 
                ret.append(',');
        }
        ret.append(')');
        return ret.toString();
    }

    private String longName;
    public String getLongName() {
        if (longName==null)
            longName = parent.getName()+"."+getFullName();
        return longName;
    }

    public Object invoke(Object object, Object[] parameters) 
    {
        if (true) {
            throw new RuntimeException(
                "wrong invocation on an abstract method "+this);
        }
        return null;
    }

    AbstractMethodItem concreteMethod;

    /**
     * Returns the method item who really holds the byte code. 
     */
    public AbstractMethodItem getConcreteMethod() {
        if (concreteMethod==null) {
            Member m = (Member)delegate;
            if (parent.getDelegate() == m.getDeclaringClass()) {
                return this;
            }
            concreteMethod = ClassRepository.get().getClass(m.getDeclaringClass()).
                getAbstractMethod(getRealFullName());
        }
        return concreteMethod;
    }

    /**
     * Returns the owning class of this method.
     */
    public ClassItem getOwningClass() {
        return (ClassItem)parent;
    }

    public abstract boolean isAdder();
    public abstract CollectionItem[] getAddedCollections();
    /**
     * Get the value of the collection that is added by this
     * method (the method is the unique adder of the collection).<p>
     *
     * @return value of addedCollection.
     */
    public final CollectionItem getAddedCollection() {
        CollectionItem[] colls = getAddedCollections();
        return ((colls != null) ? colls[0] : null);
    }

    public abstract boolean isRemover();
    public abstract CollectionItem[] getRemovedCollections();

    public abstract boolean isAccessor();
    public abstract boolean isWriter();
    public abstract boolean isGetter();
    public abstract boolean isSetter();

    public abstract boolean isCollectionGetter();
    public abstract boolean isCollectionAccessor();
    public abstract boolean isCollectionSetter();

    public abstract boolean isFieldGetter();
    public abstract boolean isFieldSetter();

    public abstract boolean isReferenceGetter();
    public abstract boolean isReferenceSetter();
    public abstract boolean isReferenceAccessor();

    public abstract FieldItem getSetField();

    /** Stores the fields that are written by this method.<p> */
    FieldItem[] writtenFields = null;
   
    /**
     * Gets the value of the fields that are written by this method.<p>
     *
     * @return value of writtenFields.  */

    public final FieldItem[] getWrittenFields() {
        ((ClassItem)parent).buildFieldInfo();
        return writtenFields;
    }
   
    public final boolean hasWrittenFields() {
        ((ClassItem)parent).buildFieldInfo();
        return writtenFields!=null && writtenFields.length>0;
    }

    /**
     * Sets the value of the fields that are written by this method.<p>
     *
     * @param writtenFields value to assign to writtenFields
     * @see #addWrittenField(FieldItem)
     */

    public final void setWrittenFields(FieldItem[] writtenFields) {
        this.writtenFields = writtenFields;
    }

    /**
     * Adds a new written field for this method.<p>
     *
     * @param writtenField the field to add
     * @see #setWrittenFields(FieldItem[])
     * @see #removeWrittenField(FieldItem)
     */
    public final void addWrittenField(FieldItem writtenField) {
        if (writtenFields == null) {
            writtenFields = new FieldItem[] { writtenField };
        } else {
            FieldItem[] tmp = new FieldItem[writtenFields.length + 1];
            System.arraycopy(writtenFields, 0, tmp, 0, writtenFields.length);
            tmp[writtenFields.length] = writtenField;
            writtenFields = tmp;
        }
    }

    /**
     * Removes a new written field for this method.<p>
     *
     * @param field the field to remove
     * @see #addWrittenField(FieldItem)
     */
    public final void removeWrittenField(FieldItem field) {
        if (writtenFields != null) {
            Vector v = new Vector(Arrays.asList(writtenFields));
            v.remove(field);
            writtenFields = new FieldItem[v.size()];
            System.arraycopy(v.toArray(),0,writtenFields,0,v.size());
        }
    }

    /** Stores the number of references read by this method */
    int numAccessedReferences = 0;

    /** Stores the number of collections read by this method */
    int numAccessedCollections = 0;

    /** Stores the collections that are modified by this method.<p> */
    CollectionItem[] modifiedCollections = null;
   
    /**
     * Gets the value of the collections that are modified by this
     * method.<p>
     *
     * @return value of removedCollections.  */

    public final CollectionItem[] getModifiedCollections() {
        ((ClassItem)parent).buildFieldInfo();
        return modifiedCollections;
    }
   
    /**
     * Returns true if the method has at least one modified collection
     */

    public final boolean hasModifiedCollections() {
        ((ClassItem)parent).buildFieldInfo();
        return modifiedCollections!=null && modifiedCollections.length>0;
    }

    /**
     * Adds a new modified collection for this method.<p>
     *
     * @param modifiedCollection the collection to add
     */

    public final void addModifiedCollection(CollectionItem modifiedCollection) {
        if ( modifiedCollections == null ) {
            modifiedCollections = new CollectionItem[] { modifiedCollection };
        } else {
            CollectionItem[] tmp = new CollectionItem[modifiedCollections.length + 1];
            System.arraycopy( modifiedCollections, 0, tmp, 0, modifiedCollections.length );
            tmp[modifiedCollections.length] = modifiedCollection;
            modifiedCollections = tmp;
        }
    }

    /** Stores the fields that are read by this method.<p> */
    FieldItem[] accessedFields = null;

    /**
     * Gets the value of the fields that are written by this method.<p>
     *
     * @return value of accessedFields.  */

    public final FieldItem[] getAccessedFields() {
        ((ClassItem)parent).buildFieldInfo();
        return accessedFields;
    }

    /**
     * Sets the value of the fields that are read by this method.<p>
     *
     * @param accessedFields value to assign to accessedFields
     * @see #addAccessedField(FieldItem)
     */

    public void setAccessedFields(FieldItem[]  accessedFields) {
        this.accessedFields = accessedFields;
        numAccessedReferences = 0;
        numAccessedCollections = 0;
        if (accessedFields!=null) {
            for(int i=0; i<accessedFields.length; i++) {
                if (accessedFields[i].isReference())
                    numAccessedReferences++;
                else if (accessedFields[i] instanceof CollectionItem)
                    numAccessedCollections++;
            }
        }
    }

    /**
     * Adds a new accessed field for this method.<p>
     *
     * @param accessedField the field to add
     * @see #setAccessedFields(FieldItem[])
     * @see #removeAccessedField(FieldItem)
     */

    public void addAccessedField(FieldItem accessedField) {
        if (accessedField.isReference())
            numAccessedReferences++;
        else if (accessedField instanceof CollectionItem)
            numAccessedCollections++;
        if (accessedFields == null) {
            accessedFields = new FieldItem[] { accessedField };
        } else {
            FieldItem[] tmp = new FieldItem[accessedFields.length + 1];
            System.arraycopy( accessedFields, 0, tmp, 0, accessedFields.length );
            tmp[accessedFields.length] = accessedField;
            accessedFields = tmp;
        }
    }

    /**
     * Removes an accessed field for this method.<p>
     *
     * @param field the field to remove
     * @see #addAccessedField(FieldItem)
     */
    public void removeAccessedField(FieldItem field) {
        if (accessedFields != null) {
            Vector v = new Vector(Arrays.asList(accessedFields));
            if (v.remove(field)) {
                if (field.isReference())
                    numAccessedReferences--;
                else if (field instanceof CollectionItem) 
                    numAccessedCollections--;
                accessedFields = new FieldItem[v.size()];
                System.arraycopy(v.toArray(),0,accessedFields,0,v.size());
            }
        }
    }

    /**
     * Gets the value of the fields that are written by this method.<p>
     *
     * @return value of accessedFields.
     */
    public final FieldItem[] getAccessedReferences() {
        ((ClassItem)parent).buildFieldInfo();
        FieldItem[] refs = new FieldItem[numAccessedReferences];
        int j=0;
        for (int i=0; i<accessedFields.length; i++) {
            if (accessedFields[i].isReference())
                refs[j++] = accessedFields[i];
        }
        return refs;
    }

    public final CollectionItem[] getAccessedCollections() {
        ((ClassItem)parent).buildFieldInfo();
        CollectionItem[] colls = new CollectionItem[numAccessedCollections];
        int j=0;
        for (int i=0; i<accessedFields.length; i++) {
            if (accessedFields[i] instanceof CollectionItem)
                colls[j++] = (CollectionItem)accessedFields[i];
        }
        return colls;
    }

    /** Tells wether the method modifies the state of the object */
    public final boolean isModifier() {
        ((ClassItem)parent).buildFieldInfo();
        return (writtenFields!=null && writtenFields.length>0) 
            || (modifiedCollections!=null && modifiedCollections.length>0) 
            || isAdder() || isRemover();
    }

   public String toString() {
       return getLongName();
   }
}
