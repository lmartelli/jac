/*
  Copyright (C) 2002 Laurent Martelli <laurent@aopsys.com>, 
  Julien van Malderen <julien@aopsys.com>

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 2 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  USA */

package org.objectweb.jac.aspects.persistence;

import org.apache.log4j.Logger;
import org.objectweb.jac.core.ACManager;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.util.Base64;
import org.objectweb.jac.util.Log;

/**
 * General converter from and to String for all Objects.
 *
 * <p>It converts Objects into Strings to store them for persistence,
 * and converts stored Strings into their original form for new
 * use. OIDs are converted to <localId>@<storage_id></p>
 */
public class ValueConverter
{
    static Logger logger = Logger.getLogger("persistence.converter");

    /**
     * Returns a string representation of a value so that it can be
     * stored.<p>
     *
     * @param currentStorage the current storage. OID values from this
     * storage will be converted to a string which does not contain
     * the storage id.
     * @param obj a persistent or primitive object
     * @return a ready to store string representation 
     *
     * @see #stringToObject(Storage,String)
     */
    static public String objectToString(Storage currentStorage, Object obj)
    {
        if (obj == null) 
            return "null";
        Class lClass = obj.getClass();
        String className = lClass.getName();
        String value;
      
        StringConverter converter = (StringConverter)ClassRepository.get()
            .getClass(className).getAttribute(PersistenceAC.VALUE_CONVERTER);

        if (obj instanceof OID) {
            OID oid = (OID)obj;
            if (oid.getStorage()==currentStorage)
                value = ((OID)obj).localId();
            else
                value = ((OID)obj).localId()+'@'+oid.getStorage().getId();
        } else {
            if (lClass == java.lang.String.class) {
                value = (String)obj; 
            } else if (converter != null) {
                value = converter.objectToString(obj);
            } else if (lClass.isArray() &&
                       (lClass.getComponentType()==byte.class)) {
                // array of bytes
                value = Base64.encodeToString((byte[])obj);
            } else {
                value = obj.toString();
            }
            value = className+":"+value;
        }
        return value;
    }

    /**
     * Returns an object from a string, depending on the needed type.<p>
     *
     * @param currentStorage the current storage. OID values with no
     * storage id will be attributed to this storage.
     * @param str the type and value in a string format (type:value)
     * @return an object value deduced from the string representation
     * and from the needed type 
     *
     * @see #objectToString(Storage,Object)
     */
    static public Object stringToObject(Storage currentStorage, String str)
    {
        logger.debug("stringToObject("+str+")");
        if (str.equals("null")) 
            return null;
        char firstChar = str.charAt(0);
        if (Character.isDigit(firstChar)) {
            Storage storage;
            int index = str.indexOf('@');
            if (index==-1) {
                storage = currentStorage;
                return new LongOID(storage,Long.parseLong(str));
            } else {
                PersistenceAC ac = (PersistenceAC)ACManager.getACM().getAC("persistence");
                String storageName = str.substring(index+1);
                storage = ac.getStorage(storageName.equals("null")?null:storageName);
                return new LongOID(storage,Long.parseLong(str.substring(0,index)));
            }
        } else {
            int separator = str.indexOf(":");
            String type = str.substring(0,separator);
            str = str.substring(separator+1);
            logger.debug("type = "+type+" ; value = "+str);

            StringConverter converter = (StringConverter)ClassRepository.get()
                .getClass(type).getAttribute(PersistenceAC.VALUE_CONVERTER);

            if (type.equals("java.lang.String")) {
                return str;
            } else if (converter != null) {
                return converter.stringToObject(str);
            } else if (type.equals("boolean") ||
                       type.equals("java.lang.Boolean")) {
                if (str.equals("true"))
                    return Boolean.TRUE;
                else if (str.equals("false"))
                    return Boolean.FALSE;
                else {
                    logger.error("BAD BOOLEAN VALUE "+str);
                    return null;
                }
            } else if (type.equals("java.io.File")) {
                return new java.io.File(str);
            } else if (type.equals("org.objectweb.jac.util.File")) {
                return new org.objectweb.jac.util.File(str);
            } else if (type.equals("int") || type.equals("java.lang.Integer")) {
                return new Integer(str);
            } else if (type.equals("long") || type.equals("java.lang.Long")) {
                return new Long(str);
            } else if (type.equals("float") || type.equals("java.lang.Float")) {
                return new Float(str);
            } else if (type.equals("double") || type.equals("java.lang.Double")) {
                return new Double(str);
            } else if (type.equals("byte") || type.equals("java.lang.Byte")) {
                return new Byte(str);
            } else if (type.equals("[B")) {
                return Base64.decode(str);
            }
            else {
                logger.error("Unhandled type for value `"+str+"' : "+type);
                throw new Error("Unhandled type for value `"+str+"' : "+type);
            }
        }
    }

}
