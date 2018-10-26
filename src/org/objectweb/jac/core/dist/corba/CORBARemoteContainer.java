/*
  Renaud Pawlak, pawlak@cnam.fr, CEDRIC Laboratory, Paris, France.
  Lionel Seinturier, Lionel.Seinturier@lip6.fr, LIP6, Paris, France.

  JAC-Core is free software. You can redistribute it and/or modify it
  under the terms of the GNU Library General Public License as
  published by the Free Software Foundation.
  
  JAC-Core is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

  This work uses the Javassist system - Copyright (c) 1999-2000
  Shigeru Chiba, University of Tsukuba, Japan.  All Rights Reserved.  */

package org.objectweb.jac.core.dist.corba;

import org.objectweb.jac.core.utils.Lib;
import org.objectweb.jac.core.dist.RemoteContainer;

import java.util.Vector;


/**
 * CORBARemoteContainer is a container for remote objects
 * that can be accessed with CORBA.
 *
 * CORBARemoteContainer instances are created by CORBADistd.
 *
 * @author <a href="http://cedric.cnam.fr/~pawlak/index-english.html">Renaud Pawlak</a>
 * @author <a href="http://www-src.lip6.fr/homepages/Lionel.Seinturier/index-eng.html">Lionel Seinturier</a>
 */
 
public class CORBARemoteContainer
   extends RemoteContainer implements CORBARemoteContainerInterfOperations {

   /**
    * Create a new container.
    *
    * @param verbose  true if information messages are to be printed.
    */
   
   public CORBARemoteContainer( boolean verbose ) {
      super(verbose);
   }
   
   
   /**
    * Create a new container.
    *
    * @param className  the name of a class to instantiate
    * @param verbose    true if information messages are to be printed.
    */
   
   public CORBARemoteContainer( String className, boolean verbose ) {   
      super(className,verbose);
   }
   
   
   /**
    * This method instantiates a className object.
    * Clients call it to remotely instantiate an object.
    * instantiates creates an object and returns its index.
    * This method is part of the CORBARemoteContainerInterf interface.
    *
    * @param className  the class name to instantiate
    * @param args       initialization arguments for the instantiation
    * @param classes    remote classes to load
    * @param fields     the object fields that are part of the state
    * @param state      the state to copy
    * @return           the index of the className object
    */
   
   public int
      instantiates(
         String className, byte[][] args, byte[] classes,
	 String[] fields, byte[][] state
      ) {

      /**
       * Transmitted empty arrays are transformed to null arrays.
       * The issue may need to be investigated if we really want
       * to transmit empty arrays.
       * The problem stems from the fact that CORBA stubs do not deal with
       * null arguments in a friendly way.
       */
      
      Object[] argsObjects = null;
      if ( args.length != 0 ) {
         argsObjects = new Object[ args.length ];      
         for ( int i=0 ; i < args.length ; i++ )
            argsObjects[i] = Lib.deserialize( args[i] );
      }
      
      if ( fields.length == 0 )  fields = null;
      
      Object[] stateObjects = null;
      if ( state.length != 0 ) {
         stateObjects = new Object[ state.length ];      
         for ( int i=0 ; i < state.length ; i++ )
            stateObjects[i] = Lib.deserialize( state[i] );
      }
      
      return
         super.instantiates(
	    className, args, (Vector) Lib.deserialize(classes),
	    fields, stateObjects
         );
   }


   /**
    * Copy a state into a base object.
    *
    * @param index   the base object index (see org.objectweb.jac.core.JacObject)
    * @param fields  the object fields that are part of the state
    * @param state   the state to copy
    */
    
   public void copy( int index, String[] fields, byte[][] state ) {

      Object[] stateObjects = new Object[ state.length ];      
      for ( int i=0 ; i < state.length ; i++ )
         stateObjects[i] = Lib.deserialize( state[i] );
      
      super.copy( index, fields, stateObjects );
   }
   
   
   /**
    * Invoke a method on a base object.
    * The base object is the remote counterpart of a local object
    * that has been remotely instantiated by a remote container.
    * This method is part of the CORBARemoteContainerInterf interface.
    *
    * @param index       the callee index (see org.objectweb.jac.core.JacObject)
    * @param methodName  the callee method name
    * @param methodArgs  the callee method arguments
    * @return            the result
    */
   
   public byte[] invoke( int index, String methodName, byte[][] methodArgs ) {
      
      Object[] methodArgsObjects = new Object[ methodArgs.length ];     
      for ( int i=0 ; i < methodArgs.length ; i++ )
         methodArgsObjects[i] = Lib.deserialize( methodArgs[i] );
      
      Object result = super.invoke( index, methodName, methodArgsObjects );
      
      return Lib.serialize(result);
   }


   /**
    * Get a client stub wrapping chain for a given object.
    * This method is part of the CORBADistdInterf interface.
    *
    * This method is called whenever a daemon receives as a parameter
    * a reference to a remote object, to get the wrapping chain
    * (for instance an authentication wrapper, a verbose wrapper, ...)
    * needed to create a client stub for this remote reference.
    *
    * CORBARemoteContainer.getClientStubWrappingChain2 has a different
    * return type than RemoteContainer.getClientStubWrappingChain
    * (byte[] instead of Vector).
    * Nevertheless, the latter is supposed to be the super method of the former.
    * But because polymorphism on return type is not handled in Java,
    * The method is called getClientStubWrappingChain2 instead of
    * getClientStubWrappingChain.
    *
    * @param index  the base object index (see org.objectweb.jac.core.JacObject)
    * @return       the client stub wrapping chain as a serialized object
    */
   
   public byte[] getClientStubWrappingChain2( int index ) {
   
      return Lib.serialize( super.getClientStubWrappingChain(index) );
   }

}
