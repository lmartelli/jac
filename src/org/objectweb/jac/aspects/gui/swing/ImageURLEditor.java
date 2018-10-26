/*
  Copyright (C) 2001 Renaud Pawlak, Laurent Martelli
  
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
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA */

package org.objectweb.jac.aspects.gui.swing;


import java.net.URL;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.objectweb.jac.aspects.gui.FieldEditor;
import org.objectweb.jac.aspects.gui.Length;
import org.objectweb.jac.core.rtti.FieldItem;

/**
 * This is a special value editor that allows the user to nicely edit
 * an URL. */

public class ImageURLEditor extends AbstractFieldEditor
    implements FieldEditor 
{

    protected JLabel image;
    protected URLEditor urlEditor;

    /**
     * Constructs a new URL editor.
     */
    public ImageURLEditor(Object substance, FieldItem field) {
        super(substance,field);
        setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );
        image = new JLabel();
        urlEditor = new URLEditor(substance,field);
        add(image);
        add(urlEditor);
    }

    public void setValue(Object value) {
        urlEditor.setValue(value);
        image.setIcon(value != null ? new ImageIcon((URL)value) : null);
    }

    public Object getValue() {
        return urlEditor.getValue();
    }

    public void setSize(Length width, Length height) {
        super.setSize(width,height);
        urlEditor.setSize(width,height);
    }

    public void onSetFocus(Object extra) {
        urlEditor.onSetFocus(extra);
    }

    protected JComponent getComponent() {
        return urlEditor;
    }
}
