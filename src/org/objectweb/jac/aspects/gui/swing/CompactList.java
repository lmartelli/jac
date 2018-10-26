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
import org.objectweb.jac.core.rtti.FieldItem;
import javax.swing.JList;

/**
 * Base class to implement ListView and TableView
 */
public class CompactList extends AbstractView
    implements CollectionUpdate
{
    CollectionItem collection;
    Object substance;
    CollectionModel model;

    JList list;

    public CollectionModel getCollectionModel() {
        return model;
    }

    public CompactList(ViewFactory factory, DisplayContext context,
                       CollectionItem collection, Object substance,
                       CollectionModel model) {
        super(factory,context);
        this.collection = collection;
        this.substance = substance;
        this.model = model;

        list = new JList();
        list.setModel((ListModel)model);
        add(list);
      
        Utils.registerCollection(substance,collection,this);
    }

    public void close(boolean validate) {
        closed = true;
        model.close();
    }

    protected void setNoRefresh(boolean norefresh) {
        if (norefresh==false) {
            repaint();
        }
    }

    public void setField(FieldItem field) {
        collection = (CollectionItem)field;
    }

    public void setSubstance(Object substance) {
        this.substance = substance;
    }

    public FieldItem getField() {
        return collection;
    }

    public void setValue(Object value) {
    }

    public void updateModel(Object substance) {
    }

    // CollectionUpdate interface

    public void onChange(Object substance, CollectionItem collection, 
                         Object value, Object param) {
    }

    public void onAdd(Object substance, CollectionItem collection, Object value,
                      Object added, Object param) {
    }

    public void onRemove(Object substance, CollectionItem collection, Object value,
                         Object removed, Object param) {
    }

}
