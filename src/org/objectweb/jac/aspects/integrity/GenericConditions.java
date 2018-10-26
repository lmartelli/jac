/*
  Copyright (C) 2002 Julien van Malderen, Renaud Pawlak <renaud@aopsys.com>

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

package org.objectweb.jac.aspects.integrity;

import java.util.Iterator;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.ACConfiguration;
import org.objectweb.jac.core.ObjectRepository;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.core.rtti.FieldItem;

/**
 * Some basic methods for constraints on fields values.
 *
 * <p>Constraint methods must return a Boolean that is
 * <code>Boolean.TRUE</code> if the test has been validated (passed),
 * <code>Boolean.FALSE</code> else. Their parameters are :</p>
 * 
 * <ul><li><code>Wrappee wrappee</code>: the  substance object
 * (holding the field)</li>
 *
 * <li><code>FieldItem field</code>: the constrained field</li>
 *
 * <li><code>Object value</code>: the proposed future value of the
 * field (can be refused by the contraint)</li>
 *
 * <li><code>Object[] values</code>: some configuration params that
 * can be used in the test</li></ul>
 *
 * @see IntegrityAC#addPreCondition(FieldItem,MethodItem,Object[],String)
 * @see IntegrityAC#addPostCondition(FieldItem,MethodItem,Object[],String)
 * @see IntegrityAC#doCheck() */

public class GenericConditions {
    static final Logger logger = Logger.getLogger("integrity.conditions");

    /**
     * If obj1 and obj2 are instances of String, uses equals(), else ==.
     */
    private static boolean areEqual(Object obj1, Object obj2)
    {
        logger.debug("areEqual("+obj1+","+obj2+")");
        if(obj1 instanceof String && obj2 instanceof String) {
            return ((String) obj1).equals((String) obj2);
        }

        return (obj1 == obj2);
    }

    /**
     * Check if field's value is not equal to the forbidden values. If
     * it is, the method returns <code>false</code>.
     *
     * @param substance the object that owns the field
     * @param field the tested field
     * @param value the value that is about to be set
     * @param values the forbidden values
     */
    public static boolean forbiddenValues(Object substance,
                                          FieldItem field,
                                          Object value,
                                          Object[] values)
    {
        for (int i=0; i<values.length; i++)
            if (areEqual(value, values[i]))
                return false;
        return true;
    }

    /**
     * Check if field's value is equal to one of the authorized
     * values. If it is not, the method returns
     * <code>false</code>.
     * 
     * @param substance the object that owns the field
     * @param field the tested field
     * @param value the value that is about to be set
     * @param values the authorized values
     */
    public static boolean authorizedValues(Object substance,
                                           FieldItem field,
                                           Object value,
                                           Object[] values)
    {
        for (int i=0; i<values.length; i++)
            if (areEqual(value, values[i]))
                return true;
        return false;
    }

    /**
     * Check if this field already has the same value in another object
     * of the same type.
     *
     * @param substance the object that owns the field
     * @param field the tested field
     * @param value the value that is about to be set
     * @param values unused 
     */
    public static boolean isUniqueValue(Object substance,
                                        FieldItem field,
                                        Object value,
                                        Object[] values)
    {
        ClassItem cli = ClassRepository.get().getClass(substance);
        Iterator it = ObjectRepository.getObjects(cli).iterator();
        return true;
    }

    /**
     * Tells if the value is an upper-case char begining string.
     *
     * @param substance the object that owns the field
     * @param field the tested field
     * @param value the value that is about to be set
     * @param values unused 
     */    
    public static boolean isBeginingWithUpperCaseChar(Object substance,
                                                      FieldItem field,
                                                      Object value,
                                                      Object[] values)
    {
        if (value instanceof String) {
            String s = (String)value;
            if (s==null || s.length()==0 || 
                Character.isUpperCase(s.charAt(0))) 
            {
                return true;
            } else {
                return false;
            }
        }
        throw new RuntimeException("Invalid constraint check: "+value+
                                   " is not a string");
    }

    public static boolean isNotNull(Object substance,
                                    FieldItem field,
                                    Object value,
                                    Object[] values)
    {
        return value != null;
    }

    public static boolean isNull(Object substance,
                                 FieldItem field,
                                 Object value,
                                 Object[] values)
    {
        return value == null;
    }


    /**
     * Tells if the value is a letter begining string.
     *
     * @param substance the object that owns the field
     * @param field the tested field
     * @param value the value that is about to be set
     * @param values unused 
     */
    public static boolean isBeginingWithLetter(Object substance,
                                               FieldItem field,
                                               Object value,
                                               Object[] values)
    {
        if (value instanceof String) {
            String s = (String)value;
            if (s==null || s.length()==0 || 
                Character.isLetter(s.charAt(0))) 
            {
                return true;
            } else {
                return false;
            }
        }
        throw new RuntimeException("Invalid constraint check: "+value+
                                   " is not a string");
    }

    /**
     * Tells if the value is a valid java identifier.
     *
     * @param substance the object that owns the field
     * @param field the tested field
     * @param value the value that is about to be set
     * @param values unused 
     */
    public static boolean isJavaIdentifier(Object substance,
                                           FieldItem field,
                                           Object value,
                                           Object[] values)
    {
        if (value instanceof String) {
            String s=(String)value;
            if (s==null) 
                return true;
            if (s.length()==0) 
                return false;
            if (!Character.isJavaIdentifierStart(s.charAt(0)))
                return false;
            for(int i=1;i<s.length(); i++) {
                if(!Character.isJavaIdentifierPart(s.charAt(i)))
                    return false;
            }
            return true;
        }
        throw new RuntimeException("Invalid constraint check: "+value+
                                   " is not a string");
    }

    /**
     * Tells if the value is greater than a given number.
     *
     * @param substance the object that owns the field
     * @param field the tested field
     * @param value the value that is about to be set
     * @param values unused 
     */
    public static boolean isGreaterThan(Object substance,
                                        FieldItem field,
                                        Object value,
                                        Object[] values)
        throws Exception
    {
        if (value instanceof Number) {
            Number th = (Number)ACConfiguration.convertValue(values[0],
                                                             value.getClass());
            if (((Number)value).doubleValue()>th.doubleValue()) {
                return true;
            } else {
                return false;
            }
        }
        throw new RuntimeException("Invalid constraint check: "+value+
                                   " is not a number");
    }

    /**
     * Tells if the value is lower than a given number.
     *
     * @param substance the object that owns the field
     * @param field the tested field
     * @param value the value that is about to be set
     * @param values unused 
     */
    public static boolean isLowerThan(Object substance,
                                      FieldItem field,
                                      Object value,
                                      Object[] values)
        throws Exception
    {
        if (value instanceof Number) {
            Number th = (Number)ACConfiguration.convertValue(values[0],
                                                             value.getClass());
            if (((Number)value).doubleValue()<th.doubleValue()) {
                return true;
            } else {
                return false;
            }
        }
        throw new RuntimeException("Invalid constraint check: "+value+
                                   " is not a number");
    }

}
