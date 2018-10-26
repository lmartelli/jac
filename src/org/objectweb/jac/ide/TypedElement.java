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

public abstract class TypedElement extends ModelElement implements Typed {
   
   Type type = Projects.types.resolveType("void");
   
   /**
    * Get the value of type.
    * @return value of type.
    */
   public Type getType() {
      return type;
   }
   
   /**
    * Set the value of type.
    * @param v  Value to assign to type.
    */
   public void setType(Type v) {
      this.type = v;
   }

   public String getPrototype() {
      return type.getGenerationFullName()+(array?"[]":"")+" "+getGenerationName();
   }

   public String getToString() {
      return name+":"+type.getName()+(array?"[]":"");
   }

   public String getTypeName() {
      return type.getGenerationFullName()+(array?"[]":"");
   }

   boolean array=false;
   
   /**
    * Get the value of array.
    * @return value of array.
    */
   public boolean isArray() {
      return array;
   }
   
   /**
    * Set the value of array.
    * @param v  Value to assign to array.
    */
   public void setArray(boolean  v) {
      this.array = v;
   }
      
}
