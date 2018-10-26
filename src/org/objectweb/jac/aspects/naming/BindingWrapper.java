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

package org.objectweb.jac.aspects.naming;

import org.aopalliance.intercept.ConstructorInvocation;
import org.aopalliance.intercept.MethodInvocation;
import org.objectweb.jac.core.*;
import org.objectweb.jac.wrappers.ForwardingWrapper;

/**
 * This wrapper class binds an object to the actual named object by
 * forwarder.
 *
 * <p><code>BindingWrapper</code> wraps the JAC object that that has not
 * been resolved yet by the binding aspect. When a call is performed
 * on the wrappee, the binder wrapper gets the actual JAC object that
 * corresponds to the logical name by asking the name repository of
 * the naming aspect. Then, it creates a forwarding wrapper to this
 * object and replaces itself with it.
 * 
 * <p>The binding aspect uses the naming aspect. Do not try to use it
 * alone.
 *
 * @author <a href="http://cedric.cnam.fr/~pawlak/index-english.html">Renaud Pawlak</a>
 *
 * @see ForwardingWrapper */

public class BindingWrapper extends Wrapper {

	/**
	 * Construct a new binding wrapper.
	 *
	 * @param logicalName the name of the wrappee within the naming
	 * aspect */

	public BindingWrapper(AspectComponent ac, String logicalName) {
		super(ac);
		this.logicalName = logicalName;
	}

	/** The name of the Jac object the binber must bind to. */
	protected String logicalName;

	/**
	 * The getter method for the Jac object's name (role method).
	 *
	 * @return the name
	 */

	public String getLogicalName() {
		return logicalName;
	}

	/**
	 * This wrapping method binds the wrappee.
	 *
	 * <p>Binds the wrappee to its actual location by creating a new
	 * forwarder wrapper to wrap it. This wrapping method is called
	 * only once since the binder wrapper unwraps itself once the new
	 * wrapper is created.
	 *
	 * @see ForwardingWrapper */

	public Object bind(Interaction interaction) throws BindingErrorException {

		/** Get the name repository */
		NameRepository repository = (NameRepository) NameRepository.get();

		if (repository == null) {
			throw new BindingErrorException("Binding aspect cannot work without the naming aspect.");
		}

		/** Get the actual reference of the object by invoking the
		    repository. */

		Object object = repository.getObject(logicalName);

		//if (true) return null;

		if (object == null) {
			throw new BindingErrorException(
				"Object '" + logicalName + "' not found in the repository.");
		}

		//      RemoteRef rr = null;
		Object to_forward = null;

		/** If their is several objects for the name, then it is a
		    replicated object. We try to bind to the local one if
		    exist. It not, we will bind to the first one. This is the
		    only interaction with the distribution aspect. */

		//        if (objects.length > 1) {
		//            for (int i = 0; i < objects.length; i++) {
		//               if ( ! ((Wrappee)objects[i]).isWrappedByClass ( StubWrapper.class ) ) {
		//                  to_forward = objects[i];
		//               } else {
		//                  if ( ((RemoteRef) ((Wrappee)objects[i]).invokeRoleMethod ( 
		//                     "org.objectweb.jac.dist.StubWrapper", "getRemoteRef", new Class[] {}, new Object[] {} ))
		//                       . getRemCont().isLocal () ) {
		//                     to_forward = objects[i];
		//                  }
		//               }
		//            }
		//         }

		//         if ( to_forward == null ) {
		//            to_forward = objects[0];
		//        }

		/** We replace the binder with a forwarder. */

		ForwardingWrapper forwarder =
			new ForwardingWrapper(getAspectComponent(), object);
		Wrapping.unwrapAll(interaction.wrappee, null, this);
		Wrapping.wrapAll(interaction.wrappee, null, forwarder);

		/** We redo the call (that will be forwarded this time */
		Object ret =
			interaction.method.invoke(interaction.wrappee, interaction.args);
		return ret;

	}

	public Object invoke(MethodInvocation invocation) throws Throwable {
		return bind((Interaction) invocation);
	}

	public Object construct(ConstructorInvocation invocation)
		throws Throwable {
		throw new Exception("Wrapper "+this+" does not support construction interception.");
	}

}
