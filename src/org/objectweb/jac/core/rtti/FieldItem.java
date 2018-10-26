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
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA */

package org.objectweb.jac.core.rtti;

import java.lang.NoSuchMethodException;
import java.lang.reflect.*;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.objectweb.jac.util.*;

/**
 * This class defines a meta item that corresponds to the
 * <code>java.lang.reflect.Field</code> meta element.<p>
 *
 * <p>In addition to the <code>java.lang.reflect</code> classical
 * features, this RTTI method element is able to tell if a field is
 * accessed for reading or writting by a given method.
 *
 * <p>It also provides some modification methods that are aspect compliant.
 *
 * <p>For the moment, default meta informations are setted by the
 * <code>ClassRepository</code> class using some naming
 * conventions. In a close future, these informations will be deduced
 * from the class bytecodes analysis at load-time.
 *
 * @see java.lang.reflect.Field
 * @see #getWritingMethods()
 * @see #getAccessingMethods()
 *
 * @author Renaud Pawlak
 * @author Laurent Martelli
 */

public class FieldItem extends MemberItem {

    static Class wrappeeClass = ClassRepository.wrappeeClass;
    static Logger logger = Logger.getLogger("rtti.field");

    /**
     * Transforms a field items array into a fields array containing
     * the <code>java.lang.reflect</code> fields wrapped by the method
     * items.<p>
     *
     * @param fieldItems the field items
     * @return the actual fields in <code>java.lang.reflect</code>
     */

    public static Field[] toFields(FieldItem[] fieldItems) {
        Field[] res = new Field[fieldItems.length];
        for (int i=0; i<fieldItems.length; i++) {
            if (fieldItems[i] == null) {
                res[i] = null;
            } else {
                res[i] = fieldItems[i].getActualField();
            }
        }
        return res;
    }

    /**
     * Default contructor to create a new field item object.<p>
     *
     * @param delegate the <code>java.lang.reflect.Field</code> actual
     * meta item */

    public FieldItem(Field delegate, ClassItem parent) 
        throws InvalidDelegateException 
    {
        super(delegate,parent);
        name = delegate.getName();
    }

    public FieldItem(ClassItem parent) {
        super(parent);
    }

    /**
     * Creates a calculated FieldItem
     * @param name name of the field
     * @param getter the getter method of the field
     */
    public FieldItem(String name, MethodItem getter, ClassItem parent) {
        super(parent);
        isCalculated = true;
        this.name = name;
        addAccessingMethod(getter);
        setGetter(getter);
        getter.addAccessedField(this);
        getter.setReturnedField(this);
    }

    /**
     * Creates a FieldItem with specific getter and setter
     * @param name name of the field
     * @param getter the getter method of the field
     * @param setter the setter method of the field
     */
    public FieldItem(String name, MethodItem getter, MethodItem setter, 
                     ClassItem parent) 
    {
        super(parent);
        this.name = name;
        setGetter(getter);
        setSetter(setter);
    }

    /**
     * Creates an expression FieldItem
     * @param expression expression of the field
     */
    public FieldItem(String expression, List path, ClassItem parent) {
        super(parent);
        isCalculated = true;
        isExpression = true;
        this.name = expression;
        this.path = path;
        type = getPathTop().getType();
    }

    boolean isCalculated = false;
    boolean isExpression = false;
    String name;
    Class type;

    // Used if isExpression==true FieldItem[]
    List path;

