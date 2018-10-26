/*
  Copyright (C) 2003 Renaud Pawlak
  
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

import org.objectweb.jac.aspects.gui.GuiAC;
import org.objectweb.jac.util.Matrix;
import org.objectweb.jac.core.rtti.FieldItem;
import java.io.PrintWriter;

public class MatrixView extends AbstractFieldView implements HTMLViewer {
    Matrix matrix;

    public MatrixView(Object value, Object substance, FieldItem field) {
        super(substance, field);
        setValue(value);
    }

    public void setValue(Object value) {
        this.matrix = (Matrix) value;
    }

    public void genHTML(PrintWriter out) {
        out.println("<table class=\"matrix\">");
        for (int j=0; j<matrix.getRowCount(); j++) {
            out.println("  <tr>");
            for (int i=0; i<matrix.getColumnCount(); i++) {
                out.println(
                    "    <td>"
                    + GuiAC.toString(matrix.get(i, j))
                    + "</td>");
            }
            out.println("  </tr>");
        }
        out.println("</table>");
    }
}
