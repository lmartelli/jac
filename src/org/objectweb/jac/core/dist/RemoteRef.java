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

  You should have received a copy of the GNU Lesser General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA */

package org.objectweb.jac.core.dist;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.Collaboration;
import org.objectweb.jac.core.JacPropLoader;
import org.objectweb.jac.core.ObjectRepository;
import org.objectweb.jac.core.SerializedJacObject;
import org.objectweb.jac.util.WrappedThrowableException;

/**
 * <code>RemoteRef</code> stores the reference of a remote object.
 * The way the remote object is accessed depends on
 * the underlying communication protocol (eg CORBA or RMI).<p>
 *
 * Supporting a new communication protocol requires to subclass RemoteRef
 * (eg RMIRemoteRef or CORBARemoteRef) and to implement the resolve and
 * reresolve methods.<p>
 *
 * @see org.objectweb.jac.core.dist.RemoteRef#resolve(java.lang.String)
 * @see org.objectweb.jac.core.dist.RemoteRef#reresolve()
 * @see org.objectweb.jac.core.dist.rmi.RMIRemoteRef
 *
 * @author <a href="http://www-src.lip6.fr/homepages/Lionel.Seinturier/index-eng.html">Lionel Seinturier</a>
 * @author <a href="http://cedric.cnam.fr/~pawlak/index-english.html">Renaud Pawlak</a>
 */ 
public class RemoteRef implements Serializable {
    static Logger logger = Logger.getLogger("dist");
    static Logger loggerSerial = Logger.getLogger("serialization");

    /** The reference of the container that handles the remote object. */
    protected RemoteContainer remCont;
   
    /** The index (see org.objectweb.jac.core.JacObject) of the remote object. */
    protected int remIndex;

    /** The name of the remote object that is of will be associated to
       the remote ref. */
    protected String name = null;

    /** Property key for the remote reference class. */
	//protected static final String remRefClassProp = "Jac.remoteRefClass";
   
    /** Default remote reference class. */
	//protected static final String remRefDefaultClassName = "org.objectweb.jac.core.dist.rmi.RMIRemoteRef";

    /**
     * This class method returns a new RemoteRef object.<p>
     *
     * Property org.objectweb.jac.dist.remoteRefClass defines the class of the actual
     * RemoteRef returned (e.g. CORBARemoteRef). If the property is not
     * defined or if the class does not exist, RMIRemoteRef is the
     * default.<p>
     *
     * @param name the name to give to the remote ref (should be equal
     * to the pointed object name)
     * @return a new RemoteRef object
     */
    public static RemoteRef create(String name) {
      
        RemoteRef remoteRef = null;
        String remoteRefClassName = JacPropLoader.remoteRefClassName;

        try {
            Class remoteRefClass = Class.forName(remoteRefClassName);
            remoteRef = (RemoteRef) remoteRefClass.newInstance();
        } catch (Exception e) { 
            logger.error("create "+name,e); 
        }
	  
        remoteRef.setName(name);

        return remoteRef;
    }

    /**
     * This class method returns a remote reference
     * for an existing remote JAC object.<p>
     *
     * Property org.objectweb.jac.dist.remoteRefClass defines the class of the actual
     * RemoteRef returned (e.g. CORBARemoteRef). If the property is not
     * defined or if the class does not exist, RMIRemoteRef is the
     * default.<p>
     *
     * @param name the remote object name (can be null if not needed)
     * @param remCont the remote container where the JAC object is
     * instantiated
     * @param remIndex the index of the JAC object in the remote
     * container
     * @return the remote reference of the JAC object
     */
    public static RemoteRef create(String name,
                                   RemoteContainer remCont,
                                   int remIndex) 
    {
        logger.debug("creating remote ref " + name + ", " + 
                     remCont + ", " + remIndex);

        RemoteRef remoteRef = null;
        String remoteRefClassName = JacPropLoader.remoteRefClassName;
        /*
          String remoteRefClassName = null;

          if (JacObject.props != null) {
          remoteRefClassName = JacObject.props.getProperty(remRefClassProp);
          }
      
          if ( remoteRefClassName == null ) {
          remoteRefClassName = "org.objectweb.jac.core.dist.rmi.RMIRemoteRef";
          }
        */
        try {
            Class remoteRefClass = Class.forName(remoteRefClassName);
         
            Constructor c =
                remoteRefClass.getConstructor( 
                    new Class[] { RemoteContainer.class, int.class }
                );
            remoteRef =
                (RemoteRef)c.newInstance(
                    new Object[] { remCont, new Integer(remIndex) });
        } catch(Exception e) { 
            logger.error("create "+name+","+remCont+","+remIndex,e);
        }
      
        remoteRef.setName(name);
        logger.debug("returning remote ref " + remoteRef);
      
        return remoteRef;
    }

