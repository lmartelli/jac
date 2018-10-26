/*
  Copyright (C) 2001 Lionel Seinturier.

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

package org.objectweb.jac.core.dist;

import org.aopalliance.intercept.ConstructorInvocation;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.*;
import org.objectweb.jac.util.*;

/**
 * StubWrapper is a dynamic client stub for org.objectweb.jac.
 * Every method called on an object wrapped by such a wrapper
 * is forwarded to a remote reference.
 *
 * The call is blocking.
 * For non-blocking calls see NonBlockingStubWrapper.
 *
 * This a wrapper class.
 * The invoke method wraps all the methods of a wrappee.
 *
 * @see org.objectweb.jac.core.dist.NonBlockingStubWrapper
 *
 * @author <a href="http://www-src.lip6.fr/homepages/Lionel.Seinturier/index-eng.html">Lionel Seinturier</a>
 */
 
public class StubWrapper extends Wrapper {
    static Logger logger = Logger.getLogger("stub");

   /**
    * Construct a new dynamic stub.
    *
    * @param remoteRef  the remote reference associated to the stub
    */
   
   public StubWrapper(AspectComponent ac, RemoteRef remoteRef) {
      super(ac);
      this.remoteRef = remoteRef;
   }

   /**
    * A more user-friendly constructor.
    *
    * @param serverContainer the name of the container where the
    * server is deployed (can be a regular expression) */

   public StubWrapper(AspectComponent ac, String serverContainer) {
      super(ac);
      this.serverContainer = serverContainer;
      Topology t = Topology.getPartialTopology( serverContainer );
      if( t!=null && t.countContainers()>0 ) {
         this.serverContainer=t.getContainer(0).getName(); 
      } else {
         this.serverContainer = serverContainer;
      }
   }
   
   String serverContainer = null;
   
   /** The remote reference attached to this stub */

   protected RemoteRef remoteRef;
   
   
   /**
    * The getter method for the remoteRef field.
    *
    * @return  the remoteRef field
    */
    
   public RemoteRef getRemoteRef() { return remoteRef; }
   
   
   /**
    * Forward a call to the remote reference.
    */
   
   public Object _invoke(Interaction interaction) {

      if( remoteRef == null ) {
         if( serverContainer == null ) {
            logger.warn("local call (1) for stub "+interaction.wrappee);
            return proceed(interaction);
         }
         RemoteContainer rc = Topology.get().getFirstContainer(serverContainer);
         if( rc == null ) {
            logger.warn("local call (2) for stub "+interaction.wrappee);
            return proceed(interaction);
         }
         remoteRef = rc.bindTo(NameRepository.get().getName(interaction.wrappee));
         if( remoteRef == null ) {
            logger.warn("local call (3) for stub "+interaction.wrappee+
                        " ("+rc+","+serverContainer+")");
            return proceed(interaction);
         }
      }

      logger.debug(interaction.wrappee + " forwards to the server");
   
      /** Invoke the remote reference */

      return remoteRef.invoke(interaction.method.getName(), interaction.args);
   }

   public Object invoke(MethodInvocation invocation) throws Throwable {
	   return _invoke((Interaction) invocation);
   }

   public Object construct(ConstructorInvocation invocation)
	   throws Throwable {
	   throw new Exception("Wrapper "+this+" does not support construction interception.");
   }

}







