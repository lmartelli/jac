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

import gnu.regexp.RE;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import org.apache.log4j.Logger;
import java.lang.reflect.Constructor;

/**
 * This class defines a meta item that corresponds to the
 * <code>java.lang.reflect.Class</code> meta element.<p>
 *
 * @author Renaud Pawlak
 * @author Laurent Martelli
 */

public class ClassItem extends MetaItemDelegate {

    static Logger logger = Logger.getLogger("rtti.class");

    static Class wrappeeClass = ClassRepository.wrappeeClass;

    /**
     * Default contructor to create a new class item object.<p>
     *
     * @param delegate the <code>java.lang.reflect.Class</code> actual
     * meta item 
     */
    public ClassItem(Class delegate) throws InvalidDelegateException {
        super(delegate);
        Class superClass = delegate.getSuperclass();
        if (superClass!=Object.class && superClass!=null) {
            try {
                setSuperClass(ClassRepository.get().getClass(superClass));
            } catch(Exception e) {
                logger.error("ClassItem("+delegate.getName()+"): "+e);
            }
        }
    }

    private int collsCount = 0;
    private int refsCount = 0;
    private int primsCount = 0;
    private int methodsCount = 0;
    private int constructorsCount = 0;

    /**
     * Stores the fields of the class.<p> */

    protected Hashtable fields = new Hashtable();

    /**
     * Stores the methods of the class. */

    protected Hashtable methods = new Hashtable();

    boolean rttiBuilt = false;

    protected void buildFieldInfo() {
        if (!rttiBuilt) {
            rttiBuilt = true;
            ClassRepository.get().buildDefaultFieldRTTI(this);
        }
    }

    /**
     * Gets a field from its name. Throw an exception if the field does
     * not exist.<p>
     *
     * @param name the field name. If name contains a dot, it is
     * considered an expression field and is automatically created if
     * it does not exist yet.
     * @return a <code>FieldItem</code> instance 
     * @see #getFieldNoError(String) 
     */
    public FieldItem getField(String name) {
        buildFieldInfo();
        FieldItem ret = getFieldNoError(name);
        if (ret==null) {
            throw new NoSuchFieldException(this,name);
        }
        return ret;
    }

    /**
     * Gets a field from its name. Don't throw an exception if the
     * field does not exist.<p>
     *
     * @param name the field name
     * @return a <code>FieldItem</code> instance, or null if no such
     * field exists in the class 
     * @see #getField(String)
     */
    public FieldItem getFieldNoError(String name) {
        buildFieldInfo();
        FieldItem field = (FieldItem)fields.get(name);
        if (field==null) {
            if (name.indexOf('.')!=-1) {
                try {
                    List path = parseExpression(name);
                    if (path.get(path.size()-1) instanceof CollectionItem) {
                        field = new CollectionItem(name,path,this);
                    } else {
                        field = new FieldItem(name,path,this);
                    }
                    addField(field);
                } catch (Exception e) {
                    field = null;
                    logger.error("Expression field "+this.getName()+"."+name+
                                 " instanciation failed: "+e);
                    e.printStackTrace();
                }
            }
        }
        if (field==null && superClass!=null) {
            field = superClass.getFieldNoError(name);
            if (field!=null) {
                field = field.clone(this);
                addField(field);
            }
        }
        return field;
    }

    /**
     * Parse the expression and computes its type.
     * @param expression the expression (<field>.<field>.<field>...)
     */
    public List parseExpression(String expression) {
        Vector path = new Vector();
        String current = expression;
        ClassItem curClass = this;
        int dot = current.indexOf('.');
        FieldItem field = null;
        while (dot!=-1) {
            String fieldName = current.substring(0,dot);
            field = curClass.getField(fieldName);
            path.add(field);
            if (field instanceof CollectionItem)
                curClass = ((CollectionItem)field).getComponentType();
            else
                curClass = field.getTypeItem();
            current = current.substring(dot+1);
            dot = current.indexOf('.');
        }

        field = curClass.getField(current);
        path.add(field);
      
        /*
          if (field!=null) {
          type = field.getType();
          setter = field.getSetter();
          }
        */

        return path;
    }

    /**
     * Gets a collection from its name.<p>
     *
     * @param name the collection name
     * @return a <code>CollectionItem</code> instance
     */

    public CollectionItem getCollection(String name) {
        buildFieldInfo();
        return (CollectionItem) fields.get(name);
    }

    /**
     * Gets all the fields for this class item.<p>
     *
     * @return an array of field items
     */

    public FieldItem[] getFields() {
        buildFieldInfo();
        Collection c = fields.values();
        FieldItem[] res = new FieldItem[c.size()];
        Iterator it = c.iterator();
        int i = 0;
        while (it.hasNext()) {
            res[i] = (FieldItem) it.next();
            i++;
        }
        return res;
    }

