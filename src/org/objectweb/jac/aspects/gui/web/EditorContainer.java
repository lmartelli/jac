/*
  Copyright (C) 2001-2003 Laurent Martelli <laurent@aopsys.com>
  
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
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.servlet.http.HttpServletResponse;
import org.objectweb.jac.aspects.gui.Constants;
import org.objectweb.jac.aspects.gui.FieldEditor;
import org.objectweb.jac.aspects.gui.GuiAC;

public class EditorContainer extends Container
    implements org.objectweb.jac.aspects.gui.EditorContainer, DialogListener
{
    Vector editors = new Vector();
    boolean showButtons;

    /**
     * @param showButtons wether to show an OK and a Cancel button
     */
    public EditorContainer(boolean showButtons) {
        super(Constants.VERTICAL);
        this.showButtons = showButtons;
    }

    public void addEditor(Object editor) {
        editors.add(editor);
    }
   
    public void removeEditor(Object editor) {
        editors.remove(editor);
    }
   
    public List getEditors() {
        return (List)editors.clone();
    }

    public boolean hasEnabledEditor() {
        Iterator it = editors.iterator();
        while (it.hasNext()) {
            Object view = it.next();
            if (view instanceof FieldEditor && 
                ((FieldEditor)view).isEnabled()) {
                return true;
            }
        }
        return false;
    }

    public void setShowButtons(boolean value) {
        this.showButtons = value;
    }
    public boolean showButtons() {
        return showButtons;
    }

    public void genHTML(PrintWriter out) throws IOException {
        out.println("<div class=\""+type+"\">");
        if (!editors.isEmpty() && showButtons) {
            out.println("<form action=\""+
                        ((WebDisplay)context.getDisplay()).getServletName()+"\""+
                        " accept-charset=\"utf-8\">");
        }
        genItemsHTML(out);
        if (!editors.isEmpty() && showButtons) {
            out.println("  <div class=\"actions\">");
            out.println("    <input type=\"hidden\" name=\"source\" value=\""+getId()+"\">");
            out.println("    <button class=button type=submit name=event "+
                        "value=\"onOK\">OK</button>");
            out.println("    <button class=\"button\" type=\"submit\" name=\"event\" "+
                        "value=\"onCancel\">"+GuiAC.getLabelCancel()+"</button>");
            out.println("  </div>");
            out.println("</form>");
        }
        out.println("</div>");
    }

    // DialogListener interface

    public void onOK(JacRequest request) {
        onValidate(request);
        ((WebDisplay)context.getDisplay()).refresh();
    }

    public void onRefresh(JacRequest request) {
        onValidate(request);
        ((WebDisplay)context.getDisplay()).refresh();
    }

    public void onValidate(JacRequest request){
        Iterator i = editors.iterator();
        while (i.hasNext()) {
            FieldEditor editor = (FieldEditor)i.next();
            ((HTMLEditor)editor).readValue(request.getParameter(editor.getLabel()));
            ((HTMLEditor)editor).commit();
        }
    }

    public void onCancel() {
        ((WebDisplay)context.getDisplay()).refresh();
    }

    public void restoreContext() {}

    public HttpServletResponse getResponse() {
        return null;
    }

    public JacRequest getRequest() {
        return null;
    }

}
