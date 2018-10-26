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
import org.apache.log4j.Logger;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.MetaItem;
import org.objectweb.jac.core.rtti.RttiAC;
import org.objectweb.jac.util.Strings;

public class FloatFormat implements NumberFormat {
    static Logger logger = Logger.getLogger("gui");    

    protected DecimalFormat format;
    protected FieldItem field;
    
    public FloatFormat(FieldItem field) {
        this.field = field;

        MetaItem type = field!=null?RttiAC.getFieldType(field):null;
        String formatString = null;
        if (type!=null)
            formatString = GuiAC.getFloatFormat(type);
        else if (field!=null)
            formatString = GuiAC.getFloatFormat(field);
        else
            formatString = GuiAC.getFloatFormat();

        if (formatString!=null) 
            format = new DecimalFormat(formatString);
        else
            logger.error("No format for "+field);
    }
    
    public String format(Object value) {
        return format((Number)value);
    }

    public String format(Number value) {
        if (value!=null)
            return format(value.doubleValue());
        else
            return "";
    }

    public String format(double value) {
        if (format!=null)
            return format.format(value);
        else
            return ""+value;
    }

    public Object parse(String str, ParsePosition pos) {
        return parseNumber(str,pos);
    }

    public Number parseNumber(String str, ParsePosition pos) {
        String string = str.trim();

        if (Strings.isEmpty(string))
            return null;

        return format.parse(str,pos);
    }

    public Float parseFloat(String str, ParsePosition pos) {
        Number n = parseNumber(str,pos);
        if (n!=null) {
            return new Float(n.floatValue());
        } else {
            return null;
        }
    }

    public Double parseDouble(String str, ParsePosition pos) {
        Number n = parseNumber(str,pos);
        if (n!=null) {
            return new Double(n.doubleValue());
        } else {
            return null;
        }
    }
}

