/*
  Renaud Pawlak, pawlak@cnam.fr, CEDRIC Laboratory, Paris, France.
  Lionel Seinturier, Lionel.Seinturier@lip6.fr, LIP6, Paris, France.

  JAC-Core is free software. You can redistribute it and/or modify it
  under the terms of the GNU Library General Public License as
  published by the Free Software Foundation.
  
  JAC-Core is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

  This work uses the Javassist system - Copyright (c) 1999-2000
  Shigeru Chiba, University of Tsukuba, Japan.  All Rights Reserved.  */

package org.objectweb.jac.aspects.distribution.bootstrap;

import org.objectweb.jac.core.*;
import org.objectweb.jac.core.dist.*;
import org.objectweb.jac.aspects.distribution.consistency.*;

/**
 * The following class binds a new Jac client to a Jac distributed system. */

public class BindClient {

   /**
    * This method is the entry point for a Jac application launched
    * with the -C option. */

   public static void main( String[] args ) throws Throwable {      

      System.out.println( 
         "--- Binding to the distributed namespace and aspect-space ---" );
      System.out.println( 
         "--- Topology is " + Topology.get() + " ---" );

      /** We suppose that the Jac reference server is running on s0. */
      Topology.get().addContainer ( RemoteContainer.resolve ( "s0" ) );
      ((ACManager)ACManager.get()).registering = true;
      Consistency.bindToDistObj( "JAC_ac_manager", (Wrappee) ACManager.get() );
      Consistency.bindToDistObj( "JAC_topology", (Wrappee) Topology.get() );
      Consistency.bindToDistObj( "JAC_application_repository",
                                 (Wrappee) ApplicationRepository.get() );
      Topology.get().addContainer ( RemoteContainer.resolve ( args[0] ) );
      ((ACManager)ACManager.get()).registering = false;

      System.out.println( 
         "--- End of binding ---" );
      System.out.println( 
         "--- New topology is " + Topology.get() + " ---" );

   }

}





