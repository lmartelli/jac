/*
  Copyright (C) 2001-2002 Renaud Pawlak, Lionel Seinturier

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

package org.objectweb.jac.core.dist;


import org.objectweb.jac.core.AspectComponent;
import org.objectweb.jac.core.NameRepository;
import org.objectweb.jac.core.Wrappee;
import org.objectweb.jac.core.Wrapping;
import java.io.Serializable;

/**
 * This class deploies a set of objects on a set of host given a
 * deployment mapping style (a function that gives the objects
 * location in the topology).
 * 
 * <p>It can be used by an aspect component that would implement a
 * distribution aspect.
 *
 * @author <a href="http://cedric.cnam.fr/~pawlak/index-english.html">Renaud Pawlak</a>
 * @author <a href="http://www-src.lip6.fr/homepages/Lionel.Seinturier/index-eng.html">Lionel Seinturier</a> */

public class Deployment implements Serializable {

   /** Store the topology. */
   Topology topology;

   transient AspectComponent ac;
   

   /**
    * Creates a new deployment. For the moment, only the MS_ONE2ONE is
    * available. Anyway, it is the most usual strategy.
    *
    * @param topology the topology used by the deployment
    */
   public Deployment(AspectComponent ac, Topology topology) {
      this.topology = topology;
      this.ac = ac;
   }

   /**
    * Deploys a set of JAC object that are centralized on the local
    * host to the Topology.<p>
    *
    * Do not copy the state of the objects.
    * 
    * @param objects a set of object to deploy on the topology
    * @param forward if true, create a stub wrapper on each local
    * object that forwards all the calls to the remotly deployed
    * objects
    * @return remote references on the deployed objects
    *
    * @see #deploy(Object[],boolean) */

   public RemoteRef[] deployStruct(Object[] objects, boolean forward) {
      
      RemoteContainer[] rcs = topology.getDistContainers ();
      RemoteRef[] ret = new RemoteRef[objects.length];

      for (int i = 0; i < objects.length; i++) {

         System.out.println( "Deploying " + 
                             NameRepository.get().getName(objects[i]) + 
                             " on " + rcs[i].getName());
         RemoteRef rr = RemoteRef.create( 
            NameRepository.get().getName( objects[i] ) );

         // THIS COMMENT MAY CAUSE REGRESSION
         /*Collaboration.get().setCurApp(
            ApplicationRepository
            .getOwningApplication((Wrappee)objects[i]).getName());*/
         rr.remoteNew( rcs[i].getName(),
                       objects[i].getClass().getName() );
         if ( forward ) {
            StubWrapper w = new StubWrapper(ac,rr);
            Wrapping.wrapAll((Wrappee)objects[i],null,w);
         }
         ret[i] = rr;
      }      
      return ret;
   }

   /**
    * Equals to deployStruct( objects, false ).
    *
    * @param objects the object to deploy
    * @return the array of the remote references pointing on the
    * deployed objects
    *
    * @see #deploy(Object[],boolean) */
   
   public RemoteRef[] deployStruct(Object[] objects) {
      return deployStruct(objects, false);
   }  

   /**
    * Deploys a set of JAC object that are centralized on the local
    * host to the Topology. This is a static deployment and the
    * references between the different local objects must have already
    * been built (it means that the application must have been
    * initialized in a centralized way.
    *
    * @param objects a set of object to deploy on the topology
    * @param forward if true, create a stub wrapper on each local
    * object that forwards all the calls to the remotly deployed
    * objects
    * @return remote references on the deployed objects
    *
    * @see #deployStruct(Object[],boolean) */

   public RemoteRef[] deploy(Object[] objects, boolean forward) {
      
      RemoteRef[] ret = new RemoteRef[objects.length];

      for (int i = 0; i < objects.length; i++) {

         System.out.println( "Deploying " + NameRepository.get().getName(objects[i]) + 
                             " on " + topology.getContainer(i).getName());
         RemoteRef rr = RemoteRef.create( NameRepository.get().getName( objects[i] ) );
         
         // THIS COMMENT MAY CAUSE REGRESSION
         /*Collaboration.get().setCurApp(
            ApplicationRepository
            .getOwningApplication((Wrappee)objects[i]).getName());*/
         rr.remoteNewWithCopy( topology.getContainer(i).getName(),
                               objects[i].getClass().getName(),
                               objects[i] );
         if ( forward ) {
            StubWrapper w = new StubWrapper(ac,rr);
            Wrapping.wrapAll((Wrappee)objects[i],null,w);
         }
         ret[i] = rr;
      }      
      return ret;
   }

