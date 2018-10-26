/*
  Copyright (C) 2001-2002 Renaud Pawlak, Lionel Seinturier.

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 2 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  USA */

package org.objectweb.jac.core;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.rtti.MethodItem;

/**
 * <code>JacObjectOutputStream</code> is used to write JAC objects
 * into an output stream during a serialization process.
 *
 * <p>This stream is used when serializing a JAC object into an array
 * of bytes when calling <code>JacObject.serialize()</code>. All the
 * objects that are not JAC objects are serialized with the default
 * procedure. When a JAC object is encountered, a
 * <code>whenSerialized</code> event is thrown on the current AC
 * manager so that the aspect components can parametrize the
 * serialization process.
 *
 * <p>A symetric process for deserialization is implemented by
 * <code>JacObjectInputStream</code>.
 *
 * @see ACManager#whenSerialized(Wrappee,SerializedJacObject)
 * @see JacObjectInputStream
 *
 * @author Renaud Pawlak
 * @author Lionel Seinturier
 */
 
public class JacObjectOutputStream extends ObjectOutputStream {
    static Logger logger = Logger.getLogger("serialization");

    /**
    * Creates a JacObjectInputStream. 
    *
    * @param os the output stream where the bytes are written. 
    */
    public JacObjectOutputStream(OutputStream os) throws IOException {   
        super(os);
        enableReplaceObject(true);
    }

    /**
    * This method is upcalled by the Java serialization process each
    * time a new object to serialize is encountered.
    *
    * <p>If a JAC object is encountered (instance of
    * <code>Wrappee</code>), the aspect component manager is upcalled
    * to parametrize the serialization.
    *
    * @param obj the encountered JAC object 
    * @return the final serialized JAC object
    *
    * @see SerializedJacObject
    * @see ACManager#whenSerialized(Wrappee,SerializedJacObject) */
   
    protected Object replaceObject(Object obj) throws IOException {
        if (obj instanceof Wrappee) {
            SerializedJacObject sjo = 
                new SerializedJacObject(obj.getClass().getName());
            if (obj.getClass().getName().startsWith("org.objectweb.jac.lib.java.util")) {
                sjo.disableForwarding();
            }
            ((ACManager)ACManager.get()).whenSerialized((Wrappee)obj,sjo);
            return sjo;
        } else if (obj instanceof AspectComponent) {
            SerializedJacObject sjo = 
                new SerializedJacObject(obj.getClass().getName());
            return sjo;
        } else if (obj instanceof MethodItem) {
            return new SerializedMethodItem((MethodItem)obj);
        } else if (obj.getClass().getName().equals("org.objectweb.jac.aspects.gui.DisplayContext")) {
            // <HACK reason="Don't crash when trying to serialize displayContext">
            logger.debug("replaceObject "+obj+" -> null");
            return null;
            // </HACK>
        }
        return obj;
    }

}
