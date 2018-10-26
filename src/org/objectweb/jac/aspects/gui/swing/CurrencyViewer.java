/*
  Copyright (C) 2001 Laurent Martelli
  
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
import javax.swing.JComponent;
import javax.swing.JLabel;

/**
 * <p>A Swing viewer for the currency abstract type. It displays the
 * name of the currency after the value.
 */

public class CurrencyViewer extends AbstractFieldView
   implements FieldView
{
   String currency;
   JLabel label;

   public CurrencyViewer(Object value, Object substance, FieldItem field) {
      super(substance,field);
      currency = GuiAC.getDefaultCurrency();
      label = new JLabel();
      setValue(value);
      add(label);
   }

   public void setValue(Object value) {
      label.setText(value.toString()+" "+currency);
   }

   protected JComponent getComponent() {
      return label;
   }
}
