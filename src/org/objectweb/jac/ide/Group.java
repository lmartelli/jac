/*
  Copyright (C) 2002 Renaud Pawlak <renaud@aopsys.com>

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

public class Group extends ModelElement {

   String objectPCD;
   
   /**
    * Get the value of objectPCD.
    * @return value of objectPCD.
    */
   public String getObjectPCD() {
      return objectPCD;
   }
   
   /**
    * Set the value of objectPCD.
    * @param v  Value to assign to objectPCD.
    */
   public void setObjectPCD(String  v) {
      this.objectPCD = v;
   }
   
   String classPCD;
   
   /**
    * Get the value of classPCD.
    * @return value of classPCD.
    */
   public String getClassPCD() {
      return classPCD;
   }
   
   /**
    * Set the value of classPCD.
    * @param v  Value to assign to classPCD.
    */
   public void setClassPCD(String  v) {
      this.classPCD = v;
   }

   Package container;
   
   /**
    * Get the value of container.
    * @return value of container.
    */
   public Package getContainer() {
      return container;
   }
   
   /**
    * Set the value of container.
    * @param v  Value to assign to container.
    */
   public void setContainer(Package  v) {
      this.container = v;
   }

}
