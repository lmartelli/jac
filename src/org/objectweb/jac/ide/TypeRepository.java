/*
  Copyright (C) 2002 Renaud Pawlak <renaud@aopsys.com>

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

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

/**
 * A repository for all the types that are not user-defined
 * classes. */

public class TypeRepository {

    /**
     * Resolve a type from its name (use for primitive types only).
     *
     * @param name the type name (e.g. int) */

    public Type resolveType(String name) {
        return resolveType(name,"");
    }

    /**
     * Resolve a type from its name and path.
     *
     * @param name the type name (e.g. Vector)
     * @param path the type path (e.g. java.util) */

    public Type resolveType(String name,String path) {
        Iterator it = primitiveTypes.iterator();
        while(it.hasNext()) {
            Type type = (Type)it.next();
            if (type.getName().equals(name)) {
                return type;
            }
        } 

        it = extendedTypes.iterator();
        while(it.hasNext()) {
            Type type = (Type)it.next();
            if (type.getName().equals(name)) {
                return type;
            }
        } 

        it = externalClasses.iterator();
        while(it.hasNext()) {
            Type type = (Type)it.next();
            if (path.equals("") && type.getName().equals(name)) {
                return type;
            } else if (path.equals(type.getPackagePath()) && 
                       type.getName().equals(name)) {
                return type;
            }
        }
        return null;
    }

    HashSet primitiveTypes = new HashSet();
   
    /**
     * Get all the primitive types.
     * @return value of primitiveTypes.
     */
    public Set getPrimitiveTypes() {
        return primitiveTypes;
    }

    /**
     * Add a primitive type in the repository.
     *
     * @param type the primitive type 
     */
    public void addPrimitiveType(Type type) {
        if (!containsType(type))
            primitiveTypes.add(type);
    }
    public void removePrimitievType(Type type) {
        primitiveTypes.remove(type);
    }

    public Type getVoid() {
        return resolveType("void");
    }

    public Type getInt() {
        return resolveType("int");
    }

    public Type getLong() {
        return resolveType("long");
    }

    public Type getBoolean() {
        return resolveType("boolean");
    }

    public Type getDouble() {
        return resolveType("double");
    }

    public Type getFloat() {
        return resolveType("float");
    }

    HashSet externalClasses = new HashSet();
   
    /**
     * Get the external classes.
     * @return value of externalClasses.
     */
    public Set getExternalClasses() {
        return externalClasses;
    }

    /**
     * Add an external class in the repository.
     *
     * @param type the external class's type 
     */
    public void addExternalClass(Type type) {
        if (!containsType(type))
            externalClasses.add(type);
    }

    /**
     * Remove an external class from the repository.
     *
     * @param type the external class's type 
     */
    public void removeExternalClass(Type type) {
        externalClasses.remove(type);
    }

    HashSet extendedTypes = new HashSet();
    public Set getExtendedTypes() {
        return extendedTypes;
    }
    public void addExtendedType(ExtendedType type) {
        if (!containsType(type))
            extendedTypes.add(type);
    }
    public void removeExtendedType(ExtendedType type) {
        extendedTypes.remove(type);
    }

    HashSet enumeratedTypes = new HashSet();
    public Set getEnumeratedTypes() {
        return enumeratedTypes;
    }
    public void addEnumeratedType(EnumeratedType type) {
        if (!containsType(type))
            enumeratedTypes.add(type);
    }
    public void removeEnumeratedType(EnumeratedType type) {
        enumeratedTypes.remove(type);
    }

    /**
     * Tells wether the repository contains a given type
     */
    public boolean containsType(Type type) {
        return resolveType(type.getName(),type.getPackagePath())!=null;
    }
}
