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
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA */

package org.objectweb.jac.aspects.timestamp;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;
import java.io.File;

public class Timestamps {
    Hashtable stamps = new Hashtable();

    /**
     * Set the stamp of an object to current time
     * @see #touch(Object)
     */
    public void touch(Object object) {
        stamps.put(object,new Long(System.currentTimeMillis()));
    }

    /**
     * Gets the time an object was last modified, or 0.
     *
     * @param object the object whose timestamp you request
     * @return if object is a file, obect.lastModified(), otherwise
     * the stored timestamp for that object, or if 0 no timestamp is
     * stored for it.
     *
     * @see #touch(Object) 
     */
    public long getStamp(Object object) {
        if (object instanceof File) {
            return ((File)object).lastModified();
        } else {
            Long stamp = (Long)stamps.get(object);
            if (stamp!=null) {
                return stamp.longValue();
            } else {
                return 0;
            }
        }
    }

    /**
     * Delete all timestamps
     */
    public void touchAll() {
        Long time = new Long(System.currentTimeMillis());
        Iterator i = stamps.entrySet().iterator();
        while (i.hasNext()) {
            Entry entry = (Entry)i.next();
            entry.setValue(time);
        }
    }
}
