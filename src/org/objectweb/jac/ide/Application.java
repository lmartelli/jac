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

import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import org.apache.log4j.Level;
import org.objectweb.jac.core.Jac;
import org.objectweb.jac.util.File;
import org.objectweb.jac.util.Pipe;
import org.objectweb.jac.util.Strings;

public class Application extends ModelElement {

    public Application() {
    }

    Vector hosts = new Vector();
   
    /**
     * Get the value of hosts.
     * @return value of hosts.
     */
    public List getHosts() {
        return hosts;
    }
   
    public void addHost(String host) {
        hosts.add(host);
    }

    public void removeHost(String host) {
        hosts.remove(host);
    }

    String name;
   
    /**
     * Get the value of name.
     * @return value of name.
     */
    public String getName() {
        return name;
    }
   
    /**
     * Set the value of name.
     * @param v  Value to assign to name.
     */
    public void setName(String  v) {
        this.name = v;
    }

    Class mainClass;
   
    /**
     * Get the value of mainClass.
     * @return value of mainClass.
     */
    public Class getMainClass() {
        return mainClass;
    }
   
    /**
     * Set the value of mainClass.
     * @param v  Value to assign to mainClass.
     */
    public void setMainClass(Class  v) {
        this.mainClass = v;
    }
   
    Vector aspectConfigurations = new Vector();
   
    /**
     * Get the value of aspectConfigurations.
     * @return value of aspectConfigurations.
     */
    public List getAspectConfigurations() {
        return aspectConfigurations;
    }
   
    public void addAspectConfiguration(AspectConfiguration a) {
        aspectConfigurations.add(a);
    }

    public void removeAspectConfiguration(AspectConfiguration a) {
        aspectConfigurations.remove(a);
    }

    Vector externalPaths = new Vector();
    /**
     * Get the value of externalPaths.
     * @return value of externalPaths.
     */
    public List getExternalPaths() {
        return externalPaths;
    }
    public void addExternalPath(String path) {
        externalPaths.add(path);
    }
    public void removeExternalPath(String path) {
        externalPaths.remove(path);
    }

    /**
     * Classes that JAC must not translate
     */
    Set ignoredClasses = new HashSet();
    public Set getIgnoredClasses() {
        return ignoredClasses;
    }
    public void addIgnoredClass(String expr) {
        ignoredClasses.add(expr);
    }
    public void removeIgnoredClass(String expr) {
        ignoredClasses.remove(expr);
    }

    Project project;
    /**
     * Get the value of project.
     * @return value of project.
     */
    public Project getProject() {
        return project;
    }
    /**
     * Set the value of project.
     * @param v  Value to assign to project.
     */
    public void setProject(Project  v) {
        this.project = v;
    }

    boolean startSwingGUI = false;
    public void setStartSwingGUI(boolean value) {
        this.startSwingGUI = value;
    }
    public boolean getStartSwingGUI() {
        return startSwingGUI;
    }

    String swingGUIs;
    public String getSwingGUIs() {
        return swingGUIs;
    }
    public void setSwingGUIs(String  v) {
        this.swingGUIs = v;
    }
   
    String webGUIs;
    public String getWebGUIs() {
        return webGUIs;
    }
    public void setWebGUIs(String  v) {
        this.webGUIs = v;
    }
   
    boolean startWebGUI = false;
    public void setStartWebGUI(boolean value) {
        this.startWebGUI = value;
    }
    public boolean getStartWebGUI() {
        return startWebGUI;
    }

    boolean enableDistribution = true;
    public void setEnableDistribution(boolean value) {
        this.enableDistribution = value;
    }
    public boolean isDistributionEnabled() {
        return enableDistribution;
    }

    String serverName = "s1";
    public void setServerName(String name) {
        this.serverName = name;
    }
    public String getServerName() {
        return serverName;
    }
   
