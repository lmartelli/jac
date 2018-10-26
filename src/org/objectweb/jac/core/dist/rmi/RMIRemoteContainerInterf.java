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

import org.objectweb.jac.core.dist.RemoteRef;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * RMIRemoteContainerInterf is the interface of RMI remote containers
 * that are used by jac to remotely instantiate objects.
 *
 * @author <a href="http://www-src.lip6.fr/homepages/Lionel.Seinturier/index-eng.html">Lionel Seinturier</a>
 */
 
public interface RMIRemoteContainerInterf extends Remote {
   
   /**
    * This method instantiates a className object.
    * Clients call it to remotely instantiate an object.
    * instantiates creates an object and returns its index.
    *
    * @param className  the class name to instantiate
    * @param args       initialization arguments for the instantiation
    * @param fields     the object fields that are part of the state
    * @param state      the state to copy
    * @param collaboration the collaboration of the client
    * @return           the index of the className object
    */
   
   int instantiates(String name, String className, Object[] args,
                    String[] fields, byte[] state, 
                    byte[] collaboration )
      throws RemoteException;
   
   
   /**
    * Copy a state into a base object.
    *
    * @param index   the callee index (see org.objectweb.jac.core.JacObject)
    * @param fields  the object fields that are part of the state
    * @param state   the state to copy
    * @param collaboration the collaboration of the client
    */
    
   void copy( String name, int index, String[] fields, byte[] state, 
              byte[] collaboration )
      throws RemoteException;


   /**
    * Invoke a method on a base object.
    * The base object is the remote counterpart of a local object
    * that has been remotely instantiated by a org.objectweb.jac.dist.Distd daemon.
    *
    * @param index       the callee index (see org.objectweb.jac.core.JacObject)
    * @param methodName  the callee method name
    * @param methodArgs  the callee method arguments
    */
   
   byte[] invoke( int index, String methodName, byte[] methodArgs, 
                  byte[] collaboration )
      throws RemoteException;

   byte[] invokeRoleMethod( int index, String methodName, 
                            byte[] methodArgs, 
                            byte[] collaboration )
      throws RemoteException;

   byte[] getByteCodeFor ( String className ) throws RemoteException;

   /**
    * Returns a remote reference on the object corresponding to the
    * given name. */

   RemoteRef bindTo ( String name ) throws RemoteException;

//    /**
//     * Get a client stub wrapping chain for a given object.
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
//    Vector getClientStubWrappingChain( int index ) throws RemoteException;

}
