/*
  Copyright (C) 2002-2003 Renaud Pawlak <renaud@aopsys.com>

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

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.AspectComponent;
import org.objectweb.jac.core.Collaboration;
import org.objectweb.jac.core.Wrapper;
import org.objectweb.jac.core.rtti.CollectionItem;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.MethodItem;
import org.objectweb.jac.core.rtti.RttiAC;
import org.objectweb.jac.util.Strings;

/**
 * This aspect handle different kinds of data integrity among objects
 * sets. */

public class IntegrityAC extends AspectComponent implements IntegrityConf {
    static final Logger loggerRep = Logger.getLogger("integrity.repository");

	HashMap preConditionsFields = new HashMap();
	HashMap postConditionsFields = new HashMap();

	RoleWrapper roleWrapper = new RoleWrapper(this);

	public static final String CONSTRAINTS = "IntegrityAC.CONSTRAINTS";
	public static final String DISABLE_ROLE_UPDATES =
		"IntegrityAC.DISABLE_ROLE_UPDATES";

	public static final String DISABLE_ADD_TO_REPOSITORY =
		"IntegrityAC.DISABLE_ADD_TO_REPOSITORY";

	public void declareRepository(
		String repositoryName,
		CollectionItem collection,
		FieldItem field) 
    {
		RepositoryWrapper rw =
			new RepositoryWrapper(
				this,
				repositoryName,
				collection,
				field,
				RepositoryWrapper.ADDER);
		if (field instanceof CollectionItem) {
			pointcut(
				"ALL",
				field.getParent().getName(),
				"ADDERS(" + field.getName() + ")",
				rw,
				null);
		} else {
			pointcut(
				"ALL",
				field.getParent().getName(),
				"SETTERS(" + field.getName() + ")",
				rw,
				null);
		}

		// This pointcut adds an attribute in context so that we can
		// avoid adding an object twice in the repository
		pointcut(repositoryName,collection.getParent().getName(),
		         "ADDERS("+collection.getName()+")",
                 new AntiDupWrapper(this,collection),null);
	}

    class AntiDupWrapper extends Wrapper {
        public AntiDupWrapper(AspectComponent ac, CollectionItem collection) {
            super(ac);
            this.collection = collection;
        }
        CollectionItem collection;
        public Object invoke(MethodInvocation invocation) throws Throwable {
            disableAddToRepository(collection);
            loggerRep.debug("doAddToRepository " + collection);
            try {
                return proceed(invocation);
            } finally {
                enableAddToRepository(collection);
            }
        }
    }

	public void declareConstraint(
		FieldItem relation,
		FieldItem target,
		String constraint) 
    {
		List constraints = (List) target.getAttributeAlways(CONSTRAINTS);
		if (constraints == null) {
			constraints = new Vector();
			target.setAttribute(CONSTRAINTS, constraints);
		}
		int c;
		if (constraint.compareToIgnoreCase("DELETE_CASCADE") == 0) {
			warning("DELETE_CASCADE is not implemented yet");
			c = Constraint.DELETE_CASCADE;
		} else if (constraint.compareToIgnoreCase("SET_NULL") == 0) {
			c = Constraint.SET_NULL;
		} else if (constraint.compareToIgnoreCase("FORBIDDEN") == 0) {
			c = Constraint.FORBIDDEN;
		} else {
			throw new RuntimeException("Unknown constraint type " + constraint);
		}
		constraints.add(new Constraint(relation, c));
		if (target instanceof CollectionItem) {
			pointcut(
				"ALL",
				target.getParent().getName(),
				"REMOVERS(" + target.getName() + ")",
				new RepositoryWrapper(
					this,
					"",
					(CollectionItem) target,
					null,
					RepositoryWrapper.REMOVER),
				null);
		} else {
			pointcut(
				"ALL",
				target.getParent().getName(),
				"SETTERS(" + target.getName() + ")",
				new RepositoryWrapper(
					this,
					"",
					null,
					target,
					RepositoryWrapper.REMOVER),
				null);
		}
	}

	boolean hasConditions = false;

	private void addCondition(
		FieldItem field,
		MethodItem constraint,
		Object[] params,
		String errorMsg,
		HashMap conditionsList) 
    {
		hasConditions = true;
		field.setAttribute(RttiAC.CONSTRAINTS, Boolean.TRUE);

		Vector vect = (Vector) conditionsList.get(field);
		if (vect == null)
			vect = new Vector();

		ConstraintDescriptor obj =
			new ConstraintDescriptor(constraint, params, errorMsg);
		vect.add(obj);
		conditionsList.put(field, vect);
	}

