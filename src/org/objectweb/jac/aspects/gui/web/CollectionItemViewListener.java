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

package org.objectweb.jac.aspects.gui.web;

/**
 * This interface defines a callbacks used when events occur on a collection.
 */

public interface CollectionItemViewListener {

   /**
    * Called when the user wants to go to the next element in the
    * collection
    */
   void onNextInCollection();

   /**
    * Called when the user wants to go to the previous element in the
    * collection
    */
   void onPreviousInCollection();

   /**
    * Called when the user wants to go back to the view of the collection
    */
   void onBackToCollection();

   /**
    * Called when the user wants to remove the selected item from the
    * collection
    */
   void onRemoveInCollection();
}
