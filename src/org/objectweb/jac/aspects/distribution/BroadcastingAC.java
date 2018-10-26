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

  You should have received a copy of the GNU Lesser Generaly Public License
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
 * This aspect component implements a simple broadcasting aspect.
 *
 * <p>Principles: a broadcaster, located on a given host forwards some
 * calls to a set of replicas located on remote hosts.
 *
 * @see BroadcastingConf
 * @author Renaud Pawlak
 */

public class BroadcastingAC
	extends AspectComponent
	implements BroadcastingConf 
{
    static Logger logger = Logger.getLogger("broadcasting");

	public void addBroadcaster(
		String wrappeeName,
		String methods,
		String broadcasterHost,
		String replicasHost) {

		pointcut(
			wrappeeName,
			".*",
			methods + " && !CONSTRUCTORS",
			new BroadcastingWrapper(this, replicasHost),
			broadcasterHost,
			null);
	}

	/**
	 * This wrapper wraps the broadcaster with a wrapping method that
	 * broadcast all the calls to the remote replicas. */

	public class BroadcastingWrapper extends Wrapper {

		Vector replicas = null;
		String hostExpr;
		boolean doFill = true;

		public BroadcastingWrapper(AspectComponent ac, String hostExpr) {
			super(ac);
			this.hostExpr = hostExpr;
		}

		public Object invoke(MethodInvocation invocation) throws Throwable {
			return broadcast((Interaction) invocation);
		}

		public Object construct(ConstructorInvocation invocation)
			throws Throwable {
			throw new Exception("This wrapper does not support constructor wrapping");
		}

		public void invalidate() {
			doFill = true;
		}

		/**
		 * Performs a broadcasting. */

		public Object broadcast(Interaction interaction) {
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
				logger.warn("no replica found, local call performed");
				return proceed(interaction);
			}
			Object ret = null;
			for (int i = 0; i < replicas.size(); i++) {
				ret =
					((RemoteRef) replicas.get(i)).invoke(
						interaction.method.getName(),
						interaction.args);
			}
			return ret; //proceed(interaction);
		}

	}
}
