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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import org.objectweb.jac.aspects.gui.EventHandler;
import org.objectweb.jac.aspects.gui.FieldEditor;
import org.objectweb.jac.aspects.gui.InvokeEvent;
import org.objectweb.jac.aspects.gui.MethodView;
import org.objectweb.jac.core.rtti.AbstractMethodItem;
import org.objectweb.jac.core.rtti.MethodItem;

public class EmbeddedMethod extends AbstractCompositeView
    implements MethodView, MethodListener
{
    Object substance;
    AbstractMethodItem method;
    String icon;
    EditorContainer parameters;

    public EmbeddedMethod(Object substance, AbstractMethodItem method, 
                          EditorContainer parameters) {
        this.substance = substance;
        this.method = method;
        this.parameters = parameters;
    }

    // MethodView interface

    public void setMethod(AbstractMethodItem method) {
        this.method = method;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    boolean onlyIcon = false;
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

    public void genHTML(PrintWriter out) throws IOException {
        JacRequest request = WebDisplay.getRequest();
        parameters.genHTML(out);
        if (request.isIEUserAgent()) {
            out.print("<table class=\"method\"><tr><td>"+
                      iconElement(icon,label,false)+
                      eventURL(getText(),"onInvoke","")+
                      "</td></tr></table>");
        } else {
            out.print("<span class=\"method\">"+
                      iconElement(icon,label,false)+
                      eventURL(getText(),"onInvoke","")+
                      "</span>");
        }
    }

    // MethodListener interface

    public void onInvoke() {
        List editors = parameters.getEditors();
        Object[] params = new Object[editors.size()];
        JacRequest request = WebDisplay.getRequest();
        for (int i=0; i<params.length; i++) {
            FieldEditor editor = (FieldEditor)editors.get(i);
            ((HTMLEditor)editor).readValue(request.getParameter(editor.getLabel()));
            params[i] = editor.getValue();
        }
        EventHandler.get().onInvoke(
            context,
            new InvokeEvent(this,substance,method,params),
            false,null,null);
    }
}
