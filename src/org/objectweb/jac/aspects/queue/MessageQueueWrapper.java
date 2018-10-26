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

import org.aopalliance.intercept.ConstructorInvocation;
import org.aopalliance.intercept.MethodInvocation;
import org.objectweb.jac.core.AspectComponent;
import org.objectweb.jac.core.Interaction;
import org.objectweb.jac.core.Wrapper;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.MethodItem;
import org.objectweb.jac.util.Log;

public class MessageQueueWrapper extends Wrapper  {
   MessageQueue mqueue;
   public MessageQueueWrapper(AspectComponent ac) {
      super(ac);
      mqueue = ((MessageQueueAC)ac).getMessageQueue();
   }
   
   public Object fieldChange(Interaction interaction) {
      Log.trace("mqueue","fieldChange: "+interaction);
      FieldItem[] fields = null;
      // values of the fields before change
      Object[] previousValues = null;

      if (interaction.method instanceof MethodItem) {
         fields = ((MethodItem)interaction.method).getWrittenFields();
         previousValues = new Object[fields.length];
         for (int i=0; i<fields.length; i++) {
            previousValues[i] = fields[i].get(interaction.wrappee);
         }
      }

      Object result = proceed(interaction);

      if (fields!=null) {
         for (int i=0; i<fields.length; i++) {
            Object newValue = fields[i].get(interaction.wrappee);
            if ( (newValue!=null && 
                  !newValue.equals(previousValues[i])) ||
                  (newValue==null && previousValues[i]!=null) ) 
            {
               mqueue.fieldChanged(interaction.wrappee,fields[i],
                                   previousValues[i], newValue);
            }
         }
      }
      return result;
   }

   public Object invoke(MethodInvocation invocation) throws Throwable {
	   return fieldChange((Interaction) invocation);
   }

   public Object construct(ConstructorInvocation invocation)
	   throws Throwable {
	   throw new Exception("Wrapper "+this+" does not support construction interception.");
   }
}
