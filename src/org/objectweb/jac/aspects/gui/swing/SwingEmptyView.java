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


import org.objectweb.jac.aspects.gui.FieldView;
import org.objectweb.jac.core.rtti.FieldItem;

public class SwingEmptyView extends AbstractView implements FieldView {

    public SwingEmptyView() {
    }

    public void close(boolean validate) {}

    public void setSubstance(Object substance) {}
    public Object getSubstance() {return null; }
    public void setValue(Object value) {}

    public void setField(FieldItem field) {}
    public FieldItem getField() { return null; }
    public void setAutoUpdate(boolean autoUpdate) {}
}