	public void addPostCondition(
		FieldItem field,
		MethodItem constraint,
		Object[] params,
		String errorMsg) 
    {
		addCondition(
			field,
			constraint,
			params,
			errorMsg,
			postConditionsFields);
	}

	public void addPreCondition(
		FieldItem field,
		MethodItem constraint,
		Object[] params,
		String errorMsg) 
    {
		addCondition(
			field,
			constraint,
			params,
			errorMsg,
			preConditionsFields);
	}

	/**
	 * Activates constraints checking.
	 *
	 * <p>This method must be called once after all calls to
	 * <code>addPreCondition</code> and <code>addPostCondition</code>
	 * to effectively add pointcuts for these methods.</p>
	 *
	 * @see #addPreCondition(FieldItem,MethodItem,Object[],String)
	 * @see #addPostCondition(FieldItem,MethodItem,Object[],String)
	 */
	protected void doCheck() {
		ConstraintWrapper cw = new ConstraintWrapper(this);
		pointcut(
			"ALL",
			"ALL",
			"SETTERS(<" + RttiAC.CONSTRAINTS + ">)",
			cw,
			null);
	}

	/**
	 * Activates primary keys checking.
	 *
	 * <p>This method must be called once in the file integrity.acc to
	 * effectively add pointcuts for primary key checking methods.</p>
	 *
	 * @see org.objectweb.jac.core.rtti.RttiAC#definePrimaryKey(ClassItem,String,String[])
	 */
	void doPrimaryKeyCheck() {
		PrimaryKeyWrapper pw = new PrimaryKeyWrapper(this);
		pointcut(
			"ALL",
			"ALL",
			"ADDERS(<" + RttiAC.PRIMARY_KEY + ">)",
			pw,
			null);
	}

    boolean updateAssociations = false;
    public void updateAssociations() {
        this.updateAssociations = true;
    }

	public void whenConfigured() {
        if (!updateAssociations)
            return;
		if (hasConditions) {
			doCheck();
		}
		doPrimaryKeyCheck();
        Set classes = ((RttiAC)getAC("rtti")).getClassesWithAssociations();
        if (!classes.isEmpty()) {
            String classExpr = Strings.join(classes, " || ");
            pointcut(
                "ALL",
                classExpr,
                "ADDERS(<" + RttiAC.OPPOSITE_ROLE + ">) "
				+ "|| REMOVERS(<" + RttiAC.OPPOSITE_ROLE + ">) "
				+ "|| SETTERS(<" + RttiAC.OPPOSITE_ROLE	+ ">) "
				+ "|| CONSTRUCTORS",
                roleWrapper,
                null);
            //pointcut("ALL",classExpr,"CONSTRUCTORS",
		//         roleWrapper,null);
        }
    }

    /**
     * Tells wether adding objects to a repository collection is enabled or not.
     * @param collection the repository collection to test
     */
    public static boolean isAddToRepositoryEnabled(CollectionItem collection) {
        List disabled = getDisabled();
        loggerRep.debug("isAddToRepositoryEnabled "+collection+" (disabled="+disabled+") ?");
        return !disabled.contains(collection);
    }

    /**
     * Disables adding objects to a repository collection. 
     * @param collection the repository collection to disable
     */
    public static void disableAddToRepository(CollectionItem collection) {
        Collaboration collab = Collaboration.get();
        List disabled = getDisabled();
        loggerRep.debug("disableAddToRepository "+collection+
                        " (disabled="+disabled+")");
        disabled.add(collection);
    }

    /**
     * Enables adding objects to a repository collection. 
     * @param collection the repository collection to enable
     */
    public static void enableAddToRepository(CollectionItem collection) {
        loggerRep.debug("enableAddToRepository "+collection);
        List disabled = getDisabled();
        disabled.remove(collection);
    }

    /**
     * Gets the list of disabled repository
     */
    protected static List getDisabled() {
        List disabled = 
            (List)Collaboration.get().getAttribute(
                IntegrityAC.DISABLE_ADD_TO_REPOSITORY);
        if (disabled==null) {
            disabled = new Vector();
            Collaboration.get().addAttribute(
                IntegrityAC.DISABLE_ADD_TO_REPOSITORY, 
                disabled);
        }
        return disabled;
    }

    public static void disableRoleUpdates() {
        Collaboration.get().addAttribute(DISABLE_ROLE_UPDATES,Boolean.TRUE);
    }
    public static void enableRoleUpdates() {
        Collaboration.get().removeAttribute(DISABLE_ROLE_UPDATES);
    }
}
