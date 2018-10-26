/*
  Copyright (C) 2001 Laurent Martelli <laurent@aopsys.com>
  
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

import java.util.HashMap;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.objectweb.jac.aspects.gui.*;

public abstract class AbstractMenu extends AbstractView 
    implements MenuView, HTMLViewer, MenuListener 
{
    static Logger logger = Logger.getLogger("gui.menu");
    static Logger loggerEvents = Logger.getLogger("gui.events");

    // key -> [ callback | Menu | null ]
    HashMap map = new HashMap();
    // item order
    Vector keys = new Vector();

    public AbstractMenu(ViewFactory factory, DisplayContext context) {
        super(factory,context);
    }

    // MenuView interface

    public void addSubMenu(String label, String icon, MenuView submenu) {
        if (!map.containsKey(label)) {
            logger.debug(this+".addSubMenu "+label+" -> "+submenu);
            keys.add(label);
            map.put(label,submenu);
        }
    }

    public void addAction(String label, String icon, Callback callback) {
        if (!map.containsKey(label)) {
            logger.debug(this+".addAction "+label+" -> "+callback);
            keys.add(label);
            map.put(label,new MenuItem(label,icon,callback));
        }
    }

    public void addSeparator() {
    }
   
    String position = org.objectweb.jac.aspects.gui.Menu.LEFT;
   
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
        if (position==null)
            position = org.objectweb.jac.aspects.gui.Menu.LEFT;
    }

    // MenuListener interface

    public void onMenuClick(String key) {
        try {
            loggerEvents.debug(this+".onMenuClick `"+key+"'");
            MenuItem item = (MenuItem)map.get(key);
            if (item!=null && item.callback!=null)
                EventHandler.get().onInvoke(
                    context,
                    new InvokeEvent(this,
                                    item.callback.getObject(),
                                    item.callback.getMethod(),
                                    item.callback.getParameters()),
                    true,
                    null,null);
            else {
                loggerEvents.debug("  No item("+item+") or callback("+
                          (item==null?"":""+item.callback)+") is null");
                context.getDisplay().refresh();
            }
        } catch (Exception e) {
            context.getDisplay().showError("Menu error","onMenuClick "+key+": "+
                                           e.toString()+"<br><pre>"+map+"</pre>");
            logger.error("onMenuClick "+key,e);
        }
    }
}