    /**
     * Gets all the fields for this class item
     *
     * @return the fields as a collection
     */

    public Collection getAllFields() {
        buildFieldInfo();
        return fields.values();
    }

    /**
     * Gets all the current class item's fields that are tagged or not
     * tagged with the given attribute.<p>
     *
     * @param attribute the attribute
     * @param not if true, returns fields not tagged with attribute
     *
     * @return a collection of field items 
     */
    public Collection getTaggedFields(String attribute, boolean not) {
        buildFieldInfo();
        Collection c = fields.values();
        Iterator it = c.iterator();
        Vector result = new Vector();
        int i = 0;
        while (it.hasNext()) {
            FieldItem field = (FieldItem) it.next();
            if (field.getAttribute(attribute)!=null ^ not) {
                result.add(field);
            }
        }
        return result;
    }

    /**
     * @param expression something like [!](static|transient|private|public|protected)
     */
    public Collection filterFields(String expression) { 
        logger.debug(this+".filterFields "+expression);
        String keyword;
        boolean not = false;
        if (expression.charAt(0)=='!') {
            not = true;
            expression = expression.substring(1,expression.length());
        }
        int filter = 0;
        if (expression.equals("static")) 
            filter = Modifier.STATIC;
        else if (expression.equals("public")) 
            filter = Modifier.PUBLIC;
        else if (expression.equals("transient")) 
            filter = Modifier.TRANSIENT;
        else if (expression.equals("private")) 
            filter = Modifier.PRIVATE;
        else if (expression.equals("protected")) 
            filter = Modifier.PROTECTED;
        Vector result = new Vector();      
        Iterator it = fields.values().iterator();
        while (it.hasNext()) {
            FieldItem field = (FieldItem) it.next();
            if (((field.getModifiers() & filter) != 0) ^ not) {
                result.add(field);
            }
        }
        logger.debug("    -> "+result);
        return result;
    }

    public Collection getTaggedMethods(String attribute, boolean not) {
        Collection c = getAllMethods();
        Iterator it = c.iterator();
        Vector result = new Vector();
        int i = 0;
        while (it.hasNext()) {
            AbstractMethodItem method = (AbstractMethodItem) it.next();
            if (method.getAttribute(attribute)!=null ^ not) {
                result.add(method);
            }
        }
        return result;
    }

    public Collection getTaggedMembers(String attribute, boolean not) {
        Collection result = getTaggedMethods(attribute,not);
        result.addAll(getTaggedFields(attribute,not));
        return result;
    }

    /**
     * Returns the members that are tagged by a given attribute that
     * has a given value.
     *
     * @param attribute the attribute id
     * @param value the value of the attribute (must not be null!!) */

    public Collection getTaggedMembers(String attribute, Object value) {
        Collection result = getAllMembers();
        Collection result2 = new Vector();
        Iterator it = result.iterator();
        while (it.hasNext()) {
            MemberItem member = (MemberItem)it.next();         
            if (member.getAttribute(attribute)!=null
                && value.equals(member.getAttribute(attribute)))
            {
                result2.add(member);
            }
        }
        return result2;
    }

    /**
     * Returns the member (method or field) of this class with the
     * specified name.
     * @param name the name of the member that you want.
     * @return the member with the specified name
     * @see #getMembers(String[]) */
    public MemberItem getMember(String name) {
        MemberItem result = null;
        try {
            if (name.indexOf('(')!=-1)
                result = getAbstractMethod(name);
            else
                result = getField(name);
        } catch(NoSuchFieldException e) {
            if (result==null) {
                try {
                    result = getAbstractMethod(name);
                } catch (NoSuchMethodException e2) {
                    throw new NoSuchMemberException(this,name);
                }
            }
        }
        return result;
    }

    /**
     * Returns a MemberItem array.
     *
     * @param names the names of the members
     * @return a MemberItem array members such as
     * members[i].getName().equals(names[i]). If no member with a given
     * name is found, it is ignored (but a warning is issued).
     * @see #getMember(String) 
     */
    public MemberItem[] getMembers(String[] names) {
        MemberItem[] tmp = new MemberItem[names.length];
        int j = 0;
        for(int i=0; i<names.length; i++) {
            try {
                tmp[j] = getMember(names[i]);
                j++;
            } catch (NoSuchMemberException e) {
                logger.warn(e);
            }
        }
        MemberItem[] members = new MemberItem[j];
        System.arraycopy(tmp,0,members,0,j);
        return members;
    }

    public Collection getAllMembers() {
        Collection result = getAllMethods();
        result.addAll(getAllFields());
        return result;
    }

