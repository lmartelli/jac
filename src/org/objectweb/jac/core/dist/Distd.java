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

package org.objectweb.jac.core.dist;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Hashtable;
import org.apache.log4j.Logger;

/**
 * Distd is an abstract class for all daemon objects.
 * Its implementation is protocol dependent (eg CORBA or RMI).
 *
 * This is an abstract class that needs to be subclassed
 * (see org.objectweb.jac.dist.rmi.RMIDistd).
 *
 * Daemons hold containers which themselves hold remote objects.
 *
 * @see org.objectweb.jac.core.dist.Distd#init()
 * @see org.objectweb.jac.core.dist.Distd#newContainer(String)
 * @see org.objectweb.jac.core.dist.Distd#newContainer(String,String)
 * @see org.objectweb.jac.core.dist.Distd#run()
 *
 * @author <a href="http://www-src.lip6.fr/homepages/Lionel.Seinturier/index-eng.html">Lionel Seinturier</a>
 */
 
abstract public class Distd {
    static final Logger logger = Logger.getLogger("dist");

    /** The number of bytes that have been transmitted to the output of
        this deamon. */
    public static long outputCount = 0;
    /** The number of bytes that have been transmitted to the input of
        this deamon. */
    public static long inputCount = 0;

    /**
     * Store a string array into a hashtable.
     *
     * @param strs  the string array
     * @return      the hashtable
     */
    public static Hashtable stringArrayToHashtable(String[] strs) {
   
        Hashtable ret = new Hashtable();
      
        for ( int i=0 ; i < strs.length ; i++ ) {
            ret.put( strs[i], "" );
        }
      
        return ret;
    }

    /**
     * This abstract method initializes the underlying communication
     * protocol.  
     */
    abstract public void init();

    /**
     * This abstract method creates a new container.
     *
     * @param name  the container name
     * @return      the container reference
     */
    abstract protected RemoteContainer newContainer(String name) 
        throws Exception;

    /**
     * This abstract method creates a new container and
     * instantiates a given class.
     *
     * @param name       the container name
     * @param className  the name of the class to instantiate
     * @return           the container reference
     */
    abstract protected RemoteContainer newContainer(String name, String className) 
        throws Exception;

    /**
     * This abstract method enters the event loop of the underlying
     * communication protocol.
     */
    abstract public void run();

    /**
     * The is the main constructor of Distd.
     *
     * @param args  command line arguments
     */
    public Distd(String[] args) {

        /** Parse command line arguments */
        Hashtable hArgs = parseArguments(args, flags, options);
      
      
        /** Initialize the underlying communication protocol */
        init();

        try {
   
            /** Check the flags and options */
            verbose =
                (hArgs.get("v") == null && hArgs.get("-verbose") == null) ?
                false : true ;

            Object oNames = hArgs.get("_files");
            if (oNames == null)
                usage();
	 
            String[] names = (String[])oNames;
            if (names.length != 1)
                usage();
            String name = names[0];
      
      
            /** Instantiates a container. */
            RemoteContainer container = null;

            if (getClass().getClassLoader() instanceof DistdClassLoader) {
                logger.debug("DistdClassLoader...");
                Object classRepositoryName = hArgs.get("r");
                if (classRepositoryName == null) 
                    classRepositoryName = hArgs.get("-repository");
                if (classRepositoryName == null)
                    classRepositoryName = "s0";
                classRepositoryName = getFullHostName((String)classRepositoryName);
                DistdClassLoader.classRepositoryName = (String)classRepositoryName;
                referenceContainerName = (String) classRepositoryName;
            } else {
                //System.out.println("NOT DistdClassLoader");
            }
         
            Object className = hArgs.get("i");
            if (className == null)
                className = hArgs.get("-init");
	 
            // we try 20 times to launch it because protocol
            // initialization may take some time
            int ok = 20;
            while (ok>0) {
                try {
                    if (className == null) {
                        container = newContainer(name);
                    } else {
                        container = newContainer(name, (String)className);
                    }
                    logger.info("--- Distd started.");
                    ok = 0;
                } catch(Exception e) {
                    ok--;
                    if (ok == 0) {
                        logger.error("ERROR: distd did not start",e);
                    } else {
                        logger.debug("--- Distd starting try, "+ok+" left...");
                        Thread.sleep(200);
                    }
                }
            }

            containers.put(name, container);
            localContainerName = container.getName();
         
            /** Enter the daemon event loop. */
            run();

            if (getClass().getClassLoader() instanceof DistdClassLoader) {
                ((DistdClassLoader)getClass().getClassLoader())
                    .bootstrapping = false;
            }

        } catch(Exception e) { 
            logger.error("Distd "+Arrays.asList(args),e); 
        }
    }

    /** Store the reference container. */
    public static String referenceContainerName = null;

