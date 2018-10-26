/*
  Copyright (C) 2001 Laurent Martelli <laurent@aopsys.com>

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

package org.objectweb.jac.util;

import java.lang.reflect.Array;

public class Classes 
{

    /**
     * Return the java.lang type wrapped for a primitive type. For
     * instance, the wrapper type for int is java.lang.Integer.
     *
     * @param type the type you want the wrapper type for.  
     * @return the wrapper type for type if type is a primitive
     * type, otherwise type itself.
     */
    public static Class getPrimitiveTypeWrapper(Class type) {
        if (!type.isPrimitive()) 
            return type;
        if (type == int.class)
            return Integer.class;
        else if (type == long.class)
            return Long.class;
        else if (type == short.class)
            return Short.class;
        else if (type == float.class)
            return Float.class;
        else if (type == double.class)
            return Double.class;
        else if (type == boolean.class)
            return Boolean.class;
        else if (type == byte.class)
            return Byte.class;
        else if (type == void.class)
            return Void.class;
        else
            return type;
    }

    /**
     * Return the java.lang type wrapped for a primitive type. For
     * instance, the wrapper type for int is java.lang.Integer.
     *
     * @param type the type you want the wrapper type for.  
     * @return the wrapper type for type if type is a primitive
     * type, otherwise type itself.
     */
    public static String getPrimitiveTypeWrapper(String type) {
        if (type == null) 
            return "java.lang.Void";
        if (type.equals("int"))
            return "java.lang.Integer";
        else if (type.equals("long"))
            return "java.lang.Long";
        else if (type.equals("short"))
            return "java.lang.Short";
        else if (type.equals("float"))
            return "java.lang.Float";
        else if (type.equals("double"))
            return "java.lang.Double";
        else if (type.equals("boolean"))
            return "java.lang.Boolean";
        else if (type.equals("byte"))
            return "java.lang.Byte";
        else if (type.equals("void"))
            return "java.lang.Void";
        else
            return type;
    }


    /**
     * Return the java.lang type wrapped for a primitive type. For
     * instance, the wrapper type for int is java.lang.Integer.
     *
     * @param type the type you want the wrapper type for.  
     * @return the wrapper type for type if type is a primitive
     * type, otherwise type itself.
     */
    public static boolean isPrimitiveType(String type) {
        return 
            type.equals("void") || 
            type.equals("int") || 
            type.equals("long") || 
            type.equals("short") || 
            type.equals("float") || 
            type.equals("double") || 
            type.equals("boolean") || 
            type.equals("byte");
    }

    /**
     * Convert an array of a primitive type to an array of Object.
     */
    public static Object[] convertPrimitiveArray(Object primitiveArray) {
        Class type = primitiveArray.getClass().getComponentType();
        int length = Array.getLength(primitiveArray);
        Object[] result = (Object[])Array.newInstance(getPrimitiveTypeWrapper(type),
                                                      length);
        for (int i=0; i<length; i++) {
            Array.set(result,i,Array.get(primitiveArray,i));
        }
        return result;
    }
}
