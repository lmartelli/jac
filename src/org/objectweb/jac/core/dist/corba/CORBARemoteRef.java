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

import org.objectweb.jac.core.dist.RemoteContainer;
import org.objectweb.jac.core.dist.RemoteRef;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;


/**
 * CORBARemoteRef stores the reference of a remote object
 * that can be accessed by the CORBA/IIOP protocol.
 *
 * @author <a href="http://cedric.cnam.fr/~pawlak/index-english.html">Renaud Pawlak</a>
 * @author <a href="http://www-src.lip6.fr/homepages/Lionel.Seinturier/index-eng.html">Lionel Seinturier</a>
 */
 
public class CORBARemoteRef extends RemoteRef {

   /**
    * The string used to identify the type of objects registered
    * in the COS Naming.
    */
   
   final static protected String cosNamingEntryType = "jac daemon";
   
   
   /** This is the container CORBA remote reference stringified. */
   protected String remContString;
   

   /**
    * This method sets the reference of the remote CORBA container
    * starting from a logical name registered in the COS Naming.
    *
    * @param contName  the name of the container
    * @return          the container reference
    */
   
   public void resolve( String contName ) {
   
      org.omg.CORBA.Object obj = null;
      
      /** This is a logical name registered in the COS Naming */

      try {
         obj =
	    nc.resolve(
	       new NameComponent[]{
	          new NameComponent( contName, cosNamingEntryType )
	       }
	    );
      }
      catch( Exception e ) { e.printStackTrace(); }

      CORBARemoteContainerInterf stub =
         CORBARemoteContainerInterfHelper.narrow(obj);
      remContString = orb.object_to_string(obj);
      remCont = new CORBARemoteContainerStub(stub);
      
      remCont.setName ( contName );
      
   }
   
   
   /**
    * This method re-gets the reference of a remote container.
    * CORBA do not linearalize remote references in a standard way.
    * Thus a remote reference may need to be adapted whenever it is transmitted.
    *
    * This method is called when a remote reference
    * is received by a RemoteContainer.
    */
   
   public void reresolve() {

      org.omg.CORBA.Object obj = orb.string_to_object(remContString);
      CORBARemoteContainerInterf stub =
         CORBARemoteContainerInterfHelper.narrow(obj);
      remCont = new CORBARemoteContainerStub(stub);
   }
   


   /** The CORBA ORB. */
   static protected ORB orb;
   
   /** The CORBA root POA. */
   static protected POA poa;
   
   /** The root naming context of the COSNaming. */
   static protected NamingContext nc;

   
   static {
   
      orb = ORB.init( new String[]{}, null );
      
      try {
         poa = POAHelper.narrow( orb.resolve_initial_references("RootPOA") );
         nc =
	    NamingContextHelper.narrow(
	       orb.resolve_initial_references("NameService")
	    );
      }
      catch( Exception e ) { e.printStackTrace(); }
   }
   
}
