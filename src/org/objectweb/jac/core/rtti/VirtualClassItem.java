/*
  Copyright (C) 2001-2003 Renaud Pawlak <renaud@aopsys.com>, 
                          Laurent Martelli <laurent@aopsys.com>
  
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


package org.objectweb.jac.core.rtti;

/**
 * This class defines a meta item that corresponds to the
 * <code>java.lang.reflect.Class</code> meta element.<p>
 *
 * @author Renaud Pawlak
 * @author Laurent Martelli
 */

public class VirtualClassItem extends MetaItem {

   String name;
   ClassItem actualType;

   /**
    * Default contructor to create a new virtual class item object.<p>
    *
    * @param name the name of the virtual class to create
    * @param actualType the existing real class it can substituted to
    */
   public VirtualClassItem(String name, ClassItem actualType) {
      this.name = name;
      this.actualType = actualType;
   }

   public String getName() {
      return name;
   }

   public ClassItem getActualType() {
      return actualType;
   }

   public Object getAttribute(String attribute) {
      Object value = super.getAttribute(attribute);
      if (value==null && actualType!=null) {
         value = actualType.getAttribute(attribute);
      }
      return value;
   }

   public String toString() {
      return "VirtualClassItem "+name;
   }
}
