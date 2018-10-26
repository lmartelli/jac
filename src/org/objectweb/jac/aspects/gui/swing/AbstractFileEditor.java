/*
  Copyright (C) 2004 Renaud Pawlak, Laurent Martelli
  
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
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import org.objectweb.jac.aspects.gui.FieldEditor;
import org.objectweb.jac.aspects.gui.Length;
import org.objectweb.jac.aspects.gui.ResourceManager;
import org.objectweb.jac.core.rtti.FieldItem;

/**
 * Base class for file related types. It shows a TextField and a
 * button to show a file chooser.
 */
public abstract class AbstractFileEditor extends TextFieldEditor
    implements FieldEditor, ActionListener
{
   /**
    * Constructs a new File editor.
    */
    public AbstractFileEditor(Object substance, FieldItem field) {
        super(substance,field);
        textField = new JTextField(20);
        textField.addFocusListener(this);
        add(textField);

        JButton button = new JButton(ResourceManager.getIconResource("open_icon"));
        button.setToolTipText("Edit");
        button.setActionCommand("choose");
        button.addActionListener(this);
        button.setMargin(new Insets(1,1,1,1));
        add(button);
    }

    /**
     * Handles the actions performed by the users on this view.
     *
     * <p>On an URL editor, a "choose" action can be performed to allow
     * the user to open a file chooser box and to navigate within the
     * file system to choose his file.
     *
     * @param evt the performed action 
     */
    public void actionPerformed(ActionEvent evt) {
        if (evt.getActionCommand().equals("choose")) {
            JFileChooser chooser = createFileChooser();
            int returnVal = chooser.showOpenDialog(this);
         
            if (returnVal==JFileChooser.APPROVE_OPTION) {
                textField.setText(chooser.getSelectedFile().toString());
            }
        }
    }

    /**
     * Returns a file chooser initialized with the current value
     */
    JFileChooser createFileChooser() {
        return new JFileChooser();
    }

    public void setValue(Object value) {
        super.setValue(value);
        if (value==null) 
            textField.setText("");
        else
            textField.setText(value.toString());
    }
}
