/*
  Copyright (C) 2001-2002 Renaud Pawlak, Laurent Martelli, Lionel Seinturier.

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
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.objectweb.jac.util.*;

/**
 * This is the main class of org.objectweb.jac. It launches a JAC container/server
 * that is called s0 by default.
 *
 * If an application descriptor is given as a last parameter, it runs
 * the corresponding JAC program on the newly created JAC container by
 * using a customized class loader <code>JacLoader</code>.
 *
 * <p>Use the following command to run <code>MyApp.main()</code> with
 * <code>arg1, ...</code>:
 *
 * <pre>
 * <b>% java org.objectweb.jac.core.Jac [options] [app.jac arg1, ...]</b>
 *
 * where 'app.jac' is a JAC application descriptor
 *
 * where options are:
 *    -r (release): JAC displays the release number
 *    -v (verbose): the JAC class loader is set to verbose
 *    -d (debug)  : the JAC class loader is set to debugging mode
 *    -c (clean)  : JAC cleans the tempory classes directory (previously 
 *                  created with 'write' option)
 *    -w (write)  : the JAC class loader writes on disk all the translated
 *                  classes (and use them for the next run)
 *    -V [server:]logname  : set the log to verbose mode
 *    -L file     : redirect all the logs to a file
 *    -D [name] (dist): JAC runs in distributed mode, i.e. it constructs a 
 *                  distributed JAC system. If no name is given, the default is s0.
 *    -M [name]   : defines a master site where all the bytecode will be
 *                  fetched (by default, classes are loaded from the local
 *                  file system).
 *    -R dir      : specify jac_root directory
 *    -G [gui,...] : launch a list of customized GUI
 *    -G app@server:[gui,...] : launch a list of customized GUIs
 *                  with the swing GUI on a remote server for an application
 *    -A name     : launches an administration GUI on a remote server called 'name'
 *                  and do not start org.objectweb.jac.
 *    -W [gui[:port],...] : start the Web GUI server and provide access to some customized GUIs.
 *    -C <clpath> : specify a particular classpath that will replace $CLASSPATH
 *    -a application aspect server : reload the configuration for an aspect on a server.
 *    -n application aspectClassName server aspectConfPath: create a new aspect on a server.
 *    -u application aspect server : unweave an aspect on a server.
 *    -x          : redirect ouput to out.txt
 *    -h (help)   : display the help.
 * </pre>
 *
 * @see JacLoader
 * @author Renaud Pawlak
 * @author Lionel Seinturier
 * @author Laurent Martelli */

public class Jac {

    /** Memorize when the JAC system was launched. */
    static Date start_time = new Date();

    /**
     * Returns the JRE version on which JAC is currently running. */
    public static String getFullJavaVersion() {
        return System.getProperties().getProperty("java.version");
    }

    /**
     * Returns the main JRE version (only the 2 first numbers, e.g. 1.3
     * or 1.4) on which JAC is currently running. */
    public static String getMainJavaVersion() {
        return System.getProperties().getProperty("java.version").substring(0,3);
    }

    /**
     * Returns the date when the JAC system was launched.
     *
     * @return the date when JAC was launched */

    public static Date getStartTime() {
        return start_time;
    }

    /** Verbose flag. */
    private static boolean verbose = false;

    /** Debug flag (more verbose). */
    private static boolean debug = false;

    /** Generate flag. */
    private static boolean gen = false;

    /** Clean flag. JAC cleans the temporary directory of translated
        class files. */
    private static boolean clean = false;

    /** Write flag. JAC writes the translated class files into a
        temporary directory. */
    private static boolean write = false;

    /** Server flag. JAC is launched in server mode. It waits for
        remote call and instantiations. */
    private static boolean server = false;

    /** Client flag. JAC binds to the namespace and the aspect-space. */
    private static boolean client = false;

    /** Stores the client host name in client or bind mode */
    private static String clientHost = "";

    /** Stores the server host name in bind mode */
    private static String serverHost = "";

    /** Stores the master server host name */
    private static String master = "";

    /** Distributed flag. JAC bootstraps a namespace and an
        aspect-space on the topology (a set of JAC servers). Then, it
        launches a deployment program. */
    private static boolean distributed = false;

    /** Store the class repository container name. */
    private static String classRepository = "";

