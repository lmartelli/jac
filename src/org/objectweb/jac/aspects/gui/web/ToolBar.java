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

import org.objectweb.jac.aspects.gui.*;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

public class ToolBar extends AbstractView implements MenuView, HTMLViewer, MenuListener {

    // key -> [ callback | Menu | null ]
    HashMap map = new HashMap();
    // item order
    Vector keys = new Vector();

    public ToolBar(ViewFactory factory, DisplayContext context) {
        super(factory,context);
    }

    // MenuView interface

    public void addSubMenu(String label, String icon, MenuView submenu) {
    }

    public void addAction(String label, String icon, Callback callback) {
        if (!map.containsKey(label)) {
            keys.add(label);
            map.put(label,new MenuItem(label,icon,callback));
        }
    }

    public void addSeparator() {
    }

    String position;
   
    /**
     * Get the value of position.
     * @return value of position.
     */
    public String getPosition() {
        return position;
    }
   
    /**
     * Set the value of position.
     * @param v  Value to assign to position.
     */
    public void setPosition(String  v) {
        this.position = v;
    }
   
    // HTMLViewer interface

    public void genHTML(PrintWriter out) {
        out.println("<div class=\"toolBar\">");
        Iterator i = keys.iterator();
        while (i.hasNext()) {
            String key = (String)i.next();
            Object item = map.get(key);
            if (item instanceof MenuItem) {
                out.print("<a href=\""+eventURL("onMenuClick")+
                          "&amp;item="+key+"\">");
                if (((MenuItem)item).icon!=null) 
                    out.print(iconElement(((MenuItem)item).icon,""));
                else
                    out.println(key);
                out.println("</a>");
            }
        }
        out.println("</div>");
    }

    // MenuListener interface

    public void onMenuClick(String key) {
        try {
            MenuItem item = (MenuItem)map.get(key);
            if (item.callback!=null)
                EventHandler.get().onInvoke(
                    context,
                    new InvokeEvent(this,null,item.callback.getMethod()));
            else 
                context.getDisplay().refresh();
        } catch (Exception e) {
            context.getDisplay().showError("Menu error",e.toString());
        }
    }
}
