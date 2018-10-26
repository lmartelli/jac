/*
  Copyright (C) 2003 Laurent Martelli <laurent@aopsys.com>

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

import java.util.Collection;
import java.util.Vector;
import org.objectweb.jac.core.ObjectRepository;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.NamingConventions;
import org.objectweb.jac.util.Strings;

public class RelationRole extends Role implements Typed {

    protected static final String UNDEFINED = "<UNDEFINED END>";

    public RelationRole(Link link) {
        super(link);
    }

    public RelationRole() {
    }

    String cardinality = "0-1";
    public String getCardinality() {
        return cardinality;
    }
    public void setCardinality(String cardinality) {
        this.cardinality = cardinality;
    }

    /**
     * Returns the role name used for code generation
     */
    public String getGenerationName() {
        if (end==null) 
            return UNDEFINED;
        return Strings.toUSAscii(getRoleName());
    }

    public String getGenerationFullName() {
        return start.getGenerationFullName()+"."+getGenerationName();
    }

    public String getRoleName() {
        if (name==null || name.equals("")) {
            if (end==null) {
                return UNDEFINED;
            } else {
                if (isMultiple()) {
                    return NamingConventions.maybeLowerFirst(
                        Projects.plurals.getPlural(end.getName()));
                } else {
                    return NamingConventions.maybeLowerFirst(end.getName());
                }
            }
        } else {
            return name;
        }
    }

    public Type getType() {
        if (end == null) 
            return null;
        if (isMultiple()) {
            if (primaryKey!=null)
                return Projects.types.resolveType("HashMap","java.util");
            else
                return Projects.types.resolveType("Vector","java.util");
        } else {
            return (Type)end;
        }
    }

    public Type getAbstractType() {
        if (end == null) 
            return null;
        if (isMultiple()) {
            if (primaryKey!=null)
                return Projects.types.resolveType("Map","java.util");
            else
                return Projects.types.resolveType("List","java.util");
        } else {
            return (Type)end;
        }
    }

    public boolean isMultiple() {
        if( cardinality!=null && (
            cardinality.equals("*") || cardinality.equals("0-*")||
            cardinality.equals("1-*"))) {
            return true;
        } else {
            return false;
        }
    }

    public String getPrototype() {
        if (end==null) 
            return UNDEFINED;
        String role = getGenerationName();
        String prototype = (getAbstractType().getGenerationFullName())+" "+role;
        if (isMultiple()) {
            return prototype+" = new "+getType().getGenerationFullName()+"()";
        } else {
            return prototype;
        }
    }

    Method adder;
    public Method getAdder() {
        return adder;
    }
    public void setAdder(Method method) {
        this.adder = method;
        if (method!=null)
            method.setParent((Class)start);
    }

    /**
     * Initialize adder. Sets its name, parameters and return type.
     *
     * @param adder adder method to initialize
     */
    public void initAdder(Method adder) {
        adder.setType(Projects.types.resolveType("void"));
        adder.addParameter(new Parameter(NamingConventions.lowerFirst(end.getName()),(Type)end));
        adder.setName(CodeGeneration.getAdderName(getGenerationName()));
    }

    Method remover;
    public Method getRemover() {
        return remover;
    }
    public void setRemover(Method method) {
        this.remover = method;
        if (method!=null)
            method.setParent((Class)start);
    }
    /**
     * Initialize remover. Sets its name, parameters and return type.
     *
     * @param remover remover method to initialize
     */
    public void initRemover(Method remover) {
        remover.setType(Projects.types.resolveType("void"));
        remover.addParameter(new Parameter(NamingConventions.lowerFirst(end.getName()),(Type)end));
        remover.setName(CodeGeneration.getRemoverName(getGenerationName()));
    }

    Method clearer;
    public Method getClearer() {
        return clearer;
    }
    public void setClearer(Method method) {
        this.clearer = method;
        if (method!=null)
            method.setParent((Class)start);
    }
    /**
     * Initialize clearer. Sets its name, parameters and return type.
     *
     * @param clearer clearer method to initialize
     */
    public void initClearer(Method clearer) {
        clearer.setType(Projects.types.resolveType("void"));
        clearer.setName(CodeGeneration.getClearerName(getGenerationName()));
    }

    Method getter;
    public Method getGetter() {
        return getter;
    }
    public void setGetter(Method method) {
        this.getter = method;
        if (method!=null)
            method.setParent((Class)start);
    }
    /**
     * Initialize getter. Sets its name, and return type.
     *
     * @param getter getter method to initialize
     */
    public void initGetter(Method getter) {
        getter.setType(getAbstractType());
        getter.setName(CodeGeneration.getGetterName(getGenerationName()));
    }

    public boolean isNavigable() {
        RelationLink rel = (RelationLink)link;
        if (rel.getOrientation()==RelationLink.ORIENTATION_BOTH) {
            return true;
        } else {
            if (link.getStartRole()==this) {
                return rel.getOrientation()==RelationLink.ORIENTATION_STRAIGHT;
            } else {
                return rel.getOrientation()==RelationLink.ORIENTATION_REVERSE;
            }
        }
    }

    public boolean isAggregation() {
        RelationLink rel = (RelationLink)link;
        return rel.isAggregation() && isStartRole();
    }   

    /** Field or RelationRole */
    Typed primaryKey;
    public Typed getPrimaryKey() {
        return primaryKey;
    }
    public void setPrimaryKey(Typed primaryKey) {
        this.primaryKey = primaryKey;
    }

    /**
     * Returns the name of the getter method.
     */
    public String getRemoverName() {
        if (remover!=null) {
            return remover.getGenerationName();
        } else {
            return CodeGeneration.getRemoverName(getGenerationName());
        }
    }

    public Collection primaryKeyChoices() {
        if (end==null) {
            return new Vector(0);
        } else {
            Class cl = (Class)end;
            Vector result = new Vector();
            result.addAll(cl.getReferenceRoles());
            result.addAll(cl.getAllFields());
            return result;
        }
    }

    public Collection methodChoices() {
        ClassItem cl = ClassRepository.get().getClass(Method.class);
        FieldItem field = cl.getField("parent");
        return ObjectRepository.getObjectsWhere(cl,field,null);
    }
}

