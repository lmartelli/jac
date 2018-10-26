/*
  Copyright (C) 2003 Laurent Martelli <laurent@aopsys.com>

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

package org.objectweb.jac.core;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Iterator;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.core.rtti.CollectionItem;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.MethodItem;
import org.objectweb.jac.core.rtti.NoSuchMethodException;
import org.objectweb.jac.core.rtti.RttiAC;
import org.objectweb.jac.util.ExtArrays;

public class Utils {
    static Logger logger = Logger.getLogger("clone");

    /**
     * "Semantic" clone. Same as <code>clone(o,null)</code>
     *
     * @param o the object to clone
     * @return the cloned object
     *
     * @see #clone(Object,FieldItem)
     */
    public static Object clone(Object o) 
        throws InstantiationException, IllegalAccessException, Exception 
    {
        return clone(o,(FieldItem)null);
    }

    /**
     * "Semantic" clone. Collections marked as aggregation are
     * recursively cloned (objects in the collection are cloned),
     * otherwise the collection of the cloned objet will contains the
     * same objects as the source object.
     *
     * @param o the object to clone
     * @param ignoredRelation do not clone this relation and leave it
     * empty. If null, all relations are cloned.
     * @return the cloned object 
     *
     * @see #clone(Object)     
     */
    public static Object clone(Object o, FieldItem ignoredRelation) 
        throws InstantiationException, IllegalAccessException, Exception 
    {
        logger.debug("Cloning "+o);
        ClassRepository cr = ClassRepository.get();
        ClassItem cli = cr.getClass(o);
        Object clone = cli.newInstance();
        Iterator i = cli.getAllFields().iterator();
        while (i.hasNext()) {
            FieldItem field = (FieldItem)i.next();
            if (field.isCalculated() || ignoredRelation==field)
                continue;
            if (field.isPrimitive()) {
                logger.debug("  copying value of fied "+field.getName());
                try {
                    Object fieldValue = field.getThroughAccessor(o);
                    
                    // Cloneable is useless in this generic context
                    try {
                        MethodItem mClone = cr.getClass(fieldValue).getMethod("clone()");
                        if (mClone!=null && Modifier.isPublic(mClone.getModifiers()))
                            fieldValue = mClone.invoke(fieldValue, ExtArrays.emptyObjectArray);
                    } catch(NoSuchMethodException e) {
                    }
                    field.setThroughWriter(clone,fieldValue);
                } catch (Exception e) {
                    logger.error("clone("+o+"): failed to clone field "+field,e);
                }
            } else if (field instanceof CollectionItem) {
                CollectionItem collection = (CollectionItem)field;
                logger.debug("  copying collection "+field.getName());
                if (collection.isMap()) {
                } else {
                    Iterator j = ((Collection)collection.getThroughAccessor(o)).iterator();
                    while(j.hasNext()) {
                        Object item = j.next();
                        if (collection.isAggregation()) {
                            item = clone(item,(FieldItem)field.getAttribute(RttiAC.OPPOSITE_ROLE));
                        }
                        collection.addThroughAdder(clone,item);
                    }
                }
            } else {
                field.setThroughWriter(clone,field.getThroughAccessor(o));
            }
        }
        logger.debug(o+" cloned");
        return clone;
    }

    public static Object clone(Object o, String ignoredRelation) 
        throws InstantiationException, IllegalAccessException, Exception 
    {
        return clone(o,ClassRepository.get().getClass(o).getField(ignoredRelation));
    }
}
