/*
  Copyright (C) 2001-2002 Renaud Pawlak <renaud@aopsys.com>, 
  Laurent Martelli <laurent@aopsys.com>
  
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

package org.objectweb.jac.aspects.authentication;

import org.aopalliance.intercept.ConstructorInvocation;
import org.aopalliance.intercept.MethodInvocation;
import org.objectweb.jac.aspects.gui.*;
import org.objectweb.jac.core.*;
import org.objectweb.jac.core.AspectComponent;
import org.objectweb.jac.core.rtti.*;
import org.objectweb.jac.util.*;

/**
 * This wrapper uses an authenticator and a controller to ensure that
 * the current interaction is authenticated and allowed.
 *
 * @see AuthenticationAC
 * @see Authenticator */

public class AuthenticationWrapper extends Wrapper {

	Authenticator authenticator;
	MethodItem controller;

	public Object invoke(MethodInvocation invocation) throws Throwable {
		return authenticateAndControl((Interaction) invocation);
	}

	public Object construct(ConstructorInvocation invocation)
		throws Throwable {
		return authenticateAndControl((Interaction) invocation);
	}

	/**
	 * Constructs a new authentication wrapper.
	 *
	 * @param ac the aspect component that owns this wrapper
	 * @param authenticator the object that authenticates
	 * @param controller the method that grants the rights or not 
	 */
	public AuthenticationWrapper(
		AspectComponent ac,
		Authenticator authenticator,
		MethodItem controller) 
    {
		super(ac);
		Log.trace(
			"authentication",
			"new authentication wrapper: " + authenticator + "," + controller);
		this.authenticator = authenticator;
		this.controller = controller;
	}

	/**
	 * Sets the access rights controller.
	 *
	 * @param controller a static method of the prototype <code>boolean
	 * controller(String username,Object wrappee,MethodItem method)</code>
	 * @see org.objectweb.jac.aspects.user.UserAC#userController(String,Object,MethodItem)
	 * @see #dummyController(String,Object,MethodItem)
	 */
	public void setController(MethodItem controller) {
		Log.trace(
			"authentication",
			"wrapper setController(" + controller + ")");
		this.controller = controller;
	}

	public void setAuthenticator(Authenticator authenticator) {
		Log.trace(
			"authentication",
			"wrapper setAuthenticator(" + authenticator + ")");
		this.authenticator = authenticator;
	}

	/**
	 * This wrapping method authenticates a call on the wrapped method
	 * and controls that the authentcated user owns the rights to call
	 * it.<p>
	 *
	 * @return the value returned by the wrapped method 
     */
	public Object authenticateAndControl(Interaction interaction)
		throws AuthenticationFailedException, AccessDeniedException, Throwable {

		if (interaction.wrappee instanceof Display
			&& interaction.method.getName().equals("showCustomized")) {
			CustomizedGUI cgui = (CustomizedGUI) interaction.args[1];
			if (cgui != null) {
				Log.trace(
					"application",
					"auth sets application to " + cgui.getApplication());
				Collaboration.get().setCurApp(cgui.getApplication());
			} else {
				Log.trace(
					"application",
					"auth cannot set the application since "
                    + "customized GUI is null");
			}
		}

		Log.trace(
			"authentication",
			"authenticate for method "
            + interaction.method
            + " on "
            + interaction.wrappee);
		Log.trace(
			"authentication",
			"name is: " + (String) attr(AuthenticationAC.USER));
		String name = (String) attr(AuthenticationAC.USER);
		String password = null;
		if (name == null) {
			try {
				name = authenticator.authenticate();
				Log.trace("authentication", "authenticated " + name);
				attrdef(AuthenticationAC.USER, name);
			} catch (Exception e) {
				Log.trace(
					"authentication",
					"user authentication failed for "
                    + interaction.method
                    + " because of exception: "
                    + e);
				e.printStackTrace();
			}
		}
		try {
			Boolean allowed =
				(Boolean) controller.invokeStatic(
					new Object[] {
						name,
						interaction.wrappee,
						interaction.method });
			if (allowed.booleanValue()) {
				Log.trace(
					"authentication",
					"accesses granted to "
                    + name
                    + " for "
                    + interaction.method);
				return proceed(interaction);
			} else {
				Log.trace(
					"authentication",
					"accesses denied to "
                    + name
                    + " for "
                    + interaction.method);
				throw new AccessDeniedException(
					accessDeniedMessage != null
                    ? accessDeniedMessage
                    : "you are not allowed to call "
                    + interaction.method
                    + " on "
                    + interaction.wrappee);
			}
		} catch (Exception e) {
			Log.trace(
				"authentication",
				"accesses denied to "
                + name
                + " for "
                + interaction.method
                + " because of exception: "
                + e);
			Log.trace("authentication", 2, e);
			throw new AccessDeniedException(
				"you are not allowed to call "
                + interaction.method
                + " on "
                + interaction.wrappee
                + ": "
                + e);
		}
	}

	/**
	 * Returns true if the user is in the trusted users list.
	 *
	 * @param username the name of the user to check
	 * @return true if trusted 
     */
	public boolean isTrustedUser(String username) {
		Log.trace(
			"authentication",
			"is trusted user: " + username + ":" + getAspectComponent());
		return ((AuthenticationAC) getAspectComponent()).isTrustedUser(
			username);
	}

	String accessDeniedMessage = "Access denied";

	/**
	 * Sets the message to show when the access is denied.
	 * 
	 * @param message the message 
     */
	public void setAccessDeniedMessage(String message) {
		this.accessDeniedMessage = message;
	}

	/**
	 * This exception handler reacts when the access is denied.
	 *
	 * @param e the acces denied exception 
     */
	public void catchAccessDenied(AccessDeniedException e) {
		Log.trace("authentication", "catching " + e.toString());
		DisplayContext context =
			(DisplayContext) Collaboration.get().getAttribute(
				GuiAC.DISPLAY_CONTEXT);
		if (context != null) {
			Display display = context.getDisplay();
			if (display != null) {
				display.showMessage("Error", accessDeniedMessage);
				return;
			}
		}
		Log.error("no display available");
	}

	/**
	 * Always return true. Use to force authentication.
	 *
	 * @param username the user's name
	 * @param wrappee the authenticated method
	 * @param method the authenticated method
	 */
	public static boolean dummyController(
		String username,
		Object wrappee,
		MethodItem method) {
		return true;
	}

}
