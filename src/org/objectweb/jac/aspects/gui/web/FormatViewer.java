/*
  Copyright (C) 2003 Laurent Martelli <laurent@aopsys.com>
  
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

import java.io.PrintWriter;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.aspects.gui.Format;

public abstract class FormatViewer extends AbstractFieldView 
    implements HTMLViewer
{
    Object value;

    /** Stores the default format of the float */
    protected Format format;

    public FormatViewer(Object value, Object substance, FieldItem field) {
        super(substance,field);
        initFormat(field);
        setValue(value);
    }

    public FormatViewer() {
        isCellViewer = true;
    }

    protected abstract void initFormat(FieldItem field);

    public void setValue(Object value) {
        this.value = value;
    }

    public void setField(FieldItem field) {
        super.setField(field);
        initFormat(field);
    }

    protected String alignment = null;

    public void genHTML(PrintWriter out) {
        if (value!=null) {
            if (isCellViewer && alignment!=null) 
                out.print("<div style=\"text-align: "+alignment+"\">");
            out.print(format.format(value));
            if (isCellViewer && alignment!=null) 
                out.print("</div>");         
        }
    }
}
