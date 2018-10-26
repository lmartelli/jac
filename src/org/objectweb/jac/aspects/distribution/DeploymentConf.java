/*
  Copyright (C) 2001-2003 Renaud Pawlak

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

package org.objectweb.jac.aspects.distribution;

import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.MethodItem;

/**
 * This aspect is configuration interface of the Deployment aspect.
 * 
 * @author <a href="http://cedric.cnam.fr/~pawlak/index-english.html">Renaud Pawlak</a>
 *
 * @see DeploymentAC
 * @see DeploymentRule */

public interface DeploymentConf {

   /**
    * This configuration method creates a deployment rule that tells
    * that the objects that match are deployed on a given remote
    * container.<p>
    * 
    * @param deploymentHost the host from where the objects are
    * deployed
    * @param nameRegexp all the objects for which the name will
    * match this pattern will be deployed with this rule
    * @param containerName the container that will hold the objects */

   void deploy( String deploymentHost, String nameRegexp, String containerName );

   /**
    * This configuration method creates a deployment rule that replicates
    * an original object on a set of containers.
    * 
    * @param deploymentHost the host from where the objects are
    * deployed
    * @param nameRegexp all the objects for which the name will match
    * this regular expression will be replicated
    * @param contRegexp identifies the containers that will hold the
    * replication group (one replica per container) */

   void replicate ( String deploymentHost, String nameRegexp, String contRegexp );

   /**
    * This configuration method creates remote-access stubs for the
    * object called name on all the client-hosts defined by the hosts
    * expression.
    *
    * @param name the name of the object the stubs are created for
    * @param serverHost the name of the container the server is located
    * @param hosts an expression telling where the stubs are deployed
    * @param stubType the type of the stub (a StubWrapper subclass)
    */

   void createTypedStubsFor( String name, String serverHost, 
                             String hosts, String stubType );

   /**
    * This configuration method creates the stubs with the default
    * type (org.objectweb.jac.core.dist.StubWrapper).
    *
    * @param name the name of the object the stubs are created for
    * @param serverHost the name of the container the server is located
    * @param hosts an expression telling where the stubs are deployed
    */

   void createStubsFor( String name, String serverHost, String hosts );

   /**
    * This configuration method creates the stubs with an asynchronous
    * type (org.objectweb.jac.core.dist.NonBlockingStubWrapper).
    *
    * @param name the name of the object the stubs are created for
    * @param serverHost the name of the container the server is located
    * @param hosts an expression telling where the stubs are deployed
    */

   void createAsynchronousStubsFor( String name, String serverHost, String hosts );

   /**
    * This configuration method sets a field to be transient.
    *
    * @param classItem the class item of the field
    * @param fieldName the field name */

   void setTransient(ClassItem classItem, String fieldName);

    /**
     * Defines the passing mode of the parameters for a given method.
     *
     * <p>When a method is fowarded to a remote host by a stub
     * (wrapper), the parameters of this method can be passed by value
     * (then the whole object is serialized on the local host and
     * deserialized on the remote host) or by reference (then the
     * object's state is not serialized and it is bounded to a remote
     * object on the remote host).
     *
     * @param method the method that holds the parameters
     * @param refs TRUE or FALSE for each parameter... by default, the
     * parameters are passed by value */
  void setParametersPassingMode(MethodItem method, Boolean[] refs);

}


