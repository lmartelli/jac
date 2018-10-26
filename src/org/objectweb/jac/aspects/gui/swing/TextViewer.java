/*
  Copyright (C) 2001 Renaud Pawlak
  
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
import org.objectweb.jac.core.rtti.FieldItem;
import javax.swing.JComponent;
import javax.swing.JTextArea;

/**
 * A Swing viewer for texts.
 */

public class TextViewer extends AbstractFieldView
   implements FieldView 
{
   JTextArea textArea = new JTextArea();

   public TextViewer(Object value, Object substance, FieldItem field) {
      super(substance,field);
      textArea.setEditable(false);
      textArea.setLineWrap(true);
      setValue(value);
      add(textArea);
   }

   public void setValue(Object text) {
      if( text == null ) { 
         textArea.setText(""); 
      } else {
         textArea.setText(text.toString());
      }
   }

   public Object getValue() {
      return textArea.getText();
   }

   protected JComponent getComponent() {
      return textArea;
   }
}

