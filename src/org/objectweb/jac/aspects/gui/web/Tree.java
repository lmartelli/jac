/*
  Copyright (C) 2002-2003 Laurent Martelli <laurent@aopsys.com>
  
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

package org.objectweb.jac.aspects.gui.web;

import org.objectweb.jac.aspects.gui.*;
import org.objectweb.jac.core.Wrappee;
import org.objectweb.jac.core.rtti.CollectionItem;
import java.io.PrintWriter;
import java.util.HashSet;

/**
 * This class defines a Swing component tree view for objects that are
 * related to a root object through relations or collections.
 *
 * @see GuiAC */

public class Tree extends AbstractView
    implements View, HTMLViewer, TreeListener
{
    TreeModel model;
    boolean showRelations = true;
    RootNode rootNode = null;
    HashSet expandedNodes = new HashSet();

    /**
     * Constructs a new tree view.
     * @param pathDef designate root objects of the tree
     * @param showRelations wether to build a node for relation items
     */
    public Tree(ViewFactory factory, DisplayContext context,
                String pathDef, boolean showRelations) {
        super(factory,context);
        this.showRelations = showRelations;

        rootNode = new RootNode();
        model = new TreeModel( rootNode, pathDef, showRelations );
    }
    // interface TreeView

    public void close(boolean validate) {
        model.unregisterEvents();
    }

    public void genHTML(PrintWriter out) {
        genNode(out,(AbstractNode)model.getRoot(),"");
    }

    protected boolean isExpanded(AbstractNode node) {
        return expandedNodes.contains(node);
    }

    protected void genNode(PrintWriter out, AbstractNode node, String curPath) {
        String nodePath = node instanceof RootNode ? "/" : curPath;
        String nodeClass = node.isLeaf() ? "leafNode" : "treeNode";
        boolean isRoot = node == model.getRoot();

        if (!isRoot) {
            out.println("<div class=\""+nodeClass+"\">");
            if (!node.isLeaf()) {
                if (isExpanded(node)) 
                    out.print("  <a href=\""+eventURL("onCollapseNode")+
                              "&amp;nodePath="+nodePath+"\" class=\"fixed\">[-]</a>");
                else 
                    out.print("  <a href=\""+eventURL("onExpandNode")+
                              "&amp;nodePath="+nodePath+"\" class=\"fixed\">[+]</a>");
            }
            out.println(iconElement(node.getIcon(),"")+
                        " <a href=\""+eventURL("onSelectNode")+"&amp;nodePath="+
                        nodePath+"\">"+node.getText()+"</a>");
        }

        if (isExpanded(node) || isRoot) {
            out.println("  <div class=\""+(isRoot?"rootNodes":"nodes")+"\">");
            for (int i=0; i<node.getChildCount();i++) {
                genNode(out,(AbstractNode)node.getChildAt(i),curPath+"/"+i);
            }
            out.println("  </div>");
        }
        if (!isRoot) {
            out.println("</div>");
        }
    }

    // TreeListener interface

    public void onSelectNode(String nodePath) {
        try {
            AbstractNode node = pathToNode(nodePath);
            logger.debug("onSelectNode path="+nodePath+" -> "+node);
            if (node == null) 
                return;
            logger.debug("  node="+node);
            Object selected = node.getUserObject();
            logger.debug("  selected="+selected);
            if (selected instanceof CollectionItem) {
                context.getDisplay().refresh();
            } else {
                EventHandler.get().onNodeSelection(context,node,true);
            }
        } catch (Exception e) {
            context.getDisplay().showError("Error",e.toString());
        }
    }

    public void onExpandNode(String nodePath) {
        try {
            AbstractNode node = pathToNode(nodePath);
            // ensure that the children of this node are uptodate
            if (node instanceof ObjectNode)
                ((ObjectNode)node).updateChildren();
            expandedNodes.add(node);
        } finally {
            context.getDisplay().refresh();
        }
    }

    public void onCollapseNode(String nodePath) {
        try {
            AbstractNode node = pathToNode(nodePath);
            expandedNodes.remove(node);
        } finally {
            context.getDisplay().refresh();
        }
    }

    /*
     * Get a Node from its path. 
     */
    protected AbstractNode pathToNode(String nodePath) {
        AbstractNode node = (AbstractNode)model.getRoot();
        if (nodePath.startsWith("/")) {
            nodePath = nodePath.substring(1);
            int index = nodePath.indexOf("/");
            while (index!=-1) {
                node = (AbstractNode)node.getChildAt(
                    Integer.parseInt(nodePath.substring(0,index)));
                nodePath = nodePath.substring(index+1);
                index = nodePath.indexOf("/");
            } 
            if (nodePath.length()>0)
                node = (AbstractNode)node.getChildAt(Integer.parseInt(nodePath));
        }
        return node;
    }
}
