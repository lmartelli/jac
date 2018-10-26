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

package org.objectweb.jac.core.rtti;


/**
 * This class defines the super class for all the meta items whithin
 * the rtti aspect.<p>
 *
 * A meta item encapsulates a <code>java.lang.reflect</code> item so
 * that the user of this item can add extra informations
 * (attributes). Typically this feature can be used by an aspect to
 * tag an element of the model to react to this tag later on.<p>
 *
 * Examples:<p> <ul> 
 *
 * <li>A persistence aspect can tag some field persistent, add methods
 * that change the object states even if they do not fit naming
 * conventions...<p>
 *
 * <li>A GUI can tag a given field to be invisible or a class to be
 * displayed by a special view (eg a given Swing component)...
 *
 * </ul>
 *
 * @author Renaud Pawlak
 * @author Laurent Martelli
 */

public interface LoadtimeRTTI {
    /**
     * Tells that a method modifies a field
     */
    void addltModifiedField(String className, String methodSign, String fieldName);

    /**
     * Tells that a method reads a field
     */
    void addltAccessedField(String className, String methodSign, String fieldName);

    /**
     * Tells that a method calls add on a collection field
     */
    void addltAddedCollection(String className, String methodSign, String fieldName);

    /**
     * Tells that a method calls remove on a collection field
     */
    void addltRemovedCollection(String className, String methodSign, String fieldName);

    /**
     * Tells that a method calls modifies the content of a collection field
     */
    void addltModifiedCollection(String className, String methodSign, String fieldName);

    /**
     * Tells that a method is the setter of a field (sets the field
     * with the value of a parameter)
     */
    void addltSetField(String className, String methodSign, String fieldName);

    /**
     * Tells that a method returns the value of field
     */
    void addltReturnedField(String className, String methodSign, String fieldName);

    /**
     * Tells wether a method is a getter of a field or not
     */
    void setltIsGetter(String className, String methodSign, boolean isGetter);

    /**
     * Returns the class info of a class
     */
    ClassInfo getClassInfo(String className);

    /**
     * 
     */
    void setClassInfo(String className, ClassInfo classInfo);

    /**
     * Tells that a method's parameter is used as an index of a
     * collection field 
     */
    void setCollectionIndexArgument(String className, String method, int argument);

    /**
     * Tells that a method's parameter is used as an item to be added
     * to a collection field
     */
    void setCollectionItemArgument(String className, String method, int argument);

    /**
     * Tells that a method calls the super method
     */
    void setCallSuper(String className, String method);

    /**
     * Tells that a method invokes another method
     */
    void addInvokedMethod(String className, String methodSign, 
                          InvokeInfo invokeInfo);
}
