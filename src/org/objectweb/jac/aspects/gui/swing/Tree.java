/*
  Copyright (C) 2003-2003 Renaud Pawlak <renaud@aopsys.com>, 
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

package org.objectweb.jac.aspects.gui.swing;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.apache.log4j.Logger;
import org.objectweb.jac.aspects.gui.*;
import org.objectweb.jac.aspects.gui.Transfer;
import org.objectweb.jac.core.Collaboration;
import org.objectweb.jac.core.Wrappee;
import org.objectweb.jac.core.rtti.CollectionItem;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.MethodItem;

/**
 * This class defines a Swing component tree view for objects that are
 * related to a root object through relations or collections.
 *
 * @see GuiAC */

public class Tree extends AbstractView
    implements View, TreeSelectionListener, MouseListener, 
               TreeExpansionListener, TreeListener, KeyListener,
               DragGestureListener, DragSourceListener, DropTargetListener
{
    static Logger logger = Logger.getLogger("gui.treeview");

    JTree tree;
    TreeModel model;
    //   String pathDef = null;
    boolean showRelations = true;

    JButton viewButton = null;
    JButton newButton = null;
    JButton removeButton = null;

    RootNode rootNode = null;

    /**
     *  Builds a new tree view.
     *
     * @param pathDef designate root objects of the tree
     * @param showRelations wether to build a node for relation items
     */
    public Tree(ViewFactory factory, DisplayContext context,
                String pathDef, boolean showRelations ) {
        super(factory,context);

        this.showRelations = showRelations;

        setLayout(new BorderLayout());

        tree = new JTree();

        DragSource dragSource = DragSource.getDefaultDragSource();
        // creating the recognizer is all that's necessary - it
        // does not need to be manipulated after creation
        dragSource.createDefaultDragGestureRecognizer(
            tree, // component where drag originates
            DnDConstants.ACTION_COPY_OR_MOVE, // actions
            this); // drag gesture listener

        JScrollPane upperCont = new JScrollPane(tree);
        tree.addTreeExpansionListener(this);
        JPanel downCont = new JPanel();

        tree.putClientProperty("JTree.lineStyle", "Angled");
        ToolTipManager.sharedInstance().registerComponent(tree);
        tree.setRootVisible(false);
        tree.addTreeSelectionListener( this );      

        rootNode = new RootNode();
        model = new TreeModel(rootNode, pathDef, showRelations);
        tree.setModel(model);
        tree.setCellRenderer(new TreeNodeRenderer());
        tree.addMouseListener(this);
        tree.addKeyListener(this);
        model.addTreeListener(this);

        // add, remove and view buttons
        viewButton = createButton("view_icon","View",new openHandler());
        downCont.add(viewButton);

      
        newButton = createButton("new_icon","Add",new addHandler());
        downCont.add(newButton);

        removeButton = createButton(
            "remove_icon","Remove",         
            new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        doDelete(false);
                    }
                }
        );

        downCont.add(removeButton);

        add(upperCont, BorderLayout.CENTER);
        add(downCont, BorderLayout.SOUTH);

        new DropTarget(tree, // component
                       DnDConstants.ACTION_COPY_OR_MOVE, // actions
                       this); // DropTargetListener

        expandRoot();
    }

    // DND interfaces
    public void dragGestureRecognized(DragGestureEvent e) {
        loggerDnd.debug("drag gesture detected");
        // drag anything ...

        TreePath tp = tree.getPathForLocation( 
            (int)e.getDragOrigin().getX(), (int)e.getDragOrigin().getY() );
        if (tp != null) {
            AbstractNode node=(AbstractNode) tp.getLastPathComponent();
            Object o = node.getUserObject();
            if(o instanceof Wrappee) {
                node=(AbstractNode)node.getParent();
                Object parent = node.getUserObject();
                if(parent != null && (parent instanceof FieldItem)) {
                    node=(AbstractNode)node.getParent();
                    parent = node.getUserObject();
                }
                Wrappee[] toTransfer;
                if(parent instanceof Wrappee) {
                    toTransfer=new Wrappee[] {(Wrappee)o,(Wrappee)parent};
                } else {
                    toTransfer=new Wrappee[] {(Wrappee)o,null};
                }
                loggerDnd.debug("to transfer: "+Arrays.asList(toTransfer));
                e.startDrag(
                    null,//DragSource.DefaultCopyDrop, // cursor
                    Transfer.getJACTransfer(toTransfer),
                    this); // drag source listener
            }
        }
    }
    public void dragDropEnd(DragSourceDropEvent e) {}
    public void dragEnter(DragSourceDragEvent e) {}
    public void dragExit(DragSourceEvent e) {}
    public void dragOver(DragSourceDragEvent e) {}
    public void dropActionChanged(DragSourceDragEvent e) {}

    public void drop(DropTargetDropEvent e) {
        try {
            loggerDnd.debug("drop event");
            Transferable tr = e.getTransferable();
            TreePath tp = tree.getPathForLocation( 
                (int)e.getLocation().getX(), (int)e.getLocation().getY() );
            if(tp!=null) {
                List transfered=Transfer.getTransferedWrappees(tr);
                Object droppedObject=transfered.get(0);
                Object source=transfered.get(1);
                Object target=((AbstractNode) tp.getLastPathComponent()).getUserObject();         
                loggerDnd.debug("target="+target+", droppedObject="+
                                droppedObject+", source="+source);
                if(droppedObject==null || target==null ||
                   (!(target instanceof Wrappee)) ) return;
                EventHandler.get().onDropObject(getContext(),target,droppedObject,source,false);
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
    public void dragEnter(DropTargetDragEvent e) {}
    public void dragExit(DropTargetEvent e) {}
    public void dragOver(DropTargetDragEvent e) {}
    public void dropActionChanged(DropTargetDragEvent e) {}

    // end of DND interfaces

    /**
     * Expands nodes linked to the tree root. Useful because it is not
     * automatically done and nodes or expand buttons are not displayed
     * at the beginning (you can't open children).
     */
   
    public void expandRoot()
    {
        Object root = model.getRoot();
        int rootCount = model.getChildCount(root);
        for (int i=0; i<rootCount; i++)
        {
            Object child = model.getChild(root, i);
            TreePath path = new TreePath(model.getPathToRoot((TreeNode) child));
            logger.debug("Expanding path "+path);
            tree.expandPath(path);
        }
    }

    /**
     * Create a disabled button
     * @param inconName resource name of the icon
     * @param text text of the icon, which is used as a tooltip if iconName!=null
     * @param listener an ActionListener for the button
     * @return a new button
     */
    JButton createButton(String iconName, String text, ActionListener listener) {
        ImageIcon icon = ResourceManager.getIconResource(iconName);
        JButton button;
        if (icon==null)
            button = new JButton (text);
        else {
            button = new JButton(icon);
            button.setToolTipText(text);
        }
        button.addActionListener(listener);
        button.setEnabled(false);
        return button;
    }

    // interface TreeView

    public void close(boolean validate) {
        model.unregisterEvents();
    }

    void selectObject(AbstractNode root,Object toSelect) {
        Enumeration children=root.children();
        while(children.hasMoreElements()) {
            AbstractNode node = (AbstractNode)children.nextElement();
            if( node.getUserObject()==toSelect ) {
                AbstractNode parent = (AbstractNode)node.getParent();
                while (parent!=null) {
                    tree.expandPath(new TreePath(parent.getPath()));
                    parent = (AbstractNode)parent.getParent();
                }
                tree.expandPath(new TreePath(node.getPath()));
                tree.addSelectionPath(new TreePath(node.getPath()));
            }
            selectObject(node,toSelect);
        }
    }

    public void treeCollapsed(TreeExpansionEvent event) {
        loggerEvents.debug("treeCollapsed "+event.getPath());
    }

    public void treeExpanded(TreeExpansionEvent event) {
        loggerEvents.debug("treeExpanded "+event.getPath());
        Object node = (AbstractNode)event.getPath().getLastPathComponent();
        if (node instanceof ObjectNode) {
            ((ObjectNode)node).updateChildren();
        }
    }

    /**
     * Handles the tree view selection changes. 
     */
    public void valueChanged(TreeSelectionEvent event) {
        loggerEvents.debug("valueChanged "+event.getSource().getClass().getName());
        AbstractNode node = (AbstractNode)
            tree.getLastSelectedPathComponent();        
        if (node == null) {
            viewButton.setEnabled(false);
            removeButton.setEnabled(false);
        } else {
            Object selected = node.getUserObject();
            if (selected instanceof CollectionItem) {
                loggerEvents.debug("selected the collection "+
                                   ((CollectionItem)selected).getName());
                viewButton.setEnabled(false);
                newButton.setEnabled(true);
                removeButton.setEnabled(doDelete(true));
            } else {
                loggerEvents.debug("selected a wrappee "+selected);
                viewButton.setEnabled(selected!=null);
                newButton.setEnabled(false);
                removeButton.setEnabled(doDelete(true));
                EventHandler.get().onNodeSelection(context,node,false);
            }
        }
    }

    // MouseListener interface

    /** Do nothing. */
    public void mouseClicked(MouseEvent me){}

    /**
     * Shows a popup if needed.
     *
     * @param e mouse event descriptor */

    public void mousePressed(MouseEvent e) {
        maybeShowPopup(e);
    }

    /**
     * Closes a popup if needed.
     *
     * @param e mouse event descriptor */

    public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
    }

    /** Do nothing. */
    public void mouseExited(MouseEvent me){}
    /** Do nothing. */
    public void mouseEntered(MouseEvent me){}   

    private void maybeShowPopup(MouseEvent e) {
        loggerEvents.debug("maybeShowPopup: "+e.getClass().getName());
        TreePath tp = tree.getPathForLocation( e.getX(), e.getY() );
        if (tp != null) {
            Object o = ((AbstractNode) tp.getLastPathComponent()).getUserObject();
            if (e.isPopupTrigger()) {
                //tree.setSelectionPath(tp);
                if (o instanceof Wrappee) 
                    SwingEvents.showObjectMenu(context, o, e);
            }
        }
    }

    // KeyListener interface

    public void keyTyped(KeyEvent event) {}

    public void keyPressed(KeyEvent event) {
        System.out.println("keyPressed "+event);
    }

    public void keyReleased(KeyEvent event) {
        if (event.getKeyCode()==KeyEvent.VK_DELETE) {
            doDelete(false);
        }
    }

    public void setSelection(TreePath selection) {
        tree.setSelectionPath(selection);
    }

    /**
     * Handles "open" actions
     */ 
    class openHandler implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            loggerEvents.debug("action performed: OPEN");
            setContext();
            //TreePath anchorPath = tree.getAnchorSelectionPath();
            TreePath anchorPath = tree.getSelectionPath();
            if (anchorPath!=null) {
                AbstractNode node = (AbstractNode)anchorPath.getLastPathComponent();
                AbstractNode parentNode = (AbstractNode)node.getParent();         
                Collaboration.get().addAttribute(
                    "Session.sid", 
                    "Swing"+org.objectweb.jac.core.dist.Distd.getLocalContainerName() );
                
                if (node instanceof ObjectNode) {
                    ObjectNode objectNode = (ObjectNode)node;
                    EventHandler.get().onSelection(
                        context,objectNode.getRelation(),objectNode.getUserObject(),
                        null,null,true);
                }
            }
        }
    }

    /**
     * Handles "add" actions
     */ 
    class addHandler implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            loggerEvents.debug("action performed: ADD");
            setContext();
            AbstractNode node = (AbstractNode)tree
                .getAnchorSelectionPath().getLastPathComponent();
            AbstractNode parentNode = (AbstractNode)node.getParent();         
            Collaboration.get().addAttribute(
                "Session.sid", "Swing"+org.objectweb.jac.core.dist.Distd.getLocalContainerName() );

            Object o = node.getUserObject();
            if( o instanceof CollectionItem ) {
                if( parentNode != null ) { 
                    try {
                        MethodItem[] addingMethods = ((CollectionItem)o).getAddingMethods();                  
                        if( addingMethods != null && addingMethods.length > 0 ) {
                            logger.debug("invoking "+addingMethods[0]+" on "+
                                      parentNode.getUserObject());
                            if (((CollectionItem)o).getAttribute(GuiAC.AUTO_CREATE)!=null) {
                                logger.debug("auto creation asked");
                                Collaboration.get().addAttribute(
                                    GuiAC.AUTO_CREATE, addingMethods[0].getParameterTypes()[0]);
                            }
                            Collaboration.get().addAttribute(
                                GuiAC.ASK_FOR_PARAMETERS, addingMethods[0]);
                            Collaboration.get().addAttribute(
                                GuiAC.DISPLAY_CONTEXT, context);
                            addingMethods[0].invoke(
                                parentNode.getUserObject(),
                                new Object[addingMethods[0].getParameterTypes().length]);
                        }
                    } catch( Exception e ) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Delete selected objects.
     *
     * @param softRun if true, will not actually call delete, but just
     * return if the user is allowed to see a delete button.
     * @return true if the user is allowed to see a delete button for
     * all of the selected objects.
     */
    protected boolean doDelete(boolean softRun) {
        setContext();

        TreePath[] selectionPaths = tree.getSelectionPaths();
        if (selectionPaths==null)
            return false;
        boolean result = true;
        for (int i=0; i<selectionPaths.length; i++) {
            loggerEvents.debug("selectionPath = "+selectionPaths[i]);
            AbstractNode node = 
                (AbstractNode)selectionPaths[i].getLastPathComponent();
            AbstractNode parentNode = 
                (AbstractNode)node.getParent();
            Object selectedObject = node.getUserObject();
            if (selectedObject instanceof CollectionItem) {
                CollectionItem collection = (CollectionItem)selectedObject;
                // If the selected node is a collection node,
                // interactively invoke the remove method
                if (parentNode != null &&
                    GuiAC.isRemovable(parentNode.getUserObject(),collection)) {
                    if (!softRun) {
                        EventHandler.get().onRemoveFromCollection(
                            context,
                            new RemoveEvent(
                                this,
                                parentNode.getUserObject(),
                                (CollectionItem)selectedObject,
                                null), 
                            false);
                    } else {
                        result = false;
                    }
                }
            } else if (node instanceof ObjectNode && 
                       ((ObjectNode)node).getRelation() instanceof CollectionItem) {
                loggerEvents.debug("node = "+node+"; parentNode="+parentNode);
                FieldItem coll = ((ObjectNode)node).getRelation();
                if (coll instanceof CollectionItem &&
                    GuiAC.isRemovable(((ObjectNode)node).getSubstance(),
                                      (CollectionItem)coll)) 
                {
                    if (!softRun) {
                        Collaboration.get().addAttribute(GuiAC.REMOVED_NODE,node);
                        try {
                            EventHandler.get().onRemoveFromCollection(
                                context,
                                new RemoveEvent(
                                    this,
                                    ((ObjectNode)node).getSubstance(),
                                    (CollectionItem)coll,
                                    selectedObject), 
                                false);
                        } finally {
                            Collaboration.get().removeAttribute(GuiAC.REMOVED_NODE);
                        }
                    }
                } else {
                    result = false;
                }
            } else {
                result = false;
            }
        }
        return result;
    }

    static class TreeNodeRenderer extends DefaultTreeCellRenderer {
      
        public TreeNodeRenderer() {}
      
        public Component getTreeCellRendererComponent(JTree tree,
                                                      Object value,
                                                      boolean sel,
                                                      boolean expanded,
                                                      boolean leaf,
                                                      int row,
                                                      boolean hasFocus) {
         
            this.hasFocus = hasFocus;
            AbstractNode node = (AbstractNode)value;
            setText(node.getText());
            setToolTipText(node.getToolTip());
      
            if(sel)
                setForeground(getTextSelectionColor());
            else
                setForeground(getTextNonSelectionColor());
            // There needs to be a way to specify disabled icons.
            if (!tree.isEnabled()) {
                setEnabled(false);
                if (leaf) {
                    setDisabledIcon(getLeafIcon());
                } else if (expanded) {
                    setDisabledIcon(getOpenIcon());
                } else {
                    setDisabledIcon(getClosedIcon());
                }
            } else {
                setEnabled(true);
                setIcon(ResourceManager.getIcon(node.getIcon()));
            }
            setComponentOrientation(tree.getComponentOrientation());	    
            selected = sel;

            return this;
        }

        public Dimension getPreferredSize() {
            Dimension dim = super.getPreferredSize();
            Icon icon = getIcon();
            if (icon!=null) {
                if (icon.getIconHeight()+2>dim.getHeight())
                    dim.setSize((int)dim.getWidth(), getIcon().getIconHeight()+2);
            }
            return dim;
        }

    }
}