    String otherOptions = "";
    public String getOtherOption() {
        return otherOptions;
    }
    public void setOtherOptions(String options) {
        this.otherOptions = options;
    }

    String jvmOptions = "";
    public String getJvmOption() {
        return jvmOptions;
    }
    public void setJvmOptions(String options) {
        this.jvmOptions = options;
    }

    Map properties = new Hashtable();
    public Map getProperties() {
        return properties;
    }
    public void addProperty(String name, String value) {
        properties.put(name,value);
    }
    public void removeProperty(String name) {
        properties.remove(name);
    }

    boolean enableDebugging;
    public boolean isEnableDebugging() {
        return enableDebugging;
    }
    public void setEnableDebugging(boolean enableDebugging) {
        this.enableDebugging = enableDebugging;
    }

    int debugPort = 4444;
    public int getDebugPort() {
        return debugPort;
    }
    public void setDebugPort(int debugPort) {
        this.debugPort = debugPort;
    }

    transient Process currentProcess = null;
    public void resetCurrentProcess() {
        currentProcess = null;
    }

    public boolean isStarted() {
        return currentProcess!=null;
    }
    public boolean isNotStarted() {
        return currentProcess==null;
    }

    public File getJacFile() {
        return new File(
            new File(project.getGenerationPath(),getGenerationName()),
            getGenerationName()+".jac");
    }

    /**
     * Returns the command and arguments to run the application
     */
    public String[] getRunCommandAndArgs() {
        File dir = new File(project.getGenerationPath(),name);
        Vector runCmd = new Vector();
        runCmd.add("java");

        runCmd.addAll(Strings.splitToList(jvmOptions," "));

        if (enableDebugging) {
            runCmd.add("-Xdebug");
            runCmd.add("-Xrunjdwp:transport=dt_socket,address="+debugPort+",server=y,suspend=n");
        }

        runCmd.add("-Djava.security.policy="+
                   org.objectweb.jac.core.Jac.getJacRoot()+"jac.policy");

        // So that is works on MacOS X
        runCmd.add("-Dswing.defaultlaf=javax.swing.plaf.metal.MetalLookAndFeel");

        File jar = new File(org.objectweb.jac.core.Jac.getJacRoot()+"jac.jar");
        String jacroot = org.objectweb.jac.core.Jac.getJacRoot();
        String sep = System.getProperty("path.separator");

        // add the classpath defined in the project and all jars in $JAC_ROOT/lib
        List classpath = new LinkedList(getProject().getClasspath());
        File libDir = new File(jacroot,"lib");
        classpath.add(new File(jacroot,"classes"));
        classpath.add(new File(jacroot,"src"));
        if (jar.exists())
            classpath.add(jar);
        classpath.addAll(
            libDir.listFilesRecursively(
                new FilenameFilter() {
                        public boolean accept(java.io.File file, String name) {
                            return name.endsWith(".jar");
                        }
                    }
            ));
        if (classpath.size()>0) {
            runCmd.add("-classpath");
            runCmd.add(Strings.join(classpath,sep));
        }

        Iterator it = properties.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            runCmd.add("-D"+entry.getKey()+"="+entry.getValue());
        }

        runCmd.add("org.objectweb.jac.core.Jac");
         
        runCmd.add("-w");
        if (enableDistribution) {
            runCmd.add("-D");
            if (serverName!=null && !serverName.equals(""))
                runCmd.add(serverName);
        }

        if (!Strings.isEmpty(jacroot)) {
            runCmd.add("-R");
            runCmd.add(jacroot);
        }
        runCmd.add("-C");
        runCmd.add(project.getGenerationPath().getPath()+
                   sep+new File(project.getGenerationPath(),"classes").getPath()+
                   sep+project.getClasspathString());

        String msg="Starting application '"+name+"'";
        if (startSwingGUI) {
            runCmd.add("-G");
            runCmd.add(swingGUIs);
            msg = msg+" (Swing GUIs: "+swingGUIs+")";
        }

