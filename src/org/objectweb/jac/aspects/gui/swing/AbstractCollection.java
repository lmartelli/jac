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

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.objectweb.jac.aspects.gui.*;
import org.objectweb.jac.aspects.session.SessionAC;
import org.objectweb.jac.core.Collaboration;
import org.objectweb.jac.core.Wrappee;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.core.rtti.CollectionItem;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.MethodItem;
import org.objectweb.jac.core.rtti.NamingConventions;
import org.objectweb.jac.util.ExtArrays;

/**
 * Base class to implement ListView and TableView
 */
public abstract class AbstractCollection extends AbstractView
    implements ListSelectionListener, ActionListener, 
              MouseListener, CollectionView, KeyListener
{
    CollectionItem collection;
    Object substance;
    CollectionModel model;
    protected org.objectweb.jac.aspects.gui.CollectionItemView itemView;
    JComponent component;

    JButton viewButton;
    JButton removeButton;
    JButton moveupButton; 
    JButton movedownButton; 

    public CollectionModel getCollectionModel() {
        return model;
    }

    public AbstractCollection(ViewFactory factory, DisplayContext context,
                              CollectionItem collection, Object substance,
                              CollectionModel model, 
                              org.objectweb.jac.aspects.gui.CollectionItemView itemView) {
        super(factory,context);
        this.collection = collection;
        this.substance = substance;
        this.model = model;
        this.itemView = itemView;

        setLayout(new BorderLayout());

        component = getInnerComponent(model);
        component.addKeyListener(this);
        JScrollPane scrollPane = new JScrollPane(component);
        component.addMouseListener(this);
        add(scrollPane, BorderLayout.CENTER);
      
        // add, remove and view buttons
        JPanel lowerCont = new JPanel();
        JButton b = null;

        CustomizedGUI customized = null;
        List targets = null;
        if (context.getCustomizedView()!=null) {
            customized = context.getCustomizedView().getCustomizedGUI();
            targets = customized.getFieldTargets(collection);
        }

        if (targets==null || targets.size()==0) {
            viewButton = createButton("view_icon","View","open");
            viewButton.setEnabled(false);
            lowerCont.add(viewButton);
        }
      
        if (GuiAC.isAddable(substance,collection)) {
            b = createButton("new_icon",GuiAC.getLabelAdd(),"add");
            lowerCont.add(b);
        }
      
        if (GuiAC.isRemovable(substance,collection)) {
            removeButton = createButton("remove_icon","Remove","remove");
            removeButton.setEnabled(false);
            lowerCont.add(removeButton);
        }

        if (collection.isList() && !collection.isCalculated()) {
            moveupButton = createButton("up_icon","Move up","moveup");
            moveupButton.setEnabled(false);
            lowerCont.add(moveupButton);

            movedownButton = createButton("down_icon","Move up","movedown");
            movedownButton.setEnabled(false);
            lowerCont.add(movedownButton);
        }

        MethodItem[] directMeths = (MethodItem[])
            collection.getAttribute(GuiAC.DIRECT_COLLECTION_METHODS);
        if (directMeths != null) {
            for (int i=0; i<directMeths.length; i++) {
                // We should use SwingMethodView for more consistency
                lowerCont.add(new DirectButton(directMeths[i]));
            }
        }

        add(lowerCont,BorderLayout.SOUTH);

        //      Utils.registerCollection(substance,collection,this);
    }

    protected JButton createButton(String icon, String tooltip, String action) {
        JButton button = new JButton(ResourceManager.getIconResource(icon));
        button.setToolTipText(tooltip);
        button.setActionCommand(action);
        button.addActionListener(this);
        button.setMargin(new Insets(1,5,1,5));
        return button;
    }

    protected abstract JComponent getInnerComponent(Model model);

    boolean isEditor;
    public boolean isEditor() {
        return isEditor;
    }
    public void setEditor(boolean isEditor) {
        this.isEditor = isEditor;
    }

    public void setAutoUpdate(boolean autoUpdate) {
        // TODO ...
    }

    public void close(boolean validate) {
        closed = true;
        model.close();
    }

    /**
     * Defines what happens when the selection changes. 
     */

    public void valueChanged(ListSelectionEvent event) {
        loggerEvents.debug("valueChanged "+event);

        ListSelectionModel lsm = (ListSelectionModel)event.getSource();
        if (lsm.isSelectionEmpty()) {
            if (viewButton!=null)
                viewButton.setEnabled(false);
            if (removeButton!=null)
                removeButton.setEnabled(false);
            if (moveupButton!=null)
                moveupButton.setEnabled(false);
            if (movedownButton!=null)
                movedownButton.setEnabled(false);
            return;
        } else if (event.getValueIsAdjusting()) {
            return;
        } else {
            if (removeButton!=null)
                removeButton.setEnabled(true);
            if (viewButton!=null)
                viewButton.setEnabled(selectionContainsAWrappee());
            int indice = getSelectedIndices()[0];
            CollectionPosition collpos = new CollectionPosition(
                this,
                collection,
                indice,
                substance);
            EventHandler.get().onSelection(context,
                                           collection,
                                           getSelected()[0],
                                           null,
                                           collpos);
            //null);
         
            if (moveupButton!=null)
                moveupButton.setEnabled(indice>0);
            if (movedownButton!=null)
                movedownButton.setEnabled(indice<model.getRowCount()-1);
        }
    }

    /**
     * Tells wether the selection contains at least on Wrappee object
     */
    boolean selectionContainsAWrappee() {
        Object[] selection = getSelected();
        for (int i=0; i<selection.length; i++) {
            if (selection[i] instanceof Wrappee) {
                return true;
            }
        }
        return false;
    }

    // KeyListener interface

    public void keyTyped(KeyEvent event) {}

    public void keyPressed(KeyEvent event) {}

    public void keyReleased(KeyEvent event) {
        if (event.getKeyCode()==KeyEvent.VK_DELETE &&
            removeButton!=null
            /* GuiAC.isRemovable(collection) // this does not work
               because the session's context is not initialized */ ) {
            doDelete();
        }
    }

    void doDelete() {
        Object[] selected = getSelected();
        MethodItem removeMethod = collection.getRemover();
        loggerEvents.debug("remover="+removeMethod);
        if (removeMethod!=null) {
            // try with the removing method
            for (int i=0; i<selected.length; i++) {
                EventHandler.get().onRemoveFromCollection(
                    context,
                    new RemoveEvent(
                        this,
                        substance,
                        collection,
                        selected[i]),
                    false);
            }
        } else {
            // call the collection's method directly
            /*
              getMethod = collection.getGetter();
              if (getMethod!=null) {
              MethodItem method = null;
              Object c;
              try {
              c = getMethod.invoke(substance,ExtArrays.emptyObjectArray);
              } catch (Exception e) {
              Log.error("Getting collection with "+getMethod.getName()+
              " failed : "+e);
              return;
              }
              ClassItem cl = ClassRepository.get().getClass(c.getClass());
              if (!collection.isMap()) {
              method = cl.getMethod("remove(java.lang.Object)");
              Log.trace("gui","Invoking "+method.getName()+" on collection itself");
              for (int i=0; i<selected.length; i++) {
              try {
              method.invoke(c,new Object[] {selected[i]});
              } catch (Exception e) {
              Log.error("Removing from collection with "+method.getName()+
              " failed : "+e);
              }
              }
              }
            */   
        }
        onRemove();
    }

    /**
     * Handles the actions on this view.
     *
     * <p>On a collection view, the three default possible actions are
     * to open a new view on the selected item, to add a new item to
     * the collection, and to remove the selected item from the
     * collection.
     *
     * @param event the user event 
     */
    public void actionPerformed(ActionEvent event) {
        setContext();
        loggerEvents.debug("action performed on collection : "+event.getActionCommand()+
                  "; modifiers = "+event.getModifiers());
        if (event.getActionCommand().equals("open")) {
            Object[] selected = getSelected();
            for (int i=0; i<selected.length; i++) {
                try {
                    EventHandler.get().onSelection(
                        context,collection,selected[i],null,null,true);
                } catch (Exception e) {
                    loggerEvents.error("failed to view "+selected[i],e);
                }
            }
        } else if (event.getActionCommand().equals("add")) {
            System.out.println(event. getModifiers()+"&"+ActionEvent.CTRL_MASK+"="+
                               (event. getModifiers()& ActionEvent.CTRL_MASK));
            EventHandler.get().onAddToCollection(
                context,
                new AddEvent(this,substance,collection,null),
                (event. getModifiers()& ActionEvent.CTRL_MASK) != 0);
        } else if (event.getActionCommand().equals("remove")) {
            doDelete();
        } else if (event.getActionCommand().equals("movedown")) {
            Object selected = getSelected()[0];
            int index = getSelectedIndices()[0];
            if (index<model.getRowCount()-1) {
                Integer i = new Integer(index);
                collection.remove(substance,selected,i);
                CollectionUpdate update = getCollectionUpdate();
                update.onRemove(substance, collection, null, i , null);
                i = new Integer(index+1);
                collection.add(substance,selected,i);
                update.onAdd(substance, collection, null, i, null);
                setSelected(index+1);
            }
        } else if (event.getActionCommand().equals("moveup")) {
            Object selected = getSelected()[0];
            int index = getSelectedIndices()[0];
            if (index>0) {
                Integer i = new Integer(index);
                collection.remove(substance,selected,i);
                CollectionUpdate update = getCollectionUpdate();
                update.onRemove(substance, collection, null, i, null);
                i = new Integer(index-1);
                collection.add(substance,selected,i);
                update.onAdd(substance, collection, null, i, null);
                setSelected(index-1);
            }
        }
    }

    // button for direct method
    class DirectButton extends JButton 
        implements ListSelectionListener, ActionListener 
    {
        MethodItem method;
        DirectButton(MethodItem method) {
            super(GuiAC.getLabel(method));
            this.method = method;
            setIcon(ResourceManager.getIcon(GuiAC.getIcon(method)));
            addActionListener(this);
            setEnabled(false);
            getSelectionModel().addListSelectionListener(this);
        }
        public void valueChanged(ListSelectionEvent event) {
            ListSelectionModel lsm = (ListSelectionModel)event.getSource();
            setEnabled(!lsm.isSelectionEmpty());
        }
        public void actionPerformed(ActionEvent event) {
            Collaboration.get().addAttribute(
                SessionAC.SESSION_ID, GuiAC.getLocalSessionID());

            Object[] selected = getSelected();
            for (int i=0; i<selected.length; i++) {
                try {
                    EventHandler.get().onInvoke(
                        context,
                        new InvokeEvent(AbstractCollection.this,selected[i],method)).join();
                } catch(InterruptedException e) {
                    context.getDisplay().showModal(
                        e,
                        "Error: "+e,
                        "An error occured during the invocation of "+
                        	method.getCompactFullName()+
                        	" on "+selected[i].getClass().getName()+
                        	" \""+GuiAC.toString(selected[i])+"\"",
                        context.getWindow(),
                        false,false,true);
                }
            }
        }   
    }

    // should call clear selection on the component's selection model
    protected abstract void onRemove();

    protected abstract CollectionUpdate getCollectionUpdate();

    /**
     * Returns an array of the selected objects. The array is empty if
     * no object is selected, but not null.
     **/

    protected Object[] getSelected() {
        Object[] selected = null;
        int[] indices = getSelectedIndices();
        if (indices != null) {
            selected = new Object[indices.length];
            for (int i=0; i<indices.length; i++)
            {
                selected[i]= model.getObject(indices[i]);
            }
        } else {
            selected = ExtArrays.emptyObjectArray;
        }
        return selected;
    }

    /**
     * Returns the indices of selected objects.
     */
    protected abstract int[] getSelectedIndices();

    protected abstract ListSelectionModel getSelectionModel();

    protected void setNoRefresh(boolean norefresh) {
        if (norefresh==false) {
            repaint();
        }
    }

    static class BetterListSelectionModel extends DefaultListSelectionModel {
        JComponent list = null;
        public BetterListSelectionModel( JComponent list ) {
            super();
            this.list = list;
        }
        public JComponent getList() {
            return list;
        }
    }

    // MouseListener interface

    public void mouseClicked(MouseEvent me){
        loggerEvents.debug("mouseClicked");
        if (model.getRowCount()==1) {
            loggerEvents.debug("rowCount==1 => forceRefresh");
            if (getSelectedIndices().length>0) {
                CollectionPosition collpos = new CollectionPosition(
                    this,
                    collection,
                    getSelectedIndices()[0],
                    substance);
                EventHandler.get().onSelection(context,
                                               collection,
                                               getSelected()[0],
                                               null,
                                               collpos);
            }
        }
    }

    public void mousePressed(MouseEvent e) {
        maybeShowPopup(e);
    }
    public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
    }
    public void mouseExited(MouseEvent me){}
    public void mouseEntered(MouseEvent me){}   
    void maybeShowPopup(MouseEvent event) {
        loggerEvents.debug("maybeShowPopup");
        if (event.isPopupTrigger()) {
            int index = locationToIndex(event.getPoint());
            if (index!=-1) {
                Object o = model.getObject(index);
                if (o instanceof Wrappee) 
                    SwingEvents.showObjectMenu(context, o, event);
            }
        }
    }
    /**
     * Returns the index of the element at the given location, or -1 if
     * if no element is under this location .  */
    abstract int locationToIndex(Point location);

    public void setField(FieldItem field) {
        collection = (CollectionItem)field;
    }

    public void setSubstance(Object substance) {
        this.substance = substance;
    }

    public Object getSubstance() {
        return substance;
    }

    public FieldItem getField() {
        return collection;
    }

    public void setValue(Object value) {
    }

    public void updateModel(Object substance) {
    }

}