    /** Internally used flag, starts a program if true. Set to false if
        an error occurs when parsing the options. */
    private static boolean start = true;

    /** The Javassist class loader. */
    //public static javassist.Loader classLoader;
    public static JacLoader classLoader;


    /** The JAC root directory. */
    private static String jac_root = "";

    /** The class that is launched when JAC is started. */
    private static String launchedClass = "";

    /** A flag that tell if the GUI must be started. */
    private static String[] startSwingGUI = null;

    /** A list of customized GUI IDs to be made available through the WEB */
    private static String[] startWebGUI = null;

    private static Hashtable logLevels = new Hashtable();

    private static String logFileName;

    /** Stores the server host name in bind mode */
    private static String remoteGuiServerName = null;

    /** alternative classpath, added with -C option */
    public static ClassLoader otherClasspath;

    static final String version = "0.12";

    static Logger logger = Logger.getLogger("jac");
    static Logger perf = Logger.getLogger("perf");
    static Logger classpath = Logger.getLogger("classpath");
    static Logger urlloader = Logger.getLogger("urlloader");

    /**
     * The entry point of the JAC system.
     *
     * <p>Creates a JAC loader that will load the classes and translate them.
     *
     * @param args the command-line options
     *
     * @see JacLoader 
     */
    public static void main(String[] args) throws Throwable {
        Logger root = Logger.getRootLogger();
        root.addAppender(
            new ConsoleAppender(
                new PatternLayout("%d %p %c: %x %m%n")));
        root.setLevel(Level.WARN);
        Logger.getLogger("jac").setLevel(Level.INFO);
        Logger.getLogger("gui").setLevel(Level.INFO);
        Logger.getLogger("dist").setLevel(Level.INFO);
        Logger.getLogger("props").setLevel(Level.ERROR);
        logger.info("JAC version "+version);
      
        args = parseOptions(args);

        classLoader = new JacLoader(write, clean, otherClasspath);

        if (remoteGuiServerName!=null) {
            remoteInvoke(remoteGuiServerName,"launchGUI", new Object[0]);
            System.exit(0);
        }

        // launch RMI
        //Process p = Runtime.getRuntime().exec("rmiregistry");
        //p.waitFor();//Thread.currentThread().sleep(3000);

        Class daemon = null;

        /** Try to launch a Jac server on the local host */
        if (!master.equals("")) {
            try {
                ClassLoader cl =
                    (ClassLoader)Class.forName("org.objectweb.jac.core.dist.DistdClassLoader")
                    .newInstance ();
                Class jac = cl.loadClass("org.objectweb.jac.core.Jac");
                jac.getMethod("setJacRoot", new Class[] {String.class}).invoke(
                    null,new Object[] {jac_root});

                daemon = cl.loadClass("org.objectweb.jac.core.dist.rmi.RMIDistd");
                //System.out.println("-r "+master+" "+serverHost);
                daemon.getConstructor( new Class[] { String[].class } )
                    .newInstance ( new Object [] {
                        new String[] {"-r",master,serverHost} } );

                Class bootstrap =
                    cl.loadClass("org.objectweb.jac.aspects.distribution.bootstrap.DistBootstrap");
                bootstrap.getMethod("main", new Class[] {String[].class}).invoke(
                    null,new Object[] {new String[0]});

                if (start) {
                    long _start_time = System.currentTimeMillis();

                    ///** start the Aspect Component manager */
                    //classLoader.run ( "org.objectweb.jac.core.ACManager", null );
                    Class acm = cl.loadClass("org.objectweb.jac.core.ACManager");
                    acm.getMethod("main", new Class[] {String[].class}).invoke(
                        null,new Object[] {new String[0]});

                    /** runs the application if any */
                    if (args.length > 0) {
                        Class appRep = cl.loadClass("org.objectweb.jac.core.ApplicationRepository");
                        appRep.getMethod("main", new Class[] {String[].class}).invoke(
                            null,new Object[] {args});
                    }
                    perf.info("application started in "+
                              (System.currentTimeMillis()-_start_time)+"ms");
                    //Class jacObject = cl.loadClass ( "org.objectweb.jac.core.JacObject" );
                    //jacObject.getMethod("main", new Class[] {String[].class}).invoke(
                    //   null,new Object[] {new String[0]});
                    //Semaphore waitingSemaphore = new Semaphore();
                    //waitingSemaphore.acquire();
                }

            } catch (Exception e) {
                System.out.println ("Error: cannot launch Jac server.");
                e.printStackTrace();
                while (true) {}
            }

        } else {

            long _start_time = System.currentTimeMillis();
            if (distributed) {
                daemon = classLoader.loadClass("org.objectweb.jac.core.dist.rmi.RMIDistd");
                daemon.getConstructor(new Class[] { String[].class })
                    .newInstance ( new Object [] { new String[] {serverHost} } );
            }
            if (client) {
                daemon = classLoader.loadClass("org.objectweb.jac.core.dist.rmi.RMIDistd");
                daemon.getConstructor(new Class[] { String[].class })
                    .newInstance ( new Object [] { new String[] { clientHost } } );
            }

            /*
              Class jac = classLoader.loadClass ( "org.objectweb.jac.core.Jac" );

              Jac.getMethod( "setJacRoot", new Class[] { String.class } ).invoke(
              null, new Object[] { jac_root } );
              if ( startSwingGUI() ) {
              Jac.getMethod( "setStartSwingGUI", new Class[] {String[].class} ).invoke(
              null, new Object[] {startSwingGUI} );
              }
              if ( startWebGUI() ) {
              Jac.getMethod( "setStartWebGUI", new Class[] {String[].class} ).invoke(
              null, new Object[] {startWebGUI} );
              }
            */

            if (start) {

                classLoader.loadClass("org.objectweb.jac.util.Repository");
                if (distributed || client || server) {
                    /** bootstrap the distributed JAC system */
                    classLoader.run("org.objectweb.jac.aspects.distribution.bootstrap.DistBootstrap", null);
                }
                //if ( client ) {
                //   /** bind to the distributed JAC system */
                //   classLoader.run("org.objectweb.jac.aspects.distribution.bootstrap.BindClient",
                //                   new String[] {clientHost});
                //}
                /** start the Aspect Component manager */
                classLoader.run("org.objectweb.jac.core.ACManager",null);

                /** runs the application if any */
                if (args.length > 0) {
                    classLoader.run("org.objectweb.jac.core.ApplicationRepository", args);
                }
                //classLoader.run ( "org.objectweb.jac.core.JacObject", null );
                //Semaphore waitingSemaphore = new Semaphore();
                //waitingSemaphore.acquire();
            }
            perf.info("application started in "+
                      (System.currentTimeMillis()-_start_time)+"ms");

        }
    }


