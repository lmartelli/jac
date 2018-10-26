/*
  Copyright (C) 2002 Julien van Malderen

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
 * The *StringConverter interface that converts Objects into String
 * for stocking, and String into Objects
 */

public interface StringConverter {

   /**
    * Returns a string representation of a field so that it can be
    * stored.<p>
    *
    * @param obj a persistent or primitive object
    * @return a ready to store string representation */

   String objectToString(Object obj);


   /**
    * Returns an object from a string, depending on the needed type.<p>
    *
    * @param str the value in a string format
    * @return an object value deduced from the string representation
    * and from the needed type */

   Object stringToObject(String str);
}
