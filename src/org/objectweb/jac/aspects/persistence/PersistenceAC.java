/*
  Copyright (C) 2001-2004 Laurent Martelli <laurent@aopsys.com>
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

import gnu.regexp.RE;
import gnu.regexp.REException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.AspectComponent;
import org.objectweb.jac.core.BaseProgramListener;
import org.objectweb.jac.core.Collaboration;
import org.objectweb.jac.core.MethodPointcut;
import org.objectweb.jac.core.NameRepository;
import org.objectweb.jac.core.Naming;
import org.objectweb.jac.core.ObjectRepository;
import org.objectweb.jac.core.SerializedJacObject;
import org.objectweb.jac.core.Wrappee;
import org.objectweb.jac.core.Wrapper;
import org.objectweb.jac.core.Wrapping;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.CollectionItem;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.util.ExtArrays;

/**
 * This AC defines a generic and configurable persistence aspect.<p>
 */

public class PersistenceAC extends AspectComponent implements PersistenceConf {

    static Logger logger = Logger.getLogger("persistence");
    static Logger loggerCache = Logger.getLogger("persistence.cache");
    static Logger loggerNaming = Logger.getLogger("persistence.naming");

    /** The global identifier for a persistence root class in the
        RTTI. */
    public static final String ROOT = "root";

    /** The global identifier for a persistent class in the RTTI. */
    public static final String PERSISTENT = "persistent";

    public static final String VALUE_CONVERTER = "valueConverter";
    public static final String PRELOAD_FIELD = "preloadField";

    public static final String DISABLE_PRELOAD = "PersistenceAC.DISABLE_PRELOAD";
    public static final String NO_CACHE = "PersistenceAC.NO_CACHE";
    public static final String RESTORE = "PersistenceAC.RESTORE";

    // Object -> OID
    Hashtable oids = new Hashtable();
    // OID -> Object
    Hashtable objects = new Hashtable();

    private boolean connected = false;

    /**
     * The default storage. */
    protected StorageSpec defaultStorage = null;
    /** List of StorageSpec */
    protected Vector storages = new Vector();
    class StorageSpec {
        StorageSpec(String id,
                    ClassItem storageClass, String[] parameters) 
        {
            this.id = id;
            this.storageClass = storageClass;
            this.parameters = parameters;
        }
        public void addClasses(String classExpr) throws REException {
            classExprs = (RE[])
                ExtArrays.add(
                    MethodPointcut.buildRegexp(classExpr),
                    classExprs);
        }
        private RE[] classExprs = new RE[0];
        private Storage storage;
        private String[] parameters;
        private ClassItem storageClass;
        private String id;
        synchronized Storage getStorage() {
            if (storage == null) {
                if (storageClass == null) {
                    throw new RuntimeException("Persistence: storage is not configured");
                }
                logger.debug(
                    getClass().getName() + ".connectStorage("
                    + storageClass + ","
                    + (parameters != null
                       ? Arrays.asList(parameters).toString()
                       : "null")
                    + ")");
                try {
                    storage = 
                        (Storage)storageClass.newInstance(
                            ExtArrays.add(0,PersistenceAC.this,parameters,Object.class));
                } catch (Exception e) {
                    logger.error("Failed to connect to storage ", e);
                }
            }
            return storage;
        }
        /**
         * Tells wether instances of class should be stored on this storage.
         */
        boolean match(ClassItem cli) {
            if (classExprs==null)
                return false;
            String className = cli.getName();
            for (int i=0; i<classExprs.length; i++) {
                if (classExprs[i].isMatch(className))
                    return true;
            }
            return false;
        }
        String getId() {
            return id;
        }
    }

    /**
     * Gets the storage with a given id, or null.
     * @param id id of the storage to get. If null, returns the defaultStorage.
     */
    protected Storage getStorage(String id) {
        if (id==null) {
            return defaultStorage.getStorage();
        } else {
            Iterator it = storages.iterator();
            while (it.hasNext()) {
                StorageSpec storageSpec = (StorageSpec)it.next();
                if (id.equals(storageSpec.getId()))
                    return storageSpec.getStorage();
            }
            logger.error("No such storage: "+id);
            return null;
        }
    }

