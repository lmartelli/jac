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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JCheckBox;
import org.objectweb.jac.aspects.gui.FieldEditor;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.util.ExtBoolean;

/**
 * A Swing editor component for boolean values.
 */

public class BooleanEditor extends AbstractFieldEditor
   implements FieldEditor, ItemListener
{
    JCheckBox checkBox = new JCheckBox();

    /**
     * Constructs a new primitive checkbox editor for booleans. 
     */
    public BooleanEditor(Object substance, FieldItem field) {
        super(substance,field);
        checkBox.addFocusListener(this);
        checkBox.addItemListener(this);
        add(checkBox);
    }

    public Object getValue() {
        return ExtBoolean.valueOf(checkBox.isSelected());
    }

    public void setValue(Object value) {
        super.setValue(value);
        checkBox.setSelected(((Boolean)value).booleanValue());
    }

    /**
     * Set the focus on the checkBox
     */
    public void requestFocus() {
        checkBox.requestFocus();
        loggerFocus.debug("focusing "+checkBox.getClass().getName());
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

