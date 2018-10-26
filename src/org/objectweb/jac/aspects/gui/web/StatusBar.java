/*
  Copyright (C) 2002-2003 Renaud Pawlak <renaud@aopsys.com>
  
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
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA */

package org.objectweb.jac.aspects.gui.web;

import java.io.PrintWriter;
import org.objectweb.jac.aspects.gui.*;
import org.objectweb.jac.core.rtti.MethodItem;
import org.objectweb.jac.util.ExtArrays;

public class StatusBar extends AbstractView implements StatusView, HTMLViewer {

    MethodItem method = null;

    public StatusBar(ViewFactory factory, DisplayContext context,MethodItem method) {
        super(factory,context);
        this.method = method;
    }

    // StatusView interface

    String position;
   
    /**
     * Get the value of position.
     * @return value of position.
     */
    public String getPosition() {
        return position;
    }
   
    /**
     * Set the value of position.
     * @param v  Value to assign to position.
     */
    public void setPosition(String  v) {
        this.position = v;
    }

    public void showMessage(String message) {
    }

    // HTMLViewer interface

    public void genHTML(PrintWriter out) {
        if (method!=null) {
            out.println("<div class=\"statusBar\">");
            out.println(method.invoke(null,ExtArrays.emptyObjectArray));
            out.println("</div>");
        }
    }

}
