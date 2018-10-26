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

/**
 * An implementation of OID with a long
 *
 */

public class LongOID extends OID {
    private long oid = 0;
    public LongOID(Storage storage, long oid) {
        super(storage);
        this.oid = oid;
    }
    public long getOID() {
        return oid;
    }
    public String localId() {
        return ""+oid;
    }

    public boolean equals(Object obj) {
        return (obj instanceof LongOID)
            && (oid == ((LongOID)obj).getOID())
            && (storage == ((OID)obj).storage);
    }
    public String toString() {
        return Long.toString(oid)+"@"+storage.getId();
    }   
    public int hashCode() {
        return (int)(oid ^ (oid >>> 32));
    }
}
