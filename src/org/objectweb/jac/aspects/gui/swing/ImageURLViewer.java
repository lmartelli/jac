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
import org.objectweb.jac.core.rtti.FieldItem;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;

/**
 * A Swing viewer component for imageURL values.
 * @see java.net.URL
 */
public class ImageURLViewer extends AbstractFieldView 
    implements FieldView
{
    JLabel label = new JLabel();
   
    /**
     * Constructs a new imageURL editor. 
     */
    public ImageURLViewer(URL value, Object substance, FieldItem field) {
        super(substance,field);
        setValue(value);
        add(label);
    }

    public ImageURLViewer() {
        add(label);
    }

    /**
     * Sets the value of the edited date.
     *
     * @param value an URL
     */
    public void setValue(Object value) {
        if (value!=null)
            label.setIcon(new ImageIcon((URL)value));
        else
            label.setIcon(null);
    }

    protected JComponent getComponent() {
        return label;
    }
}
