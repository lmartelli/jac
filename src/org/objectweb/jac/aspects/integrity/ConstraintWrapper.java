/*
  Copyright (C) 2002 Julien van Malderen, Renaud Pawlak <renaud@aopsys.com>

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

import java.util.Iterator;
import java.util.Vector;
import org.aopalliance.intercept.ConstructorInvocation;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.AspectComponent;
import org.objectweb.jac.core.Interaction;
import org.objectweb.jac.core.Wrapper;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.MethodItem;

/**
 * This wrapper provides a constraints scheme to limit fields
 * modifications.
 */

public class ConstraintWrapper extends Wrapper {
    static final Logger logger = Logger.getLogger("integrity.conditions");

	public ConstraintWrapper(AspectComponent ac) {
		super(ac);
	}

	private void invokeTest(
		MethodItem method,
		FieldItem field,
		Object[] args,
		String errorMsg)
		throws Exception 
    {
		Boolean result = (Boolean) method.invoke(null, args);

		if (result == null)
			throw new Exception("The constraint " + method + " does not exist");

		if (!result.booleanValue()) {
			if ((errorMsg == null) || errorMsg.length() == 0)
				throw new Exception(
					"The constraint "
						+ method
						+ " has not been validated for "
						+ field);
			throw new Exception(field + ": " + errorMsg);
		}
	}

	/**
	 * This wrapping method checks if pre and post conditions are
	 * validated for modified fields.
	 */

	public Object testConditions(Interaction interaction) throws Exception {
		logger.debug(
			"entering test conditions for " + interaction.method);

		IntegrityAC ac = (IntegrityAC) getAspectComponent();
		String fieldName =
			((MethodItem) interaction.method).getWrittenFields()[0].getName();
		ClassItem curClass =
			ClassRepository.get().getClass(interaction.wrappee);
		FieldItem field = null;
		Vector preList = null;

		// get the pre list from the superclasses if not defined here
		while (preList == null && curClass != null) {
			field = curClass.getField(fieldName);
			preList = (Vector) ac.preConditionsFields.get(field);
			logger.debug(
				field.getClassItem() + "." + field + " => " + preList);
			curClass = curClass.getSuperclass();
		}

		Vector postList = null;
		curClass = ClassRepository.get().getClass(interaction.wrappee);

		// get the post list from the superclasses if not defined here
		while (postList == null && curClass != null) {
			field = curClass.getField(fieldName);
			postList = (Vector) ac.postConditionsFields.get(field);
			logger.debug(
				field.getClassItem() + "." + field + " => " + preList);
			curClass = curClass.getSuperclass();
		}

		if (preList != null) {
			Iterator i = preList.iterator();
            Object currentFieldValue = 
                field.getThroughAccessor(interaction.wrappee);
			while (i.hasNext()) {
				ConstraintDescriptor conditionToTest =
					(ConstraintDescriptor) i.next();
				logger.debug(
					"testing pre " + conditionToTest.getConstraint());
				invokeTest(
					conditionToTest.getConstraint(),
					field,
					new Object[] {
						interaction.wrappee,
						field,
						currentFieldValue,
						conditionToTest.getParams()},
					conditionToTest.getErrorMsg());
			}
		}

		if (postList != null) {
			Iterator i = postList.iterator();
			while (i.hasNext()) {
				ConstraintDescriptor conditionToTest =
					(ConstraintDescriptor) i.next();
				logger.debug(
					"testing post " + conditionToTest.getConstraint());
				invokeTest(
					conditionToTest.getConstraint(),
					field,
					new Object[] {
						interaction.wrappee,
						field,
						interaction.args[0],
						conditionToTest.getParams()},
					conditionToTest.getErrorMsg());
			}
		}

		return proceed(interaction);
	}

	public Object invoke(MethodInvocation invocation) throws Throwable {
		return testConditions((Interaction) invocation);
	}
}
