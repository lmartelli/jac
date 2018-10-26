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
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA */

package org.objectweb.jac.aspects.gui;

import org.objectweb.jac.core.rtti.CollectionItem;

/**
 * A class that represents a position in a given collection. */

public class CollectionPosition
{
   CollectionItem collection;
   CollectionView collectionView;
   int index;
   Object substance;

   /**
    * Constructs a new position.
    *
    * @param collectionView the collection view that corresponds to
    * the substance
    * @param collection the substance collection
    * @param index the row index of this position 
    * @param substance the object that holds the collection */
   public CollectionPosition(CollectionView collectionView,
                             CollectionItem collection, int index,
                             Object substance) {
      this.collectionView = collectionView;
      this.collection = collection;
      this.index = index;
      this.substance = substance;
   }

   /**
    * Returns the collection view. */
   public CollectionView getCollectionView() {
      return collectionView;
   }
   
   /**
    * Returns the collection. */
   public CollectionItem getCollection() {
      return collection;
   }
   
   /**
    * The index of this position in the collection. */
   public int getIndex() {
      return index;
   }
   
   /**
    * Returns the substance that holds the collection. */ 
   public Object getSubstance() {
      return substance;
   }
}
