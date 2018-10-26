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
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA */

package org.objectweb.jac.aspects.distribution.consistency;

import java.util.*;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.*;
import org.objectweb.jac.core.dist.RemoteRef;
import org.objectweb.jac.core.rtti.MethodItem;
import org.objectweb.jac.util.Log;

/**
 * This wrapper implements a consistency protocol that forwards all
 * the writing calls to all the replicas that are known by the
 * wrapper.
 *
 * <p>It is called "push" since the wrapper pushes the data to the
 * other replicas. Despite this strategy is the most curently used,
 * other strong or weak consistency strategies can be implemented by
 * other consistency wrappers.
 * 
 * @author <a href="http://cedric.cnam.fr/~pawlak/index-english.html">Renaud Pawlak</a>
 *
 * @see #whenWrite(Interaction)
 * @see StrongPullConsistencyWrapper
 * @see WeakConsistencyWrapper */

public class StrongPushConsistencyWrapper extends ConsistencyWrapper {
    static Logger logger = Logger.getLogger("consistency");

    /** a false that is true during notification. */
    boolean inNotification = false;

    /**
     * A friendly constructor for a push consistency wrapper.
     *
     * @param hosts a regular expression that defines the host where
     * the consistency protocol is installed */

    public StrongPushConsistencyWrapper(AspectComponent ac, String hosts) {
        super(ac);
        knownReplicas = null;
        this.hosts = hosts;
    } 

    /** An empty constructor for the Consistency class. */
    public StrongPushConsistencyWrapper(AspectComponent ac) {
        super(ac);
    } 

    /**
     * Forwards the call to all the replicas and then call the
     * replica.<p>
     *
     * The pushing mecanism is stopped by using the collaboration
     * attribute value defined by <code>visitedReplicas</code>.
     *
     * @return the value returned by the wrapped method.
     * @see ConsistencyWrapper#getVisitedReplicas() */

    public Object whenWrite(Interaction interaction) {

        Object ret = null;

        if (knownReplicas == null) {
            calculateKnownReplicas(interaction.wrappee);
        } 

        attrdef("Persistence.disabled", "true");

        if (knownReplicas != null) {
            if (inNotification) return proceed(interaction);
            inNotification = true;
            Collaboration c = Collaboration.get();
            Vector notified_replicas = (Vector)c.getAttribute(visitedReplicas);
            if (notified_replicas == null) {
                notified_replicas = (Vector)c.addAttribute( 
                    visitedReplicas, new Vector());
            }

            try {
                Vector new_nr = new Vector();
                RemoteRef cur_replica = RemoteRef.create( 
                    NameRepository.get().getName(interaction.wrappee), 
                    interaction.wrappee);
            
                for (int i = 0; i < knownReplicas.size(); i++) {
                    if ( (! notified_replicas.contains(knownReplicas.get(i)) ) && 
                         (! ((RemoteRef)knownReplicas.get(i)).getRemCont().isLocal()) ) {
                  
                        Vector kr = new Vector(knownReplicas);
                        kr.remove(knownReplicas.get(i));
                        new_nr.clear();
                        new_nr.addAll(notified_replicas);
                        new_nr.addAll(kr);
                        new_nr.add (cur_replica);
                        c.addAttribute(visitedReplicas, new_nr);
                  
                        logger.debug("(strong) write event on " + 
                                     NameRepository.get().getName(interaction.wrappee)+
                                     ":" + interaction.method + ":" +
                                     ((RemoteRef)knownReplicas.get(i)).getRemCont().getName());
                        try {
                            ((RemoteRef)knownReplicas.get(i)).invokeRoleMethod(
                                "acceptRemoteWrite",
                                new Object[] { null, 
                                               new Object[] { interaction.method, 
                                                              interaction.args } }
                            );
                        } catch ( Exception e ) {
                            logger.error("strong consistency error: "+
                                         "failed to remotely invoke "+
                                         "acceptRemoteWrite for "+
                                         interaction.wrappee+"."+interaction.method);
                            e.printStackTrace();
                            break;
                        }
                    }
                }
            } finally {
                c.addAttribute(visitedReplicas,null);
            }
        } else {
            logger.debug("none replicas are known for "+
                         NameRepository.get().getName(interaction.wrappee));
        }
        ret = proceed(interaction);

        inNotification = false;

        return ret;
    }

    /**
     * This method is called by <code>whenWrite</code> to push the
     * needed data when a state is writen in a remote replica.
     *
     * The data is :<p>
     * <ul><pre>
     * data[0] = the write method name string
     * data[1] = an array that contains the arguments of the write method 
     * </pre></ul>
     *
     * @param remoteReplica expected to be a reference on the remote
     * replica that recieved the write event
     * @param data the data transmittedd by <code>whenWrite</code>
     * @return null by default
     * @see #whenWrite(Interaction) */

    public Object acceptRemoteWrite(Wrappee wrappee, RemoteRef remoteReplica,
                                    Object[] data) {
        logger.debug("(strong) remote write event on " + 
                     NameRepository.get().getName(wrappee) + 
                     ":" + data[0]);
        Object ret=null;
        try {
            ret = ((MethodItem)data[0]).invoke(
                wrappee, (Object[])data[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }
   
    /**
     * Push the current replica state to the binding new replica.
     *
     * @param newReplica the replica that is currently binding
     */

    public void whenBindingNewReplica(Wrappee wrappee, RemoteRef newReplica) {
        logger.debug("(strong) initialized " + 
                     newReplica + " with " + 
                     NameRepository.get().getName(wrappee));
        newReplica.remoteCopy(wrappee);
    }
      
}
