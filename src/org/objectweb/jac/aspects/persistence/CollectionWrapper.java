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

package org.objectweb.jac.aspects.persistence;

import org.apache.log4j.Logger;
import org.objectweb.jac.core.AspectComponent;
import org.objectweb.jac.core.Interaction;
import org.objectweb.jac.core.Wrappee;
import org.objectweb.jac.core.Wrapping;
import org.objectweb.jac.core.rtti.CollectionItem;
import org.objectweb.jac.core.rtti.RttiAC;
import org.objectweb.jac.util.ExtArrays;
import org.objectweb.jac.util.ExtBoolean;

/**
 * Base class for collection wrappers
 */

public abstract class CollectionWrapper extends AbstractPersistenceWrapper {
    static Logger logger = Logger.getLogger("persistence");

    boolean isLoaded = false;
    CollectionItem collection;
    Object substance;

    public CollectionWrapper(AspectComponent ac, 
                             Object substance,
                             CollectionItem collection, 
                             boolean isLoaded) 
    {
        super(ac);
        this.collection = collection;
        this.substance = substance;
        this.isLoaded = isLoaded;
    }

    boolean cache=false;

    /**
     * Load the whole collection if it is not already loaded
     */
    public synchronized void load(Wrappee wrappee) throws Exception {
        if (!isLoaded) {
            logger.debug("loading collection "+getOID(wrappee)+" - "+wrappee);
            doLoad(wrappee);
            isLoaded = true;
        }
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    /**
     * Unload the collection.
     */
    public synchronized void unload(Wrappee wrappee) {
        logger.debug(getOID(wrappee)+".unload...");
        isLoaded = false;
        Wrapping.invokeOrg(wrappee,"clear",ExtArrays.emptyObjectArray);      
    }

    /**
     * Really load the whole collection. This is an abstract method
     * must be overriden by subclasses.
     */
    protected abstract void doLoad(Wrappee wrappee) throws Exception;

    /**
     * Remove all instances from the collection
     */
    public abstract Object clear(Interaction interaction) throws Exception;

    public Object preload(Interaction interaction) throws Exception {
        logger.debug(getOID(interaction.wrappee)+
                  ".preload for "+interaction.method);
        try {
            load(interaction.wrappee);
        } catch (Exception e) {
            logger.warn("Failed to preload collection for "+interaction,e);
        }
        return proceed(interaction);
    }
   
    public synchronized Object size(Interaction interaction) throws Exception {
        if (!isLoaded) {
            long size = getCollectionSize(getOID(interaction.wrappee));
            // If the collection isEmpty, we can consider it is loaded
            // even if it not (since it's empty)
            if (size==0)
                isLoaded = true;
            return new Integer(new Long(size).intValue());
        } else {
            return proceed(interaction);
        }
    }

    protected abstract long getCollectionSize(OID oid) throws Exception;

    public synchronized Object isEmpty(Interaction interaction) throws Exception {
        if (!isLoaded) {
            boolean result = getCollectionSize(getOID(interaction.wrappee))==0;
            // If the collection isEmpty, we can consider it is loaded
            // even if it not (since it's empty)
            if (result)
                isLoaded = true;
            return ExtBoolean.valueOf(result);
        } else {
            return proceed(interaction);
        }
    }

    // The last time the wrapped object was used
    long useDate = System.currentTimeMillis();

    //public Object memorizeUseDate(Interaction i) {
    //   useDate = System.currentTimeMillis());
    //   return proceed(i);
    //}

    public long getUseDate(Wrappee wrappee) {
        return useDate;
    }

    /**
     * Sets useDate to current time
     */
    protected void touch() {
        useDate = System.currentTimeMillis();
    }

    public abstract Object iterator(Interaction interaction);

    public boolean isCache() {
        return cache;
    }

    public void setCache(boolean b) {
        cache = b;
    }

    protected Object convert(Object value, Object wrappee) throws Exception {
        if (value==null) {
            return null;
        } else {
            Class collType = (Class)collection.getComponentType().getDelegate();
            if (!collType.isAssignableFrom(value.getClass())) {
                Object converted = RttiAC.convert(value,collType);
                if (converted == value)
                    logger.warn(
                        "Failed to convert "+value+" into "+collType.getName()+
                        " for collection "+substance+"["+getOID((Wrappee)substance)+"]."+
                        collection.getName());
                return converted;
            } else {
                return value;
            }
        }
    }
}
