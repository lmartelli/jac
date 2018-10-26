/*
  Copyright (C) 2002-2003 Renaud Pawlak <renaud@aopsys.com>,
                          Laurent Martelli <laurent@aopsys.com>
  
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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.Collaboration;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.core.rtti.CollectionItem;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.util.Stack;

/**
 * This class represents a tree node for an object. */

public class ObjectNode extends AbstractNode 
    implements ObjectUpdate, FieldUpdate, CollectionUpdate 
{
    static Logger logger = Logger.getLogger("gui.treeview");
    static Logger loggerEvents = Logger.getLogger("gui.events");
    static Logger loggerCol = Logger.getLogger("gui.collectionupdate");

    FieldItem relation;
    Object substance;

    /**
     * Constructor.
     *
     * @param model the tree modelto notify when changes occur
     * @param value the value that the node represents
     * @param substance the owner of the relation
     * @param relation the relation the value is part of
     */
    public ObjectNode(TreeView model, Object value, 
                      Object substance, FieldItem relation, 
                      boolean showRelations) 
    {
        super(model,value, showRelations);
        this.substance = substance;
        this.relation = relation;
        Utils.registerObject(value,this);
        if (!(relation instanceof CollectionItem)) {
            Utils.registerField(value,relation,this);
        }
        if (GuiAC.getGraphicContext()!=null)
            context.addAll(GuiAC.getGraphicContext());
        if (relation!=null)
            context.push(relation);
        rebuildData();
    }

    /** FieldItem -> Integer(Index of firt node of the relation) */
    Hashtable relationIndices;

    /**
     * Insert a node at the correct place (considering sorting)
     * @param node the node to insert
     * @return the position the node was inserted at
     */
    public int addNode(ObjectNode node) {
        logger.debug("Inserting "+node.getText()+"...");
        FieldItem relation = node.getRelation();
        if (relationIndices==null)
            relationIndices = new Hashtable();
        Integer relIndex = (Integer)relationIndices.get(relation);
        if (relIndex==null) {
            relIndex = new Integer(getChildCount());
            relationIndices.put(relation,relIndex);
        }
        // Find where to insert the node (sort according to text)
        int i;
        for (i=relIndex.intValue(); i<getChildCount(); i++) {
            ObjectNode current = (ObjectNode)getChildAt(i);
            logger.debug("  current "+current);
            if (current.getText().compareToIgnoreCase(node.getText())>0)
                break;
        }
        logger.debug("Inserting "+node.getText()+" at "+relation.getName()+"["+i+"]");
        insert(node, i);
        
        // Update relation start indices
        Iterator it = relationIndices.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            int index = ((Integer)entry.getValue()).intValue();
            if (index>=i && entry.getKey()!=relation) {
                entry.setValue(new Integer(index+1));
            }
        }
        return i;
    }

    public void removeAllChildren() {
        super.removeAllChildren();
        if (relationIndices!=null)
            relationIndices.clear();
    }

    Stack context = new Stack();

    /**
     * Returns the relation (reference or collection) the substance of
     * the node belongs to. */
    public FieldItem getRelation() {
        return relation;
    }

    /**
     * Returns the substance of this node. */
    public Object getSubstance() {
        return substance;
    }

    /**
     * Rebuild the data of this node again. */

    protected void rebuildData() {
        Object object = getUserObject();
        logger.debug("refresh("+object+")");
        if (object==null) {
            icon = null;
            text = "<null>";
            tooltip = null;
        } else {
            text = GuiAC.toString(object,context);
            tooltip = GuiAC.getToolTip(object,context);
            logger.debug("text="+text);
            icon = GuiAC.getIcon(ClassRepository.get().getClass(object),object);
        }
    }

    /**
     * Unregisters from the events this node is notified. */

    public void unregisterEvents() {
        Object value = getUserObject();
        Utils.unregister(value,this);
    }

    /**
     * Find a node in the children.
     * @param relation the relation the requested node must be part of
     * @param userObject the userObject value the requested node must have
     * @return an ObjectNode with the requested features, or null if no
     * such node can be found.
     */
    protected ObjectNode findNode(FieldItem relation, Object userObject) {
        logger.debug("Looking for node "+relation+" -> "+userObject);
        for (int i=0; i<getChildCount(); i++) {
            if (getChildAt(i) instanceof ObjectNode) {
                ObjectNode current = (ObjectNode)getChildAt(i);
                if (current.getUserObject()==userObject &&
                    current.getRelation()==relation)
                    return current;
            }
        }
        return null;
    }

    // ObjectUpdate interface

    public void objectUpdated(Object substance, Object param) {
        rebuildData();
        model.nodeChanged(this);
    }

    // FieldUpdate interface

    public void fieldUpdated(Object substance, FieldItem collection, 
                             Object value, Object param) {
        logger.debug("fieldUpdated on "+this);
        rebuildData();
        TreeModel.addNodes(model,this,getUserObject(),showRelations);
        ObjectNode newNode = findNode(collection,value);
        //model.nodesWereInserted(this,indices);
        if (newNode!=null) {
            model.setSelection(new TreePath(newNode.getPath()));
        }
        model.nodeChanged(this);
    }
    
    // CollectionUpdate interface

    public void onChange(Object substance, CollectionItem collection, 
                         Object value, Object param) {
        loggerCol.debug("ObjectNode collectionUpdated "+collection.getLongName());
        // remove all children nodes
        int[] indices = new int[getChildCount()];
        AbstractNode[] removedNodes = new AbstractNode[getChildCount()];
        for(int i=0; i<indices.length; i++) {
            indices[i] = i;
            removedNodes[i] = (AbstractNode)getChildAt(i);
        }
        removeAllChildren();
        model.nodesWereRemoved(this,indices,removedNodes);

        // rebuild children nodes
        TreeModel.addNodes(model,this,getUserObject(),showRelations);

        indices = new int[getChildCount()];
        for(int i=0;i<indices.length;i++) {
            indices[i] = i;
        }
        model.nodesWereInserted(this,indices);
    }

    public void onAdd(Object substance, CollectionItem collection, 
                      Object value, Object added, Object param) {
        loggerCol.debug("onAdd "+collection.getLongName()+" "+added+" - "+areChildrenUptodate);
        ObjectNode newNode;
        if (areChildrenUptodate) {
            newNode = new ObjectNode(model,added,substance,collection,showRelations); 
            newNode.setLeaf(TreeModel.isLeafNode(model,newNode,added,showRelations));
            int pos = addNode(newNode);
            model.nodesWereInserted(this,new int[] {pos});
            setChildrenUptodate(true);
        } else {
            TreeModel.addNodes(model,this,getUserObject(),showRelations);
            int[] indices = new int[getChildCount()];
            for(int i=0; i<indices.length; i++) {
                indices[i] = i;
            }
            newNode = findNode(collection,added);
            model.nodesWereInserted(this,indices);
        }
        if (newNode!=null) {
            model.setSelection(new TreePath(newNode.getPath()));
        }
    }

    public void onRemove(Object substance, CollectionItem collection, 
                         Object value, Object removed, Object param) {
        loggerCol.debug("onRemove "+collection.getLongName()+" "+removed);
        AbstractNode removedNode = 
            (AbstractNode)Collaboration.get().getAttribute(GuiAC.REMOVED_NODE);
        if (removedNode!=null) {
            loggerEvents.debug("removing = "+removedNode+" from "+this);
            int removedIndex = getIndex(removedNode);
            remove(removedNode);
            model.nodesWereRemoved(
                this,
                new int[] {removedIndex},
                new Object[] {removedNode});
        } else {
            onChange(substance,collection,value,param);
        }
    }

    public void updateChildren() {
        if (!areChildrenUptodate()) {
            loggerEvents.debug("updateChildren "+this);
            TreeModel.addNodes(model,this,getUserObject(),showRelations);
            int[] indices = new int[getChildCount()];
            for(int i=0; i<indices.length; i++) {
                indices[i] = i;
            }
            model.nodesWereInserted(this,indices);
            setChildrenUptodate(true);
        } else {
            loggerEvents.debug("children are uptodate for "+this);
        }
    }

    public String toString() {
        return substance+"."+relation+" -> "+getUserObject();
    }
}

