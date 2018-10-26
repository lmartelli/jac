/*
  Copyright (C) 2001-2003 Renaud Pawlak <renaud@aopsys.com>

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

package org.objectweb.jac.aspects.naming;

import org.apache.log4j.Logger;
import org.objectweb.jac.core.*;
import org.objectweb.jac.core.Naming;
import org.objectweb.jac.core.dist.*;
import org.objectweb.jac.util.*;
import org.objectweb.jac.util.ExtArrays;

/**
 * This aspect component implements the default binding policy for
 * the JAC system.
 *
 * <p>The binding aspect uses the naming aspect. Do not try to use it
 * alone.
 *
 * @see NamingAC */

public class BindingAC extends AspectComponent {

    static Logger logger = Logger.getLogger("naming");
    static Logger loggerSerial = Logger.getLogger("serialization");

    /**
     * Bind a deserialized JAC object.
     *
     * <p>This method takes the name filled by the naming aspect within
     * the serialized infos and wraps the final object by a
     * <code>BindingWrapper</code> so that it can be resolved later
     * on. If the forwarding is disabled for this object, the new
     * object is registered within the repository and is not wrapped by
     * a binding wrapper.
     *
     * @param orgObject the JAC object that is being deserialized.
     *
     * @see org.objectweb.jac.util.Repository#register(String,Object) 
     * @see org.objectweb.jac.aspects.naming.NamingAC#whenSerialized(Wrappee,SerializedJacObject)
     * @see #whenDeserialized(SerializedJacObject,Wrappee)
     * @see org.objectweb.jac.core.SerializedJacObject#getACInfos(String)
     * @see BindingWrapper 
     */
   
    public Wrappee whenDeserialized(SerializedJacObject orgObject, 
                                    Wrappee finalObject) {
      
        String name = (String)orgObject.getACInfos("naming");
        loggerSerial.debug("binding "+orgObject+"(name = "+name+")");
        if (name != null) {
            Object newFinal = NameRepository.get().getObject(name);
            if (newFinal != null) {
                loggerSerial.debug(name+" exists on the site ("+newFinal+")");
                if(!name.equals(NameRepository.get().getName(finalObject))) {
                    loggerSerial.debug("deleting "+finalObject);
                    ObjectRepository.delete(finalObject);
                }
                loggerSerial.debug("replacing with "+newFinal);
                finalObject = (Wrappee)newFinal;
            } else {
                if (orgObject.isForwarder()) {
                    loggerSerial.debug("is forwarder");
                    Wrapping.wrapAll(
                        ((Wrappee)finalObject),
                        null,
                        new BindingWrapper(this, name));
                } else {
                    loggerSerial.debug("is new object");
                    // Upcall the ACManager to notify it from a kind of 
                    // remote instantiation
                    ((ACManager)ACManager.get()).whenRemoteInstantiation(
                        (Wrappee)finalObject,
                        name);
                }
            }
        }
        return finalObject;
    }

    /**
     * Add the name to the SerializedJacObject when an object that is
     * wrapped by a <code>BindingWrapper</code> serialized.
     *
     * @param finalObject the corresponding serialized structure.
     *
     * @see #whenDeserialized(SerializedJacObject,Wrappee)
     * @see org.objectweb.jac.core.SerializedJacObject#setACInfos(String,Object)
     * @see BindingWrapper 
     */

    public Wrappee whenSerialized(Wrappee orgObject, 
                                  SerializedJacObject finalObject) {
        if (orgObject.getClass().getName().startsWith("org.objectweb.jac.lib.java.util")) {
            return orgObject;
        }
       
        NameRepository nr = (NameRepository)NameRepository.get();
        if (Wrapping.isExtendedBy(orgObject, null, BindingWrapper.class)) {
            //System.out.println( "Serializing binder for " + orgObject );
            String name =
                (String)Wrapping.invokeRoleMethod(orgObject, 
                                                  "getLogicalName", 
                                                  ExtArrays.emptyObjectArray);
            if (name != null) {
                //System.out.println( "BINDING: serializing object " + name );
                finalObject.setACInfos("naming", name);
            }
        }
        return orgObject;
    }

    public void whenObjectMiss(String name) {
        logger.debug("object miss in name repository: "+name);
        if (name.equals(Naming.PARSER_NAME)) {
            Parser acParser = new ParserImpl();
            String crName = DistdClassLoader.classRepositoryName;
            // crName == null means that we are on the master site
            if (crName != null) {
                logger.debug("Create stub for "+Naming.PARSER_NAME+" on "+crName);
                RemoteRef rr = RemoteContainer.resolve(crName).bindTo(Naming.PARSER_NAME);
                Wrapping.wrapAll(((Wrappee)acParser),null,
                                 new StubWrapper(this,rr));
            } else {
                logger.debug("No stub for "+Naming.PARSER_NAME);
            }
            attrdef(BaseProgramListener.FOUND_OBJECT, acParser);
        } else {
            attrdef(BaseProgramListener.FOUND_OBJECT, null);
        }
    }
}
