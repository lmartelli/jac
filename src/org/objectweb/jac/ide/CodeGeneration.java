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

import com.sun.tools.javac.Main;
import gnu.regexp.RE;
import gnu.regexp.REException;
import gnu.regexp.REMatch;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.rtti.MethodItem;
import org.objectweb.jac.core.rtti.NamingConventions;
import org.objectweb.jac.lib.Attachment;
import org.objectweb.jac.util.Classes;
import org.objectweb.jac.util.File;
import org.objectweb.jac.util.Files;
import org.objectweb.jac.util.LineNumberWriter;
import org.objectweb.jac.util.Streams;
import org.objectweb.jac.util.Strings;

public class CodeGeneration {
    static Logger logger = Logger.getLogger("ide.compile");

    /**
     * Generate code for all the classes and aspects of a project.
     *
     * @param project the project
     * @param baseDir base directory where to put generated files
     */
    public static void createProjectCode(Project project, String baseDir) 
        throws CannotGenerateException, IOException
    {
        createJavaCode(project,baseDir);
        Iterator it = project.getApplications().iterator();
        while(it.hasNext()) {
            Application cur = (Application)it.next();
            createApplicationCode(project, cur, new File(baseDir,cur.getGenerationName()));
        }
        print("generation completed for "+project.getName()+".");
    }

    /**
     * Generate java sources for a project
     * @param project a project
     * @param baseDir directory where to generate files
     */
    public static void createJavaCode(Project project, String baseDir) 
        throws CannotGenerateException, IOException, FileNotFoundException
    {
        Iterator it = Projects.types.getEnumeratedTypes().iterator();
        while(it.hasNext()) {
            EnumeratedType enum = (EnumeratedType)it.next();
            createEnumCode(enum, new File(baseDir,enum.getPackagePath()));
        }
        it = project.getPackages().iterator();
        while(it.hasNext()) {
            Package cur = (Package)it.next();
            createPackageCode(project, cur,
                              new File(baseDir,cur.getGenerationName()),
                              cur.getGenerationName());
        }        
    }

    /**
     * Generate java sources for an enumerated type.
     *
     * @param enum an enumerated type
     * @param dir directory where to generate the file
     */
    public static void createEnumCode(EnumeratedType enum, File dir) throws IOException {
        print("Generating code for enum "+enum.getName()+".");
        dir.mkdirs();
        String enumName = Strings.toUSAscii(enum.getName());
        FileWriter out = new  FileWriter(new File(dir, enumName+".java"));
        try {
            out.write("package "+enum.getPackagePath()+";\n");
            out.write("public interface "+enumName+" {\n");
            int value = enum.getStartValue();
            int step = enum.getStep();
            Iterator it = enum.getNames().iterator();
            while (it.hasNext()) {
                StringBuffer nameBuf = new StringBuffer((String)it.next());
                Strings.toUSAscii(nameBuf);
                Strings.toUpperCase(nameBuf);
                Strings.replace(" '-/+",'_',nameBuf);
                Strings.deleteChars("()[]",nameBuf);
                String name = nameBuf.toString();
                // remove unwanted characters such as '.'
                StringBuffer buffer = new StringBuffer(name.length());
                for (int i=0; i<name.length(); i++) {
                    char c = name.charAt(i);
                    if (c != '.')
                        buffer.append(c);
                }
                name = buffer.toString();
                out.write("    int "+(Character.isDigit(name.charAt(0))?"_":"")+name+" = "+value+";\n");
                value += step;
            }
            out.write("}\n");
        } finally {
            out.close();
        }
    }

    /**
     * Generate code for an application (<code>Run</code> and aspect
     * configurations)
     *
     * @param prj the project
     * @param app the application
     * @param baseDir the base directory where to put the generated files
     */
    public static void createApplicationCode(Project prj,
                                             Application app,
                                             File baseDir) 
        throws CannotGenerateException, IOException
    {
        Iterator it = app.getAspectConfigurations().iterator();
        while (it.hasNext()) {
            AspectConfiguration ac = (AspectConfiguration) it.next();
            createACFile(prj,ac,baseDir);
            //createDefaultACFile(prj,ac,baseDir);
        }
        createJACFile(prj,app,baseDir);
    }

