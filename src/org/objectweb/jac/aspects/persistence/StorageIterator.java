/*
  Copyright (C) 2003 Renaud Pawlak <renaud@aopsys.com>

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
import java.util.NoSuchElementException;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.Wrappee;
import org.objectweb.jac.core.Wrapping;
import org.objectweb.jac.util.ExtArrays;
import org.objectweb.jac.util.Log;

public abstract class StorageIterator implements Iterator {
    static Logger logger = Logger.getLogger("persistence.iterator");

    int index = 0;
   
    OID cid;
    Storage storage;
    Wrappee collection;

    public StorageIterator(Wrappee collection) {
        this.collection = collection;
        storage = (Storage)Wrapping.invokeRoleMethod(collection,"getStorage",ExtArrays.emptyObjectArray);
        cid = (OID)Wrapping.invokeRoleMethod(collection,"getOID",ExtArrays.emptyObjectArray);
    }

    public boolean hasNext() {
        try {
            logger.debug(cid+".hasNext(): "+
                      index+"/"+getCollectionSize());
            if(index<getCollectionSize()) {
                return true;
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public Object next() throws NoSuchElementException {
        logger.debug(cid+".next(): "+index);
        try {
            Object object = (OID)storage.getListItem(cid,index++);
            Object ret = Wrapping.invokeRoleMethod(
                collection,"normalizeOutput",
                new Object[]{object});
            logger.debug("  =>"+ret);
            return ret;
        } catch (IndexOutOfBoundsException e) {
            throw new NoSuchElementException("NoSuchElement with index="+index+" for collection "+cid);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void remove() {
        logger.warn("removing is not implemeted yet on storage iterators");
    }

    /**
     * Returns the size of the collection
     */
    protected abstract long getCollectionSize() throws Exception;

}
