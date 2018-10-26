/*
  Copyright (C) 2002 Laurent Martelli
  
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

package org.objectweb.jac.aspects.gui;

/**
 * This interface defines a callback method used to notify that
 * an object was updated.
 */
public interface ObjectUpdate {
   /**
    * The callback method.
    *
    * @param object the updated object
    * @param param extra data
    * @see ViewControlWrapper#registerObject(Wrappee,ObjectUpdate,Object)
    */
   void objectUpdated(Object object, Object param);
}
