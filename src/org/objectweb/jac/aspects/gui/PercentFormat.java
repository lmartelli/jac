/*
  Copyright (C) 2003 Laurent Martelli <laurent@aopsys.com>

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
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA */

package org.objectweb.jac.aspects.gui;

import java.text.DecimalFormat;
import java.text.ParsePosition;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.MetaItem;
import org.objectweb.jac.core.rtti.RttiAC;

public class PercentFormat extends FloatFormat {
    
    public PercentFormat(FieldItem field) {
        super(field);
    }
    
    public String format(double value) {
        String strValue = null;
        Class type = field.getType();
        double editedValue = value;
        if (type == float.class || type == Float.class ||
            type == double.class || type == Double.class)
            editedValue *= 100;
        return format.format(editedValue)+"%";
    }

    public Number parseNumber(String str, ParsePosition pos) {
        String string = ((String)str).trim();
        
        if (string==null || string.length()==0)
            return null;
        if (string.endsWith("%")) {
            string = string.substring(0,string.length()-1).trim();
        }

        Class type = field.getType();

        Number number = format.parse(string,pos);

        if (type == int.class || type == Integer.class) {
            return new Integer(number.intValue());
        } else if (type == short.class || type == Short.class) {
            return new Short(number.shortValue());
        } else if (type == long.class || type == Long.class) {
            return new Long(number.longValue());
        } else if (type == float.class || type == Float.class) {
            return new Float(number.floatValue()/100);
        } else if (type == double.class || type == Double.class) {
            return new Double(number.doubleValue()/100);
        } else {
            throw new RuntimeException("PercentEditor: Unhandled type "+type.getName());
        }
    }
}

