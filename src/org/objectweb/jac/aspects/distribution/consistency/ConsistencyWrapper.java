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

  You should have received a copy of the GNU Lesser General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA */

package org.objectweb.jac.aspects.distribution.consistency;

import java.util.*;
import org.aopalliance.intercept.ConstructorInvocation;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.*;
import org.objectweb.jac.core.dist.*;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.util.Log;

/**
 * This wrapper class is the base class for all the consistency
 * wrappers that implement a consistency protocol.
 *
 * <p>By default it does nothing and provides no consistency at all
 * between the replicas. A consistency programmer should define the
 * wrapping and role method to implement a specific consistency
 * protocol.<p>
 *
 * @author <a href="http://cedric.cnam.fr/~pawlak/index-english.html">Renaud Pawlak</a>
 */

public class ConsistencyWrapper extends Wrapper {
    static Logger logger = Logger.getLogger("consistency");

	/** Storage for known replicas. */
	protected Vector knownReplicas = new Vector();

	/** The replicas type. */
	Class type = null;

	/** The read method names. */
	String[] readMethods = null;

	/** The write method names. */
	String[] writeMethods = null;

	/** The call method names. */
	String[] callMethods = null;

	String hosts = null;

	/** Visited sites global attribute name. */
	static protected String visitedReplicas = "visitedReplicas";

	static {
		Collaboration.setGlobal(visitedReplicas);
	}

	/** Use to indicate that you need all the methods. */
	public static String ALL_METHODS = "ALL";
	/** Use to indicate that you need all the modifiers. */
	public static String ALL_MODIFIERS = "ALL_MODIFIERS";
	/** Use to indicate that you need all the getters. */
	public static String ALL_GETTERS = "ALL_GETTERS";

	/**
	 * Default constructor. */

	public ConsistencyWrapper(AspectComponent ac) {
		super(ac);
		this.knownReplicas = null;
	}

	/**
	 * Contructor for initialization.
	 *
	 * @param type the type of the wrappee
	 * @param readMethods the methods that read the wrappee's state
	 * @param writeMethods the methods that write the wrappee's state
	 * @param callMethods the stateless methods */

	public ConsistencyWrapper(
		AspectComponent ac,
		Class type,
		String[] readMethods,
		String[] writeMethods,
		String[] callMethods,
		String hosts) {
		super(ac);
		this.type = type;
		setReadMethods(readMethods);
		setWriteMethods(writeMethods);
		setCallMethods(callMethods);
		this.hosts = hosts;
	}

	/**
	 * Wraps a wrappee with a consistency wrapper. */

