/*
  Copyright (C) 2001-2003 Lionel Seinturier, Renaud Pawlak.

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

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.ACManager;
import org.objectweb.jac.core.Collaboration;
import org.objectweb.jac.core.JacLoader;
import org.objectweb.jac.core.JacPropLoader;
import org.objectweb.jac.core.NameRepository;
import org.objectweb.jac.core.ObjectRepository;
import org.objectweb.jac.core.SerializedJacObject;
import org.objectweb.jac.core.Wrappee;
import org.objectweb.jac.core.Wrapping;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.util.ExtArrays;
import org.objectweb.jac.util.WrappedThrowableException;

/**
 * RemoteContainer is used as a delegate by daemons (Distd) that hold
 * remote objects.<p>
 *
 * A remote container enables JAC objects to access and manipulate
 * objects located on other Java VMs.
 *
 * @see org.objectweb.jac.core.dist.Distd
 *
 * @author <a href="http://www-src.lip6.fr/homepages/Lionel.Seinturier/index-eng.html">Lionel Seinturier</a>
 * @author <a href="http://cedric.cnam.fr/~pawlak/index-english.html">Renaud Pawlak</a> */
 
public class RemoteContainer implements Serializable {
    static Logger logger = Logger.getLogger("dist");
    static Logger loggerSerial = Logger.getLogger("serialization");

    /** Verbose tells whether information message should be printed or
        not. */
    public static boolean verbose = false;

    /** The name of the container. */
    public String name = "theContainerWithNoName";

    /** Property key for the class providing a naming service. */
	//protected static final String namingClassProp = "org.objectweb.jac.core.dist.namingClass";

    /** Default class providing a naming service. */
	//protected static final String namingClassDefaultName = "org.objectweb.jac.core.dist.rmi.RMINaming";

    /**
     * This method dynamically binds the local container to a new
     * remote container.
     *
     * @param name the remote container to bind to */

    public static RemoteContainer bindNewContainer(String name) {

        logger.debug("binding to a new container "+name);
        RemoteContainer result = null;
        // check is the container already bounded
        result=Topology.get().getContainer(name);
        if (result != null) {
            logger.debug("container already bound");
            return result;
        } else {
            result = RemoteContainer.resolve(name);
            if (result == null) {
                throw new RuntimeException("container "+name+" does not exist");
            }
        }

        // get the remote topology      
        RemoteRef remoteTopology = result.bindTo("JAC_topology");

        // add the local known containers to the remote topology 
        RemoteContainer[] rcs = Topology.get().getContainers();
        remoteTopology.invoke("addContainers", new Object[] {rcs});

        // add all the remote containers to the local topology
        rcs = (RemoteContainer[])remoteTopology.invoke("getContainers",ExtArrays.emptyObjectArray);
        Topology.get().addContainers(rcs);
        return result;
    }

    /**
     * Create a new container.
     *
     * @param verbose  true if information messages are to be printed
     */
    public RemoteContainer(boolean verbose) { 
        RemoteContainer.verbose = verbose; 
    }

    /**
     * Create a new container and instantiates an object.
     *
     * @param className the name of the class to instantiate
     * @param verbose  true if information messages are to be printed
     */
    public RemoteContainer(String className, boolean verbose) {
        try {
            Class.forName(className).newInstance();
        } catch (Exception e) {
            logger.error("RemoteContainer "+className,e);
        }
        RemoteContainer.verbose = verbose;
    }
   
    /**
     * Create a new empty container.
     * This constructor is needed by the compiler when one tries
     * to subclass RemoteContainer.
     *
     * @see org.objectweb.jac.core.dist.rmi.RMIRemoteContainerStub
     */   
    public RemoteContainer() {}

    /**
     * Getter method for field name.<p>
     *
     * @return  the container name
     */
    public String getName() { 
        return name; 
    }
   

    /**
     * Setter method for field name.<p>
     *
     * Note that this method does not bind the container. It simply
     * sets the value of field name. Use resolve(name) to bind a
     * container to a name.<p>
     *
     * The name can be an incomplete name since it is completed by the
     * <code>getFullHostName</code> method.
     *
     * @param name the name of the container
     * @see #resolve(String)
     * @see Distd#getFullHostName(String) */

    public void setName(String name) {
        this.name = Distd.getFullHostName(name);
    }

