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
import org.objectweb.jac.aspects.gui.*;
import org.objectweb.jac.aspects.gui.web.html.Element;
import org.objectweb.jac.aspects.gui.InvokeEvent;
import org.objectweb.jac.aspects.gui.MethodView;
import org.objectweb.jac.core.rtti.AbstractMethodItem;
import org.objectweb.jac.core.rtti.MethodItem;

public class Method extends AbstractView 
    implements MethodView, HTMLViewer, MethodListener
{
    protected Object substance;
    protected AbstractMethodItem method;
    protected String icon;
    protected boolean onlyIcon = false; // If true, only show the icon

    public Method(Object substance, AbstractMethodItem method) {
        this.substance = substance;
        this.method = method;
    }

    // MethodView interface

    public void setMethod(AbstractMethodItem method) {
        this.method = method;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setOnlyIcon(boolean onlyIcon) {
        this.onlyIcon = onlyIcon;
    }

    /**
     * Returns the text of the button
     */
    protected String getText() {
        if (method instanceof MethodItem && 
            ((MethodItem)method).isSetter() && icon!=null)
            return "";
        else
            return label;
    }

    // HTMLViewer interface

    public void genHTML(PrintWriter out) {
        JacRequest request = WebDisplay.getRequest();
        Element iconElt = iconElement(icon,label,false).addCssClass("first");
        if (onlyIcon) {
            iconElt.addCssClass("last");
        }
        String button = 
            eventURL(iconElt+(onlyIcon?"":getText()),
                     "onInvoke","").toString();
        if (request.isIEUserAgent()) {
            out.print("<table class=\"method\"><tr><td>"+
                      button+"</td></tr></table>");
        } else {
            out.print(button);
        }
    }

    // MethodListener interface

    public void onInvoke() {
        EventHandler.get().onInvoke(
            context,
            new InvokeEvent(this,substance,method));
    }

    public String toString() {
        return super.toString()+":"+method.getLongName();
    }
    
}

