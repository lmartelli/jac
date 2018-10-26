/*
  Copyright (C) 2001-2003 Laurent Martelli <laurent@aopsys.com>
  
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

package org.objectweb.jac.aspects.gui;

import java.net.MalformedURLException;
import java.net.URL;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.Wrappee;
import org.objectweb.jac.core.Wrapping;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.core.rtti.CollectionItem;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.MethodItem;


/**
 * A class with static methods to factorize code between display types
 * (swing and web)
 */

public class Utils {
    static Logger logger = Logger.getLogger("gui");

    /**
     * Converts a string into an URL
     */
    public static URL stringToURL(String string) {
        if (string.equals("")) {
            return null;
        }
        if (string.indexOf(":")==-1) {
            string = "file:/"+string;
        }
      
        try {
            return new URL(string);
        } catch (MalformedURLException e) {
            logger.warn("Malfromed URL: "+string);
            return null;
        }

    }


    /**
     * Register object events for a single object.
     *
     * @param object the object that is listened to
     * @param client the object that is notified for the events
     */
    public static void registerObject(Object object, ObjectUpdate client) {
        if (object instanceof Wrappee) {
            Wrapping.invokeRoleMethod((Wrappee)object,
                                      ViewControlWrapper.class,"registerObject",
                                      new Object[] {client,null});
        }
    }

    /**
     * Register object events for a single object with extra parameters.
     *
     * @param object the object that is listened to
     * @param client the object that is notified for the events
     * @param param extra info that can be passed to the event
     * listeners */
    public static void registerObject(Object object, 
                                      ObjectUpdate client, 
                                      Object param) {
        if (object instanceof Wrappee) {
            Wrapping.invokeRoleMethod((Wrappee)object,
                                      ViewControlWrapper.class,"registerObject",
                                      new Object[] {client,param});
        }
    }

    /**
     * Register field events for the field of an object.
     *
     * @param object the object that owns the field
     * @param field the field that is listened to
     * @param client the object that is notified for the events
     */
    public static void registerField(Object object, FieldItem field, 
                                     FieldUpdate client) {
        if (object instanceof Wrappee && field!=null) {
            Wrapping.invokeRoleMethod((Wrappee)object,
                                      ViewControlWrapper.class,"registerField",
                                      new Object[] {field,client,null});
        }
    }

    /**
     * Register field events for the field of an object (with parameters).
     *
     * @param object the object that owns the field
     * @param field the field that is listened to
     * @param client the object that is notified for the events
     * @param param extra info that can be passed to the event
     * listeners */
    public static void registerField(Object object, FieldItem field, 
                                     FieldUpdate client, 
                                     Object param) {
        if (object instanceof Wrappee && field!=null) {
            Wrapping.invokeRoleMethod((Wrappee)object,
                                      ViewControlWrapper.class,"registerField",
                                      new Object[] {field,client,param});
        }
    }

    /**
     * Register for the collection events of an object.
     *
     * @param object the object that owns the collection
     * @param collection the collection that is listened to
     * @param client the object that is notified for the events
     */
    public static void registerCollection(Object object, 
                                          CollectionItem collection, 
                                          CollectionUpdate client) {
        if (object instanceof Wrappee && collection!=null) {
            Wrapping.invokeRoleMethod((Wrappee)object,
                                      ViewControlWrapper.class,"registerCollection",
                                      new Object[] {collection,client,null});
        }
    }


    /**
     * Register for the collection events of an object.
     *
     * @param object the object that owns the collection
     * @param collectionName name the collection that is listened to
     * @param client the object that is notified for the events
     */
    public static void registerCollection(Object object, 
                                          String collectionName, 
                                          CollectionUpdate client) {
        CollectionItem collection = 
            ClassRepository.get().getClass(object).getCollection(collectionName);
        registerCollection(object,collection,client);
    }

    /**
     * Register for method events. The client will be notified if the
     * value returned by the method changes.
     *
     * @param object the object that owns the field
     * @param method the method that is listened to
     * @param client the object that is notified for the events */
    public static void registerMethod(Object object, MethodItem method, 
                                      MethodUpdate client) {
        if ((object instanceof Wrappee || object==null) && method!=null) {
            Wrapping.invokeRoleMethod((Wrappee)object,
                                      ViewControlWrapper.class,"registerMethod",
                                      new Object[] {method,client,null});
        }
    }

