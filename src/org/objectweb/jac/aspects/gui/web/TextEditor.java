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
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.aspects.gui.Unit;

/**
 * HTML editor for multiline texts.
 */
public class TextEditor  extends AbstractFieldEditor 
    implements HTMLEditor
{
    public TextEditor(Object substance, FieldItem field) {
        super(substance,field);
    }

    // HTMLEditor interface

    public void genHTML(PrintWriter out) {
        String sizeSpec = "";
        if (height!=null && height.unit==Unit.EM)
            sizeSpec += " rows=\""+(int)height.value+"\"";
        if (width!=null && width.unit==Unit.EX)
            sizeSpec += " cols=\""+(int)width.value+"\"";
        String style = "";
        if (height!=null && height.unit!=Unit.EM)
            style += "height:"+height+";";
        if (width!=null && width.unit!=Unit.EX)
            style += "width:"+width+";";

        out.print("<TEXTAREA"+sizeSpec+(style.length()!=0 ? " style=\""+style+"\"" : "")+
                  " class=\"editor\""+
                  " name=\""+label+"\"");
        printAttributes(out);
        out.println(">");
        out.print((value==null ? "" : value.toString()) +"</TEXTAREA>");
    }

    protected boolean doReadValue(Object parameter) {
        setValue((String)parameter);
        return true;
    }
}

