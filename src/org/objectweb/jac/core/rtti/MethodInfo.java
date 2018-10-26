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
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA */

package org.objectweb.jac.core.rtti;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;


public class MethodInfo implements Serializable {
   public Set accessedFields = new HashSet();
   public String returnedField;
   public boolean isGetter = true;
   public Set modifiedFields = new HashSet();
   public Set setFields = new HashSet();
   public Set addedCollections = new HashSet();
   public Set removedCollections = new HashSet();
   public Set modifiedCollections = new HashSet();
   public int collectionIndexArgument = -1;
   public int collectionItemArgument = -1;
   public Set invokedMethods = new HashSet();
   public boolean callSuper = false;
}
