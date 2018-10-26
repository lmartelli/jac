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

import org.objectweb.jac.core.AspectComponent;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.MethodItem;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.objectweb.jac.util.Log;


public class MessageQueueAC extends AspectComponent implements MessageQueueConf  {

   MessageQueue mqueue = new MessageQueue();

   public void registerField(ClassItem cli, String fieldName, MethodItem callback) {
      mqueue.registerFieldChange(cli.getField(fieldName),callback);
   }

   public void whenConfigured() {
      Log.trace("mqueue",this+".whenConfigured");
      super.whenConfigured();
      Set treatedFields = new HashSet();
      Map fieldClients = mqueue.getFieldClients();
      Iterator it = fieldClients.keySet().iterator();
      while (it.hasNext()) {
         FieldItem field = (FieldItem)it.next();
         if (!treatedFields.contains(field)) {
            Log.trace("mqueue","installing pointcut for "+field.getLongName());
            pointcut("ALL",field.getClassItem().getName(),"WRITERS("+field.getName()+")",
                     MessageQueueWrapper.class.getName(),null,SHARED);
            treatedFields.add(field);
         }
      }
   }

   public MessageQueue getMessageQueue() {
      return mqueue;
   }
}
