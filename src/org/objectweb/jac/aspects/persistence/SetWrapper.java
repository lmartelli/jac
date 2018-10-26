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

import java.util.List;
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

/**
 * A wrapper for the Set interface.
 */
public class SetWrapper extends CollectionWrapper {
    static Logger logger = Logger.getLogger("persistence");

    public SetWrapper(AspectComponent ac, 
                      Object substance,
                      CollectionItem collection,
                      boolean isLoaded) 
    {
        super(ac, substance, collection, isLoaded);
    }

    protected void doLoad(Wrappee wrappee) throws Exception {
        logger.debug( "do load " + wrappee.getClass());
        // Keep the number of dynamic invocations to the minimum
        OID oid = getOID(wrappee);
        List list = oid.getStorage().getSet(oid);
        MethodItem add = cr.getClass(wrappee).getMethod("add(java.lang.Object)");
        Object[] params = new Object[1];
        for (int i = 0; i < list.size(); i++) {
            try {
                //list.set(i, normalizeOutput(list.get(i)));
                params[0] = convert(normalizeOutput(list.get(i)),wrappee);
                Wrapping.invokeOrg(wrappee, add, params);
            } catch (NoSuchOIDError e) {
                logger.error(
                    "SetWrapper.doLoad("
                    + oid + "): "+collection.getName()
                    + ": skipping object at pos "+i
                    +" with unknown OID "+list.get(i));
                list.set(i, null);
            } catch (Exception e) {
                logger.error(
                    "SetWrapper.doLoad("
                    + oid + "): "+collection.getName()
                    +"skipping object at pos "+i
                    +" because of exception",e);
                list.set(i, null);
            }
        }
        /*
        attrdef(ATTR_ADDED, "true");
        Wrapping.invokeOrg(wrappee, "addAll", new Object[] { list });
        attrdef(ATTR_ADDED, null);
        */
    }

    public Object contains(Interaction interaction) throws Exception {
        touch();
        if (isLoaded) {
            return interaction.proceed();
        } else {
            OID oid = getOID(interaction.wrappee);
            return ExtBoolean.valueOf(
                oid.getStorage().setContains(
                    oid,
                    normalizeInput(interaction.args[0])));
        }
    }

    public boolean add(Interaction interaction) throws Exception {
        touch();
        if (interaction.args[0] != null)
            logger.debug(
                "adding "
                    + interaction.args[0].getClass().getName()
                    + " to set");
        if (isLoaded)
            interaction.proceed();
        OID oid = getOID(interaction.wrappee);
        return oid.getStorage().addToSet(
            oid,
            normalizeInput(interaction.args[0]));
    }

    public Object remove(Interaction interaction) throws Exception {
        touch();
        boolean result1 = false;
        if (isLoaded) {
            result1 = ((Boolean) interaction.proceed()).booleanValue();
        }
        OID oid = getOID(interaction.wrappee);
        boolean result2 =
            oid.getStorage().removeFromSet(
                oid,
                normalizeInput(interaction.args[0]));
        if (isLoaded) {
            if (result1 != result2)
                throw new Error("SetWrapper.remove result1 != result2");
            return ExtBoolean.valueOf(result1);
        } else {
            return ExtBoolean.valueOf(result2);
        }
    }

    /**
     * Remove all instances from the collection
     */
    public Object clear(Interaction interaction) throws Exception {
        touch();
        Object result = interaction.proceed();
        OID oid = getOID(interaction.wrappee);
        oid.getStorage().clearSet(oid);
        return result;
    }

    protected long getCollectionSize(OID oid) throws Exception {
        return oid.getStorage().getSetSize(oid);
    }

    public Object iterator(Interaction interaction) {
        touch();
        return new SetIterator(interaction.wrappee);
    }

    public Object invoke(MethodInvocation invocation) throws Throwable {
        String name = invocation.getMethod().getName();
        Interaction interaction = (Interaction) invocation;
        if (name.equals("iterator")) {
            load(interaction.wrappee);
            return interaction.proceed();
        } else if (name.equals("isEmpty")) {
            return isEmpty(interaction);
        } else if (name.equals("size")) {
            return size(interaction);
        } else if (name.equals("clear")) {
            return clear(interaction);
        } else if (name.equals("add")) {
            return new Boolean(add(interaction));
        } else if (name.equals("remove")) {
            return remove(interaction);
        } else if (name.equals("contains")) {
            return contains(interaction);
        } else if (name.equals("toArray")) {
            load(interaction.wrappee);
            return interaction.proceed();
        } else if (name.equals("clone")) {
            load(interaction.wrappee);
            return interaction.proceed();
        } else {
            logger.error("SetWrapper: don't know what to do with method "+name);
        }
        return interaction.proceed();
    }

}
