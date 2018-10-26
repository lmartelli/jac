/*
  Copyright (C) 2001-2003 Renaud Pawlak, Laurent Martelli
  
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
import org.objectweb.jac.util.Files;

/**
 * This is a special value editor that allows the user to nicely edit
 * a File. 
 */
public class FileEditor extends AbstractFileEditor
    implements FieldEditor, ActionListener
{
   /**
    * Constructs a new File editor.
    */
    public FileEditor(Object substance, FieldItem field) {
        super(substance,field);
    }

    /**
     * Returns a file chooser initialized with the current value
     */
    JFileChooser createFileChooser() {
        return new JFileChooser((File)getValue());
    }

    public Object getValue() {
        String file = textField.getText();
        if (file.equals("")) {
            return null;
        }

        return createFileInstance(Files.expandFileName(file));
    }

    /**
     * Create a new instance of File. Override this method to
     * instanciate a subclass of java.util.File.
     * @param path the path to create a file for
     */
    protected File createFileInstance(String path) {
        if (type!=null)
            try {
                return (File)type.newInstance(new Object[] {path});
            } catch(Exception e) {
                logger.error("FileEditor.createFileInstance: failed to instanciate "+type+
                             ", falling back on java.io.File");
                return new File(path);
            }
        else 
            return new File(path);
    }

    public void setValue(Object value) {
        super.setValue(value);
        if (value==null) 
            textField.setText("");
        else
            textField.setText(value.toString());
    }
}
