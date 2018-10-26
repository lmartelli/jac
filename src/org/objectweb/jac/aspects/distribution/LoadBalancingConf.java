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

package org.objectweb.jac.aspects.distribution;

/**
 * This is the configuration interface of the load-balancing aspect
 *
 * @see LoadBalancingAC
 * @author Renaud Pawlak
 */

public interface LoadBalancingConf {

   /**
    * This configuration method allows the user to define a round-trip
    * load-balancer on a replication group.
    *
    * <p>It assumes that a replication group exists on a set of host
    * denoted by <code>replicaExpr</code>. It also assumes that an
    * uncorrelated replica called <code>wrappeeName</code> exists on
    * <code>hostName</code>. Note that this distributed scheme can be
    * easilly obtained by configuring the deployment aspect for an
    * object <code>myObject</code> like this:
    *
    * <pre>
    * replicated "myObject" ".*[1-6]"
    * </pre>
    *
    * <p>This means that <code>myObject</code> is replicated on all
    * the hosts one to six and that the replicas are strongly
    * consistent. Then, you can configure the load-balancing:
    *
    * <pre>
    * addRoundTripLoadBalancer "photorepository0" ".*" "s0" ".*[1-6]"
    * </pre>
    *
    * <p>Note that the round-trip balancer (located on s0) changes the
    * replica it uses for each invocation. The followed sequence is
    * 1,2,3,4,5,6,1,2,3,4,5,6,1,...
    *
    * <p>An alternative to the round-trip load-balancer is the random
    * load-balancer that randomly picks out the next replica to
    * use. This can be useful when a total decoralation is needed for
    * all clients.
    *
    * @param wrappeeName the name of the object that is replicated and
    * that will act as a load-balancer proxy 
    * @param methods a pointcut expression for the method that perform
    * the load-balancing (others perform local calls)
    * @param hostName the host where the proxy load-balances
    * @param replicaExpr a regular expression that matches all the
    * hosts of the topology where the replicas are located
    *
    * @see #addRandomLoadBalancer(String,String,String,String) */
    

   void addRoundTripLoadBalancer( String wrappeeName,
                                  String methods,
                                  String hostName, 
                                  String replicaExpr );
   
   /**
    * This configuration method allows the user to define a random
    * load-balancer on a replication group.
    *
    * <p>It follows the same principles as a round-trip balancer but
    * picks up the next replica to use randomly.
    * 
    * @see #addRoundTripLoadBalancer(String,String,String,String) */

   void addRandomLoadBalancer( String wrappeeName,
                               String methods,
                               String hostName, 
                               String replicaExpr );

}






