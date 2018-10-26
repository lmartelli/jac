/*
  Copyright (C) 2001-2004 Laurent Martelli <laurent@aopsys.com>

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
import org.objectweb.jac.core.Collaboration;
import org.objectweb.jac.core.Interaction;
import org.objectweb.jac.core.Wrapper;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.CollectionItem;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.MethodItem;
import org.objectweb.jac.core.rtti.RttiAC;

/**
 * Wrapper for updating opposite roles.
 */

public class RoleWrapper extends Wrapper {
    static final Logger logger = Logger.getLogger("integrity");

	public RoleWrapper(AspectComponent ac) {
		super(ac);
	}

	/**
	 * Tells the integrity aspect to update the opposite roles of the
	 * declared associations.
	 */
	public Object updateOppositeRole(Interaction interaction) {
		logger.debug("updateOppositeRoles " + interaction.method + "...");
		if (attr("rtti.updateOpposite") != null || 
            attr(IntegrityAC.DISABLE_ROLE_UPDATES)!=null)
			return interaction.proceed();

		doUpdate(interaction);

		Object result = interaction.proceed();
		return result;
	}

	protected void doUpdate(Interaction interaction) {
		logger.debug(
			"updateOppositeRoles "+interaction.wrappee+"."+
            interaction.method+"("+interaction.args[0]+")");
		ClassItem cli = interaction.getClassItem();
		MethodItem method = (MethodItem) interaction.method;

		// References
		FieldItem[] fields = method.getWrittenFields();
		FieldItem opposite;
		Object newValue = interaction.args[0];
		for (int i = 0; fields != null && i < fields.length; i++) {
			FieldItem field = fields[i];
			Object currentValue = field.getThroughAccessor(interaction.wrappee);
			opposite = (FieldItem) field.getAttribute(RttiAC.OPPOSITE_ROLE);
			if (opposite != null) {
				try {
					attrdef("rtti.updateOpposite", opposite);
					logger.debug(
						"UpdateOppositeRole in "+interaction.method+" : "+
                        fields[i].getName()+" -> "+opposite.getName());
					logger.debug(
						"wrappee = "+interaction.wrappee+" ;  "+
                        "currentValue = "+currentValue);
					if (opposite instanceof CollectionItem) {
						CollectionItem oppositeCollection =
							(CollectionItem) opposite;
						if (currentValue != null)
							oppositeCollection.removeThroughRemover(
								currentValue,
								interaction.wrappee);
						if (newValue != null
							&& !oppositeCollection.contains(
								newValue,
								interaction.wrappee))
							oppositeCollection.addThroughAdder(
								newValue,
								interaction.wrappee);
					} else {
						if (currentValue != null) {
							opposite.setThroughWriter(currentValue, null);
						}
                        if (newValue!=null)
                            opposite.setThroughWriter(
                                newValue,
                                interaction.wrappee);
					}
				} catch (Exception e) {
					logger.error(
						"Failed to update opposite role "+opposite.getLongName()+" of "+
                        field.getLongName()+" on "+interaction.wrappee,e);
				} finally {
					attrdef("rtti.updateOpposite", null);
				}
			}
		}

		// Adders
		CollectionItem[] collections = method.getAddedCollections();
		Object addedValue = interaction.args[0];
		for (int i = 0; collections != null && i < collections.length; i++) {
			CollectionItem collection = collections[i];
			opposite =
				(FieldItem) collection.getAttribute(RttiAC.OPPOSITE_ROLE);
			if (opposite == null) {
				logger.debug(
					collection.getParent().getName()+"."+collection.toString()+
                    " has no opposite role");
				continue;
			}
			logger.debug(
				"UpdateOppositeRole #"+i+" in "+interaction.method+" : "+
                collection.getName()+" -> "+opposite.getName());
			try {
				attrdef("rtti.updateOpposite", opposite);
				if (opposite instanceof CollectionItem) {
					CollectionItem oppositeCollection =
						(CollectionItem) opposite;
					if (addedValue != null
						&& !oppositeCollection.contains(
							addedValue,
							interaction.wrappee))
						oppositeCollection.addThroughAdder(
							addedValue,
							interaction.wrappee);
				} else {
					if (addedValue != null) {
						Object currentValue = opposite.get(addedValue);
                        logger.debug("  currentValue="+currentValue+
                                     ", interaction.wrappee="+interaction.wrappee);
						if (currentValue != null && 
                            currentValue != interaction.wrappee) {
							collection.removeThroughRemover(
								currentValue,
								addedValue);
						}
						opposite.setThroughWriter(
							addedValue,
							interaction.wrappee);
					}
				}
			} catch (Exception e) {
				logger.error(
					"Failed to update opposite role "+opposite.getLongName()+" of "+
                    collection.getLongName()+" on "+interaction.wrappee,e);
			} finally {
				attrdef("rtti.updateOpposite", null);
			}
		}

		// Removers
		collections = method.getRemovedCollections();
		Object removedValue = interaction.args[0];
		for (int i = 0; collections != null && i < collections.length; i++) {
			CollectionItem collection = collections[i];
			logger.debug("removed collection " + collection);
			opposite =
				(FieldItem) collection.getAttribute(RttiAC.OPPOSITE_ROLE);
			try {
				attrdef("rtti.updateOpposite", opposite);
				if (opposite == null) {
					logger.debug(
						collection.getParent().getName()+"."+collection.toString()+
                        " has no opposite role");
					continue;
				}
				logger.debug(
					"UpdateOppositeRole in "+interaction.method+" : "+collection.getName()+
                    " -> "+opposite.getName());
				if (removedValue != null) {
					if (opposite instanceof CollectionItem) {
						((CollectionItem) opposite).removeThroughRemover(
							removedValue,
							interaction.wrappee);
					} else {
						opposite.setThroughWriter(removedValue, null);
					}
				}
			} catch (Exception e) {
				logger.error("Failed to update opposite role "+opposite.getLongName()+" of "
                             +collection.getLongName()+" on "+interaction.wrappee,e);
			} finally {
				attrdef("rtti.updateOpposite", null);
			}

		}
	}

	/**
	 * Initializes the opposite role of an object auto created on an
	 * adder or reference setter if there's an attribute
	 * GuiAC.AUTOCREATE_REASON in the context, whose value is an
	 * Interaction.  */
	public Object initAutoCreatedObject(Interaction interaction) {
		Object result = interaction.proceed();
		Collaboration collab = Collaboration.get();
		Interaction reason =
			(Interaction) collab.getAttribute("GuiAC.AUTOCREATE_REASON");
		if (reason != null) {
			logger.debug(
				"initAutoCreatedObject for "
					+ interaction + " because of " + reason);
			reason.args[0] = interaction.wrappee;
			// We better unset the attribute now because doUpdate() may
			// create objects (under weird circumtances) and we do not
			// want to apply the initialization on them
			collab.removeAttribute("GuiAC.AUTOCREATE_REASON");
			doUpdate(reason);
		}
		return result;
	}

	/**
	 * Disable opposite role updating
	 * @param role role for which opposite role updating must be disabled
	 */
	public static void disableRoleUpdate(FieldItem role) {
		Collaboration.get().addAttribute("rtti.updateOpposite", role);
	}

	public static void enableRoleUpdate(FieldItem role) {
		Collaboration.get().removeAttribute("rtti.updateOpposite");
	}

	public Object invoke(MethodInvocation invocation) throws Throwable {
		return this.updateOppositeRole((Interaction) invocation);
	}

	public Object construct(ConstructorInvocation invocation)
		throws Throwable 
    {
		return this.initAutoCreatedObject((Interaction) invocation);
	}
}
