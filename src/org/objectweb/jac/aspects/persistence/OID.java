/*
  Copyright (C) 2001-2004 Laurent Martelli <laurent@aopsys.com>

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



/**
 * Abstract class that defines an object identifier. It should be
 * subclassed by <code>Storage</code> implementors.
 *
 * <strong>Note that you should also define the <code>hashCode</code>
 * method.</em> 
 */
public abstract class OID implements java.io.Serializable {

    static final Logger logger = Logger.getLogger("persistence");

    /**
     * Test if two OIDs are equal.
     * @param obj the OID to test
     * @return true if obj equals this OID
     */
    public abstract boolean equals(Object obj);
    public abstract int hashCode();
    
    public OID(Storage storage) {
        this.storage = storage;
        if (storage==null)
            logger.error("Storage is null",new Exception());
    }

    /** The storage that defines the OID */
    protected Storage storage;
    public final Storage getStorage() {
        return storage;
    }

    public abstract String localId();
}
