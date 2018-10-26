/*
  Copyright (C) 2001 Renaud Pawlak <renaud@aopsys.com>

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

import java.io.FileInputStream;
import java.util.Hashtable;
import org.apache.log4j.Logger;

/**
 * DistdClassLoader is a class loader that load classes from a remote
 * JAC container (the class repository site).
 *
 * @see RemoteContainer#getByteCodeFor(String)
 *
 * @author <a href="http://cedric.cnam.fr/~pawlak/index-english.html">Renaud Pawlak</a> */

public class DistdClassLoader extends ClassLoader {
    static Logger logger = Logger.getLogger("dist.classloader");

   /** loadedClasses is a hashtable of classes that have been
       loaded. */
    protected transient Hashtable loadedClasses = new Hashtable();

    /** loadedByteCodes is a hashtable of byte codes that have been
       remotely load (so that this site may be used as an intermediate
       class repository. */
    protected Hashtable loadedByteCodes = new Hashtable();

    /** Bootstrapping flag (do not load classes while true). */
    public boolean bootstrapping = true;

    /** The class repository site. */
    public static String classRepositoryName = null;
   
    /**
    * Overrides the default mechanism to load classes (only for
    * non-java classes). The behavior is the following :<p>
    *
    * <ul>
    *   <li>check whether the class is already loaded</li>
    *   <li>try to load it from the class repository site</li>
    *   <li>if success, store its bytecode (see getByteCode)</li>
    *   <li>try to load it from the local filesystem</li>
    *   <li>if all these failed, delegate to the parent</li>
    * </ul>
    * 
    * @param name the name for the class to load
    * @return the loaded class
    */

    public Class loadClass(String name)
        throws ClassNotFoundException {

        /** Do not reload the same class */
        if (name.equals(getClass().getName())) {
            logger.debug("Do not reload "+getClass().getName());
            return getClass();
        }

        Class cl;
        if (name.startsWith("java.")) {
            logger.debug("Get system class "+name+" from parent classloader");
            cl = getParent().loadClass(name);
            loadedClasses.put(name, cl);
            return cl;
        }
      
        /** Check whether already loaded */
        cl = (Class)loadedClasses.get(name);

        if (cl == null) {

            byte[] bc = null;

            /** Check if we know a class repository to download the bytecode */

            if ( (!bootstrapping) && classRepositoryName != null ) {

                /** Download the bytecode */
                logger.debug("Downloading "+name+" from "+classRepositoryName);
                RemoteContainer rc = RemoteContainer.resolve(classRepositoryName);
                try {
                    bc = rc.getByteCodeFor(name);
                } catch(Exception e) {
                    logger.debug("Failed to get bytecode for "+name+
                              " from "+classRepositoryName+": "+e);
                }
                if (bc != null) {
                    loadedByteCodes.put(name, bc);
                }
            }
         
            /** Check if download was successfully performed */

            if (bc == null) {

                /** Try to load it from the local filesystem */
                logger.debug("Loading "+name+" from local FS");
                try {
                    FileInputStream in = new FileInputStream( 
                        getParent().getResource(name.replace('.','/')+".class").getFile());
                    bc = new byte[in.available()];
                    in.read(bc);
                } catch (Exception e) {
                    /** If failed, delegate to the parent classloader */
                    cl = getParent().loadClass(name);
                    loadedClasses.put(name, cl);
                    return cl;
                }
            
            }

            if (bc != null) {
                /** Define the class from the bytecode */
                try {
                    cl = defineClass(name, bc, 0, bc.length);
                    loadedClasses.put(name, cl);
                } catch (Exception e) {
                    /** Bytecode was corrupted or some error occured */
                    logger.error("Cannot load class "+name,e);
                }
            }
      
        } else {
            logger.debug("Already loaded "+name);
        }

        return cl;
    }

    /**
    * Gets the bytecode for a given remotely loaded class name.
    *
    * @param className the name of the class
    * @return the corresponding bytecode */

    public byte[] getByteCode (String className) {
        byte[] bc = (byte[])loadedByteCodes.get (className);
        if (bc == null) {
        }
        return bc;
    }
      
}
