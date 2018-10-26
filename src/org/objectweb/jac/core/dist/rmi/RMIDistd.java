/*
  Copyright (C) 2001 Lionel Seinturier

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 2 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser Generaly Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA */

package org.objectweb.jac.core.dist.rmi;

import java.io.File;
import java.io.IOException;
import java.lang.Runtime;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.dist.Distd;
import org.objectweb.jac.core.dist.RemoteContainer;

/**
 * RMIDistd is a jac daemon that supports the RMI communication protocol.
 *
 * Daemons hold containers (only one for the moment) which themselves hold
 * remote objects.
 *
 * @author <a href="http://www-src.lip6.fr/homepages/Lionel.Seinturier/index-eng.html">Lionel Seinturier</a>
 */
 
public class RMIDistd extends Distd {
    static final Logger logger = Logger.getLogger("dist.rmi");

    /**
     * This method initializes the RMI communication protocol.
     */   
    public void init() {

        /** Create and install a security manager */
       
        if (System.getSecurityManager() == null)
            System.setSecurityManager(new RMISecurityManager());

        try {
            File path;
            File jar = new File(org.objectweb.jac.core.Jac.getJacRoot()+"jac.jar");
            logger.debug("$JAC_ROOT/jac.jar = "+jar.getPath());
            if (jar.exists()) {
                path = jar;
            } else {
                path = new File(org.objectweb.jac.core.Jac.getJacRoot()+"classes");
            }
            logger.debug("Launching rmiregistry with CLASSPATH="+path.getPath());
            rmiProcess = Runtime.getRuntime().exec(
                new String[] { "rmiregistry", 
                               "-J-classpath", 
                               "-J"+path.getPath()});
        } catch (IOException e) {
            logger.error("Could not start rmiregistry",e);
        }
    }

    static Process rmiProcess = null;

    static {
        Runtime.getRuntime().addShutdownHook(
            new Thread() {
                    public void run() {
                        if (rmiProcess!=null) {
                            logger.info("Stopping rmiregistry...");
                            rmiProcess.destroy();
                        }
                    }
                }
        );
    }

    /**
     * This method creates a new container and returns it.
     *
     * @param name  the container name
     * @return      the container reference
     */   
    protected RemoteContainer newContainer(String name) throws Exception {

        RMIRemoteContainer remCont = null;
      
        try { 
            remCont = new RMIRemoteContainer(verbose); 
        } catch(Exception e) { 
            logger.error("newContainer("+name+
                         "): Instantiation of RMIRemoteContainer failed",e); 
            return null; 
        }
      
        //try {
            registerContainer(remCont, name);
            /*
        } catch(Exception e) {
            // It may fail because rmiregistry is not ready yet
            // But we don't care since Distd will retry
            throw new RuntimeException(e.toString());
        }
            */
        remCont.getDelegate().setName(getFullHostName(name));

        return remCont.getDelegate();
    }
  

    /**
     * This method creates a new container, instantiates a given class, and
     * returns the container.
     *
     * @param name       the container name
     * @param className  the name of the class to instantiate
     * @return           the container reference
     */   
    protected RemoteContainer newContainer(String name, String className) 
        throws Exception 
    {
        RMIRemoteContainer remCont = null;
      
        try { 
            remCont = new RMIRemoteContainer(className,verbose); 
        } catch( Exception e ) { 
            e.printStackTrace(); 
            return null; 
        }

        registerContainer(remCont, name);

        remCont.getDelegate().setName(getFullHostName(name));

        return remCont.getDelegate();
    }
   

    /**
     * Registers a container in the RMI registry.
     *
     * @param container  the container reference
     * @param name       the container name
     */   
    protected void registerContainer(
        RMIRemoteContainer container,
        String name) 
        throws java.rmi.RemoteException 
    {
        /** Register the container in the RMI registry */
        String fullName = getFullHostName(name);
	 
        try {
            Naming.rebind(fullName, container);
        } catch( java.net.MalformedURLException e ) {
            logger.error("registerContainer("+container+","+name+")",e);
        }

        logger.info(
            "--- JAC Distd (RMI): new container " + fullName + " ---"
        );
        logger.info(
            "--- Default class repository: " + referenceContainerName + " ---"
        );
	 
    }

    /**
     * This method enters the event loop of
     * the underlying communication protocol.
     */
    public void run() {
        logger.info( "--- JAC Distd (RMI) is running ---" );
    }

    /**
     * The is the main constructor.
     *
     * @param args  command line arguments
     */   
    public RMIDistd(String[] args) { 
        super(args); 
    }

    public static void main(String[] args) { 
        new RMIDistd(args);
    }
   
}
