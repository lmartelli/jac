/*
  Copyright (C) 2002-2003 Renaud Pawlak <renaud@aopsys.com>

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 2 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  USA */

package org.objectweb.jac.ide;

import org.objectweb.jac.core.ObjectRepository;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.core.rtti.NamingConventions;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

public class Field extends Member {
   
    public Field() {
        type = null;
    }

    public String getPrototype() {
        if (type==null) 
            throw new RuntimeException("Invalid type for "+getFullName()+": "+type);
        return (isStatic?"static ":"")+(isTransient?"transient ":"")+
            type.getGenerationFullName()+" "+getGenerationName();
    }

    /** The default value of the field */
    String defaultValue;
    public String getDefaultValue() {
        return defaultValue;
    }
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /** Wether the field is read-only. If it is, it won't have a setter. */
    boolean readOnly = false;
    public boolean isReadOnly() {
        return readOnly;
    }
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**Wether the field is calculated */
    boolean calculated = false;
    public boolean isCalculated() {
        return calculated;
    }
    public void setCalculated(boolean calculated) {
        this.calculated = calculated;
    }

    /** A custom getter method */
    Getter getter;
    public Getter getGetter() {
        return getter;
    }
    public void setGetter(Getter getter) {
        this.getter = getter;
    }
    public void initGetter(Getter getter) {
        getter.setName(CodeGeneration.getGetterName(name));
        getter.setType(type);
    }

    /** A custom getter method */
    Setter setter;
    public Setter getSetter() {
        return setter;
    }
    public void setSetter(Setter setter) {
        this.setter = setter;
    }
    public void initSetter(Setter setter) {
        setter.setType(Projects.types.resolveType("void"));
        setter.addParameter(new Parameter(NamingConventions.lowerFirst(name),type));
        setter.setName(CodeGeneration.getSetterName(name));
    }

    /**
     * Returns all non void types
     * @param field unused parameter
     */
    public static Collection getAvailableTypes(Field field) {
        Collection types = ObjectRepository.getObjects(
            ClassRepository.get().getClass(Type.class));
        Iterator it = types.iterator();
        Vector result = new Vector();
        while (it.hasNext()) {
            Type type = (Type)it.next();
            if (!type.getName().equals("void") &&
                (!(type instanceof Class) || field.getProject()==null ||
                 ((Class)type).getProject()==field.getProject())) {
                result.add(type);
            }
        }
        return result;
    }

    boolean isTransient;
    public boolean isTransient() {
        return isTransient;
    }
    public void setTransient(boolean isTransient) {
        this.isTransient = isTransient;
    }
}
