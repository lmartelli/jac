/*
  Copyright (C) 2001 Renaud Pawlak <renaud@aopsys.com>

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

package org.objectweb.jac.wrappers;

import org.aopalliance.intercept.ConstructorInvocation;
import org.aopalliance.intercept.MethodInvocation;
import org.objectweb.jac.core.AspectComponent;
import org.objectweb.jac.core.Interaction;
import org.objectweb.jac.core.Wrapper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This wrapper forwards the method calls that arrive on the wrappee
 * to another object.  The wrapped methods should be supported both by
 * the wrappee and the forwardee. Otherwise, an exception is raised.
 *
 * @author <a href="http://cedric.cnam.fr/~pawlak/index-english.html">Renaud Pawlak</a>
 *
 * @see ForwardingException */

public class ForwardingWrapper extends Wrapper {

	/**
	 * Construct a new forwarding wrapper.
	 *
	 * @param forwardee the object that receives the forwarded calls */

	public ForwardingWrapper(AspectComponent ac, Object forwardee) {
		super(ac);
		this.forwardee = forwardee;
	}

	/** The forwardee. */
	protected Object forwardee;

	/**
	 * The getter method for the forwardee.
	 *
	 * @return the object to which the calls are forwarded
	 *
	 * @see #forward(Interaction)
	 */

	public Object getForwardee() {
		return forwardee;
	}

	/**
	 * Forwards all the incoming calls to the forwardee.
	 *
	 * <p>The forwardee class must support the wrapped method
	 * prototype. Otherwise, an exception is raised.
	 *
	 * @return the result of the forwarded method call
	 */

	public Object forward(Interaction interaction) throws ForwardingException {

		Object ret = null;

		//      System.out.println( "Forwarding " + method() + " to " + forwardee );
		Method[] methods = forwardee.getClass().getMethods();
		boolean ok = false;
		for (int i = 0; i < methods.length; i++) {
			//System.out.println ( "---> trying " + methods[i].getName() + method_args );
			if (methods[i].getName().equals(interaction.method)) {
				try {
					ok = false;
					//System.out.println ( "Try to call " + method_name );
					ret = methods[i].invoke(forwardee, interaction.args);
					ok = true;
				} catch (IllegalArgumentException e) {
				} catch (InvocationTargetException e) {
					Throwable t = e.getTargetException();
					if (t instanceof RuntimeException) {
						throw (RuntimeException) t;
					}
					e.printStackTrace();
					System.exit(1);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
					System.exit(1);
				}
				if (ok)
					break;
			}
		}
		if (!ok) {
			throw new ForwardingException(
				"The forwardee '"
					+ forwardee
					+ "' does not support '"
					+ interaction.method
					+ "' to forward from '"
					+ interaction.wrappee
					+ "'.");
		}

		return ret;
	}

	public Object invoke(MethodInvocation invocation) throws Throwable {
		return forward((Interaction) invocation);
	}

	public Object construct(ConstructorInvocation invocation)
		throws Throwable {
		throw new Exception("Wrapper "+this+" does not support construction interception.");
	}
}