    /**
     * Return true if the container is local.<p>
     *
     * @param name the container name to resolve
     * @return true if the container is local.
     * @see #resolve(String)
     * @see #isLocal() 
     */
    public static boolean isLocal(String name) {
        RemoteContainer rc = RemoteContainer.resolve(name);
        if (rc==null) 
            return false;
        return rc.isLocal();
    }
   
    /**
     * This class method resolve a container from its name.
     * The way the resolution is performed is protocol dependent.<p>
     *
     * Property org.objectweb.jac.dist.remoteContainerClass defines the class of
     * the actual RemoteContainer returned (e.g. CORBARemoteContainer).
     * If the property is not defined or
     * if the class does not exist, RMIRemoteContainer is the default.
     *
     * @param name  the name of the container
     * @return      the container reference    
     */   
    public static RemoteContainer resolve(String name) 
    {
        logger.debug("resolving remote container "+name);

        RemoteContainer remoteContainer = null;
      
        /** Lookup the actual remote container class name from the properties. */

        String namingClassName = JacPropLoader.namingClassName;
        /*
          String namingClassName =  null;
          if (JacObject.props != null) {
          namingClassName = JacObject.props.getProperty(namingClassProp);
          }

        */
        if (namingClassName == null) {
            namingClassName = JacPropLoader.namingClassDefaultName;
        }
	  
        /** Invoke the class method resolve on the above mentioned class. */
      
        try {
            Class namingClass = Class.forName(namingClassName);

            Method meth =
                namingClass.getMethod( "resolve", new Class[]{String.class} );

            remoteContainer =
                (RemoteContainer) meth.invoke(null, new Object[]{name});
      
        } catch (Exception e) {
            logger.error("resolve "+name,e);
            throw new RuntimeException(e.toString());
        }

        logger.debug("resolve is returning " + remoteContainer);
      
        return remoteContainer;
    }


    /**
     * Return true if the container is local.<p>
     *
     * RemoteContainer objects are always locals. RemoteContainer
     * stubs objects (see RMIRemoteContainerStub) may not always be
     * local.
     *
     * @return true if the container is local.
     * @see Distd#containsContainer(RemoteContainer) */

    public boolean isLocal() {
        return Distd.containsContainer( this );
    }

    /**
     * This method instantiates a className object.<p>
     *
     * Clients call it to remotely instantiate an object.
     * <code>instantiates</code> creates an object and returns its
     * local JAC index.<p>
     *
     * The object's name can be null. If not, the object is
     * registered into the local repository.<p>
     *
     * The Aspect Component Manager is upcalled to notify the system
     * from a remote instantiation.<p>
     *
     * @param name       the name of the object
     * @param className  the class name to instantiate
     * @param args       initialization arguments for the instantiation
     * @param fields     the object fields that are part of the state
     * @param state      the state to copy
     * @return the index of the instantiated object
     *
     * @see org.objectweb.jac.core.ACManager#whenRemoteInstantiation(Wrappee,String) 
     */
    public int instantiates(String name, String className, Object[] args,
                            String[] fields, byte[] state, byte[] collaboration) 
    {
        /** Set the local interaction */
        Collaboration.set(
            (Collaboration)SerializedJacObject.deserialize(collaboration));

        logger.debug("remote instantiation of "+className);

        Object substance = null;
      
        try {

            /** Instantiate the base object */
         
            Class substanceClass = null;
         
            try {
                /** Begin critical section */
                // WAS THIS REALLY USEFUL ????
                //JacObject.remoteInstantiation = true;            
            
                substanceClass = Class.forName(className);

                if (args == null) {
                    Constructor c = substanceClass.getConstructor ( 
                        new Class[] {});
                    substance = c.newInstance(new Object[] {});
                } else {
                    throw new InstantiationException(
                        "No arguments allowed when remote instantiation");
                }

                /** End critical section */
                // USEFULL ????
                ///JacObject.remoteInstantiation = false;
            
            } catch(Exception e) {
                // USEFUL????
                //JacObject.remoteInstantiation = false;
                logger.error("Instantiates "+name+" "+className+Arrays.asList(args),e);
                return -1;
            }         

            /** Copy the transmitted state if needed */
            
            if (fields != null && state != null) {
                Object[] dstate = (Object[])
                    SerializedJacObject.deserialize(state);
            
                ObjectState.setState(
                    substance,new Object[] { fields, dstate }
                );
                loggerSerial.debug("deserialized fields="+
                          Arrays.asList(fields)+"; state="+Arrays.asList(dstate));
            } else {
                loggerSerial.debug("deserialize: transmitted state is empty");
            }
         
            if (verbose) {
                System.out.println(
                    "--- A " + className + " instance has been created (" + 
                    ObjectRepository.getMemoryObjectIndex(substance) + ") ---"
                );
            }
         
        } catch (Exception e) { 
            logger.error("Instantiates "+name+" "+className+Arrays.asList(args),e);
            return -1; 
        }

        /** Upcall the ACManager to notify it from a remote instatiation. */
        ((ACManager) ACManager.get()).whenRemoteInstantiation( (Wrappee) substance, name );
      
        return ObjectRepository.getMemoryObjectIndex(substance);      
    }

