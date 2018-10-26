/*
  Copyright (C) 2002 Renaud Pawlak

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

/**
 * This interface implements common methods for view of
 * collections. */

public interface CollectionView extends FieldView {
   /**
    * Sets the selected index of the collection view.
    *
    * @param index the new selected index */
   void setSelected(int index);

   /**
    * Gets the associated collection model. */
   CollectionModel getCollectionModel();

   /**
    * Updates the collection model
    */
   void updateModel(Object substance);

   /**
    * Tells wether the view allows to edit the collection (i.e it has
    * add/remove buttons)
    * @see #setEditor(boolean)
    */
   boolean isEditor();

   /**
    * Set the "editability" of the view. If the view is editable, it
    * has add/remove buttons)
    * @see #isEditor() 
    */
   void setEditor(boolean isEditor);
}
