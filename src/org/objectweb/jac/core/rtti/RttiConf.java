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

/**
 * This class defines the rtti aspect.
 *
 * <p>It allows the programmer to add some runtime type information on
 * the classes of its applications.
 *
 * <p>Some configuration methods are useless since the type
 * information is retrieved through bytecode analysis. They are kept
 * in the interface in case of.
 *
 * @see ClassItem
 * @see MethodItem
 * @see FieldItem
 * @see CollectionItem
 *
 * @author Renaud Pawlak
 * @author Laurent Martelli
 */

public interface RttiConf {

    /**
     * Introduces a new member (field or method) into a given
     * class. <b>Does not work yet.</b>
     *
     * <p>The target class does not declare the member but is wrapped by
     * a wrapper that contains a member that should be considered as a
     * base-object member.
     *
     * <p>Once introduced, the member will behave like a regular member
     * if accessed or setted through the RTTI.
     *
     * @param target the target class where to add the memeber
     * @param roleType the type of the role wrapper that actually
     * contains the member
     * @param memberType the member's type
     * @param memberName the member's name 
     */
    /*
    void introduce(ClassItem target,ClassItem roleType,
                   String memberType,String memberName);
    */

    /**
     * Adds some written fields to a given method.
     *
     * <p>This configuration method must be used when a method changes
     * some field values and does not follow the naming conventions (it
     * is not a setter or an adder for instance).
     *
     * @param method the method
     * @param writtenFields some new fields the method writes 
     */ 
    void addWrittenFields(AbstractMethodItem method, String[] writtenFields);

    /**
     * Adds some accessed fields to a given method.
     *
     * <p>This configuration method must be used when a method reads
     * some field values and does not follow the naming conventions (it
     * is not a getter for instance).
     *
     * @param method the method
     * @param accessedFields some new fields the method reads 
     */ 
    void addAccessedFields(MethodItem method, String[] accessedFields);

    /**
     * Declare a calculated field, which is a field with only a getter
     * and no actual field.
     *
     * @param cl the class item of te calculated field
     * @param fieldName the name of the calculated field
     * @param getterName the name of the getter method
     * @see #addDependentField(FieldItem,String)
     */
    void declareCalculatedField(ClassItem cl, String fieldName,
                                String getterName);

    /**
     * Sets the setter of a field
     * @param field the field
     * @param setterName name of the setter method
     */
    void setSetter(FieldItem field, String setterName);

    /**
     * Sets the getter of a field
     * @param field the field
     * @param getterName name of the getter method
     */
    void setGetter(FieldItem field, String getterName);

    /**
     * Declares a field dependency. It will cause the field to be
     * refreshed when the dependent field's value changes.
     *
     * @param field the field to refresh
     * @param dependentField the name of the field it depends on. It
     * must be in the same class as field.
     * @see #declareCalculatedField(ClassItem,String,String)
     */
    void addDependentField(FieldItem field, String dependentField);

    /**
     * Tells that when field changes, dependentField changes too.
     */
    void addFieldDependency(FieldItem field, FieldItem dependentField);

    /**
     * Adds an adding method for a collection 
     *
     * @param collection the collection's name
     * @param method the name of the adding method
     */
    void addAdder(CollectionItem collection,String method);

    /**
     * Sets <em>the</em> adder of a collection.
     * @param collection the collection
     * @param method the name of the adder method
     */
    void setAdder(CollectionItem collection, String method);

    /**
     * Adds a removing method for a collection 
     * @param collection the collection
     * @param method the name of the removing method
     */
    void addRemover(CollectionItem collection, String method);

    /**
     * Sets <em>the</em> remover of a collection.
     *
     * @param collection the collection
     * @param method the name of the remover method
     */
    void setRemover(CollectionItem collection, String method);

    /**
     * Declares a field to be of a given type
     * 
     * @param field the field
     * @param type the type
     *
     * @see #setDynamicFieldType(FieldItem,MethodItem)
     */
    void setFieldType(FieldItem field, String type);

    /**
     * Use a method to dynamically determine the type of a field
     * 
     * @param field the field
     * @param method a static method taking as arguments a FieldItem
     * and an Object (holder of the field), and return a ClassItem or
     * a VirtualClassItem or a String.
     *
     * @see #setFieldType(FieldItem,String)
     */
    void setDynamicFieldType(FieldItem field, MethodItem method);

    /**
     * Sets the component type of a collection, i.e. the type of
     * objects it contains.
     *
     * @param collection the collection 
     * @param type the component type of the collection
     */
    void setComponentType(CollectionItem collection, String type);

    /**
     * Declare a method's parameters to be of a given type
     * 
     * @param method the method
     * @param types the types of each parameteropf the method
     */
    void setParametersType(AbstractMethodItem method, String[] types) ;

    /**
     * Create a new virtual class. Virtual classes allow you extend and
     * refine the types used by the application, so that aspects can
     * behave differently. 
     *
     * @param className name of the new class
     * @param actualType the actual primitive type that is extended
     */
    void newVirtualClass(String className, ClassItem actualType);

    /**
     * Declare a repository to get instances of a class from, instead
     * of fetching all instances of the class, when
     * ObjectRepository.getObjects(ClassItem) is called.
     *
     * @param type the type of objects to add in the repository
     * @param repositoryName the name of the object holding the repository
     * @param repositoryCollection the collection to get the objects
     * from. Any expression field can be used.
     *
     * @see org.objectweb.jac.core.ObjectRepository#getObjects(ClassItem) 
     */
    void defineRepository(ClassItem type, 
                          String repositoryName,
                          CollectionItem repositoryCollection); 

