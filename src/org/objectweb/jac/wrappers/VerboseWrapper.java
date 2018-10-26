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

package org.objectweb.jac.wrappers;


import java.util.Arrays;
import java.util.Date;
import org.aopalliance.intercept.ConstructorInvocation;
import org.aopalliance.intercept.MethodInvocation;
import org.objectweb.jac.aspects.gui.GuiAC;
import org.objectweb.jac.core.AspectComponent;
import org.objectweb.jac.core.Collaboration;
import org.objectweb.jac.core.Interaction;
import org.objectweb.jac.core.Jac;
import org.objectweb.jac.core.NameRepository;
import org.objectweb.jac.core.Wrapper;
import org.objectweb.jac.core.Wrapping;
import org.objectweb.jac.core.WrappingChain;
import org.objectweb.jac.core.rtti.MethodItem;
import org.objectweb.jac.util.Log;

/**
 * This wrapper prints some informations about the wrapped method when
 * it is called. */

public class VerboseWrapper extends Wrapper {

    public static final int TIMED = 0;
    public static final int NAMED = 1;
    public static final int STACK = 2;
    public static final int WRAPPERS= 3;

    protected int type = TIMED;

    static final String DISABLE_TRACE = "DISABLE_TRACE";

	public VerboseWrapper(AspectComponent ac, int type) {
		super(ac);
        this.type = type;
	}

	/**
	 * A wrapping method that prints out the calling informations.
	 *
	 * @return the value of the called method */

	public Object printCallingInfos(Interaction interaction) {

		/** Before code ... */
		Date d1 = new Date();
		String on =
			interaction.method.isStatic()
				? ""
				: (" on '"
					+ NameRepository.get().getName(interaction.wrappee)
					+ "'");

		System.out.println(
			"<<< "
				+ (d1.getTime() - Jac.getStartTime().getTime())
				+ " ms: calling '" + interaction.method + "'"
				+ on
				+ ", args = "
				+ Arrays.asList(interaction.args)
				+ " >>>");
		/** End before code */

		Object ret = interaction.proceed();

		/** After code ... */
		Date d2 = new Date();
		System.out.println(
			"<<< "
				+ (d2.getTime() - Jac.getStartTime().getTime())
				+ " ms: returning '"
				+ interaction.method
				+ "'"
				+ on
				+ ", value = "
				+ ret
				+ ", duration = "
				+ (d2.getTime() - d1.getTime())
				+ " ms >>>");
		/** End after code */

		return ret;
	}

	public Object printNamedArgs(Interaction interaction) {
        Collaboration collab = Collaboration.get();
        if (collab.getAttribute(DISABLE_TRACE)!=null) 
            return proceed(interaction);
        // printing of parameters can call the wrapped method
        collab.addAttribute(DISABLE_TRACE, Boolean.TRUE);
        try {
            String[] paramNames = GuiAC.getParameterNames(interaction.method);
            String params =
                interaction.method.isStatic()
				? ""
				: "this=" + GuiAC.toString(interaction.wrappee);
            Object[] args = interaction.args;
            for (int i = 0; i < args.length; i++) {
                if (paramNames != null) {
                    params += "," + paramNames[i] + "=" + GuiAC.toString(interaction.args[i]);
			} else {
				params += ",arg[" + i + "]=" + GuiAC.toString(interaction.args[i]);
			}
            }
            System.out.println("Calling " + interaction.method + " with " + params);
        } finally {
            collab.removeAttribute(DISABLE_TRACE);
        }

        Object result = proceed(interaction);

        collab.addAttribute(DISABLE_TRACE, Boolean.TRUE);
        try {
            if (interaction.method instanceof MethodItem) {
                Class returnType = ((MethodItem) interaction.method).getType();
                if (returnType != void.class) {
                    if (returnType.isArray()
                        && Object.class.isAssignableFrom(
                            returnType.getComponentType()))
                        System.out.println(
                            "Called "
							+ interaction.method
							+ " -> "
							+ Arrays.asList((Object[]) result));
                    else
					System.out.println(
						"Called " + interaction.method + " -> " + GuiAC.toString(result));
                }
            }
        } finally {
            collab.removeAttribute(DISABLE_TRACE);
        }
        return result;
	}

	public Object printStackTrace(Interaction interaction) {
		System.out.println(
			"Calling '"
				+ interaction.method
				+ "' on '"
				+ NameRepository.get().getName(interaction.wrappee)
				+ "', args = "
				+ Arrays.asList(interaction.args));
		new Exception().printStackTrace();
		return proceed(interaction);
	}

	public Object printWrappersTrace(Interaction interaction) {
		String on =
			interaction.method.isStatic()
				? ""
				: (" on '"
					+ NameRepository.get().getName(interaction.wrappee)
					+ "'");

		System.out.println(
			"Calling '"
				+ interaction.method
				+ "'"
				+ on
				+ ", args = "
				+ Arrays.asList(interaction.args));
		WrappingChain chain =
			Wrapping.getWrappingChain(interaction.wrappee, interaction.method);
		System.out.println("WrappingChain = " + chain);
		return proceed(interaction);
	}

	// TODO: support all the possible traces

	public Object invoke(MethodInvocation invocation) throws Throwable {
        switch (type) {
            case TIMED:
                return printCallingInfos((Interaction) invocation);
            case NAMED:
                return printNamedArgs((Interaction) invocation);
            case STACK:
                return printStackTrace((Interaction) invocation);
            case WRAPPERS:
                return printWrappersTrace((Interaction) invocation);
            default:
                Log.error("VerboseWrapper: unknown type "+type);
        }
		return invocation.proceed();
	}

	public Object construct(ConstructorInvocation invocation)
		throws Throwable {
		return printCallingInfos((Interaction) invocation);
	}
}
