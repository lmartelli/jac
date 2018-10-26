/*
  Copyright (C) 2003 Laurent Martelli <laurent@aopsys.com>
  
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

import javax.swing.JComponent;
import javax.swing.JLabel;
import org.objectweb.jac.aspects.gui.FieldView;
import org.objectweb.jac.aspects.gui.Format;
import org.objectweb.jac.core.rtti.FieldItem;

/**
 * A Swing viewer component for date values.
 */

public abstract class FormatViewer extends AbstractFieldView
    implements FieldView
{   
    /** Stores the default format of the date */
    protected Format format;
    protected JLabel label = new JLabel();

    /**
    * Constructs a new date editor. */

    public FormatViewer(Object value, Object substance, FieldItem field) {
        super(substance,field);
        initFormat(field);
        setValue(value);
        add(label);
    }

    public FormatViewer() {
        setTableFont();
        setLayout();
        add(label);
    }

    protected abstract void initFormat(FieldItem field);

    protected void setLayout() {
    }

    public void setField(FieldItem field) {
        super.setField(field);
        initFormat(field);
    }

    // FieldView interface

    public void setValue(Object value) {
        if (value!=null)
            label.setText(format.format(value));
        else
            label.setText("");
    }

    protected JComponent getComponent() {
        return label;
    }
}
