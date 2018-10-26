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

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.AspectComponent;
import org.objectweb.jac.core.Wrappee;
import java.lang.reflect.Method;

/**
 * This class defines the rtti aspect.
 *
 * <p>It allows the programmer to add some runtime type informations on
 * the classes of its applications.
 *
 * @see ClassItem
 * @see MethodItem
 * @see FieldItem
 * @see CollectionItem
 *
 * @author Renaud Pawlak
 * @author Laurent Martelli
 */

public class RttiAC extends AspectComponent implements RttiConf {
    static Logger logger = Logger.getLogger("rtti");

    public static final String OPPOSITE_ROLE = "RttiAC.OPPOSITE_ROLE";
    public static final String FIELD_TYPE = "RttiAC.FIELD_TYPE";
    public static final String DYNAMIC_FIELD_TYPE = "RttiAC.DYNAMIC_FIELD_TYPE";
    public static final String PARAMETER_TYPES = "RttiAC.PARAMETERS_TYPES";
    public static final String CLONED_FIELDS = "RttiAC.CLONED_FIELDS";
    public static final String REPOSITORY_NAME = "RttiAC.REPOSITORY_NAME";
    public static final String REPOSITORY_COLLECTION = "RttiAC.REPOSITORY_COLLECTION";
    public static final String NULL_ALLOWED_PARAMETERS = "RttiAC.NULL_ALOWED_PARAMETERS";
    public static final String NULL_ALLOWED = "RttiAC.NULL_ALLOWED"; // Boolean
    public static final String IS_INDEX = "RttiAC.IS_INDEX"; // Boolean
    public static final String INDEXED_FIELD = "RttiAC.INDEXED_FIELD"; // FieldItem
    public static final String AUTHORIZED_VALUES = "RttiAC.AUTHORIZED_VALUES";
    public static final String FORBIDDEN_VALUES = "RttiAC.FORBIDDEN_VALUES";
    public static final String CONSTRAINTS = "RttiAC.CONSTRAINTS";
    public static final String PARAMETERS_FIELDS = "RttiAC.PARAMETERS_FIELDS"; // FieldItem[]

    public static final String PRIMARY_KEY = "RttiAC.PRIMARY_KEY";

    public void addWrittenFields(AbstractMethodItem method, 
                                 String[] writtenFields) {
        ClassItem cl = method.getClassItem();
        for( int i=0; i<writtenFields.length; i++ ) {
            FieldItem fi = cl.getField(writtenFields[i]);
            method.addWrittenField(fi);
        }
    }

    public void declareCalculatedField(ClassItem cl, String fieldName,
                                       String getterName) 
    {
        MethodItem getter = cl.getMethod(getterName);
        FieldItem calculatedField; 
        if (RttiAC.isCollectionType(getter.getType()))
            calculatedField = new CollectionItem(fieldName,getter,cl);
        else
            calculatedField = new FieldItem(fieldName,getter,cl);
        cl.addField(calculatedField);
        FieldItem[] fields = getter.getAccessedFields();
        for (int i=0;i<fields.length;i++) {
            if (fields[i]!=calculatedField) {
                logger.debug(calculatedField.getLongName()+" depends on "+fields[i]);
                fields[i].addDependentField(calculatedField);
            }
        }
    }

    public void setSetter(FieldItem field, String setterName) {
        MethodItem setter = field.getClassItem().getMethod(setterName);
        setter.setSetField(field);
        field.setSetter(setter);
    }

    public void setGetter(FieldItem field, String getterName) {
        MethodItem getter = field.getClassItem().getMethod(getterName);
        getter.setReturnedField(field);
        field.setGetter(getter);
    }

    public void addDependentField(FieldItem field, String dependentField) {
        field.getClassItem().getField(dependentField).addDependentField(field);
    }

    public void addFieldDependency(FieldItem field, FieldItem dependentField) {
        field.addDependentField(dependentField);
    }

    public void addAdder(CollectionItem collection, String methodName) {
        MethodItem method = collection.getClassItem().getMethod(methodName);
        collection.addAddingMethod(method);
        method.addAddedCollection(collection);
    }

    public void setAdder(CollectionItem collection, String methodName) {
        MethodItem method = collection.getClassItem().getMethod(methodName);
        collection.setAdder(method);
        method.addAddedCollection(collection);
    }

    public void addRemover(CollectionItem collection,String methodName) {
        MethodItem method = collection.getClassItem().getMethod(methodName);
        collection.addRemovingMethod(method);
        method.addRemovedCollection(collection);
    }

    public void setRemover(CollectionItem collection,String methodName) {
        MethodItem method = collection.getClassItem().getMethod(methodName);
        collection.setRemover(method);
        method.addRemovedCollection(collection);
    }

    public void addAccessedFields(MethodItem method, 
                                  String[] accessedFields) {
        ClassItem cl = method.getClassItem();
        for(int i=0; i<accessedFields.length; i++) {
            FieldItem fi = cl.getField(accessedFields[i]);
            method.addAccessedField(fi);
        }
    }

