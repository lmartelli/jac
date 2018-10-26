/*
  Copyright (C) 2001 Lionel Seinturier

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 2 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser Generaly Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA */

package org.objectweb.jac.core.dist.rmi;

import org.objectweb.jac.core.dist.RemoteContainer;
import org.objectweb.jac.core.dist.RemoteRef;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * RMIRemoteContainer is a container for remote objects that can be accessed
 * with the RMI communication protocol.
 *
 * RMIRemoteContainer delegates most of his job to a RemoteContainer.
 * RMIRemoteContainer instances are created by RMIDistd.
 *
 * @author <a href="http://www-src.lip6.fr/homepages/Lionel.Seinturier/index-eng.html">Lionel Seinturier</a>
 */
 
public class RMIRemoteContainer
   extends UnicastRemoteObject implements RMIRemoteContainerInterf {

   /** The remote container to which most of the job is delegated. */
   protected RemoteContainer delegate;
   
   
   /** Create a new container. */
   
   public RMIRemoteContainer() throws RemoteException { 
      super(); 
   }
   

   /**
    * Create a new container.
    *
    * @param verbose  true if information messages are to be printed.
    */
   
   public RMIRemoteContainer(boolean verbose) throws RemoteException {
      this();
      delegate = new RemoteContainer(verbose);
   }
   
   
   /**
    * Create a new container.
    *
    * @param className  the name of a class to instantiate
    * @param verbose    true if information messages are to be printed.
    */
   
   public RMIRemoteContainer(String className, boolean verbose)
      throws RemoteException 
   {
      this();
      delegate = new RemoteContainer(className,verbose);
   }
   
   
   /**
    * Getter method for the delegate field.
    *
    * @return  the delegate field value
    */
   
   public RemoteContainer getDelegate() { 
      return delegate; 
   }
   
   
   /**
    * This method instantiates a className object.
    * Clients call it to remotely instantiate an object.
    * instantiates creates an object and returns its index.
    * This method is part of the RMIRemoteContainerInterf interface.
    *
    * @param className     the class name to instantiate
    * @param args          initialization arguments for the instantiation
    * @param fields        the object fields that are part of the state
    * @param state         the state to copy
    * @param collaboration the collaboration of the client
    * @return              the index of the className object
    */
   
   public int instantiates(String name, String className, Object[] args,
                           String[] fields, byte[] state,
                           byte[] collaboration)
      throws RemoteException 
   {
      return delegate.instantiates(name, 
                                   className, 
                                   args, 
                                   fields, 
                                   state, 
                                   collaboration);
   }


   /**
    * Copy a state into a base object.
    * This method is part of the RMIRemoteContainerInterf interface.
    *
    * @param index         the base object index (see org.objectweb.jac.core.JacObject)
    * @param fields        the object fields that are part of the state
    * @param state         the state to copy
    * @param collaboration the collaboration of the client
    */
    
   public void copy(String name, int index, String[] fields, byte[] state,
                    byte[] collaboration)
      throws RemoteException {
      delegate.copy(name, index, fields, state, collaboration);
   }
   
   /**
    * Invoke a method on a base object.
    * The base object is the remote counterpart of a local object
    * that has been remotely instantiated by a remote container.
    * This method is part of the RMIRemoteContainerInterf interface.
    *
    * @param index       the callee index (see org.objectweb.jac.core.JacObject)
    * @param methodName  the callee method name
    * @param methodArgs  the callee method arguments
    * @return            the result
    */
   
   public byte[] invoke(int index, String methodName, 
                        byte[] methodArgs, byte[] collaboration)
      throws RemoteException {

      return delegate.invoke(index, methodName, methodArgs, collaboration);
   }

   public byte[] invokeRoleMethod(int index, String methodName, 
                                  byte[] methodArgs, 
                                  byte[] collaboration)
      throws RemoteException {

      return delegate.invokeRoleMethod(
         index, methodName, methodArgs, collaboration );
   }

   public byte[] getByteCodeFor(String className) throws RemoteException {
      return delegate.getByteCodeFor(className);
   }


   /**
    * Returns a remote reference on the object corresponding to the
    * given name. */

   public RemoteRef bindTo (String name) throws RemoteException {
      return delegate.bindTo(name);
   }


//    /**
//     * Get a client stub wrapping chain for a given object.
//     * This method is part of the RMIRemoteContainerInterf interface.
//     *
//     * This method is called whenever a daemon receives as a parameter
//     * a reference to a remote object, to get the wrapping chain
//     * (for instance an authentication wrapper, a verbose wrapper, ...)
//     * needed to create a client stub for this remote reference.
//     *
//     * @param index  the base object index (see org.objectweb.jac.core.JacObject)
//     * @return       the client stub wrapping chain as a serialized object
//     */
//    
//    public Vector getClientStubWrappingChain( int index )
//       throws RemoteException {
//    
//       return delegate.getClientStubWrappingChain(index);
//    }

}
