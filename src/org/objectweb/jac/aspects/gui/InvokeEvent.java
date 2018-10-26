/*
  Copyright (C) 2004 Laurent Martelli <laurent@aopsys.com>
  
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

package org.objectweb.jac.aspects.gui;

import java.util.Arrays;
import org.objectweb.jac.core.rtti.AbstractMethodItem;

/**
 * Event for a method invocation
 */
public class InvokeEvent extends SubstanceEvent {
    public InvokeEvent(View source, 
                       Object substance, AbstractMethodItem method,
                       Object[] parameters) {
        super(source,substance);
        this.method = method;
        this.parameters = parameters;
    }

    public InvokeEvent(View source, 
                       Object substance, AbstractMethodItem method) {
        super(source,substance);
        this.method = method;
    }

    /** Invoked method */
    AbstractMethodItem method;
    public AbstractMethodItem getMethod() {
        return method;
    }
    public void setMethod(AbstractMethodItem newMethod) {
        this.method = newMethod;
    }

    Object[] parameters;
    public Object[] getParameters() {
        return parameters;
    }
    public void setParameters(Object[] newParameters) {
        this.parameters = newParameters;
    }

    public String toString() {
        return "InvokeEvent{source="+source+", "+substance+"."+method.getLongName()+
            " with "+(parameters!=null?Arrays.asList(parameters):null)+"}";
    }
}