    public void setFieldType(FieldItem field, String type) 
    {
        Object cl = ClassRepository.get().getObject(type);

        if (cl == null)
            throw new RuntimeException("no such type "+type);

        field.setAttribute(FIELD_TYPE, cl);
    }

    public void setDynamicFieldType(FieldItem field, MethodItem method) {
        if (!method.isStatic()) {
            error("Method must be static");
        } if (!(method.getType()==String.class 
                || method.getType()==Object.class 
                || MetaItem.class.isAssignableFrom(method.getType()))) {
            error("Method must return a String, a MetaItem or an Object");
        } else {
            field.setAttribute(DYNAMIC_FIELD_TYPE, method);
        }
    }

    public void setComponentType(CollectionItem collection, String type) {
        collection.setComponentType(currentImports.getClass(type));
    }

    /**
     * Gets the type of a field. May return a ClassItem, a
     * VirtualClassItem or a MethodItem.
     * @param field a field
     */
    public static MetaItem getFieldType(FieldItem field) {
        return (MetaItem)field.getAttribute(FIELD_TYPE);
    }

    /**
     * Gets the type of a field for a given object. May return a
     * ClassItem, a VirtualClassItem.
     * @param field a field 
     * @param substance the object holding the field
     */
    public static MetaItem getFieldType(FieldItem field, Object substance) {
        MethodItem dynType = (MethodItem)field.getAttribute(DYNAMIC_FIELD_TYPE);
        if (dynType!=null) {
            Object type = dynType.invokeStatic(new Object[] {field,substance});
            if (type instanceof String)
                return cr.getVirtualClass((String)type);
            else
                return (MetaItem)type;
        } else {
            return getFieldType(field);
        }
    }

    public void setParametersType(AbstractMethodItem method, 
                                  String[] types) 
    {
        ClassRepository cr = ClassRepository.get();
        MetaItem[] metaItems = new MetaItem[types.length];
        for (int i=0; i<types.length;i++) {
            metaItems[i] = (MetaItem)cr.getObject(types[i]);
        }
        method.setAttribute(PARAMETER_TYPES,metaItems);
    }

    public void newVirtualClass(String className, ClassItem actualType)
    {
        logger.info("newVirtualClass("+className+")");
        ClassRepository.get().register(className,
                                       new VirtualClassItem(className,actualType));
    }

    public void defineRepository(ClassItem type, 
                                 String repositoryName,
                                 CollectionItem repositoryCollection) 
    {
        type.setAttribute(REPOSITORY_NAME, repositoryName);
        type.setAttribute(REPOSITORY_COLLECTION, repositoryCollection);
    }

    public void setClonedFields(String className, String[] fields) {
        ClassRepository.get().getClass(className)
            .setAttribute(CLONED_FIELDS,fields);
    }

    public void whenClone(Wrappee cloned, Wrappee clone) {
      
        ClassItem cli = ClassRepository.get().getClass(cloned.getClass());
        String[] clonedFields = (String[])cli.getAttribute(CLONED_FIELDS);
      
        if( clonedFields != null ) {
            for( int i=0; i<clonedFields.length; i++ ) {
                FieldItem fi = cli.getField( clonedFields[i] );
                logger.debug("cloning field "+clonedFields[i]);
                /*
                  try {
                  fi.set(clone,((Wrappee)fi.get(cloned)).clone());
                  } catch( Exception e ) {}
                */
            }
        }
    }

    public void ignoreFields(String packageExpr) {
        logger.info("ignoreFields"+packageExpr);
        ClassRepository.get().ignoreFields(packageExpr);
    }

    void setItemClass(MetaItem item, String className, ClassItem actualType) {
        MetaItem virtualClass;
        ClassRepository cr = ClassRepository.get();
        try {
            virtualClass = cr.getVirtualClass(className);
        } catch (NoSuchClassException e) {
            virtualClass = new VirtualClassItem(className,actualType);
            cr.register(className,virtualClass);
        }
        item.setItemClass(virtualClass);
    }

    public void setClass(MemberItem member, String className) {
        setItemClass(member,className,member.getTypeItem());
    }

    public void setClass(ClassItem cli, String className) {
        setItemClass(cli,className,cli);
    }

    /*
    public void introduce(ClassItem target,ClassItem roleType,
                          String memeberType,String memberName) {
    }
    */

    public void setParametersFields(AbstractMethodItem method, 
                                    FieldItem[] fields) {
        method.setAttribute(PARAMETERS_FIELDS, fields);
    }

    public void setNullAllowed(FieldItem field) {
        setNullAllowed(field,true);
    }

    public void setNullAllowed(FieldItem field, boolean allowed) {
        field.setAttribute(NULL_ALLOWED, allowed ? Boolean.TRUE : Boolean.FALSE);
        logger.info("setNullAllowed("+field.getName()+")");
    }

    public static boolean isNullAllowed(FieldItem field) {
        Boolean result = (Boolean) field.getAttribute(NULL_ALLOWED);
        if (result == null)
            return false;
        return result.booleanValue();
    }

