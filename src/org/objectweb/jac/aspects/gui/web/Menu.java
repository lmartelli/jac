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
import java.net.URLEncoder;
import java.util.Iterator;
import org.objectweb.jac.aspects.gui.*;

public class Menu extends AbstractMenu
    implements MenuView, HTMLViewer, MenuListener 
{

    public Menu(ViewFactory factory, DisplayContext context) {
        super(factory,context);
        logger.debug("new Menu "+this);
    }
   
    // HTMLViewer interface
    public void genHTML(PrintWriter out) throws IOException {
        if (position.equals(org.objectweb.jac.aspects.gui.Menu.TOP)||
            position.equals(org.objectweb.jac.aspects.gui.Menu.BOTTOM)) {
            out.println("<div class=\"menuH\">");
        } else if (position.equals(org.objectweb.jac.aspects.gui.Menu.LEFT)||
                   position.equals(org.objectweb.jac.aspects.gui.Menu.RIGHT)) {
            out.println("<div class=\"menuV\">");
        }
        Iterator i = keys.iterator();
        while (i.hasNext()) {
            String key = (String)i.next();
            Object item = map.get(key);
            if (item instanceof Menu) {
                out.println(key);
                ((HTMLViewer)item).genHTML(out);
            } else if (item instanceof MenuItem) {
                out.print("<div><a href=\""+eventURL("onMenuClick")+
                          "&amp;item="+URLEncoder.encode(key,GuiAC.getEncoding())+"\">");
                if (((MenuItem)item).icon!=null) 
                    out.print(iconElement(((MenuItem)item).icon,""));
                out.println(key+"</a></div>");
            }
        }
        out.println("</div>");
    }
}
