/*
  Copyright (C) 2002 Renaud Pawlak <renaud@aopsys.com>

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

package org.objectweb.jac.util;

import java.util.Arrays;
import java.util.List;

public class Enum {
    String[] values;
    int start = 0;
    int step = 1;
    public Enum(String[] values, int start, int step) {
        this.values = values;
        this.start = start;
        this.step = step;
    }

    /**
     * Converts an integer value to it's string representation
     * @param index the integer value to convert
     * @throws InvalidIndexException
     */
    public String int2string(int index) {
        if (((float)(index-start)/((float)step))!=(float)((index-start)/step))
            throw new InvalidIndexException(index);
        String value;
        try {
            value=values[(index-start)/step];
        } catch(Exception e) {
            throw new InvalidIndexException(index);
        }
        return value;
    }
    /**
     * Converts a string value to an integer. Throws an exception if
     * the enum does not define that string.
     */
    public int string2int(String string) {
        if (string==null) 
            throw new RuntimeException("Invalid string value null for enum");
        int index = start;
        for(int i=0; i<values.length; i++) {
            if (string.equals(values[i])) {
                return index;
            } else {
                index += step;
            }
        }
        throw new RuntimeException("Invalid string value '"+string+"' for enum");
    }
    public List getValues() {
        return Arrays.asList(values);
    }
    public int getStart() {
        return start;
    }
    public int getStep() {
        return step;
    }
}
