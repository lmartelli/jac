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
import org.apache.log4j.Logger;
import org.objectweb.jac.aspects.gui.*;
import org.objectweb.jac.util.Strings;

public class Container extends AbstractCompositeView 
    implements HTMLViewer 
{
    static Logger logger = Logger.getLogger("web.html");

    int layout;

    public Container(int layout) {
        super();
        this.layout = layout;
    }

    public void genHTML(PrintWriter out) throws IOException {
        genDescription(out);
        genMessage(out);
        //String myStyle = ((View) component).getStyle();
        out.println("<div class=\""+type+(!Strings.isEmpty(style)?(" "+style):"")+"\">");
        genItemsHTML(out);
        out.println("</div>");
    }

    /**
     * Generates HTML code for contained items
     * @param out where to write the HTML
     */
    protected void genItemsHTML(PrintWriter out) {
        Iterator i = components.iterator();
        while (i.hasNext()) {
            HTMLViewer component = (HTMLViewer)i.next();
            if (layout==Constants.VERTICAL) {
                out.println("<div>");
            }
            try {
                String openBoder = ((AbstractView)component).getOpenBorder();
                if (!Strings.isEmpty(openBoder))
                    out.println(openBoder);
                component.genHTML(out);
                String closeBoder = ((AbstractView)component).getCloseBorder();
                if (!Strings.isEmpty(closeBoder))
                    out.println(closeBoder);
            } catch(Exception e) {
                logger.error("Container.genHTML(): genHTML of component "+
                             component+" raised an exception",e);
            }
            if (layout==Constants.VERTICAL) 
                out.println("</div>");
        }
    }
}
