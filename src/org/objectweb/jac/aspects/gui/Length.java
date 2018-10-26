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

  You should have received a copy of the GNU Lesser General Public
  License along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  USA */

package org.objectweb.jac.aspects.gui;

public class Length {
    public float value;
    public Unit unit;

    public Length(float value, Unit unit) {
        this.value = value;
        this.unit = unit;
    }

    public Length(String str) {
        str = str.toLowerCase();
        if (str.endsWith("em")) {
            unit = Unit.EM;
        } else if (str.endsWith("ex")) {
            unit = Unit.EX;
        } else if (str.endsWith("px")) {
            unit = Unit.PX;
        } else {
            throw new RuntimeException("Unrecognized length unit for "+str);
        }
        value = Float.parseFloat(str.substring(0, str.length()-2));
    }

    public String toString() {
        return value+unit.toString();
    }
}
