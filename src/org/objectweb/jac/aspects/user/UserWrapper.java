/*
  Copyright (C) 2002-2003 Laurent Martelli <laurent@aopsys.com>
                          Renaud Pawlak <renaud@aopsys.com>

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

package org.objectweb.jac.aspects.user;

import org.aopalliance.intercept.ConstructorInvocation;
import org.aopalliance.intercept.MethodInvocation;
import org.objectweb.jac.aspects.authentication.AuthenticationAC;
import org.objectweb.jac.aspects.gui.GuiAC;
import org.objectweb.jac.core.AspectComponent;
import org.objectweb.jac.core.Interaction;
import org.objectweb.jac.core.ObjectRepository;
import org.objectweb.jac.core.Wrapper;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.util.Log;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

/**
 * This wrapper is used by the user aspect to implement several
 * croscutting treatments.
 */

public class UserWrapper extends Wrapper {

	public UserWrapper(AspectComponent ac) {
		super(ac);
	}

	/** Classes to initialize */
	Vector classes = new Vector();
	public void addClass(ClassItem cl) {
		classes.add(cl);
	}

	/** 
	 * Set user attributes of an object.
	 *
	 * @param wrappee the object to initialize
	 * @param user the user object
	 */
	protected void setUser(Object wrappee, Object user) {
		UserAC ac = (UserAC) getAspectComponent();
		ClassItem classItem = ClassRepository.get().getClass(wrappee);
		FieldItem[] fields = classItem.getFields();
		for (int i = 0; i < fields.length; i++) {
			if (fields[i].getTypeItem() == ac.getUserClass()) {
				if (fields[i].getThroughAccessor(wrappee) == null) {
					Log.trace(
						"user",
						"initializing field "
							+ fields[i].getName()
							+ " with "
							+ GuiAC.toString(user));
					try {
						fields[i].setThroughWriter(wrappee, user);
					} catch (Exception e) {
						Log.error(
							"Failed to set user field "
								+ wrappee
								+ "."
								+ fields[i]
								+ " with "
								+ user);
						e.printStackTrace();
					}
				}
			}
		}
	}

	protected Object getUser() {
		UserAC ac = (UserAC) getAspectComponent();
		Object user = attr(UserAC.USER);
		if (user == null) {
			// Get a user from authentication
			String authuser = (String) attr(AuthenticationAC.USER);
			if (authuser != null) {
				Collection users =
					ObjectRepository.getObjects(ac.getUserClass());
				Iterator i = users.iterator();
				// Find a user whose loginField matches the authenticated username
				while (i.hasNext()) {
					Object testedUser = i.next();
					Object id =
						ac.getLoginField().getThroughAccessor(testedUser);
					if (authuser.equals(id)) {
						user = testedUser;
						break;
					}
				}
			}
		}
		return user;
	}

	/**
	 * Set the user attribute of new objects
	 */
	public Object setUserOnNew(Interaction interaction) {
		Log.trace("user", "setUser for " + interaction.method);
		Object result = proceed(interaction);
		UserAC ac = (UserAC) getAspectComponent();
		Object user = getUser();

		Log.trace("user", "user = " + user + "(" + GuiAC.toString(user) + ")");
		if (user != null) {
			setUser(interaction.wrappee, user);
		}
		return result;
	}

	/**
	 * <p>Set user attributes of parameters.</p>
	 */
	public Object setUser(Interaction interaction) {
		Log.trace("user", "setUser for " + interaction.method);
		Object result = proceed(interaction);
		UserAC ac = (UserAC) getAspectComponent();
		Object user = getUser();

		Log.trace("user", "user = " + user + "(" + GuiAC.toString(user) + ")");
		if (user != null) {
			for (int i = 0; i < interaction.args.length; i++) {
				if (classes
					.contains(
						ClassRepository.get().getClass(interaction.args[i]))) {
					Log.trace(
						"user",
						"Init param "
							+ i
							+ "("
							+ interaction.args[i]
							+ ") of "
							+ interaction.method);
					setUser(interaction.args[i], user);
				}
			}
		}
		return result;
	}

	public Object invoke(MethodInvocation invocation) throws Throwable {
		return setUser((Interaction) invocation);
	}
	public Object construct(ConstructorInvocation invocation)
		throws Throwable {
			return setUserOnNew((Interaction)invocation);
	}

}
