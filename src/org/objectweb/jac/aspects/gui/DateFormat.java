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
import java.text.SimpleDateFormat;
import java.util.Date;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.MetaItem;
import org.objectweb.jac.core.rtti.RttiAC;
import org.objectweb.jac.util.Strings;

public class DateFormat implements Format {
    protected SimpleDateFormat format;
    protected FieldItem field;
    
    public DateFormat(FieldItem field) {
        this.field = field;
        format = getFormat(field);
    }

    protected SimpleDateFormat getFormat(FieldItem field) {
        return new SimpleDateFormat(GuiAC.getDateFormat());
    }
    
    public String format(Object value) {
        return format.format(value);
    }

    public Object parse(String str, ParsePosition pos) {
        return parseDate(str,pos);
    }

    public Date parseDate(String str, ParsePosition pos) {
        String string = str.trim();

        if (Strings.isEmpty(string))
            return null;

        return format.parse(str,pos);
    }
}

