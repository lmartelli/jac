/*
  Copyright (C) 2001 Renaud Pawlak, Laurent Martelli
  
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

package org.objectweb.jac.aspects.gui.web;


import org.objectweb.jac.aspects.gui.*;
import org.objectweb.jac.core.rtti.FieldItem;
import java.io.PrintWriter;

/**
 * This is a special value editor that allows the user to nicely edit
 * an URL. */

public class URLEditor extends AbstractFieldEditor
    implements FieldEditor, HTMLEditor
{
    /**
     * Constructs a new URL editor.
    */

    public URLEditor(Object substance, FieldItem field) {
        super(substance,field);
    }

    // HTMLEditor interface

    public void genHTML(PrintWriter out) {
        out.println("<input type=text size=30 name=\""+label+"\" "+
                    "value=\""+(value!=null?value.toString():"")+"\"");
        printAttributes(out);
        out.println(">");
    }

    protected boolean doReadValue(Object parameter) {
        setValue(Utils.stringToURL((String)parameter));
        return true;
    }
}
