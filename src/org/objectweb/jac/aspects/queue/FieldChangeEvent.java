/*
  Copyright (C) 2003 Laurent Martelli <laurent@aopsys.com>
  
  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 2 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  USA */

package org.objectweb.jac.aspects.queue;

import org.objectweb.jac.core.rtti.FieldItem;

public class FieldChangeEvent {
   Object substance;
   FieldItem field;
   Object oldValue;
   Object newValue;

   public FieldChangeEvent(Object substance, FieldItem field, 
                           Object oldValue, Object newValue) {
      this.substance = substance;
      this.field = field;
      this.oldValue = oldValue;
      this.newValue = newValue;
   }
   /**
    * Gets the object whose field was modified.
    */
   public Object getSubstance() {
      return substance;
   }
   
   /**
    * Gets the modified field item.
    */
   public FieldItem getField() {
      return field;
   }

   /**
    * Gets the value of the field before the modification.
    */
   public Object getOldValue() {
      return oldValue;
   }

   /**
    * Gets the value of the field after the modification.
    */
   public Object getNewValue() {
      return newValue;
   }

   public String toString() {
      return substance+"."+field.getName()+" changed from "+
         oldValue+" to "+newValue;
   }
}