    /**
     * Transforms a classpath into a list of URLs
     *
     * @param path the classpath
     */
    private static URL[] parseClasspath(String path)
    {
        String[] tab = Strings.splitPath(path);

        URL[] result = new URL[tab.length];

        try {
            for (int i = 0; i < tab.length; i++) {
                urlloader.info("adding "+new File(tab[i]).toURL());
                result[i] = new File(tab[i]).toURL();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Parse the command line and extract the options to set the values
     * of the appropriate fields.
     *
     * @param args the command-line options */

    private static String[] parseOptions(String[] args) {
        int i = 0;
        String[] ret = args;
        if (args.length == 0) {
            start = false;
            displayLaunchingHelp();
            return ret;
        }
        try {
        parse:
            while((i < args.length) && args[i].startsWith("-")) {
                String current =
                    (args[i].length()>2)
                    ? args[i].substring(2)
                    : null;
                switch (args[i].charAt(1)) {
                    case '-':
                        i++;
                        break parse;
                    case 'r':
                        System.out.println( "- JAC version "+version+" - " +
                                            "Get new release at http://org.objectweb.jac.aopsys.com/ -" );
                        break;
                    case 'v':
                        verbose = true;
                        break;
                    case 'd':
                        debug = true;
                        break;
                    case 'g':
                        gen = true;
                        start = false;
                        break;
                    case 'w':
                        write = true;
                        break;
                    case 'c':
                        clean = true;
                        break;
                    case 'C': // new Classpath
                        i++;
                        String[] cp = Strings.splitPath(args[i]);
                        String newClassPath = "";

                        // transform relative paths into absolute paths
                        for (int j = 0; j < cp.length; j++)
                        {
                            File file = new File(cp[j]);
                            cp[j] = file.getAbsolutePath();
                            if (newClassPath.length() != 0)
                                newClassPath += System.getProperty("path.separator");
                            newClassPath += cp[j];
                        }

                        classpath.info("java.class.path="+System.getProperty("java.class.path"));
                        // reset java.class.path for resources search
                        System.setProperty("java.class.path",
                                           newClassPath
                                           + System.getProperty("path.separator")
                                           + System.getProperty("java.class.path"));

                        otherClasspath =
                            new URLClassLoader(parseClasspath(newClassPath));

                        break;
                    case 'D':
                        distributed = true;
                        if( (i+1>=args.length) || args[i+1].startsWith("-") ||
                            args[i+1].endsWith(".jac")) {
                            serverHost = "s0";
                        } else {
                            i++;
                            serverHost = args[i];
                        }
                        break;
                    case 'M':
                        i++;
                        master = args[i];
                        break;
                    case 'V':
                        {
                            String category = current;
                            if (category==null) {
                                i++;
                                category = args[i];
                            }
                            int index = category.indexOf(':');
                            String serverName=null;
                            if (index!=-1) {
                                serverName = category.substring(0,index);
                                category = category.substring(index+1);
                            }
                            int equalIndex = category.indexOf('=');
                            Level level = Level.DEBUG;
                            if (equalIndex!=-1) {
                                try {
                                    level = 
                                        level.toLevel(
                                            Integer.parseInt(category.substring(equalIndex+1)));
                                } catch (NumberFormatException e) {
                                    level = Level.toLevel(category.substring(equalIndex+1));
                                }
                                category = category.substring(0,equalIndex);
                            }
                            if (serverName!=null) {
                                remoteSetTrace("",serverName,category,level.toInt());
                                System.exit(0);
                            } else {
                                Logger.getLogger(category).setLevel(level);
                            }
                        }
                        break;
                    case 'L':
                        if (current==null) {
                            i++;
                            current = args[i];
                        }
                        logFileName = current;
                        Logger.getRootLogger().addAppender(
                            new FileAppender(
                                new PatternLayout(PatternLayout.TTCC_CONVERSION_PATTERN),
                                logFileName));
                        break;
                    case 'h':
                        start = false;
                        displayLaunchingHelp();
                        break;
                    case 'R':
                        i++;
                        Jac.setJacRoot(args[i]);
                        break;
                    case 'G':
                        {
                            if (current==null) {
                                i++;
                                current = args[i];
                            }
                            String guis = current;
                            int index = guis.indexOf(':');
                            String serverName = null;
                            if (index!=-1) {
                                serverName = guis.substring(0,index);
                                guis = guis.substring(index+1);
                            }
                            if (serverName==null) {
                                Jac.setStartSwingGUI(Strings.split(guis,","));
                            } else {
                                index = serverName.indexOf('@');
                                if (index!=-1) {
                                    remoteStartSwingGUI(serverName.substring(0,index),
                                                        serverName.substring(index+1),
                                                        Strings.split(guis,","));
                                    System.exit(0);
                                } else {
                                    logger.fatal("No application name specified "+
                                                 "(appName@sever:guiName");
                                    System.exit(1);
                                }
                            }
                        }
                        break;
                    case 'x':
                        PrintStream stream =
                            new PrintStream(new FileOutputStream("out.txt"));
                        System.setOut(stream);
                        System.setErr(stream);
                        break;
                    case 'A':
                        i++;
                        remoteGuiServerName = args[i];

                        start = false;
                        break;
                    case 'a':
                        {
                            i++;
                            String applicationName = args[i];
                            i++;
                            String aspectName = args[i];
                            i++;
                            String serverName = args[i];
                            remoteReloadAspect(applicationName,serverName,aspectName);
                            start = false;
                            System.exit(0);
                        }
                        break;
                    case 'u':
                        {
                            i++;
                            String applicationName = args[i];
                            i++;
                            String aspectName = args[i];
                            i++;
                            String serverName = args[i];
                            remoteUnweaveAspect(applicationName,serverName,aspectName);
                            start = false;
                            System.exit(0);
                        }
                        break;
                    case 'n':
                        {
                            i++;
                            String applicationName = args[i];
                            i++;
                            String aspectName = args[i];
                            i++;
                            String serverName = args[i];
                            i++;
                            String confPath = args[i];
                            remoteWeaveAspect(applicationName,serverName,aspectName,confPath);
                            start = false;
                            System.exit(0);
                        }
                        break;
                    case 'W':
                        if (client || server) {
                            System.out.println("Incompatible options "+
                                               "(-W, -S, and -C are incompatible)");
                            displayLaunchingHelp();
                        }
                        i++;
                        Jac.setStartWebGUI(Strings.split(args[i],","));
                        break;
                    case 't':
                        String aspect = args[++i];
                        String config = args[++i];
                        try {
                            ACManager.main(new String [0]);
                            Logger.getLogger("aspects.config").setLevel(Level.DEBUG);
                            Logger root = Logger.getRootLogger();                            
                            root.removeAllAppenders();
                            root.addAppender(
                                new ConsoleAppender(
                                    new PatternLayout("%p %m%n")));
                            AspectComponent ac = 
                                (AspectComponent)Class.forName(
                                    ACManager.getACM().getACPathFromName(aspect)).newInstance();
                            ac.configure(aspect,config);
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Wrong option: " + args[i]);
                        displayLaunchingHelp();
                }
                if (args[i].startsWith("-S")) { i++; break; }
                if (args[i].equals("-g")) { break; }
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();

            System.out.println("Error while arguments parsing");
            displayLaunchingHelp();
        }

        //if(i == args.length) {
        //start = false;
        //   return ret;
        //}
        if (i>0) {
            ret = new String[args.length - i];
            System.arraycopy(args, i, ret, 0, args.length - i);
        }
        return ret;
    }

    /**
     * Reload an aspect on a remote server
     * @param application the application for which to reload an aspect
     * @param server the name of the JAC server on which the application runs
     * @param aspect the name of the aspect to reload
     */
    public static void remoteReloadAspect(String application, String server,
                                          String aspect)
        throws Exception
    {
        remoteInvoke(server,"reloadAspect",
                     new Object[] { application, aspect });
    }

    public static void remoteUnweaveAspect(String application, String server,
                                           String aspect)
        throws Exception
    {
        remoteInvoke(server,"unweaveAspect",
                     new Object[] { application, aspect });
    }

    public static void remoteWeaveAspect(String application, String server,
                                         String aspect,String aspectConfPath)
        throws Exception
    {
        remoteInvoke(server,"weaveAspect",
                     new Object[] { application, aspect, aspectConfPath });
    }



    /**
     * Sets a trace loggin level on a remote server.
     *
     * @param application the application for which to reload an aspect
     * @param server the name of the JAC server on which the application runs
     * @param category the trace category
     * @param level the trace level to set for the category
     */
    public static void remoteSetTrace(String application, String server,
                                      String category, int level)
        throws Exception
    {
        remoteInvoke(
            server,
            "setTrace",
            new Object[] { application, category, new Integer(level) });
    }

    /**
     * Starts a swing GUI on a remote server.
     *
     * @param application the application for which start the GUI
     * @param server the name of the JAC server on which the application runs
     * @param guiNames the names of the GUI windows to start
     */
    public static void remoteStartSwingGUI(String application, String server,
                                           String[] guiNames)
        throws Exception
    {
        remoteInvoke(
            server,
            "startSwingGUI",
            new Object[] { application, guiNames });
    }

    /**
     * Invoke a distant method on the JAC_topology object.
     *
     * @param server server name where to find the JAC_topology object
     * @param method name of the method to invoke
     * @param args parameters for method invocation
     */
    static Object remoteInvoke(String server, String method, Object[] args) 
        throws Exception 
    {
        // Argl!! I hate reflection in Java!
        try {
            Class rcClass = Class.forName("org.objectweb.jac.core.dist.RemoteContainer");
            Object rc = rcClass.
                getMethod("resolve",new Class[] {String.class}).
                invoke(null,new Object[] {server} );
            if (rc==null) {
                throw new RuntimeException("Could not find a JAC server with that name: "+server);
            } else {
                Object topology = rcClass.
                    getMethod("bindTo",new Class[] {String.class}).
                    invoke(rc,new Object[] {"JAC_topology"} );
                return Class.forName("org.objectweb.jac.core.dist.RemoteRef")
                    .getMethod("invoke",new Class[]{String.class,Object[].class})
                    .invoke(topology, new Object[] { method, args });
            }
        } catch (Exception e) {
            logger.error("remoteInvoke("+server+"."+method+") failed",e);
            throw e;
        }
    }

    /**
     * Print the help (displayed when the -h option is set). 
     */
    public static void displayLaunchingHelp() {
        System.err.println(
            "Launch a JAC container with :\n"+
            "% java org.objectweb.jac.core.Jac [options] [app.jac arg1, ...]\n" +
            "  where 'app.jac' is a JAC application descriptor\n" +
            "  where options are:\n" +
            "    -r (release): JAC displays the release number\n" +
            "    -v (verbose): set JAC class loader to verbose\n" +
            "    -d (debug)  : the JAC class loader is set to debugging mode\n" +
            "    -c (clean)  : JAC cleans the tempory classes directory (previously created\n"+
            "                  with 'write' option)\n" +
            "    -w (write)  : the JAC class loader writes on disk all the translated classes\n"+
            "                  (and use them for the next run)\n" +
            "    -V [server:]log: JAC activates the corresponding log on server\n"+
            "    -L file     : redirect all the logs to the given file\n"+
            "    -D [name] (dist): launches precises the name of the JAC container's daemon\n"+
            "                  for distributed mode. If no name is given, default is s0.\n"+
            "    -M [name]   : defines a master site where all the bytecode will be\n"+
            "                  fetched (by default, classes are loaded from the local\n"+
            "                  file system).\n"+
            "    -R dir      : specify jac_root directory\n"+
            "    -G [gui,...] : launch a list of customized GUIs with the swing GUI\n"+
            "    -G app@server:[gui,...] : launch a list of customized GUIs with the swing GUI\n"+
            "                  on a remote server for an application\n"+
            "    -A <name>   : launches an administration GUI on a remote server called 'name'\n"+
            "                  and do not start org.objectweb.jac.\n"+
            "    -W gui[:port][,...] : launch the GUI web server for a list of customized GUIs\n"+
            "    -C <clpath>  : specify a particular classpath that will replace $CLASSPATH\n" +
            "    -a application aspect server : reload the configuration for an aspect on a server\n"+
            "    -n application aspectClassName server aspectConfPath: create a new aspect\n"+
            "       on a server for a given application.\n"+
            "    -u application aspect server : unweave an aspect on a server.\n"+
            "    -x          : redirect ouput to out.txt\n"+
            "    -t <aspect> <config> : test loading of a config file\n"+
            "    -h (help)   : display the help\n");
        System.exit( 0 );
    }

    /**
     * This method is internally used to clean the cache directory of
     * the translated classes (to use the cache, use the -w option and
     * clean it with the -c option).
     *
     * @param dir the directory to clean 
     */
    protected static void cleanDirectory(File dir) {
        if (debug) 
            System.out.println("Deleting dir " + dir.getPath() + "...");

        File[] in_d = dir.listFiles();

        for (int i=0; i<in_d.length; i++) {
            if (in_d[i].isDirectory()) {
                cleanDirectory(in_d[i]);
            } else {
                if (debug) System.out.println("Deleting file " + dir.getPath() + "...");
                in_d[i].delete();
            }
        }
        dir.delete();
    }

    /* like cleanDirectory(), but only remove files
       if the original class file is newer */

    /*
      protected static void autoCleanDirectory(String dirName) {


      File dirFile = new File(getClassesTmp()+dirName);
      if (!dirFile.exists())
      return;
      if (debug) System.out.println("Auto cleaning dir " + dirFile.getPath() + "...");
      File[] in_d = dirFile.listFiles();

      if (!dirName.equals(""))
      dirName += "/";
      for (int i=0; i<in_d.length; i++) {
      if (in_d[i].isDirectory()) {
      autoCleanDirectory(dirName+in_d[i].getName());
      } else {
      File orig = new File(getJacRoot()+"classes/"+dirName+in_d[i].getName());
      if (debug) System.out.println(in_d[i]+" : "+orig.lastModified()+" / "+in_d[i].lastModified());
      if (orig.lastModified()>in_d[i].lastModified()) {
      if (debug) System.out.println("Deleting file " + in_d[i] + "...");
      in_d[i].delete();
      }
      }
      }
      dirFile.delete();
      }
    */

    /**
     * Returns the JAC root directory.<p>
     *
     * The root directory is usually defined by an environment variable
     * that is set to <code>$HOME/jac</code> where $HOME is the home
     * directory of the current user.<p>
     *
     * It can also be defined at launching time with the
     * <code>-R</code> option.<p>
     *
     * @return the JAC root directory value
     * @see #setJacRoot(String) */

    public static String getJacRoot() {
        if (jac_root==null) {
            System.err.println("No JAC_ROOT. Cannot continue.");
            System.exit(-1);
        }
        return jac_root;
    }

    /**
     * Sets the JAC root directory.<p>
     *
     * @param dir the path of the JAC root directory
     * @see #getJacRoot()
     */

    public static void setJacRoot(String dir) {
        String fileSep = System.getProperty("file.separator");
        if (!dir.endsWith(fileSep))
            dir += fileSep;
        jac_root = dir;
    }

    /**
     * Gets the name of the class that has been launched when JAC was
     * started (in an autonomous or a distributed mode).<p>
     *
     * The launched class is the class that contains the static
     * <code>run</code> method.<p>
     *
     * @return the launched class name
     * @see #setLaunchedClass(String) */

    public static String getLaunchedClass() {
        return launchedClass;
    }

    /**
     * Sets the launched class.<p>
     *
     * @param className the launched class name
     * @see #getLaunchedClass() */

    public static void setLaunchedClass(String className) {
        launchedClass = className;
    }

    /**
     * Returns the logical name of the program that was launched when
     * the JAC system started.<p>
     *
     * The logical program name is by convention the package name of
     * the class that runs the launched program.<p>
     *
     * For instance, if the command line is:<p>
     *
     * <ul><pre>jac -c -D org.objectweb.jac.samples.agenda.Run</pre></ul><p>
     *
     * Then the launched program is <code>org.objectweb.jac.samples.agenda</code><p>
     *
     * @return the launched program name
     * @see #getLaunchedClass() */

    public static String getLaunchedProgram() {
        String lc = getLaunchedClass();
        if (lc == null || lc.equals("")) {
            return "";
        }
        String lp = lc;
        int ndx = 0;
        if ((ndx = lp.indexOf(' ')) != -1) {
            lp = lc.substring(0, ndx);
        }
        if ((ndx = lp.lastIndexOf('.')) != -1) {
            lp = lc.substring(0, ndx);
        }

        return lp;
    }

    /**
     * Tells if the JAC system must try to launch the GUI of the
     * program is exist (see the <code>-G</code> option).<p>
     *
     * If the program does not provides a GUI aspect, then the default
     * GUI is launched.<p>
     *
     * @return true if the GUI must be launched
     * @see #setStartSwingGUI(String[]) */

    public static boolean startSwingGUI() {
        return startSwingGUI!=null && startSwingGUI.length>0;
    }

    /**
     * Tells if the JAC system must try to launch the Web GUI of the
     * program is exist (see the <code>-G</code> option).<p>
     *
     * If the program does not provides a Web GUI aspect, then the default
     * GUI is launched.<p>
     *
     * @return true if the Web GUI must be launched
     * @see #setStartWebGUI(String[]) */

    public static boolean startWebGUI() {
        return startWebGUI!=null && startWebGUI.length>0;
    }

    /**
     * Tells the JAC system to start the GUI.<p>
     *
     * @see #startSwingGUI() */

    public static void setStartSwingGUI(String[] start) {
        startSwingGUI = start;
    }

    /**
     * Returns the array of of customized GUI ids to be shown with the
     * Swing interface
     */
    public static String[] getStartSwingGUI() {
        return startSwingGUI;
    }

    /**
     * Tells the JAC system to start the GUI.<p>
     *
     * @see #startWebGUI() */

    public static void setStartWebGUI(String[] customizedGUIs) {
        startWebGUI = customizedGUIs;
    }

    /**
     * Returns the array of of customized GUI ids to be made available
     * on the web gui.
     */
    public static String[] getStartWebGUI() {
        return startWebGUI;
    }

}
