/*
  Copyright (C) 2004 Laurent Martelli <laurent@aopsys.com>
  
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
import javax.swing.JTextField;
import org.objectweb.jac.aspects.gui.FieldEditor;
import org.objectweb.jac.aspects.gui.GuiAC;
import org.objectweb.jac.aspects.gui.Length;
import org.objectweb.jac.core.rtti.FieldItem;

/**
 * A Swing editor component for fields values (primitive types).
 */

public abstract class TextFieldEditor extends AbstractFieldEditor 
{
    protected JTextField textField;

    public TextFieldEditor(Object substance, FieldItem field) 
    {
        super(substance,field);
    }

    public void setSize(Length width, Length height) {
        this.width = width;
        this.height = height;
        SwingUtils.setColumns(textField,width);
    }

    /**
     * Set the focus on the textField
     */
    public void requestFocus() {
        textField.requestFocus();
    }

}