    /**
     * Returns a FieldItem array.
     *
     * @param names the names of the members
     * @return a FieldItem array fields such as
     * fields[i].getName().equals(names[i]). If no field with a given
     * name is found, it is ignored (but a warning is issued).
     * @see #getField(String) */
    public FieldItem[] getFields(String[] names) {
        FieldItem[] tmp = new FieldItem[names.length];
        int j=0;
        for(int i=0;i<names.length;i++) {
            try {
                tmp[j] = getField(names[i]);
                j++;
            } catch (NoSuchFieldException e) {
                logger.warn(e);
            }
        }
        FieldItem[] fields = new FieldItem[j];
        System.arraycopy(tmp,0,fields,0,j);
        return fields;
    }


    /**
     * Returns a MethodItem array.
     *
     * @param names the names of the members
     * @return a MethodItem array methods such as
     * methods[i].getName().equals(names[i]). If no method with a given
     * name is found, it is ignored (but a warning is issued).
     * @see #getMethod(String) 
     */
    public MethodItem[] getMethods(String[] names) {
        MethodItem[] tmp = new MethodItem[names.length];
        int j=0;
        for(int i=0;i<names.length;i++) {
            try {
                tmp[j] = getMethod(names[i]);
                j++;
            } catch (NoSuchMethodException e) {
                logger.warn(e);
            }
        }
        MethodItem[] methods = new MethodItem[j];
        System.arraycopy(tmp,0,methods,0,j);
        return methods;
    }

    /**
     * Gets all the primitive fields for this class item.<p>
     *
     * @return an array of field items
     */

    public FieldItem[] getPrimitiveFields() {
        buildFieldInfo();
        Collection c = fields.values();
        FieldItem[] res = new FieldItem[primsCount];
        Iterator it = c.iterator();
        int i = 0;
        while ( it.hasNext() ) {
            FieldItem field = (FieldItem) it.next();
            if (field.isPrimitive()) {
                res[i] = field;
                i++;
            }
        }
        return res;
    }

    /**
     * Gets all the references for this class item.<p>
     *
     * @return an array of field items
     */

    public FieldItem[] getReferences() {
        buildFieldInfo();
        Collection c = fields.values();
        FieldItem[] res = new FieldItem[refsCount];
        Iterator it = c.iterator();
        int i = 0;
        while ( it.hasNext() ) {
            FieldItem field = (FieldItem) it.next();
            if (field.isReference()) {
                res[i] = field;
                i++;
            }
        }
        return res;
    }

    /**
     * Gets all the references and collections that match the
     * expression for this class item.<p>
     *
     * @return a vector of field items */

    public Collection getMatchingRelations(String expr) {
        buildFieldInfo();
        Vector res = new Vector();
        Collection c = fields.values();
        Iterator it = c.iterator();
        RE re;
        try {
            re = new RE(expr);
        } catch (Exception e) {
            logger.error("getMatchingRelations "+expr,e);
            return null;
        }
        while (it.hasNext()) {
            FieldItem field = (FieldItem) it.next();
            if (field.isReference() || (field instanceof CollectionItem)) {
                if (re.isMatch(field.getName())) {
                    res.add( field );
                }
            }
        }
        return res;
    }

    /**
     * Gets all the collections for this class item.<p>
     *
     * @return an array of field items
     */

    public CollectionItem[] getCollections() {
        buildFieldInfo();
        Collection c = fields.values();
        CollectionItem[] res = new CollectionItem[collsCount];
        Iterator it = c.iterator();
        int i = 0;
        while ( it.hasNext() ) {
            FieldItem field = (FieldItem) it.next();
            if (field instanceof CollectionItem) {
                res[i] = (CollectionItem)field;
                i++;
            }
        }
        return res;
    }

    /**
     * Add a field item to this class item. The parent of the field is
     * set to this.<p>
     *
     * @param field the new field 
     */ 
    void addField(FieldItem field) {
        buildFieldInfo();
        boolean override = fields.containsKey(field.getName());
        if (override) {
            logger.warn("Overriding field "+fields.get(field.getName())+
                        " with "+field);
        }
        try {
            field.setParent(this);
        } catch(Exception e) {
            logger.error("addField "+field.getName(),e);
            return;
        }
        fields.put(field.getName(), field);
        if (field instanceof CollectionItem && !override) {
            collsCount++;
        } else if (field.isReference() && !override) {
            refsCount++;
        } else if (!override) {
            primsCount++;
        }  
    }
   
