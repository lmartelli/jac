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

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * This tree node caches the text and the icon to display so that
 * calls to the wrappee are not needed every time tree is painted
 */
public abstract class AbstractNode extends DefaultMutableTreeNode {
    String icon;
    String text;
    String tooltip;
    TreeView model;
    boolean showRelations = false;
    boolean isLeaf = true;
    boolean areChildrenUptodate = false;

    public AbstractNode() {
    }

    public AbstractNode(TreeView model, Object object, boolean showRelations) {
        super(object);
        this.model = model;
        this.showRelations = showRelations;
    }

    /**
     * Returns true if this node is a leaf of the tree that holds
     * it. */
    public boolean isLeaf() {
        return super.isLeaf() && isLeaf;
    }
   
    /**
     * Sets this node to be a leaf or not of the tree.
     *
     * @param isLeaf true => leaf
     * @see #isLeaf() */
    public void setLeaf(boolean isLeaf) {
        this.isLeaf = isLeaf;
    }

    /**
     * Returns true if the children of this node are to be updated. */

    public boolean areChildrenUptodate() {
        return areChildrenUptodate;
    }

    /**
     * Sets the uptodate state of this node's children.
     *
     * @param value true => uptodate
     * @see #areChildrenUptodate */

    public void setChildrenUptodate(boolean value) {
        this.areChildrenUptodate = value;
    }

    /**
     * Sets the model (abstract tree representation) of this node. */

    public void setModel(TreeView model) {
        this.model = model;
    }

    /**
     * Gets the icon of this node (null if none). */

    public String getIcon() {
        return icon;
    }

    /**
     * Gets the text of this node (null is none). */

    public String getText() {
        return text;
    }

    public String getToolTip() {
        return tooltip;
    }

    /**
     * Unregister from all update events
     */
    public abstract void unregisterEvents();

    /**
     * Redefines the DefaultMutableTreeNode.setParent in order to
     * unregister the update events.
     *
     * @param parent the parent node
     * @see #unregisterEvents() */

    public void setParent(DefaultMutableTreeNode parent) {
        super.setParent(parent);
        if (parent==null) {
            unregisterEvents();
        }
    }
}
