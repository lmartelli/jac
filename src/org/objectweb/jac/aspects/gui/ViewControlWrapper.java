/*
  Copyright (C) 2002-2003 Renaud Pawlak <renaud@aopsys.com>, 
                          Laurent Martelli <laurent@aopsys.com>
  
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

package org.objectweb.jac.aspects.gui;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import org.aopalliance.intercept.ConstructorInvocation;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.AspectComponent;
import org.objectweb.jac.core.Interaction;
import org.objectweb.jac.core.Wrappee;
import org.objectweb.jac.core.Wrapper;
import org.objectweb.jac.core.Wrapping;
import org.objectweb.jac.core.rtti.CollectionItem;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.MemberItem;
import org.objectweb.jac.core.rtti.MethodItem;
import org.objectweb.jac.util.Strings;

/**
 * This wrapper updates the views of a given object when its state
 * changes, that is to say when a write method is called on the
 * wrappee.<p>
 *
 * A view controller can control several views of the same wrappee at
 * the same time.<p>
 *
 * This mecanism is similar to the famous MVC design pattern. The
 * model is the wrappee and the controller is the wrapper.<p>
 *
 * @see View
 * @see #registerObject(Wrappee,ObjectUpdate,Object)
 * @see #registerField(Wrappee,FieldItem,FieldUpdate,Object)
 * @see #registerCollection(Wrappee,CollectionItem,CollectionUpdate,Object)
 * @see #unregisterObject(Wrappee,ObjectUpdate)
 * @see #unregisterField(Wrappee,FieldItem,FieldUpdate)
 * @see #unregisterCollection(Wrappee,CollectionItem,CollectionUpdate)
 */

public class ViewControlWrapper extends Wrapper {
    static Logger logger = Logger.getLogger("gui.viewcontrol");
    static Logger loggerReg = Logger.getLogger("gui.register");

	// FieldItem -> (FieldUpdate -> param)  (clients to notify for each field)
	WeakHashMap fieldClients = new WeakHashMap();
	// MethodItem -> (MethodUpdate -> param)  (clients to notify for each method)
	WeakHashMap methodClients = new WeakHashMap();
	// FieldItem -> (CollectionUpdate -> param)  (clients to notify for each collection)
	WeakHashMap collectionClients = new WeakHashMap();
	// ObjectUpdate -> param (clients to notify when the wrappee's state is modified)
	WeakHashMap objectClients = new WeakHashMap();

	// FieldItem -> (FieldUpdate -> param)  (clients to notify for each field)
	//Hashtable allFieldClients = new Hashtable();
	// MethodItem -> (MethodUpdate -> param)  (clients to notify for each method)
	static Hashtable allMethodClients = new Hashtable();
	// FieldItem -> (CollectionUpdate -> param)  (clients to notify for each collection)
	//Hashtable allCollectionClients = new Hashtable();
	// ObjectUpdate -> param (clients to notify when the wrappee's state is modified)
	//HashMap allObjectClients = new HashMap();

	/**
	 * Creates an empty  view control wrapper.<p>
	 */
	public ViewControlWrapper(AspectComponent ac) {
		super(ac);
	}

	/**
	 * Register for a fieldUpdated event.<p>
	 *
	 * @param wrappee
	 * @param field the field whose updates must be notified
	 * @param client the object to notify when the field is updated
	 * @param param
	 * @see #unregisterField(Wrappee,FieldItem,FieldUpdate) 
	 */
	public void registerField(
		Wrappee wrappee,
		FieldItem field,
		FieldUpdate client,
		Object param) {
		Map clients = (Map) fieldClients.get(field.getName());
		if (clients == null) {
			clients = new HashMap();
			fieldClients.put(field.getName(), clients);
		}
		clients.put(client, param);
		loggerReg.debug(
            "registerField(" + Strings.hex(client)
            + ") on " + Strings.hex(wrappee) + "." + field.getName());
	}

