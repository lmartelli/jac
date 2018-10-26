/*
  Copyright (C) 2001-2002 Renaud Pawlak, Laurent Martelli
  
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

import org.objectweb.jac.aspects.gui.FieldView;
import org.objectweb.jac.aspects.gui.GuiAC;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.lib.Attachment;
import org.objectweb.jac.util.Thumbnail;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;

/**
 * A Swing viewer component for date values.
 */

public class AttachmentViewer extends AbstractFieldView 
   implements FieldView
{
   JLabel label = new JLabel();
   Attachment value;
   
   /**
    * Constructs a new date editor. 
    */

   public AttachmentViewer(Object value, Object substance, FieldItem field) {
      super(substance,field);
      setValue(value);
      add(label);
   }

   public AttachmentViewer() {
      isCellViewer = true;
      setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
      label.setAlignmentX((float)0.5);
      add(label);
   }

   /**
    * Sets the value of the edited date.
    *
    * @param newValue a <code>Date</code> instance 
    */
   public void setValue(Object newValue) {
      //      System.out.println("AttachmentViewer.setValue "+newValue);
      if (newValue!=null) {
         if (value==newValue)
            return;
         Attachment value = (Attachment)newValue;
         
         //         System.out.println("new value "+newValue);

         if (value.getMimeType()==null) {
            label.setIcon(null);
            label.setText(value.getName());
         } else if (value.getMimeType().startsWith("image/")) {
            if (isCellViewer) {
               byte[] thumb = null;
               try {
                  thumb = Thumbnail.createThumbArray(
                     value.getData(),
                     GuiAC.THUMB_MAX_WIDTH,GuiAC.THUMB_MAX_HEIGHT,
                     GuiAC.THUMB_QUALITY);
               } catch(Exception e) {
                  logger.error("Failed to create thumbnail for "+
                               substance+"."+field.getName(),e);
               }
               label.setIcon(new ImageIcon(thumb));
               setPreferredSize(label.getPreferredSize());
            } else {
               label.setIcon(new ImageIcon(value.getData()));
            }
         } else {
            label.setIcon(null);
            label.setText(value.getName());
         }
      } else {
         value = (Attachment)newValue;
         label.setIcon(null);
         setPreferredSize(label.getPreferredSize());
      }
   }

   protected JComponent getComponent() {
      return label;
   }
}
