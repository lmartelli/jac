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

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import org.objectweb.jac.aspects.gui.swing.SHEditorConfig;
import org.objectweb.jac.aspects.export.ExportAC;
import org.objectweb.jac.aspects.export.Importer;
import org.objectweb.jac.aspects.timestamp.Timestamps;
import org.objectweb.jac.core.ACManager;
import org.objectweb.jac.core.Wrappee;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.util.File;
import org.xml.sax.SAXException;

public class Projects {
    public static transient Projects root;
    public static transient TypeRepository types;
    public static transient Plurals plurals;
    public static transient Timestamps stamps;
    public static transient Preferences prefs;

    public static Boolean notPrimitiveType(Wrappee substance,
                                           FieldItem field,
                                           Object value,
                                           Object[] values)
    {
        Iterator it = Projects.types.getPrimitiveTypes().iterator();
        while(it.hasNext()) {
            Type type = (Type)it.next();
            if (((String)value).equals(type.getName())) {
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    /**
     * Handle upgrading from class with no package to a class with
     * package, for compatibility with 0.10 versions
     * @param name name the class to add
     * @param pkg package name of the class to add
     */
    private static Type initExternalClass(String name, String pkg) {
        Type type = types.resolveType(name, "");
        if (type!=null) {
            type.setPackagePath(pkg);
        } else {
            type = types.resolveType(name,pkg);
            if (type==null) {
                type = new Type(name,pkg);
                types.addExternalClass(type);
            }
        }
        return type;
    }

    private static ExtendedType initExtendedType(String name, Type realType) {
        Type type = types.resolveType(name, "");
        if (type!=null) {
            if (type instanceof ExtendedType)
                ((ExtendedType)type).setRealType(realType);
            else {
                System.err.println("Warning: there's alreayd a type named "+
                                   name+", but it's not an extended type");
                return null;
            }
        } else {
            type = new ExtendedType(name,realType);
            types.addExtendedType((ExtendedType)type);
        }
        return (ExtendedType)type;
    }

    private static Type initPrimitiveType(String name) {
        Type type = types.resolveType(name, "");
        if (type==null) {
            type = new Type(name,"");
            types.addPrimitiveType(type);
        }
        return type;
    }

    public static void main(String[] args) {
        root = new Projects();
        types = new TypeRepository();
        plurals = new Plurals();
        stamps = new Timestamps();
        prefs = new Preferences();
        prefs.setEditorPrefs(new SHEditorConfig());

        // create the primitive types if needed

        initPrimitiveType("void");
        initPrimitiveType("boolean");
        initPrimitiveType("int");
        initPrimitiveType("long");
        initPrimitiveType("float");
        initPrimitiveType("double");

        // create some useful external classes
        initExternalClass("Object","java.lang");
      
        Type type = initExternalClass("String","java.lang");
        initExtendedType("text",type);
        initExtendedType("email",type);
        initExtendedType("password",type);
        initExtendedType("javaCode",type);
        initExtendedType("accCode",type);
      
        type = initExternalClass("float","");
        initExtendedType("percentage",type);

        type = initExternalClass("File","java.io");
        initExtendedType("directory",type);
      
        type = initExternalClass("URL","java.net");
        initExtendedType("directoryURL",type);
        initExtendedType("imageURL",type);
      
        type = initExternalClass("Date","java.util");
        initExtendedType("dateHour",type);
      
        type = initExternalClass("Vector","java.util");
        type = initExternalClass("List","java.util");
        type = initExternalClass("Map","java.util");
        type = initExternalClass("HashMap","java.util");
        type = initExternalClass("Set","java.util");
        type = initExternalClass("HashSet","java.util");
        type = initExternalClass("Collection","java.util");
        type = initExternalClass("Reader","java.io");
        type = initExternalClass("Writer","java.io");
        type = initExternalClass("InputStream","java.io");
        type = initExternalClass("OutputStream","java.io");
    }

    Vector projects = new Vector();
   
    /**
     * Get the value of projects.
     * @return value of projects.
     */
    public List getProjects() {
        return projects;
    }
   
    public void addProject(Project p) {
        projects.add(p);
    }
   
    public void removeProject(Project p) {
        projects.remove(p);
    }

    public String toString() {
        return "projects";
    }

    Application currentApplication = null;
    public void setCurrentApplication(Application application) {
        currentApplication = application;
    }
    public Application getCurrentApplication() {
        return currentApplication;
    }

    /**
     * Starts the current application
     */
    public void startCurrentApplication() throws IOException {
        if (currentApplication!=null) {
            currentApplication.start();
        }
    }

    /**
     * Stops the current application
     */
    public void stopCurrentApplication() {
        if (currentApplication!=null) {
            currentApplication.stop();
        }
    }

    public boolean isNotStarted() {
        return currentApplication!=null && currentApplication.isNotStarted();
    }

    public boolean isStarted() {
        return currentApplication!=null && currentApplication.isStarted();      
    }

    /**
     * Exports projects to an XML file
     * @param f the file to export to
     */
    public static void export(File f) throws IOException, Exception {
        ExportAC exportAC = (ExportAC)ACManager.getACM().getACFromFullName("ide.export");
        if (exportAC==null) {
            throw new Exception("No export aspect found");
        } else {
            exportAC.export(f);
        }
    }

    public static void importObjects(File f) throws SAXException, IOException {
        Importer importer = new Importer();
        importer.importObjects(f);
    }
}
