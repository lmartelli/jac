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
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA */

package org.objectweb.jac.aspects.synchronization;

import org.aopalliance.intercept.ConstructorInvocation;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.AspectComponent;
import org.objectweb.jac.core.Interaction;
import org.objectweb.jac.core.Wrapper;
import org.objectweb.jac.util.Log;
import org.objectweb.jac.util.Semaphore;

/**
 * This aspect component allows the programmer to synchronize a set of
 * methods on different objects or classes in a centralized way (do
 * not use the synchronized java keyword anymore).
 *
 * <p>The monitor implementation to provide synchronization is
 * implemented by a semaphore.
 * 
 * @see org.objectweb.jac.util.Semaphore */

public class SynchronizationAC
	extends AspectComponent
	implements SynchronizationConf {

    static Logger logger = Logger.getLogger("synchronization");

	public void synchronize(String classes, String methods, String objects) {
		pointcut(
			objects,
			classes,
			methods,
			new SynchronizationWrapper(this),
			null);
	}

	/**
	 * This inner wrapper implements the methods synchronization with a
	 * semaphore. */

	public class SynchronizationWrapper extends Wrapper {
		// one resource is available by default 
		// (only on thread can enter)
		Semaphore semaphore = new Semaphore(1);

		public SynchronizationWrapper(AspectComponent ac) {
			super(ac);
		}

		public Object synchronize(Interaction interaction) {
			Object ret = null;
			logger.debug(interaction.wrappee
                         + " acquires semaphore for "
                         + interaction.method);
			semaphore.acquire();
			try {
				proceed(interaction);
			} finally {
				logger.debug(interaction.wrappee
                             + " releases semaphore for "
                             + interaction.method);
				semaphore.release();
			}
			return ret;
		}

		public Object invoke(MethodInvocation invocation) throws Throwable {
			return synchronize((Interaction) invocation);
		}

		public Object construct(ConstructorInvocation invocation)
			throws Throwable {
			return synchronize((Interaction) invocation);
		}

	}
}
