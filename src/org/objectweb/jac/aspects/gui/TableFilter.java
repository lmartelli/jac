/*
  Copyright (C) 2003 Laurent Martelli <laurent@aopsys.com>

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
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
*/

package org.objectweb.jac.aspects.gui;

import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.TableModelEvent;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.Collaboration;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.CollectionItem;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.util.Enum;


public class TableFilter extends TableMap implements ListDataListener {
    static Logger logger = Logger.getLogger("gui.filter");
    static Logger loggerEvents = Logger.getLogger("gui.events");

    int indexes[];
    /** list of filters */
    Vector filters = new Vector();
   
    public TableFilter() {
        indexes = new int[0]; // for consistency
    }

    public TableFilter(ExtendedTableModel model, Collection filteredColumns) {
        setModel(model);
        Iterator it = filteredColumns.iterator();
        while (it.hasNext()) {
            FieldItem field = (FieldItem)it.next();
            int index = getColumnIndex(field);
            if (index!=-1)
                filters.add(new FilterCriteria(index,field));
            else
                logger.warn("Ignoring filter on field "+field+" since it's not a column of the table");
        }
    }

    public void setModel(ExtendedTableModel model) {
        super.setModel(model); 
        reallocateIndexes(); 
        defaultFilter();
    }

    public int getRowCount() {
        return indexes.length; 
    }

    /**
     * Sets the filter for the collection from the context or from
     * RTTI configuration.  
     */
    public void defaultFilter() {
        // First see if there's a config in the context
        HashMap map = (HashMap)Collaboration.get().getAttribute(GuiAC.TABLE_FILTER);
        if (map != null) {
            FilterCriteria criteria = (FilterCriteria)map.get(getCollection());
            if (criteria != null) {
                logger.debug("Using filter criteria from context: "+criteria);
                filter(criteria);
                return;
            }
        }
    }


    /**
     * Reset to default unsorted order of the model.
     */
    public void reallocateIndexes() {
        int rowCount = model.getRowCount();
      
        logger.debug(this+".reallocateIndexes "+rowCount);

        // Set up a new array of indexes with the right number of elements
        // for the new data model.
        indexes = new int[rowCount];
      
        // Initialise with the identity mapping.
        for (int row = 0; row < rowCount; row++) {
            indexes[row] = row;
        }
    }

    public int getActualIndex(int row) {
        return indexes[row];
    }

    public void tableChanged(TableModelEvent e) {
        if (e.getType()==TableModelEvent.INSERT || 
            e.getType()==TableModelEvent.DELETE) {
            logger.debug(this+".tableChanged "+
                      (e.getType()==TableModelEvent.DELETE?"DELETE":"INSERT")+
                      " "+e.getFirstRow()+"-"+e.getLastRow());
            reallocateIndexes();
            filter();
            super.tableChanged(e);
        } else {
            logger.debug(this+".tableChanged UPDATE "+
                      e.getFirstRow()+"-"+e.getLastRow());
            filter();
            super.tableChanged(e);
        }
    }

    public void filter() {
        logger.debug("Filtering with "+filters);
        int rowCount = model.getRowCount();
        int filteredRowCount = 0;
        int tmp[] = new int[rowCount];
        for (int i=0; i<rowCount; i++) {
            Iterator it = filters.iterator();
            boolean keep = true;
            while (it.hasNext()) {
                FilterCriteria filter = (FilterCriteria)it.next();
                if (!filter.match(model,i)) {
                    keep = false;
                    break;
                }
            }
            if (keep) {
                logger.debug("  keeping row "+i);
                tmp[filteredRowCount] = i;
                filteredRowCount++;
            }
        }
        indexes = new int[filteredRowCount];
        System.arraycopy(tmp, 0, indexes, 0, filteredRowCount);
    }

    public void filter(FilterCriteria filter) {
        filters.clear();
        filters.add(filter);
        filter();
    }

    public void checkModel() {
    }

    // The mapping only affects the contents of the data rows.
    // Pass all requests to these rows through the mapping array: "indexes".
   
    public Object getValueAt(int aRow, int aColumn) {
        checkModel();
        if(indexes.length>aRow) {
            logger.debug("getValueAt("+aRow+","+aColumn+") -> "+
                      model.getValueAt(indexes[aRow], aColumn));
            return model.getValueAt(indexes[aRow], aColumn);
        } else {
            return null;
        }
    }
   