    /**
     * Generate a warning that the file was automatically generated.
     *
     * @param writer writer where to write the warning
     */
    static void writeWarning(Writer fw) {
        try {
            fw.write("//\n"+
                     "// WARNING: this file has been automatically generated by JAC\n"+
                     "// DO NOT MODIFY unless you know what you are doing\n"+
                     "//\n");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // aspect name -> AspectPlugin
    static transient Map aspectPlugins = new Hashtable();
    static {
        aspectPlugins.put("rtti", new RttiPlugin());
        //aspectPlugins.put("integrity", new IntegrityPlugin());
        aspectPlugins.put("gui", new GuiPlugin());
        aspectPlugins.put("persistence", new PersistencePlugin());
    }

    /**
     * Generate aspect configuration code for an aspect of a project.
     *
     * @param project the project
     * @param ac the aspect configuration
     * @param dir directory where to put the file
     */
    static void createACFile(Project project,
                             AspectConfiguration ac, File dir) {
        try {
            File f = new File(dir,ac.getGenerationName()+".acc");
            if (f.exists())
                f.delete();
            f.getParentFile().mkdirs();
            f.createNewFile();
            FileWriter fw = new FileWriter(f);

            // Automatically generated config code
            if (aspectPlugins.containsKey(ac.getName())) {
                AspectPlugin plugin =
                    (AspectPlugin)aspectPlugins.get(ac.getName());
                plugin.genConfig(fw,project);
            }

            // common default configurations for all classes
            if (ac.getDefaultConfigurationCode()!=null &&
                !ac.getDefaultConfigurationCode().equals("")) {
                Collection classes = createClassesList(project);
                Iterator itClasses = classes.iterator();
                while(itClasses.hasNext()) {
                    String classname = (String)itClasses.next();
                    fw.write("class "+classname+" {\n");
                    fw.write(ac.getDefaultConfigurationCode());
                    fw.write("\n}\n");
                }
            }

            //Create Acc ConfigurationCode from the ConfigItem
            //First creating code from the Project
            List configItems = project.getConfigItems();
            Iterator ite = configItems.iterator();
            fw.write("//CodeGeneration : generate Project code\n");
            while (ite.hasNext()){
                ConfigItem configItem = (ConfigItem)ite.next();
                if (configItem.getAspectConfiguration()==ac){
                    fw.write(configItem.getMethod()+" "+translateParam(configItem));
                }
            }
            fw.write("//CodeGeneration : end of Project code\n\n");
            //Iterate on each package of the project
            ite = project.getPackages().iterator();
            fw.write("//CodeGeneration : generate Package code\n");
            while(ite.hasNext()){
                Package pack = (Package)ite.next();
                generateConfigItemPackageCode(pack,ac,fw);
            }
            fw.write("//CodeGeneration : end of Package code\n\n");
            // main config
            fw.write(ac.getConfigurationCode());
            fw.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Build a list of paths for all the classes of a project.
     *
     * @param project the project
     * @return a collection of Strings which are the paths of the
     * source files of the classes
     * @see createClassesList(Package,String)
     */
    static Collection createClassesList(Project project) {
        Vector result = new Vector();
        Iterator it = project.getPackages().iterator();
        while(it.hasNext()) {
            Package cur = (Package)it.next();
            result.addAll(createClassesList(cur,cur.getGenerationName()));
        }
        return result;
    }

    /**
     * Build a list of paths for all the classes of a package and its
     * subpackages.
     *
     * @param package the package
     * @return a collection of Strings which are the paths of the
     * source files of the classes
     */
    static Collection createClassesList(Package project,String baseDir) {
        Vector result = new Vector();
        Iterator it = project.getSubPackages().iterator();
        while(it.hasNext()) {
            Package cur = (Package)it.next();
            result.addAll(createClassesList(cur,baseDir+"."+cur.getGenerationName()));
        }
        it = project.getClasses().iterator();
        while(it.hasNext()) {
            Class cur = (Class)it.next();
            result.add(baseDir+"."+cur.getGenerationName());
        }
        return result;
    }

    /**
     * Generate the descriptor of an application (the .jac file).
     *
     * @param prj the project
     * @param app the applicatino
     * @param baseDir directory where to put the <code>.jac</code> file
     */

    static void createJACFile(Project prj, Application app, File baseDir) 
        throws CannotGenerateException, IOException
    {
        File f = app.getJacFile();
        if (f.exists())
            f.delete();
        f.getParentFile().mkdirs();
        f.createNewFile();
        FileWriter fw = new FileWriter(f);
         
        if (app.getMainClass()==null) {
            throw new CannotGenerateException("No main class defined.");
        }
        fw.write("applicationName: "+app.getName()+"\n"+
                 "launchingClass: "+app.getMainClass().getGenerationFullName()+"\n");

        if (app.getAspectConfigurations()!=null &&
            app.getAspectConfigurations().size()>0) {
            fw.write( "aspects: ");
            if (app.getAspectConfigurations().size()>0 &&
                !((AspectConfiguration)app.getAspectConfigurations().get(0))
                .getName().equals("rtti"))
            {
                fw.write( "rtti"
                          + " \"" + app.getGenerationName()
                          + "/" + "null.acc\""
                          + " true ");
            }
            Iterator it = app.getAspectConfigurations().iterator();
            while(it.hasNext()) {
                AspectConfiguration cur = (AspectConfiguration)it.next();
                if( ((org.objectweb.jac.core.ACManager)org.objectweb.jac.core.ACManager.get())
                    .isACDeclared(cur.getName()) ) {
                    fw.write( cur.getName()
                              + " \"" + app.getGenerationName()
                              + "/" + cur.getGenerationName() + ".acc\""
                              + " " + cur.isWoven() + " ");
                } else {
                    fw.write( cur.getAspect().getGenerationFullName()+"AC"
                              + " \"" + app.getGenerationName()
                              + "/" + cur.getGenerationName() + ".acc\""
                              + " " + cur.isWoven() + " ");
                }
            }
            fw.write("\n\n");
        }

        if (app.getHosts()!=null && app.getHosts().size()>0) {
            fw.write( "\ntopology: ");
            Iterator it = app.getHosts().iterator();
            while(it.hasNext()) {
                String cur=(String)it.next();
                fw.write(cur+" ");
            }
            fw.write("\n\n");
        }

        if (!app.getIgnoredClasses().isEmpty()) {
            fw.write("\njac.toNotAdapt: ");
            Iterator it = app.getIgnoredClasses().iterator();
            while(it.hasNext()) {
                String expr = (String)it.next();
                fw.write(expr+" ");
            }
            fw.write("\n\n");
        }

        fw.close();
    }

    /**
     * Generate code for the classes of a package and its subpackages.
     *
     * @param prj the project
     * @param pkg the package
     * @param baseDir directory where to put the files of the package
     * @param ppath java package path (<code>myapp.mypackage</code> for instance)
     */
    public static void createPackageCode(Project prj,
                                         Package pkg,
                                         File baseDir,
                                         String ppath) 
        throws FileNotFoundException, IOException
    {
        baseDir.mkdirs();

        Iterator it = pkg.getSubPackages().iterator();
        while (it.hasNext()) {
            Package cur = (Package)it.next();
            createPackageCode(prj, cur,
                              new File(baseDir,cur.getGenerationName()),
                              ppath+"."+cur.getGenerationName());
        }
        it = pkg.getClasses().iterator();
        while (it.hasNext()) {
            Class cur = (Class)it.next();
            if (cur instanceof Aspect) {
                createAspectCode(prj,(Aspect)cur,baseDir,ppath);
            } else {
                createClassCode(prj,cur,baseDir,ppath);
            }
        }

        it = pkg.getResources().iterator();
        while (it.hasNext()) {
            Attachment res = (Attachment)it.next();
            FileOutputStream out = new FileOutputStream(new File(baseDir,res.getName()));
            try {
                byte[] data = res.getData();
                out.write(data,0,data.length);
            } finally {
                out.close();
            }
        }
    }

    // Class -> ( Method -> (Integer)lineNumber )
    static transient Hashtable classesLineNumbers = new Hashtable();

    /**
     * Returns a Map giving the line number of methods in the generated
     * java source file.
     */
    public static Map getClassLineNumbers(Class c) {
        Map map = (Map)classesLineNumbers.get(c);
        if (map==null) {
            map = new Hashtable();
            classesLineNumbers.put(c,map);
        }
        return map;
    }

    /**
     * Generate the java source code of a class.
     *
     * @param prj the project
     * @param c the class
     * @param baseDir directory where to put the generated file
     * @param ppath java package path of the class
     */
    public static void createClassCode(Project prj, Class c,
                                       File baseDir, String ppath) {
        try {
            Map lineNumbers = getClassLineNumbers(c);
            lineNumbers.clear();
            print("generating "+c.getFullName());
            File f = new File(baseDir,c.getGenerationName()+".java");
            classFiles.put(f.getAbsolutePath(),c);
            if (f.exists())
                f.delete();
            f.getParentFile().mkdirs();
            f.createNewFile();
            String shortName = c.getGenerationName();
            LineNumberWriter output = new LineNumberWriter(Files.newFileWriter(f,"UTF-8"));
            writeWarning(output);
            output.write("\npackage "+ppath+";\n\n");
            String description = c.getDescription();

            HashSet generatedImports = new HashSet();
            // Automatic imports for field types
            Iterator it = c.getFields().iterator();
            while(it.hasNext()) {
                Type type = ((Field)it.next()).getType();
                if (!(type instanceof EnumeratedType) &&
                    type.getPackagePath()!=null && !type.getPackagePath().equals("") 
                    && !generatedImports.contains(type.getGenerationFullName())) {
                    output.write("import "+type.getGenerationFullName()+";\n");
                    generatedImports.add(type.getGenerationFullName());
                }
            }

            // Imports of the class
            it = c.getImports().iterator();
            while(it.hasNext()) {
                String imp = (String)it.next();
                if (!generatedImports.contains(imp)) {
                    output.write("import "+imp+";\n");
                    generatedImports.add(imp);
                }
            }

            if (description!=null && description.length()>0)
                output.write("\n/**\n"+description+"\n*/\n");
            boolean isInterface = c instanceof Interface;
            output.write("\npublic "+(c.isAbstract()?"abstract ":"")+
                         (isInterface?"interface ":"class ")+
                         shortName+
                         ((c.getSuperClass() == null)?"":" extends "+
                          c.getSuperClass().getGenerationFullName()));
            Set interfaces = c.getInterfaces();
            if (!interfaces.isEmpty()) {
                if (isInterface)
                    output.write(" extends ");
                else 
                    output.write(" implements ");
                joinFullNames(output,interfaces,",");
            }
            output.write( " {\n" );
            List ms = c.getMethods();
            for (int i=0; i < ms.size(); i++) {
                Method method = (Method)ms.get(i);
                createMethodCode(output,lineNumbers,method,isInterface);
            }

            List fs = c.getFields();
            for (int i=0; i<fs.size(); i++) {
                Field field = (Field)fs.get(i);
                createFieldCode(output,lineNumbers,field,isInterface);
            }

            List rs = c.getLinks();
            for (int i=0; i<rs.size(); i++) {
                Role role = (Role)rs.get(i);
                if (role instanceof RelationRole) {
                    RelationRole relRole = (RelationRole)role;
                    RelationLink rel = (RelationLink)role.getLink();
                    if (relRole.isNavigable()) {
                        createRelationCode(output,lineNumbers,rel,relRole,isInterface);
                    }
                }
            }

            output.write("}\n");
            output.close();
            //print("generation fullfilled.");
        } catch( Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Generate the code for a field, its getter and its setter
     *
     * @param output where to write generated code
     * @param lineNumbers line numbers information will be stored in this map
     * @param field the field to generate code for
     * @param isInterface wether the field belongs to an interface
     */
    public static void createFieldCode(LineNumberWriter output,
                                       Map lineNumbers,
                                       Field field, 
                                       boolean isInterface)
        throws IOException
    {
        String fieldName = field.getGenerationName();
        String type = field.getType().getGenerationFullName();

        if (!(field.isCalculated() || isInterface)) {
            String description = field.getDescription();
            if (description!=null && description.length()>0)
                output.write("\n/**\n" + description + "\n*/\n");
            lineNumbers.put(field,new Integer(output.getLines()));
            output.write("\n    " + field.getPrototype());
            String defaultValue = field.getDefaultValue();
            if (defaultValue!=null && !defaultValue.equals("")) {
                output.write(" = "+defaultValue);
            }
            output.write(";\n");
        }

        boolean isStatic = field.isStatic();
        if (!field.isReadOnly()) {
            Setter setter = field.getSetter();
            if (setter==null) {
                output.write("\n    /**\n"+
                             "     * Sets the value of field "+fieldName+".\n"+
                             "     * @param "+fieldName+" value of field "+fieldName+
                             "\n     */\n");
                lineNumbers.put(field,new Integer(output.getLines()));
                createSetter(
                    output,fieldName,type, isStatic,
                    field.isStatic()?field.getParent().getGenerationFullName():null,
                    isInterface);
            } else {
                createMethodCode(output,lineNumbers,setter,isInterface);
            }
        }

        Getter getter = field.getGetter();
        if (getter==null) {
            if (!field.isCalculated()) {
                output.write("\n    /**\n"+
                             "     * Gets the value of field "+fieldName+".\n"+
                             "     * @return value of field "+fieldName+
                             "\n     */\n");
                lineNumbers.put(field,new Integer(output.getLines()));
                createGetter(
                    output,fieldName,type,isStatic,
                    field.isStatic()?field.getParent().getGenerationFullName():null,
                    isInterface);
            }
        } else {
            createMethodCode(output,lineNumbers,field.getGetter(),isInterface);
        }
    }

    public static void createRelationCode(LineNumberWriter output,
                                          Map lineNumbers,
                                          RelationLink rel,
                                          RelationRole role,
                                          boolean isInterface)
        throws IOException
    {
        String roleGen = role.getGenerationName();
        String type = role.getAbstractType().getGenerationFullName();
        boolean isCalculated = rel.isCalculated();
        if (!isCalculated) {
            output.write("\n    " + role.getPrototype()+";\n");
            createSetter(output,roleGen,type,false,null,isInterface);
        }
        Method getter = role.getGetter();
        if (getter==null && !isCalculated) {
            createGetter(output,roleGen,type,false,null,isInterface);
        } else if (getter!=null) {
            createMethodCode(output,lineNumbers,getter,isInterface);
        }

        if (role.isMultiple() && !isCalculated) {
            Typed primaryKey = role.getPrimaryKey();
            type = role.getEnd().getGenerationFullName();
         
            Method adder = role.getAdder();
            if (adder==null) {
                createAdder(output,roleGen,type,primaryKey);
            } else {
                createMethodCode(output,lineNumbers,adder,isInterface);
            }

            Method remover = role.getRemover();
            if (remover==null) {
                createRemover(output,roleGen,type,primaryKey);
            } else {
                createMethodCode(output,lineNumbers,remover,isInterface);
            }

            Method clearer = role.getClearer();
            if (clearer==null) {
                createClearer(output,roleGen);
            } else {
                createMethodCode(output,lineNumbers,clearer,isInterface);
            }
            if (primaryKey!=null) {
                createIndexGetter(output,role);
            }
        }
    }

    /**
     * Generate the code for a method
     * @param output where to write generated code
     * @param lineNumbers
     * @param method the method to generate code for
     * @param isInterface wether the method belongs to an interface
     */
    public static void createMethodCode(LineNumberWriter output,
                                        Map lineNumbers,
                                        Method method,
                                        boolean isInterface)
        throws IOException
    {
        output.write("\n/**\n");
        String description = method.getDescription();
        if (description!=null) {
            output.write(description);
        }
        Iterator params = method.getParameters().iterator();
        while (params.hasNext()) {
            Parameter param = (Parameter)params.next();
            output.write("\n@param "+param.getGenerationName()+" "+
                         param.getDescription());
        }
        output.write("\n*/\n");
        lineNumbers.put(method,new Integer(output.getLines()));
        //Log.trace("ide.codegen",method.getParent()+"."+method.getGenerationName()+
        //          " starts at "+output.getLines());
        output.write("\n    "+method.getModifiers()+" "+
                     method.getPrototype());
        if (method.isAbstract() || isInterface) {
            output.write(";\n");
        } else {
            output.write(" {");
            String body = method.getBody();
            if (body!=null)
                output.write( "\n    "+body);
            output.write("\n    }\n");
        }
    }

    /**
     * Generate java source code for an aspect.
     *
     * @param prj the project
     * @param a the aspect
     * @param baseDir directory where to put the file
     * @param ppath java package path of the aspect component
     */
    public static void createAspectCode(Project prj,Aspect a,
                                        File baseDir,String ppath) {
        try {
            print("generating "+a.getName());
            File f = new File(baseDir,a.getGenerationName() + "AC.java");
            if (f.exists())
                f.delete();
            f.getParentFile().mkdirs();
            f.createNewFile();
            String shortName = a.getGenerationName();
            FileWriter fw = new FileWriter(f);
            writeWarning(fw);
            fw.write( "\npackage "+ppath+";\n" );
            fw.write( "import org.objectweb.jac.core.*;\n" );
            fw.write( "import org.objectweb.jac.core.rtti.*;\n" );
            fw.write( "import org.objectweb.jac.util.*;\n" );
            fw.write( "\npublic class "+shortName+
                      "AC extends org.objectweb.jac.core.AspectComponent");
            fw.write( " {\n" );
            fw.write( "\n    public "+shortName+"AC() {");
            Iterator it = a.getPointcutLinks().iterator();
            while(it.hasNext()) {
                PointcutLink pc = (PointcutLink)it.next();
                if(pc.getEnd() instanceof org.objectweb.jac.ide.Class) {
                    fw.write("\n        pointcut(\""+
                             "ALL\",\""+
                             ((org.objectweb.jac.ide.Class)pc.getEnd()).getGenerationFullName()+"\",\""+
                             pc.getMethodPCD()+"\","+
                             "new "+shortName+"Wrapper(this)"+",\""+
                             pc.getAspectRole()+"\",\""+
                             pc.getHostPCD()+"\",null);");
                } else if (pc.getEnd() instanceof Instance) {
                    fw.write("\n        pointcut(\""+
                             ((Instance)pc.getEnd()).getGenerationName()+"\",\""+
                             ((org.objectweb.jac.ide.Class)((Instance)pc.getEnd()).getType()).getGenerationName()+"\",\""+
                             pc.getMethodPCD()+"\","+
                             "new "+shortName+"Wrapper()"+",\""+
                             pc.getAspectRole()+"\",\""+
                             pc.getHostPCD()+"\");");
                }
            }
            fw.write("\n    }");

            fw.write( "\n    public class "+shortName+
                      "Wrapper extends org.objectweb.jac.core.Wrapper {");

            fw.write( "\n    public "+shortName+"Wrapper(AspectComponent ac) {");
            fw.write( "\n        super(ac);");
            fw.write( "\n    }");

            List ms = a.getMethods();
            for (int i=0; i<ms.size(); i++) {
                Method method = (Method)ms.get(i);
                fw.write("\n    "+method.getModifiers()+" "+method.getPrototype()+" {" );
                fw.write("\n    "+method.getBody());
                fw.write("\n    }\n");
            }

            List fs = a.getFields();
            for ( int i = 0; i < fs.size(); i++ ) {
                Field field=(Field)fs.get(i);
                fw.write( "\n    " + field.getPrototype()+";\n" );
                //createSetter(fw,field);
                //createGetter(fw,field);
            }

            /*Vector rs = a.getRelationLinks();
              for ( int i = 0; i < rs.size(); i++ ) {
              RelationLink rel=(RelationLink)rs.get(i);
              fw.write( "\n    " + rel.getPrototype()+";\n" );
              createSetter(fw,rel);
              createGetter(fw,rel);

              if(rel.isMultiple()) {
              createAdder(fw,rel);
              createRemover(fw,rel);
              }            } */

            fw.write("\n    }");
            fw.write( "}\n" );
            fw.close();
            //print("generation fullfilled.");
        } catch( Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Generate getter code for collection
     * @param output write code to this writer
     * @param name name of the attribute
     * @param type type of the attribute
     * @param className the class name if the field is static (null
     * otherwise) */
    static void createGetter(Writer output, String name, String type,
                             boolean isStatic,String className, 
                             boolean isInterface)
        throws IOException
    {
        output.write("\n    "+(isInterface?"":"public ")+
                     (isStatic?"static ":"")+type+" get"+
                     NamingConventions.capitalize(name)+"()");
        if (isInterface) {
            output.write(";\n");
        } else {
            output.write(" {\n");
            output.write("        return "+name+";\n");
            output.write("    }\n");
        }
    }

    /**
     * Generate setter code for collection
     * @param output write code to this writer
     * @param name name of the attribute
     * @param type type of the attribute
     * @param className the class name if the field is static (null
     * otherwise) */
    static void createSetter(Writer output, String name, String type,
                             boolean isStatic, String className, 
                             boolean isInterface)
        throws IOException
    {
        output.write("\n    "+(isInterface?"":"public")+
                     (isStatic?" static":"")+" void "+getSetterName(name)+"("+
                     type+" value)");
        if (isInterface) {
            output.write(";\n");
        } else {
            output.write(" {\n");
            if(className==null) {
                output.write("        this."+name+"=value;\n");
            } else {
                output.write("        "+className+"."+name+"=value;\n");
            }
            output.write("    }\n");
        }
    }

    /**
     * Returns the setter's name that corresponds to the given field's
     * name. */

    public static String getSetterName(String fieldName) {
        return "set"+NamingConventions.capitalize(fieldName);
    }

    /**
     * Returns the getter's name that corresponds to the given field's
     * name. */

    public static String getGetterName(String fieldName) {
        return "get"+NamingConventions.capitalize(fieldName);
    }

    /**
     * Returns the adder's name that corresponds to the given
     * collection's name. */

    public static String getAdderName(String fieldName) {
        String fn = NamingConventions.getNormalizedString(fieldName);
        fn = Projects.plurals.getSingular(fn);
        return "add"+fn;
    }

    /**
     * Returns the clearer's name that corresponds to the given
     * collection's name. */

    public static String getClearerName(String fieldName) {
        String fn = NamingConventions.getNormalizedString(fieldName);
        fn = Projects.plurals.getSingular(fn);
        return "clear"+fn;
    }

    /**
     * Returns the remover's name that corresponds to the given
     * collection's name. */

    public static String getRemoverName(String fieldName) {
        String fn = NamingConventions.getNormalizedString(fieldName);
        fn = Projects.plurals.getSingular(fn);
        return "remove"+fn;
    }

    public static String keyWrapper(Typed keyField, String value) {
        if (keyField.getType().isPrimitive()) {
            return "new "+Classes.getPrimitiveTypeWrapper(keyField.getType().getName())+"("+value+")";
        } else {
            return value;
        }
    }

    /**
     * Generate adder code for collection
     *
     * @param output write code to this writer
     * @param name name of the collection attribute
     * @param type type of the collection attribute
     */
    static void createAdder(Writer output, String name, String type, 
                            Typed primaryKey)
        throws IOException
    {
        output.write("\n    public void "+getAdderName(name)+"("+type+" value) {\n");
        if (primaryKey==null)
            output.write("        "+name+".add(value);\n");
        else
            output.write("        "+name+".put("+
                         keyWrapper(primaryKey,"value."+getGetterName(primaryKey.getGenerationName())+"()")+
                         ",value);\n");
        output.write("    }\n");
    }


    /**
     * Generate getter for indexed collection
     *
     * @param output write code to this writer
     * @param name name of the collection attribute
     * @param type type of the collection attribute
     */
    static void createIndexGetter(Writer output, RelationRole role)
        throws IOException
    {
        Typed primaryKey = role.getPrimaryKey();
        String keyName = primaryKey.getGenerationName();
        String returnType = role.getEnd().getGenerationName();
        output.write("\n    public "+returnType+
                     " get"+Projects.plurals.getSingular(NamingConventions.getNormalizedString(role.getGenerationName()))+"("+
                     role.getPrimaryKey().getType().getGenerationName()+" "+
                     keyName+") {\n");
        output.write("        return ("+returnType+")"+role.getGenerationName()+".get("+
                     keyWrapper(primaryKey,keyName)+
                     ");\n");
        output.write("    }\n");
    }

    /**
     * Generate remover code for collection
     * @param output write code to this writer
     * @param name name of the collection attribute
     * @param type type of the collection attribute
     */
    static void createRemover(Writer output, String name, String type, Typed primaryKey)
        throws IOException
    {
        output.write("\n    public void "+getRemoverName(name)+"("+type+" value) {\n");
        if (primaryKey==null) {
            output.write("        "+name+".remove(value);\n");
        } else {
            output.write("        "+name+".remove("+
                         keyWrapper(primaryKey,"value."+getGetterName(primaryKey.getGenerationName())+"()")+
                         ");\n");
        }
        output.write("    }\n");
    }

    /**
     * Generate clearer code for collection
     *
     * @param output write code to this writer
     * @param name name of the collection attribute
     * @param type type of the collection attribute
     */
    static void createClearer(Writer output, String name)
        throws IOException
    {
        String fn = NamingConventions.getNormalizedString(name);
        output.write("\n    public void clear"+
                     fn+"() {\n");
        output.write("        "+name+".clear();\n");
        output.write("    }\n");
    }

    /**
     * Build a list of the java files for a project.
     *
     * @param project the project
     * @param baseDir base directory for the files
     * @param list adds the filenames to this list
     */
    static void createJavaFilesList(Project project, File baseDir, List list) {
        Iterator it = Projects.types.getEnumeratedTypes().iterator();
        while(it.hasNext()) {
            EnumeratedType enum = (EnumeratedType)it.next();
            list.add(new File(baseDir,enum.getPackagePath()+
                              System.getProperty("file.separator")+
                              Strings.toUSAscii(enum.getName())+".java").getPath());
        }


        it = project.getPackages().iterator();
        while(it.hasNext()) {
            Package cur = (Package)it.next();
            createJavaFilesList(cur,
                                new File(baseDir,cur.getGenerationName()),
                                list);
        }
    }

    /**
     * Build a list of the java files for a package.
     *
     * @param pkg the package
     * @param baseDir base directory for the files
     * @param list adds to filenames to this list
     */
    static void createJavaFilesList(Package pkg, File baseDir, List list) {
        Iterator it = pkg.getSubPackages().iterator();
        while(it.hasNext()) {
            Package cur = (Package)it.next();
            createJavaFilesList(cur,
                                new File(baseDir,cur.getGenerationName()),
                                list);
        }
        it = pkg.getClasses().iterator();
        while(it.hasNext()) {
            Class cl = (Class)it.next();
            if (cl instanceof Aspect) {
                list.add(new File(baseDir,cl.getGenerationName()+"AC.java").getPath());
            } else {
                list.add(new File(baseDir,cl.getGenerationName()+".java").getPath());
            }
        }

        it = pkg.getResources().iterator();
        while(it.hasNext()) {
            Attachment res = (Attachment)it.next();
            if ("text/x-java".equals(res.getMimeType())) {
                list.add(new File(baseDir,res.getName()).getPath());
            }
        }
    }

    /**
     * Build a space separated list of the root packages source
     * directories of a project.
     *
     * @param project the project
     * @param baseDir base directory of the project
     * @return a String
     */
    static String createPackageFilesList(Project project,String baseDir) {
        StringBuffer result =
            new StringBuffer((baseDir.length()+10)*project.getPackages().size());
        Iterator it = project.getPackages().iterator();
        while(it.hasNext()) {
            Package cur = (Package)it.next();
            result.append(baseDir + System.getProperty("file.separator")
                          + cur.getGenerationName() + " ");
        }
        return result.toString();
    }

    // filename -> Class
    static transient Hashtable classFiles = new Hashtable();

    /**
     * Get the class corresponding to a filename
     */
    static Class filenameToClass(String filename) {
        Class cl = null;
        if ((cl = (Class)classFiles.get(filename))!=null) {
            return cl;
        } else {
            logger.error("Cannot find Class for filename "+filename);
            return null;
        }
    }

    public static Errors generateAndCompileProjectCode(Project project, String baseDir)
        throws CannotGenerateException, CannotCompileException, IOException, InterruptedException
    {
        createProjectCode(project,baseDir);
        return compileProjectCode(project);
    }

    /**
     * Run compilation command for a project.
     *
     * @param project the project to compile
     * @return A list of errors that occured during compilation.
     */
    public static Errors compileProjectCode(Project project)
        throws CannotCompileException, IOException, InterruptedException
    {
        File baseDir = project.getGenerationPath();
        Vector cmdList = new Vector();
        if (!project.getUseToolsJavac())
            cmdList.add(project.getCompilerCommand().toString());
        String[] options = Strings.split(project.getCompilerOptions()," ");
        for (int i=0; i<options.length; i++) {
            cmdList.add(options[i]);
        }

        File classesDir = project.getClassesDir();
        cmdList.add("-d");
        cmdList.add(classesDir.getPath());

        // Force UTF-8 since we generate sources with this encoding
        cmdList.add("-encoding");
        cmdList.add("UTF-8");

        String classpathString = null;
        Collection classpath = new Vector();
        classpath.addAll(project.getClasspath());
        if (!classpath.contains(baseDir)) {
            classpath.add(baseDir);
        }
        if (!classpath.contains(classesDir)) {
            classpath.add(classesDir);
        }

        String jacRoot = org.objectweb.jac.core.Jac.getJacRoot();
        File jacClasses = new File(jacRoot,"classes");
        if (jacClasses.exists() && !classpath.contains(jacClasses)) {
            classpath.add(jacClasses);
        }

        File jacJar = new File(jacRoot+"jac.jar");
        if (jacJar.exists() && !classpath.contains(jacJar)) {
            classpath.add(jacJar);
        }

        File jacDir = new File(jacRoot,"lib");
        java.io.File jars[] = jacDir.listFiles(
            new FilenameFilter() {
                    public boolean accept(java.io.File file, String name) {
                        return name.endsWith(".jar");
                    }
                }
        );
        if (jars!=null)
	        classpath.addAll(Arrays.asList(jars));
        String sep = System.getProperty("file.separator");
        classpath.add(new File(jacRoot+sep+"lib"+sep+"opt","jasperreports.jar"));

        classpathString = Strings.createPathString(classpath);

        logger.info("Classpath = "+classpathString);
        if (classpathString!=null) {
            cmdList.add("-classpath");
            cmdList.add(classpathString);
        }

        createJavaFilesList(project,baseDir,cmdList);

        File f = new File(baseDir, "classes");
        if (!f.exists())
            f.mkdir();

        logger.info("CMD = "+cmdList);
        print("compiling project "+project.getName()+"...");

        BufferedReader reader = null;
        final String[] args = (String[])cmdList.toArray(new String[] {});
        if (project.getUseToolsJavac()) {
            PrintStream oldErr = System.err;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            System.setErr(new PrintStream(out));
            int res = Main.compile(args);
            System.setErr(oldErr);
            reader = new BufferedReader(
                new InputStreamReader(
                    new ByteArrayInputStream(out.toByteArray())));
        } else {
            Process comp;
            try {
                comp = Runtime.getRuntime().exec(args);
            } catch (IOException e) {
                throw new CannotCompileException(
                    "Failed to start compilation process, is `"+
                    project.getCompilerCommand()+"' in your path?");
            }
            reader = new BufferedReader(
                new InputStreamReader(comp.getErrorStream()));
            logger.debug("waiting for compilation to finish "+comp);
        }

        Vector errors = new Vector();
        String line;
        try {
            RE re = new RE("^([^:]+):([0-9]+)");
            RE emptyLineRE = new RE("^[ 	]*$");
            while ((line=reader.readLine())!=null) {
                //Log.trace("ide.compile",line);
                REMatch match;
                if ((match=re.getMatch(line))!=null) {
                    //Log.trace("ide.compile",
                    //          "New ERROR : "+match.toString(1)+":"+match.toString(2));
                    errors.add(new Error(match.toString(1),
                                         Integer.parseInt(match.toString(2)),
                                         line,
                                         filenameToClass(match.toString(1))));
                } else if (emptyLineRE.getMatch(line)==null) {
                    errors.add(new Error(line));
                }
            }
        } catch (REException e) {
            throw new CannotCompileException(e.getMessage());
        }
        //Log.trace("ide.compile","Found "+errors.size()+" Errors.");

        print("compilation done.");
        return errors.isEmpty()?null:new Errors(errors);
    }

    /**
     * Generated javadoc documentation for a project
     */
    public static void documentProjectCode(Project project,String baseDir) {
        String list = createPackageFilesList(project,baseDir);
        File f = new File(project.getGenerationPath(),"doc");
        if (!f.exists())
            f.mkdir();
        String cmd = "javadoc -d "+new File(project.getGenerationPath(),"classes")+" "+list;
        //Log.trace("ide.compile","CMD = "+cmd);
        // to do...
    }

    /**
     * Print status message.
     */
    static void print(String msg) {
        org.objectweb.jac.aspects.gui.Actions.showStatus(msg);
    }

    /**
     * Generate the ACC configuration code from the ConfigItems
     * @param pack package to generate config for
     * @param ac the aspect configuration to generate code for
     * @param fw generate code to this writer
     * @throws IOException
     */
    static void generateConfigItemPackageCode(Package pack, AspectConfiguration ac,
                                              Writer fw)
        throws IOException
    {
        AccGenState state = new AccGenState(fw);
        Iterator iteClass = pack.getClasses().iterator();
        //Iterate the Classes in the package
        while (iteClass.hasNext()) {
            Class c = (Class)iteClass.next();

            //iterate the config of the Class
            Iterator iteClassConfig = c.getConfigItems().iterator();
            while (iteClassConfig.hasNext()) {
                ConfigItem configItem = (ConfigItem)iteClassConfig.next();
                if (configItem.getAspectConfiguration()==ac){
                    state.openClass(c);
                    state.write(translateParam(configItem));
                }
            }
            //End of the iteration on the class ConfigItem

            //iterate the class field
            Iterator iteField = c.getFields().iterator();
            while (iteField.hasNext()) {
                Field field = (Field)iteField.next();
                //Iteratate the ConfigItem
                Iterator iteFieldConfig = field.getConfigItems().iterator();
                while (iteFieldConfig.hasNext()) {
                    ConfigItem configItem = (ConfigItem)iteFieldConfig.next();
                    if (configItem.getAspectConfiguration()==ac){
                        state.openField(c,field);
                        state.write(translateParam(configItem));
                    }
                }
                state.closeMember();
            }
            //end of the ConfigItem of the field Class

            //iterate the Class Method
            Iterator iteMethod = c.getMethods().iterator();
            while (iteMethod.hasNext()) {
                Method method = (Method)iteMethod.next();
                //Iteratate the ConfigItem
                Iterator iteMethodConfig = method.getConfigItems().iterator();
                while (iteMethodConfig.hasNext()) {
                    ConfigItem configItem = (ConfigItem)iteMethodConfig.next();
                    if (configItem.getAspectConfiguration()==ac){
                        state.openMethod(c,method);
                        state.write(translateParam(configItem));
                    }
                }
                state.closeMember();
            }
            //End of the class Method Iteration

            //iterate the links between class
            /* Not really the Relation Links
               Iterator iteLinks=c.getRelationLinks().iterator();
               while(iteLinks.hasNext()){
               RelationLink link=(RelationLink)iteLinks.next();
               Iterator iteLinkConfig=link.getConfigItems().iterator();
               fw.write("   attribute "+link.getGenerationName()+" {\n");
               while(iteLinkConfig.hasNext()){
               ConfigItem item=(ConfigItem)iteLinkConfig.next();
               if (item.getAspectConfiguration()==ac){
               fw.write("      "+translateParam(configItem));
               }
               }
               fw.write("   }\n");
               }
            */
            //end of the Class links iteration
            state.closeClass();

        }

        //Iterate the sub package
        Iterator itePackage = pack.getSubPackages().iterator();
        while (itePackage.hasNext()) {
            Package apack = (Package)itePackage.next();
            generateConfigItemPackageCode(apack, ac, fw);
        }
    }

    /**
     * Return a string representing the call of method on ModelElement
     * element with param param
     * @param config the configItem of method
     * @return a valid string that the ACCParser will understand */
    public final static String translateParam(ConfigItem config){
        MethodItem method=config.getMethod();
        if (method==null || "".equals(method.getName()))
            return "";
        StringBuffer result=new StringBuffer();
        result.append(method.getName()+" ");
        Iterator params = config.getParam().iterator();
        while (params.hasNext()) {
            String s = (String)params.next();
            //todo if s contain " or keycode we should add/replace some \"
            int posBefore = 0, posAfter = 0;
            result.append("\"");
            while (posBefore!=-1){
                posAfter=s.indexOf('\"',posBefore);
                if (posAfter==-1){
                    result.append(s.substring(posBefore));
                    posBefore = -1;
                }else{
                    result.append(s.substring(posBefore,posAfter)+"\\\"");
                    if (posAfter==s.length()-1){
                        posBefore = -1;
                    }else{
                        posBefore = posAfter+1;
                    }
                }
            }
            result.append("\" ");
        }
        result.append(";\n");
        return result.toString();
    }

    /**
     * Write the full names of the items of a collection, separated by
     * a string.
     * @param out where to write 
     * @param items a collection of ModelElements
     * @param separator use this string as a separator
     */
    public static void joinFullNames(Writer out, Collection items, String separator) 
        throws IOException
    {
        Iterator it = items.iterator();
        while (it.hasNext()) {
            out.write(((ModelElement)it.next()).getGenerationFullName());
            if (it.hasNext())
                out.write(separator);
        }
    }

    // Projects mixin methods
    
    public static void generateCode(Projects projects) throws Exception {
        Iterator it = projects.getProjects().iterator();
        while (it.hasNext()) {
            Project p = (Project)it.next();
            generateCode(p);
        }
    }

    public static Errors compile(Projects projects) throws Exception {
        Iterator it = projects.getProjects().iterator();
        while (it.hasNext()) {
            Project p = (Project)it.next();
            Errors errors = null;
            errors = compile(p);
            if (errors!=null) {
                return errors;
            }
        }
        return null;
    }

    public static Errors generateAndCompile(Projects projects) throws Exception {
        Iterator it = projects.getProjects().iterator();
        while (it.hasNext()) {
            Project p = (Project)it.next();
            Errors errors = null;
            errors = generateAndCompile(p);
            if (errors!=null) {
                return errors;
            }
        }
        return null;
    }

    // Package mixin methods

    public static void generateCode(Package pkg) throws Exception {
        Project project = pkg.getProject();
        project.checkGenerationPath();
        createPackageCode(
            project,
            pkg,
            new File(project.getGenerationPath().getPath(),pkg.getPath()),
            pkg.getPPath());
    }

    // Class mixin methods

    public static void generateCode(Class cl) throws Exception {
        Package pkg = cl.getContainer();
        Project project = pkg.getProject();
        project.checkGenerationPath();
        createClassCode(
            project,
            cl,
            new File(project.getGenerationPath().getPath(),pkg.getPath()),
            pkg.getPPath());
    }

    // Project mixin methods

    public static void generateCode(Project project) throws Exception {
        File generationPath = project.getGenerationPath();
        if (generationPath!=null) {
            createProjectCode(project,generationPath.getPath());
        } else { 
            throw new CannotGenerateException(
                "Project \""+project.getName()+"\" does not have a generation path");
        }
    }

    public static void generateJavaCode(Project project) throws Exception {
        File generationPath = project.getGenerationPath();
        if (generationPath!=null) {
            createJavaCode(project,generationPath.getPath());
        } else { 
            throw new CannotGenerateException(
                "Project \""+project.getName()+"\" does not have a generation path");
        }
    }

    public static Errors compile(Project project) 
        throws CannotCompileException, IOException, InterruptedException 
    {
        File generationPath = project.getGenerationPath();
        if (generationPath!=null) {
            return compileProjectCode(project);
        } else {
            throw new CannotCompileException(
                "Project \""+project.getName()+"\" does not have a generation path");
        }
    }

    public static Errors generateAndCompile(Project project) 
        throws CannotGenerateException, CannotCompileException, 
               IOException, InterruptedException, Exception
    {
        generateJavaCode(project);
        return compile(project);
    }

    
    /**
     * Creates a JAR file containing all the .class files
     */
    public static void createJAR(Project project) throws IOException {
    	createManifest(project);
        File generationPath = project.getGenerationPath();
        JarOutputStream out = 
            new JarOutputStream(
                new FileOutputStream(
                    new File(
                        generationPath,
                        project.getGenerationName()+".jar")),
                new Manifest(
                    new FileInputStream(
                        new File(project.getManifestDir(), "MANIFEST.MF" )))
            );

        // Add class files
        File classesDir = project.getClassesDir();
        Iterator it = 
            project.getClassesDir().listFilesRecursively(
                new FilenameFilter() {
                        public boolean accept(java.io.File file, String name) {
                            return name.endsWith(".class");
                        }
                    }
            ).iterator();
        while (it.hasNext()) {
            File classFile = (File)it.next();
            JarEntry entry = new JarEntry(classFile.getRelativePath(classesDir).replace('\\','/'));
            out.putNextEntry(entry);
            FileInputStream input = new FileInputStream(classFile);
            Streams.copy(input,out);
            out.closeEntry();
        }
        
        // Add .acc and .jac files
        it = project.getApplications().iterator();
        while (it.hasNext()) {
            Application application = (Application)it.next();
            Iterator j = application.getAspectConfigurations().iterator();
            File appDir = new File(generationPath,application.getGenerationName());
            while (j.hasNext()) {
                AspectConfiguration ac = (AspectConfiguration)j.next();
                File accFile = new File(appDir,ac.getGenerationName()+".acc");
                JarEntry entry = 
                    new JarEntry(
                        accFile.getRelativePath(generationPath).replace('\\','/'));
                out.putNextEntry(entry);
                FileInputStream input = new FileInputStream(accFile);
                Streams.copy(input,out);
                out.closeEntry();
            }
            File jacFile = application.getJacFile();
            JarEntry entry = new JarEntry(jacFile.getRelativePath(generationPath));
            out.putNextEntry(entry);
            FileInputStream input = new FileInputStream(jacFile);
            Streams.copy(input,out);
            out.closeEntry();
        }

        // Add external files
        it = project.getExternalFiles().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            JarEntry jarEntry = new JarEntry((String)entry.getKey());
            out.putNextEntry(jarEntry);
            FileInputStream input = new FileInputStream((File)entry.getValue());
            Streams.copy(input,out);
            out.closeEntry();
        }

        // Add resources
        it = project.getAllResources().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            Package pkg = (Package)entry.getKey();
            Attachment res = (Attachment)entry.getValue();
            JarEntry jarEntry = 
                new JarEntry(
                    pkg.getGenerationName()+'/'+res.getName());
            out.putNextEntry(jarEntry);
            ByteArrayInputStream input = new ByteArrayInputStream(res.getData());
            Streams.copy(input,out);
            out.closeEntry();            
        }
        out.close();
    }

    public static void createManifest(Project project) throws IOException {
    	File file = project.getManifestDir();
    	file.mkdir();
    	if (!file.exists())
    		new IOException("Could not create META-INF directory");
    		
        FileWriter out = 
                new FileWriter(new File(project.getManifestDir(), "MANIFEST.MF"));
        out.write("Manifest-Version: 1.0\n\n\n\n");

        // Add class files
        File classesDir = project.getClassesDir();
        Iterator it = 
            project.getClassesDir().listFilesRecursively(
                new FilenameFilter() {
                        public boolean accept(java.io.File file, String name) {
                            return name.endsWith(".class");
                        }
                    }
            ).iterator();
        while (it.hasNext()) {
            File classFile = (File)it.next();
            out.write("Name: ");
            out.write(classFile.getRelativePath(classesDir).replace('\\','/') );
            out.write("\nJava-Bean: True\n\n");
        }

        out.close();
    }

    // AspectConfiguration mixin methods

    public static void generateCode(AspectConfiguration ac) throws Exception {
        Project project = ac.getApplication().getProject();
        project.checkGenerationPath();
        CodeGeneration.createACFile(
            project,ac,
            new File(project.getGenerationPath().getPath(),ac.getApplication().getGenerationName()));
    }

    public static void generateAndReload(AspectConfiguration ac) throws Throwable {
        generateCode(ac);
        ac.reload();
    }
}