    protected Storage[] getStorages() {
        Storage[] result = 
            new Storage[storages.size()+ (defaultStorage!=null ? 1 : 0)];
        Iterator it = storages.iterator();
        int i=0;
        while (it.hasNext()) {
            StorageSpec storageSpec = (StorageSpec)it.next();
            result[i] = storageSpec.getStorage();
            i++;
        }
        if (defaultStorage!=null)
            result[i] = defaultStorage.getStorage();
        return result;
    }

    /**
     * The loaded objects from the storage. */
    //   protected Hashtable objects = new Hashtable();

    /**
     * Close every storage
     */
    public void onExit() {
        Storage[] storages = getStorages();
        for (int i=0; i<storages.length; i++){
            storages[i].close();
        }
    }

    //private HashSet statics = new HashSet();

    // ---- PersistenceConf interface

    public void setValueConverter(ClassItem cl, ClassItem converterClass) {
        StringConverter converter;
        try {
            converter = (StringConverter)converterClass.newInstance();
        } catch (ClassCastException e) {
            error("Converter class "+converterClass.getName()+
                  " does not implement StringConverter");
            return;
        } catch (Exception e) {
            error("Failed to instantiate value converter "+converterClass.getName());
            return;
        }
        cl.setAttribute(VALUE_CONVERTER, converter);
    }

    /**
     * This method is a callback for the timer that defines the max
     * idle time.
     *
     * <p>For all the collections that have a max idle time, it checks
     * that this time is not reached. If it is reached, it unloads the
     * collection.
     *
     * @see #defineMaxIdleCheckPeriod(long)
     * @see #maxIdle(CollectionItem,long) */

