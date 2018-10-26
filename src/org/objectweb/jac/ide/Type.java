/*
  Copyright (C) 2002-2003 Renaud Pawlak <renaud@aopsys.com>

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

package org.objectweb.jac.ide;

/**
 * A representation of a type. It can be an external type --
 * e.g. <code>java.util.Vector</code> or an internally defined class
 * or typed element.
 *
 * @see Class
 * @see Aspect */

public class Type extends ModelElement {

   public Type() {}

   public Type(String name, String packagePath) {
      super(name);
      this.packagePath = packagePath;
   }

   public String getFullName() {
      if (packagePath != null && !packagePath.equals("")) {
         return packagePath+"."+name;
      } else {
         return name;
      }
   }

   public boolean isPrimitive() {
      return Character.getType(name.charAt(0))==Character.LOWERCASE_LETTER;
   }

   String packagePath="";
   
   /**
    * Get the value of packagePath.
    * @return value of packagePath.
    */
   public String getPackagePath() {
      return packagePath;
   }
   
   /**
    * Set the value of packagePath.
    * @param v  Value to assign to packagePath.
    */
   public void setPackagePath(String  v) {
      this.packagePath = v;
   }
   
}
