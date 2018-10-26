/*
  Copyright (C) 2002 Laurent Martelli <laurent@aopsys.com>
  
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

import org.apache.log4j.Logger;
import org.objectweb.jac.core.Wrappee;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.CollectionItem;

/**
 * This is an abstract representation of a combo box. */

public class ComboBoxModel extends LessAbstractListModel 
    implements ObjectChooserModel
{
    static Logger logger = Logger.getLogger("gui.combobox");

    /**
     * The constructor for an independent combobox model. */
    public ComboBoxModel() {
        super();
    }

    /**
     * The constructor for a combobox model that is linked to a
     * collection (values will be consistent).
     *
     * @param collection the substance collection
     * @param substance the object that holds the collection's value */
    public ComboBoxModel(CollectionItem collection, Object substance) {
        super(collection,substance);
    }

    /**
     * Adds an object in the combo box.
     *
     * @param object the new object
     * @param label the associated label 
     */
    public void addObject(Object object, String label) {
        logger.debug("addChoice("+object+" -> "+label+")");
        String key = label;
        int i=2;
        while (rows.contains(key)) {
            key = label+"<"+(i++)+">";
        }
        super.addObject(object,key);
    }

    int selectedIndex = -1;
    Object selectedObject = null;
    Object selectedObjectString = null;

    /**
     * Returns the currently selected object of the combo (same as
     * <code>getSelectedObject</code>). */
    public Object getSelectedItem() {
        return selectedObjectString;
    }
    /**
     * Sets the selected object by it's name. 
     * @param object name of the object to select (should be a String)
     * @see #setSelectedObject(Object)
     */
    public void setSelectedItem(Object object) {
        logger.debug(this+".setSelectedItem("+object+")");
        //logger.debug("rows = "+rows);
        //logger.debug("objects = "+objects);
        selectedIndex = rows.indexOf(object);
        selectedObjectString = object;
        if (selectedIndex!=-1) {
            selectedObject = objects.get(selectedIndex);
        } else {
            if (type!=null && Wrappee.class.isAssignableFrom(type.getActualClass()))
                throw new RuntimeException("ComboBoxModel: no such element '"+object+"'");
            // <HACK> we should transform the string (object) into the correct type
            selectedObject = object;
            // </HACK>
        }
        logger.debug("    selectedIndex="+selectedIndex);
        logger.debug("    selectedObject="+selectedObject);
        logger.debug("    selectedObjectString="+selectedObjectString);
        fireContentsChanged(this,-1,-1);
    }

    /**
     * Sets the selected object
     * @param object the object to select 
     * @see #setSelectedItem(Object)
     */
    public void setSelectedObject(Object object) {
        logger.debug(this+".setSelectedObject("+object+")");
        //logger.debug("rows = "+rows);
        //logger.debug("objects = "+objects);
        selectedIndex = objects.indexOf(object);
        selectedObject = object;
        if (selectedIndex!=-1)
            selectedObjectString = rows.get(selectedIndex);
        else
            selectedObjectString = GuiAC.toString(object);
        logger.debug("    selectedIndex="+selectedIndex);
        logger.debug("    selectedObject="+selectedObject);
        logger.debug("    selectedObjectString="+selectedObjectString);
        fireContentsChanged(this,-1,-1);
    }

    /**
     * Returns the currently selected object of the combo (same as
     * <code>getSelectedItem</code>). */
    public Object getSelectedObject() {
        return selectedObject;
    }

    // ObjectChooserModel interface

    ClassItem type;
    public void setType(ClassItem type) {
        this.type = type;
    }
    public ClassItem getType() {
        return type; 
    }
}
