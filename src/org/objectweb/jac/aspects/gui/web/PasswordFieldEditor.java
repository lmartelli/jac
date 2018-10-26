/*
  Copyright (C) 2001-2003 Laurent Martelli <laurent@aopsys.com>
  
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
import org.objectweb.jac.aspects.gui.Length;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.NamingConventions;
import org.objectweb.jac.util.Strings;


/**
 * HTML editor for password types (int, long, float, double, short) and String
 *
 */
public class PasswordFieldEditor  extends AbstractFieldEditor 
    implements HTMLEditor
{

    public PasswordFieldEditor(Object substance, FieldItem field) {
        super(substance,field);
        width = new Length("15ex");
    }

    // HTMLEditor interface

    public void genHTML(PrintWriter out) {
        out.println("<INPUT type=\"password\" class=\"editor\""+
                    " name=\""+label+"\""+sizeSpec());
        printAttributes(out);
        out.println(">");
        out.println("<BR>");
        out.print("<div class=\"label\">Retype " +
                  NamingConventions.textForName(field.getName()) +
                  ": </div>");
        out.println("<INPUT type=\"password\" class=\"editor\""+
                    " name=\""+label+"Confirm"+"\"");
        printAttributes(out);
        out.println(">");
    }

    protected boolean doReadValue(Object parameter) 
        throws RuntimeException 
    {
        String string = (String)parameter;
      
        if (!((String) (WebDisplay.getRequest()
                        .getParameter(label+"Confirm"))).equals(string)) {
            throw new RuntimeException ("'" +
                                        NamingConventions.textForName(field.getName()) +
                                        "'" +
                                        " and its confirmation are different");
        } else {
            if (Strings.isEmpty(string)) {
                return false;
            }
            setValue(string);
        }
        return true;
    }
}

