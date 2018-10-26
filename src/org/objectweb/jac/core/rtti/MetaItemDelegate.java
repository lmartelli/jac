/*
  Copyright (C) 2001 Renaud Pawlak, Laurent Martelli
  
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

package org.objectweb.jac.core.rtti;

import java.lang.reflect.*;

/**
 * This class defines the super class for all the meta items whithin
 * the rtti aspect.<p>
 *
 * A meta item encapsulates a <code>java.lang.reflect</code> item so
 * that the user of this item can add extra informations
 * (attributes). Typically this feature can be used by an aspect to
 * tag an element of the model to react to this tag later on.<p>
 *
 * Examples:<p> <ul> 
 *
 * <li>A persistence aspect can tag some field persistent, add methods
 * that change the object states even if they do not fit naming
 * conventions...<p>
 *
 * <li>A GUI can tag a given field to be invisible or a class to be
 * displayed by a special view (eg a given Swing component)...
 *
 * </ul>
 *
 * @author Renaud Pawlak
 * @author Laurent Martelli
 */

public abstract class MetaItemDelegate extends MetaItem {

    /** Stores the corresponding <code>jav.lang.reflect</code>
        meta item. */
    protected Object delegate;

    /** Stores the parent of this meta item */
    protected MetaItemDelegate parent = null;

    public Object getDelegate() {
        return delegate;
    }

    /**
     * Sets the parent.<p>
     *
     * For any type of meta item, the only possible type of the parent
     * is a class item. For a class item, the parent is
     * <code>null</code> in most cases (except in the case of
     * inner-classes).
     *
     * @param parent the new parent 
     */
    public final void setParent(MetaItemDelegate parent) 
        throws InvalidParentException 
    {
        if ( ! (parent.getClass() == ClassItem.class) ) {
            throw new InvalidParentException();
        }
        this.parent = parent;
    }

    /**
     * Gets the parent class item of this meta item.<p>
     *
     * @return the parent 
     */
    public final MetaItemDelegate getParent() {
        return parent;
    }
   
    /**
     * Default contructor to create a new meta item object.<p>
     *
     * @param delegate the <code>java.lang.reflect</code> actual
     * meta item 
     */
    public MetaItemDelegate(Object delegate) throws InvalidDelegateException {
        if (! (delegate instanceof Member) && ! (delegate instanceof Class)) {
            throw new InvalidDelegateException(delegate, "must be a Member or a Class");
        }
        this.delegate = delegate;
    }

    public MetaItemDelegate() {
        delegate = null;
    }

    /**
     * Get the modifiers (see java.lang.reflect) of the meta item.
     *
     * @return an int representing the modifiers
     * @see java.lang.reflect.Modifier 
     */
    public int getModifiers() {
        return ((Member)delegate).getModifiers();
    }

    /**
     * This method gets the type of the meta item by delegating to the
     * actual <code>java.lang.reflect</code> meta item.<p>
     *
     * @return the item type 
     */
    public abstract Class getType();

    /**
     * Overloads the default method to call the delegate one.
     *
     * @return a textual representation of the object 
     */
    public String toString() {
        return getName();
    }

}

class InvalidParentException extends Exception {
}

