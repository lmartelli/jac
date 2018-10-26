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


/**
 * RMIRemoteRef stores the reference of a remote object
 * that can be accessed by the RMI protocol.
 *
 * @author <a href="http://www-src.lip6.fr/homepages/Lionel.Seinturier/index-eng.html">Lionel Seinturier</a>
 */
 
public class RMIRemoteRef extends RemoteRef {

   /**
    * Default constructor. */

   public RMIRemoteRef() {}

   /**
    * This is a full constructor for RemoteRef.
    *
    * @param remCont   the ref of the container that handles the remote object.
    * @param remIndex  the index of the remote object
    */
    
   public RMIRemoteRef(RemoteContainer remCont, int remIndex) {
      super(remCont, remIndex);
   }
   
   
   /**
    * This is a more friendly constructor for RemoteRef.
    *
    * @param remCont   the name of the container that handles the remote object.
    * @param remIndex  the index of the remote object.
    */   
   public RMIRemoteRef(String remCont, int remIndex) {
      super(remCont, remIndex);
   }

   /**
    * This method resolves a container from a container name.
    * This method simply delegates its job to a RMIRemoteContainer.
    *
    * @param contName  the name of the container
    * @return          the container reference
    */
   
   public RemoteContainer resolve( String contName ) {
      return RMINaming.resolve(contName);  
   }

   /**
    * This method re-gets the reference of a remote container.
    * CORBA do not linearalize remote references in a standard way.
    * Thus a remote reference may need to be adapted whenever it is transmitted.
    *
    * This method performs nothing in the case of RMI.
    * This method is called when a remote reference
    * is received by a RemoteContainer.
    *
    * @return  the container reference
    */
   
   public RemoteContainer reresolve() { return null; }
   
}
