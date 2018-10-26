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

import javax.swing.table.TableModel;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.MemberItem;

/**
 * Data model for tables.
 */
public interface ExtendedTableModel extends TableModel, CollectionModel {

    /**
     * Returns the members that are displayed in the table.
     *
     * @return one member per column */
    MemberItem[] getMembers();
   
    /**
     * Gets the headers' titles.
     *
     * @return one title per column */
    String[] getHeaders();

    int getColumnIndex(FieldItem field);

    /**
     * Gets the value at a given row, column couple. */
    Object getObject(int row, int column);

    /**
     * Build a cell render for a given column.
     *
     * @param tableView the actual table view
     * @param column the column for which to build the viewer
     * @param factory the factory to use 
     * @param context the display context
     */
    Object getCellRenderer(View tableView, int column, 
                           ViewFactory factory, DisplayContext context);

    TableFilter getFilter();
    TableSorter getSorter();
}