    /**
     * Gets a set of homonym methods (including constructors) from
     * their names.<p>
     *
     * @param name the name of the methods
     * @return a <code>MethodItem</code> instance
     */
    public AbstractMethodItem[] getAbstractMethods(String name) 
        throws NoSuchMethodException
    {
        if (name.startsWith("<init>"))
            name = getShortName()+name.substring(6);
        //Log.trace("rtti.method","getAbstractMethods("+name+")");
        AbstractMethodItem[] res=null;
        if (name.endsWith(")")) {
            String name2 = name.substring(0,name.indexOf("("));
            //Log.trace("rtti.method","name2 : "+name2);
            AbstractMethodItem[] meths = 
                (AbstractMethodItem[])methods.get(name2);
            if (meths!=null) {
                for (int i=0; i<meths.length; i++) {
                    //Log.trace("rtti.method","trying "+meths[i].getFullName());
                    if (meths[i].getFullName().endsWith(name)) {
                        res = new AbstractMethodItem[] {meths[i]};
                    }
                }
            }
        } else {
            res = (AbstractMethodItem[])methods.get(name);
        }
        if (res == null) {
            throw new NoSuchMethodException(
                "ClassItem.getAbstractMethods: no such method "+name+" in "+this);
        }
        return res;
    }

    Hashtable methodCache = new Hashtable();

    /**
     * Gets an abstract method from its name.
     *
     * <p>If this method has homonym(s), then an
     * <code>AmbiguousMethodNameException</code> is thrown.
     *
     * <p>An abstract method can be a static, an instance method or a
     * constructor.
     *
     * @param name the name of the method to search
     * @return the corresponding method if found
     * @see #getMethod(String)
     * @see #getAbstractMethods(String) 
     */
    public AbstractMethodItem getAbstractMethod(String name) 
        throws NoSuchMethodException, AmbiguousMethodNameException
    {
        AbstractMethodItem cachedMethod =
            (AbstractMethodItem)methodCache.get(name);
        if (cachedMethod!=null) {
            return cachedMethod;
        } else {
            AbstractMethodItem[] res = getAbstractMethods(name);      
            if (res.length>1) {
                throw new NoSuchMethodException("Ambiguous method name "+
                                                this+"."+name);
            }
            methodCache.put(name,res[0]);
            return res[0];
        }
    }

    /**
     * Tells wether the class contains a method. All methods of the
     * class are examined (even the private and protected ones wich
     * are not regisered as MethodItem)
     *
     * @param name name of the searched method
     * @param paramTypes types of the parameters of the searched method 
     * @see Class#getDeclaredMethod(String,Class[])
     */
    public boolean hasMethod(String name, Class[] paramTypes) {
        try {
            ((Class)delegate).getDeclaredMethod(name,paramTypes);
            return true;
        } catch(java.lang.NoSuchMethodException e) {
            return false;
        }
    }

    /**
     * Strip the arguments part from a full method name.
     * aMethod(aType) -> aMethod
     * aMethod -> aMethod
     * @param name a method name
     */
    static String stripArgs(String name) {
        int index = name.indexOf("(");
        if (index!=-1)
            return name.substring(0,index);
        else
            return name;
    }

    /**
     * Gets a set of homonym methods (excluding constructors) from
     * their names.<p>
     *
     * @param name the name of the methods
     * @return a <code>MethodItem</code> instance */

    public MethodItem[] getMethods(String name) 
        throws NoSuchMethodException
    {
        MethodItem[] res = null;

        //Log.trace("rtti.method","getMethods("+name+")");
        if (name.endsWith(")")) {
            String name2 = name.substring(0,name.indexOf("("));
            //Log.trace("rtti.method","name2 : "+name2);
            MethodItem[] meths = (MethodItem[])methods.get(name2);
            if (meths!=null) {
                for (int i=0; i<meths.length; i++) {
                    //Log.trace("rtti.method","trying "+meths[i].getFullName());
                    if (meths[i].getFullName().endsWith(name)) {
                        res = new MethodItem[] {meths[i]};
                    }
                }
            }
        } else {
            res = (MethodItem[])methods.get(name);
        }

        /*ClassItem superClass=getSuperclass();
     
          if(res==null && superClass!=null) {
          res=superClass.getMethods(name);
          }*/

        if (res == null) {
            //Log.trace("rtti.method",2,"methods = "+methods);
            throw new NoSuchMethodException(
                "ClassItem.getMethods: no such method "+name+" in class "+this);
        }
        return res;
    }

    /**
     * Gets a method from its name.
     *
     * <p>If this method has homonym(s), then an
     * <code>AmbiguousMethodNameException</code> is thrown.
     *
     * <p>A method can be a static or instance method but not a
     * constructor.
     *
     * @param name the name of the method to search
     * @return the corresponding method if found
     * @see #getAbstractMethod(String)
     * @see #getMethods(String) */

    public MethodItem getMethod(String name) 
        throws NoSuchMethodException, AmbiguousMethodNameException
    {
        //Log.trace("rtti.method","getMethod("+name+")");      
        MethodItem[] res= getMethods(name);
        if (res.length>1) {
            throw new AmbiguousMethodNameException(
                "Ambiguous method name "+this+"."+name+" choice is:"
                +java.util.Arrays.asList(res));
        }
        return res[0];
    }

