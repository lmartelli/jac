/*
  Copyright (C) 2002 Laurent Martelli <laurent@aopsys.com>

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

package org.objectweb.jac.aspects.integrity;

import org.objectweb.jac.core.rtti.FieldItem;


public class Constraint {
   public static final int DELETE_CASCADE = 0;
   public static final int SET_NULL = 1;
   public static final int FORBIDDEN = 2;

   public FieldItem relation;
   public int constraint;
   public Constraint(FieldItem relation, int constraint) {
      this.relation = relation;
      this.constraint = constraint;
   }
   public static String constraintToString(int constraint) {
      switch (constraint) {
         case DELETE_CASCADE:
            return "DELETE_CASCADE";
         case SET_NULL:
            return "SET_NULL";
         case FORBIDDEN:
            return "FORBIDDEN";
         default:
            return "???";
      }
   }
   public String toString() {
      return "("+relation.getLongName()+","+constraintToString(constraint)+")";
   }
}

