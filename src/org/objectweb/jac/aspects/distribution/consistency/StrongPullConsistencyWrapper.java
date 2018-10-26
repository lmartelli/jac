/*
  Copyright (C) 2001-2002 Renaud Pawlak <renaud@aopsys.com>

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

import java.util.*;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.*;
import org.objectweb.jac.core.dist.*;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.util.*;

/**
 * This wrapper class implements the core protocol for a strong
 * consistency that is based on a pull strategy.
 *
 * <p>On contrary to the <code>StrongPushConsistencyWrapper</code>,
 * this protocol pulls the data from the other replicas. Indeed, each
 * time a data is read and is not locally available, it is fetched
 * from the known replicas.<p>
 *
 * @see StrongPushConsistencyWrapper
 * 
 * @author <a href="http://cedric.cnam.fr/~pawlak/index-english.html">Renaud Pawlak</a>
 */

public class StrongPullConsistencyWrapper extends ConsistencyWrapper {
    static Logger logger = Logger.getLogger("consistency");

    String hosts = null;

    /**
    * A friendly constructor for a pull consistency wrapper.
    *
    * @param hosts a regular expression that defines the host where
    * the consistency protocol is installed */

    public StrongPullConsistencyWrapper(AspectComponent ac, String hosts) {
        super(ac);
        this.hosts = hosts;
        knownReplicas = null;
    } 

    /** An empty constructor for the Consistency class. */
    public StrongPullConsistencyWrapper(AspectComponent ac) {
        super(ac);
    } 

    /**
    * This wrapping method first try to use the wrappee to get the
    * result of the read method.<p>
    * 
    * If this result is null or if an exception occurs, it considers
    * that the read information was not locally present and tries to
    * fetch it from the replicas it knows (recursivelly all the
    * replicas are finally asked). If none of the replica returns a
    * value, it returns null or throws an exception.
    *
    * @return the value returned by the wrapped read method */

    public Object whenRead(Interaction interaction) {

        Object ret = null;
      
        ret = proceed(interaction);

        logger.debug("Pull knownReplicas = "+knownReplicas);

        if (ret == null) {

            if (knownReplicas != null) {
                Collaboration c = Collaboration.get();
                Vector asked_replicas = (Vector)c.getAttribute(visitedReplicas);
                if (asked_replicas == null) {
                    asked_replicas = (Vector)c.addAttribute( 
                        visitedReplicas, new Vector());
                }

                //System.out.println ( "########### " + notified_replicas );
                try {
                    Vector new_ar = new Vector();
                    RemoteRef cur_replica = RemoteRef.create( 
                        NameRepository.get().getName(interaction.wrappee), 
                        interaction.wrappee);
               
                    for (int i = 0; i < knownReplicas.size(); i++) {
                        if ( (! asked_replicas.contains( knownReplicas.get(i) ) ) &&
                             (! ((RemoteRef)knownReplicas.get(i)).getRemCont().isLocal()) ) {
                     
                            Vector kr = new Vector(knownReplicas);
                            kr.remove(knownReplicas.get(i));
                            new_ar.clear();
                            new_ar.addAll(asked_replicas);
                            new_ar.addAll(kr);
                            new_ar.add(cur_replica);
                            c.addAttribute(visitedReplicas, new_ar);
                     
                            logger.debug("(strong pull) read event on " + 
                                      interaction.wrappee + ":" + 
                                      interaction.method + ":" + 
                                      ((RemoteRef)knownReplicas.get(i)).getRemCont().getName());
                            ret = ((RemoteRef)knownReplicas.get(i)).invokeRoleMethod(
                                "acceptRemoteRead",
                                new Object[] { null,
                                               new Object[] { interaction.method,
                                                              interaction.args } } );
                            if (ret != null) {
                                break;
                            }
                        }
                    }
                } finally {
                    c.addAttribute(visitedReplicas, null);
                }
            }
        }   
        return ret;
    }
      
    /**
    * Try to read the method asked by <code>whenRead</code>.<p>
    *
    * The data is :<p>
    * <ul><pre>
    * data[0] = the read method name string
    * data[1] = an array that contains the arguments of the read method 
    * </pre></ul>
    *
    * @param remoteReplica the replica that received the read event
    * @param data the read event data
    * @return the return value of the <code>data[0]</code> method*/

    public Object acceptRemoteRead(Wrappee wrappee, RemoteRef remoteReplica,
                                   Object[] data) {
        logger.debug("(strong pull) remote read event on " + 
                     wrappee + ":" + (String)data[0]);
        return ClassRepository.get().getClass(wrappee).getMethod((String)data[0]).invoke(
            wrappee , (Object[])data[1] );
    }
   
      
}