	public static void wrap(
		Wrappee wrappee,
		Class wrapperClass,
		String[] readMethods,
		String[] writeMethods,
		String[] callMethods,
		String hosts) {
		ConsistencyWrapper wrapper = null;
		try {
			wrapper =
				(ConsistencyWrapper) wrapperClass
					.getConstructor(new Class[] { AspectComponent.class })
					.newInstance(new Object[] { null });
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		wrapper.type = wrappee.getClass();
		wrapper.setReadMethods(readMethods);
		wrapper.setWriteMethods(writeMethods);
		wrapper.setCallMethods(callMethods);
		wrapper.hosts = hosts;
		// TODO: consitency reengineering.
		//Wrapping.wrap(wrappee, wrapper, "whenRead", readMethods);
		//Wrapping.wrap(wrappee, wrapper, "whenWrite", writeMethods);
		//Wrapping.wrap(wrappee, wrapper, "whenCall", callMethods);
	}

	/**
	 * Add a replica to the knowledge graph.<p>
	 *
	 * The way the member is added
	 * to the knowledge graph depends on the knowledge graph.
	 *
	 * @param newReplica the reference on the replica to add
	 */

	public void addMember(RemoteRef newReplica) {

		String[][] wrapped_methods =
			new String[][] { readMethods, writeMethods, callMethods };
		String[] wrapping_methods =
			new String[] { "whenRead", "whenWrite", "whenCall" };
		Class wrapper_type = getClass();
		ConsistencyWrapper wrapper = null;

		try {

			wrapper = (ConsistencyWrapper) wrapper_type.newInstance();

			if (wrapper == null) {
				throw new InstantiationException();
			}

			wrapper.setReadMethods(readMethods);
			wrapper.setWriteMethods(writeMethods);
			wrapper.setCallMethods(callMethods);

		} catch (Exception e) {
			e.printStackTrace();
		}

		wrapper.addKnownReplica(newReplica);

		newReplica.invoke(
			"wrap",
			new Object[] { wrapper, wrapping_methods, wrapped_methods });

		whenBindingNewReplica(newReplica);

		/*      newReplica.invoke(
		   "invokeRoleMethod",
		   new Object[] {
		      "whenNewReplicaBounded",
		      new Object[] {
		         null,
		      }
		   }
		   );*/

	}

	/**
	 * Invalidates the topology.
	 *
	 * <p>When this method is called, the next consistency protocol run
	 * will recalculate the known replica with the new topology. */

	public void invalidateTopology() {
		knownReplicas = null;
	}

	/**
	 * Calculates the known replicas with the topology and the host
	 * expression. */

	void calculateKnownReplicas(Wrappee wrappee) {
		logger.debug("calculing known replicas for " + wrappee);
		knownReplicas = Topology.getPartialTopology(hosts).getReplicas(wrappee);
		logger.debug("result for known replicas="+ knownReplicas
				+ "(on topology " + Topology.getPartialTopology(hosts) + ")");
	}

	/**
	 * This method is called when new member is bounded to the
	 * replication group.
	 *
	 * <p>It is typically used to initialize (in a push manner) the new
	 * member state with the data that need to be replicated. By
	 * default, this method does nothing.
	 *
	 * @param newReplica the replica that is beeing added */

	public void whenBindingNewReplica(RemoteRef newReplica) {
	}

	/**
	 * This method is called on the new member when the binding is
	 * finished.
	 *
	 * <p>It is typically used to initialize (in a pull manner)
	 * the new member state with the data that need to be
	 * replicated. By default, this method does nothing.
	 *
	 * @param remoteReplica the replica that has just been bounded
	 */

	public void whenNewReplicaBounded(RemoteRef remoteReplica) {
	}

	/**
	 * Returns the visited replicas global attribute of the
	 * collaboration.
	 * 
	 * @return the name of the attribute
	 */

	public String getVisitedReplicas() {
		return visitedReplicas;
	}

	/**
	 * Returns a string representation of the wrapper.
	 *
	 * @return a string representation of the wrapper
	 */

	public String toString() {
		if (knownReplicas == null)
			return "Consistency wrapper, no known replicas";
		else
			return "Consistency wrapper, known replicas = " + knownReplicas;
	}

	/**
	 * Set the read method names (methods that read the replica
	 * state).
	 *
	 * @param readMethods the names of the read methods */

	public void setReadMethods(String[] readMethods) {
		this.readMethods = expandMethods(readMethods);
	}

	/** 
	 * Set the write method names (methods that change the replica
	 * state).
	 * 
	 * @param writeMethods the names of the write methods
	 */

	public void setWriteMethods(String[] writeMethods) {
		this.writeMethods = expandMethods(writeMethods);
	}

	/** 
	 * Set the call method names (methods that neither write or read
	 * the replica state).
	 * 
	 * @param callMethods the names of the call methods
	 */

	public void setCallMethods(String[] callMethods) {
		this.callMethods = expandMethods(callMethods);
	}

	/**
	 * The getter method for the known replicas.
	 *
	 * <p>The known replicas of a consistency wrapper are remote
	 * references on other member of the replication group. With this
	 * information, the consistency wrapper can implement consistency
	 * protocols. The most usual schemes are to be aware of all the
	 * replicas of the group (see for instance
	 * <code>StrongPushConsistencyWrapper</code>) or to be aware of ony
	 * one replica (see for instance
	 * <code>ClientServerConsistencyWrapper</code>.
	 *
	 * @return a set of references on the known replicas */

	public Vector getKnownReplicas() {
		return knownReplicas;
	}

	/**
	 * Adds a known replica.
	 * 
	 * @param newReplica the known replica to add
	 */
	public void addKnownReplica(RemoteRef newReplica) {
		knownReplicas.add(newReplica);
	}

	/**
	 * The get method for the consistency wrapper actual type.<p>
	 *
	 * @return the class of the wrapper */

	public Class getConsistencyWrapperType() {
		return getClass();
	}

	/**
	 * The setter method for the known replicas.<p>
	 *
	 * The known replicas of a consistency wrapper are remote
	 * references on other member of the replication group. With this
	 * information, the consistency wrapper can implement consistency
	 * protocols. The most usual schemes are to be aware of all the
	 * replicas of the group (see for instance
	 * <code>StrongPushConsistencyWrapper</code>) or to be aware of ony
	 * one replica (see for instance
	 * <code>ClientServerConsistencyWrapper</code>.<p>
	 *
	 * @param knownReplicas the new known replicas */

	public void setKnownReplicas(Vector knownReplicas) {
		this.knownReplicas = knownReplicas;
	}

	/**
	 * This wrapping method must be defined to implement a given
	 * builtin consistency protocol and must wrap the replica methods
	 * that need to be consistent with the other replicas.<p>
	 *
	 * It should not wrap a method that is allready wrapped by a
	 * <code>WhenWrite</code> or <code>WhenRead</code> method.<p>
	 *
	 * Default: do nothing and call the replica.<p>
	 *
	 * @return by default the wrapped method return value
	 * @see #acceptRemoteCall(RemoteRef,Object[]) */

	public Object whenCall(Interaction interaction) {
		return proceed(interaction);
	}

	/**
	 * This wrapping method must be defined to implement a given
	 * builtin consistency protocol and must wrap all the replicas
	 * methods that provoque a change in this replica state.<p>
	 *
	 * Default: do nothing and call the replica.<p>
	 *
	 * @return by default the wrapped method return value
	 * @see #acceptRemoteWrite(Wrappee,RemoteRef,Object[]) */

	public Object whenWrite(Interaction interaction) {
		return proceed(interaction);
	}

	/**
	 * This wrapping method must be defined to implement a given
	 * builtin consistency protocol and must wrap all the replica
	 * methods that read the replica state.<p>
	 *
	 * Default: do nothing and call the replica.<p>
	 *
	 * @return by default the wrapped method return value
	 * @see #acceptRemoteRead(Wrappee,RemoteRef,Object[]) */

	public Object whenRead(Interaction interaction) {
		return proceed(interaction);
	}

	/**
	 * This role method can called by the <code>whenCall</code>
	 * wrapping method of a remote replica.<p>
	 *
	 * By overloading this method, the programmer can implement
	 * specific consistency protocols.<p>
	 *
	 * Default: do nothing.<p>
	 *
	 * @param remoteReplica expected to be a reference on the remote
	 * replica that recieved the call event
	 * @param data the data transmittedd by <code>whenCall</code>
	 * @return null by default
	 * @see #whenCall(Interaction) */

	public Object acceptRemoteCall(RemoteRef remoteReplica, Object[] data) {
		return null;
	}

	/**
	 * This role method can called by the <code>whenWrite</code>
	 * wrapping method of a remote replica.<p>
	 *
	 * By overloading this method, the programmer can implement
	 * specific consistency protocols.<p>
	 *
	 * Default: do nothing.<p>
	 *
	 * @param remoteReplica expected to be a reference on the remote
	 * replica that recieved the write event
	 * @param data the data transmittedd by <code>whenWrite</code>
	 * @return null by default
	 * @see #whenWrite(Interaction) */

	public Object acceptRemoteWrite(
		Wrappee wrappee,
		RemoteRef remoteReplica,
		Object[] data) {
		return null;
	}

	/**
	 * This role method can called by the <code>whenRead</code>
	 * wrapping method of a remote replica.<p>
	 *
	 * By overloading this method, the programmer can implement
	 * specific consistency protocols.<p>
	 *
	 * Default: do nothing.<p>
	 *
	 * @param remoteReplica expected to be a reference on the remote
	 * replica that recieved the read event
	 * @param data the data transmittedd by <code>whenRead</code>
	 * @return null by default
	 * @see #whenRead(Interaction) */

	public Object acceptRemoteRead(
		Wrappee wrappee,
		RemoteRef remoteReplica,
		Object[] data) {
		return null;
	}

	/**
	 * Construct a real methods array with an array that can contain
	 * consistency specific strings (like the one that indicates that
	 * we need all the modifiers).
	 *
	 * @param methods a set of methods to expand (can contain
	 * ALL_METHODS, ALL_MODIFIERS, and ALL_GETTERS keywords)
	 * @return a set of methods where the keywords have been expanded
	 * with the corresponding method of the type 
	 */
	protected String[] expandMethods(String[] methods) {
		if (methods == null)
			return null;
		Vector newVM = new Vector();
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].equals(ALL_METHODS)) {
				newVM.addAll(
					Arrays.asList(ClassRepository.getMethodsName(type)));
			} else if (methods[i].equals(ALL_MODIFIERS)) {
				newVM.addAll(
					Arrays.asList(ClassRepository.getModifiersNames(type)));
			} else if (methods[i].equals(ALL_GETTERS)) {
				newVM.addAll(
					Arrays.asList(ClassRepository.getGettersNames(type)));
			} else {
				newVM.add(methods[i]);
			}
		}
		String[] newMethods = new String[newVM.size()];
		for (int i = 0; i < newMethods.length; i++) {
			newMethods[i] = (String) newVM.get(i);
		}
		return newMethods;
	}

	/* (non-Javadoc)
	 * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
	 */
	public Object invoke(MethodInvocation invocation) throws Throwable {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.aopalliance.intercept.ConstructorInterceptor#construct(org.aopalliance.intercept.ConstructorInvocation)
	 */
	public Object construct(ConstructorInvocation invocation)
		throws Throwable {
		// TODO Auto-generated method stub
		return null;
	}

}
