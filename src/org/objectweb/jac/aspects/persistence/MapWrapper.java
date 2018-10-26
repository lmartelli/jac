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
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA */

package org.objectweb.jac.aspects.persistence;

import java.util.Iterator;
import java.util.Map;
import org.aopalliance.intercept.ConstructorInvocation;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.AspectComponent;
import org.objectweb.jac.core.Interaction;
import org.objectweb.jac.core.Wrappee;
import org.objectweb.jac.core.Wrapping;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.core.rtti.CollectionItem;
import org.objectweb.jac.core.rtti.MethodItem;
import org.objectweb.jac.util.ExtBoolean;
import org.objectweb.jac.util.Log;

/**
 * A wrapper for the Map interface.
 */
public class MapWrapper extends CollectionWrapper {
    static Logger logger = Logger.getLogger("persistence");

    public MapWrapper(AspectComponent ac, 
                      Object substance,    
                      CollectionItem collection, 
                      boolean isLoaded) 
    {
        super(ac, substance, collection, isLoaded);
    }

    protected void doLoad(Wrappee wrappee) throws Exception {
        OID oid = getOID(wrappee);
        Map map = oid.getStorage().getMap(oid);
        // Keep the number of dynamic invocations to the minimum
        //HashMap normalizedMap = new HashMap(map.size());
        Iterator i = map.entrySet().iterator();
        Object[] params = new Object[2];
        MethodItem put = cr.getClass(wrappee).getMethod("put");
        while (i.hasNext()) {
            Map.Entry entry = (Map.Entry) i.next();
            try {
                /*
                  normalizedMap.put(
                    normalizeOutput(entry.getKey()),
                    normalizeOutput(entry.getValue()));
                */
                params[0] = normalizeOutput(entry.getKey());
                params[1] = normalizeOutput(entry.getValue());
                Wrapping.invokeOrg(wrappee, put, params);                
            } catch (NoSuchOIDError e) {
                logger.error(
                    "MapWrapper.doLoad("
                        + oid + "): skipping entry with unknown OID " + entry);
            }
        }
        /*
        attrdef(ATTR_ADDED, "true");
        Wrapping.invokeOrg(wrappee, "putAll", new Object[] { normalizedMap });
        attrdef(ATTR_ADDED, null);
        */
    }

    public Object containsKey(Interaction interaction) throws Exception {
        touch();
        if (isLoaded) {
            return interaction.proceed();
        } else {
            OID oid = getOID(interaction.wrappee);
            return ExtBoolean.valueOf(
                oid.getStorage().mapContainsKey(
                    oid,
                    normalizeInput(interaction.args[0])));
        }
    }

    public Object containsValue(Interaction interaction) throws Exception {
        touch();
        if (isLoaded) {
            return interaction.proceed();
        } else {
            OID oid = getOID(interaction.wrappee);
            return ExtBoolean.valueOf(
                oid.getStorage().mapContainsValue(
                    oid,
                    normalizeInput(interaction.args[0])));
        }
    }

    public Object put(Interaction interaction) throws Exception {
        touch();
        Object ret = null;
        if (isLoaded)
            interaction.proceed();
        OID oid = getOID(interaction.wrappee);
        ret =
            normalizeOutput(
                oid.getStorage().putInMap(
                    oid,
                    normalizeInput(interaction.args[0]),
                    normalizeInput(interaction.args[1])));
        return ret;
    }

    public Object get(Interaction interaction) throws Exception {
        touch();
        if (isLoaded) {
            return interaction.proceed();
        } else {
            OID oid = getOID(interaction.wrappee);
            return normalizeOutput(
                oid.getStorage().getFromMap(
                    oid,
                    normalizeInput(interaction.args[0])));
        }
    }

    public Object remove(Interaction interaction) throws Exception {
        touch();
        Object result1 = null;
        boolean proceeded = false;
        if (isLoaded) {
            result1 = interaction.proceed();
            proceeded = true;
        }
        OID oid = getOID(interaction.wrappee);
        Object result2 =
            oid.getStorage().removeFromMap(
                oid,
                normalizeInput(interaction.args[0]));
        if (proceeded)
            return result1;
        else
            return normalizeOutput(result2);
    }

    /**
     * Remove all instances from the collection
     */
    public Object clear(Interaction interaction) throws Exception {
        touch();
        Object result = interaction.proceed();
        OID oid = getOID(interaction.wrappee);
        oid.getStorage().clearMap(oid);
        return result;
    }

    protected long getCollectionSize(OID oid) throws Exception {
        return oid.getStorage().getMapSize(oid);
    }

    public Object iterator(Interaction interaction) {
        touch();
        return new MapIterator(interaction.wrappee);
    }

    public Object invoke(MethodInvocation invocation) throws Throwable {
        String name = invocation.getMethod().getName();
        Interaction interaction = (Interaction) invocation;
        if (name.equals("keySet")) {
            load(interaction.wrappee);
            return interaction.proceed();
        } else if (name.equals("entrySet")) {
            load(interaction.wrappee);
            return interaction.proceed();
        } else if (name.equals("values")) {
            load(interaction.wrappee);
            return interaction.proceed();
        } else if (name.equals("isEmpty")) {
            return isEmpty(interaction);
        } else if (name.equals("size")) {
            return size(interaction);
        } else if (name.equals("containsKey")) {
            return containsKey(interaction);
        } else if (name.equals("containsValue")) {
            return containsValue(interaction);
        } else if (name.equals("clear")) {
            return clear(interaction);
        } else if (name.equals("put")) {
            return put(interaction);
        } else if (name.equals("remove")) {
            return remove(interaction);
        } else if (name.equals("get")) {
            return get(interaction);
        } else if (name.equals("clone")) {
            load(interaction.wrappee);
            return interaction.proceed();
        } else {
            logger.error("MapWrapper: don't know what to do with method "+name);
        }
        return interaction.proceed();
    }

}