    public void checkUnload() {

        Iterator it = collectionIdles.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();

            CollectionItem coll = (CollectionItem) entry.getKey();
            long idle = ((Long) entry.getValue()).longValue();

            loggerCache.debug("checking collection " + coll + " (idle=" + idle + ")");
            ClassItem cl = coll.getClassItem();
            Object[] objects = ObjectRepository.getMemoryObjects(cl);
            for (int i = 0; i < objects.length; i++) {
                Wrappee wrappee = (Wrappee) coll.get(objects[i]);
                loggerCache.debug("checking wrappee "
                          + NameRepository.get().getName(objects[i])
                          + "." + wrappee);
                Date useDate =
                    (Date) Wrapping.invokeRoleMethod(
                        wrappee,
                        CollectionWrapper.class,
                        "getUseDate",
                        ExtArrays.emptyObjectArray);
                Date now = new Date();
                long lastUsed = now.getTime() - useDate.getTime();
                loggerCache.debug("last used=" + lastUsed + " ms ago");
                if (lastUsed > idle) {
                    loggerCache.debug("unloading collection "
                              + NameRepository.get().getName(objects[i])
                              + "." + coll);
                    Wrapping.invokeRoleMethod(
                        wrappee,
                        CollectionWrapper.class,
                        "unload",
                        ExtArrays.emptyObjectArray);
                }
            }
        }
    }

    long checkPeriod = -1;

    public void defineMaxIdleCheckPeriod(long period) {
        checkPeriod = period;
    }

    HashMap collectionIdles = new HashMap();

    public void maxIdle(CollectionItem collection, long maxIdle) {
        collectionIdles.put(collection, new Long(maxIdle));
    }

    public void whenConfigured() {
        if (checkPeriod == -1) {
            checkPeriod = 200000;
        }
        defineTimer(
            checkPeriod,
            cr.getClass(PersistenceAC.class).getMethod(
                "checkUnload"),
            new Object[] {
            });
    }

    public void configureStorage(ClassItem storageClass,
                                 String[] storageParameters) 
    {
        try {
            this.defaultStorage = 
                new StorageSpec(null,storageClass,storageParameters);
            this.defaultStorage.addClasses("ALL");
        } catch (REException e) {
            error(e.toString());
        }
    }

    public void configureStorage(String id,
                                 ClassItem storageClass, 
                                 String[] storageParameters) 
    {
        storages.add(
            new StorageSpec(id,storageClass,storageParameters));
    }

    public void setStorage(String classExpr, String storageId) {
        try {
            getStorageSpec(storageId).addClasses(classExpr);
        } catch (REException e) {
            error(e.toString());
        }
    }

    /** MethodPointcuts for static objects */
    Vector staticPointcuts = new Vector();

    public void registerStatics(String classExpr, String nameExpr) {
        /* Wrap constructors with PersistenceWrapper.handleStatic() */
        logger.debug("registerStatics " + classExpr + " " + nameExpr);
        staticPointcuts.add(
            pointcut(
                nameExpr,
                classExpr,
                "CONSTRUCTORS",
                PersistenceWrapper.class.getName(),
                null,
                SHARED));
    }

    /** Pointcuts for persistent objects */
    Vector persistentPointcuts = new Vector();

    public void makePersistent(String classExpr, String nameExpr) {
        // Wrap modifiers, collection accessors, reference accessors
        // (but not constructors) with PersistenceWrapper.applyPersistence()
        persistentPointcuts.add(
            pointcut(
                nameExpr,
                classExpr,
                "MODIFIERS({!transient}) || COLACCESSORS({!transient}) || REFACCESSORS({!transient}) && !CONSTRUCTORS",
                PersistenceWrapper.class.getName(),
                null,
                NOT_SHARED));
    }

    /**
     * Tells wether a wrappee is persistent or not.
     * @param wrappee the wrappee
     * @see #makePersistent(String,String)
     */
    boolean isPersistent(Wrappee wrappee) {
        Iterator it = persistentPointcuts.iterator();
        ClassItem cli = cr.getClass(wrappee);
        while (it.hasNext()) {
            MethodPointcut pointcut = (MethodPointcut) it.next();
            if (pointcut.isClassMatching(wrappee, cli))
                return true;
        }
        return false;
    }

    /**
     * Tells wether a wrappee is static or not.
     * @param wrappee the wrapee
     * @param objName name of the wrappee
     * @see #makePersistent(String,String)
     */
    boolean isStatic(Wrappee wrappee, String objName) {
        ClassItem cli = cr.getClass(wrappee);
        // check the statics
        Iterator it = staticPointcuts.iterator();
        while (it.hasNext()) {
            MethodPointcut pointcut = (MethodPointcut) it.next();
            if (pointcut.isClassMatching(wrappee, cli)
                && pointcut.isNameMatching(wrappee, objName))
                return true;
        }
        return false;
    }

    // ---- end of PersistenceConf

    /**
     * Returns the storage for a given class
     *
     * @param cli a class
     * @return the storage of the class
     */
    public Storage getStorage(ClassItem cli) {
        Iterator it = storages.iterator();
        while (it.hasNext()) {
            StorageSpec storageSpec = (StorageSpec)it.next();
            if (storageSpec.match(cli)) {
                return storageSpec.getStorage();
            }
        }
        if (defaultStorage!=null)
            return defaultStorage.getStorage();
        throw new RuntimeException(
            "Cannot find storage for class "+cli.getName());
    }

    public StorageSpec getStorageSpec(String storageId) {
        Iterator it = storages.iterator();
        while (it.hasNext()) {
            StorageSpec storageSpec = (StorageSpec)it.next();
            if (storageSpec.getId().equals(storageId)) {
                return storageSpec;
            }
        }
        throw new RuntimeException();
    }

    /**
     * Returns the storage for a given object
     *
     * @param obj object to get the storage for
     * @return the storage of the class
     */
    public Storage getStorage(Object obj) {
        return getStorage(cr.getClass(obj));
    }

    /**
     * The persistence aspect checks whether an object was in the
     * storage when a <code>NameRepository.getObject</code> call
     * failed.
     * @see BaseProgramListener#whenObjectMiss(String)
     */
    public void whenObjectMiss(String name) {
        loggerNaming.debug("whenObjectMiss " + name);
        // Loop through all the storages
        Storage[] storages = getStorages();
        for (int i=0; i<storages.length; i++){
            try {
                OID oid = storages[i].getOIDFromName(name);
                if (oid != null) {
                    loggerNaming.debug("found name " + name + " -> " + oid);
                    attrdef(BaseProgramListener.FOUND_OBJECT, getObject(oid, null));
                    return;
                }
            } catch (Exception e) {
                logger.error("whenObjectMiss "+name,e);
            }
        }
    }

    /**
     * Delegates naming to the storage
     */
    public String whenNameObject(Object object, String name) {
        loggerNaming.debug("whenNameObject " + object + " <- " + name);
        if (!(object instanceof Wrappee))
            return name;
        Wrappee wrappee = (Wrappee) object;
        if (isPersistent(wrappee) && !isStatic(wrappee, name)) {
            try {
                name = getStorage(object).newName(object.getClass().getName());
            } catch (Exception e) {
                logger.error("Failed to name object "+object,e);
            }
            loggerNaming.debug("   -> " + name);
        }
        return name;
    }

    public void getNameCounters(Map counters) {
        try {
            Storage[] storages = getStorages();
            for(int i=0; i<storages.length; i++) {
                counters.putAll(storages[i].getNameCounters());                
            }
        } catch (Exception e) {
            logger.error("getNameCounters failed",e);
        }
    }

    public synchronized void updateNameCounters(Map counters) {
        try {
            Storage[] storages = getStorages();
            for(int i=0; i<storages.length; i++) {
                storages[i].updateNameCounters(counters);
            }
        } catch (Exception e) {
            logger.error("updateNameCounters failed",e);
        }
    }

    /**
     * Add an object in the list of persistent objects.
     *
     * @param oid the OID of the object
     * @param object the object
     */
    protected void registerObject(OID oid, Object object) {
        logger.debug("registerObject(" + oid + "," + object.getClass() + ")");
        Object currentObject = objects.get(oid);
        if (currentObject!=null) {
            if (currentObject!=object) {
                logger.error("registerObject "+oid+","+object,new Exception());
                throw new Error(
                    "PersistenceAC.registerObject("+oid+","+object+"): an object "+
                    currentObject+" is already registered with this oid");
            } else {
                logger.warn("PersistenceAC.registerObject("+oid+","+object+
                            "): already registered");
            }
        }
        objects.put(oid, object);
        oids.put(object, oid);
        logger.debug("object " + oid + " added");
    }

    /**
     * Returns a reference to an object with a given OID.
     * 
     * <p>Loads the object from a storage if necessary, or returns a
     * cached object.</p>
     *
     * @param oid OID of the object 
     * @param newObject use this object instead of instanciating a new one
     */
    synchronized Object getObject(OID oid, Object newObject) {
        Object result = objects.get(oid);
        try {
            if (result != null) {
                logger.debug("Object " + oid + " found in cache -> " + result);
                return result;
            } else {
                logger.debug(this + ".Object " + oid
                          + " NOT found in cache; Loading from storage\n");
                Storage storage = oid.getStorage();
                String lClassID = storage.getClassID(oid);
                if (lClassID == null)
                    logger.error("getClassID(" + oid + ") -> NULL");
                ClassItem lClass = cr.getClass(lClassID);
                logger.debug("Class = " + lClass.getName());
                if (newObject == null) {
                    Naming.setName(storage.getNameFromOID(oid));
                    Collaboration collab = Collaboration.get();
                    collab.addAttribute(RESTORE, Boolean.TRUE);
                    try {
                        newObject = lClass.newInstance();
                    } finally {
                        collab.removeAttribute(RESTORE);
                    }
                }
                Wrappee wrappee = (Wrappee) newObject;
				//PersistenceWrapper wrapper = wrap(wrappee,oid);
                registerObject(oid, wrappee);
				// load the new object's fields
                Wrapping
                    .invokeRoleMethod(
                        wrappee,
                        PersistenceWrapper.class,
                        "loadAllFields",
                        new Object[] {oid});
				// wrap its collections
                Wrapping.invokeRoleMethod(
                    wrappee,
                    PersistenceWrapper.class,
                    "wrapCollections",
                    new Object[] { oid, Boolean.FALSE });
                logger.debug("New object " + oid + " : " + newObject);
                logger.debug("Object loaded");
                result = newObject;
            }
        } catch (Exception e) {
            logger.error("getObject "+oid,e);
        }
        return result;
    }

    public OID getOID(Wrappee wrappee) {
        return (OID) oids.get(wrappee);
    }

    /**
     * This method allows the deserialization of the OID of a
     * persistent object.<p>
     *
     * @param orgObject the JAC object structure that is beeing
     * deserialized
     * @see #whenSerialized(SerializedJacObject) */

    public void whenDeserialized(SerializedJacObject orgObject) {
        /*      
		OID oid = (OID) orgObject.getACInfos( "persistence" );
		if ( oid != null ) {
                Wrappee finalObject = (Wrappee)attr("finalObject");
                PersistenceWrapper pw = wrap( finalObject, oid, false );
                //pw.setOid( oid );
		}
        */
    }

    /**
     * This method add the OID info to the serialized JAC object when a
     * serialization is requested by another aspect.<p>
     *
     * This adding allows, for instance, the OID to be transmitted to
     * remote containers.<p>
     *
     * @param finalObject the object that is being serialized */

    public void whenSerialized(SerializedJacObject finalObject) {
        /*  
            Wrappee orgObject = (Wrappee)attr("orgObject");
            if ( orgObject.isExtendedBy( PersistenceWrapper.class ) ) {
            finalObject.setACInfos(
            "persistence", orgObject.invokeRoleMethod( "getOid", ExtArrays.emptyObjectArray ) );
            }
        */
    }

    /**
     * Load objects from the storage when required.
     */
    public void whenGetObjects(Collection objects, ClassItem cl) {
        logger.debug("PersistenceAC.whenGetObjects " + cl);
        if (cl == null)
            return;
        try {
            logger.debug("PersistenceAC.whenGetObjects " + cl);
            Collection oids = getStorage(cl).getObjects(cl);
            Iterator i = oids.iterator();
            while (i.hasNext()) {
                OID oid = (OID) i.next();
                Object object = getObject(oid, null);
                if (!objects.contains(object))
                    objects.add(object);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean beforeRunningWrapper(
        Wrapper wrapper,
        String wrappingMethod) {
        if (attr("Persistence.disabled") != null) {
            return false;
        } else {
            return true;
        }
    }

    public void whenDeleted(Wrappee object) {
        try {
            OID oid = getOID(object);
            Storage storage = oid.getStorage();
            if (storage != null) {
                if (oid != null)
                    storage.deleteObject(oid);
                whenFree(object);
            }
        } catch (Exception e) {
            logger.error("whenDeleted "+object+" failed",e);
        }
    }

    public void whenFree(Wrappee object) {
        try {
            OID oid = getOID(object);
            Storage storage = oid.getStorage();
            if (storage != null) {
                if (oid != null)
                    oids.remove(oid);
                if (objects.contains(object))
                    objects.remove(object);
            }
        } catch (Exception e) {
            logger.error("whenFree "+object+" failed",e);
        }
    }

    public void preloadField(FieldItem field) {
        field.setAttribute(PRELOAD_FIELD, Boolean.TRUE);
    }

    public void preloadAllFields(ClassItem cl) {
        FieldItem[] fields = cl.getFields();
        if (fields != null) {
            for (int i = 0; i < fields.length; i++) {
                fields[i].setAttribute(PRELOAD_FIELD, Boolean.TRUE);
            }
        }
    }

    public static boolean isFieldPreloaded(FieldItem field) {
        Boolean value = (Boolean) field.getAttribute(PRELOAD_FIELD);
        return value != null && value.booleanValue();
    }

    public void disableCache(CollectionItem collection) {
        collection.setAttribute(NO_CACHE, "true");
    }

    public String[] getDefaultConfigs() {
        return new String[] {
            "org/objectweb/jac/aspects/persistence/persistence.acc",
            "org/objectweb/jac/aspects/user/persistence.acc" };
    }

    public static class NoSuchWrapperException extends RuntimeException {
    }

    /**
     * Converts a String into a LongOID. 
     * @param str the string representing the OID. If it's like
     * "<number>@<string>", the "<string>" must be a storage
     * id. Otherwise, the defaultStorage is used
     * @param defaultStorage storage to use to build the OID if the
     * string does not contain an OID part.
     */
    public LongOID parseLongOID(String str, Storage defaultStorage) {
        int index = str.indexOf('@');
        if (index==-1) {
            return 
                new LongOID(defaultStorage,Long.parseLong(str));
        } else {
            return 
                new LongOID(
                    getStorage(str.substring(index+1)),
                    Long.parseLong(str.substring(0,index)));
        }
    }
}
