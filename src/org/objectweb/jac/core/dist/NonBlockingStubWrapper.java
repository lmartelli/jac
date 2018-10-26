/*

  Copyright (C) 2001 Lionel Seinturier

  This program is free software; you can redistribute it and/or modify
  it under the terms of the Lesser GNU Lesser General Public License as
  published by the Free Software Foundation; either version 2 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser Generaly Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA */

package org.objectweb.jac.core.dist;

import org.apache.log4j.Logger;
import org.objectweb.jac.core.*;
import org.objectweb.jac.util.*;

/**
 * NonBlockingStubWrapper is a dynamic client stub for org.objectweb.jac.
 * Every method called on an object wrapped by such a wrapper
 * is forwarded to a remote reference.
 *
 * The call is non blocking.
 * For blocking calls see StubWrapper.
 *
 * This a wrapper class.
 * The invoke method wraps all the methods of a wrappee.
 *
 * @see org.objectweb.jac.core.dist.StubWrapper
 *
 * @author <a href="http://www-src.lip6.fr/homepages/Lionel.Seinturier/index-eng.html">Lionel Seinturier</a>
 */
 
public class NonBlockingStubWrapper extends StubWrapper {
    static Logger logger = Logger.getLogger("stub");

   /**
    * Construct a new dynamic stub.
    *
    * @param remoteRef  the remote reference associated to the stub
    */
   
    public NonBlockingStubWrapper(AspectComponent ac, RemoteRef remoteRef) {
        super(ac,remoteRef);
    }

    /**
    * A more user-friendly constructor.
    *
    * @param serverContainer the name of the container where the
    * server is deployed */

    public NonBlockingStubWrapper(AspectComponent ac, String serverContainer) {
        super(ac,serverContainer);
    }


    /**
    * Forward a call to the remote reference.
    */
   
    public Object invoke(Interaction interaction) {

        if (remoteRef == null) {
            if (serverContainer == null) {
                logger.warn("local call (1) for stub "+interaction.wrappee);
                return proceed(interaction);
            }
            RemoteContainer rc = Topology.get().getFirstContainer(serverContainer);
            if (rc == null) {
                logger.warn("local call (2) for stub "+interaction.wrappee);
                return proceed(interaction);
            }
            remoteRef = rc.bindTo(NameRepository.get().getName(interaction.wrappee));
            if (remoteRef == null) {
                logger.warn("local call (3) for stub "+interaction.wrappee+
                            " ("+rc+","+serverContainer+")");
                return proceed(interaction);
            }
        }

        logger.debug(interaction.wrappee + " forwards to the server");
      
        /**
         * These final local variables are required
         * for the enclosed local class defined below.
         */
      
        final String finalMethodName = interaction.method.getName();
        final Object[] finalMethodArgs = interaction.args;
   
        // I disabled the results...
        new Thread() {
                public void run() {
                    //results[firstFreeCell] =
                    remoteRef.invoke( finalMethodName, finalMethodArgs );
                }
            } . start();
      
        return null;
        //new Integer( firstFreeCell );
    }
   
    public void setFirstFreeCell() {
    }

    /**
     * Maximum number of results stored.
     *
     * The idea is that if nbMaxOfResults consecutive calls have been made,
     * the probability that the initial result is to be required is low.
     */
    final static protected int nbMaxOfResults = 16;

    /**
     * Mailbox for results received from asynchonous calls.
     * The array is managed as a circular list.
     */
    protected Object[] results = new Object[nbMaxOfResults];

    /** Index of the 1st free cell in results. */
    protected int firstFreeCell = 0;

    /**
     * Return the requested result.
     *
     * @param index  the result index
     * @return       the requested result
     */
    public Object getResult( Integer index ) {
        int ind = index.intValue();
        Object result = results[ind];
      
        // Deference the result in the array.
        // Once the result have been requested, if we do not set the cell to null,
        // we may prevent the result object from being garbage collected.
        results[ind] = null;
      
        return result;
    }

}
