/*
  Copyright (C) 2001 Renaud Pawlak

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


import java.util.*;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.*;
import org.objectweb.jac.core.dist.*;
import org.objectweb.jac.util.*;

/**
 * This aspect component implements a remote access aspect.
 *
 * <p>On contrary to the deployement AC, JAC do not have to be lauched
 * in a distributed mode and the server host do not have to be
 * registered in the topology. Hence, this aspect allows a JAC
 * container to bind to a different namespace/aspect-space in a
 * client/server mode.
 * 
 * @author <a href="http://cedric.cnam.fr/~pawlak/index-english.html">Renaud Pawlak</a>
 *
 * @see RemoteAccessConf
 * @see DeploymentRule */

public class RemoteAccessAC extends AspectComponent implements RemoteAccessConf {
    static Logger logger = Logger.getLogger("remoteaccess");
    static Logger loggerSerial = Logger.getLogger("serialization");

    public void whenSerialized ( SerializedJacObject finalObject ) {
	
        loggerSerial.debug("DeploymentAC.whenSerialize");
        NameRepository nr = (NameRepository) NameRepository.get();
        Wrappee orgObject = (Wrappee)attr( "orgObject" );
        String name = nr.getName( orgObject );
        //if ( ! isMatchingARule( orgObject ) ) {
        loggerSerial.debug(name + " is not matching any rule");
        finalObject.disableForwarding();
        Object[] state = ObjectState.getState(orgObject);
        String[] fieldsName = (String[]) state[0];
        Object[] fieldsValue = (Object[]) state[1];
      
        for ( int i = 0; i < fieldsName.length; i++ ) {
            loggerSerial.debug(" serializing field "+fieldsName[i]+
                               " - "+fieldsValue[i]); 
            finalObject.addField( fieldsName[i], fieldsValue[i] );
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
    }

    /**
     * Fill the field values when the forwarding is disabled for this
     * object..
     *
     * @param orgObject the JAC object that is being deserialized. */
   
    public void whenDeserialized ( SerializedJacObject orgObject ) {
        loggerSerial.debug("DeploymentAC.whenDeserialize");
        if ( ! orgObject.isForwarder() ) {
            loggerSerial.debug("not a forwarder");
            Wrappee finalObject = (Wrappee)attr("finalObject");
            RemoteRef server;
            if( (server = (RemoteRef) orgObject.getACInfos( ACManager.get().getName( this ) ) ) != null ) {
                Wrapping.wrapAll(finalObject,null,new StubWrapper(this,server));
            } else {
                Iterator it = orgObject.getFields().keySet().iterator();
                while ( it.hasNext() ) {
                    String fn = (String) it.next();
                    loggerSerial.debug("setting field "+fn+" <- "+
                                       orgObject.getField( fn ));
                    ObjectState.setFieldValue(finalObject,fn,
                                              ObjectState.getField(orgObject,fn) );
                }
            }
        }
    }   

    /**
     * 
     */
   
    public void whenCreatingRemoteAccess(Wrappee wrappee, String serverHost) {
        Wrapping.wrapAll(wrappee,null,new StubWrapper(this,serverHost));
    }

    public void createRemoteAccess(String nameExpr, 
                                   String classExpr, 
                                   String methodExpr,
                                   String serverHost) {
        try {
            pointcut(nameExpr, classExpr, methodExpr,
                     StubWrapper.class.getName(), 
                     new Object[] {serverHost}, "invoke", null, true);
            //         pointcut( nameExpr, classExpr, 
            //          "whenCreatingRemoteAccess", new Object[] {serverHost} );
        } catch ( Exception e ) {
            logger.error("createRemoteAccess: pointcut creation failed",e);
        }
    }

}
