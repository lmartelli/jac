/*
  Copyright (C) 2003 Laurent Martelli <laurent@aopsys.com>
  
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

package org.objectweb.jac.aspects.gui;

import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.MemberItem;
import org.objectweb.jac.core.rtti.MethodItem;

/**
 * Defines a generic object view (attributes order, tabs, ...)
 */
public class ObjectView {
    ClassItem cl;
    String name;
    ObjectView parent;

    public ObjectView(ClassItem cl, String name) {
        this.cl = cl;
        this.name = name;
    }

    public ObjectView(ClassItem cl, String name, ObjectView parent) {
        this.cl = cl;
        this.parent = parent;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    FieldItem[] attributesOrder;
    public void setAttributesOrder(FieldItem[] attributesOrder) {
        this.attributesOrder = attributesOrder;
    }
    /**
     * If no attributesOrder were configured for this view, try the
     * parent view, and then the view of the super class.
     */
    public FieldItem[] getAttributesOrder() {
        if (attributesOrder!=null) {
            return attributesOrder;
        } else {
            if (parent!=null) 
                return parent.getAttributesOrder();
            else {
                ClassItem superClass = cl.getSuperclass();
                if (superClass!=null)
                    return GuiAC.getView(superClass,name).getAttributesOrder();
                else
                    return null;
            }
        }
    }

    MethodItem[] methodsOrder;
    public void setMethodsOrder(MethodItem[] methods) {
        this.methodsOrder = methods;
    }
    /**
     * If no methodsOrder were configured for this view, try the
     * parent view.
     */
    public MethodItem[] getMethodsOrder() {
        if (methodsOrder!=null) {
            return methodsOrder;
        } else {
            if (parent!=null) 
                return parent.getMethodsOrder();
            else {
                ClassItem superClass = cl.getSuperclass();
                if (superClass!=null)
                    return GuiAC.getView(superClass,name).getMethodsOrder();
                else
                    return null;
            }
        }
    }

    MemberItem[] tableMembersOrder;
    public void setTableMembersOrder(MemberItem[] members) {
        this.tableMembersOrder = members;
    }
    /**
     * If no tableMembersOrder were configured for this view, try the
     * parent view.  
     */
    public MemberItem[] getTableMembersOrder() {
        if (tableMembersOrder!=null) {
        return tableMembersOrder;
        } else {
            if (parent!=null) 
                return parent.getTableMembersOrder();
            else
                return null;            
        }
    }

    String[] categories;
    public void setCategories(String[] categories) {
        this.categories = categories;
    }
    public String[] getCategories() {
        if (categories!=null) {
            return categories;
        } else {
            if (parent!=null) 
                return parent.getCategories();
            else
                return null;
        }
    }

    /** wether fields should be editable */
    boolean readOnly;
    public boolean isReadOnly() {
        return readOnly;
    }
    public void setReadOnly(boolean newReadOnly) {
        this.readOnly = newReadOnly;
    }

    boolean enableLinks = true;
    public void setEnableLinks(boolean enable) {
        this.enableLinks = enable;
    }
    public boolean areLinksEnabled() {
        return enableLinks;
    }
}
