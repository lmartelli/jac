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

package org.objectweb.jac.aspects.gui.web;

import java.text.ParsePosition;
import org.objectweb.jac.aspects.gui.FloatFormat;
import org.objectweb.jac.aspects.gui.Length;
import org.objectweb.jac.aspects.gui.NumberFormat;
import org.objectweb.jac.core.rtti.FieldItem;

/**
 * HTML editor and viewer for percentage values. It can handle short,
 * int, long, float, and double. In the case of short, int and long,
 * the percentage value is considered to be between 0 and 100, while
 * in the case of float and double it must be between 0 and 1.
 */
public class FloatEditor extends FormatEditor 
    implements HTMLEditor
{
    public FloatEditor(Object substance, FieldItem field) {
        super(substance,field);
        width = new Length("15ex");
    }

    protected void initFormat(FieldItem field) {
        format = new FloatFormat(field);
    }

    public Object parse(String s) {
        ParsePosition pos = new ParsePosition(0);        
        Class cl = type.getActualClass();
        if (cl==float.class || cl==Float.class) {
            return ((NumberFormat)format).parseFloat(s,pos);
        } else if (cl==double.class || cl==Double.class) {
            return ((NumberFormat)format).parseDouble(s,pos);
        } else {
            throw new RuntimeException("FloatEditor: Unhandled type "+type.getName());
        }
    }
}
