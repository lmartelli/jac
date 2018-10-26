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

/**
 * An implementation of OID with a String
 */
public class StringOID extends OID {
    private String oid = "";
    public StringOID(Storage storage, String oid) {
        super(storage);
        this.oid = oid;
    }
    public String getOID() {
        return oid;
    }
    public String localId() {
        return oid;
    }
    public boolean equals(Object obj) {
        return (obj instanceof StringOID)
            && (oid == ((StringOID)obj).getOID()) 
            && (storage == ((OID)obj).storage);
    }
    public String toString() {
        return oid;
    }   
    public int hashCode() {
        return oid.hashCode();
    }
}