    public void setNullAllowedParameters(AbstractMethodItem method,
                                         boolean[] nulls) {
        method.setAttribute(NULL_ALLOWED_PARAMETERS, nulls);
    }

    public static boolean isNullAllowedParameter(AbstractMethodItem method,
                                                 int i) {
        //System.out.println("is null allowed "+method+" "+i);
        boolean[] nulls=(boolean[])method.getAttribute(NULL_ALLOWED_PARAMETERS);
        if(nulls==null) return false;
        //System.out.println("=> "+nulls[i]);
        return nulls[i];
    }

    public void setAggregation(FieldItem field, boolean isAggregation) {
        field.setAggregation(isAggregation);
    }

    public void setIndexedField(CollectionItem collection, 
                                FieldItem indexedField) {
        setIsIndex(collection,true);
        collection.setAttribute(INDEXED_FIELD, indexedField);
    }

    public void setIsIndex(CollectionItem collection, 
                           boolean isIndex) {
        collection.setAttribute(IS_INDEX, isIndex?Boolean.TRUE:Boolean.FALSE);
    }

    public static FieldItem getIndexFied(CollectionItem collection) {
        return (FieldItem)collection.getAttribute(INDEXED_FIELD);
    }

    public static boolean isIndex(CollectionItem collection) {
        Boolean result = (Boolean) collection.getAttribute(IS_INDEX);
        if (result == null)
            return false;
        return result.booleanValue();      
    }

    public String[] getDefaultConfigs() {
        return new String[] {"org/objectweb/jac/core/rtti/rtti.acc",
                             "org/objectweb/jac/aspects/user/rtti.acc"};
    }

    public void definePrimaryKey(CollectionItem collection,
                                 String[] fields) {
        collection.setAttribute(PRIMARY_KEY, fields);
    }

    /**
     * Tells wether a given type represents a collection
     */
    public static boolean isCollectionType(Class type) {
        return Collection.class.isAssignableFrom(type) || 
            Map.class.isAssignableFrom(type) || 
            (type.isArray() && type.getComponentType()!=byte.class);
    }

    static Hashtable allowedCasts = new Hashtable();
    public void addAllowedCast(ClassItem src, ClassItem dest) {
        Set casts = (Set)allowedCasts.get(src);
        if (casts == null) {
            casts = new HashSet();
            allowedCasts.put(src,casts);
        }
        casts.add(dest);
    }

    public static boolean isCastAllowed(ClassItem src, ClassItem dest) {
        Set casts = (Set)allowedCasts.get(src);
        return casts!=null && casts.contains(dest);
    }

    public static boolean isCastAllowed(Class src, Class dest) {
        ClassRepository cr = ClassRepository.get();
        return isCastAllowed(cr.getClass(src),cr.getClass(dest));
    }

	HashSet classesWithAssociations = new HashSet();
    public Set getClassesWithAssociations() {
        return classesWithAssociations;
    }

    public void setOppositeRole(FieldItem field, FieldItem oppositeRole) {
        field.setOppositeRole(oppositeRole);
		classesWithAssociations.add(field.getClassItem());
    }
    
    public void declareAssociation(FieldItem roleA, FieldItem roleB) {
        roleA.setOppositeRole(roleB);
        roleB.setOppositeRole(roleA);
		classesWithAssociations.add(roleA.getClassItem());
		classesWithAssociations.add(roleB.getClassItem());
    }

    /**
     * Tries to convert an object into a given type 
     *
     * <p>If type is
     * String, toString() is called on value. If type Integer or Long
     * (or int or long), and the value is numeric type
     * (float,Float,double or Double) an Integer or Long is
     * returned. Otherwise, isCastAllowed() is called, and if it
     * returns true, we try to invoke a constructor take value as a
     * parameter.</p>
     *
     * @param value the value to convert
     * @param type the type to convert the value into
     * @return an instance of type built from value, or value 
     * @see #isCastAllowed(Class,Class)
     */
    public static Object convert(Object value, Class type) 
        throws InstantiationException, IllegalAccessException, 
               InvocationTargetException, java.lang.NoSuchMethodException 
    {
        Class valueType = value.getClass();
        if ((type==int.class || type==Integer.class) 
            && (valueType==float.class || valueType==Float.class 
                || valueType==double.class || valueType==Double.class)) {
            return new Integer(((Number)value).intValue());
        } else if ((type==long.class || type==Long.class) 
                   && (valueType==float.class || valueType==Float.class 
                       || valueType==double.class || valueType==Double.class)) {
            return new Long(((Number)value).longValue());
        } else if (type==String.class) {
            return value.toString();
        } else {
            if (RttiAC.isCastAllowed(valueType,type)) {
                return 
                    type.getConstructor(new Class[] {valueType})
                    .newInstance(new Object[] {value});
            }
        }
        return value;
    }

    public void addMixinMethod(ClassItem cli, MethodItem method) 
        throws InvalidDelegateException 
    {
        cli.addMethod(new MixinMethodItem((Method)method.getDelegate(),cli));
    }

}
