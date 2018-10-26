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

package org.objectweb.jac.aspects.persistence;

import org.apache.log4j.Logger;
import org.objectweb.jac.core.AspectComponent;
import org.objectweb.jac.core.Wrappee;
import org.objectweb.jac.core.Wrapper;
import org.objectweb.jac.core.Wrapping;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.util.ExtArrays;

/**
 * This wrapper defines persistence extensions for objects that a
 * defined persitent by a persistent aspect component. */

public abstract class AbstractPersistenceWrapper extends Wrapper {

    static Logger logger = Logger.getLogger("persistence");

 	public AbstractPersistenceWrapper(AspectComponent ac) {
		super(ac);
	}

	public static final String ATTR_ADDED = "persistence.added";

	static boolean isWrapped(Wrappee wrappee, FieldItem field) {
		Object value = field.get(wrappee);
		if (value instanceof Wrappee
			&& Wrapping.isExtendedBy(
				(Wrappee) value,
				null,
				CollectionWrapper.class)) {
			logger.debug(field + " is wrapped");
			return true;
		} else {
			logger.debug(field + " is not wrapped");
			return false;
		}
	}

	/**
	 * Gets the object in memory object for a "storage" value.
	 * @param value the "storage" value
	 * @return if value is an OID, a reference to the object with that
	 * OID is returned, otherwise, value is returned.
	 * @see #normalizeInput(Object)
	 */
	public final Object normalizeOutput(Object value) throws Exception {
		if (value instanceof OID) {
			value = getAC().getObject((OID) value, null);
		}
		return value;
	}

	/**
	 * Performs various stuff before storing a value in a storage. If
	 * the value is a Wrappee, it is made persistent.
	 * @param value the value to be stored
	 * @return if value is a wrappee, the OID of the object is
	 * returned, otherwise, value is returned.
	 * @see #normalizeInput(Object) 
	 */
	public final Object normalizeInput(Object value) throws Exception {
		if (value != null) {
			logger.debug(
				"normalizeInput(" + value.getClass().getName() + ")");
		}
		if (value instanceof Wrappee) {
			Wrappee wrappee = (Wrappee) value; // added object
			try {
				Wrapping.invokeRoleMethod(
					wrappee,
					"makePersistent",
					ExtArrays.emptyObjectArray);
			} catch (Exception e) {
				logger.error("makePersistent failed for "+wrappee, e);
			}

			//PersistenceWrapper wrapper = getAC().makePersistent(wrappee);

			if (getOID(wrappee) == null) {
				// this should never happen
				throw new InvalidOidException("oid is NULL,");
			}
			value = getOID(wrappee);
		} else {
			if (value != null) {
				logger.debug(
					value.getClass().getName() + " is not a wrappee");
			}
		}
		return value;
	}

	public final OID getOID(Wrappee wrappee) {
		return getAC().getOID(wrappee);
	}

	final void setOID(Wrappee wrappee, OID oid) {
		getAC().registerObject(oid, wrappee);
	}

	/**
	 * Tells if the current object is persistent (role method).
	 *
	 * @return true if persistent 
     */
	public final boolean isPersistent(Wrappee wrappee) {
		return getOID(wrappee) != null;
	}

    /**
     * Returns the storage for a given class
     *
     * @param cli a class
     * @return the storage of the class, or null.
     */
	public final Storage getStorage(ClassItem cli) {
		try {
			return getAC().getStorage(cli);
		} catch (Exception e) {
			logger.error("getStorage failed "+cli.getName(),e);
			return null;
		}
	}

	public final PersistenceAC getAC() {
		return (PersistenceAC) getAspectComponent();
	}

	public final void checkOid(Wrappee wrappee) throws InvalidOidException {
		if (getOID(wrappee) == null)
			throw new InvalidOidException("oid is NULL");
	}

	public static class InvalidOidException extends RuntimeException {
		public InvalidOidException(String msg) {
			super(msg);
		}
	}
}
