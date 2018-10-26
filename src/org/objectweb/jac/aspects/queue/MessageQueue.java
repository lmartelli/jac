/*
  Copyright (C) 2003 Laurent Martelli <laurent@aopsys.com>
  
  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 2 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  USA */

package org.objectweb.jac.aspects.queue;



import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.MethodItem;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.objectweb.jac.util.Log;


/**
 * Handles an asynchronous  message queue. 
 */
public class MessageQueue {

   NotifyThread thread;
   public MessageQueue() {
      thread = new NotifyThread();
      thread.start();
   }

   // FieldItem -> Set of MethodItem
   HashMap fieldClients = new HashMap();

   /**
    * Gets the map of field change client callbacks.
    * @return a Map whose keys are of type FieldItem, and whose values
    * are sets of MethodItem
    */
   public Map getFieldClients() {
      return fieldClients;
   }

   List queue = Collections.synchronizedList(new LinkedList());

   /**
    * Send a message to the queue saying that the field of an object
    * has changed, so that it will be sent to all registered clients.
    * @param substance the object whose field has changed
    * @param field the field that changed
    */
   public void fieldChanged(Object substance, FieldItem field, 
                            Object previousValue, Object currentValue) {
      Log.trace("mqueue","fieldChanged("+substance+","+field+","+
                previousValue+" -> "+currentValue);
      queue.add(new FieldChangeEvent(substance,field,previousValue,currentValue));
      thread.notifyClients();
   }

   /**
    * Register for the changes of a field.
    * @param field the field to register for
    * @param callback a static method to be called when the field changes. 
    * @see #unregisterFieldChange(FieldItem,MethodItem)
    */
   public void registerFieldChange(FieldItem field, MethodItem callback) {
      Set clients = (Set)fieldClients.get(field);
      if (clients==null) {
         clients = new HashSet();
         fieldClients.put(field,clients);
      }
      clients.add(callback);
   }

   /**
    * Notify registered clients of a field change.
    * @param event the FieldChangeEvent to dispatch
    */
   public void notifyFieldChange(FieldChangeEvent event) {
      Set clients = (Set)fieldClients.get(event.getField());
      if (clients!=null) {
         MethodItem[] array = (MethodItem[])clients.toArray(new MethodItem[] {});
         Object[] params = new Object[] {event};
         for(int i=0; i<array.length; i++) {
            try {
               Log.warning("mqueue","notifying "+event+" to "+array[i]);
               array[i].invokeStatic(params);
            } catch (Exception e) {
               Log.warning("mqueue","Failed to invoke "+array[i]+": "+e);
            }
         }
      }
   }

   /**
    * Unregister for the changes of a field.
    * @param field the field to unregister from
    * @param callback a static method to to call anymore when the
    * field changes.
    * @see #registerFieldChange(FieldItem,MethodItem) */
   public void unregisterFieldChange(FieldItem field, MethodItem callback) {
      Set clients = (Set)fieldClients.get(field);
      if (clients!=null) {
         clients.remove(callback);
      }
   }

   class NotifyThread extends Thread {
      public void run() {
         while (true) {
            try {
               synchronized(this) {
                  this.wait();
               }
            } catch (InterruptedException e) {
            }
            Log.trace("mqueue","Queue = "+queue);
            if (!queue.isEmpty()) {
               Object events[] = queue.toArray();
               for (int i=0; i<events.length; i++) {
                  if (events[i] instanceof FieldChangeEvent) {
                     FieldChangeEvent event = (FieldChangeEvent)events[i];
                     notifyFieldChange(event);
                     queue.remove(event);
                  }
               }
            }
         }
      }

      /**
       * Call this method when a new event was added to the queue.
       */
      public synchronized void notifyClients() {
         this.notify();
      }
   }
}
