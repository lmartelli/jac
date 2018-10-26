package org.objectweb.jac.aspects.gui;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.rtti.CollectionItem;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.MemberItem;

/** 
 * In a chain of data manipulators some behaviour is common. TableMap
 * provides most of this behavour and can be subclassed by filters
 * that only need to override a handful of specific methods. TableMap 
 * implements TableModel by routing all requests to its model, and
 * TableModelListener by routing all events to its listeners. Inserting 
 * a TableMap which has not been subclassed into a chain of table filters 
 * should have no effect.
 *
 * @version 1.4 12/17/97
 * @author Philip Milne */

class TableMap extends AbstractTableModel 
    implements TableModelListener, ExtendedTableModel 
{
    static Logger logger = Logger.getLogger("gui.model");

    protected ExtendedTableModel model; 

    public ExtendedTableModel getModel() {
        return model;
    }

    public void setModel(ExtendedTableModel model) {
        if (this.model!=null)
            this.model.removeTableModelListener(this); 
        this.model = model; 
        model.addTableModelListener(this); 
    }

    // By default, implement TableModel by forwarding all messages 
    // to the model. 

    public CollectionItem getCollection() {
        return model.getCollection();
    }

    public Object getValueAt(int aRow, int aColumn) {
        return model.getValueAt(aRow, aColumn); 
    }

    public Object getObject(int row) {
        return model.getObject(row);
    }
        
    public Object getObject(int row, int column) {
        return getObject(row,column);
    }

    public int indexOf(Object object) {
        return model.indexOf(object);
    }
        
    public void setValueAt(Object aValue, int aRow, int aColumn) {
        model.setValueAt(aValue, aRow, aColumn); 
    }

    public int getRowCount() {
        return (model == null) ? 0 : model.getRowCount(); 
    }

    public int getColumnCount() {
        return (model == null) ? 0 : model.getColumnCount(); 
    }
        
    public String getColumnName(int aColumn) {
        return model.getColumnName(aColumn); 
    }

    public Class getColumnClass(int aColumn) {
        return model.getColumnClass(aColumn); 
    }
        
    public boolean isCellEditable(int row, int column) { 
        return model.isCellEditable(row, column); 
    }

    public Object getCellRenderer(View tableView, int column, 
                                  ViewFactory factory, DisplayContext context) {
        return model.getCellRenderer(tableView, column,factory,context);
    }

    public MemberItem[] getMembers() {
        return model.getMembers();
    }

    public String[] getHeaders() {
        return model.getHeaders();
    }

    public int getColumnIndex(FieldItem field) {
        return model.getColumnIndex(field);
    }

    //
    // Implementation of the TableModelListener interface, 
    //
    // By default forward all events to all the listeners. 
    public void tableChanged(TableModelEvent e) {
        fireTableChanged(e);
    }

    // Model interface

    public void close() {
        ((Model)model).close();
    }

    public TableFilter getFilter() {
        if (this instanceof TableFilter)
            return (TableFilter)this;
        else
            return model.getFilter();
    }

    public TableSorter getSorter() {
        logger.debug(this+".getSorter");
        if (this instanceof TableSorter)
            return (TableSorter)this;
        else
            return model.getSorter();
    }
}
