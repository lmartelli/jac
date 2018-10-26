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

package org.objectweb.jac.util;

import java.util.HashMap;


/**
 * Implements a map whose data may be shared between several
 * instances. It has the benefit of having a fast clone() method which
 * only duplicates the data when needed.
 */
public class SharedMap implements Cloneable {
   HashMap map;
   boolean shared=false;
   public SharedMap() {
      map = new HashMap();
   }
   public SharedMap(HashMap map) {
      this.map = map;
   }
   SharedMap(HashMap map, boolean shared) {
      this.map = map;
      this.shared = shared;
   }
   public Object clone() {
      shared = true;
      return new SharedMap(map,true);
   }
   public Object get(Object key) {
      return map.get(key);
   }
   public synchronized void put(Object key, Object value) {
      if (shared) {
         //System.out.println("cloning shared map");
         map = (HashMap)map.clone();
         shared = false;
      }
      map.put(key,value);
   }
}
