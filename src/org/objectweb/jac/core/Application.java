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

import java.io.*;
import java.util.*;
import org.apache.log4j.Logger;
import org.objectweb.jac.util.*;

/**
 * This class represents the JAC applications.
 *
 * <p>Before it is actually started, each application must define a
 * name, some path informations, and a set of aspect configurations
 * (one for each aspect the application needs to use) by instantiating
 * and configuring this class. Then, the system registers the new
 * application instance into the application repository of the system.
 *
 * <p>At a user-level, an application configuration is described in
 * application descriptor files (*.acc). A typical application
 * descriptor is:
 *
 * <pre class=code>
 * // file myApp.acc
 * applicationName: myApp
 * lauchingClass: myPath.Run
 * aspects: \
 *     rtti rtti.acc true \
 *     session session.acc false \
 *     persistence persistence.acc true \
 *     gui gui.acc true
 * </pre>
 *
 * @see #Application(String,String,String,String[])
 * @see #addAcConfiguration(ACConfiguration)
 * @see ACConfiguration#ACConfiguration(Application,String,String,boolean)
 * @see ApplicationRepository */

public class Application implements Serializable {
    static Logger logger = Logger.getLogger("application");

    String name;
    String path;
    String constructorClass;
    String[] arguments;
    /*org.objectweb.jac.lib.java.util.*/Vector acConfigurations = 
    new /*org.objectweb.jac.lib.java.util.*/Vector();
    Hashtable acs = new Hashtable();
    boolean instantiated = false;

    Properties props = new Properties();

    /**
     * Creates a new application.
     *
     * @param name the name of the application
     * @param path the path of the application (root directory). If
     * it's null, the current directory is used
     * @param constructorClass the path of the launching class
     * accessible within the current class path)
     * @param arguments the launching arguments */

    public Application(String name, String path,
                       String constructorClass, String[] arguments) {
        this.name = name;
        if (path==null) {
            path = System.getProperty("user.dir");
        }
        this.path = path;
        this.constructorClass = constructorClass;
        this.arguments = arguments;
        // put the application in the context of this thread
        Collaboration.get().setCurApp(name);
    }

    /**
     * Inits the application by creating and weaving the aspects
     * configuration that are not woven on demand. */

    public void init() {
        Iterator it = acConfigurations.iterator();
        while( it.hasNext() ) {
            ACConfiguration acConf = (ACConfiguration) it.next();
            if ( ! acConf.getWeaveOnDemand() ) {
                //acConf.weave();
                ApplicationRepository.get().extend(name,acConf.getName());
            }
        }
    }


    /**
     * Gets the name of the application.
     *
     * @return the application's name */

    public String getName() {
        return name;
    }

    /**
     * Sets the application's name.
     *
     * @param name the application's name */

    public void setName(String name) {
        this.name = name;
    }   

    /**
     * Gets the path of the application (its root directory).
     *
     * @return the application's path
     * @see #setPath(String) */

    public String getPath() {
        return path;
    }

    /**
     * The path's setter.
     *
     * @param path the new application root directory
     * @see #getPath() */

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Gets the path of the launching class (that implements the static
     * <code>main(String[])</code> method).
     *
     * @return the launching class */

    public String getConstructorClass() {
        return constructorClass;
    }

    /**
     * Gets the path of the launching class (that implements the static
     * <code>main(String[])</code> method).
     *
     * @param constructorClass the launching class */

    public void setConstructorClass(String constructorClass) {
        this.constructorClass = constructorClass;
    }

    /**
     * Tells if this application currently realizes a given aspect
     * component. <b>Not available yet</b>.
     *
     * @param acName the aspect component name
     * @return true if the application realizes the given aspect */

    public boolean realizes(String acName) {
        return false; //acs.containsKey( "acName" );
    }

    /**
     * Tells if this application configures a given aspect
     * component. <b>Not available yet</b>. 
     *
     * @param acName the aspect component name
     * @return true if the application configures the given aspect */

    public boolean configures(String acName) {
        return false; //acs.containsKey( "acName" );
    }      

    /**
     * Adds an aspect component configuration for this application.
     *
     * @param configuration the new configuration */

    public void addAcConfiguration(ACConfiguration configuration) {
        acConfigurations.add(configuration);
    }

    /**
     * Removes an aspect component configuration for this application.
     *
     * @param configuration the configuration to remove */

    public void removeAcConfiguration(ACConfiguration configuration) {
        acConfigurations.remove(configuration.getName());
    }

    /**
     * Returns all the configurations for the current application.
     *
     * @return a collection of configurations */

    public Collection getAcConfigurations() {
        return (org.objectweb.jac.lib.java.util.Vector)acConfigurations.clone();
    }

    /**
     * Gets a configurations from its name (the name of the aspect as
     * defined in the <code>jac.prop</code> file.
     *
     * @param name the aspect name
     * @return the corresponding configuration */

    public ACConfiguration getAcConfiguration(String name) {
        Iterator i = acConfigurations.iterator();
        while(i.hasNext()) {
            ACConfiguration conf = (ACConfiguration)i.next();
            if (conf.getName().equals(name))
                return conf;
        }
        return null;
    }

    /**
     * Starts the application with its current aspect component
     * configurations.
     *
     * <p>If the application is already instantiated or if the launching
     * path is not found, then do nothing. 
     */

    public void start() {
        if (instantiated) {
            logger.warn("application '" + name + "' is already instantiated");
            return;
        }
        try {
            logger.info("launching application "+this);
            Class.forName(constructorClass)
                .getMethod( "main", new Class[] { String[].class } )
                .invoke( null, new Object[] { arguments } );
            instantiated = true;
            Iterator it = acConfigurations.iterator();
            while( it.hasNext() ) {
                ACConfiguration conf = (ACConfiguration) it.next();
                if( ! conf.weaveOnDemand ) {
                    conf.weave();
                }
            }

        } catch(Exception e) {
            logger.error("application '" + name + "' unable to start",e);
        }
    }


    /**
     * Returns a string representation of this application.
     * @return a string */

    public String toString() {
        return "Application " + name;
    }

}
