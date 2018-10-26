/*
  Copyright (C) 2001 Laurent Martelli
  
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

public interface CollectionListener {
    /**
     * Called when the user selects an item to view it.
     *
     * @param index the index of the element to view
     */
    void onView(int index);

    /**
     * Called when the user selects an item to view it.
     *
     * @param name the name of the object to view
     *
     * @see #onView(int)
     */
    void onViewObject(String name);

    /**
     * Called when the user selects an item to remove it.
     *
     * @param index the index of the element to remove
     */
    void onRemove(int index);

    /**
     * Called when a method is called on an object belonging to the
     * collection.
     * @param index the index of the element to view
     * @param methodName the name of the method to invoke */
    void onTableInvoke(int index,String methodName);

    /**
     * Called when the user wants to add an object to the collection.
     * @see #onAddExistingToCollection()
     */
    void onAddToCollection();

    /**
     * Called when the user wants to add an object to the collection,
     * without creating a new one even if the collection is "autocreate".
     * @see #onAddToCollection() */
    void onAddExistingToCollection();

    /**
     * Called when the user wants to remove an object to the collection.
     */
    void onRemoveFromCollection();

    /**
     * Display the next page of items
     */
    void onNext();

    /**
     * Display the previous page of items
     */
    void onPrevious();
   
    /**
     * Display to the first page of items
     */
    void onFirst();

    /**
     * Display to the last page of items
     */
    void onLast();

    /**
     * Call when a parameter of the view has changed and the view
     * should be refresh to take it into account.
     */
    void onRefreshCollection();
}

