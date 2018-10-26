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
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA */

package org.objectweb.jac.aspects.distribution;


import java.util.*;
import org.aopalliance.intercept.ConstructorInvocation;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.*;
import org.objectweb.jac.core.dist.*;
import org.objectweb.jac.util.Log;

/**
 * This Aspect Component allows the programmer to easily implement
 * load-balancing features for its application when JAC is running in
 * distributed mode.
 *
 * @see LoadBalancingConf
 * @author Renaud Pawlak
 */

public class LoadBalancingAC
	extends AspectComponent
	implements LoadBalancingConf 
{
    static Logger logger = Logger.getLogger("loadbalancing");

	public void addRoundTripLoadBalancer(
		String wrappeeName,
		String methods,
		String hostName,
		String replicaExpr) {

		pointcut(
			wrappeeName,
			".*",
			methods + " && !CONSTRUCTORS && !STATICS",
			new LoadBalancingWrapper(this, replicaExpr),
			hostName,
			null);
	}

	public void addRandomLoadBalancer(
		String wrappeeName,
		String methods,
		String hostName,
		String replicaExpr) {

		pointcut(
			wrappeeName,
			".*",
			methods + " && !CONSTRUCTORS && !STATICS",
			new LoadBalancingWrapper(this, replicaExpr),
			hostName,
			null);
	}

	/**
	 * This inner-wrapper handles the load-balancing wrapping methods that
	 * actually implement the load-balancing algorithms. */

	public class LoadBalancingWrapper extends Wrapper {

		int count = 0;
		Vector replicas = null;
		Random random = new Random();
		String hostExpr;
		boolean doFill = true;

		public LoadBalancingWrapper(AspectComponent ac, String hostExpr) {
			super(ac);
			this.hostExpr = hostExpr;
		}

		public void invalidate() {
			doFill = true;
		}

		public Object invoke(MethodInvocation invocation) throws Throwable {
			return roundTripBalance((Interaction) invocation);
		}

		public Object construct(ConstructorInvocation invocation)
			throws Throwable {
			throw new Exception("This wrapper does not support constructor wrapping");
		}

		/**
		 * Performs a round-trip load-balancing. */

		public Object roundTripBalance(Interaction interaction) {
			if (doFill) {
				replicas =
					Topology.getPartialTopology(hostExpr).getReplicas(
						interaction.wrappee);
				logger.debug("filled partial topo with "+ hostExpr
                             + " on "+ Topology.get() + ": " + replicas);
				doFill = false;
			}
			if (replicas.size() == 0) {
				// none replicas where found, we perform a local call and 
				// will try to get them again on the next call
				doFill = true;
				logger.warn(
					"load-balancing: no replica found, on "
						+ interaction.wrappee + ": local call performed");
				return proceed(interaction);
			}
			if (count >= replicas.size()) {
				count = 0;
			}
			return ((RemoteRef) replicas.get(count++)).invoke(
				interaction.method.getName(),
				interaction.args);
		}

		/**
		 * Performs a random load-balancing. */

		public Object randomBalance(Interaction interaction) {
			if (doFill) {
				replicas =
					Topology.getPartialTopology(hostExpr).getReplicas(
						interaction.wrappee);
				doFill = false;
			}
			if (replicas.size() == 0) {
				// none replicas where found, we perform a local call and 
				// will try to get them again on the next call
				doFill = true;
				logger.warn("load-balancing: no replica found, local call performed");
				return proceed(interaction);
			}
			return (
				(RemoteRef) replicas.get(
					random.nextInt(replicas.size()))).invoke(
				interaction.method.getName(),
				interaction.args);
		}
	}

}
