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

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * The GUI terget independant tree view. */

public interface TreeView {

   /**
    * Set the root node of the tree.
    * @param root the root node
    */
   void setRootNode(AbstractNode root);

   /**
    * Add a child node to a node
    * @param parent the node to add the child node to
    * @param child the node to add
    */
   void addNode(AbstractNode parent, AbstractNode child);

   /**
    * Upcalled when a substance changed for a given node.
    *
    * @param node the node that changed 
    */
   void nodeChanged(TreeNode node);

   /**
    * Upcalled when nodes where removed in this tree.
    * 
    * @param node the parent node from where the nodes were removed
    * @param indices the indices of the removed nodes
    * @param removedNodes the removed nodes 
    */ 
   void nodesWereRemoved(TreeNode node,int[] indices, Object[] removedNodes);

   /**
    * Upcalled when nodes where inserted in this tree.
    * 
    * @param node the parent node from where the nodes were inserted
    * @param indices the indices of the inserted nodes
    */ 
   void nodesWereInserted(TreeNode node,int[] indices);

   /**
    * Sets the selected node for this tree. 
    *
    * @param selectionPath a tree path indicating the selected node 
    */
   void setSelection(TreePath selectionPath);
}
