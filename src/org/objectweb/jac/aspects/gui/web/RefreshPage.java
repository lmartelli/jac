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
import org.objectweb.jac.aspects.gui.*;
import org.objectweb.jac.core.Collaboration;
import org.objectweb.jac.util.Semaphore;

/**
 * An HTML page containing a View and a close button.
 */
public class RefreshPage extends Page implements DialogView
{

    public RefreshPage(View view) {
        super(view,false);
    }

    // Konqueror bug workaround
    int count = 0;

    // HTMLViewer interface
    public void genBody(PrintWriter out) throws IOException {
        ((HTMLViewer)view).genHTML(out);

        out.println(
            "<script type=\"text/javascript\">"+
            "setTimeout(\"window.location.href = '"+
            getBaseURL()+"?event=onRefresh&source="+getId()+
            "&refresh="+(count++)+"';\", '3000');"+
            "</script>");
    }

    HttpServletResponse response;
    JacRequest jacRequest;
    Semaphore semaphore = new Semaphore();
    boolean waiting = false;

    public boolean waitForClose() throws TimeoutException {
        waiting = true;
        semaphore.acquire();
        // Sets the response and request so that the caller can use them
        WebDisplay.setResponse(response);
        WebDisplay.setRequest(jacRequest);
        return true;
    }

    public  void restoreContext() {}

    // WindowListener interface

    public void onRefresh(JacRequest request) {
        if (waiting) {
            response = WebDisplay.getResponse();
            jacRequest = WebDisplay.getRequest();
            ((WebDisplay)context.getDisplay()).closeWindow(this,false);
            semaphore.release();
        } else {
            super.onRefresh(request);
        }
    }

    public void onClose() {
        throw new RuntimeException("onClose is not implemented for RefreshPage");
    }
}