	/**
	 * Unregister from a fieldUpdated event.<p>
	 *
	 * @param field the field whose updates must not be notified anymore
	 * @param client the object not to notify anymore
	 * @see #registerField(Wrappee,FieldItem,FieldUpdate,Object) 
	 */
	public void unregisterField(
		Wrappee wrappee,
		FieldItem field,
		FieldUpdate client) {
		Map clients = (Map) fieldClients.get(field.getName());
		if (clients != null) {
			loggerReg.debug("unregisterField("
					+ Strings.hex(client)
					+ ") on "
					+ Strings.hex(wrappee)
					+ "."
					+ field.getName());
			clients.remove(client);
		}
	}

	/**
	 * Register for a collectionUpdated event.<p>
	 *
	 * @param collection the collection whose updates must be notified
	 * @param client the object to notify when the field is updated
	 * @see #unregisterCollection(Wrappee,CollectionItem,CollectionUpdate) 
	 */
	public void registerCollection(
		Wrappee wrappee,
		CollectionItem collection,
		CollectionUpdate client,
		Object param) {
		Map clients = (Map) collectionClients.get(collection.getName());
		if (clients == null) {
			clients = new HashMap();
			collectionClients.put(collection.getName(), clients);
		}
		clients.put(client, param);
		loggerReg.debug("registerCollection("
				+ Strings.hex(client)
				+ ") on "
				+ Strings.hex(wrappee)
				+ "."
				+ collection.getName());
	}

	/**
	 * Unregister from a collectionUpdated event.<p>
	 *
	 * @param collection the collection whose updates must not be notified anymore
	 * @param client the object not to notify anymore
	 * @see #registerCollection(Wrappee,CollectionItem,CollectionUpdate,Object) 
	 */

	public void unregisterCollection(
		Wrappee wrappee,
		CollectionItem collection,
		CollectionUpdate client) {
		Map clients = (Map) collectionClients.get(collection.getName());
		if (clients != null) {
			loggerReg.debug("unregisterCollection("
					+ Strings.hex(client)
					+ ") on "
					+ Strings.hex(wrappee)
					+ "."
					+ collection.getName());
			clients.remove(client);
		}
	}

	/**
	 * Register for a fieldUpdated event.<p>
	 *
	 * @param wrappee
	 * @param method the method whose updates must be notified
	 * @param client the object to notify when the field is updated
	 * @param param
	 * @see #unregisterField(Wrappee,FieldItem,FieldUpdate) 
	 */
	public void registerMethod(
		Wrappee wrappee,
		MethodItem method,
		MethodUpdate client,
		Object param) {
		String key = method.getFullName();
		Map clients = (Map) methodClients.get(key);
		if (clients == null) {
			clients = new HashMap();
			methodClients.put(key, clients);
		}
		clients.put(client, param);

		clients = (Map) allMethodClients.get(key);
		if (clients == null) {
			clients = new HashMap();
			allMethodClients.put(key, clients);
		}
		clients.put(client, param);

		loggerReg.debug("registerMethod("
				+ Strings.hex(client)
				+ ") on "
				+ Strings.hex(wrappee)
				+ "."
				+ key);
	}

	public void unregisterMethod(
		Wrappee wrappee,
		MethodItem method,
		MethodUpdate client) {
		String key = method.getFullName();
		Map clients = (Map) methodClients.get(key);
		if (clients != null) {
			loggerReg.debug("unregisterMethod("
					+ Strings.hex(client)
					+ ") on "
					+ Strings.hex(wrappee)
					+ "."
					+ method.getName());
			clients.remove(client);
		}
		clients = (Map) allMethodClients.get(key);
		if (clients != null) {
			clients.remove(client);
		}
	}

	/**
	 * Register for an objectUpdated event. 
	 *
	 * @param client whom to notify when the wrappee is updated
	 * @param param an object that will be passed back to client on
	 * each notification event.
	 */
	public void registerObject(
		Wrappee wrappee,
		ObjectUpdate client,
		Object param) {
		loggerReg.debug(
            "registerObject " + Strings.hex(client) +
            " on " + Strings.hex(wrappee));
		objectClients.put(client, param);
	}

	/**
	 * Unregister from an objectUpdated event. 
	 *
	 * @param client whom not to notify anymore
	 */
	public void unregisterObject(Wrappee wrappee, ObjectUpdate client) {
		loggerReg.debug(
            "unregisterObject(" + Strings.hex(client)
            + ") on " + Strings.hex(wrappee));
		objectClients.remove(client);
	}