    public Object getObject(int row) {
        checkModel();
        return model.getObject(indexes[row]);
    }

    public int indexOf(Object object) {
        checkModel();
        return indexes[model.indexOf(object)];
    }

    public Object getObject(int row, int column) {
        checkModel();
        return model.getObject(indexes[row],column);
    }

    public void setValueAt(Object aValue, int aRow, int aColumn) {
        checkModel();
        model.setValueAt(aValue, indexes[aRow], aColumn);
    }

    public boolean isFiltered(FieldItem field) {
        Iterator it = filters.iterator();
        while (it.hasNext()) {
            FilterCriteria filter = (FilterCriteria)it.next();
            if (getMembers()[filter.getColumn()]==field)
                return true;
        }
        return false;
    }

    /**
     * Sets the value of the filter of a field
     * @param field the field whose filter to change
     * @param value the value of the filter
     */
    public void setFilterValue(FieldItem field, Object value) {
        Iterator it = filters.iterator();
        while (it.hasNext()) {
            FilterCriteria filter = (FilterCriteria)it.next();
            if (getMembers()[filter.getColumn()]==field) {
                filter.setValue(value);
                return;
            }
        }
        logger.warn("setFilterValue: no filter for the field "+field);
    }

    /**
     * Build filter editor components for each filtered column
     * @return a Map: FieldItem -> editor component
     */
    public Map buildFilterEditors(ViewFactory factory, DisplayContext context) {
        Hashtable editors = new Hashtable();
        filterEditors = new Hashtable();
        Iterator it = filters.iterator();
        while (it.hasNext()) {
            FilterCriteria filter = (FilterCriteria)it.next();
            FieldItem field = filter.getField();
            FieldEditor editor;
            Enum enum = GuiAC.getEnum(field);
            if (enum==null) {
                ClassItem type;
                if (field instanceof CollectionItem) 
                    type = ((CollectionItem)field).getComponentType();
                else
                    type = field.getTypeItem();
                editor = 
                    GenericFactory.createReferenceEditor(
                        factory,context,
                        null,null,
                        "filter "+field.getName(),
                        type,
                        null,
                        true, GuiAC.getLabelAll(),
                        false);
            } else {
                editor = GenericFactory.createEnumEditor(
                    factory, context,
                    null, null,
                    "filter "+field.getName(),
                    enum, true, GuiAC.getLabelAll());
            }
            editors.put(field,editor);
            ComboBoxModel model = ((ReferenceEditor)editor).getModel();
            model.setSelectedObject(null);
            filterEditors.put(model,filter);
            model.addListDataListener(this);
            context.addEditor(editor);
        }
        return editors;
    }

    // ComboBoxModel -> FilterCriteria
    Hashtable filterEditors;

    // Implementation of javax.swing.event.ListDataListener

    public void intervalAdded(ListDataEvent event) {
        loggerEvents.debug("intervalAdded: "+event+" from "+event.getSource());
    }

    public void intervalRemoved(ListDataEvent event) {
        loggerEvents.debug("intervalRemoved: "+event+" from "+event.getSource());
    }

    public void contentsChanged(ListDataEvent event) {
        loggerEvents.debug("contentsChanged: "+event+" from "+event.getSource());
        ComboBoxModel model = (ComboBoxModel)event.getSource();
        FilterCriteria filter = (FilterCriteria)filterEditors.get(model);
        Object value = model.getSelectedObject();
        if (value==null) {
            filter.setActive(false);
        } else {
            filter.setActive(true);
            filter.setValue(value);
        }
        tableChanged(new TableModelEvent(this));
    }
    

    /**
     * Save the sort criteria in the context
     * @param criteria the sort criteria to save
     */
    /*
    protected void saveFilterCriteria(FilterCriteria criteria) {
        //      new Exception().printStackTrace();
        HashMap map = (HashMap)Collaboration.get().getAttribute(GuiAC.TABLE_FILTER);
        if (map == null) {
            map = new HashMap();
            Collaboration.get().addAttribute(GuiAC.TABLE_FILTER, map);
        }
        map.put(getCollection(), criteria);
    }
    */
}
