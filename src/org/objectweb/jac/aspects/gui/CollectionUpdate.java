/*
  Copyright (C) 2002 Laurent Martelli <laurent@aopsys.com>
  
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
 * This interface defines callback methods used to notify that a
 * collection was updated (that is, an object was added to it or
 * removed from it).
 */
public interface CollectionUpdate {

   /**
    * Upcalled when the collection is changed (with a set or other
    * methods such as clear, removeAll, addAll, ...).
    * 
    * @param substance the object of which a field was updated
    * @param collection the updated collection
    * @param value the new collection
    * @param param extra data
    *
    * @see ViewControlWrapper#registerCollection(Wrappee,CollectionItem,CollectionUpdate,Object) 
    */
   void onChange(Object substance, CollectionItem collection, Object value,
                 Object param);

   /**
    * Upcalled when an item is added in a collection.
    * 
    * @param substance the object of which a collection was updated
    * @param collection the updated collection
    * @param value the collection's value
    * @param added the value added to the collection
    * @param param extra data (e.g. index)
    *
    * @see ViewControlWrapper#registerCollection(Wrappee,CollectionItem,CollectionUpdate,Object) 
    */
   void onAdd(Object substance, CollectionItem collection, Object value,
              Object added, Object param);

   /**
    * Upcalled when an item is removed from a collection.
    * 
    * @param substance the object of which a collection was updated
    * @param collection the updated collection
    * @param value the collection's value
    * @param removed the removed item
    * @param param extra data (e.g. index)
    *
    * @see ViewControlWrapper#registerCollection(Wrappee,CollectionItem,CollectionUpdate,Object) 
    */
   void onRemove(Object substance, CollectionItem collection, Object value,
                 Object removed, Object param);
}
