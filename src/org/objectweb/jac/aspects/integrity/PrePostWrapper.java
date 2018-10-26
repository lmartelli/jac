/*
  Copyright (C) 2002 Renaud Pawlak <renaud@aopsys.com>

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

package org.objectweb.jac.aspects.integrity;

import org.aopalliance.intercept.ConstructorInvocation;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.AspectComponent;
import org.objectweb.jac.core.Interaction;
import org.objectweb.jac.core.Wrapper;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.ClassRepository;

/**
 * This wrapper provides pre-post condition testing.
 */

public class PrePostWrapper extends Wrapper {
    static final Logger logger = Logger.getLogger("integrity.conditions");

	public PrePostWrapper(AspectComponent ac) {
		super(ac);
	}

	/**
	 * This wrapping method checks if pre and post conditions are
	 * validated for modified fields.
	 */

	public Object checkPrePost(Interaction interaction) throws Exception {
		logger.debug(
			"entering test conditions for " + interaction.method);

		ClassItem curClass =
			ClassRepository.get().getClass(interaction.wrappee);

		Boolean pre = Boolean.TRUE;
		Boolean post = Boolean.TRUE;

		// Handle pre
		try {
			pre =
				(Boolean) curClass.invoke(
					interaction.wrappee,
					"PRE_" + interaction.method.getName(),
					interaction.args);
		} catch (org.objectweb.jac.core.rtti.NoSuchMethodException e) {
			logger.debug(
				"no pre for " + interaction.method.getName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (!pre.booleanValue()) {
			logger.debug(
				"precondition failed for method "
					+ interaction.method.getName());
			throw new Exception(
				"precondition failed for method "
					+ interaction.method.getName());
		}

		Object ret = proceed(interaction);

		// Handle post
		try {
			post =
				(Boolean) curClass.invoke(
					interaction.wrappee,
					"POST_" + interaction.method.getName(),
					interaction.args);
		} catch (org.objectweb.jac.core.rtti.NoSuchMethodException e) {
			logger.debug(
				"no post for " + interaction.method.getName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (!post.booleanValue()) {
			logger.debug(
				"precondition failed for method "
					+ interaction.method.getName());
			throw new Exception(
				"postcondition failed for method "
					+ interaction.method.getName());
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
	 */
	public Object invoke(MethodInvocation invocation) throws Throwable {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.aopalliance.intercept.ConstructorInterceptor#construct(org.aopalliance.intercept.ConstructorInvocation)
	 */
	public Object construct(ConstructorInvocation invocation)
		throws Throwable {
		// TODO Auto-generated method stub
		return null;
	}
}
