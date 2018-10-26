/*
  Copyright (C) 2004 Laurent Martelli <laurent@aopsys.com>
  
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
 * Event for a method invocation
 */
public class RemoveEvent extends CollectionEvent {
    public RemoveEvent(View source, 
                       Object substance, CollectionItem collection,
                       Object removed) {
        super(source,substance,collection);
        this.removed = removed;
    }

    Object removed;
    public Object getRemoved() {
        return removed;
    }
    public void setRemoved(Object newRemoved) {
        this.removed = newRemoved;
    }

    public String toString() {
        return "InvokeEvent{remove "+removed+
            " from "+substance+"."+collection.getName()+"}";
    }
}
