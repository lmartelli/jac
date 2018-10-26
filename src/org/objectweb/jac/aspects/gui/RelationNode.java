/*
  Copyright (C) 2002 Renaud Pawlak, Laurent Martelli
  
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
import org.objectweb.jac.core.rtti.CollectionItem;
import org.objectweb.jac.core.rtti.FieldItem;

/**
 * This tree node represents a relation. */ 

public class RelationNode extends AbstractNode implements CollectionUpdate {
    static Logger logger = Logger.getLogger("gui.treeview");
    static Logger loggerEvents = Logger.getLogger("gui.events");

    Object substance;
   
    /**
     * Constructs the node.
     *
     * @param model the tree model
     * @param substance the object that holds the relation
     * @param relation the substance relation */

    public RelationNode(TreeView model, 
                        Object substance, FieldItem relation) {
        super(model,relation,true);
        this.substance = substance;
        rebuildData();
    }

    /**
     * Rebuild the node's data. */

    protected void rebuildData() {
        Object object = getUserObject();
        logger.debug("rebuildData on "+this);
        if ( object instanceof FieldItem ) {
            text = ((FieldItem)object).getName();
            icon = GuiAC.getIcon((FieldItem)object);
        }
    }

    /**
     * Unregister all the events this node is listening to. */
    public void unregisterEvents() {
        Utils.unregister(substance,this);
    }

    // CollectionUpdate interface

    public void onChange(Object substance, CollectionItem collection, 
                         Object value, Object param) {
        loggerEvents.debug("collectionUpdated");
        int[] indices = new int[getChildCount()];
        for(int i=0;i<indices.length;i++) {
            indices[i]=i;
        }
        AbstractNode[] removedNodes = new AbstractNode[getChildCount()];
        for(int i=0;i<indices.length;i++) {
            removedNodes[i]=(AbstractNode)getChildAt(i);
        }
        removeAllChildren();
        model.nodesWereRemoved(this,indices,removedNodes);
    }

    public void onAdd(Object substance, CollectionItem collection, 
                      Object value, Object added, Object param) {
        onChange(substance,collection,value,param);
    }

    public void onRemove(Object substance, CollectionItem collection, 
                         Object value, Object removed, Object param) {
        onChange(substance,collection,value,param);
    }

    public String toString() {
        return "RelationNode["+getUserObject()+"]";
    }
}
