/*
  Copyright (C) 2001-2002 Renaud Pawlak <renaud@aopsys.com>
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
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA */

package org.objectweb.jac.aspects.session;

import java.util.*;
import org.aopalliance.intercept.ConstructorInvocation;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.*;
import org.objectweb.jac.core.Collaboration;
import org.objectweb.jac.util.*;

/**
 * This wrapper handles the session for each object within the JAC system.
 *
 * @see #handleSession(Interaction)
 * @author Renaud Pawlak */

public class SessionWrapper extends Wrapper {
    static Logger logger = Logger.getLogger("session");

	/** Stores the sessions and their contextual attributes (sid ->
	    saved attributes). */
	protected static Hashtable sessions = new Hashtable();

	/** Stores the applications (sid -> applicationName) */
	protected static Hashtable applications = new Hashtable();

	public SessionWrapper(AspectComponent ac) {
		super(ac);
	}

	/**
	 * Removes a session attribute.
	 *
	 * <p>The given attribute will be forgotten for the current
	 * interaction and for all the forthcoming interactions of the same
	 * session. This can be used for instance to log-out a user.
	 *
	 * @param name the name of the attribute to forget */

	public void clearCurrentSessionAttribute(String name) {
		String sid = (String) attr(SessionAC.SESSION_ID);
		if (sid == null) {
			logger.debug("clearCurrentSessionAttribute: no Session_ID found");
			return;
		}
		logger.debug("clearCurrentSessionAttribute for session " + sid);
		Hashtable attrs = (Hashtable) sessions.get(sid);
		if (attrs != null) {
			attrs.remove(name);
		}
		Collaboration.get().removeAttribute(name);
	}

	/**
	 * Handles sessions for the wrapped method.
	 *
	 * <p>The session handling algorithm is:
	 *
	 * <ul>
	 *   <li>if the session id - <code>attr("Session.sid")</code> - 
	 *       is not defined, do nothing</li>
	 *   <li>try to restore the saved context attributes for this session id 
	 *       if already saved</li>
	 *   <li>else save them into the <code>sessions</code> field</li>
	 * </ul>
	 *
	 * @return the wrapped method return value
	 */

	public Object handleSession(Interaction interaction) {

		Object result = null;

		String sid = (String) attr(SessionAC.SESSION_ID);

		if (attr(SessionAC.INITIALIZED) != null) {

			logger.debug("session initiliazed for "
                         +interaction.wrappee+"."+ interaction.method);

            // I believe we do not need this (Laurent)
            /*
			if (applications.containsKey(sid)) {
				Log.trace("application",
                          "retreiving cur app from session: "
                          + (String) applications.get(sid));
                if (!applications.get(sid).equals(Collaboration.get().getCurApp()))
                    Log.warning("curr app changed from "+Collaboration.get().getCurApp()+" to "+applications.get(sid));
				Collaboration.get().setCurApp((String) applications.get(sid));
			}
            */

			result = proceed(interaction);

            /*
			if (applications.containsKey(sid)) {
				Log.trace("application",
                          "retreiving cur app from session: "
                          + (String) applications.get(sid));
                if (!applications.get(sid).equals(Collaboration.get().getCurApp()))
                    Log.warning("curr app changed from "+Collaboration.get().getCurApp()+" to "+applications.get(sid));
				Collaboration.get().setCurApp((String) applications.get(sid));
			}
            */

			return result;

		}

		logger.debug("handling session "+sid+" for "
                     + interaction.wrappee + "." + interaction.method);
        
		if (sid == null) {
			logger.debug("session is not defined by client");
			return proceed(interaction);
		}

		if (applications.containsKey(sid)) {
			logger.debug("retreiving cur app from session: "
                                     + (String) applications.get(sid));
			Collaboration.get().setCurApp((String) applications.get(sid));
		}

		logger.debug("in session, application=" + Collaboration.get().getCurApp());

		logger.debug("found session " + sid + " for " + interaction.method.getName());
		Hashtable savedAttributes = null;

		if (sessions.containsKey(sid)) {
			savedAttributes = (Hashtable) sessions.get(sid);
		} else {
			savedAttributes = new Hashtable();
			sessions.put(sid, savedAttributes);
		}

		String[] storedAttributes =
			((SessionAC) getAspectComponent()).getStoredAttributes();

		for (int i = 0; i < storedAttributes.length; i++) {
			if (savedAttributes.containsKey(storedAttributes[i])) {
				logger.debug("reading "+ storedAttributes[i]
                             + "=" + savedAttributes.get(storedAttributes[i])
                             + " for " + interaction.method.getName());
				Collaboration.get().addAttribute(
					storedAttributes[i],
					savedAttributes.get(storedAttributes[i]));
			}
		}

		attrdef(SessionAC.INITIALIZED, "true");

		result = proceed(interaction);

		for (int i = 0; i < storedAttributes.length; i++) {
			if (Collaboration.get().getAttribute(storedAttributes[i])
				!= null) {
				logger.debug("saving " + storedAttributes[i]
                             + "=" + Collaboration.get().getAttribute(storedAttributes[i])
                             + " for " + interaction.method.getName());
				savedAttributes.put(
					storedAttributes[i],
					Collaboration.get().getAttribute(storedAttributes[i]));
			} else {
				logger.debug("NOT saving " + storedAttributes[i] 
                             + "=" + Collaboration.get().getAttribute(storedAttributes[i])
                             + " for " + interaction.method.getName());
			}
		}
		String application = Collaboration.get().getCurApp();
		if (application != null) {
			logger.debug("session saves app "+application);
			applications.put(sid, application);
		}
		return result;
	}

	public Object invoke(MethodInvocation invocation) throws Throwable {
		return handleSession((Interaction) invocation);
	}

	public Object construct(ConstructorInvocation invocation)
		throws Throwable {
		return handleSession((Interaction) invocation);
	}

}