    /**
     * Gets a set of arrays containing all the method items for this
     * class item.
     *
     * <p>Each arrays contains the methods of the same name.
     *
     * @return a collection containing the methods 
     */
    public Collection getMethods() {
        Collection c = methods.values();
        Vector res = new Vector();
        Iterator it = c.iterator();
        while (it.hasNext()) {
            Object[] methods = (Object[]) it.next();
            if (methods[0] instanceof MethodItem) {
                res.add(methods);
            }
        }
        return res;
    }

    /**
     * Gets all the method items for this class item.<p>
     *
     * @return a collection containing the methods
     */
    public Collection getAllMethods() {
        Collection c = methods.values();
        Vector res = new Vector();
        Iterator it = c.iterator();
        while (it.hasNext()) {
            Object[] methods = (Object[]) it.next();
            for (int i=0; i<methods.length; i++) {
                if ( methods[i] instanceof MethodItem && 
                     !ClassRepository.isJacMethod(
                         ((MethodItem)methods[i]).getName()) ) {
                    res.add(methods[i]);
                }
            }
        }
        return res;
    }

    /**
     * Gets all the method items for this class item.<p>
     *
     * @return a collection containing the methods
     */
    public Collection getMixinMethods() {
        Collection c = methods.values();
        Vector res = new Vector();
        Iterator it = c.iterator();
        while (it.hasNext()) {
            Object[] methods = (Object[]) it.next();
            for (int i=0; i<methods.length; i++) {
                if ( methods[i] instanceof MixinMethodItem && 
                     !ClassRepository.isJacMethod(
                         ((MethodItem)methods[i]).getName()) ) {
                    res.add(methods[i]);
                }
            }
        }
        return res;
    }

    /**
     * @return a collection of static MethodItem 
     */
    public Collection getAllStaticMethods() {
        Collection c = methods.values();
        Vector res = new Vector();
        Iterator it = c.iterator();
        while (it.hasNext()) {
            Object[] methods = (Object[]) it.next();
            //Log.trace("rtti.static","getStatic checks "+methods[0]);
            for (int i=0; i<methods.length; i++) {
                if ( methods[i] instanceof MethodItem && 
                     !ClassRepository.isJacMethod(
                         ((MethodItem)methods[i]).getName()) && 
                     ((MethodItem)methods[i]).isStatic()) {
                    //Log.trace("rtti.static","getStatic adds "+methods[0]);
                    res.add(methods[i]);
                }
            }
        }
        return res;
    }

    /**
     * @return a collection of non static MethodItem 
     */
    public Collection getAllInstanceMethods() {
        Collection c = methods.values();
        Vector res = new Vector();
        Iterator it = c.iterator();
        while (it.hasNext()) {
            Object[] methods = (Object[]) it.next();
            for (int i=0; i<methods.length; i++) {
                if ( methods[i] instanceof MethodItem && 
                     !ClassRepository.isJacMethod(
                         ((MethodItem)methods[i]).getName()) && 
                     !((MethodItem)methods[i]).isStatic()) {
                    res.add(methods[i]);
                }
            }
        }
        return res;
    }

    /**
     * Gets all the method items that modify the state of the instances
     * for this class item.
     *
     * @return a collection of MethodItem containing the modifiers 
     */
    public Collection getAllModifiers() {
        Iterator it = getAllMethods().iterator();
        Vector modifiers = new Vector();
        while(it.hasNext()) {
            MethodItem method = (MethodItem)it.next();
            if (method.isModifier()) {
                modifiers.add(method);
            }
        }
        return modifiers;
    }


    /**
     * Gets all the method items that modify the state of the instances
     * for this class item.
     *
     * @return a collection of MethodItem containing the modifiers 
     */
    public Collection getAllSetters() {
        Iterator it = getAllMethods().iterator();
        Vector modifiers = new Vector();
        while(it.hasNext()) {
            MethodItem method = (MethodItem)it.next();
            if (method.isSetter()) {
                modifiers.add(method);
            }
        }
        return modifiers;
    }

    /**
     * Gets all the method items that modify a field of the instances
     * for this class item.
     *
     * @return a collection of MethodItem containing the writers 
     */
    public Collection getAllWriters() {
        Iterator it = getAllMethods().iterator();
        Vector modifiers = new Vector();
        while(it.hasNext()) {
            MethodItem method = (MethodItem)it.next();
            if (method.hasWrittenFields()) {
                modifiers.add(method);
            }
        }
        return modifiers;
    }

    /**
     * Gets all the getter methods of the class.
     *
     * @return a collection containing the getters 
     * @see MethodItem#isGetter()
     */
    public Collection getAllGetters() {
        Vector modifiers = new Vector();
        Iterator it = getAllMethods().iterator();
        while(it.hasNext()) {
            MethodItem method = (MethodItem)it.next();
            if (method.isGetter()) {
                modifiers.add(method);
            }
        }
        return modifiers;
    }