	/**
	 * Unregister a client from all update events
	 * @param wrappee the object to unregister from
	 * @param client the client to unregister
	 */
	public void unregister(Wrappee wrappee, Object client) {
		loggerReg.debug("unregister(" + Strings.hex(client)
                        + ") on " + Strings.hex(wrappee));
		objectClients.remove(client);
		Iterator i;
		i = fieldClients.values().iterator();
		while (i.hasNext()) {
			Map clients = (Map) i.next();
			clients.remove(client);
		}

		i = collectionClients.values().iterator();
		while (i.hasNext()) {
			Map clients = (Map) i.next();
			clients.remove(client);
		}
	}

	/**
	 * Get the views controlled by this wrapper.<p>
	 *
	 * @return the set of controlled views
	 */

	/*
	public Vector getViews() {
	   return controlledViews;
	}
	*/

	/**
	 * A wrapping method that updates the views of the objects.<p>
	 *
	 * It uses the RTTI aspect to know the fields and the collections
	 * that are written, added, or removed by the currently wrapped
	 * method. Then it upcall the <code>refreshStateComponent</code> of
	 * all the controlled views to refresh the implied GUI
	 * components.<p>
	 *
	 * @see org.objectweb.jac.core.rtti
	 * @see ObjectUpdate
	 * @see FieldUpdate
	 * @see CollectionUpdate
	 */

	public Object updateView(Interaction interaction) {

		Object ret = proceed(interaction);

		logger.debug(this
				+ " checking view updating for method "
				+ Strings.hex(interaction.wrappee)
				+ "."
				+ interaction.method.getName());

		MethodItem method = (MethodItem) interaction.method;

		/*
		JTextArea console = (JTextArea) method.getAttribute("Gui.loggingMethod"); 
		if( console != null ) {
		   console.append( (String)arg(0) );
		   console.setCaretPosition( console.getText().length() );
		}
		*/

		// notify registered clients for fieldUpdated
		FieldItem[] writtenFields = method.getWrittenFields();
		if (writtenFields != null) {
			Class cl = interaction.getActualClass();
			for (int i = 0; i < writtenFields.length; i++) {
				if (writtenFields[i].getGetter() == interaction.method) {
					logger.warn(
						"Skipping "	+ interaction.method
							+ " since it's the getter of " + writtenFields[i]);
					continue;
				}
				logger.debug(method.getClassItem() + "."	+ method.getFullName()
						+ " writes " + writtenFields[i].getLongName());
                onFieldWrite(interaction.wrappee,cl,writtenFields[i]);

			}
		}

		// notify registered clients for collectionUpdated
		CollectionItem[] addedCollections = method.getAddedCollections();
		CollectionItem[] removedCollections = method.getRemovedCollections();
		CollectionItem[] modifiedCollections = method.getModifiedCollections();
		HashSet clientCollections = new HashSet();
		if (addedCollections != null) {
			clientCollections.addAll(Arrays.asList(addedCollections));
		}
		if (removedCollections != null) {
			clientCollections.addAll(Arrays.asList(removedCollections));
		}
		if (modifiedCollections != null) {
			clientCollections.addAll(Arrays.asList(modifiedCollections));
		}
		Iterator i = clientCollections.iterator();
		while (i.hasNext()) {
			CollectionItem collection = (CollectionItem) i.next();
			HashMap clients =
				(HashMap) collectionClients.get(collection.getName());
			if (clients != null) {
				Iterator it = ((Map) clients.clone()).keySet().iterator();
				Object value =
					collection.getThroughAccessor(interaction.wrappee);
				while (it.hasNext()) {
					CollectionUpdate client = (CollectionUpdate) it.next();
					logger.debug("collectionUpdated("
							+ collection.getLongName()
							+ ") on "
							+ Strings.hex(client));
					try {
						if (method.isAdder())
							client.onAdd(
								interaction.wrappee,
								collection,
								value,
								interaction.args[0],
								clients.get(client));
						else if (method.isRemover())
							client.onRemove(
								interaction.wrappee,
								collection,
								value,
								interaction.args[0],
								clients.get(client));
						else
							client.onChange(
								interaction.wrappee,
								collection,
								value,
								clients.get(client));
					} catch (Exception e) {
						logger.error(
							"Caught exception in collectionUpdated("
								+ collection.getLongName()
								+ ") on "
								+ Strings.hex(client), 
                            e);
					}
				}
			}
			notifyDependentCollections(
				collection,
				interaction.wrappee,
				method,
				interaction.args);
		}

		// notify registered clients for objectUpdated
		if (method.isModifier()) {
			logger.debug(method + " is a modifier");
            onObjectModified(interaction.wrappee);
		}

		return ret;
	}

