/*
  Copyright (C) 2002-2003 Laurent Martelli <laurent@aopsys.com>
  
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

import java.lang.reflect.*;
/**
 * This class defines a meta item that corresponds to a field or a
 * method.
 *
 * @author Renaud Pawlak
 * @author Laurent Martelli
 */

public abstract class MemberItem extends MetaItemDelegate {

    /**
     * A util method to get a member item reference from a full name.
     *
     * @param str member's full name, E.g.: myPackage.Person.name. 
     */
    public static MemberItem getMemberFromFullName(String str) throws Exception {
        MemberItem ret=null;
        int index = -1;
        int paren = str.indexOf("(");
        if (paren==-1)
            index = str.lastIndexOf(".");
        else
            index = str.lastIndexOf(".",paren);
        if (index!=-1) {
            ClassItem classItem = 
                ClassRepository.get().getClass
                (str.substring(0,index));
            ret = classItem.getMember(str.substring(index+1));
        } else {
            new Exception("Failed to convert "+str+
                          " into a class member");
        }
        return ret;
    }

    static Class wrappeeClass = ClassRepository.wrappeeClass;

    public MemberItem(ClassItem parent) {
        this.parent = parent;
    }

    public MemberItem(Object delegate, ClassItem parent) throws InvalidDelegateException {
        super(delegate);
        this.parent = parent;
    }

    public abstract Class getType();

    public final ClassItem getTypeItem() {
        return ClassRepository.get().getClass(getType());
    }

    /** Returns the class item that owns the field (like getParent). */ 
    public final ClassItem getClassItem() {
        return (ClassItem)getParent();
    }

    MethodItem[] dependentMethods = MethodItem.emptyArray;
    /**
     * @see #getDependentMethods()
     */
    public final void addDependentMethod(MethodItem method) {
        MethodItem[] tmp = new MethodItem[dependentMethods.length+1];
        System.arraycopy(dependentMethods, 0, tmp, 0, dependentMethods.length);
        tmp[dependentMethods.length] = method;
        dependentMethods = tmp;
        ClassItem superClass = getClassItem().getSuperclass();
        if (superClass!=null) {
            FieldItem superField = superClass.getFieldNoError(getName());
            if (superField!=null)
                superField.addDependentMethod(method);
        }
    }
    /**
     * Returns an array of methods whose result depend on the member.
     * @see #addDependentMethod(MethodItem)
     */
    public final MethodItem[] getDependentMethods() {
        return dependentMethods;
    }


    protected boolean role;
   
    /**
     * Tells if this field is actually implemented by a role wrapper
     * field.
     *
     * @return value of role
     * @see #setRole(ClassItem,String,String) */

    public boolean isRole() {
        return role;
    }

    protected ClassItem roleClassType = null;
    protected String roleType = null;
    protected String roleName = null;
   
    /**
     * Sets this field to be actually implemented by a field of a role
     * wrapper.
     *
     * <p>When this method is called once, the <code>isRole()</code>
     * method will return true. Moreover, the actually accessed and
     * modified field when using set, get, etc, is the field of the
     * role wrapper.
     */
    public void setRole(ClassItem roleClassType, String roleType, String roleName) {
        this.role = true;
        this.roleClassType = roleClassType;
        this.roleType = roleType;
        this.roleName = roleName;
    }
   
    /**
     * Returns the name prfixed with the owning class name.
     */
    public String getLongName() {
        return parent!=null ? parent.getName()+"."+getName() : "???";
    }

    public int getModifiers() {
        return ((Member)delegate).getModifiers();
    }

    public String toString() {
        return getLongName()+":"+getType().getName();
    }

    /**
     * Two members are equal if the class of one is a subclass of the
     * other's class and they have the same name
     */
    public boolean equals(Object o) {
        if (!(o instanceof MemberItem)) 
            return false;
        MemberItem m = (MemberItem)o;
        return (m.getClassItem().isSubClassOf(getClassItem()) ||
                getClassItem().isSubClassOf(m.getClassItem()))
            && getName().equals(m.getName());
    }
}
