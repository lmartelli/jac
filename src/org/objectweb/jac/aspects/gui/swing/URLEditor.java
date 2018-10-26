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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import org.objectweb.jac.aspects.gui.FieldEditor;
import org.objectweb.jac.aspects.gui.Length;
import org.objectweb.jac.aspects.gui.ResourceManager;
import org.objectweb.jac.core.rtti.FieldItem;

/**
 * This is a special value editor that allows the user to nicely edit
 * an URL. */

public class URLEditor extends TextFieldEditor
    implements FieldEditor, ActionListener
{
    /**
     * Constructs a new URL editor.
     */
    public URLEditor(Object substance, FieldItem field) {
        super(substance,field);
        textField = new JTextField(10);
        textField.addFocusListener(this);
        add(textField);

        JButton button = new JButton (ResourceManager.getIconResource("edit_icon"));
        button.setToolTipText("Edit");
        button.setActionCommand("choose");
        button.addActionListener(this);
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
            JFileChooser chooser = new JFileChooser();      
            //chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            /*String fileSelectionMode = (String)field.getAttribute(GuiAC.FILE_SELECTION_MODE");
              if(fileSelectionMode!=null) {
              if(fileSelectionMode.equals("DIRECTORIES_ONLY")) {
              chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
              } else if(fileSelectionMode.equals("FILES_AND_DIRECTORIES")) {
              chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
              }
              } 
              String[] fileExtensions = (String[])field.getAttribute(GuiAC.FILE_EXTENSIONS);
              String fileExtensionsDescription = 
              (String)field.getAttribute(GuiAC.FILE_EXTENSIONS_DESCRIPTION");

              if(fileExtensions!=null) {
              CustomizedFileFilter filter = 
              new CustomizedFileFilter(fileExtensions,fileExtensionsDescription);
              chooser.setFileFilter(filter);
              }*/
            int returnVal = chooser.showOpenDialog( this );
         
            if ( returnVal == JFileChooser.APPROVE_OPTION ) {
                textField.setText( "file:" + chooser.getSelectedFile().toString() );
            }
        }
    }

    public Object getValue() {
        String urlString = textField.getText();
        if (urlString.equals("")) {
            return null;
        }
        if (urlString.indexOf(":")==-1) {
            urlString = "file:/"+urlString;
        }
      
        try {
            return new URL(urlString);
        } catch (MalformedURLException e) {
            logger.warn("Malfromed URL: "+urlString);
            return null;
        }
    }

    public void setValue(Object value) {
        super.setValue(value);
        if( value == null ) 
            textField.setText("");
        else
            textField.setText(((URL)value).toString());
    }

}