    public FieldItem getPathTop() {
        return (FieldItem)path.get(path.size()-1);
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
                if (parent.hasField(getName()))  {
                    value = parent.getField(this.name).getAttribute(name);
                }
            }
        }
        if (isExpression && value==null) {
            return ((FieldItem)path.get(path.size()-1)).getAttribute(name);
        }
        return value;
    }


    MethodItem[] accessingMethods;

    /**
     * Get the methods that access this field for reading.<p>
     *
     * @return value of accessingMethods.
     */
    public final MethodItem[] getAccessingMethods() {
        ((ClassItem)parent).buildFieldInfo();
        return accessingMethods;
    }
   
    public final boolean hasAccessingMethods() {
        ((ClassItem)parent).buildFieldInfo();
        return accessingMethods!=null && accessingMethods.length>0;
    }

    /**
     * Set the methods that access this field for reading.<p>
     *
     * @param accessingMethods value to assign to accessingMethods.
     */

    public final void setAccessingMethods(MethodItem[] accessingMethods) {
        this.accessingMethods = accessingMethods;
    }

    /**
     * Add a new accessing method for this field.<p>
     *
     * @param accessingMethod the method to add
     */

    public final void addAccessingMethod(MethodItem accessingMethod) {
        if (accessingMethods == null) {
            accessingMethods = new MethodItem[] { accessingMethod };
        } else {
            MethodItem[] tmp = new MethodItem[accessingMethods.length + 1];
            System.arraycopy(accessingMethods, 0, tmp, 0, accessingMethods.length);
            tmp[accessingMethods.length] = accessingMethod;
            accessingMethods = tmp;
        }
    }
   
    MethodItem[] writingMethods;
   
    /**
     * Get the methods that access this field for writing.<p>
     *
     * @return value of writingMethods.
     */
    public final MethodItem[] getWritingMethods() {
        ((ClassItem)parent).buildFieldInfo();
        return writingMethods;
    }
    public final boolean hasWritingMethods() {
        ((ClassItem)parent).buildFieldInfo();
        return writingMethods!=null && writingMethods.length>0;
    }

    /**
     * Set the methods that access this field for writing.<p>
     *
     * @param writingMethods value to assign to writingMethods.
     */

    public final void setWritingMethods(MethodItem[] writingMethods) {
        this.writingMethods = writingMethods;
    }

    /**
     * Add a new writing method for this field.<p>
     *
     * @param writingMethod the method to add
     */

    public final void addWritingMethod(MethodItem writingMethod) {
        if (writingMethods == null) {
            writingMethods = new MethodItem[] { writingMethod };
        } else {
            MethodItem[] tmp = new MethodItem[writingMethods.length + 1];
            System.arraycopy(writingMethods, 0, tmp, 0, writingMethods.length);
            tmp[writingMethods.length] = writingMethod;
            writingMethods = tmp;
        }
    }
   
    /**
     * Remove accessing and writing methods
     */
    public void clearMethods() {
        if (writingMethods != null) {
            for (int i=0; i<writingMethods.length; i++) {
                writingMethods[i].removeWrittenField(this);
            }
            writingMethods = null;
        }
        if (accessingMethods != null) {
            for (int i=0; i<accessingMethods.length; i++) {
                accessingMethods[i].removeAccessedField(this);
            }
            accessingMethods = null;
        }
    }

    FieldItem[] dependentFields = FieldItem.emptyArray;
    /**
     * @see #getDependentFields()
     */
    public final void addDependentField(FieldItem field) {
        FieldItem[] tmp = new FieldItem[dependentFields.length+1];
        System.arraycopy(dependentFields, 0, tmp, 0, dependentFields.length);
        tmp[dependentFields.length] = field;
        dependentFields = tmp;
        ClassItem superClass = getClassItem().getSuperclass();
        if (superClass!=null) {
            FieldItem superField = superClass.getFieldNoError(getName());
            if (superField!=null)
                superField.addDependentField(field);
        }
    }
    /**
     * Returns an array of calculated fields which depend on the field.
     * @see #addDependentField(FieldItem)
     */
    public final FieldItem[] getDependentFields() {
        return dependentFields;
    }

    /**
     * Get the field represented by this field item.<p>
     *
     * @return the actual field
     */

    public final Field getActualField() {
        return (Field)delegate;
    }

    /**
     * Returns proper substance to invoke methods on for expression
     * fields.
     */
    public Object getSubstance(Object substance) {
        if (isExpression) {
            Iterator it = path.iterator();
            while (it.hasNext() && substance!=null) {
                FieldItem field = (FieldItem)it.next();
                if (!it.hasNext()) {
                    return substance;
                }
                substance = field.getThroughAccessor(substance);
            }
            return null;
        } else {
            return substance;
        }
    }

    /**
     * Returns the substances list for expression fields.
     * 
     * <p>Since expression fields can contain collections, there can be
     * multiple substances related to them.
     * @param substance
     * @return a list (can be empty but never null)
     */    
    public List getSubstances(Object substance) {
        Vector substances = new Vector();
        substances.add(substance);
        if (isExpression) {
            Iterator it = path.iterator();
            while (it.hasNext() && substance!=null) {
                FieldItem field = (FieldItem)it.next();
                if (!it.hasNext()) {
                    break;
                }
                Vector current = substances;
                substances = new Vector(current.size());
                for(Iterator j = current.iterator(); j.hasNext();) {
                    Object o = j.next();
                    if (o!=null) {
                        if (field instanceof CollectionItem) {
                            substances.addAll(
                                ((CollectionItem)field).getActualCollectionThroughAccessor(o));
                        } else {
                            substances.add(field.getThroughAccessor(o));
                        }
                    }
                }
            }
        }
        return substances;
    }

    /**
     * Returns the actual field. In the case of an expression field,
     * this is the last element of the path, otherwise it is the field
     * itself.
     */
    public FieldItem getField() {
        if (isExpression) {
            return getPathTop();
        } else {
            return this;
        }
    }

    /**
     * Get the value of this field item for a given object.<p>
     *
     * @param object the object that supports the field
     * @return the field value in the given object
     * @see #set(Object,Object) 
     */
    public final Object get(Object object) {
        Object ret = null;
        try {
            ret = ((Field)delegate).get(object);
        } catch (Exception e) {
            logger.error("Failed to get value of field "+this+" for "+object+
                         (object!=null?(" ("+object.getClass().getName()+")"):""),
                         e);
        }
        return ret;
    }

    /**
     * Get a field value through accessor if it exists.<p>
     *
     * @param substance the object that supports the field
     */
    public Object getThroughAccessor(Object substance) 
    {
        ((ClassItem)parent).buildFieldInfo();
        if (isExpression) {
            Iterator it = path.iterator();
            while (it.hasNext() && substance!=null) {
                FieldItem field = (FieldItem)it.next();
                substance = field.getThroughAccessor(substance);
            }
            return substance;
        } else {
            Object value = null;
            String name;
            MethodItem getter = getGetter();
            if (getter!=null) {
                //Log.trace("rtti.field",this+": invoking "+accessors[i]);
                return (getter.invoke(substance,ExtArrays.emptyObjectArray));
            } else {
                logger.warn("No accessor found for field " + this);
                return get(substance);
            }
        }
    }

    /**
     * Gets the leaves of an object path.
     *
     * <p>If path is not an expression field, returns
     * <code>getActualCollectionThroughAccessor()</code> if it's a
     * CollectionItem or a CollectionItem containing
     * <code>getThroughAccessor()</code> otherwise. If path is an
     * expression field, getPathLeaves() is called recursively on all
     * the component fields of the expression field.</p> 
     *
     * @param path the path
     * @param root the root object the path will be applied to.
     */
    public static Collection getPathLeaves(FieldItem path, Object root) {
        ((ClassItem)path.parent).buildFieldInfo();

        if (path.isExpression) {
            HashSet currentSet = new HashSet();
            currentSet.add(root);
            Iterator it = path.path.iterator();
            while (it.hasNext()) {
                FieldItem field = (FieldItem)it.next();
                HashSet newSet = new HashSet();
                Iterator j = currentSet.iterator();
                while(j.hasNext()) {
                    Object o = j.next();
                    newSet.addAll(getPathLeaves(field,o));
                }
                currentSet = newSet;
            }
            return currentSet;
        } else {
            if (path instanceof CollectionItem)
                return ((CollectionItem)path).getActualCollectionThroughAccessor(root);
            else {
                ArrayList singleton = new ArrayList(1);
                singleton.add(path.getThroughAccessor(root));
                return singleton;
            }
        }
    }

    /**
     * Sets the value of this field item for a given object.<p>
     *
     * @param object the object whose field must be set
     * @param value the value to set
     * @see #get(Object) 
     * @see #setConvert(Object,Object)
     */
    public final void set(Object object, Object value) 
        throws IllegalAccessException, IllegalArgumentException
    {
        if (value==null && getType().isPrimitive()) {
            logger.error("Cannot set primitive field "+this+" to null", new Exception());
        } else {
            ((Field)delegate).set(object, value);
        }
    }

    /**
     * Sets the value of this field item for a given object. If the
     * value is not assignable to the field, tries to convert it.<p>
     *
     * <p>It can convert floats and doubles to int or long, and
     * anything to String.</p>
     *
     * @param object the object whose field must be set
     * @param value the value to set. Must not be null.
     * @return true if value had to be converted, false otherwise
     *
     * @see #set(Object,Object) */
    public final boolean setConvert(Object object, Object value) 
        throws IllegalAccessException, IllegalArgumentException, 
        InstantiationException, InvocationTargetException, NoSuchMethodException
    {
        try {
            set(object,value);
            return false;
        } catch(IllegalArgumentException e) {
            Object convertedValue = RttiAC.convert(value,getType());
            set(object,convertedValue);
            return true;
        }
    }

    /**
     * Sets the value of this field item by using its setter method if
     * any (else use the <code>set</code> method.
     *
     * @param substance the object to set the field of 
     * @param value the new value of the field
     * @see #set(Object,Object) 
     */
    public final void setThroughWriter(Object substance, Object value) 
        throws IllegalAccessException, IllegalArgumentException
    {
        ((ClassItem)parent).buildFieldInfo();
        if (isExpression) {
            Iterator it = path.iterator();
            while (it.hasNext()) {
                FieldItem field = (FieldItem)it.next();
                if (it.hasNext()) {
                    substance = field.getThroughAccessor(substance);
                } else {
                    field.setThroughWriter(substance,value);
                    return;
                }
            }
        } else {
            logger.debug("setThroughWriter "+substance+"."+getName()+","+value);
            String name;
            if (setter!=null) {
                try {
                    logger.debug(this+": invoking "+setter);
                    setter.invoke(substance,new Object[] { value });
                    return;
                } catch (WrappedThrowableException e) {
                    Throwable target = e.getWrappedThrowable();
                    if (target instanceof IllegalArgumentException)
                        logger.error("setThroughWriter: IllegalArgumentException for "+substance+"."+getName()+
                                  " = "+Strings.hex(value)+"("+value+")");
                    throw e;
                } 
            }
        }

        if (isCalculated()) {
            throw new RuntimeException("Cannot set calculted field "+getLongName());
        }
        logger.warn("No setter found for field "+this);
        set(substance,value);
    }

    /* <em>the</em> setter of the field */
    MethodItem setter;

    public MethodItem getSetter() {
        ((ClassItem)parent).buildFieldInfo();
        if (setter!=null)
            return setter;
        else if (isExpression)
            return getPathTop().getSetter();
        else
            return null;
    }

    public void setSetter(MethodItem setter) {
        if (this.setter!=null) {
            logger.warn("overriding setter "+
                        this.setter.getFullName()+" for field "+
                        this+" with "+setter.getFullName());
        }
        this.setter = setter;
        addWritingMethod(setter);
    }

    /* <em>the</em> getter of the field */
    MethodItem getter;
    /**
     * Returns <em>the</em> getter of the field, if any.
     */
    public MethodItem getGetter() {
        if (!isExpression)
            ((ClassItem)parent).buildFieldInfo();
        return getter;
    }

    public void setGetter(MethodItem getter) {
        ((ClassItem)parent).buildFieldInfo();
        if (this.getter!=null) {
            if (getType().isAssignableFrom(getter.getType())) {
                setType(getter.getType());
            } else {
                logger.warn("overriding getter "+
                            this.getter.getLongName()+
                            " for field "+this+" with "+getter.getLongName());
            }
        }
        if (!isCalculated && getType()!=getter.getType() && 
            getType().isAssignableFrom(getter.getType())) {
            setType(getter.getType());
        }
        this.getter = getter;
        addAccessingMethod(getter);      
    }

    public String getName() {
        return name;
    }

    public Class getType() {
        if (type!=null)
            return type;
        else if (getter!=null)
            return getter.getType();
        else
            return ((Field)delegate).getType();
    }

    public void setType(Class type) {
        logger.info("overriding field type of "+this+
                    " with "+type.getName());
        this.type = type;
    }

    /**
     * Tells if this field item represents a primitive type (that is to
     * say it is of a type that is not a reference towards a Jac
     * object).
     *
     * <p>Allways returns false on collections (use
     * <code>isWrappable</code> to know if the field is wrappable).
     *
     * @return true if primitive
     * @see #isReference() */
 
    public boolean isPrimitive() {
        return !isReference();
    }

    /**
     * Tells if this field item represents a reference type (that is to
     * say it is of a type that is a reference towards a Jac
     * object).
     *
     * <p>Allways returns false on collections (use
     * <code>isWrappable</code> to know if the field is wrappable).
     *
     * @return true if reference
     * @see #isPrimitive()
     * @see #isWrappable(Object) 
     */

    public boolean isReference() {
        return wrappeeClass.isAssignableFrom(getType());
    }

    /**
     * Tells if the field item represents a wrappable type.
     *
     * <p>This method can be used on all kinds of field item including
     * collections (on contrary to <code>isPrimitive</code> and
     * <code>isReference</code>).
     *
     * @return true if wrappable
     * @see #isPrimitive()
     * @see #isReference() */

    public boolean isWrappable(Object substance) {
        Object value = get(substance);
        if (value==null) {
            return wrappeeClass.isAssignableFrom(getType());
        }
        return wrappeeClass.isAssignableFrom(value.getClass());
    }

    /**
     * Tells whether the field is transient or not.
     */
    public boolean isTransient() {
        return isCalculated ? true 
            : Modifier.isTransient(((Member)delegate).getModifiers());
    }
   
    public boolean isFinal() {
        return isCalculated ? false
            : Modifier.isFinal(((Member)delegate).getModifiers());
    }

    public boolean isStatic() {
        return isCalculated ? ( isExpression ? lastField().isStatic() : getter.isStatic() )
            : Modifier.isStatic(((Member)delegate).getModifiers());
    }

    public int getModifiers() {
        if (isCalculated)
            return Modifier.PUBLIC | Modifier.TRANSIENT; 
        else
            return super.getModifiers();
    }

    protected FieldItem lastField() {
        return (FieldItem)path.get(path.size()-1);
    }

    /**
     * Tells whether the field is transient or not.
     */
    public boolean isCalculated() {
        return isCalculated;
    }

    /**
     * Copies this field to an other class.
     * @param parent the class to copy the field to
     */
    public FieldItem clone(ClassItem parent) {
        FieldItem clone = null;
        try {
            if (isCalculated)
                clone = new FieldItem(name,getter,parent);
            else 
                clone = new FieldItem((Field)delegate,parent);
        } catch(Exception e) {
            logger.error("Failed to clone field "+this);
        }
        return clone;
    }

    boolean isAggregation = false;
    public void setAggregation(boolean isAggregation) {
        this.isAggregation = isAggregation;
    }
    public boolean isAggregation() {
        return isAggregation;
    }

    public boolean startsWith(FieldItem field) {
        return this!=field && name.startsWith(field.getName());
    }

    public FieldItem getRelativeField(FieldItem base) {
        if (base instanceof CollectionItem)
            return ((CollectionItem)base).getComponentType().getField(name.substring(base.getName().length()+1));
        else
            return base.getTypeItem().getField(name.substring(base.getName().length()+1));
    }

    FieldItem oppositeRole;
    public FieldItem getOppositeRole() {
        if (oppositeRole!=null)
            return oppositeRole;
        else
            return (FieldItem)getAttribute(RttiAC.OPPOSITE_ROLE);
    }
    public void setOppositeRole(FieldItem oppositeRole) {
        this.oppositeRole = oppositeRole;
        setAttribute(RttiAC.OPPOSITE_ROLE,oppositeRole);
    }

    public static final FieldItem[] emptyArray = new FieldItem[0];
}