    public void onObjectModified(Object substance) {
        Iterator it = (new HashMap(objectClients)).keySet().iterator();
        while (it.hasNext()) {
            ObjectUpdate client = (ObjectUpdate) it.next();
            logger.debug("objectUpdated("
                + Strings.hex(substance)
                + ") on "
                + Strings.hex(client));
            try {
                client.objectUpdated(
                    substance,
                    objectClients.get(client));
            } catch (Exception e) {
                logger.error(
                    "Caught exception in objectUpdated("
                    + Strings.hex(substance)
                    + ") on "
                    + Strings.hex(client), 
                    e);
            }
        }
    }

    /**
     * @param substance the object whose field is written
     * @param cl the class of substance
     * @param writtenField the field which is written
     */
    public void onFieldWrite(Object substance, Class cl, FieldItem writtenField) {
        logger.debug("onFieldWrite "+substance+"."+writtenField);

        HashMap clients =
            (HashMap) fieldClients.get(writtenField.getName());
        if (clients != null) {
            Iterator it = ((Map) clients.clone()).entrySet().iterator();
            Object value =
                writtenField.getThroughAccessor(substance);
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                FieldUpdate client = (FieldUpdate) entry.getKey();
                logger.debug("  fieldUpdated("
                    + writtenField.getLongName()
                    + ") on "
                    + Strings.hex(client));
                try {
                    client.fieldUpdated(
                        substance,
                        writtenField,
                        value,
                        entry.getValue());
                } catch (Exception e) {
                    logger.error(
                        "Caught exception in fieldUpdated("
                        + writtenField.getLongName()
                        + ") on "
                        + Strings.hex(client), e);
                }
            }
        }

        // Notify dependent fields
        FieldItem[] dependentFields =
            writtenField.getDependentFields();
        for (int j = 0; j < dependentFields.length; j++) {
            if (!dependentFields[j]
                .getClassItem()
                .getActualClass()
                .isAssignableFrom(cl)) {
                logger.debug("  Skipping dependentField "
                    + dependentFields[j]
                    + " (class=" + cl.getName()	+ ")");
                continue;
            }

            logger.debug("  dependent field " + dependentFields[j].getLongName());
            List depSubstances = (List)dependentFields[j].getSubstances(substance);
            Iterator sit = depSubstances.iterator();
            while(sit.hasNext()) {
                Wrappee depSubstance = (Wrappee)sit.next();
                logger.debug("  iterating on " + depSubstance);
                if (depSubstance!=null && depSubstance!=substance) {
                    Wrapping.invokeRoleMethod(
                        depSubstance,
                        ViewControlWrapper.class,
                        "onFieldWrite",
                        new Object[] {depSubstance,depSubstance.getClass(),dependentFields[j].getField()}
                    );
                    Wrapping.invokeRoleMethod(
                        depSubstance,
                        ViewControlWrapper.class,
                        "onObjectModified",
                        new Object[] {depSubstance}
                    );
                }
            }
            clients =
                (HashMap) fieldClients.get(
                    dependentFields[j].getName());
            if (clients != null) {
                Iterator it =
                    ((Map) clients.clone()).entrySet().iterator();
                Object value =
                    dependentFields[j].getThroughAccessor(
                        substance);
                while (it.hasNext()) {
                    Map.Entry entry = (Map.Entry) it.next();
                    FieldUpdate client = (FieldUpdate) entry.getKey();
                    logger.debug("  fieldUpdated("
                        + dependentFields[j].getLongName()
                        + ") on " + Strings.hex(client));
                    try {
                        client.fieldUpdated(
                            substance,
                            dependentFields[j],
                            value,
                            entry.getValue());
                    } catch (Exception e) {
                        logger.error(
                            "Caught exception in fieldUpdated("
                            + dependentFields[j].getLongName()
                            + ") on " + Strings.hex(client),
                            e);
                    }
                }
            }
        }

