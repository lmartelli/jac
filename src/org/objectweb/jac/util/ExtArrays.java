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

package org.objectweb.jac.util;

import java.util.Arrays;
import java.util.List;
import org.aopalliance.intercept.Interceptor;
import java.lang.reflect.Array;



/**
 * Various often used array functions
 */
public class ExtArrays
{
    public static final Object[] emptyObjectArray = new Object[0];
    public static final String[] emptyStringArray = new String[0];
    public static final Class[] emptyClassArray = new Class[0];
    public static final Interceptor[] emptyInterceptorArray = new Interceptor[0];

    /**
     * Insert an object into an array
     *
     * @param position of the object to insert (>=0 and <=array.length)
     * @param toInsert the object to insert
     * @param array the array
     * @return a new array of size array.length+1, where item at
     * position "position" is toInsert. The actual type of the
     * returned array is the same as the parameter <code>array</code>,
     * or the type of <code>toInsert</code>, or Object[].
     *
     * @see #add(int,Object,Object[],Class)
     * @see #add(Object,Object[]) 
     */
    public static Object[] add(int position, Object toInsert, Object[] array) {
        Class type = array.getClass().getComponentType();
        if (!type.isInstance(toInsert)) {
            if (toInsert.getClass().isAssignableFrom(type))
                type = toInsert.getClass();
            else 
                type = Object.class;
        }
        return add(position,toInsert,array,type);
    }

    /**
     * Insert an object into an array
     *
     * @param position of the object to insert (>=0 and <=array.length)
     * @param toInsert the object to insert
     * @param array the array
     * @param type component type of the array to return
     * @return a new array of size array.length+1, where item at
     * position "position" is toInsert.
     *
     * @see #add(int,Object,Object[])
     */
    public static Object[] add(int position, Object toInsert, Object[] array, Class type) {
        Object[] newArray = (Object[])
            Array.newInstance(type,array.length+1);
        if (position>0)
            System.arraycopy(array,0,newArray,0,position);
        newArray[position] = toInsert;
        if (position<array.length) {
            System.arraycopy(array,position,
                             newArray,position+1,
                             array.length-position);
        }
        return newArray;
    }

    /**
     * Append an object at the end of an array
     * @param toAppend the object to add
     * @param array the array
     * @return a new array, of length array.length+1 and whose last item 
     * is toAppend
     *
     * @see #add(int,Object,Object[],Class)
     * @see #add(Object,Object[],Class)
     */
    public static Object[] add(Object toAppend, Object[] array) {
        return add(array.length,toAppend,array);
    }

    /**
     * Append an object at the end of an array
     * @param toAppend the object to add
     * @param array the array
     * @param type component type of the array to return
     * @return a new array, of length array.length+1 and whose last item 
     * is toAppend
     *
     * @see #add(Object,Object[])
     * @see #add(int,Object,Object[])
     */
    public static Object[] add(Object toAppend, Object[] array, Class type) {
        return add(array.length,toAppend,array,type);
    }

    /**
     * Returns the index of a value in an array of Object.
     *
     * @param array the array
     * @param value the searched value
     * @return Returns the lowest integer value i such that 
     * array[i]==value, or -1.
     */
    public static int indexOf(Object[] array, Object value) {
        for (int i=0; i<array.length; i++) {
            if (value==array[i]) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Tells wether an array of objects contains a given value
     * @param array the array to search the value in
     * @param value the object to search for
     * @return true if array contains value
     */
    public static boolean contains(Object[] array, Object value) {
        return indexOf(array,value)!=-1;
    }

    /**
     * Tests equality of some elements of two arrays of bytes.
     * @param a first array of bytes
     * @param offseta start comparison in first array with this offset
     * @param b second array
     * @param offsetb start comparison in second array with this offset
     * @param length number of bytes to compare
     */
    public static boolean equals(byte[] a, int offseta,
                                 byte[] b, int offsetb,
                                 int length) {
        for (int i=0; i<length; i++) {
            if (a[offseta+i]!=b[offsetb+i])
                return false;
        }
        return true;
    }

    /**
     * Builds a List out of an array of bytes
     * @return a List whose elements are Byte objects
     */
    public static List asList(byte[] array) {
        Object[] objects = new Object[array.length];
        for (int i=0; i<array.length; i++) {
            objects[i] = new Byte(array[i]);
        }
        return Arrays.asList(objects);
    }

    /**
     *
     */
    public static Object subArray(Object[] array, int start) {
        Object result = 
            Array.newInstance(
                array.getClass().getComponentType(), 
                array.length-start);
        System.arraycopy(array, start, result, 0, array.length-start);
        return result;
    }
}
