/*
  Copyright (C) 2002-2003 Laurent Martelli <laurent@aopsys.com>

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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import org.aopalliance.intercept.ConstructorInvocation;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.AspectComponent;
import org.objectweb.jac.core.Interaction;
import org.objectweb.jac.core.NameRepository;
import org.objectweb.jac.core.Wrappee;
import org.objectweb.jac.core.Wrapping;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.CollectionItem;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.MethodItem;
import org.objectweb.jac.util.ExtArrays;

/**
 * This wrapper defines persistence extensions for objects that a
 * defined persitent by a persistent aspect component. */

public class PersistenceWrapper extends AbstractPersistenceWrapper {
    static final Logger loggerRef = Logger.getLogger("persistence.ref");
    static final Logger loggerCol = Logger.getLogger("persistence.col");
    static final Logger loggerWrap = Logger.getLogger("persistence.wrap");

    public final static String[] toWrapForLists =
    new String[] {
        "add",
        "addAll",
        "clear",
        "contains",
        "get",
        "remove",
        "removeRange",
        "set",
        "size",
        "indexOf",
        "lastIndexOf",
        "isEmpty",
        // -- preloads --
        "toArray", "iterator", "clone" /*"equals"*/
    };

    public final static String[] toWrapForSets =
    new String[] { "add", "clear", "contains", "remove", "size", "isEmpty",
                   // -- preloads --   
                   "toArray", "iterator", "clone" /*"equals"*/
    };

    public final static String[] toWrapForMaps =
    new String[] {
        "clear",
        "isEmpty",
        "size",
        "containsKey",
        "containsValue",
        "get",
        "put",
        "remove",
        // -- preloads --       
        "keySet", "entrySet", "values", "clone" /*"equals"*/
    };

    public PersistenceWrapper(AspectComponent ac) {
        super(ac);
    }

    boolean doStatics = false;

    public PersistenceWrapper(AspectComponent ac, Boolean doStatics) {
        super(ac);
        this.doStatics = doStatics.booleanValue();
    }

    //public static final String ATTR_ADDED = "persistence.added";

    // names of already loaded collections
    private HashSet loadedVectors = new HashSet();
    // names of already loaded references
    private HashSet loadedReferences = new HashSet();
    // oids of not yet loaded references (FieldItem -> oid)
    private HashMap notloadedReferences = new HashMap();

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
     * This wrapping method is called on static objects constructors as
     * defined by the pointcut of <code>PersistenceAC</code>.
     *
     * @see PersistenceAC#registerStatics 
     */
    public Object handleStatic(Interaction interaction) {
        logger.debug("handle static "+interaction.wrappee.getClass().getName());
        Object ret = interaction.proceed();
        //wrap(null,true);
        try {
            String name = NameRepository.get().getName(interaction.wrappee);
            ClassItem cli = cr.getClass(interaction.wrappee);
            Storage storage = getStorage(cli);
            OID oid = storage.getOIDFromName(name);
            if (oid == null) {
                oid = storage.createObject(cli.getName());
                setOID(interaction.wrappee, oid);
                storage.bindOIDToName(oid, name);
                logger.debug(interaction.wrappee.getClass().getName() + " isNew");
                CollectionItem[] array = new CollectionItem[0];
                wrapCollections(interaction.wrappee, oid, true);
                initAllFields(interaction.wrappee, oid);
            } else {
                setOID(interaction.wrappee, oid);
                // Call loadAllFields before wrapCollections so that it
                // preloads the OID of collections
                loadAllFields(interaction.wrappee, oid);
                wrapCollections(interaction.wrappee, oid, false);
            }
        } catch (Exception e) {
            logger.error("handleStatic "+interaction,e);
        }
        return ret;
    }