   /**
    * Equals to deploy( objects, false ).
    *
    * @param objects the object to deploy
    * @return the array of the remote references pointing on the
    * deployed objects
    *
    * @see #deploy(Object[],boolean) */
   
   public RemoteRef[] deploy(Object[] objects) {
      return deploy (objects, false);
   }

   /**
    * Replicates a JAC object that is located on the local
    * host to the hosts of the Topology.
    *
    * @param object the local object to replicate
    * @param forwardTo the index of the topology where the local
    * object will forward the calls (if -1 or not valid for the
    * topology then do not forward any call)
    * @return remote references on the replicated objects
    *
    * @see #replicateStruct(Object,int) */

   public RemoteRef[] replicate(Object object, int forwardTo) {

      RemoteContainer[] rcs = topology.getDistContainers ();
      RemoteRef[] ret = new RemoteRef[rcs.length];
      RemoteRef rr = null;
      String name = NameRepository.get().getName(object);

      for (int i = 0; i < rcs.length; i++) {

         System.out.println( "Replicating " + name + 
                             " on " + rcs[i].getName());
         
         if( rcs[i].bindTo( name ) != null ) {
            System.out.println( name + " already replicated on "
                                + rcs[i].getName());
            continue; 
         }

         // THIS COMMENT MAY CAUSE REGRESSION

         /*         Collaboration.get().setCurApp(
            ApplicationRepository.
            getOwningApplication((Wrappee)object).getName());*/

         rr = RemoteRef.create( name );
         rr.remoteNewWithCopy( rcs[i].getName(),
                               object.getClass().getName(),
                               object );
         
         if ( forwardTo == i ) {
            StubWrapper w = new StubWrapper(ac,rr);
            Wrapping.wrapAll((Wrappee)object,null,w);
         }
         ret[i] = rr;
      }
      return ret;
   }

   /**
    * Equals to replicate( object, -1 ).
    *
    * @param object the object to replicate
    * @return the array of the remote references pointing on the
    * replicated objects
    *
    * @see #replicate(Object,int) */

   public RemoteRef[] replicate(Object object) {
      return replicate(object, -1);
   }

   /**
    * Replicates a JAC object that is located on the local
    * host to the hosts of the Topology.
    *
    * <p>Do not copy the objects states.
    *
    * @param object the local object to replicate
    * @param forwardTo the index of the topology where the local
    * object will forward the calls (if -1 or not valid for the
    * topology then do not forward any call)
    * @return remote references on the replicated objects
    *
    * @see #replicate(Object,int) */

   public RemoteRef[] replicateStruct(Object object, int forwardTo) {

      RemoteContainer[] rcs = topology.getDistContainers ();
      RemoteRef[] ret = new RemoteRef[rcs.length];
      RemoteRef rr = null;
      String name = NameRepository.get().getName(object);
      
      for (int i = 0; i < rcs.length; i++) {

         if( rcs[i].bindTo( name ) != null ) {
            System.out.println( name + " already replicated on "
                                + rcs[i].getName());
            continue; 
         }

         System.out.println( "Replicating (struct) " + 
                             NameRepository.get().getName(object) + 
                             " on " + rcs[i].getName());
         
         // THIS COMMENT MAY CAUSE REGRESSION

         /*         Collaboration.get().setCurApp(
            ApplicationRepository
            .getOwningApplication((Wrappee)object).getName());*/

         rr = RemoteRef.create( NameRepository.get().getName( object ) );
         rr.remoteNew( rcs[i].getName(), object.getClass().getName() );
         if ( forwardTo == i ) {
            StubWrapper w = new StubWrapper(ac,rr);
            Wrapping.wrapAll((Wrappee)object,null,w);
         }
         ret[i] = rr;
      }
      return ret;
   }

   /**
    * Equals to replicateStruct( object, -1 ).
    *
    * @param object the object to replicate
    * @return the array of the remote references pointing on the
    * replicated objects
    *
    * @see #replicateStruct(Object,int) */
   
   public RemoteRef[] replicateStruct(Object object) {
      return replicateStruct (object, -1);
   }


}







