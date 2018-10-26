/*
  Copyright (C) 2001-2003 Renaud Pawlak, Laurent Martelli
  
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

package org.objectweb.jac.aspects.gui;

import java.util.Arrays;
import org.objectweb.jac.core.NameRepository;
import org.objectweb.jac.core.rtti.AbstractMethodItem;
import org.objectweb.jac.util.Strings;

/**
 * This class holds a method and its parameters that can be passed to a
 * peer object as a callback.
 */
public class Callback {
    AbstractMethodItem method;
    Object[] parameters;
    String objectName;
    /**
     * Constructs the callback. */
    public Callback(String objectName,AbstractMethodItem method, Object[] parameters) {
        if (Strings.isEmpty(objectName) && !method.isStatic())
            throw new RuntimeException("Method "+method+" is not static; An object must be provided");
        this.objectName = objectName;
        this.method = method;
        this.parameters = parameters;
    }
    /**
     * The callback method. */
    public AbstractMethodItem getMethod() {
        return method;
    }
    /**
     * The parameters that can be passed to the callback method. */ 
    public Object[] getParameters() {
        return parameters;
    }
    public String getObjectName() {
        return objectName;
    }
    Object object;
    public Object getObject() {
        if (object==null && objectName!=null) {
            object = NameRepository.get().getObject(objectName);
        }
        return object;
    }

    public void invoke(DisplayContext context, View source) {
        EventHandler.get().onInvoke(
            context,
            new InvokeEvent(source,getObject(),method,parameters));
    }

    public String toString() {
        return "Callback@"+Integer.toHexString(System.identityHashCode(this))+
            " "+objectName+"."+method.getName()+
            "("+(parameters!=null?Arrays.asList(parameters).toString():"")+")";
    }
}