    /**  
     * Makes an object persistent if it is not already. Is is assigned
     * an OID, and its members are saved in the storage.
     * @param wrappee the object to make persistent 
     */
    public OID makePersistent(Wrappee wrappee) throws Exception {
        OID oid = null;
            
        logger.debug("makePersistent(" + wrappee + ")");
        oid = getOID(wrappee);
        if (oid!=null) {
            logger.debug(wrappee + " is already persistent");
        } else {
            ClassItem cli = cr.getClass(wrappee);
            Storage storage = getStorage(cli);
            oid = storage.createObject(cli.getName());
            setOID(wrappee, oid);
            storage.bindOIDToName(
                oid,
                NameRepository.get().getName(wrappee));
            logger.debug(wrappee + " isNew");

            // Initialize loadedVectors and loadedReferences.
            // It's important to do this as early as possible, in case
            // someone accesses them.

            FieldItem[] refs = cli.getReferences();
            for (int i = 0; i < refs.length; i++) {
                loadedReferences.add(refs[i].getName());
            }
            CollectionItem[] colls = cli.getCollections();
            for (int i = 0; i < colls.length; i++) {
                loadedVectors.add(colls[i].getName());
            }

            wrapCollections(wrappee, oid, true);
            initAllFields(wrappee, oid);
        }

        return oid;
    }

    /**
     * Load all the fields of an object from the storage and initialize
     * the object.
     *
     * @param wrappee the object
     */