    /**
     * Gets all the method items that access the state of the instances
     * for this class item.
     *
     * @return a collection containing the accessors 
     */
    public Collection getAllAccessors() {
        Vector accessors = new Vector();
        Iterator it = getAllMethods().iterator();
        while(it.hasNext()) {
            MethodItem method = (MethodItem)it.next();
            if (method.isAccessor()) {
                accessors.add(method);
            }
        }
        return accessors;
    }


    /**
     * Gets all the method items that removes an object from a
     * collection of this class item.
     *
     * @return a collection containing the modifiers 
     */
    public Collection getAllRemovers() {
        Vector removers = new Vector();
        Iterator it = getAllMethods().iterator();
        while(it.hasNext()) {
            MethodItem method = (MethodItem)it.next();
            if (method.isRemover()) {
                removers.add(method);
            }
        }
        return removers;
    }

    /**
     * Gets all the method items that adds an object from a
     * collection of this class item.
     *
     * @return a collection containing the modifiers 
     */
    public Collection getAllAdders() {
        Collection methods = getAllMethods();
        Iterator it = methods.iterator();
        Vector adders = new Vector();

        while(it.hasNext()) {
            MethodItem method = (MethodItem)it.next();
            if (method.getAddedCollections() != null &&
                method.getAddedCollections().length>0) {
                adders.add(method);
            }
        }
        return adders;
    }

    /**
     * Gets all the constructors items for this class item.<p>
     *
     * @return a collection containing the constructors 
     */
    public Collection getConstructors() {
        Collection c = methods.values();
        Vector res = new Vector();
        Iterator it = c.iterator();
        while ( it.hasNext() ) {
            Object[] methods = (Object[]) it.next();
            //Log.trace("rtti.constructor","checking "+methods[0]);
            if (methods[0] instanceof ConstructorItem) {
                //Log.trace("rtti.constructor","adding "+methods[0]);
                res.addAll(java.util.Arrays.asList(methods));
            }
        }
        return res;
    }

    public ConstructorItem getConstructor(Class[] parameterTypes) 
        throws NoSuchMethodException
    {
        Iterator i = getConstructors().iterator();
        while (i.hasNext()) {
            ConstructorItem constructor = (ConstructorItem)i.next();
            if (java.util.Arrays.equals(constructor.getParameterTypes(),
                                        parameterTypes)) {
                return constructor;
            }
        }
        return null;
    }

    /**
     * Get a constructor with given parameter types
     * @param parameterTypes the types of the constructor
     * parameters. For instance "(java.lang.Object,java.lang.String)".
     */
    public ConstructorItem getConstructor(String parameterTypes) {
        return (ConstructorItem)getAbstractMethod(getShortName()+parameterTypes);
    }

    /**
     * Gets the constructor item of this class item that matches the
     * given <code>java.lang.reflect.Constructor</code>.
     *
     * @return the corresponding constructor item 
     */
    public ConstructorItem getConstructor(Constructor constructor) {
        //Log.trace("rtti","getConstructor("+constructor+")");
        Collection constructors = getConstructors();
        Iterator it = constructors.iterator();
        while ( it.hasNext() ) {
            ConstructorItem[] current = (ConstructorItem[])it.next();
            for (int i=0; i<current.length; i++) {
                //Log.trace("rtti","compare with "+current[i]);
                if (current[i].getActualConstructor().toString().equals(constructor.toString())) {
                    return current[i];
                }
            }
        }
        return null;
    }

    /**
     * Creates a new instance of this class item by using the default
     * constructor (the one with no parameters).
     *
     * @return the newly created instance */

    public Object newInstance() 
        throws InstantiationException, IllegalAccessException {
        return getActualClass().newInstance();
    }

    /**
     * Creates a new instance of this class item by using the 
     * constructor that matches the given parameter types.
     *
     * @param types the types of the constructor arguments
     * @param values the arguments values
     * @return the newly created instance */

    public Object newInstance(Class[] types,Object[] values) 
        throws InstantiationException, IllegalAccessException, 
        java.lang.NoSuchMethodException, InvocationTargetException {
        Constructor c = getActualClass().getConstructor(types);
        return c.newInstance(values);
    }

