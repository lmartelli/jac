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

  You should have received a copy of the GNU Lesser General Public
  License along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  USA */

package org.objectweb.jac.ide;

import org.objectweb.jac.util.Strings;

public class Constructor extends Method {
   public String getName() {
      
      return parent!=null ? parent.getName() : "<orphan_constructor>";
   }

   public String getPrototype() {
      String prototype = getGenerationName()+"("+getParametersString()+")";
      if (!exceptions.isEmpty()) {
         prototype += " throws "+Strings.join(exceptions,",");
      }
      return prototype;
   }

}
