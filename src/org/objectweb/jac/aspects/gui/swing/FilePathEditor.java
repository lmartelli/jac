/*
  Copyright (C) 2004 Laurent Martelli <laurent@aopsys.com>
  
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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import org.objectweb.jac.aspects.gui.FieldEditor;
import org.objectweb.jac.aspects.gui.ResourceManager;
import org.objectweb.jac.core.rtti.FieldItem;

/**
 * This is a special value editor that allows the user to nicely edit
 * a file path. 
 */
public class FilePathEditor extends AbstractFileEditor
    implements FieldEditor, ActionListener
{
   /**
    * Constructs a new File editor.
    */
    public FilePathEditor(Object substance, FieldItem field) {
        super(substance,field);
    }

    public Object getValue() {
        return textField.getText();
    }

    public void setValue(Object value) {
        super.setValue(value);
        if (value==null) 
            textField.setText("");
        else
            textField.setText((String)value);
    }
}
