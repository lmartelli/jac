/*
  Copyright (C) 2001-2002 Renaud Pawlak <renaud@aopsys.com>

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

package org.objectweb.jac.core;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.lang.NoSuchMethodException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.rtti.*;
import org.objectweb.jac.util.*;

/**
 * This class is a repository for all the applications defined in the
 * JAC system.
 *
 * @see Application */

public class ApplicationRepository {
    static Logger logger = Logger.getLogger("jac");
    static Logger loggerCP = Logger.getLogger("classpath");
    static Logger loggerAspects = Logger.getLogger("aspects");

    static Hashtable owningApplications = new Hashtable();
   
    public static Application getOwningApplication(Wrappee wrappee,
                                                   ClassItem cl) {
        if (wrappee!=null)
            return (Application)owningApplications.get(wrappee);
        else if (cl!=null)
            return (Application)owningApplications.get(cl);
        else
            return null;
    }

    public static String getOwningApplicationName(Wrappee wrappee,
                                                  ClassItem cl) {
        Application app = getOwningApplication(wrappee,cl);
        if (app!=null)
            return app.getName();
        else
            return null;
    }
   
    public static void setOwningApplication(Wrappee wrappee,ClassItem cl,
                                            Application application) {
        if(wrappee!=null)
            owningApplications.put(wrappee,application);
        else if(cl!=null)
            owningApplications.put(cl,application);
    }

    HashMap applications = new HashMap();

    static ApplicationRepository applicationRepository;

    /**
     * Returns the application of the current thread.
     *
     * @return an application, null if none */

    public static Application getCurrentApplication() {
        String appName = (String)Collaboration.get().getCurApp();
        if( appName == null ) return null;
        return (Application)get().applications.get(appName);
    }

    /**
     * Launches a JAC program.
     *
     * @param args the program arguments (first is the .jac file) */