    /**
     * This configuration method tells that the fields (references or
     * collections) must be cloned when the class is cloned.
     *
     * @param className the class name
     * @param fields the names of the fields that are cloned 
     */
    void setClonedFields(String className, String[] fields);

    /**
     * Sets a user defined class on a class. This can be used by
     * aspects user-defined configuration. 
     *
     * @param cli the class to redefine
     * @param className the new class name
     */
    void setClass(ClassItem cli, String className);

    /**
     * Sets a user defined class on a class' member. This can be used by
     * aspects user-defined configuration. 
     *
     * @param member the member whose type to redefine
     * @param className the new class name
     */
    void setClass(MemberItem member, String className);

    /**
     * Specifies that the parameters of a method will be assigned to a
     * given field. 
     *
     * <p>Other aspects (such as the GUI aspect) may use this
     * information to provide better default behaviour for the
     * paramters.</p> 
     *
     * @param method the of the method
     * @param fields an array of field items, one per parameter of the
     * method. Elements of this array may be null if some parameters
     * are not assigned to any field.
     */
    void setParametersFields(AbstractMethodItem method, FieldItem[] fields);

    /**
     * This configuration method tells that the field can be set to null
     * (forbidden by default)
     * 
     * @param field the field
     *
     * @see #setNullAllowed(FieldItem,boolean)
     */
    void setNullAllowed(FieldItem field);

    /**
     * This configuration method tells wether the field can be set to
     * null or not. (forbidden by default)
     * 
     * @param field the field
     * @param allowed wether to allow null values
     *
     * @see #setNullAllowed(FieldItem) 
     */
    void setNullAllowed(FieldItem field, boolean allowed);

    /**
     * Tells if JAC object-typed (references) arguments of a method can take
     * null value while the method's invocation or if they should be
     * choosen in existing instances list.
     *
     * @param method the method
     * @param nulls a flags array that tells for each parameter whether
     * it can be null (true) or not (false). It has no effect if the
     * parameter is not a JAC object (a reference) 
     */
    void setNullAllowedParameters(AbstractMethodItem method,
                                  boolean[] nulls);

    /**
     * Specify wether a Map implementing a collection is a mere index
     * for the collection (this is not the default). In this case,
     * CollectionItem.getActualCollection() returns the values
     * contained in the hashtable, otherwise the (key,values) entries
     * are returned.
     *
     * @param collection the collection
     * @param isIndex wether the map is an index
     *
     * @see #setIndexedField(CollectionItem,FieldItem)
     * @see CollectionItem#getActualCollection(Object)
     * @see CollectionItem#getActualCollectionThroughAccessor(Object)
     */
    void setIsIndex(CollectionItem collection, boolean isIndex);

    /**
     * Tells that a Map implementing a collection indexes a field of
     * the contained objects.
     *
     * @param collection the collection
     * @param indexedField the field which is indexed by the map
     *
     * @see #setIsIndex(CollectionItem,boolean)
     * @see CollectionItem#getActualCollection(Object)
     * @see CollectionItem#getActualCollectionThroughAccessor(Object) 
     */
    void setIndexedField(CollectionItem collection, FieldItem indexedField);

    /**
     * Defines primary keys for a collection.
     *
     * <p>It is used to check for double entries in the collection by
     * checking the precised fields (it is the same as primary keys in
     * a database).</p>
     */
    void definePrimaryKey(CollectionItem collection, String[] fields);

    /**
     * Tells wether a relation is an aggregation or not. By default,
     * relations are not aggregations.
     *
     * @param field the relation
     * @param isAggregation wether the relation is an aggregations or not.
     */
    void setAggregation(FieldItem field, boolean isAggregation);

    /**
     * <p>Specify that a type can be safely casted into another type.</p>
     *
     * <p>It can be useful it you changed the type of a field to a
     * subclass of the original type that only adds new methods, and a
     * persistence aspect complains that it cannot load this field
     * anymore. dest should have constructor which takes a value of
     * type src as the only argument.</p>
     *
     * @param src type of the value to be casted
     * @param dest type the value should be casted to 
     */
    void addAllowedCast(ClassItem src, ClassItem dest);

    /**
     * Sets the opposite role of a reference or collection field
     *
     * @param field the field whose opposite role to set
     * @param oppositeRole the opposite role of the fieldb
     *
     * @see #declareAssociation(FieldItem,FieldItem)
     */
    void setOppositeRole(FieldItem field, FieldItem oppositeRole);

    /**
     * Declares an association made of two roles.
     *
     * <p>When declared, the relations that constitutes the
     * association's roles are tagged in the RTTI by the
     * <code>RttiAC.OPPOSITE_RELATION</code> attribute (then they can
     * be interpreted by other aspects such as Integrity, GUI or
     * Persistence).
     *
     * <p>For instance, if you have a Customer class and an Order
     * class:</p>
     *
     * <pre>
     *    ,----------. 1    n ,-------.
     *    | Customer |--------| Order |
     *    `----------'        `-------'
     * </pre>
     *
     * @param roleA the starting role (e.g. Customer.orders)
     * @param roleB the ending role (e.g. Order.customer)
     */
    void declareAssociation(FieldItem roleA, FieldItem roleB);

    /**
     * Adds a mixin method to a class.
     *
     * <p>A mixin method of class is a method which is not defined in
     * the code of that class, but which will be made available on
     * that class's ClassItem.</p>
     *
     * @param cli a class
     * @param method a static method whose 1st argument must be cli.
     *
     * @see MixinMethodItem#invoke(Object,Object[])
     */
    void addMixinMethod(ClassItem cli, MethodItem method) throws InvalidDelegateException;
}