    /**
     * Create a new instance of the class, using the first constructor
     * suitable for the given parameters.
     *
     * @param parameters parameters of the constructor */
    public Object newInstance(Object[] parameters) 
        throws InstantiationException, IllegalAccessException, 
        java.lang.NoSuchMethodException, InvocationTargetException
    {
        Iterator i = getConstructors().iterator();
        ConstructorItem constructor = null;
        while (i.hasNext()) {
            constructor = (ConstructorItem)i.next();
            Class[] parameterTypes = constructor.getParameterTypes();
            if (parameterTypes.length==parameters.length) {
                int j;
                for (j=0; j<parameters.length; j++) {
                    // WARNING: this test does not work for primitive typed
                    // parameters               
                    if (!parameterTypes[j]
                        .isAssignableFrom(parameters[j].getClass())) {
                        break;
                    }
                }
                if (j==parameters.length) {
                    return constructor.newInstance(parameters);
                }
            }
        }
        throw new InstantiationException(
            "Could not find a suitable constructor for class "+getName()+
            " with arguments "+java.util.Arrays.asList(parameters));
    }

    /**
     * Gets the class represented by this class item.<p>
     *
     * @return the actual class
     * @see #getType()
     */

    public Class getActualClass() {
        return (Class)delegate;
    }

    ClassItem[] interfaceItems = null;
    /**
     * Returns the interfaces implemented by this class.
     */
    public ClassItem[] getInterfaceItems() {
        if (interfaceItems==null) {
            Class[] interfaces = getActualClass().getInterfaces();
            interfaceItems = new ClassItem[interfaces.length];
            ClassRepository cr = ClassRepository.get();
            for (int i=0; i<interfaces.length; i++) {
                interfaceItems[i] = cr.getClass(interfaces[i]);
            }
        }
        return interfaceItems;
    }

    /** the super class */
    ClassItem superClass;

    /**
     * Set the super class of this class and updates the children of
     * the super class.
     * @param superClass the super class
     */
    protected void setSuperClass(ClassItem superClass) {
        this.superClass = superClass;
        superClass.addChild(this);
    }

    /**
     * Gets the superclass of this class item.<p>
     *
     * @return the superclass, null if the superclass is Object
     */
    public ClassItem getSuperclass() {
        return superClass;
    }

    /** A list of ClassItem whose super class is this class */
    Vector children = new Vector();

    public Collection getChildren() {
        return children;
    }

    public void addChild(ClassItem child) {
        children.add(child);
    }

    /**
     * Tells wether the class inherits from a subclass whose name
     * matches a regular expression.
     * @param classNameRE the regular expression
     */
    public boolean isSubClassOf(RE classNameRE) {
        if (classNameRE.isMatch(getName())) {
            return true;
        } else if (superClass!=null) {
            return superClass.isSubClassOf(classNameRE);
        } else {
            ClassItem[] interfaces = getInterfaceItems();
            for (int i=0; i<interfaces.length; i++) {
                if (interfaces[i].isSubClassOf(classNameRE))
                    return true;
            }
            return false;
        }
    }

    /**
     * Tells wether the class inherits from a subclass 
     *
     * @param cl the regular expression
     */
    public boolean isSubClassOf(ClassItem cl) {
        if (cl==this) {
            return true;
        } else if (superClass!=null) {
            return superClass.isSubClassOf(cl);
        } else {
            ClassItem[] interfaces = getInterfaceItems();
            for (int i=0; i<interfaces.length; i++) {
                if (interfaces[i].isSubClassOf(cl))
                    return true;
            }
            return false;
        }
    }

    public int getModifiers() {
        return ((Class)delegate).getModifiers();
    }

    /**
     * Synonym of <code>getActualClass</code>.
     *
     * @return the actual class
     * @see #getActualClass()
     */

    public Class getType() {
        return getActualClass();
    }

    /**
     * Add a method item to this class item.
     *
     * @param method the new method
     */
    public void addMethod(MethodItem method) {
        String name = method.getName();
        //Log.trace("rtti.method","addMethod: "+name+" -> "+getName()+"."+method);
        if (!methods.containsKey(name)) {
            methods.put(name, new MethodItem[] { method } );
        } else {
            MethodItem[] meths = (MethodItem[])methods.get(name);
            MethodItem[] newMeths = new MethodItem[meths.length + 1];
            System.arraycopy(meths, 0, newMeths, 0, meths.length);
            newMeths[meths.length] = method;
            methods.remove(name);
            methods.put(name, newMeths);
        }
        methodsCount++;
        if (method instanceof MixinMethodItem) {
            Iterator i = children.iterator();
            while (i.hasNext()) {
                ClassItem subclass = (ClassItem)i.next();
                subclass.addMethod(method);
            }
        }
        try {
            method.setParent(this);
        } catch(Exception e) {
            logger.error("addMethod "+method.getFullName()+
                         ": could not set the parent of the method",e);
        }
    }   

    Vector mixinMethods = new Vector();

