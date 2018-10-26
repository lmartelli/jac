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
import org.objectweb.jac.aspects.gui.*;
import org.objectweb.jac.aspects.gui.Menu;
import org.objectweb.jac.util.ExtArrays;

public class MenuBar extends AbstractMenu implements MenuView, HTMLViewer {

    public MenuBar(ViewFactory factory, DisplayContext context) {
        super(factory,context);
    }

    // HTMLViewer interface
    public void genHTML(PrintWriter out) throws IOException {
        if (message!=null) {
            String msg = (String)message.invoke(null,ExtArrays.emptyObjectArray);
            out.println("<div class=\"message\">"+msg+"</div>");
        }
        if (position.equals(Menu.TOP)) { 
            out.println("<div class=\"menuBarT\">");
        } else if (position.equals(Menu.BOTTOM)) {
            out.println("<div class=\"menuBarB\">");         
        } else if (position.equals(Menu.LEFT)) {
            out.println("<div class=\"menuBarL\">");
        } else if (position.equals(Menu.RIGHT)) {
            out.println("<div class=\"menuBarR\">");         
        }
        Iterator i = keys.iterator();
        while (i.hasNext()) {
            String key = (String)i.next();
            Object item = map.get(key);
            if (item instanceof MenuView) {
                out.println(key);
                ((MenuView)item).setPosition(position);
                ((HTMLViewer)item).genHTML(out);
            } else {
                out.print("<div><a href=\""+eventURL("onMenuClick")+
                          "&amp;item="+key+"\">");
                if (((MenuItem)item).icon!=null) 
                    out.print(iconElement(((MenuItem)item).icon,""));
                out.println(key+"</a></div>");
            }
        }
        out.println("</div>");
    }
}
