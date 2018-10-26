/*
  Copyright (C) 2003 <laurent@aopsys.com>

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

package org.objectweb.jac.aspects.idGen;

import org.aopalliance.intercept.ConstructorInvocation;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.AspectComponent;
import org.objectweb.jac.core.Collaboration;
import org.objectweb.jac.core.Interaction;
import org.objectweb.jac.core.NameRepository;
import org.objectweb.jac.core.Wrapper;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.util.Log;

public class IdGenAC extends AspectComponent {
    static Logger logger = Logger.getLogger("idgen");

	public static final String COUNTER = "IdGenAC.COUNTER";
	public static final String ID_FIELD = "IdGenAC.ID_FIELD";

	public void genId(ClassItem cl, String counter, String fieldName) {
		cl.setAttribute(COUNTER, counter);
		cl.setAttribute(ID_FIELD, cl.getField(fieldName));
		pointcut(
			"ALL",
			cl.getName(),
			"CONSTRUCTORS",
			IdGenWrapper.class.getName(),
			null,
			false);
	}

	Counters counters;
	protected Counters getCounters() {
		if (counters == null) {
			if (countersName != null) {
				counters =
					(Counters) NameRepository.get().getObject(countersName);
				if (counters == null) {
					logger.error("IdGenAC: No object named " + countersName);
				}
			}
		}
		return counters;
	}

	String countersName = "counters#0";
	public void setCountersName(String countersName) {
		this.countersName = countersName;
	}

	public class IdGenWrapper extends Wrapper {
		public IdGenWrapper(AspectComponent ac) {
			super(ac);
		}

		public Object genId(Interaction interaction) {
			Object result = proceed(interaction);
			logger.debug("generating id for " + interaction.wrappee);
			ClassItem cl = interaction.getClassItem();
			FieldItem field = (FieldItem) cl.getAttribute(ID_FIELD);
			if (Collaboration.get().getAttribute("PersistenceAC.RESTORE")
				== null) {
				String counterName = (String) cl.getAttribute(COUNTER);
				Counters counters = getCounters();
				if (counters != null) {
					long id = counters.genId(counterName);
					logger.debug("    -> " + id);
					try {
						if (field.getType() == String.class)
							field.setThroughWriter(
								interaction.wrappee,
								Long.toString(id));
						else
							field.setThroughWriter(
								interaction.wrappee,
								new Long(id));
					} catch (IllegalAccessException e) {
						logger.error(
							"Failed to to set field " + field
								+ " for " + interaction.wrappee);
					}
				} else {
					logger.debug("    No counters object");
				}
			} else {
				logger.debug("    skipping id generation for " + interaction.wrappee);
			}
			return result;
		}

		public Object invoke(MethodInvocation invocation) throws Throwable {
			throw new Exception("This wrapper does not support invocation interception.");
		}

		public Object construct(ConstructorInvocation invocation)
			throws Throwable {
			return genId((Interaction) invocation);
		}
	}
}
