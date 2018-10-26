/*
  Copyright (C) 2002 Laurent Martelli <laurent@aopsys.com>
  
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
import org.apache.log4j.Logger;
import org.objectweb.jac.aspects.gui.*;

/**
 * An HTML page containing a View
 */
public abstract class AbstractPage extends AbstractView 
    implements WindowView, HTMLViewer
{
    static Logger logger = Logger.getLogger("web");

    /* the view embedded in the page */
    protected View view;
    /* tells if the page is in a new window (popup) */
    boolean newWindow = false;

    public AbstractPage(ViewFactory factory, DisplayContext context,
                        View view, boolean newWindow) 
    {
        super(factory,context);
        view.setParentView(this);
        this.view = view;
        this.newWindow = newWindow;
    }

    public AbstractPage(View view, boolean newWindow) {
        super();
        view.setParentView(this);
        this.view = view;
        this.newWindow = newWindow;
    }

    public void close(boolean validate) {
        if (!newWindow) {
            logger.debug("closing "+this);
            super.close(validate);
            view.close(validate);
        } else {
            logger.debug("NOT closing "+this);
        }
    }

    // WindowView interface
    public View getContentView() {
        return view;
    }

    // HTMLViewer interface

    public void genHTML(PrintWriter out) throws IOException {
        genPage(out);
    }

}
