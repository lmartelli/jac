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
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA */

/**
 * ListWrapper.java
 *
 *
 * Created: Sat Oct 13 16:14:15 2001
 *
 * @author <a href="mailto: "Laurent Martelli</a>
 * @version
 */

package org.objectweb.jac.aspects.persistence;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
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
 * A wrapper for the list interface.
 */
public class ListWrapper extends CollectionWrapper {
    static Logger logger = Logger.getLogger("persistence");

    public ListWrapper(AspectComponent ac,
                       Object substance,
                       CollectionItem collection, 
                       boolean isLoaded) 
    {
        super(ac, substance, collection, isLoaded);
    }

    protected void doLoad(Wrappee wrappee) throws Exception {
        OID oid = getOID(wrappee);
        List list = oid.getStorage().getList(oid);
        MethodItem add = cr.getClass(wrappee).getMethod("add(java.lang.Object)");
        Object[] params = new Object[1];
        for (int i=0; i<list.size(); i++) {
            try {
                //list.set(i, normalizeOutput(list.get(i)));
                params[0] = convert(normalizeOutput(list.get(i)),wrappee);
                Wrapping.invokeOrg(wrappee, add, params);
            } catch (NoSuchOIDError e) {
                logger.error(
                    "ListWrapper.doLoad("
                    + oid + "): "+collection.getName()+
                    ": skipping item "+i+" with unknown OID " + list.get(i));
                list.set(i, null);
            } catch (Exception e) {
                logger.error(
                    "ListWrapper.doLoad("
                    + oid + "): "+collection.getName()+
                    ": skipping item "+i+" because of exception",e);
                list.set(i, null);
            }
        }
        /*
        attrdef(ATTR_ADDED, "true");
        // Keep the number of dynamic invocations to the minimum
        Wrapping.invokeOrg(wrappee, "addAll", new Object[] { list });
        attrdef(ATTR_ADDED, null);
        */
    }

    public Object contains(Interaction interaction) throws Exception {
        if (isLoaded) {
            return interaction.proceed();
        } else {
            OID oid = getOID(interaction.wrappee);
            return ExtBoolean.valueOf(
                oid.getStorage().listContains(
                    oid,
                    normalizeInput(interaction.args[0])));
        }
    }

    public Object add(Interaction interaction) throws Exception {
        logger.debug("adding " + interaction.args[0]
                + " to list " + getOID(interaction.wrappee));
        Object result = Boolean.TRUE;
        if (isLoaded)
            result = interaction.proceed();

        OID oid = getOID(interaction.wrappee);
        if (interaction.args.length == 1) {
            // add(Object o)
            oid.getStorage().addToList(
                oid,
                normalizeInput(interaction.args[0]));
        } else if (interaction.args.length == 2) {
            // add(int index, Object o)
            oid.getStorage().addToList(
                oid,
                ((Integer) interaction.args[0]).longValue(),
                normalizeInput(interaction.args[1]));
        }
        return result;
    }

    public Object addAll(Interaction interaction) throws Exception {
        logger.debug("adding " + interaction.args[0]
                + " to list " + getOID(interaction.wrappee));
        Object result = Boolean.TRUE;
        if (isLoaded)
            result = interaction.proceed();
        if (interaction.args.length==1) {
            // addAll(Collection c)
            Iterator i = ((Collection)interaction.args[0]).iterator();
            OID cid = getOID(interaction.wrappee);
            Storage storage = cid.getStorage();
            while (i.hasNext()) {
                storage.addToList(
                    cid, normalizeInput(i.next()));
            }
        } else {
            // addAll(int index, Collection c)
            Iterator i = ((Collection)interaction.args[0]).iterator();
            OID cid = getOID(interaction.wrappee);
            long index = ((Integer) interaction.args[0]).longValue();
            Storage storage = cid.getStorage();
            while (i.hasNext()) {
                storage.addToList(
                    cid, index++, normalizeInput(i.next()));
            }
        }
        return result;
    }

    public Object get(Interaction interaction) throws Exception {
        touch();
        if (isLoaded) {
            return interaction.proceed();
        } else {
            OID oid = getOID(interaction.wrappee);
            return normalizeOutput(
                oid.getStorage().getListItem(
                    oid,
                    ((Integer) interaction.args[0]).longValue()));
        }
    }

