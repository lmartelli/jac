/*
  Copyright (C) 2002 Laurent Martelli <laurent@aopsys.com>

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

package org.objectweb.jac.util;

import java.util.Arrays;


/**
 * This utility class wraps an array of objects and defines an
 * <code>hashCode()</code> and an <code>equals()</code>
 */

public class ObjectArray {
    Object [] args;
    public ObjectArray(Object[] args) {
        this.args = args;
    }
    public Object[] getArray() {
        return args;
    }
    public int hashCode() {
        int result = 0;
        for (int i=args.length-1; i>=0; i--) {
            if (args[i]!=null)
                result = result ^ args[i].hashCode();
        }
        return result;
    }
    public boolean equals(Object o) {
        return (o instanceof ObjectArray) 
            && Arrays.equals(((ObjectArray)o).args,args);
    }
    public String toString() {
        return Arrays.asList(args).toString();
    }
}
