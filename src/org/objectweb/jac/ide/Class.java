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

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import org.objectweb.jac.aspects.gui.DisplayContext;
import org.objectweb.jac.aspects.gui.HandlerResult;
import org.objectweb.jac.core.ObjectRepository;
import org.objectweb.jac.util.Strings;

/**
 * This class represents a class meta element. 
 */
public class Class extends Type {

    public String getGenerationName() {
        return Strings.toUSAscii(getName());
    }

    Type superClass;
    /**
     * Get the value of superClass.
     * @return value of superClass.
     */
    public Type getSuperClass() {
        return superClass;
    }
    /**
     * Set the value of superClass.
     * @param v  Value to assign to superClass.
     */
    public void setSuperClass(Type v) {
        if (v!=superClass && superClass!=null) {
            // Remove existing inheritance links from diagrams
            // This should rather be done with an aspect
            Collection diagrams = ObjectRepository.getObjects(Diagram.class);
            Iterator it = diagrams.iterator();
            while(it.hasNext()) {
                Diagram d = (Diagram)it.next();
                if (superClass instanceof Class)
                    d.removeInheritanceLink(this,(Class)superClass);
            }         
        }

        this.superClass = v;
    }

    /**
     * Finds a field with a given name
     * @param name field name
     * @return a field with the given name, or null
     */
    public Field findField(String name) {
        Iterator it = getFields().iterator();
        while (it.hasNext()) {
            Field f = (Field)it.next();
            if (f.getName().equals(name)) {
                return f;
            }
        }
        return null;
    }

    Vector fields = new Vector();
    /**
     * Get the value of fields.
     * @return value of fields.
     */
    public List getFields() {
        return fields;
    }
    public void addField(Field f) {
        fields.add(f);
    }
    public void removeField(Field f) {
        fields.remove(f);
    }

    /**
     * Returns all fields, including inherited ones
     */
    public List getAllFields() {
        Vector result = new Vector();
        if (superClass instanceof Class)
            result.addAll(((Class)superClass).getAllFields());
        result.addAll(getFields());
        return result;
    }

    Vector methods = new Vector();
    /**
     * Get the value of methods.
     * @return value of methods.
     */
    public List getMethods() {
        return methods;
    }
    public void addMethod(Method m) {
        methods.add(m);
    }
    public void removeMethod(Method m) {
        methods.remove(m);
    }
    public void addConstructor(Constructor c) {
        methods.add(c);
    }

    /**
     * Gets al methods, including specific
     * getter,setters,adders,removers and clearers.
     */
    public List getAllMethods() {
        Vector allMethods = new Vector();
        allMethods.addAll(methods);
        Method m;
        Iterator it = fields.iterator();
        while (it.hasNext()) {
            Field field = (Field)it.next();
            if ((m=field.getSetter())!=null)
                allMethods.add(m);
            if ((m=field.getGetter())!=null)
                allMethods.add(m);
        }

        it = getNavigableRoles().iterator();
        while (it.hasNext()) {
            RelationRole role = (RelationRole)it.next();
            if ((m=role.getGetter())!=null)
                allMethods.add(m);
            if ((m=role.getAdder())!=null)
                allMethods.add(m);
            if ((m=role.getRemover())!=null)
                allMethods.add(m);
            if ((m=role.getClearer())!=null)
                allMethods.add(m);
        }
        return allMethods;
    }

    public List getInheritedMethods() {
        if (superClass instanceof Class) {
            List superMethods = ((Class)superClass).getAllMethods();
            List superInheritedMethods = ((Class)superClass).getInheritedMethods();         
            superMethods.addAll(superInheritedMethods);
            return superMethods;
        } else {
            return new Vector();
        }
    }

    public List getAbstractMethods() {
        Vector abstractMethods = new Vector();
        Iterator i = methods.iterator();
        while (i.hasNext()) {
            Method m = (Method)i.next();
            if (m.isAbstract()) {
                Method m2 = findMethod(m);
                if (m2==null || m2==m)
                    abstractMethods.add(m);
            }
        }
        if (superClass instanceof Class) {
            List superMethods = ((Class)superClass).getAbstractMethods();
            i = superMethods.iterator();
            while (i.hasNext()) {
                Method m = (Method)i.next();
                Method m2 = findMethod(m);
                if (m2==null || m2==m)
                    abstractMethods.add(m);
            }
        } 
        return abstractMethods;
    }

    /**
     * Finds a method with a given name and parameters
     * @param name method name
     * @param parameters the types of the parameters. 
     * @return a method with the given name and parameter types, or null 
     * @see #findMethod(Method)
     */
    public Method findMethod(String name, List parameters) {
        Iterator it = getAllMethods().iterator();
        while (it.hasNext()) {
            Method m = (Method)it.next();
            if (m.getName().equals(name)) {
                if (Arrays.asList(m.getParameterTypes()).equals(parameters)) {
                    return m;
                }
            }
        }
        return null;
    }

    /**
     * Finds a method with the same name and the same parameter types
     * as a given method.
     * @param method method whose name and parameter types must match
     * @return a method with the same name and the same parameter types, or null
     * @see #findMethod(String,List)
     */
    public Method findMethod(Method method) {
        return findMethod(method.getName(),Arrays.asList(method.getParameterTypes()));
    }

    public void addMethodIntf(Method m) {
        addMethod(m);
    }

    public void removeMethodIntf(Method m) {
        removeMethod(m);
    }

    /**
     * Adds a "public static void main(String[] parameters)" method
     */
    public void addMainMethod() {
        Method main = new Method();
        main.setName("main");
        main.setVisibility(Visibility.PUBLIC);
        main.setStatic(true);
        Parameter param = new Parameter();
        param.setName("parameters");
        param.setType(Projects.types.resolveType("String", "java.lang"));
        param.setArray(true);
        main.addParameter(param);
        addMethod(main);
    }

