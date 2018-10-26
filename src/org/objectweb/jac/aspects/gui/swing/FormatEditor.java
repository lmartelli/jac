/*
  Copyright (C) 2003 Laurent Martelli <laurent@aopsys.com>
  
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
import java.text.ParsePosition;
import org.objectweb.jac.aspects.gui.FieldEditor;
import org.objectweb.jac.aspects.gui.Format;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.util.WrappedThrowableException;

public abstract class FormatEditor extends TextFieldEditor
    implements FieldEditor
{
    /** Stores the default format of the date */
    protected Format format;

    /**
    * Constructs a new date editor. */

    public FormatEditor(Object substance, FieldItem field) {
        super(substance,field);
        textField = new JTextField(10);
        textField.addFocusListener(this);
        add(textField);
        initFormat(field);
    }

    protected abstract void initFormat(FieldItem field);

    public void setField(FieldItem field) {
        super.setField(field);
        initFormat(field);
    }

    public Object getValue() {
        try {
            if (textField.getText().equals("")) {
                return null;
            } else {
                return parse(textField.getText());
            }
        } catch (Exception e) {
            throw new WrappedThrowableException(e);
        }
    }

    protected Object parse(String s) {
        ParsePosition pos = new ParsePosition(0);
        return format.parse(s,pos);        
    }

    public void setValue(Object value) {
        super.setValue(value);
        if( value != null )
            textField.setText(format.format(value));
    }
}
