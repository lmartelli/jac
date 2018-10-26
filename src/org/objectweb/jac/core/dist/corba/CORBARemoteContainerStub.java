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

import java.io.Serializable;
import java.util.Vector;


/**
 * CORBARemoteContainerStub acts as a client stub to access a remote container.
 *
 * CORBARemoteContainerStub holds a CORBARemoteContainerInterf instance.
 * This is the client stub of the remote CORBARemoteContainer object
 * that owns as a delegate the container that is to be accessed.
 *
 * <p>
 * Note: what we need is an instance of something that extends RemoteContainer.
 * But we can't have an object that is both a client stub for a remote CORBA
 * object and a RemoteContainer (no multiple inheritance in Java).
 * So we implemented this delegating scheme where:
 * <ul>
 * <li> CORBARemoteContainerStub (which is a RemoteContainer) delegates
 *   its job to a CORBARemoteContainer client stub </li>
 * <li> which itself transmits it to a remote CORBARemoteContainer object, </li>
 * </ul>
 * </p>
 *
 * @see org.objectweb.jac.core.dist.corba.CORBARemoteContainer
 *
 * @author <a href="http://cedric.cnam.fr/~pawlak/index-english.html">Renaud Pawlak</a>
 * @author <a href="http://www-src.lip6.fr/homepages/Lionel.Seinturier/index-eng.html">Lionel Seinturier</a>
 */
 
public class CORBARemoteContainerStub
   extends RemoteContainer implements Serializable {

   /** The CORBA stub where the job is to be delegated. */
   
   protected CORBARemoteContainerInterf delegate;
   

   /**
    * Create a new remote container stub.
    *
    * @param delegate  the stub where the job is to be delegated
    */
   
   public CORBARemoteContainerStub( CORBARemoteContainerInterf delegate ) {
      this.delegate = delegate;
   }
   

   /**
    * This method instantiates a className object.
    * Clients call it to remotely instantiate an object.
    * instantiates creates an object and returns its index.
    * This method is part of the RMIDistdInterf interface.
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
         String className, Object[] args, Vector classes,
	 String[] fields, Object[] state
      ) {
      
      /**
       * null array arguments are transmitted empty arrays.
       * The issue may need to be investigated if we really want
       * to transmit empty arrays.
       * The problem stems from the fact that CORBA stubs do not deal with
       * null arguments in a friendly way.
       */
      
      byte[][] argsBytes = new byte[0][0];
      if ( args != null ) {
         argsBytes = new byte[args.length][];      
         for ( int i=0 ; i < args.length ; i++ )
            argsBytes[i] = Lib.serialize( args[i] );
      }
      
      if ( fields == null )  fields = new String[0];
      
      byte[][] stateBytes = new byte[0][0];
      if ( state != null ) {
         stateBytes = new byte[state.length][];      
         for ( int i=0 ; i < state.length ; i++ )
            stateBytes[i] = Lib.serialize( state[i] );
      }
      
      return
         delegate.instantiates(
	    className, argsBytes, Lib.serialize(classes),
	    fields, stateBytes
	 );
   }


   /**
    * Copy a state into a base object.
    *
    * @param index   the callee index (see org.objectweb.jac.core.JacObject)
    * @param fields  the object fields that are part of the state
    * @param state   the state to copy
    */
    
   public void copy( int index, String[] fields, Object[] state ) {
   
      byte[][] stateBytes = new byte[state.length][];      
      for ( int i=0 ; i < state.length ; i++ )
         stateBytes[i] = Lib.serialize( state[i] );

      delegate.copy( index, fields, stateBytes );
   }
   
   
   /**
    * Invoke a method on a base object.
    *
    * The base object is the remote counterpart of a local object
    * that has been remotely instantiated by the org.objectweb.jac.dist.Distd daemon.
    *
    * @param index       the callee index (see org.objectweb.jac.core.JacObject)
    * @param methodName  the callee method name
    * @param methodArgs  the callee method arguments
    * @return            the result
    */
   
   public Object invoke( int index, String methodName, Object[] methodArgs ) {
      
      byte[][] methodArgsBytes = new byte[methodArgs.length][];      
      for ( int i=0 ; i < methodArgs.length ; i++ )
         methodArgsBytes[i] = Lib.serialize( methodArgs[i] );

      return
         Lib.deserialize( delegate.invoke(index,methodName,methodArgsBytes) );
   }


   /**
    * Get a client stub wrapping chain for a given object.
    *
    * This method is called whenever a daemon receives as a parameter
    * a reference to a remote object, to get the wrapping chain
    * (for instance an authentication wrapper, a verbose wrapper, ...)
    * needed to create a client stub for this remote reference.
    *
    * @param index  the base object index (see org.objectweb.jac.core.JacObject)
    * @return       the client stub wrapping chain as a serialized object
    */
   
   public Vector getClientStubWrappingChain( int index ) {
   
      return
         (Vector) Lib.deserialize(delegate.getClientStubWrappingChain2(index));
   }

}
