package org.objectweb.jac.samples.bench;

import org.aopalliance.intercept.ConstructorInvocation;
import org.aopalliance.intercept.MethodInvocation;
import org.objectweb.jac.core.AspectComponent;
import org.objectweb.jac.core.Wrapper;
import org.objectweb.jac.core.Interaction;

public class NullAC extends AspectComponent {

	/**
	 * Simple sample of configuration method which wraps methods
	 *
	 * @param n number of pointcuts to create
	 * @param wrappeeExpr the objects to wrap
	 * @param wrappeeClassExpr the classes to wrap
	 * @param wrappeeMethodExpr the methods to wrap
	 */

	public void wrap(
		int n,
		String wrappeeExpr,
		String wrappeeClassExpr,
		String wrappeeMethodExpr) {
		for (; n > 0; n--) {
			pointcut(
				wrappeeExpr,
				wrappeeClassExpr,
				wrappeeMethodExpr,
				NullWrapper.class.getName(),
				"doProceed",
				null,
				false);
		}
	}

	public class NullWrapper extends Wrapper {
		public NullWrapper(AspectComponent ac) {
			super(ac);
		}
		public Object doProceed(Interaction interaction) {
			//System.out.print("+");
			return proceed(interaction);
		}
		public Object invoke(MethodInvocation invocation) throws Throwable {
			return doProceed((Interaction) invocation);
		}
		public Object construct(ConstructorInvocation invocation)
			throws Throwable {
			return doProceed((Interaction) invocation);
		}
	}
}