    Package container;
    /**
     * Get the value of container.
     * @return value of container.
     */
    public Package getContainer() {
        return container;
    }
    /**
     * Set the value of container.
     * @param v  Value to assign to container.
     */
    public void setContainer(Package  v) {
        this.container = v;
    }

    public String getFullName() {
        if (container == null) {
            return name;
        } else {
            return container.getPPath()+"."+name;
        }
    }

    public String getGenerationFullName() {
        if (container == null) {
            return getGenerationName();
        } else {
            return container.getPPath()+"."+getGenerationName();
        }
    }


    /**
     * Gets navigable roles
     * @return a collection of RelationRole
     */
    public Collection getNavigableRoles() {
        Vector result = new Vector();
        Iterator it = links.iterator();
        while (it.hasNext()) {
            Role role = (Role)it.next();
            if (role instanceof RelationRole && ((RelationRole)role).isNavigable())
                result.add(role);
        }
        return result;
    }

    /**
     * Gets all navigable roles, including those form inherited classes.
     */
    public Collection getAllNavigableRoles() {
        Vector result = new Vector();
        if (superClass instanceof Class)
            result.addAll(((Class)superClass).getAllNavigableRoles());
        result.addAll(getNavigableRoles());
        return result;
    }

    /**
     * Gets all navigable reference roles (whose cardinality is 1 or
     * 0-1), including those form inherited classes.
     */
    public Collection getReferenceRoles() {
        Vector result = new Vector();
        if (superClass instanceof Class)
            result.addAll(((Class)superClass).getReferenceRoles());

        Iterator it = links.iterator();
        while (it.hasNext()) {
            Role role = (Role)it.next();
            if (role instanceof RelationRole) {
                RelationRole relRole = (RelationRole)role;
                if (relRole.isNavigable() && !relRole.isMultiple()) {
                    result.add(role);
                }
            }
        }

        return result;
    }

    public Collection getRelationRoles() {
        Vector result = new Vector();
        Iterator it = links.iterator();
        while (it.hasNext()) {
            Role role = (Role)it.next();
            if (role instanceof RelationRole)
                result.add(role);
        }
        return result;
    }

    public Collection getRelationLinks() {
        Vector result = new Vector();
        Iterator it = links.iterator();
        while (it.hasNext()) {
            Role role = (Role)it.next();
            if (role instanceof RelationRole)
                result.add(role.getLink());
        }
        return result;
    }

    public Project getProject() {
        if (container!=null)
            return container.getProject();
        else 
            return null;
    }

    Vector imports = new Vector();
    public List getImports() {
        return imports;
    }
    public void addImport(String _import) {
        imports.add(_import);
    }
    public void removeImport(String _import) {
        imports.remove(_import);
    }

    boolean isAbstract = false;
    public boolean isAbstract() {
        return isAbstract;
    }
    public void setAbstract(boolean value) {
        isAbstract = value;
    }

    Set interfaces = new HashSet();
    public Set getInterfaces() {
        return interfaces;
    }
    public void addInterface(Interface _interface) {
        interfaces.add(_interface);
    }
    public void removeInterface(Interface _interface) {
        interfaces.remove(_interface);
    }

    /**
     * Adds the necessary fields and methods to implement a interface
     * @param intf the interface to implement
     */
    public void implementInterface(Interface intf) {
        Iterator it = intf.getAllFields().iterator();
        while (it.hasNext()) {
            Field intfField = (Field)it.next();
            if (findField(intfField.getName())!=null)
                continue;
            Field field = new Field();
            field.setName(intfField.getName());
            field.setType(intfField.getType());
            field.setArray(intfField.isArray());
            addField(field);
        }

        it = intf.getAllMethods().iterator();
        while (it.hasNext()) {
            Method intfMethod = (Method)it.next();
            if (findMethod(intfMethod)!=null)
                continue;
            addMethod(intfMethod.cloneMethod());
        }
    }

    /**
     * Adds an overriding method
     * @param method method to override
     */
    public void overrideMethod(Method method) {
        addMethod(method.cloneMethod());
    }

    /**
     * Adds an implementation for an abstract method
     * @param method abstract method to implement
     */    
    public void implementMethod(Method method) {
        addMethod(method.cloneMethod());
    }

    public HandlerResult gotoLine(DisplayContext context,int lineNumber) {
        Map lineNumbers = CodeGeneration.getClassLineNumbers(this);
        if (lineNumbers.isEmpty()) {
            throw new RuntimeException("No line number information available. Please (re)generate code.");
        }
        Member member = null;
        Iterator i = lineNumbers.entrySet().iterator();
        int closestLine = 0;
        while (i.hasNext()) {
            Map.Entry entry = (Map.Entry)i.next();
            int line = ((Integer)entry.getValue()).intValue();
            if (lineNumber > line && line > closestLine) {
                closestLine = line;
                member = (Member)entry.getKey();
            }
        }
        if (member!=null) {
            if (member instanceof Method) {
                Method method = (Method)member;
                return new HandlerResult(
                    context.getDisplay().getCustomizedView("ide"),
                    org.objectweb.jac.core.rtti.ClassRepository.get()
                    .getClass(org.objectweb.jac.ide.Class.class).getField("methods"),
                    method,"body",new Integer(lineNumber-closestLine-1));
            } else {
                return new HandlerResult(
                    context.getDisplay().getCustomizedView("ide"),
                    org.objectweb.jac.core.rtti.ClassRepository.get()
                    .getClass(org.objectweb.jac.ide.Class.class).getField("fields"),
                    member,null,null);
            }
        } else {
            return null;        
        }
    }
}

