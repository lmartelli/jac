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
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA */

package org.objectweb.jac.aspects.tracing;

import org.aopalliance.intercept.ConstructorInvocation;
import org.aopalliance.intercept.MethodInvocation;
import org.objectweb.jac.core.*;
import java.util.*;
import org.objectweb.jac.core.dist.*;

/**
 * This wrapper upcalls the debugger when a method that have to be
 * debugged is called and returns.
 *
 * @see Debugger */

public class DebuggingWrapper extends Wrapper {

	/** The actual debugger. */
	public Debugger debugger = null;

	/**
	 * The wrapper constructor.
	 *
	 * @param ac the aspect that manages this wrapper
	 */
	public DebuggingWrapper(AspectComponent ac) {
		super(ac);
		debugger = (Debugger) NameRepository.get().getObject("debugger0");
		if (debugger == null) {
			debugger = new Debugger();
		}
	}

	/**
	 * This wrapping method is used to upcall the debugger at each
	 * method call.
	 *
	 * @return the return value of the orginal method
	 *
	 * @see Debugger#startOfMethod(String,String,String,Object[])
	 * @see Debugger#endOfMethod(String,String,String,Object[],Object,long) */

	public Object step(Interaction interaction) {

		String wrappeeName =
			(String) NameRepository.get().getName(interaction.wrappee);
		debugger.startOfMethod(
			Distd.getLocalContainerName(),
			wrappeeName,
			interaction.method.getName(),
			interaction.args);
		Date d1 = new Date();
		Object ret = proceed(interaction);
		Date d2 = new Date();

		debugger.endOfMethod(
			Distd.getLocalContainerName(),
			wrappeeName,
			interaction.method.getName(),
			interaction.args,
			ret,
			d2.getTime() - d1.getTime());

		return ret;
	}

	public Object invoke(MethodInvocation invocation) throws Throwable {
		return step((Interaction) invocation);
	}

	public Object construct(ConstructorInvocation invocation)
		throws Throwable {
		return step((Interaction) invocation);
	}

}
