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
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA */

package org.objectweb.jac.aspects.integrity;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.aopalliance.intercept.ConstructorInvocation;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.AspectComponent;
import org.objectweb.jac.core.Collaboration;
import org.objectweb.jac.core.Interaction;
import org.objectweb.jac.core.NameRepository;
import org.objectweb.jac.core.ObjectRepository;
import org.objectweb.jac.core.Wrapper;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.CollectionItem;
import org.objectweb.jac.core.rtti.FieldItem;

/**
 * This wrapper manages repository collections.
 */

public class RepositoryWrapper extends Wrapper {
    static final Logger logger = Logger.getLogger("integrity.repository");
	
	public static final int ADDER = 0;
	public static final int REMOVER = 1;
	
	Object repository;
	String repositoryName;
	CollectionItem collection;
	FieldItem field;
	int type = ADDER;

	public RepositoryWrapper(
		AspectComponent ac,
		String repositoryName,
		CollectionItem collection,
		FieldItem field, 
        int type) 
    {
		super(ac);
		this.repositoryName = repositoryName;
		this.collection = collection;
		this.field = field;
		this.type=type;
	}

	/**
	 * Adds the object added to field to the repository
	 */
	public Object addToRepository(Interaction interaction) {
		Object ret = doAddToRepository(interaction);
		if (IntegrityAC.isAddToRepositoryEnabled(collection)) {
			logger.debug("checking for repository on " + interaction.method);
			if (repository == null)
				repository = NameRepository.get().getObject(repositoryName);
			Object toAdd = interaction.args[0];

			if (toAdd != null
				&& repository != null
				&& !collection.getActualCollection(repository).contains(toAdd)) {
				logger.debug("addToRepository(" + repositoryName + ")");
				collection.addThroughAdder(repository, toAdd);
			}
		} else {
			logger.debug(collection + " disabled");
		}
		return ret;
	}

	/**
	 * Disables addToRepository
	 */
	public Object doAddToRepository(Interaction interaction) {
        IntegrityAC.disableAddToRepository(collection);
		logger.debug("doAddToRepository " + collection);
		try {
			return proceed(interaction);
		} finally {
            IntegrityAC.enableAddToRepository(collection);
		}
	}

	public Object removeFromRepository(Interaction interaction) {
		logger.debug("removeFromRepository(" + repositoryName + ")");
		Object toRemove = interaction.args[0];
		FieldItem target = collection != null ? collection : field;
		List constraints = (List) target.getAttribute(IntegrityAC.CONSTRAINTS);
		if (constraints == null)
			return proceed(interaction);
		Iterator it = constraints.iterator();
		while (it.hasNext()) {
			Constraint constraint = (Constraint) it.next();
			Collection objects =
				ObjectRepository.getObjectsWhere(
					constraint.relation.getClassItem(),
					constraint.relation,
					toRemove);
			logger.debug("   " + constraint + " => " + objects);
			switch (constraint.constraint) {
				case Constraint.DELETE_CASCADE :
					break;
				case Constraint.SET_NULL :
					Iterator it2 = objects.iterator();
					while (it2.hasNext()) {
						Object substance = it2.next();
						logger.debug(
							"      SET_NULL "
								+ substance + "." + constraint.relation);
						try {
							if (constraint.relation
								instanceof CollectionItem) {
								((CollectionItem) constraint.relation)
                                    .removeThroughRemover(
                                        substance,
                                        toRemove);
							} else {
								constraint.relation.setThroughWriter(
									substance,
									null);
							}
						} catch (Exception e) {
							logger.error(
								"Failed to enforce SET_NULL constraint for "
                                + constraint.relation + " on " + substance,e);
						}
					}
					break;
				case Constraint.FORBIDDEN :
					if (objects.size() > 0)
						throw new RuntimeException(
							"Cannot delete "
								+ toRemove + " from " + target.getLongName()
								+ " because of constraint " + constraint);
					break;
				default :
					logger.warn(
						"Unknown constraint type " + constraint.constraint);
			}
		}

		return proceed(interaction);
	}

	public Object invoke(MethodInvocation invocation) throws Throwable {
		Interaction interaction = (Interaction) invocation;
		switch (type) {
            case ADDER:
                return addToRepository(interaction);		
            case REMOVER:
                return removeFromRepository(interaction);
            default:
                throw new Exception("Unknown RepositoryWrapper type: "+type);
		}
	}
}

