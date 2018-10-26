/*
  Copyright (C) 2001-2002 Laurent Martelli.

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

package org.objectweb.jac.aspects.gui;

import java.util.Map;
import gnu.regexp.*;

/**
 * This class is a mere wrapper which delegates all methods to a real
 * Map. It allows you to have wrappable maps.
 */

public class WrappableMap
{
   private Map delegate;

   protected Map getDelegate() {
      return delegate;
   }

   public WrappableMap(Map delegate) {
      this.delegate = delegate;
   }
   
   /**
    * Calls put on delegate for every key already in delegate which
    * matches key. For instance, call with <code>put("gui.*",0)</code>
    * will turn off all gui traces.
    * @param key regexp key
    * @param value trace level
    */

   public void put ( String key, Integer value )  
      throws REException
   {
      Object keys[] = delegate.keySet().toArray();
      RE re = new RE(key);
      for (int i=0; i<keys.length;i++) {
         String cur_key = (String)keys[i];
         if (re.isMatch(cur_key))
            delegate.put(cur_key, value);
      }
   }
   
   public void clear (  )  {
      delegate.clear();
   }
   
   public Object remove ( String key )  {
      return delegate.remove(key);
   }
 
   static public String[] getCategories(WrappableMap wmap ) {
      java.util.Set keys = wmap.getDelegate().keySet();
      String[] result = new String[keys.size()];
      java.util.Iterator i = keys.iterator();
      int j=0;
      while (i.hasNext()) {
         result[j++] = (String)i.next();
      }
      return result;
   }

   static public Integer[] getLevels(WrappableMap wmap) {
      return new Integer[] {new Integer(0),new Integer(1),
                            new Integer(2),new Integer(3)};
   }
}
