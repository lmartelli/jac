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

package org.objectweb.jac.core.dist.corba;

import org.objectweb.jac.core.dist.Distd;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManager;


/**
 * CORBADistd is a jac daemon that support the IIOP communication protocol.
 *
 * Daemons hold containers (only one for the moment) which themselves hold
 * remote objects.
 *
 * @author <a href="http://cedric.cnam.fr/~pawlak/index-english.html">Renaud Pawlak</a>
 * @author <a href="http://www-src.lip6.fr/homepages/Lionel.Seinturier/index-eng.html">Lionel Seinturier</a>
 */
 
public class CORBADistd extends Distd {

   /** The CORBA ORB. */
   protected ORB orb;
   
   /** The CORBA root POA. */
   protected POA poa;
   
   /** The CORBA root POA manager. */
   POAManager manager;

   /** The root naming context of the COSNaming. */
   protected NamingContext nc;

   
   /**
    * This method initializes the CORBA environment.
    */
   
   public void init() {
   
      orb = ORB.init( new String[]{}, null );
      
      try {
         poa = POAHelper.narrow( orb.resolve_initial_references("RootPOA") );
         manager = poa.the_POAManager();
         nc =
	    NamingContextHelper.narrow(
	       orb.resolve_initial_references("NameService")
	    );
      }
      catch( Exception e ) { e.printStackTrace(); }
   }
   
   
   /**
    * This method creates a new container.
    *
    * @param name  the identifier of the container
    */
   
   public void newContainer( String name ) {

      registerContainer( new CORBARemoteContainer(verbose), name );
   }
  

   /**
    * The string used to identify the type of objects registered
    * in the COS Naming.
    */
   
   final static protected String cosNamingEntryType = "jac daemon";
   
   
   /**
    * This method register a container in the CORBA COSNaming.
    *
    * @param container  the container
    * @param name       the identifier of the container
    */
   
   protected void registerContainer( CORBARemoteContainer container, String name ) {

      try {
      
         /**
	  * Create the CORBA tie object that acts as a delegator
	  * for the container.
	  */
         
	 CORBARemoteContainerInterfPOATie containerTie =
	    new CORBARemoteContainerInterfPOATie(container);
	 
	 
         /** Register the container in the CORBA COSNaming */
         
	 nc.bind(
	    new NameComponent[]{new NameComponent(name,cosNamingEntryType)},
	    poa.servant_to_reference(containerTie)
	 );
	 
         System.out.println(
	    "--- org.objectweb.jac.dist.corba.CORBADistd: new container " + name + " ---"
	 );
	 
      }
      catch( Exception e ) { e.printStackTrace(); }
   }
  

   /**
    * This method creates a new container and instantiates a given class.
    *
    * @param name       the identifier of the container
    * @param className  the name of the class to instantiate
    */
   
   public void newContainer( String name, String className ) {
   
      registerContainer( new CORBARemoteContainer(className,verbose), name );
   }
   

   /**
    * This method enters the event loop of
    * the underlying communication protocol.
    */

   public void run() {
	 
      System.out.println( "--- org.objectweb.jac.dist.corba.CORBADistd is running ---" );
      
      try { manager.activate(); }
      catch( Exception e ) { e.printStackTrace(); }
      
      orb.run();
   }


   /**
    * The is the main constructor.
    *
    * @param args  command line arguments
    */
   
   public CORBADistd( String[] args ) { super(args); }
   
   public static void main( String[] args ) { new CORBADistd(args); }
   
}