    /**
     * Add a constructor item to this class item.<p>
     *
     * @param constructor the new constructor
     */
    public void addConstructor(ConstructorItem constructor) {
        String name = NamingConventions.getShortConstructorName(constructor.getActualConstructor());
        //Log.trace("rtti.method","addConstructor: "+name+" -> "+constructor);
        if ( ! methods.containsKey( name )) {
            methods.put( name, new ConstructorItem[] { constructor } );
        } else {
            ConstructorItem[] constructors = 
                (ConstructorItem[]) methods.get( name );
            ConstructorItem[] newConstructors = 
                new ConstructorItem[constructors.length + 1];
            System.arraycopy(constructors, 0, newConstructors, 0, constructors.length);
            newConstructors[constructors.length] = constructor;
            methods.remove(constructor.getName());
            methods.put(name, newConstructors);
        }
        constructorsCount++;
        try {
            constructor.setParent(this);
        } catch( Exception e ) {
            logger.error("addConstructor "+constructor.getFullName(),e);
        }
    }   
   
    /**
     * Tests if a method exist in this class item.<p>
     * 
     * @return true if exist
     */
    public boolean hasMethod(String name) {
        try {
            getAbstractMethod(name);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        } catch (AmbiguousMethodNameException e) {
            return true;
        }
    }

    public boolean hasMethod(MethodItem method) {
        return method.parent==this;
    }

    /**
     * Tests if a field exist in this class item.<p>
     * 
     * @return true if exist
     */

    public boolean hasField( String name ) {
        buildFieldInfo();
        return (name!=null) && fields.containsKey( name );
    }

    public String getName() {
        return ((Class)delegate).getName();
    }

    public String getShortName() {
        String name = getName();
        int index = name.lastIndexOf('.');
        return index==-1 ? name : name.substring(index+1);
    }

    /**
     * A shortcut to invoke a method (avoid to get the method item).
     *
     * @param object the object to perform the invocation on (may be
     * null for a static method)
     * @param methodName the name of the method to invoke
     * @param parameters the parameters passed to the method
     * @see MethodItem#invoke(Object,Object[]) */

    public Object invoke(Object object, String methodName, Object[] parameters)
        throws IllegalAccessException, InvocationTargetException 
    {
        return getMethod(methodName).invoke(object, parameters);
    }

    /**
     * Tells wether this class is an inner class of some other class 
     *
     * @return true if the class is an inner class
     */
    public boolean isInner() {
        return getName().indexOf('$')!=-1;
    }

    public boolean isAbstract() {
        return Modifier.isAbstract(getModifiers());
    }

    /**
     * Gets the class this class is an inner class of.
     *
     * @return the ClassItem this class is an inner class of, or null.
     */
    public ClassItem getOwnerClassItem() {
        int index = getName().indexOf('$');
        if (index==-1) {
            return null;
        } else {
            return ClassRepository.get().getClass(getName().substring(0,index));
        }
    }

    /**
     * Finds the collection of this class item that contains the given
     * object.
     *
     * @param substance the instance of the current class item to seek
     * in
     * @param object the object to find
     * @return the collection item, null if not found */

    public CollectionItem findCollectionFor(Object substance,Object object) {
        CollectionItem[] collections=getCollections();
        for(int i=0;i<collections.length;i++) {
            Collection cur=collections[i].getActualCollection(substance);
            if(cur.contains(object)) {
                return collections[i];
            } 
        }
        return null;
    }

    /**
     * Get an attribute by searching recursively through all super classes.
     */
    public Object getAttribute(String name) {
        ClassItem cur = this;
        Object result = null;
        while (cur!=null && result==null) {
            result = cur.superGetAttribute(name);
            cur = cur.getSuperclass();
        }
        return result;
    }

    public Object superGetAttribute(String name) {
        return super.getAttribute(name);
    }

    Vector constraints;
    /**
     * Return all field and collection items whose component type is
     * this class.
     * @return a collection of FieldItem
     */
    public Collection getConstraints() {
        if (constraints==null) {
            constraints = new Vector();
            Object[] classes = ClassRepository.get().getClasses();
            for (int i=0; i<classes.length;i++) {
                if (classes[i] instanceof ClassItem) {
                    ClassItem cl = (ClassItem)classes[i];
                    FieldItem[] fields = cl.getFields();
                    for (int j=0; j<fields.length; j++) {
                        if (fields[j] instanceof CollectionItem) {
                            if (((CollectionItem)fields[j]).getComponentType()==this)
                                constraints.add(fields[j]);
                        } else if (fields[j].getTypeItem()==this) {
                            constraints.add(fields[j]);
                        }
                    }
                }
            }
        }
        return constraints;
    }

    /**
     * The exception that is thrown when the accessed method has some
     * synonymes (methods with same names but different parameter
     * types). */
    public static class AmbiguousMethodNameException extends RuntimeException {
        public AmbiguousMethodNameException(String msg) { super(msg); }
        public AmbiguousMethodNameException() { super(); }
    }
}