    /**
    * Create a remote reference from a local JAC object (in order, for
    * example, to transmit it to a remote container).<p>
    * 
    * @param name the name to be given to the remote reference
    * @param localObject the object to create the reference from
    * @return the new remote reference */

    public static RemoteRef create(String name, Object localObject) {
        logger.debug("creating remote ref "+name+" for "+localObject);
        return RemoteRef.create(
            name, RemoteContainer.resolve(Distd.getLocalContainerName()),
            ObjectRepository.getMemoryObjectIndex(localObject));
    }   
   
    /**
    * This is a full constructor for RemoteRef.<p>
    *
    * @param remCont the ref of the container that handles the remote
    * object.
    * @param remIndex the index of the remote object */
    
    public RemoteRef(RemoteContainer remCont, int remIndex) {
        this.remCont = remCont;
        this.remIndex = remIndex;
    }
   
   
    /**
    * This is a more friendly constructor for RemoteRef.<p>
    *
    * @param remCont the name of the container that handles the remote
    * object.
    * @param remIndex the index of the remote object.  */
   
    public RemoteRef(String remCont, int remIndex) {   
        this.remCont = resolve(remCont);
        this.remIndex = remIndex;
    }


    /**
    * Empty default constructor for RemoteRef needed by the compiler whenever
    * RemoteRef is subclasses (eg RMIRemoteRef or CORBARemoteRef).<p>
    *
    * This constructor should never be called in other cases (this is why it is
    * protected).<p>
    */
   
    protected RemoteRef() {}
   
   
    /**
    * The getter method for the remCont field.<p>
    *
    * It returns a the container that handles the remote object. If
    * the remote container is local, then the object pointed by the
    * remote reference is also local.<p>
    *
    * @return the remCont field value */
    
    public RemoteContainer getRemCont() { return remCont; }


    /**
    * The setter method for the name.<p>
    *
    * @param name the new name */

    public void setName(String name) {
        this.name = name;
    }
   
    /**
    * The getter method for the name.
    * 
    * @return the reference name */
   
    public String getName() {
        return name;
    }

    /**
    * The getter method for the <code>remIndex</code> field.<p>
    *
    * <code>remIndex</code> is the index (see org.objectweb.jac.core.JacObject) of
    * the remote object.
    *
    * @return the remIndex field value */
    
    public int getRemIndex() { return remIndex; }
   
    /**
    * This method resolves a container from a container name.<p>
    *
    * Its implementation is protocol dependent (eg RMI or CORBA).
    * Most concrete implementations of this method (see RMIRemoteRef)
    * simply delegate the resolution to a resolve() class method defined
    * in a container class (see RMIRemoteContainer).<p>
    *
    * @param contName the name of the container
    * @return the container
    *
    * @see org.objectweb.jac.core.dist.rmi.RMIRemoteRef#resolve(String)
    */
    public RemoteContainer resolve(String contName) { return null; }

    /**
    * This method re-gets the reference of a remote container.<p>
    *
    * Its implementation is protocol dependent (eg RMI or CORBA).
    * Some communication protocols (eg CORBA) do not linearalize
    * remote references in a standard way. Thus a remote reference
    * may need to be adapted whenever it is transmitted.<p>
    *
    * This method is called when a remote reference
    * is recieved by a <code>RemoteContainer</code>.<p>
    *
    * @return the container reference
    *
    * @see org.objectweb.jac.core.dist.rmi.RMIRemoteRef#reresolve()
    */   
    public RemoteContainer reresolve() { return null; }
   

    /** Following constants are property keys used by remoteNew(). */
   
    final protected static String toAdaptProp = "org.objectweb.jac.toAdapt";


