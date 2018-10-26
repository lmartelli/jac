/*
  Copyright (C) 2002 Julien van Malderen.

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

package org.objectweb.jac.aspects.persistence;

/**
 * String converter for <code>java.awt.Dimension</code>.
 */

public class DimensionStringConverter implements StringConverter {
   
   public String objectToString(Object obj)
   {
      java.awt.Dimension dim = (java.awt.Dimension)obj;
      return dim.width+","+dim.height;
   }
   
   public Object stringToObject(String str)
   {
      int start = 0;
      int end = str.indexOf(",");
      java.awt.Dimension d = new java.awt.Dimension();
      d.width = Integer.parseInt(str.substring(start,end));
      start = end+1;
      d.height = Integer.parseInt(str.substring(start));
      return d;
   }
}