    /**
     * Get the full host name from an incomplete host name.<p>
     *
     * For instance, if the local host name is h1:<p>
     *
     * <ul>
     *   <li>"s0" &rarr; "//h1/s0"</li>
     *   <li> "//localhost/s1" &rarr; "//h1/s0</li>
     *   <li>"//h2/s0" &rarr; "//h2/s0"</li>
     *   <li>"\\h2\s0" &rarr; "//h2/s0" (for Windows command line compatibility)</li>
     * </ul>
     * 
     * @param name the incomplete host name
     * @return the complete host name 
     */   
    public static String getFullHostName(String name) {
        String fullname = "";
        name = name.replace('\\','/');
        if (!name.startsWith("//")) {
            try {
                fullname = "//"+InetAddress.getLocalHost().getHostName()+"/"+name;
            } catch(Exception e) {
                logger.error("getFullHostName "+name,e); 
            }
        }
        if (name.startsWith("//localhost/")) {
            try {
                fullname = "//" + InetAddress.getLocalHost().getHostName() + 
                    "/" + name.substring(12);
            } catch(Exception e) { 
                logger.error("getFullHostName "+name,e); 
            }
        }
        if (fullname.equals(""))
            fullname = name;
        return fullname;
    }

    /** Store the reference container name. */
    //public static String reference_container_name = null;
   
    /** Returns the reference container. */
    //public static RemoteContainer getReferenceContainer () {
    //   //      System.out.println ( "resolving container " + reference_container_name );
    //   if ( reference_container == null && reference_container_name != null ) {
    //      reference_container = RemoteContainer.resolve( reference_container_name );
    //   }
    //   return reference_container;
    //}

    /** Store the local container name. */
    protected static String localContainerName = "";
   
    /** Get the local container name. */ 
    public static String getLocalContainerName () {
        return localContainerName;
    }
   
    /** Registered flags. */
    protected static final String[] flags = {"v","-verbose"};
   
    /** Registered options. */
    protected static final String[] options = {"i","-init","r","-repository"};

    /** verbose tells whether information message should be printed or not */
    protected static boolean verbose = false;


    /** Display command line arguments. */
   
    protected static void usage() {
        System.out.println(
            "Usage: java org.objectweb.jac.core.dist.Distd [options] name\n" +
            "\n" +
            "-v --verbose: display system informations\n" +
            "-i --init classname: create an instance of classname\n" +
            "-r --repository sitename: the site where the classes repository is located (the loader will load the classes from this site if possible -- else from the local file system)\n" +
            "name: the daemon identifier"
        );
        System.exit(1);
    }

    /**
     * Parse command line arguments composed of flags, options and files.
     *
     * @param args      command line arguments
     * @param flags     registered flags (e.g. -verbose, -quiet, etc.)
     * @param options   registered options (e.g. -d classes, etc.)
     * @return          a hashtable containing 3 types of entries:
     *		       one for each flag and option, and one for the files.
     *		       If an unregistered flag or option is encountered the
     *		       usage method is called.
     *		       The value associated to the entries is a empty string
     *		       for flags,
     *		       and the option value (e.g. classes/) for options.
     *		       Files are stored with an entry whose key is "_files",
     *		       and whose value is an array of strings.
     */
    protected static Hashtable parseArguments(String[] args, 
                                              String[] flags, 
                                              String[] options) {
      
        /** Store flags and options in hashtables */
      
        Hashtable hFlags = stringArrayToHashtable(flags);
        Hashtable hOptions = stringArrayToHashtable(options);
      
      
        /** Scan the arguments */
      
        Hashtable result = new Hashtable();
      
        String flagOrOption;
        Object flag, option;
      
        for (int i=0; i<args.length; i++) {
            if (args[i].startsWith("-")) {
                // This is either a flag or an option
                flagOrOption = args[i].substring(1);
                flag = hFlags.get(flagOrOption);
                option = hOptions.get(flagOrOption);
	    
                if (flag != null) {
                    result.put(flagOrOption, "");
                } else if (option != null) {
                    // Move to the value of the option
                    i++;
                    if (i >= args.length) { 
                        usage();
                    }
                    result.put(flagOrOption,args[i]);
                } else {
                    usage();
                }
            } else {
                // This is neither a flag, nor an option.
                // This must be the begining of file names.
                int numberOfFiles = args.length - i;
                String[] files = new String[numberOfFiles];
                System.arraycopy(args, i, files, 0, numberOfFiles);
	    
                result.put("_files", files);
	    
                return result;
            }
        }
      
        return result;
    }

    /** Containers hold by the current daemon. */
    protected static Hashtable containers = new Hashtable();

   
    /**
     * Test whether the daemon contains a given container.
     *
     * @param container the container's name
     * @return true if the container is contained in the current daemon
     */    
    public static boolean containsContainer(RemoteContainer container) {
        return containers.values().contains(container);
    }
}
