/*
  Copyright (C) 2001-2002 Renaud Pawlak. <renaud@aopsys.com>

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  USA */

package org.objectweb.jac.core;

import java.util.*;

import org.objectweb.jac.core.rtti.*;

/**
 * This interface specify the prototypes of the methods that can be
 * notified when an event occurs within the base program.
 * 
 * <p>This can be regarded as the JAC Meta-Object Protocol (MOP).
 *
 * @author Renaud Pawlak
 */

public interface BaseProgramListener {

   /**
    * This method is upcalled by JAC when a given instance is used for
    * the first time at construction-time.
    * 
    * <p>Informations about the called method can be retrieved by
    * using the <code>CollaborationParticipant</code> methods.<br>
    *
    * @param interaction the interaction that triggered the
    * whenUsingNewInstance event (usually a constructor invocation)
    *
    * @see CollaborationParticipant 
    * @see #whenUsingNewClass(ClassItem) */
   void whenUsingNewInstance(Interaction interaction);

   /**
    * This method is upcalled by JAC when a static method is called
    * for the first time.
    *
    * @param cl the class that is used for the first time.
    *
    * @see CollaborationParticipant 
    * @see #whenUsingNewInstance(Interaction) */
   void whenUsingNewClass(ClassItem cl);

   /**
    * This method is upcalled by JAC when a new object is instantiated
    * from a remote site.
    *
    * <p>The name that is passed is the name of the remote
    * reference that has been used to create the object.
    * 
    * @param newInstance the instance that have been created by a
    * remote host
    * @param name the name of the new instance 
    */
   void whenRemoteInstantiation(Wrappee newInstance, String name);

   /**
    * This method is upcalled by JAC when a JAC object is cloned.
    * 
    * @param cloned the object that is being cloned
    * @param clone the new object (the clone of cloned) 
    */
   void whenClone(Wrappee cloned, Wrappee clone);


   /**
    * This method is called when a JAC object is serialized and can
    * parametrize the serialization by filling the <code>finalObject</code>
    * parameter.
    *
    * <p><b>IMPORTANT</b>: this method is upcalled only if the
    * serialization is done with a
    * <code>JacObjectOutputStream</code>. To ensure the correct use of
    * this class, only use <code>JacObject.serialize()</code> to
    * serialize an object.</p>
    *
    * @param orgObject the object being serialized
    * @param finalObject the corresponding serialized structure
    * @return the object being serialized (usually orgObject, but not
    * necessarily).
    * @see SerializedJacObject
    * @see JacObjectOutputStream 
    */
   Wrappee whenSerialized(Wrappee orgObject,SerializedJacObject finalObject);


   /**
    * This method is called when a base object is deserialized and can
    * parametrize the deserialization by reading the
    * SerializedJacObject instance to get some extra infos on the
    * aspects.
    *
    * <p><b>IMPORTANT</b>: this method is upcalled only if the
    * deserialization is done with a
    * <code>JacObjectInputStream</code>. To ensure the correct use of
    * this class, only use <code>JacObject.deserialize()</code> to
    * deserialize an object.
    *
    * @param orgObject the corresponding serialized structure
    * @param finalObject the object being deserialized
    * @return the object being deserialized (usually finalObject but
    * not necessarily)
    * @see SerializedJacObject
    * @see JacObjectInputStream 
    */
   Wrappee whenDeserialized(SerializedJacObject orgObject, Wrappee finalObject);

   /**
    * This method is upcalled by JAC when a wrapper is going to be
    * applied to a wrappee.
    *
    * @param wrapper the wrapper that is going to be runned
    * @param wrappingMethod the name of the may-be runned wrapping
    * method
    * @return a boolean that tells if the wrapper has to be runned
    * (true) or not (false)
    * @see Wrappee
    * @see Wrapping#wrap(Wrappee,Wrapper,AbstractMethodItem)
    * @see Wrapper
    * @see Wrapper#proceed(Invocation) 
    */
   boolean beforeRunningWrapper(Wrapper wrapper, String wrappingMethod);

   /**
    * This method is upcalled by JAC after the application of the
    * wrapper.
    * 
    * @param wrapper the wrapper that has just been runned
    * @param wrappingMethod the name of the runned wrapping method
    * @see Wrappee
    * @see Wrapping#wrap(Wrappee,Wrapper,AbstractMethodItem)
    * @see Wrapper
    * @see Wrapper#proceed(Invocation) 
    */
   void afterRunningWrapper(Wrapper wrapper, 
                            String wrappingMethod);

   /**
    * This method is upcalled after the wrappee is wrapped by the
    * wrapper.
    *
    * @see Wrapper
    * @see Wrappee
    * @see Wrapping#wrap(Wrappee,Wrapper,AbstractMethodItem) 
    */
   void afterWrap(Wrappee wrappee, Wrapper wrapper,
                  String[] wrapping_methods,
                  String[][] wrapped_methods);

   /**
    * Calls whenGetObjects on all aspects.
    * @param objects list of objects already in memory
    * @param cl return only instances of this class
    */
   void whenGetObjects(Collection objects, ClassItem cl);

   /**
    * Upcalled when the naming aspect names an object.
    *
    * @param object the object to name
    * @param name current name of the object or null
    * @return the proposed name for the object
    */
   String whenNameObject(Object object, String name);

   String FOUND_OBJECT = "FOUND_OBJECT";

   /**
    * Upcalled when an object is been seeked within the name
    * repository and is not found.
    *
    * <p>The finally found object is a contextual attribute called
    * FOUND_OBJECT.
    *
    * @param name the name that has not been found 
    * @see #FOUND_OBJECT
    */
   void whenObjectMiss(String name);

   /**
    * Upcalled when an object is deleted.
    * @param object the deleted object
    */
   void whenDeleted(Wrappee object);

   /**
    * Upcalled when an object is freed from the memory.
    * @param object the deleted object
    */
   void whenFree(Wrappee object);

   /**
    * Upcalled after a new application is started on the JAC system.  
    */
   void afterApplicationStarted(); 

   /**
    * Upcalled when a display is closing.
    *
    * @param display the closing display 
    */
   void whenCloseDisplay(Display display);

   /**
    * This method is upcalled when a topological change occurs whithin
    * the distributed application. */
   void whenTopologyChanged();

   /**
    * This method is upcalled when the system exits.
    */
   void onExit();

    /**
     * This method should be called by the GUI system before an object
     * is being created and initialized (but the process is not
     * finished yet).
     *
     * <p>Then, some aspects should deal differently with this object
     * thant with already created objects (for instance, the GUI
     * aspect should not show the object to other users, or a remote
     * access aspect should disable forwarding. */ 
    
    void beforeWrappeeInit(Wrappee wrappee);

    /**
     * This method should be called by the GUI system after an object
     * has been created and initialized (tells the other aspects that
     * the object is now regular). */    

    void afterWrappeeInit(Wrappee wrappee);

}
