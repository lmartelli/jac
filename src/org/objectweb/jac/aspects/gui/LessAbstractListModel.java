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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.swing.AbstractListModel;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.rtti.CollectionItem;
import org.objectweb.jac.util.Stack;

/**
 * Base class for ListModel and ComboBoxModel.
 */
public abstract class LessAbstractListModel extends AbstractListModel 
    implements ObjectUpdate, CollectionModel, CollectionUpdate
{
    static Logger logger = Logger.getLogger("gui.model");

    CollectionItem collection=null;
    Object substance;

    List rows = new Vector();
    List objects = new Vector();

    Stack context = new Stack();

    /**
     * Construct a new abstract list model which is independent from
     * any collection. */
    public LessAbstractListModel() {
        if (GuiAC.getGraphicContext()!=null)
            context.addAll(GuiAC.getGraphicContext());      
    }

    /**
     * Construct a new abstract list model in which the values depend
     * on a collection's values.
     *
     * @param collection the substance collection
     * @param substance the object that holds the collection's value */
    public LessAbstractListModel(CollectionItem collection, Object substance) {
        if (GuiAC.getGraphicContext()!=null)
            context.addAll(GuiAC.getGraphicContext());      
       
        this.collection = collection;
        this.substance = substance;

        buildData();
        Utils.registerCollection(substance,collection,this);
    }

    void buildData() {
        Collection c = collection.getActualCollectionThroughAccessor(substance);
        if (c!=null) {
            logger.debug("buildData for "+substance+"."+collection.getName());
            logger.debug("objects : " + new Vector(c));
            Iterator i = c.iterator();
            while (i.hasNext()) {
                Object obj = i.next();
                logger.debug("add "+obj);
                addObject(obj);
            }
        }
    }

    public CollectionItem getCollection() {
        return collection;
    }

    String nullLabel;
    /**
     * Sets the default label to be used for a null reference.
     * @param label label for null
     */
    public void setNullLabel(String label) {
        this.nullLabel = label;
    }

    /**
     * Adds an object in the list. Uses GuiAC.toString() to get a string
     * representation of the object.
     * @param object the object to add 
     * @see #addObject(Object,String)
     * @see #setNullLabel(String)
     */
    public void addObject(Object object) {
        addObject(object,
                  object==null ? 
                  	  (nullLabel!=null ? nullLabel : GuiAC.getLabelNone()) 
                  	: GuiAC.toString(object,context));
    }

    /**
     * Add an object in the list with a given label to be displayed. 
     * @param object the object to add
     * @param label the label to be displayed for the object
     * @see #addObject(Object)
     */
    public void addObject(Object object, String label) {
        objects.add(object);
        rows.add(label);
        fireIntervalAdded(this,objects.size()-1,objects.size()-1);
        Utils.registerObject(object,this);
    }

    /**
     * Gets the list element count. */
    public int getRowCount() {
        return rows.size();
    }

    /**
     * Returns the element at a given row. */
    public Object getElementAt(int row) {
        logger.debug("getElementAt("+row+") -> "+rows.get(row));
        return rows.get(row);
    }

    /**
     * Gets the list size (same as <code>getRowCount</code>). */
    public int getSize() {
        return getRowCount();
    }

    /**
     * Gets the object at a given index. */
    public Object getObject(int index) {
        return objects.get(index);
    }

    public int indexOf(Object object) {
        return objects.indexOf(object);
    }

    /**
     * Tells if this cell is directly editable (always returns false
     * for the moment). */

    public boolean isCellEditable(int row, int column) {
        return false;
    }

    // ObjectUpdate interface
    public void objectUpdated(Object substance,Object param) {
        int index = objects.indexOf(substance);
        if (index!=-1) {
            rows.set(index,GuiAC.toString(objects.get(index),context));
            fireContentsChanged(this,index,index);
        }
    }

    private int getMin(String row, Object obj)
    {
        int min = 0;
        for (int i = 1; i < rows.size(); i++)
        {
            if ((((String) (((Vector) rows).elementAt(i)))
                 .compareToIgnoreCase((String) (((Vector) rows).elementAt(min))))
                < 0)
                min = i;
        }
        return min;
    }

    /**
     * sort the list alphabetically by label
     */
    public void sort()
    {
        Vector newRows = new Vector();
        Vector newObjs = new Vector();
      
        Object obj = null;
        String row = null;

        while (rows.size() > 0)
        {
            int min = getMin(row, obj);
            row = (String) ((Vector) rows).elementAt(min);
            obj = ((Vector) objects).elementAt(min);
            newRows.add(row);
            newObjs.add(obj);
            rows.remove(row);
            objects.remove(obj);
        }
        rows = newRows;
        objects = newObjs;
    }


    /**
     * Unregister ourself as a view on all objects of the collection
     */
    protected void unregisterViews() {
        logger.debug("TableModel.unRegisterViews "+objects.size());
        Iterator i = objects.iterator();
        while (i.hasNext()) {
            Object object = i.next();
            Utils.unregisterObject(object,this);
        }      
    }

    public void close() {
        unregisterViews();
        if(collection!=null) {
            Utils.unregisterCollection(substance,collection,this);
        }
    }

    // CollectionUpdate
    public void onChange(Object substance, CollectionItem collection, 
                         Object value, Object param) {
        unregisterViews();
        int size = objects.size();
        objects.clear();
        rows.clear();
        if (size>0)
            fireIntervalRemoved(this,0,size-1);
        buildData();
        if (!objects.isEmpty())
            fireIntervalAdded(this, 0, objects.size()-1);
    }

    public void onAdd(Object substance, CollectionItem collection, 
                      Object value, Object added, Object param) {
        onChange(substance,collection,value,param);
    }

    public void onRemove(Object substance, CollectionItem collection, 
                         Object value, Object removed, Object param) {
        onChange(substance,collection,value,param);
    }


}
