/*
  Copyright (C) 2001 Laurent Martelli <laurent@aopsys.com>
  
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

import org.objectweb.jac.core.rtti.FieldItem;
import java.io.PrintWriter;

public class PrimitiveField extends AbstractFieldView 
    implements HTMLViewer
{
    Object value;
    
    public PrimitiveField(Object value, Object substance, FieldItem field) {
        super(substance,field);
        setValue(value);
    }
    
    public void setValue(Object value) {
        this.value = value;
    }
    
    public void genHTML(PrintWriter out) {
        out.print(value!=null?value.toString():"");
    }
}