    /**
    * Remotely instantiate a class.<p>
    *
    * Make the current <code>RemoteRef</code> instance reference the
    * created object.<p>
    *
    * @param host the host machine
    * @param clName the class to instantiate */
   
    public void remoteNew(String host, String clName) {
        remoteNewWithCopy(host, clName, null, null, null);
    }
   
    /**
    * Remotely instantiate a class.<p>
    *
    * Make the current <code>RemoteRef</code> instance reference the
    * created object.<p>
    *
    * @param host the host machine
    * @param clName the class to instantiate
    * @param args initialization arguments for the instantiation */
   
    public void remoteNew(String host, String clName, Object[] args) {
        remoteNewWithCopy(host, clName, args, null, null);
    }
   
    /**
    * Remotely instantiate a class.<p>
    *
    * Make the current <code>RemoteRef</code> instance reference the
    * created object and copy the state of the given object into the
    * remote object.<p>
    *
    * All the fields of the object are copied.<p>
    *
    * @param host the host machine
    * @param clName the class to instantiate
    * @param src the source object containing the data to copy */
   
    public void remoteNewWithCopy(String host, String clName, Object src) {
        remoteNewWithCopy(host, clName, null, src, null);
    }
   
    /**
    * Remotely instantiate a class.<p>
    *
    * Make the current <code>RemoteRef</code> instance reference the
    * created object and copy the state of the given object into the
    * remote object.<p>
    * 
    * All the fields of the object are copied.<p>
    *
    * @param host the host machine
    * @param clName the class to instantiate
    * @param args initialization arguments for the instantiation
    * @param src the source object containing the data to copy */
   
    public void remoteNewWithCopy(String host,
                                  String clName,
                                  Object[] args,
                                  Object src) {
        remoteNewWithCopy(host, clName, args, src, null);
    }


    /**
    * Remotely instantiate a class.<p>
    *
    * Make the current <code>RemoteRef</code> instance reference the
    * created object and copy the state of the given object into the
    * remote object.<p>
    *
    * Only specified fields are copied.<p>
    *
    * @param host the host machine
    * @param clName the class to instantiate
    * @param src the source object containing the data to copy
    * @param fieldsName the fields name to copy */
   
    public void remoteNewWithCopy(String host,
                                  String clName,
                                  Object src,
                                  String[] fieldsName) {
        remoteNewWithCopy(host, clName, null, src, fieldsName);
    }
   
   
    /**
    * Remotely instantiate a class.
    *
    * Make the current <code>RemoteRef</code> instance reference the
    * created object and copy the state of the given object into the
    * remote object.<p>
    *
    * Only specified fields are copied.<p>
    *
    * @param host the host machine
    * @param clName the class to instantiate
    * @param args initialization arguments for the instantiation
    * @param src the source object containing the data to copy
    * @param fieldsName the fields name to copy */
   
    public void remoteNewWithCopy(String host, 
                                  String clName, 
                                  Object[] args,
                                  Object src,
                                  String[] fieldsName) 
    {
        /**
         * Resolving the host consists in getting the concrete remote reference
         * (e.g. RMIRemoteRef or CORBARemoteRef) associated to the remote
         * container where the remote instantiation is to be performed.
         */
      
        remCont = resolve(host);

        /** Prepare the fields name and value */
      
        Object[] fieldsValue = null;
      
        if (src!=null) {
            if ( fieldsName == null ) {
                Object[] state = ObjectState.getState(src);
                fieldsName = (String[])state[0];
                fieldsValue = (Object[])state[1];
            }
            else {
                Object[] state = ObjectState.getState(src,fieldsName );
                fieldsName = (String[])state[0];
                fieldsValue = (Object[])state[1];
            }
        }

        if (fieldsName!=null)
            loggerSerial.debug(
                "serializing fields "+Arrays.asList(fieldsName)+
                " values = "+Arrays.asList(fieldsValue));

        byte[] sfieldsValue = SerializedJacObject.serialize(fieldsValue);
        if (sfieldsValue!=null) 
            Distd.outputCount += sfieldsValue.length;      

      /** Remotely create an instance of className */

        remIndex =
            remCont.instantiates(
                name, clName, args, fieldsName,
                sfieldsValue, 
                SerializedJacObject.serialize(Collaboration.get())
            );

    }

