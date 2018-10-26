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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import javax.servlet.http.HttpServletResponse;
import org.objectweb.jac.aspects.gui.Constants;
import org.objectweb.jac.aspects.gui.DialogView;
import org.objectweb.jac.aspects.gui.FieldEditor;
import org.objectweb.jac.aspects.gui.GuiAC;
import org.objectweb.jac.aspects.gui.View;

public class ObjectView extends Container implements DialogListener
{
    public ObjectView() {
        super(Constants.VERTICAL);
    }

    public void removeAllViews(boolean validate) {
        Iterator it = components.iterator();
        while (it.hasNext()) {
            Object component = it.next();
            if (component instanceof FieldEditor) {
                context.removeEditor((FieldEditor)component);
            }
        }
        super.removeAllViews(validate);
    }

    public void removeView(View component, boolean validate)
    {
        super.removeView(component,validate);
        if (component instanceof FieldEditor) {
            context.removeEditor((FieldEditor)component);
        }
    }

    public void genHTML(PrintWriter out) throws IOException {
        // This test is bit hackish, we should probably always have a
        // form in the page
        boolean showButtons = context.showButtons() && !(parentView instanceof DialogView);
        if (showButtons)
            out.println("<form action=\""+
                        ((WebDisplay)context.getDisplay()).getServletName()+"\" "+
                        "method=\"post\" accept-charset=\""+GuiAC.getEncoding()+"\" "+
                        "enctype=\"multipart/form-data\">");

        super.genHTML(out);
        if (showButtons)
            out.println("    <input type=\"hidden\" name=\"source\" value=\""+getId()+"\">");
        if (context.hasEnabledEditor() && showButtons) {
            out.println("  <div class=\"actions\">");
            out.println("    <input type=\"hidden\" name=\"event\" value=\"onValidate\">");
            out.println("    <input class=\"button\" type=\"submit\" name=\"onOK\" value=\""+GuiAC.getLabelOK()+"\">");
            out.println("    <input class=\"button\" type=\"submit\" name=\"onCancel\" value=\""+GuiAC.getLabelCancel()+"\">");
            /* // does not work with IE ...
              out.println("    <button class=\"button\" type=\"submit\" name=\"event\" "+
              "value=onOK>"+GuiAC.getLabelOK()+"</button>");
              out.println("    <button class=\"button\" type=\"submit\" name=\"event\" "+
              "value=onCancel>"+GuiAC.getLabelCancel()+"</button>");
            */
            out.println("  </div>");
        }
        if (showButtons)
            out.println("</form>");
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

    public void onValidate(JacRequest request) {
        try {
            WebDisplay.readValues(context,request,true);
            setDescription(null);
        } catch (Exception e) {
            setDescription(e.getMessage());
            logger.error(this+".onValidate",e);
        }
    }

    public void onCancel() {
        ((WebDisplay)context.getDisplay()).refresh();
    }

    public void restoreContext() {
    }

    public HttpServletResponse getResponse() {
        return null;
    }

    public JacRequest getRequest() {
        return null;
    }
}
