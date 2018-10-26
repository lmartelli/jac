/*
  Copyright (C) 2003 <laurent@aopsys.com>

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

package org.objectweb.jac.aspects.idGen;

import java.util.Hashtable;
import java.util.Map;

/**
 * Stores counters
 */

public class Counters {

   public Counters() {
   }

   /**
    *  Generate an ID 
    *  @param name counter name to use to generate the ID
    */
    public synchronized long genId(String name) {
       Long n = (Long)counters.get(name);
       if (n==null) {
          n = new Long(1);
       }
       long res = n.longValue();
       counters.put(name,new Long(res+1));
       return res;
    }

    Map counters = new Hashtable();
}
