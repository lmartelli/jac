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

public class DateHourFormat extends DateFormat {
    public DateHourFormat(FieldItem field) {
        super(field);
    }

    protected SimpleDateFormat getFormat(FieldItem field) {
        return new SimpleDateFormat(GuiAC.getDateHourFormat());
    }    
}

