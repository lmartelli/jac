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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.NameRepository;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.core.rtti.CollectionItem;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.MetaItem;

/**
 * A data model for trees.
 */

public class TreeModel extends DefaultTreeModel implements TreeView {
    static Logger logger = Logger.getLogger("gui.treeview");
    static Logger loggerPerf = Logger.getLogger("perf");

    Vector treeObjects = new Vector();
    Vector treeNodes = new Vector();
    boolean showRelations;
    String rootObjects;

    /**
     * Constructs a new tree model.
     *
     * @param rootNode the root node
     * @param rootObjects ???
     * @param showRelations tells if the tree should show the
     * interobjects relations as nodes */
    public TreeModel(RootNode rootNode, 
                     String rootObjects, boolean showRelations) {
        super(rootNode);
        this.showRelations = showRelations;
        this.rootObjects = rootObjects;
        rootNode.setModel(this);
        initTree(this,rootObjects,showRelations);
    }

    public void setRootNode(AbstractNode root) {
        setRoot(root);
    }

    public void addNode(AbstractNode parent, AbstractNode child) {
        logger.debug("addNode "+child+" to "+parent);
        parent.add(child);
    }

    public void setSelection(TreePath selection) {
        Object[] listeners = listenerList.getListenerList();
        TreeSelectionEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==TreeListener.class) {
                // Lazily create the event:
                ((TreeListener)listeners[i+1]).setSelection(selection);
            }          
        }
    }

    public void addTreeListener(TreeListener listener) {
        listenerList.add(TreeListener.class,listener);
    }

    public void unregisterEvents() {
        Iterator i = treeNodes.iterator();
        while (i.hasNext()) {
            AbstractNode node = (AbstractNode)i.next();
            node.unregisterEvents();
        }
    }

    /**
     * Add the children nodes for a node.
     *
     * @param rootNode the parent node for which to add children nodes.
     * @param o the object represented by the rootNode
     */
    public static void addNodes(TreeView tree, ObjectNode rootNode,
                                Object o, boolean showRelations) {
        logger.debug("addNodes("+rootNode+","+o+", showRelations="+showRelations+")...");
        if (o==null)
            return;
        long start = System.currentTimeMillis();
        // iterates on all the relations
        ClassItem cli = ClassRepository.get().getClass(o);
        FieldItem[] rels = (FieldItem[])cli.getAttribute(GuiAC.TREE_ATTRIBUTES_ORDER);
        Collection related = null;
        rootNode.setChildrenUptodate(true);
        if (rels==null)
            return;
        for (int itRel=0; itRel<rels.length; itRel++) {
            FieldItem rel = rels[itRel];
            if (!GuiAC.isVisible(o,(MetaItem)rel)) 
                continue;
            if (rel instanceof CollectionItem) {
                CollectionItem collection = (CollectionItem)rel;
                Collection cRel = collection.getActualCollectionThroughAccessor(o);
                related = cRel;
            
                if (showRelations && 
                    collection.getAttribute(GuiAC.HIDDEN_TREE_RELATION)==null) {
                    // add a collection node
                    RelationNode relationNode = 
                        new RelationNode(tree,o,collection); 
                    logger.debug("adding relation node for collection "+collection.getName());
                    Utils.registerCollection(o,collection,relationNode);
                    rootNode.add(relationNode);
                    Iterator it = related.iterator();
                    while (it.hasNext()) {
                        Object newObject = it.next();
                        ObjectNode newNode = new ObjectNode(tree,newObject,o,collection,showRelations); 
                        newNode.setLeaf(isLeafNode(tree,newNode,o,showRelations));
                        relationNode.add(newNode);
                    }
                } else {
                    logger.debug("adding nodes for collection "+collection.getName()+": "+
                              Arrays.asList(related.toArray()));
                    Utils.registerCollection(o,collection,rootNode);
                    // recursively add nodes
                    Iterator it = related.iterator();
                    while (it.hasNext()) {
                        Object newObject = it.next();
                        logger.debug("adding node for collection "+
                                  collection.getName()+": "+newObject);
                        ObjectNode newNode = new ObjectNode(tree,newObject,o,collection,showRelations); 
                        newNode.setLeaf(isLeafNode(tree,newNode,newObject,showRelations));
                        rootNode.addNode(newNode);                  
                    }
                }
            } else if (rel instanceof FieldItem) {
                if (showRelations &&
                    rel.getAttribute(GuiAC.HIDDEN_TREE_RELATION)==null) 
                { 
                    RelationNode relationNode = 
                        new RelationNode(tree,o,rel);
                    rootNode.add(relationNode);
                    Object relatedObject = rel.getThroughAccessor(o);
                    if (relatedObject!=null) {
                        ObjectNode newNode = new ObjectNode(tree,relatedObject,o,rel,showRelations); 
                        newNode.setLeaf(isLeafNode(tree,newNode,relatedObject,showRelations));
                        relationNode.add(newNode);
                    }
                } else {
                    Object relatedObject = rel.getThroughAccessor(o);
                    if (relatedObject!=null) {
                        ObjectNode newNode = new ObjectNode(tree,relatedObject,o,rel,showRelations); 
                        newNode.setLeaf(isLeafNode(tree,newNode,relatedObject,showRelations));
                        rootNode.addNode(newNode);
                    }
                }
            }
        }
        loggerPerf.info("Added nodes in "+(System.currentTimeMillis()-start)+"ms");
    }


    /**
     * Tells wether a node is a leaf or not, without computing all its
     * children.
     *
     * @param tree The tree the node belongs to
     * @param node the node
     * @param o the object represented by the node
     * @param showRelations wether to show a node for relations
     */
    public static boolean isLeafNode(TreeView tree, ObjectNode node,
                                     Object o, boolean showRelations) {
        if (o==null) 
            return true;
        // iterates on all the relations
        ClassItem cli = ClassRepository.get().getClass(o);
        FieldItem[] rels = (FieldItem[])cli.getAttribute(GuiAC.TREE_ATTRIBUTES_ORDER);
        Collection related = null;
        boolean isLeaf=true;
        if (rels==null)
            return true;
        for (int itRel=0; itRel<rels.length; itRel++) {
            FieldItem rel = rels[itRel];
            if (!GuiAC.isVisible(o,(MetaItem)rel)) 
                continue;
            if (rel instanceof CollectionItem) {
                CollectionItem collection = (CollectionItem)rel;
                Utils.registerCollection(o,collection,node);
                if (isLeaf && 
                    !collection.getActualCollectionThroughAccessor(o).isEmpty()) {
                    // !! we must not return here because the loop is also used
                    // to register for updates (note that this should be done in
                    // addNodes)
                    isLeaf=false;
                }
			 } else {
                 Utils.registerField(o,rel,node);
				 if (isLeaf && rel.getThroughAccessor(o)!=null) {
					isLeaf=false;
				 }
			 }
        }
        return isLeaf;
    }

    /**
     * Initialize a tree view.
     * @param tree the tree view to initialize
     * @param rootObjects a regular expression designating root objects
     * @param showRelations wether to build nodes for the relations themselves
     */
    public static void initTree(TreeView tree, String rootObjects, 
                                boolean showRelations) {
        logger.debug("initTree "+rootObjects+"...");
         
        // the first accessible collection
        AbstractNode rootNode = new RootNode();
        tree.setRootNode(rootNode);
        Iterator it = NameRepository.getObjects(rootObjects).iterator();
        while (it.hasNext()) {
            Object object = it.next();
            ObjectNode objectNode = new ObjectNode(tree,object,null,null,showRelations); 
            rootNode.add(objectNode);
            objectNode.setLeaf(isLeafNode(tree,objectNode,object,showRelations));
        }

        logger.debug("initTree "+rootObjects+" DONE");
    }

}
