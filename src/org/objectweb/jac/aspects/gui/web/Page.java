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
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA */

package org.objectweb.jac.aspects.gui.web;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.objectweb.jac.aspects.gui.*;

/**
 * An HTML page containing a View and a close button.
 */
public class Page extends AbstractPage implements WindowListener
{
    static Logger loggerWeb = Logger.getLogger("gui.events");

    public Page(View view, boolean newWindow) {
        super(view,newWindow);
    }

    // HTMLViewer interface

    public void genBody(PrintWriter out) throws IOException {
        out.println("<div class=\""+type+"\">");
        if (description!=null)
            out.println("<div class=\"description\">"+description+"</div>");
        openForm(out);
        ((HTMLViewer)view).genHTML(out);
        showFormButtons(out,false);
        closeForm(out);
        out.println("</div>");
    }

    // WindowListener interface

    HttpServletResponse response;
    JacRequest jacRequest;

    public void onOK(JacRequest request) {
        logger.debug(this+".onOK");
        WebDisplay display = (WebDisplay)context.getDisplay();
        try {
            response = WebDisplay.getResponse();
            jacRequest = WebDisplay.getRequest();
            display.closeWindow(this,true);
        } finally {
            display.refresh();
        }
    }

    public void onRefresh(JacRequest request) {
        logger.debug(this+".onRefresh");
        WebDisplay display = (WebDisplay)context.getDisplay();
        response = WebDisplay.getResponse();
        jacRequest = WebDisplay.getRequest();
        WebDisplay.readValuesAndRefresh(context,request,true);
    }

    public void onCancel() {
        // Each view inside the page is supposed to be validated with
        // its own OK/Cancel buttons. Since the close link is not a
        // button in a form, editors must not try to readValue()
        // because there will never be any data to be read.
        CustomizedDisplay display = (WebDisplay)context.getDisplay();
        display.closeWindow(this,false);
        display.refresh();
    }

    public void onValidate(JacRequest request) {
        WebDisplay.readValues(context,request,true);
    }
}
