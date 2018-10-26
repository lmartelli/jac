/*
  Copyright (C) 2003 Laurent Martelli <laurent@aopsys.com>

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

package org.objectweb.jac.aspects.naming;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

/**
 * Generate names for objects using the classname and an integer
 * counter.
 */
public class NameGenerator extends Hashtable {

    /**
     * Creates a new NameGenerator
     */
    public NameGenerator() {
    }

    public synchronized String generateName(String className) {
        Long n = (Long)get(className);
        if (n==null) {
            n = new Long(0);
        }
        // Just appending the counter to the classname is dangerous
        // because if you have 2 classes C1 and C11, you'll get name clashes
        String res = 
            className.substring(className.lastIndexOf('.')+1).toLowerCase() 
            + "#"+ n;
        put(className,new Long(n.longValue()+1));
        return res;
    }

    /**
     * Parses a name and returns its counter
     */
    public static long getCounterFromName(String name) {
        return Long.parseLong(name.substring(name.indexOf('#')+1));
    }

    /**
     * Gets the value of a counter
     */
    public long getCounter(String className) {
        Long counter = (Long)get(className);
        return counter!=null?counter.longValue():-1;
    }

    /**
     * Sets a counter
     */
    public void setCounter(String className, long count) {
        put(className,new Long(count));
    }

    /**
     * 
     */
    public synchronized void update(Map counters) {
        Iterator i = counters.entrySet().iterator();
        while(i.hasNext()) {
            Map.Entry entry = (Map.Entry)i.next();
            String name = (String)entry.getKey();
            Long value = (Long)entry.getValue();
            long current = getCounter(name);
            if (current<value.longValue())
                put(name,value);
        }
    }
}