    public static void launchProgram(String [] args) {
        InputStream fis = null;
        Properties applicationDescriptor = new Properties();
      
        try {
            fis = new FileInputStream(args[0]);
            applicationDescriptor.load(fis);
        } catch(Exception e) {
            ClassLoader loader = ApplicationRepository.class.getClassLoader();
                
            loggerCP.debug("trying to load "+args[0]+" with "+loader);
            fis = loader.getResourceAsStream(args[0]);
            if (fis == null) {
                logger.error("cannot find application descriptor "+args[0]);
                if (loader instanceof URLClassLoader)
                    logger.error("  classpath="+Arrays.asList(((URLClassLoader)loader).getURLs()));
                return;
            }
            try {
                applicationDescriptor.load(fis);
            } catch (IOException ioe) {
                logger.error("failed to read applicationDescriptor descriptor "+
                             args[0]+": "+ioe);
                return;
            }
        }

        // We must use reflection here because
        // JacLoader.class!=ApplicationRepository.class.getClassLoader().class
        ClassLoader loader = ApplicationRepository.class.getClassLoader();
        try {
            loader.getClass().getMethod("readProperties",
                                        new Class[] {Properties.class})
                .invoke(loader,new Object[] {applicationDescriptor});
        } catch (Exception e) {
            logger.error("failed to read JAC properties from application descriptor "+args[0]);
        }

        JacPropLoader.addProps(applicationDescriptor);

        String applicationName = 
            applicationDescriptor.getProperty("applicationName");
        String launchingClass = 
            applicationDescriptor.getProperty("launchingClass");
        String aspects = applicationDescriptor.getProperty("aspects");
        String topology = applicationDescriptor.getProperty("topology");

        if (applicationName==null) {
            logger.warn("bad application descriptor "+args[0]);
            return;
        }
      
        logger.info("launching application "+applicationName);

        // bind to the given topology if any
        if (topology!=null) {
            Class remoteContainerClass = null;
            try {
                remoteContainerClass = 
                    Class.forName("org.objectweb.jac.core.dist.RemoteContainer");
            } catch (ClassNotFoundException e) {
                logger.fatal("Could not find class org.objectweb.jac.core.dist.RemoteContainer");
                System.exit(1);
            }
            StringTokenizer st1 = new StringTokenizer(topology);
            while (st1.hasMoreElements()) {
                String container = (String)st1.nextElement();
                try {
                    remoteContainerClass.getMethod("bindNewContainer",
                                                   new Class[] {String.class})
                        .invoke(null,new Object[] {container});
                } catch (InvocationTargetException e) {
                    logger.error("Failed to bind to "+container+": "+
                                 e.getTargetException().getMessage());
                } catch (Exception e) {
                    logger.error("Failed to bind to "+container+": "+e.getMessage());
                }
            }
            logger.debug("application topology successfully bound");
        }

        // create the application
        Application app = new Application(applicationName,null,
                                          launchingClass,null);
      
        // create the aspects configurations
        if (aspects!=null) {
            StreamTokenizer tokens = 
                new StreamTokenizer(new StringReader(aspects));
            tokens.wordChars('/','/');
            tokens.wordChars('_','_');
            tokens.wordChars('-','-');
            tokens.wordChars(':',':');
            tokens.wordChars('$','$');
            char fileSep = System.getProperty("file.separator").charAt(0);
            tokens.wordChars(fileSep,fileSep);
            try {
                while (tokens.nextToken()!=StreamTokenizer.TT_EOF) {
                    String ac_name = tokens.sval;
                    tokens.nextToken();
                    String ac_path = tokens.sval;
                    tokens.nextToken();
                    String ac_weave = tokens.sval;
                    loggerAspects.info(
                        "adding aspect "+ac_name+"["+ac_path+
                        "]:"+ac_weave+" to "+app);
                    app.addAcConfiguration( 
                        new ACConfiguration(app, ac_name, 
                                            ac_path, 
                                            (ac_weave.equals("true")?true:false)));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
      
        logger.debug("adding application "+app);
        ApplicationRepository.get().addApplication(app);
      
        try {
            String[] appargs = new String[args.length - 1];
            if (appargs.length > 0) {
                System.arraycopy(args, 1, appargs, 0, args.length - 1);
            }
            Class lc = Class.forName(launchingClass);
            try {
                Method mainMethod = lc.getMethod("main",new Class[] {String[].class});
                if (!Modifier.isStatic(mainMethod.getModifiers())) {
                    logger.error("main method of class "+launchingClass+" is not static");
                    return;
                }
                mainMethod.invoke(null,new Object[] {appargs});
            } catch (NoSuchMethodException e) {
                logger.error("No such method "+launchingClass+".main(String[])");
                return;
            }
        } catch(Exception e) {
            logger.error(applicationName+" launch failed",e);
        }
        ((ACManager)ACManager.get()).afterApplicationStarted();
    }

    /**
     * Launches and initializes the application repository (this method
     * is called by the system).
     *
     * @param args the launching arguments of JAC */

    public static void main(String[] args) throws Throwable {
        if (applicationRepository == null) 
            applicationRepository = new ApplicationRepository();
        launchProgram(args);
    }

    public ApplicationRepository() {
    }

    /**
     * Gets the sole instance of the application repository within the
     * local JAC system.
     *
     * @return the application repository */

    public static ApplicationRepository get() {
        if (applicationRepository == null) {
            applicationRepository = new ApplicationRepository();
        }
        return applicationRepository;
    }

    /**
     * Adds an application within the repository.
     *
     * <p>When added, the application is initialized so that the needed
     * aspects are woven.
     *
     * @param app the application to add */

    public void addApplication(Application app) {
        logger.info("--- Launching " +app+ " ---"); 
        applications.put(app.getName(), app);
        app.init();
    }

    /**
     * Returns the applications that have been added to the repository.
     *
     * @return a hash map (application's name -> application)
     * @see #addApplication */
 
    public HashMap getApplications() {
        return applications;
    }

    /**
     * Gets an application from its name.
     *
     * @param name the application's name
     * @return the application named <code>name</code>
     * @see #getApplications() */
   
    public Application getApplication(String name) {
        return (Application) applications.get(name);
    }

    /**
     * Extends an application with a given aspect (only if this
     * application has a configuration for this aspect and if this
     * aspect is not yet woven).
     *
     * @param applicationName the application's name
     * @param aspectName the aspect's name */

    public void extend(String applicationName, String aspectName) {
        if (ACManager.get().getObject(applicationName+"."+aspectName)!=null)
            return;
        loggerAspects.info("extending application "+applicationName+
                           " with "+aspectName);
        Application application = getApplication(applicationName);
        if (application == null) {
            logger.error("No such application to extend: "+applicationName);
        }
        ACConfiguration acConf = application.getAcConfiguration(aspectName);
        if (acConf == null) {
            logger.error("No such AC configuration: "+aspectName+
                         " found in "+applicationName);
        }
        acConf.weave();
    }

    /**
     * Un-extends an application with a given aspect (only if this
     * application has a configuration for this aspect and if this
     * aspect is woven).
     *
     * @param applicationName the application's name
     * @param aspectName the aspect's name */

    public void unextend(String applicationName, String aspectName) {
        loggerAspects.info("un-extending application "+applicationName+
                           " with "+aspectName);
        Application application = getApplication(applicationName);
        if (application == null) {
            logger.error("No such application to extend: "+applicationName);
        }
        ACConfiguration acConf = application.getAcConfiguration(aspectName);
        if (acConf == null) {
            logger.error("No such AC configuration: "+aspectName+
                         " found in "+applicationName);
        }
        acConf.unweave();
    }

}
