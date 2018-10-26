/*
  Copyright (C) 2001 Renaud Pawlak renaud@aopsys.com

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
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA */

package org.objectweb.jac.aspects.distribution.consistency;

import java.util.*;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.*;
import org.objectweb.jac.core.dist.*;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.util.*;

/**
 * This wrapper implements a client-server consistency protocol.
 *
 * <p>It is a special consistency protocol since the wrappee acts as a
 * pure client (a stub) for its known replicas.<p>
 * 
 * @author <a href="http://cedric.cnam.fr/~pawlak/index-english.html">Renaud Pawlak</a> */

public class ClientServerConsistencyWrapper extends ConsistencyWrapper {
    static Logger logger = Logger.getLogger("consistency");

    String hosts = null;

    /**
    * A friendly constructor for a client-server consistency wrapper.
    *
    * @param hosts a regular expression that defines the host where
    * the server is located 
    */
    public ClientServerConsistencyWrapper(AspectComponent ac, String hosts) {
        super(ac);
        knownReplicas = null;
        this.hosts = hosts;
    } 

    /** An empty constructor for the Consistency class. */
    public ClientServerConsistencyWrapper(AspectComponent ac) {
        super(ac);
    }

    /**
    * Forwards the call to the server(s).<p>
    *
    * Do not call the replica except if we do not know any server (in
    * this case, we are a server).<p>
    *
    * @return the value returned by the server */

    public Object whenCall(Interaction interaction) {

        if( knownReplicas == null ) {
            knownReplicas = Topology.getPartialTopology(hosts).getReplicas(interaction.wrappee);
        } 

        if(knownReplicas != null && knownReplicas.size() > 0 &&
           (! ((RemoteRef)knownReplicas.get(0)).getRemCont().isLocal()) ) {
            logger.debug("(client-server) call event on " + 
                      NameRepository.get().getName(interaction.wrappee) + 
                      ":" + interaction.method + ":" + 
                      ((RemoteRef)knownReplicas.get(0)).getRemCont().getName());
            Object ret = ((RemoteRef)knownReplicas.get(0)).invokeRoleMethod(
                "acceptRemoteCall",
                new Object[] { null, new Object[] { interaction.method, interaction.args } }
            );
            return ret;
        } else {
            /* we do not know any replica, so we are the server... */
            return proceed(interaction);
        }
    }

    /**
    * Calls the method on the server.
    *
    * @param remoteReplica the client remote reference
    * @param data the name and the parameters of the server method
    */

    public Object acceptRemoteCall(Wrappee wrappee, RemoteRef remoteReplica,
                                   Object[] data) {
        logger.debug("(client-server) remote call event on " + 
                     NameRepository.get().getName(wrappee) +
                     ":" + (String)data[0] + ":" + 
                     Arrays.asList((Object[])data[1]));
        return ClassRepository.get().getClass(wrappee).getMethod((String)data[0]).invoke(
            wrappee , (Object[])data[1] );
    }
}
