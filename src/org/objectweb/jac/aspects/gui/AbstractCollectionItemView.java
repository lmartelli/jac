/*
  Copyright (C) 2002 Julien van Malderen <julien@aopsys.com>

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
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA */

package org.objectweb.jac.aspects.gui;

import org.objectweb.jac.core.rtti.CollectionItem;

/**
 * Interface for the component used to display elements of a
 * collection, with "prev" and "next" buttons to go to the previous or
 * next element of the collection easily.
 */

public interface AbstractCollectionItemView
{
   View getView();

   /**
    * Sets the collection item associated with the view
    * @param coll a collection item
    */
   void setCollection(CollectionItem coll);
   
   /**
    * Gets the collection item associated with the view
    * @return collection item
    */
   CollectionItem getCollection();
   
   /**
    * Sets the position in the collection of the current item
    * @param index position in collection (from 0 to collection size-1)
    */
   void setCurrent(int index);

   /**
    * Gets the position in the collection of the current item
    * @return position in collection
    */
   int getCurrent();

   /**
    * Displays next object in collection
    */
   void onNextInCollection();
   
   /**
    * Displays previous object in collection
    */
   void onPreviousInCollection();

   /**
    * Displays the collection instead of the current item
    */
   void onBackToCollection();
   
   /**
    * Remove current object from collection
    */
   void onRemoveInCollection();
}

