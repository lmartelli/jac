/*
  Copyright (C) 2001-2003 Renaud Pawlak, Laurent Martelli
  
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
import java.lang.reflect.Constructor;
import javax.swing.JPasswordField;
import org.objectweb.jac.aspects.gui.FieldEditor;
import org.objectweb.jac.aspects.gui.GuiAC;
import org.objectweb.jac.aspects.gui.Length;
import org.objectweb.jac.core.rtti.FieldItem;

/**
 * A Swing editor component for fields values (primitive types).
 */

public class PrimitiveFieldEditor extends TextFieldEditor 
    implements FieldEditor
{
    /**
     * Constructs a new primitive field editor. 
     */
    public PrimitiveFieldEditor(Object substance, FieldItem field,
                                boolean password) 
    {
        super(substance,field);
        init(password);
        checkType();
    }

    public PrimitiveFieldEditor(Object substance, FieldItem field) 
    {
        super(substance,field);
        init(false);
        checkType();
    }

    protected void checkType() {
        if (field!=null && field.getType().isArray())
            throw new RuntimeException("PrimitiveFieldEditor cannot handle arrays");
    }

    protected void init(boolean password)
    {
        if (password) 
            textField = new JPasswordField();
        else  
            textField = new JTextField();
        setSize(new Length("15ex"),null);
        textField.addFocusListener(this);
        add(textField);
    }

    // FieldEditor interface

    public Object getValue() {
        Class cl = type.getActualClass();
        if (cl == int.class || cl == Integer.class) {
            return new Integer(textField.getText());
        } else if (cl == boolean.class || cl == Boolean.class) {
            return( Boolean.valueOf(textField.getText()));
        } else if (cl == long.class || cl == Long.class) {
            return new Long(textField.getText());
        } else if (cl == float.class || cl == Float.class) {
            return new Float(textField.getText());
        } else if (cl == double.class || cl == Double.class) {
            return new Double(textField.getText());
        } else if (cl == short.class || cl == Short.class) {
            return new Short (textField.getText());
        } else if (cl == byte.class || cl == Byte.class) {
            return new Byte(textField.getText());
        } else if (cl == char.class || cl == Character.class) {
            return new Character(textField.getText().charAt(0));
        } else if (cl == String.class) {
            return textField.getText();
        } else if (!cl.isArray()) {
            try {
                // trying to construct the object from its textual 
                // representation (I think that any class should have
                // a constructor taking a string... this is so helpful...)
                // of course, this will raise an exception most of the time :-(
                Constructor c = cl.getConstructor(new Class[] {String.class});
                return c.newInstance( new Object[] {textField.getText()} );
            } catch( Exception e ) {
                logger.error("Failed to instantiate "+cl.getName(),e);
                throw new RuntimeException("Unhandled type "+type.getName());
            }
        } else {
            throw new RuntimeException("Unhandled type "+type.getName());
        }
    }

    public void setValue(Object value) {
        super.setValue(value);
        if( value == null ) value="";
        textField.setText(GuiAC.toString(value));
        Dimension minSize = textField.getPreferredSize();
        minSize.width = 100;
        textField.setMinimumSize(minSize);
    }

    public void setAutoUpdate(boolean autoUpdate) {
        // TODO
    }
}
