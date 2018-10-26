/*
  Copyright (C) 2001-2003 Laurent Martelli <laurent@aopsys.com>
  
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

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import org.objectweb.jac.aspects.gui.FieldUpdate;
import org.objectweb.jac.aspects.gui.FieldView;
import org.objectweb.jac.core.rtti.FieldItem;



public class BooleanViewer extends AbstractFieldView 
    implements FieldView, FieldUpdate 
{
    JCheckBox checkBox = new JCheckBox();

    public BooleanViewer(Boolean value, Object substance, FieldItem field) {
        super(substance,field);
        checkBox.setEnabled(false);
        add(checkBox);
    }

    public BooleanViewer() {
        setLayout(new GridLayout(1,1));
        checkBox.setVerticalAlignment(JCheckBox.CENTER);
        checkBox.setHorizontalAlignment(JCheckBox.CENTER);
        checkBox.setMargin(new Insets(0,0,0,0));
        checkBox.setBorder(BorderFactory.createEmptyBorder());
        add(checkBox);
    }
    
    public void setValue(Object value) {
        checkBox.setSelected(((Boolean)value).booleanValue());
    }

    protected JComponent getComponent() {
        return checkBox;
    }

    public Dimension getPreferredSize() {
        return checkBox.getPreferredSize();
    }
}
