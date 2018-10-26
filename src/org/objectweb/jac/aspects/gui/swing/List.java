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
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA */

package org.objectweb.jac.aspects.gui.swing;

import org.objectweb.jac.aspects.gui.*;
import org.objectweb.jac.core.rtti.CollectionItem;
import java.awt.Point;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;

public class List extends AbstractCollection
    implements ListSelectionListener
{
    // swing table component
    JList list;

    public List(ViewFactory factory, DisplayContext context,
                CollectionItem collection, Object substance, CollectionModel model,
                org.objectweb.jac.aspects.gui.CollectionItemView itemView) {
        super(factory,context,collection,substance,model,itemView);
    }

    protected JComponent getInnerComponent(Model model) {
        if (list==null) {
            list = new JList();
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            BetterListSelectionModel selModel = new BetterListSelectionModel(list);
            list.setSelectionModel(selModel);
            selModel.addListSelectionListener( this );
            list.setModel((ListModel)model);
        }
        return list;
    }

    protected void onRemove() {
        list.clearSelection();
    }

    /**
     * Returns an array of the selected objects. The array is empty if
     * no object is selected, but not null.
     */
    protected int[] getSelectedIndices() {
        return list.getSelectedIndices();
    }

    protected CollectionUpdate getCollectionUpdate() {
        return (CollectionUpdate)model;
    }

    int locationToIndex(Point location) {
        return list.locationToIndex(location);
    }

    protected ListSelectionModel getSelectionModel() {
        return list.getSelectionModel();
    }

    // CollectionView interface

    public void setSelected(int index) {
        list.setSelectedIndex(index);
    }

}
