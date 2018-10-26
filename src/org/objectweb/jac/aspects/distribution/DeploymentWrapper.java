package org.objectweb.jac.aspects.distribution;


import org.aopalliance.intercept.ConstructorInvocation;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.AspectComponent;
import org.objectweb.jac.core.Interaction;
import org.objectweb.jac.core.Wrapper;
import org.objectweb.jac.core.dist.Deployment;
import org.objectweb.jac.core.dist.Topology;
import org.objectweb.jac.util.Log;

/**
 * This wrapper wraps constructors in order to deploy the objects on
 * remote host(s) after their initialization. */

public class DeploymentWrapper extends Wrapper {
    static Logger logger = Logger.getLogger("deployment");

	String hostExpr;
	boolean state = true;

	/**
	 * The constructor.
	 *
	 * @param ac the aspect component that owns this wrapper
	 * @param hostExpr a regular expression that gives the host where
	 * the wrapped object should be deployed
	 * @param state a flag that tells if the state of the deployed
	 * object should be copied on the remote host(s) or not */

	public DeploymentWrapper(
		AspectComponent ac,
		String hostExpr,
		Boolean state) {
		super(ac);
		this.hostExpr = hostExpr;
		this.state = state.booleanValue();
	}

	public Object invoke(MethodInvocation invocation) throws Throwable {
		throw new Exception("This wrapper does not support invocation wrapping");
	}

	public Object construct(ConstructorInvocation invocation)
		throws Throwable 
    {
		return deploy((Interaction) invocation);
	}

	/**
	 * Actually performs the deployment on a constructor
	 * interaction. */

	public Object deploy(Interaction i) {
		Object o = proceed(i);
		logger.debug("deploy upcalled with " + hostExpr
                     + " wrappee=" + i.wrappee
                     + ", topology=" + Topology.get());
		Topology topology = Topology.getPartialTopology(hostExpr);
		Deployment dep = new Deployment(ac, topology);
		if (state) {
			dep.replicate(i.wrappee);
		} else {
			dep.replicateStruct(i.wrappee);
		}
		return o;
	}
}
