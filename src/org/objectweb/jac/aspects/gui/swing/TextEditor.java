/*
  Copyright (C) 2001-2002 Renaud Pawlak <renaud@aopsys.com>
  
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
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import org.objectweb.jac.aspects.gui.FieldEditor;
import org.objectweb.jac.aspects.gui.Length;
import org.objectweb.jac.core.rtti.FieldItem;

/**
 * A Swing editor component for multi-lines text values.
 */

public class TextEditor extends AbstractFieldEditor
    implements FieldEditor 
{

    JEditorPane editor;
    JScrollPane scrollPane;
   
    /**
     * Constructs a new text editor. */

    public TextEditor(Object substance, FieldItem field) {
        super(substance,field);
        init();
    }

    public void init() {
        editor = new JEditorPane();
        scrollPane = new JScrollPane(editor);
        add(scrollPane);

        ((JEditorPane)editor).addFocusListener(this);
        ((JEditorPane)editor).setContentType("html");
    }

    // FieldEditor internalView
    public void setValue(Object value) {
        super.setValue(value);
        if (value==null) 
            ((JEditorPane)editor).setText("");
        else if (value instanceof String)
            ((JEditorPane)editor).setText((String)value);
        else if (value instanceof byte[])
            ((JEditorPane)editor).setText(new String((byte[])value));
        else
            throw new RuntimeException("Unhandled type "+value.getClass().getName());
    }

    public Object getValue() {
        Object value = ((JEditorPane)editor).getText();
        if (type.getActualClass()==byte[].class)
            value  = ((String)value).getBytes();
        return value;
    }

    public void onSetFocus(Object extraOption) {
        loggerFocus.debug("TextEditor.onSetFocus "+extraOption);
        requestFocus();
        if (extraOption!=null && extraOption instanceof Integer) {
            int line = ((Integer)extraOption).intValue();
            String text = ((JEditorPane)editor).getText();
            int index = 0;
            int previndex = 0;
            for (int i=0; i<line; i++) {
                previndex = index;
                index = text.indexOf('\n',index+1);
                loggerFocus.debug("TextEditor.onSetFocus "+previndex+","+index);
            }
            loggerFocus.debug("TextEditor.onSetFocus "+previndex+","+index);
            ((JEditorPane)editor).setSelectionStart(previndex+1);
            ((JEditorPane)editor).setSelectionEnd(index);
        }
    }

    /**
     * Set the focus on the TextEditor
     */
    public void requestFocus() {
        ((JEditorPane)editor).requestFocus();
        loggerFocus.debug("focusing "+editor.getClass().getName());
    }
}
