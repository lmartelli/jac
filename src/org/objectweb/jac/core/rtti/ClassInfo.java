/*
  Copyright (C) 2001-2003 Laurent Martelli <laurent@aopsys.com>
  
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
import java.util.Hashtable;
import java.util.Map;

public class ClassInfo implements Serializable {
   Map methodInfos = new Hashtable();
   public MethodInfo getMethodInfo(String method) {
      MethodInfo methodinfo = (MethodInfo)methodInfos.get(method);
      if (methodinfo==null) {
         methodinfo = new MethodInfo();
         methodInfos.put(method,methodinfo);
      }
      return methodinfo;
   }
   public void addModifiedField(String method, String field) {
      getMethodInfo(method).modifiedFields.add(field);
   }
   public void addAccessedField(String method, String field) {
      getMethodInfo(method).accessedFields.add(field);
   }
   public void setReturnedField(String method, String field) {
      getMethodInfo(method).returnedField = field;
   }
   public void setIsGetter(String method, boolean isGetter) {
      getMethodInfo(method).isGetter = isGetter;
   }
   public void addSetField(String method, String field) {
      getMethodInfo(method).setFields.add(field);
   }
   public void addAddedCollection(String method, String field) {
      getMethodInfo(method).addedCollections.add(field);
   }
   public void addRemovedCollection(String method, String field) {
      getMethodInfo(method).removedCollections.add(field);
   }
   public void addModifiedCollection(String method, String field) {
      getMethodInfo(method).modifiedCollections.add(field);
   }
   public void setCollectionIndexArgument(String method, int argument) {
      getMethodInfo(method).collectionIndexArgument = argument;
   }
   public void setCollectionItemArgument(String method, int argument) {
      getMethodInfo(method).collectionItemArgument = argument;
   }
   public void addInvokedMethod(String method,InvokeInfo invokeInfo) {
      getMethodInfo(method).invokedMethods.add(invokeInfo);
   }
}
