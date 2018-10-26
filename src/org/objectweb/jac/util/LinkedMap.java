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

import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;


/**
 * Use only put(Object,Object) and get(Object). 
 */
public class LinkedMap extends HashMap {
    static Logger logger = Logger.getLogger("util.map");

    Map next;
    public LinkedMap() {
    }
    public LinkedMap(Map next) {
        this.next = next;
    }
    public Object get(Object key) {
        if (super.containsKey(key)) {
            return super.get(key);
        } else {
            Object result = null;
            if (next!=null) {
                result = next.get(key);
                logger.debug(key+" not found, trying next -> "+result);
                put(key,result);
            }
            return result;
        }
    }
    public boolean containsKey(Object key) {
        if (super.containsKey(key)) {
            return true;
        } else {
            return next!=null && next.containsKey(key);
        }
    }
}
