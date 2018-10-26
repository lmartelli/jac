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

package org.objectweb.jac.aspects.gui.reports;

import dori.jasper.engine.JRDataSource;
import dori.jasper.engine.JRException;
import dori.jasper.engine.JRField;
import java.util.Collection;
import java.util.Iterator;
import org.objectweb.jac.aspects.gui.GuiAC;
import org.objectweb.jac.core.ObjectRepository;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.core.rtti.CollectionItem;

/**
 * A data source for JasperReports.
 */
public class JacDataSource implements JRDataSource {
    ClassItem componentType;
    Collection collection;
    
    Iterator it;
    Object current;

    /**
     * Create a data source of all instances of a class.
     */
    public JacDataSource(ClassItem cl) {
        this.componentType = cl;
        this.collection = ObjectRepository.getObjects(cl);
    }

    /**
     * Creates a data source for a collection. 
     *
     * @param collection a collection to fetch data from
     * @param componentType the type of the elements in the collection. It can be null.
     */
    public JacDataSource(Collection collection, ClassItem componentType) {
        this.collection = collection;
        this.componentType = componentType;
    }

    /**
     * Creates a data source for a collection of an object
     *
     * @param collection a collection to fetch data from
     * @param substance 
     */
    public JacDataSource(Object substance, CollectionItem collection) {
        this.collection = collection.getActualCollectionThroughAccessor(substance);
        this.componentType = collection.getComponentType();
    }


    /**
     * Creates a data source for a collection of an object
     *
     * @param substance object holding the collection
     * @param collectionName name of the collection to fetch data from
     */
    public JacDataSource(Object substance, String collectionName) {
        this(substance,ClassRepository.get().getClass(substance).getCollection(collectionName));
    }
    // implementation of dori.jasper.engine.JRDataSource interface

    public boolean next() throws JRException
    {
        if (it==null) {
            it = collection.iterator();
        }
        boolean result = it.hasNext();
        if (result)
            current = it.next();
        return result;
    }

    /**
     * Uses the documentation of the field as the full name of the
     * field, since the field's name can not contain dots.
     */
    public Object getFieldValue(JRField field) throws JRException
    {
        if (current==null) {
            throw new JRException(
                "JacDataSource: No current object to get field "+
                field.getName());
        } 
        String name = field.getDescription();
        Object value = null;
        if (componentType!=null) {
            value =  componentType.getField(name).getThroughAccessor(current);
        } else {
            ClassItem cl = ClassRepository.get().getClass(current);
            value = cl.getField(name).getThroughAccessor(current);            
        }

        if (field.getValueClass()==String.class && 
            value!=null &&
            value.getClass()!=String.class) {
            value = GuiAC.toString(value);
        }
        return value;
    }

}
