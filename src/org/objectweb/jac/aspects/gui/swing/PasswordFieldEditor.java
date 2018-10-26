/*
  Copyright (C) 2001 Renaud Pawlak, Laurent Martelli
  
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
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import org.objectweb.jac.aspects.gui.FieldEditor;
import org.objectweb.jac.aspects.gui.GuiAC;
import org.objectweb.jac.aspects.gui.Length;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.NamingConventions;

/**
 * A Swing editor component for fields values (password types).
 */

public class PasswordFieldEditor extends AbstractFieldEditor
    implements FieldEditor
{
    JTextField textField;
    JTextField confirmField;

    /**
     * Constructs a new password field editor. */

    public PasswordFieldEditor(Object substance, FieldItem field)
    {
        super(substance,field);
        textField = new JPasswordField();
        confirmField = new JPasswordField();
        // swing "bug" workaround : prevent the textField from extending
        // vertically.
        Dimension minSize = textField.getPreferredSize();
        Dimension maxSize = textField.getMaximumSize();
        textField.setMaximumSize(new Dimension(maxSize.width,minSize.height));

        // textField.addFocusListener(this);
        add(textField);

        confirmField.setMaximumSize(new Dimension(maxSize.width,minSize.height));
        confirmField.addFocusListener(this);
        add(new JLabel("Retype "+NamingConventions.textForName(field.getName())));
        add(confirmField);

    }

    // FieldEditor interface

    public Object getValue() {
        if (!(confirmField.getText().equals(textField.getText())))
            throw new RuntimeException(NamingConventions.textForName(field.getName())+" and its confirmation are different");
        return( textField.getText() );
    }

    public void setValue(Object value) {
        super.setValue(value);
        if( value == null ) value="";
        textField.setText(GuiAC.toString(value));
        Dimension minSize = textField.getPreferredSize();
        minSize.width = 100;
        textField.setMinimumSize(minSize);
    }

    public void setSize(Length width, Length height) {
        super.setSize(width,height);
        SwingUtils.setColumns(textField,width);
        SwingUtils.setColumns(confirmField,width);
    }

    /**
     * Set the focus on the textField
     */
    public void requestFocus() {
        textField.requestFocus();
    }

}
