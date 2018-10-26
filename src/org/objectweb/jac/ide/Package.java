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
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import org.objectweb.jac.core.ObjectRepository;
import org.objectweb.jac.lib.Attachment;

public class Package extends ModelElement {

    Project project;
    public Project getProject() {
        if (parent!=null) {
            return parent.getProject();
        } else {
            return project;
        }
    }
    public void setProject(Project project) {
        this.project = project;
    }

    Package parent;
   
    /**
     * Get the value of parent.
     * @return value of parent.
     */
    public Package getParent() {
        return parent;
    }
   
    /**
     * Set the value of parent.
     * @param v  Value to assign to parent.
     */
    public void setParent(Package  v) {
        this.parent = v;
    }

    public String getPath() {
        if (parent == null) {
            return name;
        } else {
            return parent.getPath()+"/"+name;
        }
    }

    public String getPPath() {
        if( parent == null ) {
            return name;
        } else {
            return parent.getPPath()+"."+name;
        }
    }
   
    List subPackages=new Vector();
    public static String packagePathToFile(String path) {
        return path.replace('.',System.getProperty("file.separator").charAt(0));
    }

    /**
     * Get the value of packages.
     * @return value of packages.
     */
    public List getSubPackages() {
        return subPackages;
    }
   
    /**
     * Set the value of packages.
     * @param v  Value to assign to packages.
     */
    public void setSubPackages(Vector  v) {
        this.subPackages = v;
    }
   
    public void addSubPackage(Package p) {
        subPackages.add(p);
        //p.setParent(this); 
    }
   
    public void removeSubPackage(Package p) {
        subPackages.remove(p);
    }

    public Package getPackageByName(String pkgName) {
        Iterator it = subPackages.iterator();
        while (it.hasNext()) {
            Package pkg = (Package)it.next();
            if (pkg.getName().equals(pkgName))
                return pkg;
        }
        return null;
    }
   
    /**
     * Find a package by its full name (my.package.ClassName)
     * @param pkgName the package name to find
     */
    public Package findPackage(String pkgName) {
        int dot = pkgName.indexOf('.');
        if (dot!=-1) {
            Package pkg = getPackageByName(pkgName.substring(0,dot));
            if (pkg!=null)
                return pkg.findPackage(pkgName.substring(dot+1));
            else 
                return null;
        } else {
            return getPackageByName(pkgName);
        }
    }

    List diagrams = new Vector();
   
    /**
     * Get the value of diagrams.
     * @return value of diagrams.
     */
    public List getDiagrams() {
        return diagrams;
    }
   
    /**
     * Set the value of diagrams.
     * @param v  Value to assign to diagrams.
     */
    public void setDiagrams(Vector  v) {
        this.diagrams = v;
    }
   
    public void addDiagram(Diagram d) {
        //d.setContainer(this);
        diagrams.add(d);
    }

    public void removeDiagram(Diagram d) {
        diagrams.remove(d);
    }

    List classes = new Vector();
   
    /**
     * Get the value of classes.
     * @return value of classes.
     */
    public List getClasses() {
        return classes;
    }
   
    /**
     * Set the value of classes.
     * @param v  Value to assign to classes.
     */
    public void setClasses(Vector  v) {
        this.classes = v;
    }
   
    public void addClass(Class c) {
        classes.add(c);
    }

    public void addInterface(Interface i) {
        classes.add(i);
    }
   
    public void removeClass(Class c) {
        // this part should be handled by integrity
        c.getLinks().clear();

        Collection diagrams = ObjectRepository.getObjects(Diagram.class);
        Iterator it = diagrams.iterator();
        while(it.hasNext()) {
            Diagram d = (Diagram)it.next();
            d.removeElement(c);
        }
        classes.remove(c);
    }