    public void loadAllFields(Wrappee wrappee, OID oid) throws Exception {
        logger.debug("loadAllFields(" + oid + ")");
        String lClassID = wrappee.getClass().getName();

        // Load all fields from the storage
        ClassItem cli = cr.getClass(lClassID);

        // this should be faster because only one call to the storage is done
        FieldItem[] fields = cli.getFields();
        if (fields.length > 0) {
            Storage storage = oid.getStorage();
            StorageField[] values = storage.getFields(oid, cli, fields);
            for (int i = 0; i < values.length; i++) {
                if (values[i] != null) {
                    FieldItem field = values[i].fieldID;
                    if (field.getActualField() != null) {
                        try {
                            boolean force =
                                PersistenceAC.isFieldPreloaded(field);
                            if (field.isPrimitive()
                                && values[i].value != null) {
                                // TODO: preload for primitive fields here
                                if (field.setConvert(wrappee, values[i].value)) {
                                    logger.warn(oid+"."+field.getName()+
                                                " value converted from "+values[i].value.getClass()+
                                                " to "+field.getType());
                                    storage.updateField(oid, field, field.get(wrappee));
                                }
                            } else if (field.isReference()) {
                                if (!field.hasAccessingMethods()) {
                                    // load a reference if it has no getter
                                    getReference(wrappee, oid, field);
                                } else {
                                    // test if reference must be loaded here
                                    // instead of being loaded when accessed
                                    if (force) {
                                        getReference(wrappee, oid, field);
                                    } else {
                                        logger.debug("storing OID for reference "
                                                     + field
                                                     + "="
                                                     + values[i].value);
                                        notloadedReferences.put(
                                            field,
                                            values[i].value);
                                    }
                                }
                            } else if (field instanceof CollectionItem) {
                                if (!((CollectionItem) field)
                                    .isWrappable(wrappee)) {
                                    logger.debug("loadAllFields -> invoking getCollection for "
                                                 + field);
                                    Wrapping.invokeRoleMethod(
                                        wrappee,
                                        "getCollection",
                                        new Object[] { field });
                                } else {
                                    // test if collection must be loaded here
                                    // instead of being loaded when accessed
                                    if (force) {
                                        getCollection(
                                            wrappee, oid,
                                            (CollectionItem) field);
                                    } else {
                                        logger.debug("storing OID for collection "
                                                     + field
                                                     + "="
                                                     + values[i].value);
                                        notloadedReferences.put(
                                            field,
                                            values[i].value);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            logger.error(
                                "Failed to load field "+ field.getLongName()
                                + " for " + oid,
                                e);
                        }
                    }
                }
            }
        }
    }

    /**
     * This role method wraps all the collection of the current wrappee
     * so that they will trigger the needed loads from the storage when
     * they are accessed.
     *
     * @param wrappee the object whose collections to wrap
     * @param oid the oid of wrappee
     * @param isNew must true if the object is newly created, false if
     * loaded from storage 
     */
    public void wrapCollections(Wrappee wrappee, OID oid, boolean isNew) {
        logger.debug("wrapCollections("
                     + wrappee.getClass().getName()
                     + "), isNew=" + isNew);
        // Wrap collections
        try {
            ClassItem cli = cr.getClass(wrappee);
            Vector collectionsToInit = new Vector();
            CollectionItem[] collections = cli.getCollections();
            Storage storage = oid.getStorage();
            for (int i = 0; i < collections.length; i++) {
                CollectionItem collection = collections[i];
                if (collection.isTransient())
                    continue;
                Object coll_value = collections[i].get(wrappee);
                Class collType = collections[i].getType();
                if (coll_value == null) {
                    logger.warn(
                        "uninitialized collection " +collections[i].getLongName()+
                        ". Do you have a constructor with no parameters?");
                    // uninitialized field
                    if (collType.isArray()) {
                        coll_value =
                            java.lang.reflect.Array.newInstance(
                                collType.getComponentType(),
                                0);
                    } else {
                        try {
                            // This will fail if the declared type of the
                            // attribute is an interface
                            if (!collType.isInterface()) {
                                coll_value = collType.newInstance();
                            }
                        } catch (Exception e) {
                            logger.error("wrapCollections "+wrappee+"("+oid+")",e);
                        }
                    }
                    collections[i].set(wrappee, coll_value);
                }
                Class actualCollType = coll_value.getClass();
                if (//isWrapped(collections[i]) || 
                actualCollType
                    == org.objectweb.jac.lib.java.util.Vector.class
                    || actualCollType
                        == org.objectweb.jac.lib.java.util.HashMap.class
                    || actualCollType
                        == org.objectweb.jac.lib.java.util.Hashtable.class
                    || actualCollType
                        == org.objectweb.jac.lib.java.util.HashSet.class) {
                    OID coll_oid;
                    if (isNew) {
                        coll_oid =
                            storage.createObject(
                                collections[i].getType().getName());
                    } else {
                        coll_oid =
                            (OID) notloadedReferences.get(collections[i]);
                        if (coll_oid == null)
                            coll_oid =
                                (OID) storage.getField(
                                    oid,
                                    collections[i]);
                        else
                            notloadedReferences.remove(collections[i]);
                        // classes may change after objects were created and
                        // made persistent
                        if (coll_oid == null) {
                            coll_oid = initCollection(wrappee, oid, collections[i]);
                            logger.debug(
                                      "OID of new collection "+collections[i]+": "+coll_oid);
                        }
                    }

                    logger.debug(collections[i] + " -> " + coll_oid);

                    boolean preloaded =
                        PersistenceAC.isFieldPreloaded(collections[i]);
                    String[] toWrap;
                    CollectionWrapper wrapper;
                    if (collections[i].isList()) {
                        // lists
                        logger.debug(
                            "wrapping List "+ collections[i].getName()
                            + " " + System.identityHashCode(coll_value));
                        wrapper =
                            new ListWrapper(getAspectComponent(), 
                                            wrappee, 
                                            collection, 
                                            preloaded);
                        toWrap = toWrapForLists;
                    } else if (collections[i].isSet()) {
                        // sets
                        logger.debug("wrapping Set " + collections[i].getName());
                        wrapper =
                            new SetWrapper(getAspectComponent(), 
                                           wrappee, 
                                           collection, 
                                           preloaded);
                        toWrap = toWrapForSets;
                    } else if (collections[i].isMap()) {
                        // maps
                        logger.debug("wrapping Map " + collections[i].getName());
                        wrapper =
                            new MapWrapper(getAspectComponent(), 
                                           wrappee,
                                           collection, 
                                           preloaded);
                        toWrap = toWrapForMaps;
                    } else {
                        throw new Exception("unsuported collection type");
                    }

                    Wrappee coll = (Wrappee) coll_value;
                    setOID(coll, coll_oid);
                    if (coll != null) {
                        Wrapping.wrap(coll, null, wrapper);
                        Wrapping.wrap(coll, wrapper, toWrap);
                        //Wrapping.wrap(map,wrapper,"memorizeUseDate",preloads);
                    } else {
                        logger.warn(
                            "Collection "
                                + cli.getName() + "." + collections[i].getName()
                                + " is null, not wrapped");
                    }
                }
            }
        } catch (Exception e) {
            logger.error("wrapCollections "+wrappee+"("+oid+")",e);
        }
    }

    /**
     * This wrapping method apply the persistence aspect for a given
     * call on a persistent object.
     *
     * <p>It uses the RTTI aspect to determine the accessed and written
     * field types.<p>
     *
     * Default behavior is:<p>
     *
     * <ul>
     * <li>get the fields that are accessed for reading by this wrapped
     * method
     * <li>if the field is a collection, get the collection from the
     * storage
     * <li>if the field is a reference, get the referenced object from
     * the storage
     * <li>proceed the wrappee (and other wrappers if needed)
     * <li>get the fields that were written by this wrapped method
     * <li>if the field is a collection, update the storage depending
     * on the wrapped method type (adder or remover)
     * <li>if the field is a reference, update the storage to change
     * the referenced object
     * <li> if the field is a simple field, just update the storage
     * with its value.
     * </ul>
     *
     * @return the value returned by the wrapped method
     *
     * @see org.objectweb.jac.core.rtti.MethodItem#getAccessedFields()
     * @see #getCollection(Wrappee,OID,CollectionItem)
     * @see #getReference(Wrappee,OID,FieldItem)
     * @see #addToCollection(Interaction,OID,CollectionItem,Object)
     * @see #removeFromCollection(Interaction,OID,CollectionItem,Object)
     * @see #setReference(Wrappee,OID,FieldItem,Wrappee)
     * @see #setField(Wrappee,OID,FieldItem)
     * @see org.objectweb.jac.core.rtti.MethodItem#getWrittenFields()
     * @see org.objectweb.jac.core.rtti 
     */
    public Object applyPersistence(Interaction interaction) throws Exception {
        if (interaction.method.isStatic()) {
            return proceed(interaction);
        }
        OID oid = getOID(interaction.wrappee);
        logger.debug(
            "applyPersistence on "
                + oid + "(" + interaction.wrappee + ")."
                + interaction.method);
        MethodItem method = (MethodItem) interaction.method;
        FieldItem[] fields;

        // the object may be wrapped but not persistent yet
        if (oid != null) {

            ClassItem cli =
                cr.getClass(interaction.wrappee.getClass());
            // preload accessed fields and collections
            fields = method.getAccessedFields();
            for (int i = 0; fields != null && i < fields.length; i++) {
                FieldItem field = fields[i];

                if (!field.isTransient() && !field.isFinal()) {
                    if (field instanceof CollectionItem
                        && !isWrapped(interaction.wrappee, field)) {
                        getCollection(
                            interaction.wrappee, oid,
                            (CollectionItem) field);
                    } else if (field.isReference()) {
                        getReference(interaction.wrappee, oid, field);
                    }
                }
            }
        }

        Object result = proceed(interaction);

        oid = getOID(interaction.wrappee);
        if (oid != null) {

            // save written fields
            fields = method.getWrittenFields();
            for (int i = 0; fields!=null && i<fields.length; i++) {
                FieldItem field = fields[i];
                if (!field.isTransient()) {
                    logger.debug("Save written field in "
                            + interaction.method + " : "
                            + field.getName());
                    if (field.isReference()) {
                        setReference(
                            interaction.wrappee, oid,
                            field,
                            (Wrappee) field.get(interaction.wrappee));
                    } else if (field instanceof CollectionItem) {
                        // Nothing to do if the collection is wrapped
                        // BUT THE COLLECTION MUST BE SAVED IN THE CASE OF ARRAYS
                        //     storage.updateCollection(collection);
                        CollectionItem collection = (CollectionItem) field;
                        if (collection.isArray()
                            && ((MethodItem) interaction.method)
                                .getCollectionIndexArgument()
                                != -1) {
                            updateCollection(
                                collection,
                                ((MethodItem) interaction.method)
                                    .getCollectionIndexArgument());
                        }
                    } else {
                        setField(interaction.wrappee, oid, field);
                    }
                }
            }

            // handle added collections (adder)
            CollectionItem[] collections = method.getAddedCollections();
            for (int i = 0;
                collections != null && i < collections.length;
                i++) {
                if (!collections[i].isTransient()
                    && !isWrapped(interaction.wrappee, collections[i])) {
                    addToCollection(
                        interaction,
                        oid,
                        collections[i],
                        interaction.args[0]);
                }
            }
            // handle removed collections (remover)
            collections = method.getRemovedCollections();
            for (int i = 0;
                collections != null && i < collections.length;
                i++) {
                if (!collections[i].isTransient()
                    && !isWrapped(interaction.wrappee, collections[i])) {
                    removeFromCollection(
                        interaction,
                        oid,
                        collections[i],
                        interaction.args[0]);
                }
            }

            // UNCOMMENT THIS WHEN UNWRAP WILL BE THREAD-SAFE!!!
            // unwrap the method if it was only a getter
            //if ( ! method.hasAddedCollections() &&
            //     ! method.hasRemovedCollections() &&
            //     ! method.hasWrittenFields() )
            //{
            //   logger.debug("persistence.ref","unwrapping "+getOID()+" "+method);
            //   Wrapping.unwrap(interaction.wrappee,this,"applyPersistence",method);
            //}
        }
        return result;
    }

    /**
     * This method loads alls the persistent objects that are contained
     * in this collection from the storage.
     *
     * <p>If the collection has already been loaded from the storage,
     * do nothing.<p>
     *
     * @param collection the collection to load */

    public void getCollection(Wrappee wrappee, OID oid, CollectionItem collection)
        throws Exception 
    {
        logger.debug(oid + ".getCollection(" + collection.getName() + ")");
        if (!loadedVectors.contains(collection.getName())) {
            String classID = collection.getParent().getName();
            List vector = null;
            Map map = null;
            Storage storage = oid.getStorage();
            OID cid = storage.getCollectionID(oid, collection);
            loggerCol.debug("cid=" + cid);
            if (cid == null) {
                // Handle the case of a new collection added in the model
                initCollection(wrappee, oid, collection);
            }
            if (collection.isList() || collection.isArray()) {
                vector = storage.getList(oid, collection);
            } else if (collection.isSet()) {
                vector = storage.getSet(oid, collection);
            } else if (collection.isMap()) {
                map = storage.getMap(oid, collection);
            } else {
                logger.error(
                    "unhandled collection type : " + collection.getType());
                logger.error(
                    "please see the JAC programming guidelines for more details");
                //new Exception().printStackTrace();
                return;
            }

            collection.clear(wrappee);

            if (collection.isMap()) {
                Iterator it = map.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry entry = (Map.Entry) it.next();
                    attrdef(ATTR_ADDED, "true");
                    collection.add(
                        wrappee,
                        normalizeOutput(entry.getValue()),
                        normalizeOutput(entry.getKey()));
                    attrdef(ATTR_ADDED, null);
                }
            } else {
                for (int i = 0; i < vector.size(); i++) {
                    attrdef(ATTR_ADDED, "true");
                    loggerCol.debug(
                        "adding "
                            + normalizeOutput(vector.get(i))
                            + " to " + collection
                            + "(wrappee=" + wrappee + ")");
                    collection.add(
                        wrappee,
                        normalizeOutput(vector.get(i)),
                        null);
                    attrdef(ATTR_ADDED, null);
                }
            }
            loadedVectors.add(collection.getName());
        }
    }

    /**
     * This method stores the persistent object that is added to the
     * collection into the storage.<p>
     *
     * @param interaction the current interaction
     * @param oid the oid of the object holding the collection
     * @param collection the collection to add to
     * @param value the value to add to the collection
     *
     * @see #removeFromCollection(Interaction,OID,CollectionItem,Object)
     * @see #getCollection(Wrappee,OID,CollectionItem)
     */
    protected void addToCollection(Interaction interaction,
                                   OID oid,
                                   CollectionItem collection,
                                   Object value)
        throws Exception 
    {
        logger.debug(oid + ".addToCollection: "
                     + interaction.method + Arrays.asList(interaction.args));

        if (interaction.args.length == 1) {
            // the method's arguments look correct
            value = normalizeInput(value);
            Storage storage = oid.getStorage();
            OID cid =
                storage.getCollectionID(oid,collection);
            if (collection.isList() || collection.isArray()) {
                storage.addToList(cid, value);
            } else if (collection.isSet()) {
                storage.addToSet(cid, value);
            } else {
                logger.error(
                    "unhandled collection type : " + collection.getType(),new Exception());
            }

        } else {
            logger.error("NOT IMPLEMENTED YET !!!");
            // the method's arguments are weird, so let's diff
            //         storage.updateCollection(collection);
        }
    }

    /**
     * This method delete the persistent object that is removed from
     * the collection from the storage.<p>
     *
     * @param interaction the current interaction
     * @param oid the oid of the object holding the collection
     * @param collection the collection to remove from
     * @param value the value to remove from the collection
     *
     * @see #addToCollection(Interaction,OID,CollectionItem,Object)
     * @see #getCollection(Wrappee,OID,CollectionItem) 
     */
    protected void removeFromCollection(Interaction interaction,
                                        OID oid,
                                        CollectionItem collection,
                                        Object value)
        throws Exception 
    {
        logger.debug(oid + ".removeFromCollection " + interaction.method);

        value = normalizeInput(value);
        Storage storage = oid.getStorage();
        OID cid =
            storage.getCollectionID(oid, collection);
        if (collection.isList() || collection.isArray()) {
            storage.removeFromList(cid, value);
        } else if (collection.isSet()) {
            storage.removeFromSet(cid, value);
        } else {
            logger.error(
                "unhandled collection type : " + collection.getType(), new Exception());
        }
    }

    /**
     * Update the element of a collection at a gievn position
     */
    public void updateCollection(CollectionItem collection, int position)
        throws Exception {
        /*
        checkOid();
        logger.debug(
                  getOID()+".updateCollection: "+method()+Arrays.asList(args()));
        
        if (isPersistent())
        {
           // the method's arguments look correct
           value = normalizeInput(value);
           OID cid = getStorage().getCollectionID(getOID(), collection);
           if ( collection.isList() || collection.isArray() ) {
              Object value = collection.get(wrappee(),position);
              getStorage().setListItem(cid, position, value);
           } else {
              logger.error("unhandled collection type : "+collection.getType());
           }
        }     
        */
    }

    /**
     * This method loads the persistent object that is pointed by the
     * reference from the storage.<p>
     *
     * If the reference has already been loaded from the storage, do
     * nothing.<p>
     *
     * @param reference the reference to load
     *
     * @see #setReference(Wrappee,OID,FieldItem,Wrappee) 
     */
    public void getReference(Wrappee wrappee, OID oid, FieldItem reference)
        throws Exception 
    {
        try {

            loggerRef.debug(
                oid + "[" + this + "].getReference("
                + reference.getName() + ")");
            //System.out.println("GETREF: "+loadedReferences+" : "+notloadedReferences);

            if (!loadedReferences.contains(reference.getName())) {
                OID lOid = (OID) notloadedReferences.get(reference);
                if (lOid == null)
                    lOid = (OID)oid.getStorage().getField(oid,reference);
                else
                    notloadedReferences.remove(reference);
                loggerRef.debug(
                    oid + "." + reference.getName()
                    + " -> " + lOid
                    + " (" + wrappee.getClass().getName() + ")");
                Object obj = null;
                if (lOid != null) {
                    // What the hell is this ???  It bugs if a reference
                    // is initialized by the default constructor
                    //obj=getAC().getObject(lOid,reference.get(wrappee));
                    obj = getAC().getObject(lOid, null);
                }
                attrdef(ATTR_ADDED, "true");
                try {
                    reference.set(wrappee, obj);
                } catch (IllegalArgumentException illegal) {
                    // This may happen if the type of the reference has been changed
                    logger.error(
                        "Incompatible types "
                        + reference.getType() + " and " + obj);
                } finally {
                    attrdef(ATTR_ADDED, null);
                }
                loadedReferences.add(reference.getName());
            } else {
                loggerRef.debug(
                    oid + "." + reference.getName()
                    + " already loaded");
            }
        } catch (Exception e) {
            logger.error("getReference "+oid+"."+reference.getName(),e);
        }
    }

    /**
     * This method stores the persistent object that is pointed by the
     * reference into the storage.<p>
     *
     * @param reference the reference to store
     *
     * @see #getReference(Wrappee,OID,FieldItem)
     */
    protected void setReference(Wrappee wrappee,
                                OID oid,
                                FieldItem reference,
                                Wrappee value)
        throws Exception 
    {
        if (value != null) {
            OID valoid = (OID)Wrapping.invokeRoleMethod(
                value,
                "makePersistent",
                ExtArrays.emptyObjectArray);
            oid.getStorage().updateField(oid,reference,valoid);
        } else {
            oid.getStorage().updateField(oid, reference, null);
        }
        // We won't need to load the reference now
        loadedReferences.add(reference.getName());
        notloadedReferences.remove(reference);
    }

    /**
     * This method stores the field into the storage.<p>
     *
     * @param field the field to store 
     */
    protected void setField(Wrappee wrappee, OID oid, FieldItem field)
        throws Exception 
    {
        Object value = field.get(wrappee);
        if (value instanceof Wrappee) {
            value = Wrapping.invokeRoleMethod(
                (Wrappee)value,
                "makePersistent",
                ExtArrays.emptyObjectArray);
            
        } 
        oid.getStorage().updateField(oid,field,value);
    }

    public void initCollections(Wrappee wrappee, OID oid, CollectionItem[] collections)
        throws Exception 
    {
        logger.debug("wrappee = " + wrappee.getClass().getName());
        logger.debug("collections = " + Arrays.asList(collections));
        for (int i=0; i<collections.length; i++) {
            if (!collections[i].isTransient()) {
                logger.debug("collections[" + i + "] = " + collections[i]);
                initCollection(wrappee, oid, collections[i]);
            }
        }
    }

    OID initCollection(Wrappee wrappee, OID oid, CollectionItem collection)
        throws Exception 
    {
        logger.debug("Init collection " + collection.getName());
        loadedVectors.add(collection.getName());
        Storage storage = oid.getStorage();
        OID cid = null;
        if (collection.isList()
            || collection.isSet()
            || collection.isArray()) 
        {
            if (isWrapped(wrappee, collection)) {
                cid = getOID((Wrappee) collection.get(wrappee));
            } else {
                cid = storage.createObject(collection.getType().getName());
                logger.debug("  new id = " + cid);
            }
            storage.setField(oid, collection, cid);
            Collection coll = collection.getActualCollection(wrappee);
            logger.debug("  Coll value = " + coll);
            if (coll == null) {
                logger.warn(
                    "uninitialized collection "+collection.getLongName()+
                    ". Do you have a constructor with no parameters?");
                return cid;
            }
            // iterator() is wrapped method, so it triggers the
            // loading of the collection from the storage, which is bad
            Iterator it = coll.iterator();
            while (it.hasNext()) {
                Object value = normalizeInput(it.next());
                logger.debug("  Coll elt value = " + value);
                if (value != null) {
                    if (collection.isList() || collection.isArray()) {
                        storage.addToList(cid, value);
                    } else {
                        storage.addToSet(cid, value);
                    }
                }
            }
        } else if (collection.isMap()) {
            if (isWrapped(wrappee, collection)) {
                cid = getOID((Wrappee) collection.get(wrappee));
            } else {
                cid = storage.createObject(collection.getType().getName());
            }
            storage.setField(oid, collection, cid);
            Map map = (Map) collection.get(wrappee);
            Set entries = map.entrySet();
            Iterator it = entries.iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                storage.putInMap(
                    cid,
                    normalizeInput(entry.getKey()),
                    normalizeInput(entry.getValue()));
            }
        } else {
            logger.error("unhandled collection type : " + collection.getType(), 
                         new Exception());
        }
        logger.debug("  End of init collection " + collection.getName());
        return cid;
    }

    /**
     * Initialize all fields of an object in the storage.
     *
     * <p>This method is called when a new object is made persistent. The
     * references and collections are also recursively saved into the
     * storage.<p>
     *
     * @param wrappee the initialized object
     */
    public void initAllFields(Wrappee wrappee, OID oid) throws Exception {
        if (oid==null) 
            throw new InvalidOidException("oid is NULL");
        String classID = wrappee.getClass().getName();
        logger.debug("initAllfields(" + oid + ") : " + classID);
        ClassItem cli = cr.getClass(classID);
        FieldItem[] fields = cli.getPrimitiveFields();
        Storage storage = oid.getStorage();
        storage.startTransaction();
        try {
            // fields
            for (int i = 0; i < fields.length; i++) {
                if (!fields[i].isTransient()
                    && !fields[i].isStatic()
                    && !fields[i].isFinal()) 
                {
                    Object fieldValue = fields[i].get(wrappee);
                    logger.debug("Init field "
                            + fields[i].getName()+ "->" + fieldValue);
                    if (fieldValue != null) {
                        if (fieldValue instanceof Wrappee)
                            storage.setField(oid,fields[i],getOID((Wrappee)fieldValue));
                        else 
                            storage.setField(oid,fields[i],fieldValue);
                    }
                }
            }

            FieldItem[] references = cli.getReferences();

            // references
            for (int i = 0; i < references.length; i++) {
                if (!references[i].isTransient()
                    && !references[i].isStatic()
                    && !references[i].isFinal()) {
                    logger.debug("Init reference " + references[i].getName());
                    Wrappee obj =
                        (Wrappee) references[i].getActualField().get(wrappee);
                    logger.debug("Ref value = "
                            + (obj != null ? obj.getClass().getName() : "null"));

                    loadedReferences.add(references[i].getName());
                    if (obj != null) {
                        OID objoid = 
                            (OID)Wrapping.invokeRoleMethod(
                                obj,
                                "makePersistent",
                                ExtArrays.emptyObjectArray);
                        if (objoid == null) {
                            // this should never happen
                            logger.error(
                                "wrapper.oid is NULL in initAllFields, reference = "
                                    + references[i].getName());
                        } else {
                            logger.debug("Ref value = " + objoid);
                            storage.setField(oid,references[i],objoid);
                            logger.debug("Ref saved");
                        }
                    }
                }

                // UNCOMMENT THIS WHEN UNWRAP WILL BE THREAD-SAFE!!!
                // unwrap the reference's getter because it won't need to be
                // reloaded from the storage
                //MethodItem getter = references[i].getGetter();
                //if (getter!=null) {
                //   Wrapping.unwrap(interaction.wrappee,this,"applyPersistence",getter);            
                //}
            }

            initCollections(wrappee, oid, cli.getCollections());
            storage.commit();
        } catch (Exception e) {
            storage.rollback();
            throw e;
        }
    }

    public static class InvalidOidException extends RuntimeException {
        public InvalidOidException(String msg) {
            super(msg);
        }
    }

    public Object invoke(MethodInvocation invocation) throws Throwable {
        return applyPersistence((Interaction) invocation);
    }

    public Object construct(ConstructorInvocation invocation)
        throws Throwable 
    {
        return handleStatic((Interaction) invocation);
    }
}

// Local Variables: ***
// c-basic-offset:4 ***
// End: ***
