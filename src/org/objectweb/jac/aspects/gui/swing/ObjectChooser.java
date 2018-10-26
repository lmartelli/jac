/*
  Copyright (C) 2001-2003 Renaud Pawlak <renaud@aopsys.com>, 
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

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import org.apache.log4j.Logger;
import org.objectweb.jac.aspects.gui.*;
import org.objectweb.jac.core.Collaboration;
import org.objectweb.jac.core.Wrappee;
import org.objectweb.jac.core.rtti.FieldItem;

/**
 * This is a special value editor that allows the user to choose a
 * value within a set of object of a given type. */

public class ObjectChooser extends AbstractFieldEditor
    implements ActionListener, ItemListener, ReferenceEditor
{
    static Logger logger = Logger.getLogger("gui.chooser");

    JComboBox choice;
    ComboBoxModel model;
    JButton viewButton;
    JButton newButton;

    /**
     * Constructs a new object chooser.
     */

    public ObjectChooser(Object substance, FieldItem reference, 
                         ComboBoxModel model, boolean isEditable) 
    {
        super(substance,reference);

        this.model = model;
        this.isEditable = isEditable;
        choice = new JComboBox(model);

        boolean isWrappee = model.getType()!=null &&
            Wrappee.class.isAssignableFrom(model.getType().getActualClass());
        choice.setEditable(isEditable && !isWrappee);

        // This is a very dirty hack to get the real data component since
        // JComboBox does not fire the focus events (certainly a bug in 
        // JDK 1.3

        /*
          if(isEditable) {
          choice.getComponent(2).addFocusListener(this);
          choice.addFocusListener(this);
          } else {
        */
      
        choice.getComponent(0).addFocusListener(this);
        choice.addFocusListener(this);
      
        /*
          }
        */

        add(choice);
        //choice.addActionListener(this);
        choice.addItemListener(this);
      
        // Fill stupid height
        Dimension minSize = choice.getPreferredSize();
        Dimension maxSize = choice.getMaximumSize();
        choice.setMaximumSize(new Dimension(maxSize.width,minSize.height));

        Boolean small_view = (Boolean) Collaboration.get().
            getAttribute(GuiAC.SMALL_VIEW);

        if ((small_view == null) || (!small_view.booleanValue())) {
            if (isWrappee) {
                viewButton = new JButton (ResourceManager.getIconResource("view_icon"));
                viewButton.setToolTipText("View");
                viewButton.setActionCommand("open");
                viewButton.addActionListener(this);
                viewButton.setMargin(new Insets(1,1,1,1));
                add(viewButton);
            }

            if (isWrappee && isEditable && model.getType()!=null && 
                GuiAC.isCreatable(model.getType())) {
                newButton = new JButton(ResourceManager.getIconResource("new_icon"));
                newButton.setToolTipText("New");
                newButton.setActionCommand("new");
                newButton.addActionListener(this);
                newButton.setMargin(new Insets(1,1,1,1));
                add(newButton);
            }
        }
    }

    public void setFocus(FieldItem field, Object extraOption) {}


    // FieldEditor interface

    public void setValue(Object value) {
        super.setValue(value);
        model.setSelectedObject(value);
      
        if (value==null && viewButton!=null) 
            viewButton.setEnabled(false);
    }

    public Object getValue() {
        logger.debug("selectedItem = "+model.getSelectedObject());
        return model.getSelectedObject();
    }

    public void close(boolean validate) {
        super.close(validate);
        model.close();
    }

    /**
     * Handles the actions performed by the users on this view.
     *
     * <p>On an object chooser, a "new" action can be performed to
     * allow the user to add a new object to the choices it not present
     * yet.
     *
     * @param event the performed action 
     */
    public void actionPerformed(ActionEvent event) {
        loggerEvents.debug("actionPerformed: "+event.getActionCommand());
        setContext();
        if (event.getActionCommand().equals("new")) {
            Object instance = 
                EventHandler.get().onCreateObject(
                    context,model.getType(),substance,field);
            if (instance!=null) {
                requestFocus();
                model.addObject(instance);
                model.setSelectedObject(instance);
            }
        } else if (event.getActionCommand().equals("open")) {
            Object object = model.getSelectedObject();
            if (object == null) 
                return;
            if (object!=null) {
                EventHandler.get().onView(context,field,object,null,null);
            }
        }
    }

    /**
     * Set the focus on the JComboBox
     */
    public void requestFocus() {
        choice.requestFocus();
        loggerFocus.debug("focusing "+choice.getClass().getName());
    }

    boolean isEditable = false;
    public boolean isEditable() {
        return isEditable;
    }
    public void setEditable(boolean editable) {
        this.isEditable = editable;
    }

    public ComboBoxModel getModel() {
        return model;
    }
   
    // ItemListener interface

    public void itemStateChanged(ItemEvent event) {
        loggerEvents.debug("itemStateChanged on "+this);
        if (field!=null && isEmbedded) {
            invokeInContext(this,"commit", new Object[]{});
        } else {
            loggerEvents.debug("ignoring item event");
        }      
    }
} 
