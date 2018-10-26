/*
  Copyright (C) 2002 Laurent Martelli.

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

package org.objectweb.jac.core;

import java.io.Serializable;

/**
 * This class represents configuration methods for aspect components.
 *
 * @see AspectComponent */

public class ConfigMethod implements Serializable, Cloneable {

    String methodName;
    Object[] args;
    String lineNumber;
    public ConfigMethod(String methodName, Object[] args, String lineNumber) {
        this.methodName = methodName;
        this.args = args;
        this.lineNumber = lineNumber;
    }
    public ConfigMethod(String methodName, Object[] args) {
        this.methodName = methodName;
        this.args = args;
        this.lineNumber = "???:???";
    }
    public String getMethod() {
        return methodName;
    }
    public Object[] getArgs() {
        Object[] result = new Object[args.length];
        System.arraycopy(args,0,result,0,args.length);
        return result;
    }
    public void setArgs(Object[] args) {
        this.args = args;
    }
    public String getLineNumber() {
        return lineNumber;
    }
    public String toString() {
        return "ConfigMethod("+methodName+","+java.util.Arrays.asList(args)+")";
    }
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
