/*
  Copyright (C) 2004 Laurent Martelli <laurent@aopsys.com>

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

import java.text.ParsePosition;
import org.objectweb.jac.core.rtti.FieldItem;
import java.awt.Point;

public class PointFormat implements Format {

    protected FieldItem field;
    
    public PointFormat(FieldItem field) {
        this.field = field;
    }
    
    public String format(Object value) {
        if (value!=null) {
            Point p = (Point)value;
            return p.x+","+p.y;
        } else {
            return "";
        }
    }

    public Object parse(String str, ParsePosition pos) {
        int coma = str.indexOf(",");
        if (coma!=-1) {
            return new Point(Integer.parseInt(str.substring(0,coma).trim()),
                             Integer.parseInt(str.substring(coma+1,str.length()).trim()));
        } else {
            throw new RuntimeException("Malformed point string: \""+str+"\"");
        }
    }
}