    /**
     * Add a repository for a class. Creates a relation, a singleton
     * static field and static method to initialize the singleton.
     */
    public void addRepository(Class itemClass) {
        Repository repository = new Repository(itemClass);
        addClass(repository);

        RelationLink relation = new RelationLink(repository,itemClass);
        ((RelationRole)relation.getStartRole()).setCardinality("0-*");
        ((RelationRole)relation.getEndRole()).setCardinality("1");
        relation.setOrientation(RelationLink.ORIENTATION_STRAIGHT);
        relation.setAggregation(true);
        repository.setItemsRole((RelationRole)relation.getStartRole());

        Field singleton = new Field();
        singleton.setName("singleton");
        singleton.setStatic(true);
        singleton.setType(repository);
        repository.addField(singleton);

        Method init = new Method();
        init.setName("init");
        init.setStatic(true);
        init.setBody("setSingleton(new "+repository.getGenerationName()+"());");
        repository.addMethod(init);
    }

    /**
     * Returns all classes of this package or a subpackage of it.
     */
    public Collection getAllClasses() {
        Vector result = new Vector();
        result.addAll(classes);
        Iterator it = subPackages.iterator();
        while (it.hasNext()) {
            Package pkg = (Package)it.next();
            result.addAll(pkg.getAllClasses());
        }
        return result;
    }


    /**
     * Returns all resources of this package or a subpackage of it, in
     * a Map whose keys are packages and values are Attachements. 
     */
    public Map getAllResources() {
        Hashtable result = new Hashtable();
        Iterator it = resources.iterator();
        while (it.hasNext()) {
            result.put(this,it.next());
        }
        it = subPackages.iterator();
        while (it.hasNext()) {
            Package pkg = (Package)it.next();
            result.putAll(pkg.getAllResources());
        }
        return result;
    }

    /**
     * Gets a class by its name.
     * @param className the requested class name
     * @return of class of the package whose name or generation name is
     * className, or null 
     */
    public Class getClassByName(String className) {
        Iterator it = classes.iterator();
        while (it.hasNext()) {
            Class cl = (Class)it.next();
            if (cl.getName().equals(className) || cl.getGenerationName().equals(className))
                return cl;
        }
        return null;
    }

    /**
     * Gets a class by its name. Subpackages are searched recursively.
     * @param className the requested class name (partial fully
     * qualified class name:
     * &lt;sub_pkg1&gt;.&lt;sub_pkg2&gt;.&lt;class_name&gt;
     * @return a class of the package whose name is className, or null */
    public Class findClass(String className) {
        int dot = className.indexOf('.');
        if (dot!=-1) {
            String packageName = className.substring(0,dot);
            Package pkg = getPackageByName(packageName);
            if (pkg!=null)
                return pkg.findClass(className.substring(dot+1));
            else
                return null;
        } else {
            return getClassByName(className);
        }
    }

    public void addAspect(Aspect a) {
        classes.add(a);
    }

    List instances = new Vector();
   
    /**
     * Get the value of instances.
     * @return value of instances.
     */
    public List getInstances() {
        return instances;
    }
   
    /**
     * Set the value of instances.
     * @param v  Value to assign to instances.
     */
    public void setInstances(Vector  v) {
        this.instances = v;
    }   
    public void addInstance(Instance i) {
        instances.add(i);
    }
    public void removeInstance(Instance i) {
        instances.remove(i);
    }

    List groups = new Vector();
   
    /**
     * Get the value of groups.
     * @return value of groups.
     */
    public List getGroups() {
        return groups;
    }
    public void addGroup(Group g) {
        this.groups.add(g);
    }
    public void removeGroup(Group g) {
        this.groups.remove(g);
    }

    List resources = new Vector();
    public List getResources() {
        return resources;
    }
    public void addResource(Attachment resource) {
        resources.add(resource);
    }
    public void removeResource(Attachment resource) {
        resources.remove(resource);
    }

    public String getFullName() {
        return getPPath();
    }

    /**
     * Returns available main classes (Classes wich have a static void
     * main(String[]) method)
     */
    public Collection getMainClasses() {
        Vector mainClasses = new Vector();
        Vector parameters = new Vector();
        parameters.add(Projects.types.resolveType("String","java.lang"));
        Iterator it = classes.iterator();
        while (it.hasNext()) {
            Class cl = (Class)it.next();
            if (cl.findMethod("main",parameters)!=null)
                mainClasses.add(cl);
        }

        it = subPackages.iterator();
        while (it.hasNext()) {
            Package pkg = (Package)it.next();
            mainClasses.addAll(pkg.getMainClasses());
        }
        return mainClasses;
    }

}
