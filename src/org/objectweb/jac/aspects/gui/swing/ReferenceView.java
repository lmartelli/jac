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

import org.objectweb.jac.aspects.gui.*;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.util.Stack;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;

/**
 * This class defines a Swing component view for references in
 * objects.
 *
 * <p>By default this view constructs an embedded <code>JLabel</code>
 * containing the string representation of the referenced object. However,
 * the field can be attributed to be displayed with a customized
 * rendering by the GUI aspect component.
 */

public class ReferenceView extends AbstractFieldView 
    implements FieldView, FieldUpdate, ObjectUpdate, ActionListener {

    Object object;
    JButton viewButton;
    JLabel label = new JLabel();

    /**
     * Constructs a new reference view.
     *
     * @param substance the object the viewed field belongs to */

    public ReferenceView(Object object, Object substance, FieldItem reference) {
        super(substance,reference);
        this.object = object;

        add(label);
        add(Box.createRigidArea(new Dimension(20,1)));

        JButton b;
        viewButton = new JButton (ResourceManager.getIconResource("view_icon"));
        viewButton.setEnabled(false);
        viewButton.setToolTipText("View");
        viewButton.setActionCommand("open");
        viewButton.addActionListener(this);
        viewButton.setMargin(new Insets(1,1,1,1));
        add(viewButton);

        if (GuiAC.getGraphicContext()!=null)
            contexts.addAll(GuiAC.getGraphicContext());
        if (field!=null)
            contexts.push(field);
        refreshView();
    }

    Stack contexts = new Stack();

    public ReferenceView() {
        label.setFont(null);
        add(label);
    }

    /**
     * Handles the actions on this view.
     *
     * <p>On a reference view, the two default possible actions are to
     * open a new view on the referenced object, or to edit the
     * reference value.
     *
     * @param evt the user event */

    public void actionPerformed(ActionEvent evt) {
        if (evt.getActionCommand().equals("open")) {
            if (object!=null) {
                EventHandler.get().onSelection(context,field,object,null,null,true);
            }
        } else if (evt.getActionCommand().equals("edit")) {
            //         GuiAC.invoke((Display)parent,setter,substance,null);
        }
    }

    public void refreshView() {
        Utils.registerObject(object,this);
        String name;
        if (object!=null) {
            if (viewButton!=null)
                viewButton.setEnabled(true);
            name = GuiAC.toString(object,contexts);
        } else {
            if (viewButton!=null)
                viewButton.setEnabled(false);
            name = "";
        }
        label.setText(name);
    }

    // FieldView interface

    public void setValue(Object value) {
        Utils.unregisterObject(object,this);
        this.object = value;
        refreshView();
    }

    public void close(boolean validate) {
        Utils.unregisterObject(object,this);
        Utils.unregisterField(substance,field,this);
    }

    // FieldUpdate

    public void fieldUpdated(Object substance, FieldItem field, 
                             Object value, Object param) {
        setValue(value);
    }

    // ObjectUpdate interface

    public void objectUpdated(Object object, Object param) {
        refreshView();
    }
}