    /**
     * Register for method events. The client will be notified if the
     * value returned by the method changes.
     *
     * @param object the object that owns the field
     * @param method the method that is listened to
     * @param client the object that is notified for the events 
     * @param param extra info that can be passed to the event listeners 
     */
    public static void registerMethod(Object object, MethodItem method, 
                                      MethodUpdate client, Object param) {
        if ((object instanceof Wrappee || object==null )&& method!=null) {
            Wrapping.invokeRoleMethod((Wrappee)object,method.getClassItem(),
                                      ViewControlWrapper.class,"registerMethod",
                                      new Object[] {method,client,param});
        }
    }

    /**
     * Register for the collection events of an object.
     *
     * @param object the object that owns the collection
     * @param collection the collection that is listened to
     * @param client the object that is notified for the events
     * @param param extra info that can be passed to the event
     * listeners */
    public static void registerCollection(Object object,
                                          CollectionItem collection, 
                                          CollectionUpdate client,
                                          Object param) {
        if (object instanceof Wrappee && collection!=null) {
            Wrapping.invokeRoleMethod((Wrappee)object,
                                      ViewControlWrapper.class,"registerCollection",
                                      new Object[] {collection,client,param});
        }
    }

    /**
     * Unregister from a single object.
     */
    public static void unregisterObject(Object object, ObjectUpdate client) {
        if (object instanceof Wrappee) {
            Wrapping.invokeRoleMethod((Wrappee)object,
                                      ViewControlWrapper.class,"unregisterObject",
                                      new Object[] {client});
        }
    }

    /**
     * Unregister from a single object's field.
     *
     * @param object the object whose collection to unregister from
     * @param field the field to unregister from
     * @param client the client object unregister
     */
    public static void unregisterField(Object object, FieldItem field, 
                                       FieldUpdate client) {
        if (object instanceof Wrappee && field!=null) {
            Wrapping.invokeRoleMethod((Wrappee)object,
                                      ViewControlWrapper.class,"unregisterField",
                                      new Object[] {field,client});
        }
    }

    /**
     * Unregister from a single object's collection.
     *
     * @param object the object whose collection to unregister from
     * @param collection the collection to unregister from
     * @param client the client object unregister
     */
    public static void unregisterCollection(Object object,
                                            CollectionItem collection, 
                                            CollectionUpdate client) {
        if (object instanceof Wrappee && collection!=null) {
            Wrapping.invokeRoleMethod((Wrappee)object,
                                      ViewControlWrapper.class,"unregisterCollection",
                                      new Object[] {collection,client});
        }
    }

    /**
     * Unregister from a single object's collection.
     *
     * @param object the object whose collection to unregister from
     * @param collectionName the name of the collection to unregister from
     * @param client the client object to unregister
     */
    public static void unregisterCollection(Object object,
                                            String collectionName, 
                                            CollectionUpdate client) {
        CollectionItem collection = 
            ClassRepository.get().getClass(object).getCollection(collectionName);
        unregisterCollection(object,collection,client);
    }

    /**
     * Unregister from a single object's method.
     *
     * @param object the object whose method to unregister from
     * @param method the method to unregister from
     * @param client the client object unregister
     */
    public static void unregisterMethod(Object object, MethodItem method, 
                                        MethodUpdate client) {
        if (object instanceof Wrappee && method!=null) {
            Wrapping.invokeRoleMethod((Wrappee)object,method.getClassItem(),
                                      ViewControlWrapper.class,"unregisterMethod",
                                      new Object[] {method,client});
        }
    }

    /**
     * Unregister from an object's events.
     *
     * @param object the object to unregister from
     * @param client the client object to unregister
     */
    public static void unregister(Object object, CollectionUpdate client) {
        if (object instanceof Wrappee) {
            Wrapping.invokeRoleMethod((Wrappee)object,
                                      ViewControlWrapper.class,"unregister",
                                      new Object[] {client});
        }
    }

}

