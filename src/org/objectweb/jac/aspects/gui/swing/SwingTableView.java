/*
  Copyright (C) 2001-2003 Renaud Pawlak <renaud@aopsys.com>
                          Laurent Martelli  <laurent@aopsys.com>
  
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

package org.objectweb.jac.aspects.gui.swing;

import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import org.apache.log4j.Logger;
import org.objectweb.jac.aspects.gui.*;
import org.objectweb.jac.core.rtti.CollectionItem;
import org.objectweb.jac.core.rtti.MemberItem;

public class SwingTableView extends AbstractCollection
    implements ListSelectionListener
{
    static Logger logger = Logger.getLogger("gui.table");

    // swing table component
    JTable table;

    public SwingTableView(ViewFactory factory, DisplayContext context,
                          CollectionItem collection, Object substance,
                          ExtendedTableModel model, 
                          org.objectweb.jac.aspects.gui.CollectionItemView itemView) 
    {
        super(factory,context,collection,substance,model,itemView);
      
        // ensure that 'table' is not null
        getInnerComponent(model);
        setCellRenderers(table);
        table.setPreferredRowHeights(1);
        addMouseListenerToHeaderInTable((TableSorter)model);
    }

    /**
     * Sets some mouse listeners on the columns headers.  <p>"Click"
     * sorts in ascending order, "Shift+Click" sort in descending
     * order.
     */
    void addMouseListenerToHeaderInTable(final TableSorter sorter) { 
        final JTable tableView = table; 
        tableView.setColumnSelectionAllowed(false); 
        MouseAdapter listMouseListener = new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    TableColumnModel columnModel = tableView.getColumnModel();
                    int viewColumn = columnModel.getColumnIndexAtX(e.getX()); 
                    int column = tableView.convertColumnIndexToModel(viewColumn); 
                    if (e.getClickCount() == 1 && column != -1) {
                        //System.out.println("Sorting ..."); 
                        boolean shiftPressed = (e.getModifiers() & InputEvent.SHIFT_MASK) != 0;
                        if (shiftPressed) {
                            sorter.sortByColumn(column, false); 
                        } else {
                            SortCriteria criteria = sorter.getSortCriteria(column);
                            if (criteria!=null) {
                                sorter.sortByColumn(-1,criteria.isAscending());            
                            } else {
                                sorter.sortByColumn(column, true); 
                            }
                        }
                    }
                }
            };
        JTableHeader th = tableView.getTableHeader(); 
        th.addMouseListener(listMouseListener); 
    }

    /**
     * Set the cell renderer to use for each column of the table.
     */
    protected void setCellRenderers(JTable table) {
        ExtendedTableModel tableModel = (ExtendedTableModel)table.getModel();
        //      ClassItem[] viewerClasses = tableModel.getViewerClasses();
        MemberItem[] members = tableModel.getMembers();
        String[] headers = tableModel.getHeaders();
      
        for (int i=0; i<members.length; i++) {
            //if(members[i] instanceof FieldItem)
            Object cellRenderer = tableModel.getCellRenderer(this,i,factory,context);
            if (cellRenderer instanceof TableCellRenderer) {
                logger.debug("setCellRenderer for "+
                             members[i].getLongName()+" -> "+cellRenderer);
                table.getColumnModel().getColumn(i).setCellRenderer(
                    (TableCellRenderer)cellRenderer);
            } else {
                logger.debug("setCellRenderer for "+members[i].getLongName());
                table.getColumnModel().getColumn(i).setCellRenderer(new ToStringTableCellRenderer());
            }
        }
    }

    protected JComponent getInnerComponent(Model model) {
        if (table==null) {
            table = new JTable();
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            BetterListSelectionModel selModel = new BetterListSelectionModel(table);
            table.setSelectionModel(selModel);         
            table.setModel((javax.swing.table.TableModel)model);
            selModel.addListSelectionListener(this);
        }
        return table;
    }

    protected void onRemove() {
        table.clearSelection();
    }

    protected int[] getSelectedIndices() {
        return table.getSelectedRows();
    }

    protected Model getModel() {
        return (Model)table.getModel();
    }

    protected ListSelectionModel getSelectionModel() {
        return table.getSelectionModel();
    }

    protected CollectionUpdate getCollectionUpdate() {
        return (CollectionUpdate)((TableSorter)model).getModel();
    }

    int locationToIndex(Point location) {
        return table.rowAtPoint(location);
    }   

    // CollectionView interface

    public void setSelected(int index) {
        table.changeSelection(index,-1,false,false);
    }
}
