/*
  Copyright (C) 2001 Laurent Martelli <laurent@aopsys.com>

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

package org.objectweb.jac.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;


/**
 * A Predicate class.
 *
 * @author <a href="mailto:laurent@aopsys.com">Laurent Martelli</a> 
 */

public abstract class Predicate {
    public abstract boolean apply(Object object);
    /**
     * Keeps only the items matching the predicate from a collection
     * @param in filter items from that collection
     * @param out matching items are added to this collection
     */
    public void filter(Collection in, Collection out) {
        Iterator i = in.iterator();
        while (i.hasNext()) {
            Object item = i.next();
            if (apply(item)) {
                out.add(item);
            }
        }
    }
}