        if (startWebGUI) {
            runCmd.add("-W");
            runCmd.add(webGUIs);
            msg = msg+" (Web GUIs: "+webGUIs+")";
        }

        runCmd.addAll(Strings.splitToList(otherOptions," "));

        runCmd.add(new File(dir,name+".jac").getPath());

        return (String[])runCmd.toArray(new String[] {});
    }

    public void start() throws IOException {
        if (!startSwingGUI && !startWebGUI) {
            throw new RuntimeException(
                "You should probably start a GUI (see \"Run options\""+
                " of the application)");
        }

        if (isStarted()) {
            stop();
        }

        String msg="Starting application '"+name+"'";
        if (startSwingGUI) {
            msg = msg+" (Swing GUIs: "+swingGUIs+")";
        }
        if (startWebGUI) {
            msg = msg+" (Web GUIs: "+webGUIs+")";
        }
        org.objectweb.jac.aspects.gui.Actions.showStatus(msg);

        String[] runCmd = getRunCommandAndArgs();
        //Log.trace("ide.run",0,"Run command = "+Arrays.asList(runCmd));
        currentProcess = Runtime.getRuntime().exec(runCmd);

        Pipe outputPipe = new Pipe(currentProcess.getInputStream(),System.out);
        outputPipe.start();
        Pipe errorPipe = new Pipe(currentProcess.getErrorStream(),System.out);
        errorPipe.start();
        new Thread() {
                public void run() {
                    while (currentProcess!=null) {
                        try {
                            currentProcess.waitFor();
                            //Log.trace("ide","Current process completed");
                            resetCurrentProcess();
                        } catch (InterruptedException e) {
                            //Log.trace("ide","Exception while waiting for "+currentProcess);
                        }
                    }
                }
            }.start();
    }

    public void stop() {
        if (currentProcess!=null) {
            currentProcess.destroy();
            currentProcess = null;
        }
        org.objectweb.jac.aspects.gui.Actions.showStatus("Application stopped.");
    }

    public void startSlaves()  throws IOException {  
        File dir = new File(project.getGenerationPath(),name);
        Vector runCmd = new Vector();
        runCmd.add("go");
        runCmd.add(""+getHosts().size());
        File jar = new File(org.objectweb.jac.core.Jac.getJacRoot()+"jac.jar");
        runCmd.add("-C");
        runCmd.add(project.getGenerationPath().getPath()+
                   System.getProperty("path.separator")+
                   new File(project.getGenerationPath(),"classes").getPath());
      
        String msg="Starting slaves...";

        org.objectweb.jac.aspects.gui.Actions.showStatus(msg);

        //Log.trace("ide.run",0,"Run command = "+runCmd);
        Runtime.getRuntime().exec(
            (String[])runCmd.toArray(new String[] {}));
      
    }

    /**
     * Changes a trace for the running process
     * @param category category of the trace
     * @param level level of the trace (0=quiet,1=verbose,2=more verbose...)
     */
    public void setTrace(String category, Level level) throws Exception {
        if (currentProcess!=null) {
            Jac.remoteSetTrace(name,serverName,category,level.toInt());
        }
    }

    public boolean canStartSlaves() {
        return getHosts().size()>0;
    }

    public void generateCode() throws CannotGenerateException, IOException
    {
        project.checkGenerationPath();
        CodeGeneration.createApplicationCode(
            project,this,new File(project.getGenerationPath().getPath(),name));
    }

    /**
     * Returns available main classes (Classes wich have a static void
     * main(String[]) method)
     * @param application the application to return main classes for
     */
    public static Collection getMainClasses(Application application) {
        Vector mainClasses = new Vector();
        if (application.getProject()!=null) {
            Iterator it = application.getProject().getPackages().iterator();
            while (it.hasNext()) {
                Package pkg = (Package)it.next();
                mainClasses.addAll(pkg.getMainClasses());
            }
        }
        return mainClasses;
    }
}
