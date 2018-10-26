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

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import org.objectweb.jac.util.File;
import org.objectweb.jac.util.Strings;

public class Project extends ModelElement {

    public void checkGenerationPath() throws CannotGenerateException {
        if (generationPath==null) {
            throw new CannotGenerateException(
                "Project \""+name+"\" does not have a generation path");
        }
    }

    boolean useToolsJavac = false;
    public void setUseToolsJavac(boolean value) {
        useToolsJavac = value;
    }
    public boolean getUseToolsJavac() {
        return useToolsJavac;
    }

    File compilerCommand = new File("javac");
   
    /**
     * Get the value of compilerCommand.
     * @return value of compilerCommand.
     */
    public File getCompilerCommand() {
        return compilerCommand;
    }
   
    /**
     * Set the value of compilerCommand.
     * @param v  Value to assign to compilerCommand.
     */
    public void setCompilerCommand(File  v) {
        this.compilerCommand = v;
    }

    String compilerOptions;
    public String getCompilerOptions() {
        return compilerOptions;
    }
    public void setCompilerOptions(String options) {
        this.compilerOptions = options;
    }

    File generationPath;

    /**
     * Get the value of generationPath.
     * @return value of generationPath.
     */
    public File getGenerationPath() {
        return generationPath;
    }
   
    /**
     * Set the value of generationPath.
     * @param v  Value to assign to generationPath.
     */
    public void setGenerationPath(File  v) {
        this.generationPath = v;
    }   

    /**
     * Returns the directory where the .class files should be stored
     */
    public File getClassesDir() {
        return new File(generationPath,"classes");
    }
    
    public File getManifestDir() {
        return new File(generationPath,"META-INF");
    }    

    Vector packages = new Vector();
   
    /**
     * Get the value of packages.
     * @return value of packages.
     */
    public List getPackages() {
        return packages;
    }
   
    public void addPackage(Package p) {
        packages.add(p);
    }
   
    public void removePackage(Package p) {
        packages.remove(p);
    }
   
    /**
     * Gets a package with a given name
     * @param packageName the requested package name
     */
    public Package getPackageByName(String packageName) {
        Iterator it = packages.iterator();
        while (it.hasNext()) {
            Package pkg = (Package)it.next();
            if (pkg.getName().equals(packageName))
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

    Vector applications = new Vector();
   
    /**
     * Get the value of applications.
     * @return value of applications.
     */
    public List getApplications() {
        return applications;
    }
   
    public void addApplication(Application a) {
        applications.add(a);
        a.addAspectConfiguration(new AspectConfiguration("rtti"));
    }

    public void removeApplication(Application a) {
        applications.remove(a);
    }

    /** List of File */
    List classpath = new Vector();
    public List getClasspath() {
        return classpath;
    }
    public String getClasspathString() {
        return Strings.join(classpath,System.getProperty("path.separator"));
    }
    public void addClasspath(File path) {
        classpath.add(path);
    }
    public void removeClasspath(File path) {
        classpath.remove(path);
    }

    /**
     * Find a class by its full name (my.package.ClassName)
     * @param className the class name to find
     */
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
            return null;
        }
    }

    /**
     * Returns all classes of all packages of the project.
     */
    public Collection getClasses() {
        Vector classes = new Vector();
        Iterator it = packages.iterator();
        while (it.hasNext()) {
            Package pkg = (Package)it.next();
            classes.addAll(pkg.getAllClasses());
        }
        return classes;
    }

    /**
     * Returns all resources of all packages of the project.
     */
    public Map getAllResources() {
        Hashtable resources = new Hashtable();
        Iterator it = packages.iterator();
        while (it.hasNext()) {
            Package pkg = (Package)it.next();
            resources.putAll(pkg.getAllResources());
        }
        return resources;
    }

    Map externalFiles = new Hashtable();
    /**
     * Add an external file to include in the JAR
     * @param name name of the file in the JAR
     * @param file the file to include
     */
    public void addExternalFile(String name, File file) {
        externalFiles.put(name,file);
    }
    public void removeExternalFile(String name) {
        externalFiles.remove(name);
    }
    public Map getExternalFiles() {
        return externalFiles;
    }

    /**
     * Remove "dangling" Roles (whose start or end is null)
     */
    public void cleanupModel() {
        Iterator i = getClasses().iterator();
        while (i.hasNext()) {
            Class cl = (Class)i.next();
            Iterator j = cl.getEndingLinks().iterator();
            while (j.hasNext()) {
                Role role = (Role)j.next();
                if (role.getEnd()==null || role.getStart()==null) {
                    j.remove();
                }
            }
        }
    }
}