    public Object remove(Interaction interaction) throws Exception {
        touch();
        logger.debug("remove : "+ interaction.wrappee
                     + "," + Arrays.asList(interaction.args));
        Object result = null;
        if (isLoaded) {
            result = interaction.proceed();
        } else {
            // WRONG!!!
            result = Boolean.TRUE;
        }
        logger.debug("proceeded");
        OID oid = getOID(interaction.wrappee);
        if (interaction.args[0] instanceof Integer) {
            logger.debug("remove position");
            oid.getStorage().removeFromList(
                oid,
                ((Integer) interaction.args[0]).longValue());
        } else {
            logger.debug("remove value");
            oid.getStorage().removeFromList(
                oid,
                normalizeInput(interaction.args[0]));
        }
        logger.debug("value removed");
        return result;
    }

    public Object removeRange(Interaction interaction) throws Exception {
        // removeRange(int fromIndex, int toIndex)
        touch();
        if (isLoaded) {
            interaction.proceed();
        }
        int from = ((Integer)interaction.args[0]).intValue();
        int to = ((Integer)interaction.args[1]).intValue();
        int size = ((Integer)size(interaction)).intValue();
        if (to>size)
            to = size;
        OID cid = getOID(interaction.wrappee);
        Storage storage = cid.getStorage();
        for (; from<to; from++) {
            storage.removeFromList(cid,from);
        }
        return null;
    }

    /**
     * Remove all instances from the collection
     */
    public Object clear(Interaction interaction) throws Exception {
        touch();
        logger.debug("clear");
        Object result = interaction.proceed();
        OID oid = getOID(interaction.wrappee);
        oid.getStorage().clearList(oid);
        return result;
    }

    public Object set(Interaction interaction) throws Exception {
        touch();
        if (isLoaded())
            interaction.proceed();
        OID oid = getOID(interaction.wrappee);
        oid.getStorage().setListItem(
            oid,
            ((Integer) interaction.args[0]).intValue(),
            interaction.args[1]);
        return null;
    }

    public Object indexOf(Interaction interaction) throws Exception {
        touch();
        if (isLoaded) {
            return interaction.proceed();
        } else {
            OID oid = getOID(interaction.wrappee);
            return new Integer(
                new Long(
                    oid.getStorage().getIndexInList(
                        oid,
                        normalizeInput(interaction.args[0])))
                    .intValue());
        }
    }

    public Object lastIndexOf(Interaction interaction) throws Exception {
        touch();
        if (isLoaded) {
            return interaction.proceed();
        } else {
            OID oid = getOID(interaction.wrappee);
            return new Integer(
                new Long(
                    oid.getStorage().getLastIndexInList(
                        oid,
                        normalizeInput(interaction.args[0])))
                    .intValue());
        }
    }

    protected long getCollectionSize(OID oid) throws Exception {
        return oid.getStorage().getListSize(oid);
    }

    public Object iterator(Interaction interaction) {
        touch();
        return new ListIterator(interaction.wrappee);
    }

    public Object invoke(MethodInvocation invocation) throws Throwable {
        String name = invocation.getMethod().getName();
        Interaction interaction = (Interaction) invocation;
        if (name.equals("iterator")) {
            load(interaction.wrappee);
            //return iterator(interaction);
            return interaction.proceed();
        } else if (name.equals("isEmpty")) {
            return isEmpty(interaction);
        } else if (name.equals("size")) {
            return size(interaction);
        } else if (name.equals("get")) {
            return get(interaction);
        } else if (name.equals("indexOf")) {
            return indexOf(interaction);
        } else if (name.equals("lastIndexOf")) {
            return lastIndexOf(interaction);
        } else if (name.equals("clear")) {
            return clear(interaction);
        } else if (name.equals("set")) {
            return set(interaction);
        } else if (name.equals("add")) {
            return add(interaction);
        } else if (name.equals("addAll")) {
            return addAll(interaction);
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
        } else if (name.equals("removeRange")) {
            return removeRange(interaction);
        } else {
            logger.error("ListWrapper: don't know what to do with method "+name);
        }
        return interaction.proceed();
    }

}