        notifyDependentMethods(
            methodClients,
            writtenField,
            substance,
            cl,
            new HashSet());

    }


	/**
	 * @param methodClients the clients to notify
	 * @param member member item which caused the notification
	 * @param wrappee object which caused the notification
	 * @param cl class of wrappee or null
	 * @param alreadyNotified already notified clients, so that we can
	 * avoid infinite loops and notifying a client several times
	 */
	void notifyDependentMethods(
		Map methodClients,
		MemberItem member,
		Object wrappee,
		Class cl,
		Set alreadyNotified) {
		logger.debug("notifyDependentMethods for " + member + " / " + wrappee);
		// Notify dependent methods
		MethodItem[] dependentMethods = member.getDependentMethods();
		for (int i = 0; i < dependentMethods.length; i++) {
			// the class of the dependent method
			Class depClass =
				dependentMethods[i].getClassItem().getActualClass();
			if ((cl != null && !depClass.isAssignableFrom(cl))
				|| alreadyNotified.contains(dependentMethods[i])) {
				logger.debug("    skipping " + dependentMethods[i].getLongName());
				continue;
			}
			logger.debug("dependent method "
					+ dependentMethods[i].getClassItem()
					+ "."
					+ dependentMethods[i].getFullName());
			alreadyNotified.add(dependentMethods[i]);
			HashMap clients =
				(HashMap) methodClients.get(dependentMethods[i].getFullName());
			if (clients != null) {
				Iterator it = ((Map) clients.clone()).entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry entry = (Map.Entry) it.next();
					MethodUpdate client = (MethodUpdate) entry.getKey();
					logger.debug("MethodUpdated("
							+ dependentMethods[i].getLongName()
							+ ") on "
							+ Strings.hex(client));
					try {
						client.methodUpdated(
							wrappee,
							dependentMethods[i],
							entry.getValue());
					} catch (Exception e) {
						logger.error(
							"Caught exception in methodUpdated("
								+ dependentMethods[i].getLongName()
								+ ") on " + Strings.hex(client),
                            e);
					}
				}
			}
			if (member != dependentMethods[i])
				notifyDependentMethods(
					allMethodClients,
					dependentMethods[i],
					wrappee,
					null,
					alreadyNotified);
		}
	}

	void notifyDependentCollections(
		CollectionItem collection,
		Wrappee wrappee,
		MethodItem method,
		Object[] args) {
		// Notify dependent fields
		FieldItem[] dependentFields = collection.getDependentFields();
		for (int j = 0; j < dependentFields.length; j++) {
			logger.debug("dependent field "
					+ dependentFields[j].getName()
					+ " for "
					+ collection.getName());
			HashMap clients =
				(HashMap) collectionClients.get(dependentFields[j].getName());
			if (clients != null) {
				Iterator it2 = ((Map) clients.clone()).entrySet().iterator();
				Object value = dependentFields[j].getThroughAccessor(wrappee);
				while (it2.hasNext()) {
					Map.Entry entry = (Map.Entry) it2.next();
					CollectionUpdate client = (CollectionUpdate) entry.getKey();
					logger.debug(
                        "collectionUpdated(" + dependentFields[j].getLongName()
                        + ") on " + Strings.hex(client));
					try {
						client.onChange(
							wrappee,
							(CollectionItem) dependentFields[j],
							value,
							entry.getValue());
					} catch (Exception e) {
						logger.error(
							"Caught exception in collectionUpdated("
								+ dependentFields[j].getLongName()
								+ ") on " + Strings.hex(client),
                            e);
					}
				}
			}
		}
	}

	public Object invoke(MethodInvocation invocation) throws Throwable {
		return updateView((Interaction) invocation);
	}

	public Object construct(ConstructorInvocation invocation)
		throws Throwable {
		throw new Exception("This wrapper does not support constructor wrapping");
	}

}
