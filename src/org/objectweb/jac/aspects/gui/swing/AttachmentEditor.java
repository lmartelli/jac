/*
  Copyright (C) 2002 Laurent Martelli <laurent@aopsys.com>
  
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



import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.lib.Attachment;
import org.objectweb.jac.util.Streams;
import org.objectweb.jac.util.WrappedThrowableException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.swing.JFileChooser;

/**
 * This is a special value editor that allows the user to nicely edit
 * an File. 
 */

public class AttachmentEditor extends FileEditor
{
   public AttachmentEditor(Object substance, FieldItem field) {
      super(substance,field);
   }

   public Object getValue() {
      String file = textField.getText();
      if (file.equals("")) {
         return null;
      }
      try {
         return new Attachment(Streams.readStream(new FileInputStream(file)),
                               null,file);
      } catch (IOException e) {
         throw new WrappedThrowableException(e);
      }
   }

   public void setValue(Object value) {
      super.setValue(value);
      if (value==null) 
         textField.setText("");
      else
         textField.setText(((Attachment)value).getName());
   }


   /**
    * Returns a file chooser initialized with the current value
    */
   JFileChooser createFileChooser() {
      return new JFileChooser(new File(textField.getText()));
   }

}
