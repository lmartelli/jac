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
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA */

package org.objectweb.jac.aspects.queue;

import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.MethodItem;



public interface MessageQueueConf {

   /**
    * Register a call to be called when a field is changed.
    * @param cli the class of the field
    * @param fieldName the name of the field
    * @param callback the static method to call when the field is changed
    */
   void registerField(ClassItem cli, String fieldName, MethodItem callback);


}
