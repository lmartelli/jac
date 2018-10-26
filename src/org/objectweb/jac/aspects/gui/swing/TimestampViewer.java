/*
  Copyright (C) 2004 Laurent Martelli <laurent@aopsys.com>
  
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

import java.util.Date;
import org.objectweb.jac.aspects.gui.DateHourFormat;
import org.objectweb.jac.core.rtti.FieldItem;

/**
 * A Swing viewer component for date values.
 */

public class TimestampViewer extends FormatViewer
{   
    /**
     * Constructs a new date viewer. 
     */
    public TimestampViewer(Object value, Object substance, FieldItem field) {
        super(value,substance,field);
    }

    Date date;

    public TimestampViewer() {
    }

    public void setValue(Object value) {
        if (value!=null) {
            if (date==null) {
                // We initialize date here because setValue() is
                // called from the super constructor, before date
                // would be initialized by this constructor
                date = new Date();
            }
            date.setTime(((Long)value).longValue());
            label.setText(format.format(date));
        } else {
            label.setText("");
        }
    }

    protected void initFormat(FieldItem field) {
        format = new DateHourFormat(field);
    }
}
