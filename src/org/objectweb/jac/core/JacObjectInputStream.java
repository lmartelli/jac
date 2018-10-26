/*
  Copyright (C) 2001-2002 Renaud Pawlak, Lionel Seinturier.

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

package org.objectweb.jac.core;

import java.io.InputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * <code>JacObjectInputStream</code> is used to read JAC objects from
 * an input stream during a deserialization process.
 *
 * <p>This stream is used when deserializing an array of bytes with
 * <code>JacObject.deserialize()</code>. All the objects that are not
 * serialized JAC objects are deserialized with the default
 * procedure. When a serialized JAC object is encountered, a
 * <code>whenDeserialized</code> event is thrown on the current AC
 * manager so that the aspect components can parametrize the
 * deserialization process.
 *
 * <p>A symetric process for serialization is implemented by
 * <code>JacObjectOutputStream</code>.
 *
 * @see ACManager#whenDeserialized(SerializedJacObject,Wrappee)
 * @see JacObjectOutputStream
 *
 * @author Renaud Pawlak
 * @author Lionel Seinturier
 */
 
public class JacObjectInputStream extends ObjectInputStream {

   /**
    * Creates a JacObjectInputStream. 
    *
    * @param is the input stream from which the bytes are read. */

   public JacObjectInputStream(InputStream is) throws IOException {
      super(is);
      enableResolveObject(true);
   }
   
   
   /**
    * This method is upcalled by the Java deserialization process each
    * time a new object to deserialize is encountered.
    *
    * <p>If a serialized JAC object is encountered (instance of
    * <code>SerializedJacObject</code>), the aspect component manager
    * is upcalled to parametrize the deserialization.
    *
    * @param obj the encountered serialized JAC object 
    * @return the final deserialized JAC object
    *
    * @see SerializedJacObject
    * @see ACManager#whenDeserialized(SerializedJacObject,Wrappee)
    */
   
   protected Object resolveObject(Object obj) throws IOException {

      Object o = null;
      
      if (obj instanceof SerializedJacObject) {
         
         try {
            // WAS THIS USEFULL?????
            //JacObject.remoteInstantiation = true;
            o = Class.forName( ((SerializedJacObject)obj).getJacObjectClassName() )
               .newInstance();

            if (o instanceof AspectComponent) {
               return o;
            }

            // WAS THIS USEFULL?????
            // JacObject.remoteInstantiation = false;
         } catch (Exception e) { 
            e.printStackTrace(); 
         }
      
         return ((ACManager)ACManager.get()).whenDeserialized(
            (SerializedJacObject)obj,(Wrappee)o);
      
         //return Collaboration.get().getAttribute( "finalObject" );
      } else if(obj instanceof SerializedMethodItem) {
         return ((SerializedMethodItem)obj).getMethod();
      }
      return obj;
   }
   
   
}