    /**
     * Copy a state into a base object.
     *
     * @param index   the base object index (see <code>org.objectweb.jac.core.JacObject</code>)
     * @param fields  the object fields that are part of the state
     * @param state   the state to copy
     */
    
    public void copy(String name, int index, String[] fields, byte[] state,
                     byte[] collaboration) {

        /** Set the local interaction */
        Collaboration.set((Collaboration)SerializedJacObject.deserialize(collaboration));
        ObjectState.setState(ObjectRepository.getMemoryObject(index), new Object[] {
            fields, (Object[]) SerializedJacObject.deserialize(state) } );

        /** upcall the acmanager ??? */
    }
   
   
    /**
     * Invoke a method on a base object.<p>
     *
     * The base object is the remote counterpart of a local object that
     * has been remotely instantiated by the <code>Distd</code> daemon.
     *
     * @param index       the callee index (see org.objectweb.jac.core.JacObject)
     * @param methodName  the called method name
     * @param args        the called method arguments
     * @param collaboration the collaboration coming from the calling host
     * @return the result as an array of bytes (to be deserialized) */
   
    public byte[] invoke(int index, String methodName, byte[] args,
                         byte[] collaboration) {

        if (args != null) 
            Distd.inputCount += args.length;

        Object[] methodArgs =
            (Object[])SerializedJacObject.deserializeArgs(args);
      
        Object ret = null;

        try {
            Object substance = ObjectRepository.getMemoryObject(index);
            Class substanceClass = substance.getClass();
         
            /** Set the local interaction */
            Collaboration.set((Collaboration)SerializedJacObject.
                              deserialize(collaboration));

            logger.debug("remote invocation of " + methodName + " on "+
                         substance + "("+
                         NameRepository.get().getName(substance)+")");
         
            ret = ClassRepository.invokeDirect(substanceClass, methodName,
                                               substance, methodArgs);
         
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof WrappedThrowableException) {
                throw (RuntimeException)e.getTargetException();
            } else {
                logger.error("invoke "+index+"."+methodName+
                             Arrays.asList(methodArgs),e);
            }
        } catch (Exception e) {
            logger.error("invoke "+index+"."+methodName+
                         Arrays.asList(methodArgs),e);
        }
        /*catch( IllegalAccessException e ) {
          e.printStackTrace();
          return null;
          }
          catch ( NoSuchMethodException e ) {
          System.out.println ( "Error: invoke '" + methodName + 
          "' with " + Arrays.asList(methodArgs) + "." );
          }*/

        byte[] sret =  SerializedJacObject.serialize(ret);

