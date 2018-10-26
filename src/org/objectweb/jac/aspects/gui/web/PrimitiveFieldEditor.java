/*
  Copyright (C) 2002-2003 Laurent Martelli <laurent@aopsys.com>
  
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

package org.objectweb.jac.aspects.gui.web;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import org.objectweb.jac.aspects.gui.GuiAC;
import org.objectweb.jac.aspects.gui.Length;
import org.objectweb.jac.core.rtti.FieldItem;


/**
 * HTML editor for primitive types (int, long, float, double, short) and String
 *
 */
public class PrimitiveFieldEditor  extends AbstractFieldEditor 
    implements HTMLEditor
{
    boolean password;

    public PrimitiveFieldEditor(Object substance, FieldItem field,
                                boolean password) {
        super(substance,field);
        this.password = password;
        width = new Length("15ex");
    }

    public PrimitiveFieldEditor(Object substance, FieldItem field) {
        super(substance,field);
        width = new Length("15ex");
    }

    // HTMLEditor interface

    public void genHTML(PrintWriter out) {
        boolean hasDefault = field!=null && GuiAC.hasDefaultValue(field);
        out.print("<INPUT type=\""+(password?"password":"text")+"\""+
                  " class=\"editor\""+
                  " id=\""+label+"\""+
                  " name=\""+label+"\""+
                  sizeSpec()+
                  " value=\""+displayedValue()+"\"");
        printAttributes(out);
        out.print(">");
    }

    protected String displayedValue() {
        return value==null ? "" : value.toString();
    }

    protected boolean doReadValue(Object parameter) {
        if (parameter==null)
            return false;
        String string = (String)parameter;
        Class cl = type.getActualClass();
        if (cl == int.class || cl == Integer.class) {
            setValue(new Integer (string));
        } else if (cl == boolean.class || cl == Boolean.class) {
            setValue(Boolean.valueOf(string));
        } else if (cl == long.class || cl == Long.class) {
            setValue(new Long (string));
        } else if (cl == float.class || cl == Float.class) {
            setValue(new Float (string));
        } else if (cl == double.class || cl == Double.class) {
            setValue(new Double (string));
        } else if (cl == short.class || cl == Short.class) {
            setValue(new Short (string));
        } else if (cl == byte.class || cl == Byte.class) {
            setValue(new Byte(string));
        } else if (cl == char.class || cl == Character.class) {
            setValue(new Character(string.charAt(0)));
        } else if (cl == String.class) {
            setValue(string);
        } else {
            try {
                // trying to construct the object from its textual 
                // representation (I think that any class should have
                // a constructor taking a string... this is so helpful...)
                // of course, this will raise an exception most of the time :-(
                Constructor c = cl.getConstructor(new Class[] {String.class});
                setValue(c.newInstance(new Object[] {string}));
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Unhandled type "+type.getName());
            }
        }
        return true;
    }
}

