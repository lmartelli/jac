/*
  Copyright (C) 2002-2003 Julien van Malderen <julien@aopsys.com>
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
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA */

package org.objectweb.jac.aspects.gui.swing;


import org.objectweb.jac.aspects.gui.*;
import org.objectweb.jac.core.rtti.CollectionItem;
import org.objectweb.jac.util.ExtArrays;
import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class CollectionItemView extends AbstractView
    implements ActionListener, AbstractCollectionItemView
{
    Object substance;
    CollectionItem collection;
    CollectionView collectionView;
    CollectionModel model;
    int current;
    ObjectView objectView;
    String viewType;
    JButton parentButton;
    View hiddenView;
    String[] viewParams;

    public CollectionItemView(View view,
                              CollectionPosition coll,
                              String viewType, String[] viewParams,
                              View hiddenView) {
        this.objectView = (ObjectView) view;
        this.collection = coll.getCollection();
        this.collectionView = coll.getCollectionView();
        this.model = this.collectionView.getCollectionModel();
        this.current = coll.getIndex();
        this.substance = coll.getSubstance();
        this.viewType = viewType;
        this.viewParams = viewParams;
        this.hiddenView = hiddenView;
      
        draw();
    }

    public View getView() {
        return objectView;
    }

    public void close(boolean validate) {
        super.close(validate);
        objectView.close(validate);        
    }

    protected void draw() {
        setLayout(new BorderLayout());

        if (GuiAC.hasSetNavBar(objectView.context.getCustomizedView()
                               .getCustomizedGUI(),
                               collection))
        {
            JPanel panel = new JPanel();
         

            String prevStr = 
                (current>0) ? GuiAC.toString(model.getObject(current-1)) : null;
            String nextStr = 
                (current<(model.getRowCount()-1)) ? 
                GuiAC.toString(model.getObject(current+1)) : 
                null;

            if (prevStr != null) {
                JButton prevButton = 
                    new JButton(" ("+ prevStr  +") ",
                                ResourceManager.getIconResource("previous_icon"));
                prevButton.setToolTipText("Previous Item");
                prevButton.setActionCommand("previous");
                prevButton.addActionListener(this);
                prevButton.setHorizontalTextPosition(JButton.LEFT);
                //prevButton.setMargin(new Insets(1,1,1,1));
                panel.add(prevButton);
            }
         
            int cur = current + 1;
            JLabel counter = new JLabel("["+cur+" / "+model.getRowCount()+"]");
            panel.add(counter);

            if (nextStr != null) {
                JButton nextButton = 
                    new JButton(" ("+ nextStr +") ",
                                ResourceManager.getIconResource("next_icon"));
                nextButton.setToolTipText("Next Item");
                nextButton.setActionCommand("next");
                nextButton.addActionListener(this);
                //nextButton.setMargin(new Insets(1,1,1,1));
                panel.add(nextButton);
            }

            if (((View)collectionView).isClosed()) {
                parentButton = 
                    new JButton(ResourceManager.getIconResource("up_icon"));
                parentButton.setToolTipText("Back to Collection");
                parentButton.setActionCommand("back");
                parentButton.addActionListener(this);
                //parentButton.setMargin(new Insets(1,1,1,1));
                panel.add(parentButton);
            }

            if (GuiAC.isRemovable(collection)) {
                JButton removeButton = 
                    new JButton(ResourceManager.getIconResource("remove_icon"));
                removeButton.setToolTipText("Remove Item");
                removeButton.setActionCommand("remove");
                removeButton.addActionListener(this);
                removeButton.setMargin(new Insets(1,1,1,1));
                panel.add(removeButton);
            }

            add(panel,BorderLayout.NORTH);
        }

        add((java.awt.Component) objectView,BorderLayout.CENTER);

    }

    public void onNextInCollection() {
        if (current < model.getRowCount() - 1)
        {
            current++;
            if (collectionView!=null)
                collectionView.setSelected(current);
            Object curr = model.getObject(current);
            objectView.close(true);
            objectView = (ObjectView) factory.createView("target[?]", viewType,
                                                         ExtArrays.add(curr,viewParams),
                                                         context);
            removeAll();
            draw();
            validate();
        }
    }

    public void onPreviousInCollection() {
        if (current > 0) {
            current--;
            if (collectionView!=null)
                collectionView.setSelected(current);
            Object curr = model.getObject(current);
            objectView.close(true);
            objectView = (ObjectView) factory.createView("target[?]", viewType,
                                                         ExtArrays.add(curr,viewParams),
                                                         context);
            removeAll();
            draw();
            validate();
        }
    }

    public void onRemoveInCollection() {
        Collection col = collection.getActualCollection(substance);
        int old = current;
      
        if (current > 0) {
            current--;
        } else if (col.size() <= 1) {
            col.clear();
            objectView.close(true);
            onBackToCollection();
            return;
        }

        Object curr = null;
        Iterator it = col.iterator();
        for (int i=0; it.hasNext() && i<=old; i++) {
            curr = it.next();
        }

        try {
            collection.removeThroughRemover(substance,curr);
        } catch (Exception e) {
            e.printStackTrace();
            current = old;
            context.getDisplay().refresh();
            return;
        }

        Iterator it2 = col.iterator();
        for (int i=0; it2.hasNext() && i<=current; i++) {
            curr = it2.next();
        }
        objectView.close(true);
        objectView = (ObjectView) factory.createView("target[?]", viewType,
                                                     ExtArrays.add(curr,viewParams),
                                                     context);
        System.out.println("model="+collectionView.getCollectionModel());
        if (current>0) 
            collectionView.setSelected(current);
        removeAll();
        draw();
        validate();
    }

    protected CompositeView findPanel() {
        View current = getParentView();
        View last = null;
        while (current!=null && !(current instanceof PanelView)) {
            last = current;
            current = current.getParentView();
        }
        return (CompositeView)last;
    }

    public void onBackToCollection() {
        CompositeView panel = findPanel();
        if (panel!=null) {
            panel.addView(
                factory.createView(substance.getClass().getName(),
                                   "Object",new Object[] {"default",substance},context));
        }
        validate();
    }

    public void actionPerformed(ActionEvent evt) {
        //setContext();
        if (evt.getActionCommand().equals("previous")) {
            onPreviousInCollection();
        } else if (evt.getActionCommand().equals("next")) {
            onNextInCollection();
        } else if (evt.getActionCommand().equals("remove")) {
            onRemoveInCollection();
        } else if (evt.getActionCommand().equals("back")) {
            onBackToCollection();
        }
        repaint();
    }

    public void setCollection(CollectionItem coll) {
        collection = coll;
    }

    public CollectionItem getCollection() {
        return collection;
    }

    public void setCurrent(int index) {
        current = index;
    }

    public int getCurrent() {
        return current;
    }
}
