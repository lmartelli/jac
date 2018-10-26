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
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA */

package org.objectweb.jac.aspects.naming;

import java.util.Hashtable;
import java.util.Map;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.*;
import org.objectweb.jac.core.Interaction;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.util.ExtArrays;
import org.objectweb.jac.util.Log;

/**
 * @author <a href="http://cedric.cnam.fr/~pawlak/index-english.html">Renaud Pawlak</a>
 */

/**
 * Handles the naming aspect within the JAC system.
 *
 * <p>This aspect component automatically registers all the created
 * JAC object into the sole instance of <code>NameRepository</code>.
 *
 * @see NameRepository */

public class NamingAC extends AspectComponent {

    static Logger logger = Logger.getLogger("naming");
    static Logger loggerSerial = Logger.getLogger("serialization");

    /**
     * Creates a naming aspect component.
     *
     * <p>It creates the name repository if it does not exist yet.
     *
     * @see NameRepository 
     */
    public NamingAC() {

        setSystemListener(true);

        NameRepository nr = (NameRepository) NameRepository.get();
        if (nr == null) {
            System.out.println ("Error: cannot create name repository.");
            System.exit(-1);
        }
    }

    NameGenerator nameGen = new NameGenerator();

    /**
     * Name a new instance using the <code>nameObject()</code> and
     * <code>generateName()</code> methods.
     *
     * @see #nameObject(Object,String)
     * @see NameGenerator#generateName(String) 
     */
    public void whenUsingNewInstance(Interaction interaction) {
        //if (interaction.wrappee.getClass().getName().startsWith("org.objectweb.jac.lib.java.util.")) {
        // do nothing for collections
        //   return;
        //}
        if (interaction.wrappee==null) {
            // do nothing for static methods
            return;
        }
        logger.debug("when using new instance "+interaction.wrappee.getClass());
        if (NameRepository.get().getName(interaction.wrappee)!=null) {
            return;
        }
        if ( !(interaction.wrappee instanceof AspectComponent) ) {
            nameObject(interaction.wrappee, (String)attr(Naming.FORCE_NAME));
            attrdef(Naming.FORCE_NAME, null);
        }
        //System.out.println( "EO Naming " + interaction.wrappee );      
    }

    /**
     * This method is upcalled when a new object is instanciated from a
     * remote site.
     * 
     * <p>The name that is passed is the name of the remote reference
     * that has been used to create the object. Thus, the default
     * behavior of the naming aspect is to register the new object into
     * the name repository.
     * 
     * @param newInstance the remotly created new instance
     * @param name the name that was forwarded from the creator host
     * 
     * @see NameRepository
     * @see org.objectweb.jac.util.Repository#register(String,Object) */

    public void whenRemoteInstantiation(Wrappee newInstance, String name) {
        logger.debug("whenRemoteInstantiation "+newInstance+","+name);
        NameRepository.get().register(name, newInstance);
    }

    /**
     * Name a single object by registering it in the name repository
     * (see the <code>NameRepository</code> class).
     * 
     * <p>If the repository does not exist, it is created.
     *
     * @param object the object to name
     * @param name the given name
     * @return the new name of the object
     *
     * @see NameRepository
     * @see org.objectweb.jac.util.Repository#register(String,Object) 
     */
    public String nameObject(Object object, String name) {
        NameRepository nr = (NameRepository)NameRepository.get();
        if (nr==null) {
            logger.error("Error: cannot create name repository.");
            throw new RuntimeException("Error: cannot create name repository.");
        }
        if (name == null) {
            name = nameGen.generateName(object.getClass().getName());
            logger.debug("generated name "+object+" -> "+name);
            name = ACManager.getACM().whenNameObject(object,name);
        } else {
            logger.debug("forced name "+object+" -> "+name);
        }
        logger.debug("Registering "+object+" -> "+name);
        nr.register(name, object);
        return name;
    }

    /**
     * Returns the counters used to generate unique names
     */
    public Map getNameCounters() {
        Hashtable counters = new Hashtable();
        counters.putAll(nameGen);
        ((ACManager)ACManager.get()).getNameCounters(counters);
        return counters;
    }

    public void updateNameCounters(Map counters) {
        nameGen.update(counters);
    }

    /**
     * Add the name to the <code>SerializedJacObject</code> structure
     * when serialized.
     *
     * <p>This allows the name to be serialized with a JAC object so
     * that the object can be named with the same name when deserialized
     * (see <code>BindingAC</code>).
     *
     * @param finalObject the structure corresponding to the object
     * that is being serialized
     *
     * @see BindingAC#whenDeserialized(SerializedJacObject,Wrappee)
     * @see org.objectweb.jac.core.SerializedJacObject#setACInfos(String,Object)
     */

    public Wrappee whenSerialized(Wrappee orgObject, 
                                  SerializedJacObject finalObject) {

        if(orgObject.getClass().getName().startsWith("org.objectweb.jac.lib.java.util")) {
            return orgObject;
        }

        NameRepository nr = (NameRepository) NameRepository.get();
        String name = nr.getName(orgObject);
        if (name!=null) {
            loggerSerial.debug("NAMING: serializing object " + name );
            finalObject.setACInfos(ACManager.get().getName(this), name);
        } else {
            if (Wrapping.isExtendedBy(orgObject,null,
                                      org.objectweb.jac.wrappers.ForwardingWrapper.class)) {
                loggerSerial.debug("NAMING: Serializing forwardee for " + orgObject );
                Object newOrg = Wrapping.invokeRoleMethod(orgObject,
                                                          "getForwardee",
                                                          ExtArrays.emptyObjectArray);
                if (newOrg!=null) {
                    orgObject = (Wrappee)newOrg;
                } else {
                    logger.warn("Oooops!! Forwardee is " + newOrg);
                }
            }
            name = nr.getName(orgObject);
            if (name!=null) {
                loggerSerial.debug("NAMING: serializing object " + name +" (PASS 2)");
                finalObject.setACInfos(ACManager.get().getName(this), name);
            } else {
                logger.warn("Oooops!! Object is still unamed: "+orgObject);
                //System.out.println( "NAMING: still a forwarder? => " + orgObject.isExtendedBy( jac.wrappers.ForwarderWrapper.class) );
            }
        }
        return orgObject;
    }
}
