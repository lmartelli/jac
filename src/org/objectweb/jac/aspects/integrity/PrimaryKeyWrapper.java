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

import java.util.Collection;
import java.util.Iterator;
import org.aopalliance.intercept.ConstructorInvocation;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.AspectComponent;
import org.objectweb.jac.core.Interaction;
import org.objectweb.jac.core.Wrapper;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.core.rtti.CollectionItem;
import org.objectweb.jac.core.rtti.MethodItem;
import org.objectweb.jac.core.rtti.RttiAC;

/**
 * This wrapper provides a key scheme to limit fields
 * doubles.
 */

public class PrimaryKeyWrapper extends Wrapper {
    static final Logger logger = Logger.getLogger("integrity.primary");

	public PrimaryKeyWrapper(AspectComponent ac) {
		super(ac);
	}

	/**
	 * This wrapping method checks if the added Object's fields match
	 * with an Object in the added collection, and throws an exception
	 * if there is one.
	 */
	public Object checkDoubles(Interaction interaction) throws Exception {
		logger.debug("entering doubles-check for " + interaction.method);

		CollectionItem coll =
			((MethodItem) interaction.method).getAddedCollection();

		String[] fieldsToComp =
			(String[]) coll.getAttribute(RttiAC.PRIMARY_KEY);

		Object addedObject = interaction.args[0];
		logger.debug("added value: " + addedObject);

		Collection collection = coll.getActualCollection(interaction.wrappee);

		Object[] newFields = new Object[fieldsToComp.length];
        ClassItem cli = ClassRepository.get().getClass(addedObject);
		for (int i=0; i<fieldsToComp.length; i++) {
			newFields[i] = 
                cli.getField(fieldsToComp[i])
					.getThroughAccessor(addedObject);
		}

		Iterator it = collection.iterator();
		while (it.hasNext()) {
			Object object = it.next();
			if (((fieldsToComp == null) || (fieldsToComp.length == 0))
				&& (object.equals(addedObject)))
				throw new Exception(
					"Collection "
						+ coll.getName()
						+ " already contains an element equals to "
						+ addedObject);

			if ((fieldsToComp.length == 0)
				|| (!addedObject.getClass().equals(object.getClass())))
				continue;

			int counter = 0;
            cli = ClassRepository.get().getClass(object);
			for (int i=0; i<fieldsToComp.length; i++) {
				Object valueToTest =
                	cli.getField(fieldsToComp[i]).getThroughAccessor(object);

				if (((valueToTest == null) && (newFields[i] == null))
					|| ((valueToTest != null)
						&& (valueToTest.equals(newFields[i]))))
					counter++;
				else
					break;
			}
			if (counter == fieldsToComp.length) {
				String fields = "";
				for (int j = 0; j < fieldsToComp.length; j++) {
					if (j > 0)
						fields += ", ";
					fields += fieldsToComp[j];
				}
				throw new Exception(
					"Collection "
						+ coll.getName()
						+ " already contains an element with fields { "
						+ fields
						+ " } equals to those in the added element");
			}
		}
		return proceed(interaction);
	}

	public Object invoke(MethodInvocation invocation) throws Throwable {
		return checkDoubles((Interaction) invocation);
	}

	public Object construct(ConstructorInvocation invocation)
		throws Throwable {
		throw new Exception("Wrapper "+this+" does not support construction interception.");
	}
}
