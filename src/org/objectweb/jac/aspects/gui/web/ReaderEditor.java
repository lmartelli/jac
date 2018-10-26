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
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA */

package org.objectweb.jac.aspects.gui.web;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.lib.Attachment;
import org.objectweb.jac.util.Streams;

public class ReaderEditor extends AbstractFieldEditor
    implements HTMLEditor
{
    public ReaderEditor(Object substance, FieldItem field) {
        super(substance,field);
    }

    // HTMLEditor interface

    protected boolean doReadValue(Object parameter) {
        RequestPart part = (RequestPart)parameter;
        if(part.getFilename()==null || part.getFilename().equals("")) {
            return false;
        }
        setValue(new InputStreamReader(part.getData()));
        return true;
    }
   
    public void genHTML(PrintWriter out) {
        out.print("<input name=\""+label+"\" type=\"file\">");
    }
}
