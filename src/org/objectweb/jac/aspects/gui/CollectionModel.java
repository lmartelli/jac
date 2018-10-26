/*
  Copyright (C) 2001-2002 Laurent Martelli <laurent@aopsys.com>
  
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

import org.objectweb.jac.core.rtti.CollectionItem;


/**
 * An abstract representation of a collection. */

public interface CollectionModel extends Model {
   /**
    * Gets the number of rows of this collection. 
    * @return the number of rows
    */ 
   int getRowCount();

   /**
    * Returns the object at the row represented by the given index.
    *
    * @param index a row index */
   Object getObject(int index);

   /**
    * Returns the index of an object in the collection
    * @param object the object whose index to find
    * @return the index of object, or -1 if the object is not in the
    * collection
    */
   int indexOf(Object object);

   /**
    * Get the collection item represented by the model
    * @return a collection item
    */
   CollectionItem getCollection();
}
