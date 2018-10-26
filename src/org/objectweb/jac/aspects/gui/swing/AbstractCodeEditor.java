/*
  Copyright (C) 2003 Laurent Martelli <laurent@aopsys.com>
                     Renaud Pawlak <renaud@aopsys.com>

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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JScrollPane;
import org.objectweb.jac.aspects.gui.Length;
import org.objectweb.jac.aspects.gui.swing.EditorScrollPane;
import org.objectweb.jac.core.rtti.FieldItem;

/**
 * A base class for source code editors
 */
public abstract class AbstractCodeEditor extends AbstractFieldEditor
{
    protected SHEditor editor;
    JScrollPane scrollPane;
   
    public AbstractCodeEditor(Object substance, FieldItem field) {
        super(substance,field);
        
        width = new Length("400px");
        height = new Length("400px");

        setLayout(new BorderLayout());
        scrollPane = new EditorScrollPane();
        editor = ((EditorScrollPane)scrollPane).editor;
        add(BorderLayout.CENTER,scrollPane);
        //scrollPane.setPreferredSize(new Dimension(defaultWidth,defaultHeight));
        scrollPane.setMinimumSize(new Dimension(128,64));

        setMinimumSize(new Dimension(128,64));
        //setPreferredSize(new Dimension(defaultWidth,defaultHeight));

        editor.addFocusListener(this);
        editor.addKeyListener(
            new KeyAdapter() {
                    public void keyReleased(KeyEvent e) {
                        if (e.isControlDown() && e.getKeyCode()==KeyEvent.VK_S) {
                            loggerEvents.debug("saving text of "+getField());
                            commit();
                        }
                    }
                });


        init();
    }  

    abstract protected void init();

    // FieldEditor internalView
    public void setValue(Object value) {
        super.setValue(value);
        if (value==null) 
            editor.setText("");
        else if (value instanceof byte[])
            editor.setText(new String((byte[])value));
        else 
            editor.setText((String)value);
    }

    public Object getValue() {
        if (type.getActualClass()==byte[].class)
            return editor.getText().getBytes();
        else
            return editor.getText();
    }

    public void setSize(Length width, Length height) {
        this.width = width;
        this.height = height;
        SwingUtils.setSize(scrollPane, width, height);
    }

    public void onSetFocus(Object extraOption) {
        loggerFocus.debug("AbstactEditor.onSetFocus "+extraOption);
        requestFocus();
        if (extraOption instanceof Integer) {
            int line = ((Integer)extraOption).intValue();
            String text = editor.getText();
            int index = 0;
            int previndex = 0;
            for (int i=0; i<line; i++) {
                previndex = index;
                index = text.indexOf('\n',index+1);
                loggerFocus.debug("AbstactEditor.onSetFocus "+previndex+","+index);
            }
            if (index==-1) {
                index = text.length()-1;
            }
            editor.gotoLine(line);
            editor.setSelectionStart(previndex+1);
            editor.setSelectionEnd(index+1);
            loggerFocus.debug("AbstactEditor.onSetFocus "+previndex+","+index);
        }
    }

    /**
     * Set the focus on the TextEditor
     */
    public void requestFocus() {
        editor.requestFocus();
        loggerFocus.debug("focusing "+editor.getClass().getName());
    }

}