        if (sret != null) 
            Distd.outputCount += sret.length;
        logger.debug("remote invocation ok, returning "+ret);
        return sret;
    }

    public byte[] invokeRoleMethod(int index, String methodName, byte[] args,
                                   byte[] collaboration) {

        if (args != null) 
            Distd.inputCount += args.length;

        Object[] methodArgs =
            (Object[])
            SerializedJacObject.deserialize(args);
      
        Object ret = null;

        try {
         
            Object substance = ObjectRepository.getMemoryObject(index);
            Class substanceClass = substance.getClass();
         
            /** Set the local interaction */

            logger.debug(Collaboration.get().toString());

            Collaboration.set((Collaboration)SerializedJacObject
                              .deserialize(collaboration));
         
         
            logger.debug("remote invocation of role method " + methodName + " on "+
                      substance + "("+
                      NameRepository.get().getName(substance)+")"+
                      " - "+this);
            logger.debug(Collaboration.get().toString());
         
            ret=Wrapping.invokeRoleMethod((Wrappee)substance,methodName,methodArgs);
         
            logger.debug(Collaboration.get().toString());
        } catch(Exception e) {
            if (e instanceof WrappedThrowableException) {
                throw (RuntimeException)e;
            } else {
                logger.error("invokeRoleMethod "+index+"."+methodName+
                             Arrays.asList(methodArgs),e);
            }
        }

        byte[] sret = SerializedJacObject.serialize(ret);

        if (sret != null) 
            Distd.outputCount += sret.length;

        logger.debug("remote invocation ok, returning "+ret);

        return sret;
    }


    /**
     * Gets the bytecode for the given class by using the current
     * loader.<p>
     *
     * This method is used by distributed loaders to fetch classes
     * bytecodes from JAC remote containers that are classes
     * repositories.<p>
     *
     * @param className the class name
     * @return the corresponding bytecode
     */

    public byte[] getByteCodeFor(String className) {
        ClassLoader cl = getClass().getClassLoader();
        if (cl instanceof JacLoader) {
            logger.debug("getByteCodeFor "+className+" bootstraping");
            /** we are a bootstraping site */
            try {
                if (Wrappee.class.isAssignableFrom(Class.forName(className))) {
                    //( ((JacLoader)cl).getClassPath().readClassfile( className ) );
                    return null; 
                }
            } catch ( Exception e ) { 
                logger.error("Failed to fetch bytecode for "+className,e);
            }
        } else if (cl instanceof DistdClassLoader) {
            logger.debug("getByteCodeFor "+className+" distd");
            ((DistdClassLoader)cl).getByteCode(className);
        }
        logger.debug("getByteCodeFor "+className+" -> null");
        return null;
    }

    /**
     * Returns a remote reference on the object corresponding to the
     * given name.<p>
     *
     * Note: This method has been added for practical reason but
     * introduces a implicit dependency towards the naming aspect. If
     * the naming aspect is not woven, then this method does not mean
     * anything and the returned remote reference is null.
     *
     * @param name the name of the object to bind to
     * @return the corresponding remote reference */

    public RemoteRef bindTo(String name) {
        Class nrc = null;
        Method m = null;
        Object nr = null;
        Object o = null;

        logger.debug("client is binding to "+name);

        o = NameRepository.get().getObject(name);
        if (o == null) {
            logger.debug("object "+name+" not found on this container");
            return null;
        }
       
        RemoteContainer rc = RemoteContainer.resolve(this.name);
        int index = ObjectRepository.getMemoryObjectIndex(o);

        RemoteRef rr = RemoteRef.create(name, rc, index);
      
        logger.debug("returning remote reference "+rr);

        return rr;
    }

    /**
     * Check the equality of two containers (on the name).<p>
     *
     * @param container the container to check
     * @return true if the given container equals to the current container
     */

    public boolean equals(Object container) {
        if (!(container instanceof RemoteContainer)) 
            return false;
        RemoteContainer c = (RemoteContainer)container;
      
        return name.equals(c.getName());
    }
   
    /**
     * Return a textual representation of a container (its name).
     *
     * @return a textual representation of the container */

    public String toString() {
        return "Container " + getName();
    }

    /**
     * Launches a administration GUI on a remote container.
     */

    public void launchRemoteGUI() {
        RemoteContainer rc = RemoteContainer.resolve( name );
        logger.debug("launch remote GUI: container = " + rc );
        RemoteRef remoteTopology = rc.bindTo( "JAC_topology" );
        logger.debug("remote topology = " + remoteTopology );

        /*Object guiAC = ACManager.get().getObject("gui");
          Object display = null;
          String programName = null;
          try {
          display = guiAC.getClass().getMethod( "getDisplay", new Class[] { String.class } )
          .invoke( guiAC, new Object[] { "admin" } );
          programName = (String)display.getClass().getMethod("getProgram",ExtArrays.emptyClassArray)
          .invoke( display, ExtArrays.emptyObjectArray);
          } catch (Exception e) { e.printStackTrace(); return; }*/
        remoteTopology.invoke( "launchGUI", ExtArrays.emptyObjectArray );
    }
   
}
