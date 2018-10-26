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
 * String converter for <code>java.awt.Rectangle</code>.
 */

public class RectangleStringConverter implements StringConverter {
   
   public String objectToString(Object obj)
   {
      java.awt.Rectangle rect = (java.awt.Rectangle)obj;
      return rect.x+","+rect.y+","+rect.width+","+rect.height;
   }
   
   public Object stringToObject(String str)
   {
      int start = 0;
      int end = str.indexOf(",");
      java.awt.Rectangle rect = new java.awt.Rectangle();
      rect.x = Integer.parseInt(str.substring(start,end));
      start = end+1;
      end = str.indexOf(",",start);
      rect.y = Integer.parseInt(str.substring(start,end));
      start = end+1;
      end = str.indexOf(",",start);
      rect.width = Integer.parseInt(str.substring(start,end));
      start = end+1;
      rect.height = Integer.parseInt(str.substring(start));
      return rect;
   }
}
