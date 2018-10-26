/*
  Copyright (C) 2002-2003 Laurent Martelli <laurent@aopsys.com>
  
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
import org.apache.log4j.Logger;
import org.objectweb.jac.aspects.gui.GuiAC;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.util.Enum;
import org.objectweb.jac.util.InvalidIndexException;

public class EnumViewer extends AbstractFieldView 
    implements HTMLViewer
{
    static Logger loggerContext = Logger.getLogger("gui");

    Object value;
    Enum enum;

    public EnumViewer(Object value, 
                      Object substance, FieldItem field) {
        super(substance,field);
        if (field!=null)
            this.enum = (Enum)field.getAttribute(GuiAC.FIELD_ENUM);
        else
            logger.warn("EnumViewer: Cannot determine enum because field is null");
        setValue(value);
    }

    public EnumViewer() {
    }

    public void setField(FieldItem field) {
        super.setField(field);
        if (field!=null)
            this.enum = (Enum)field.getAttribute(GuiAC.FIELD_ENUM);
        else
            logger.warn("EnumViewer: Cannot determine enum because field is null");        
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void genHTML(PrintWriter out) {
        if (value!=null) {
            try {
                out.print(enum.int2string(((Integer)value).intValue()));
            } catch(InvalidIndexException e) {
                out.print(((Integer)value).intValue());
            }
        }
    }
}

