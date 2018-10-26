/*
  Copyright (C) 2001-2003 Laurent Martelli <laurent@aopsys.com>
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

import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.CollectionItem;
import org.objectweb.jac.core.rtti.FieldItem;

/**
 * This is the configuration interface of the persistence aspect.<p>
 *
 * @see PersistenceAC
 */

public interface PersistenceConf {   

    /**
     * Associate a String-Object converter with a class
     *
     * <p>In order to stock objects in database, those objects must be
     * converted in a String format. Basic classes are already
     * implemented, but for new classes (such as java.awt.Dimension)
     * new converters must be used. New converters can be used with
     * fhis function.
     *
     * @param cl the class to convert
     * @param converter the converter used. The class must implement
     * <code>org.objectweb.jac.aspects.persistenceconf.StringConverter</code>.
     * @see StringConverter 
     */
    void setValueConverter(ClassItem cl, ClassItem converter);

    /**
     * Specify which the default storage class to use and the
     * parameters to use to instanciate it. The choice of the
     * storage's constructor is based on the number of arguments, so
     * there should not be several constructors with the same number
     * of arguments.
     *
     * Available storage are <code>FSStorage</code> and
     * <code>PostgresStorage</code>
     *
     * @param storageClass the storage class. Constructors of this
     * class must have PersistenceAC as first parameter.
     * @param storageParameters the parameters 
     *
     * @see #configureStorage(String,ClassItem,String[])
     * @see FSStorage
     * @see PostgresStorage 
     */
    void configureStorage(ClassItem storageClass, String[] storageParameters);

    /**
     * Specify which storage class to use and the parameters to use to
     * instanciate it. The choice of the storage's constructor is based
     * on the number of arguments, so there should not be several
     * constructors with the same number of arguments.
     *
     * Available storage are <code>FSStorage</code> and
     * <code>PostgresStorage</code>
     *
     * @param id identifier for the storage. <b>It must not contain the character ':'</b>
     * @param storageClass the storage class. Constructors of this
     * class must have PersistenceAC as first parameter.
     * @param storageParameters the parameters 
     *
     * @see #configureStorage(ClassItem,String[])
     * @see FSStorage
     * @see PostgresStorage 
     */
    void configureStorage(String id,
                          ClassItem storageClass, String[] storageParameters);

    /**
     * Sets the storage to use for some classes.
     *
     * @param classExpr the classes to configure
     * @param storageId the storage ID to use for those classes. It
     * must have been declared with {@link #configureStorage(String,ClassItem,String[])}
     *
     * @see #configureStorage(String,ClassItem,String[]) 
     */
    void setStorage(String classExpr, String storageId);

    /**
     * Configure persistence for one class.
     *
     * <p>The persistence type must be PersistenceAC.ROOT (a named object
     * that is an entry point in the storage) or
     * PersistenceAC.PERSISTENT for a regular persistent class.
     *
     * <p>ROOT objects are always wrapped, PERSISTENT are only wrapped
     * when they become persistent, that is when they are linked to an
     * already persistent object.
     * 
     * @param classExpr class expression of static objects
     * @param nameExpr name expression of static objects
     */
    void makePersistent(String classExpr,String nameExpr);

    /**
     * Registers a static name, that is a name that will not be changed
     * by the persistence AC.
     *
     * <p>Note that the root objects, as defined by the rtti aspect are
     * automatically considered as static objects by the persistence
     * aspect. Thus, it is useless to define a root object static.
     *
     * @param classExpr class expression of static objects
     * @param nameExpr name expression of static objects
     */
    void registerStatics(String classExpr,String nameExpr);

    /**
     * Sets a field or collection to be preloaded
     *
     * <p>By default, persistent fields are loaded when
     * accessed. Preloading a field force it to be loaded when the
     * object is created : in the constructor.
     *
     * @param field the field to preload 
     */
    void preloadField(FieldItem field);

    /**
     * Set all fields of a class to be preloaded
     *
     * <p>Same as preloadField, but for all fields of the class
     *
     * @param cl the class
     * 
     * @see #preloadField(FieldItem) 
     */
    void preloadAllFields(ClassItem cl);

    /**
     * Set a max idle time for a collection.
     *
     * <p>The idle time corresponds to the time the collection is
     * loaded in memory without having been accessed. By default, a
     * collection stays in memory when it is loaded. For big
     * collections, it might be interested to define a max idle
     * time. When the max idle time is reached, the persistence aspect
     * unloads this collection from the memory. The collection will be
     * reloaded on the next access.
     *
     * <p>The frequency the max idle time has be reached is global to
     * all collection and can be defined by
     * <code>defineMaxIdleCheckPeriod</code>
     *
     * @param collection the collection that has a max idle
     * @param maxIdle the max idle time in ms
     * @see #defineMaxIdleCheckPeriod(long) 
     */
    void maxIdle(CollectionItem collection,long maxIdle);

    /**
     * Defines the period that is used to check that the collection max
     * idle time has not been reached.
     *
     * <p>If a collection max idle time is defined and that it has not
     * been used for this time, then the collection is unloaded from
     * the memory by the peristence.
     *
     * <p>This method is not mandatory, by default, a check period of
     * 200.000 ms is defined.
     *
     * @param period the period time check in ms
     * @see #maxIdle(CollectionItem,long) 
     */
    void defineMaxIdleCheckPeriod(long period);

    /**
     * This configuration method allows to disable the cache for a
     * given collection.
     *
     * <p>Then, the collection will never be loaded in the memory but
     * the objects will be accessed directly in the storage. 
     */
    void disableCache(CollectionItem collection);

}
