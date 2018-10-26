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

  You should have received a copy of the GNU Lesser General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA */

package org.objectweb.jac.aspects.distribution;


//import java.util.*;
import java.util.HashSet;
import java.util.Iterator;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.*;
import org.objectweb.jac.core.dist.*;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.MethodItem;
import org.objectweb.jac.util.*;

/**
 * This aspect component implements a generic deployment aspect.
 * 
 * @author <a href="http://cedric.cnam.fr/~pawlak/index-english.html">Renaud Pawlak</a>
 *
 * @see DeploymentConf
 * @see DeploymentRule */

public class DeploymentAC extends AspectComponent implements DeploymentConf {
    static Logger logger = Logger.getLogger("deployment");
    static Logger loggerSerial = Logger.getLogger("serialization");

    /** the name of the metadata that tells whether the parameters of
     * a methods are references (TRUE) or values (FALSE). */
    public static final String REFS= "DeploymentAC.REFS";

   /** already deployed objects */
   transient HashSet treated = new HashSet();

    /**
     * Force the treatment of the objects that are being
     * serialized. This allows the deployment aspect to be sure that
     * an object that is passed as a parameter of a remote invocation
     * is already deployed when arriving on the new site. If none of
     * the deployment rule can be applied on the serialized object,
     * then the object will not be a forwarder but a copy of the
     * original object.
     *
     * @param finalObject the structure to store the serialized infos
     */

   public Wrappee whenSerialized(Wrappee orgObject, 
                                 SerializedJacObject finalObject) {
	
       NameRepository nr = (NameRepository)NameRepository.get();
       String name = nr.getName(orgObject);
       loggerSerial.debug("DeploymentAC.whenSerialize " + name);
       //if ( ! isMatchingARule( orgObject ) ) {
       finalObject.disableForwarding();
       
       if (Collaboration.get().getAttribute(SerializedJacObject.STATELESS)==null) {
	   
	   Object[] state = ObjectState.getState(orgObject);
	   String[] fieldsName = (String[])state[0];
	   Object[] fieldsValue = (Object[])state[1];
	   
	   ClassItem cl = ClassRepository.get().getClass(orgObject.getClass());
	   for (int i = 0; i < fieldsName.length; i++) {
	       loggerSerial.debug(" serializing field " + fieldsName[i] + " - " + fieldsValue[i]);
	       FieldItem fi = cl.getField(fieldsName[i]);
	       if (fi != null && fi.getAttribute("deployment.transient") == null) {
		   finalObject.addField(fieldsName[i], fieldsValue[i]);
	       }
	   }
	   
       } else {
	   loggerSerial.debug(name+" is stateless in this context");
       }
       
       /*} else {
	 DeploymentRule r = getMatchingRule( orgObject );
	 if( r != null && r.getType().equals( "dynamic client-server" ) ) {
	 finalObject.disableForwarding();
	 finalObject.setACInfos( 
	 ACManager.get().getName( this ),
	 RemoteRef.create( NameRepository.get().getName( orgObject ), orgObject ) );
	 }
	 }*/
       return orgObject;
   }

   /**
    * Fill the field values when the forwarding is disabled for this
    * object..
    *
    * @param orgObject the JAC object that is being deserialized. */
   
   public Wrappee whenDeserialized(SerializedJacObject orgObject, 
                                   Wrappee finalObject) {
      loggerSerial.debug("DeploymentAC.whenDeserialize "+
                orgObject.getJacObjectClassName());
      if (!orgObject.isForwarder()) {
         loggerSerial.debug("not a forwarder, deserialize...");
         RemoteRef server = 
            (RemoteRef)orgObject.getACInfos(ACManager.get().getName(this));
         if (server != null) {
            Wrapping.wrapAll(finalObject,null,
                             new StubWrapper(this,server));
         } else {
            Iterator it = orgObject.getFields().keySet().iterator();
            while (it.hasNext()) {
               String fn = (String)it.next();
               loggerSerial.debug("setting field "+fn+" <- "+
                                  orgObject.getField(fn));
               ObjectState.setFieldValue(
                  finalObject,fn,orgObject.getField(fn));
            }
         }
      } else {
         loggerSerial.debug("forwarder, do not deserialize...");
      }
      return finalObject;
   }

   /*
    * Actually deploys an object with a target host expression.
    *
    * @param wrappee the object to deploy
    * @param hostExpr the host where to deploy the object
    * @param state ???
    */

   /*public void deployTo(Wrappee wrappee, String hostExpr, boolean state) {
      if (wrappee==null)
         return;
      if (treated.contains(wrappee)) 
         return;
      treated.add(wrappee);
      Log.trace("deployment",
                "DEPLOYED UPCALLED WITH "+hostExpr+
                " wrappee="+wrappee+", topology="+Topology.get());
      Topology topology = Topology.getPartialTopology(hostExpr);
      Deployment dep = new Deployment(this,topology);
      if (state) {
         dep.replicate(wrappee);
      } else {
         dep.replicateStruct(wrappee);
      }
      }*/

   public void deploy(String deploymentHost,
                      String nameRegexp, String containerName) {

      pointcut(nameRegexp,".* && !org.objectweb.jac.lib.java.util.*","CONSTRUCTORS",
               "org.objectweb.jac.aspects.distribution.DeploymentWrapper", 
               new Object[] {this,containerName,new Boolean(true)},
               deploymentHost,null,true);

      //      addDeploymentRule( new DeploymentRule( 
      //   "deployment", nameRegexp, containerName, true ) ); 
      
   }

   public void replicate(String deploymentHost, String nameRegexp,
                         String contRegexp) {

      pointcut(nameRegexp,".* && !org.objectweb.jac.lib.java.util.*","CONSTRUCTORS",
               "org.objectweb.jac.aspects.distribution.DeploymentWrapper", 
               new Object[] {this,contRegexp,new Boolean(true)},
               deploymentHost,null,true);
   }

   public void createTypedStubsFor(String name, String serverHost, 
                                   String hosts, 
                                   String stubType) {
      pointcut(name,".* && !org.objectweb.jac.lib.java.util.*","CONSTRUCTORS",
               "org.objectweb.jac.aspects.distribution.DeploymentWrapper", 
               new Object[] {this,hosts,new Boolean(true)},
               serverHost,null,true);
      /*pointcut(name, ".*", "deployTo", 
               new Object[] { hosts, new Boolean(false) }, 
               serverHost, null);*/
      try {
         pointcut(name, ".*", ".*", 
                  (Wrapper)Class.forName(stubType)
                  .getConstructor(new Class[]{String.class})
                  .newInstance(new Object[]{serverHost}), 
                  hosts+" && !"+serverHost, null);
      } catch ( Exception e ) {
         logger.error("createTypedStubsFor: pointcut creation failed",e);
      }
   }

   public void createStubsFor(String name, String serverHost, String hosts) {
      createTypedStubsFor(name, serverHost, hosts,
                          "org.objectweb.jac.core.dist.StubWrapper");
   }

   public void createAsynchronousStubsFor(String name, String serverHost, 
                                          String hosts) {
      createTypedStubsFor(name, serverHost, hosts,
                          "org.objectweb.jac.core.dist.NonBlockingStubWrapper");
   }

   public void setTransient(ClassItem classItem, String fieldName) {
      FieldItem fi = classItem.getField(fieldName);
      fi.setAttribute("deployment.transient","true");
   }

    public void setParametersPassingMode(MethodItem method, Boolean[] refs) {
	method.setAttribute(REFS, refs);
    }

}


