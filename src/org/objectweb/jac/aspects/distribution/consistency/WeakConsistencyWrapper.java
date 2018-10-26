/*
  Copyright (C) 2001 Renaud Pawlak

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
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA */

package org.objectweb.jac.aspects.distribution.consistency;

import java.lang.reflect.*;
import org.objectweb.jac.core.*;
import org.objectweb.jac.core.dist.*;

/**
 * This wrapper class implements a weak consistency protocol for a set
 * of replicas.
 *
 * <p>The semantics of this protocol is that the readed data can be
 * inconsistent with the other replicas. When a write event occurs,
 * the currently written replicas asks for the consistent state to the
 * valid replica (the owner) and becomes itself the valid
 * replica. This protocol ensures that the set of modifications are
 * finally done in at least one replica.<p>
 *
 * @author <a href="http://cedric.cnam.fr/~pawlak/index-english.html">Renaud Pawlak</a> */

public class WeakConsistencyWrapper extends ConsistencyWrapper {

   /** A reference to the valid copy. */ 
   RemoteRef owner = null;
   /** True if we are the owner of the valid copy. */
   boolean isOwner = false;

   public WeakConsistencyWrapper(AspectComponent ac) {
      super(ac);
   }

   /**
    * Update the wrappee state with the owner replica state before
    * proceeding the writing method.<p>
    *
    * The wrappee then becomes the owner.<p>
    *
     @return the value returned by the wrapped method */

   public Object whenWrite(Interaction interaction) {
      
      if(!isOwner) {
         /* Warn the owner and retrieves its state */
         Object[] ownerState = (Object[]) owner.invokeRoleMethod(
            "acceptRemoteWrite", new Object[] {}
         );
         
         /* Set the new state */
         Field[] fields = interaction.wrappee.getClass().getDeclaredFields();
         for ( int i = 0; i < fields.length; i++ ) {
            try {
               fields[i].set( interaction.wrappee, ownerState[i] );
            } catch (Exception e) {}
         }

         /* Warn the replicas that we are the new owner */
         for (int i = 0; i < knownReplicas.size(); i++) {
            ((RemoteRef)knownReplicas.get(i)).invokeRoleMethod(
               "setOwner",
               new Object[] { interaction.wrappee }
            );
         }
      }
      
      /* Call the wrappee... */ 
      return proceed(interaction);
   }

   /**
    * The current object is not the owner anymore and returns the
    * object state so that the new owner can be consistent 
    *
    * @param remoteReplica the replica that is beeing written
    * @param data this parameter is not used in this protocol (its
    * value is null)
    * @return the state of the object so that the written object can
    * become a consistent owner */

    public Object acceptRemoteWrite(Wrappee wrappee, RemoteRef remoteReplica,
                                    Object[] data) {
       Field[] fields = wrappee.getClass().getDeclaredFields();
       Object[] state = new Object[fields.length];
       for ( int i = 0; i < fields.length; i++ ) {
          try {
             state[i] = fields[i].get(wrappee);
          } catch (Exception e) {}
       }
       isOwner = false;
       return state;
   }
   
   /**
    * This role method sets a new owner.
    * 
    * @param newOwner a remote reference on the new owner
    * @return null */

   public Object setOwner (RemoteRef newOwner) {
      owner = newOwner;
      isOwner = false;
      return null;
   }
      
}
