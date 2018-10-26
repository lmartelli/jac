/*
  Copyright (C) 2001-2003 Laurent Martelli <laurent@aopsys.com>
  
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

package org.objectweb.jac.aspects.gui;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.table.AbstractTableModel;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.Collaboration;
import org.objectweb.jac.core.Wrappee;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.core.rtti.CollectionItem;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.MemberItem;
import org.objectweb.jac.core.rtti.MetaItem;
import org.objectweb.jac.core.rtti.MethodItem;
import org.objectweb.jac.core.rtti.RttiAC;
import org.objectweb.jac.util.Classes;
import org.objectweb.jac.util.Enum;
import org.objectweb.jac.util.ExtArrays;
import org.objectweb.jac.util.Stack;

/**
 * The data model for tables. */

public class TableModel extends AbstractTableModel
    implements ExtendedTableModel, ObjectUpdate, CollectionUpdate
{
    static Logger logger = Logger.getLogger("gui.generic");
    static Logger loggerTable = Logger.getLogger("gui.table");
    static Logger loggerAssoc = Logger.getLogger("gui.events");
    static Logger loggerEvents = Logger.getLogger("associations");
    static Logger loggerReg = Logger.getLogger("gui.register");

    List rows = new Vector();
    List objects = new Vector();
    String[] headers;
    ClassItem[] classes;
    MemberItem[] members;

    CollectionItem collection;
    Object substance;

    CollectionItemView collectionView;

    /**
     * Creates a new table model.
     *
     * @param collection the substance collection
     * @param substance the object that holds the collection
     * @param factory the used view factory 
     */
    public TableModel(CollectionItem collection, 
                      Object substance, String viewName,
                      ViewFactory factory)
    {
        this.collection = collection;
        this.substance = substance;
        collectionView = GuiAC.getView(collection,viewName);

        members = collectionView.getMembersOrder();
        logger.debug("membersOrder : " + 
                  (members!=null?(Arrays.asList(members).toString()):"[]"));
        if (members==null) {
            ClassItem clit = collection.getComponentType();
            if (clit!=null  && !(collection.isMap() && !RttiAC.isIndex(collection))) {
                logger.debug("class : " + clit.getName());
                ObjectView objectView = GuiAC.getView(clit,viewName);
                if (members==null)
                    members = objectView.getTableMembersOrder();
                logger.debug("class tableMembersOrder : " + 
                          (members!=null?(Arrays.asList(members)+""):""));
                if (members==null)
                    members = (MemberItem[])objectView.getAttributesOrder();
                logger.debug("class attributesOrder : " +
                          (members!=null?(Arrays.asList(members)+""):""));
                if (members==null)
                    members = clit.getFields();
            } else if (!RttiAC.isIndex(collection)) {
                members = new MemberItem[2];
                clit = ClassRepository.get().getClass(Map.Entry.class);
                members[0] = clit.getField("key");
                members[1] = clit.getField("value");
            } else {
                members = new MemberItem[0];
            }
        }
        logger.debug("columns : " + Arrays.asList(members));

        FieldItem oppositeRole = 
            (FieldItem)Collaboration.get().getAttribute(GuiAC.OPPOSITE_ROLE);
        if (oppositeRole instanceof CollectionItem) {
            loggerAssoc.debug("Ignoring collection oppositeRole "+oppositeRole);
            oppositeRole = null;
        }

        // Ensure that members contains no oppositeRole
        MemberItem[] tmp = new MemberItem[members.length];
        int j=0;
        for(int i=0; i<members.length; i++) {
            if (members[i]!=oppositeRole) {
                tmp[j] = members[i];
                j++;
            }
        }
        members = new MemberItem[j];
        System.arraycopy(tmp,0,members,0,j);

        int nbColumns = members.length;
        headers = new String[nbColumns];
        classes = new ClassItem[nbColumns];

        Stack context = GuiAC.getGraphicContext();
        for (int i=0; i<nbColumns; i++) {
            if (members[i] instanceof FieldItem) {
                headers[i] = GuiAC.getLabel(members[i],context);
            }
            classes[i] = members[i].getTypeItem();
        }

        buildData();
        Utils.registerCollection(substance,collection,this);
    }

    public CollectionItem getCollection() {
        return collection;
    }

    static class CellLocation {
        int row;
        int column;
        public CellLocation(int row, int column) {
            this.row = row;
            this.column = column;
        }
    }

    public Object getCellRenderer(View tableView, int column, 
                                  ViewFactory factory, DisplayContext context) {
        return getCellRenderer(tableView,substance,
                               members[column],headers[column],
                               collectionView,
                               factory,context);
    }

    public static Object getCellRenderer(View tableView, 
                                         Object substance,
                                         MemberItem member, 
                                         String header,
                                         CollectionItemView collectionView,
                                         ViewFactory factory, 
                                         DisplayContext context) 
    {
        if (member instanceof FieldItem) {
            FieldItem field = (FieldItem)member;
            Stack typeNames = new Stack();

            if (field.isReference())
                typeNames.push("cell:Reference");
            else if (field instanceof CollectionItem)
                typeNames.push("cell:List");
            typeNames.push("cell:"+field.getTypeItem().getName());

            MetaItem type = RttiAC.getFieldType(field);
            if (type!=null)
                typeNames.push("cell:"+type.getName());

            Enum enum = GuiAC.getEnum(field);
            if (enum!=null)
                typeNames.push("cell:Enum");

            if (collectionView!=null) {
                String viewType = collectionView.getViewType(field);
                if (viewType!=null) {
                    typeNames.push(viewType);
                }
            }

            loggerTable.debug("types for "+field+": "+typeNames);
            while (!typeNames.empty()) {
                String typeName = (String)typeNames.pop();
                if (factory.hasViewerFor(typeName)) {
                    try {
                        FieldView cellViewer = (FieldView)factory.createView(
                            "table["+field.getName()+"]",
                            typeName,
                            ExtArrays.emptyObjectArray,context);
                        if (cellViewer instanceof TableCellViewer) {
                            ((TableCellViewer)cellViewer).setTable(tableView);
                        }
                        loggerTable.debug("viewer = "+cellViewer);
                        cellViewer.setField(field);
                        return cellViewer;
                    } catch (Exception e) {
                        logger.error("Failed to instanciate TableCellRenderer "+
                                     field.getName()+" for column "+header,
                                     e);
                    }
                } else {
                    loggerTable.debug("no viewer found for type "+typeName);
                }
            }
        } else if (member instanceof MethodItem) {
            MethodItem method = (MethodItem)member;
            try {
                MethodView cellViewer = (MethodView)factory.createView(
                    "table["+method.getName()+"]",
                    "cell:Method",
                    new Object[] {substance, method},context);
                if (cellViewer instanceof TableCellViewer) {
                    ((TableCellViewer)cellViewer).setTable(tableView);
                }
                loggerTable.debug("viewer = "+cellViewer);
                return cellViewer;
            } catch (Exception e) {
                logger.error("Failed to instanciate TableCellRenderer "+
                             method.getName()+" for column "+header,
                             e);
            }
        }
        return null;
    }

    /**
     * Populate the table model with the objects from the collection.
     */
    void buildData() {
        Collection c = collection.getActualCollectionThroughAccessor(substance);
        logger.debug("substance : " + substance);
        logger.debug("objects : " + new Vector(c));
        int nbColumns = members.length;
        Iterator i = c.iterator();
        int row = 0;
        while (i.hasNext()) {
            Object obj = i.next();
            Object[] data = new Object[nbColumns];
            if (obj!=null) {
                for (int j=0; j<nbColumns; j++) {
                    try {
                        if (members[j] instanceof FieldItem) { 
                            data[j] = ((FieldItem)members[j]).getThroughAccessor(obj);
                            if (data[j] instanceof Wrappee && !(data[j] instanceof Collection)) {
                                Utils.registerObject(data[j],this,new CellLocation(row,j));
                            }
                        } else if (members[j] instanceof MethodItem) {
                            data[j] = members[j].getName();
                        }
                    } catch (Exception e) {
                        logger.error("Failed to build table cell for "+obj+"."+members[j].getName(),e);
                    }
                }
            }
            logger.debug("add "+Arrays.asList(data));
            addRow(obj,data);
            row++;
        }      
    }

    public MemberItem[] getMembers() {
        return members;
    }

    public String[] getHeaders() {
        return headers;
    }

    /**
     * Gets the column number of a field
     */
    public int getColumnIndex(FieldItem field) {
        for (int i=0; i<members.length; i++) {
            if (field==members[i])
                return i;
        }
        return -1;
    }

    public int getRowCount() {
        return rows.size();
    }
    public int getColumnCount() {
        return headers.length;
    }

    public String getColumnName(int column) {
        return headers[column];
    }
    public Class getColumnClass(int column) {
        loggerTable.debug("getColumnClass("+column+") -> "+classes[column].getName());
        return Classes.getPrimitiveTypeWrapper(classes[column].getActualClass());
    }
    public Object getValueAt(int row, int column) {
        loggerTable.debug("getValueAt("+row+","+column+") -> "+
                  ((Object[])rows.get(row))[column]);
        return ((Object[])rows.get(row))[column];
    }
    public Object getObject(int index) {
        return objects.get(index);
    }
    public int indexOf(Object object) {
        return objects.indexOf(object);
    }
    public Object getObject(int row, int col) {
        return ((Object[])rows.get(row))[col];
    }
    public Object[] getRow(int row) {
        return (Object[])rows.get(row);
    }
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    public void addRow(Object object, Object[] data) {
        objects.add(object);
        rows.add(data);
        Utils.registerObject(object,this);
    }

    // ObjectUpdate interface
    public void objectUpdated(Object substance,Object param) {
        if (param==null) {
            int index = objects.indexOf(substance);
            if (index!=-1) {
                int nbColumns = getColumnCount();
                Object[] data = new Object[nbColumns];
                for (int j=0; j<nbColumns; j++) {
                    if(members[j] instanceof FieldItem) {
                        data[j] = ((FieldItem)members[j]).getThroughAccessor(substance);
                    }
                }
                rows.set(index,data);
            }
            fireTableRowsUpdated(index,index);
        } else {
            CellLocation location = (CellLocation)param;
            fireTableCellUpdated(location.row,location.column);
        }
    }

    // CollectionUpdate
    public void onChange(Object substance, CollectionItem collection, 
                         Object value, Object param) {
        loggerEvents.debug("onChange "+substance+"."+collection);
        unregisterViews();
        int numRows = objects.size();
        loggerEvents.debug("  numRows="+numRows);
        objects.clear();
        rows.clear();
        if (numRows>0)
            fireTableRowsDeleted(0,numRows-1);
        buildData();
        fireTableRowsInserted(0,objects.size()-1);
    }

    public void onAdd(Object substance, CollectionItem collection, 
                      Object value, Object added, Object param) {
        loggerEvents.debug("onAdd "+substance+"."+collection);
        // it's not that easy to optimize because we don't know the
        // position of the added object
        onChange(substance,collection,value,param);
    }

    public void onRemove(Object substance, CollectionItem collection, 
                         Object value, Object removed, Object param) {
        loggerEvents.debug("onRemove "+substance+"."+collection);
        onChange(substance,collection,value,param);
    }

    /**
     * Register ourself as a view on all objects of the collection
     */
    protected void registerViews() {
        loggerReg.debug("TableModel.registerViews "+objects.size());
        Iterator i = objects.iterator();
        while (i.hasNext()) {
            Object object = i.next();
            Utils.registerObject(object,this);
        }
    }

    /**
     * Unregister ourself as a view on all objects of the collection
     */
    protected void unregisterViews() {
        loggerReg.debug("TableModel.unRegisterViews "+objects.size());
        Iterator i = objects.iterator();
        while (i.hasNext()) {
            Object object = i.next();
            Utils.unregisterObject(object,this);
        }      
    }

    public void close() {
        unregisterViews();
        Utils.unregisterCollection(substance,collection,this);
    }

    public TableFilter getFilter() {
        return null;
    }
    public TableSorter getSorter() {
        return null;
    }
}
