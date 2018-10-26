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
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.objectweb.jac.aspects.gui.*;
import org.objectweb.jac.core.Collaboration;
import org.objectweb.jac.util.Semaphore;

/**
 * An HTML page containing a View, and an OK and a close Button
 */
public class Dialog extends AbstractPage 
    implements DialogView, DialogListener
{
    static Logger loggerTimeout = Logger.getLogger("gui.timeout");
    static Logger loggerEvents = Logger.getLogger("gui.events");

    boolean ok = false;
    Semaphore semaphore = new Semaphore();
    String description;
    HttpServletResponse response;
    JacRequest jacRequest;
    /** Stores context attributes at creation time */
    Map attributes;

    /**
     * @param view the view to embed in the dialog
     * @param parent the parent window of the dialog
     * @param title the title
     * @param description description of the view
     */
    public Dialog(ViewFactory factory, DisplayContext context,
                  View view, Object parent,
                  String title, String description) 
    {
        super(factory,context,view,false);
        this.description = description;
        attributes = Collaboration.get().getAttributes();   
        attributes.remove(WebDisplay.REQUEST);
        attributes.remove(WebDisplay.RESPONSE);
        //System.out.println("Stored attributes: "+attributes);
        /*
        if (view instanceof org.objectweb.jac.aspects.gui.EditorContainer) {
            Iterator i = 
                ((org.objectweb.jac.aspects.gui.EditorContainer)view).getEditors().iterator();
        */
            Iterator i = context.getEditors().iterator();
            while (i.hasNext()) {
                Object editor = i.next();
                if (editor instanceof HTMLEditor) {
                    ((HTMLEditor)editor).setAttribute(
                        "onkeypress",
                        "return commitFormOnEnter(event,this,'event=onRefresh&amp;source="+getId()+"')\"");
                }
            }            
            //        }
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public JacRequest getRequest() {
        return jacRequest;
    }

    // DialogView interface

    public boolean waitForClose() throws TimeoutException {
        loggerTimeout.debug("waiting for "+this+" to be closed "+
                  GuiAC.dialogTimeout+"ms");
        if (!semaphore.acquire(GuiAC.dialogTimeout)) {
            loggerTimeout.debug("Dialog timedout: "+this);
            throw new TimeoutException(this);
        }
        loggerEvents.debug("closed "+this+" -> "+ok);
        return ok;
    }

    public View getContentView() {
        return view;
    }

    // HTMLViewer interface

    public void genHTML(PrintWriter out) throws IOException {
        Collaboration c = Collaboration.get();
        // pressing "enter" in an editor should call "refresh"
        c.addAttribute(WebDisplay.ON_ENTER_ACTION, "event=onRefresh");
        try {
            super.genHTML(out);
        } finally {
            c.removeAttribute(WebDisplay.ON_ENTER_ACTION);
        }
    }

    public void genBody(PrintWriter out) throws IOException {
        out.println("<div class=\""+type+"\">");
        if (description!=null)
            out.println("<div class=\"description\">"+description+"</div>");
        openForm(out);
        ((HTMLViewer)view).genHTML(out);
        showFormButtons(out);
        closeForm(out);
        out.println("</div>");
    }

    // DialogListener interface

    public void restoreContext() {
        loggerEvents.debug("Restoring attributes: "+attributes.keySet());
        Collaboration.get().setAttributes(attributes);
    }

    public void onOK(JacRequest request) {
        loggerEvents.debug(this+".onOK");
        restoreContext();
        WebDisplay display = (WebDisplay)context.getDisplay();
        response = WebDisplay.getResponse();
        jacRequest = WebDisplay.getRequest();
        display.closeWindow(this,true);
        ok = true;
        semaphore.release();
    }

    public void onRefresh(JacRequest request) {
        loggerEvents.debug(this+".onRefresh");
        WebDisplay display = (WebDisplay)context.getDisplay();
        restoreContext();
        response = WebDisplay.getResponse();
        jacRequest = WebDisplay.getRequest();
        WebDisplay.readValuesAndRefresh(context,request,true);
    }

    public void onValidate(JacRequest request) {
        restoreContext();
        WebDisplay.readValues(context,request,true);
    }

    public void onCancel() {
        loggerEvents.debug(this+".onCancel");
        ok = false;
        Collaboration collab = Collaboration.get();
        try {
            WebDisplay display = (WebDisplay)context.getDisplay();
            response = WebDisplay.getResponse();
            jacRequest = WebDisplay.getRequest();
            //collab.addAttribute(GuiAC.NO_COMMIT,Boolean.TRUE);
            display.closeWindow(this,false);
        } finally {
            //collab.addAttribute(GuiAC.NO_COMMIT,null);
            semaphore.release();
        }
    }
}
