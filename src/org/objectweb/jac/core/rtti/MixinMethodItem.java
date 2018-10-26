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

  You should have received a copy of the GNU Lesser General Public
  License along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  USA */

package org.objectweb.jac.core.rtti;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.objectweb.jac.util.ExtArrays;

public class MixinMethodItem extends MethodItem {
    public MixinMethodItem(Method method, ClassItem parent) 
        throws InvalidDelegateException 
    {
        super(method,parent);
        if (!Modifier.isStatic(method.getModifiers())) 
            throw new InvalidDelegateException(delegate,"Mixin method is not static");
        if (!method.getParameterTypes()[0].isAssignableFrom((Class)parent.getDelegate()))
            throw new InvalidDelegateException(
                delegate,"1st parameter of mixin method should be "+parent.getName());
        paramTypes = (Class[])ExtArrays.subArray(method.getParameterTypes(),1);
    }

    /**
     * Invoke as a static method, prepending object at the beginning
     * of parameters.
     */
    public Object invoke(Object object, Object[] parameters) {
        return invokeStatic(ExtArrays.add(0, object, parameters));
    }

    Class[] paramTypes;
    public Class[] getParameterTypes() {
        return paramTypes;
    }

    public void setParameter(Object[] params, int i, Object value) {
        params[i+1] = value;
    }
    public Object getParameter(Object[] params, int i) {
        return params[i+1];
    }
}