    /**
     * Copy the state of a given object into the remote object
     * referenced by the current reference.<p>
     *
     * All the fields of the object are copied.<p>
     *
     * @param src the source object containing the data to copy 
     */
    public void remoteCopy(Object src) {

        Object[] state = ObjectState.getState(src);
        byte[] sstate = SerializedJacObject.serialize( (Object[]) state[1] );

        if ( sstate != null ) Distd.outputCount += sstate.length;

        /** Perform the remote copy */
        remCont.copy(
            name, remIndex,
            (String[]) state[0],
            sstate,
            SerializedJacObject.serialize(Collaboration.get())
        );
    }
   
   
    /**
     * Copy the state of a given object into the remote object
     * referenced by the current reference.<p>
     *
     * Only specified fields are copied.<p>
     *
     * @param src the source object containing the data to copy
     * @param fieldsName the fields name to copy
     */
    public void remoteCopy(Object src, String[] fieldsName) {

        Object[] state = ObjectState.getState(src,fieldsName );
        byte[] sstate = SerializedJacObject.serialize( (Object[]) state[1] );

        if ( sstate != null ) Distd.outputCount += sstate.length;

        /** Perform the remote copy */

        remCont.copy(
            name,
            remIndex,
            (String[]) state[0],
            sstate,
            SerializedJacObject.serialize(Collaboration.get())
        );
    }
   
    /**
     * Forward a call to the referenced object.<p>
     *
     * @param methodName the called method name
     * @param methodArgs the called method arguments
     * @return the result
     */
    public Object invoke(String methodName, Object[] methodArgs) {
        return invoke(methodName,methodArgs,null);
    }
   
    /**
     * Forward a call to the referenced object.<p>
     *
     * @param methodName the called method name
     * @param methodArgs the called method arguments
     * @return the result
     */
    public Object invoke(String methodName, Object[] methodArgs, 
                         Boolean[] refs) 
    {
        logger.debug("invoking "+methodName+" on "+this);

        byte[] ret = null;
        byte[] args = SerializedJacObject.serializeArgs(methodArgs,refs);

        if (args != null) 
            Distd.outputCount += args.length;

        //      System.out.println("Collab = "+Collaboration.get());
        try {
            ret = remCont.invoke(
                remIndex,
                methodName,
                args,
                SerializedJacObject.serialize(Collaboration.get())
            );
        } catch (Exception e) {
            if (e instanceof WrappedThrowableException) {
                throw (RuntimeException) e;
            } 
            logger.error("Failed to remotely invoke "+methodName+": "+e);
        }

        if ( ret != null ) Distd.inputCount += ret.length;

        return SerializedJacObject.deserialize( ret );
    }

    /**
     * Forward a role method call to the referenced object.<p>
     *
     * @param methodName the called role method name
     * @param methodArgs the called role method arguments
     * @return the result 
     */
    public Object invokeRoleMethod(String methodName,Object[] methodArgs) {

        logger.debug("invoking role method "+methodName+" on "+
                     this+"-"+remCont);

        byte[] ret = null;
        byte[] args = SerializedJacObject.serialize(methodArgs);

        if (args != null) Distd.outputCount += args.length;
      
        try {
            ret = remCont.invokeRoleMethod(
                remIndex,
                methodName,
                args,
                SerializedJacObject.serialize(Collaboration.get())
            );
        } catch ( Exception e ) {
            if ( e instanceof WrappedThrowableException ) {
                throw (RuntimeException) e;
            } 
            logger.error("Failed to remotely invoke "+methodName+": "+e);
        }

        if ( ret != null ) Distd.inputCount += ret.length;

        return SerializedJacObject.deserialize(ret);
    }

    /**
     * Create a textual representation of the remote reference.
     *
     * @return the textual representation of the current reference 
     */
    public String toString() {
        return ( "#" + getRemCont().getName() + "/" + 
                 name + "[" + getRemIndex() + "]#" );
    }

    /**
     * Test the equality of 2 remote references.
     *
     * @param o the remote reference to check
     * @return true if equals the current one 
     */
    public boolean equals(Object o) {
        if ( ! (o instanceof RemoteRef) ) return false;
        RemoteRef r = (RemoteRef) o;
        return ( r.getRemIndex() == remIndex )
            && ( r.getRemCont().equals (remCont) );
    }
   
}
