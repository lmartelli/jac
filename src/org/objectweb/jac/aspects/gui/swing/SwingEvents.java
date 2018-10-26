/*
  Copyright (C) 2001-2002 Laurent Martelli <laurent@aopsys.com>
  
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

package org.objectweb.jac.aspects.gui.swing;

import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import org.apache.log4j.Logger;
import org.objectweb.jac.aspects.gui.DisplayContext;
import org.objectweb.jac.aspects.gui.GuiAC;
import org.objectweb.jac.aspects.gui.ResourceManager;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.core.rtti.MethodItem;

/**
 * Gather common swing events code
 */
public class SwingEvents {
    static Logger logger = Logger.getLogger("gui.events");

    /**
     * Displays a popup menu for an object
     * @param context
     * @param object the object to display a menu for
     * @param event the mouse event that triggered the popup. The popup
     * will be placed at the coordinates of this event.
     * @see #showObjectsMenu(DisplayContext,Object[],MouseEvent)
     */
    public static void showObjectMenu(DisplayContext context, Object object, 
                                      MouseEvent event) 
    {
        showObjectsMenu(context,new Object[] {object}, event);
    }

    /**
     * Displays a popup menu for some objects. The menus that would be
     * displayed for each object are concatenated.
     * @param context
     * @param objects the objects to display a menu for
     * @param event the mouse event that triggered the popup. The popup
     * will be placed at the coordinates of this event.
     * @see #showObjectMenu(DisplayContext,Object,MouseEvent) */
    public static void showObjectsMenu(DisplayContext context, Object[] objects, 
                                       MouseEvent event) 
    {
        logger.debug("showObjectsMenu for "+Arrays.asList(objects));
        ObjectPopup dynPopup = new ObjectPopup(context);
        for (int o=0; o<objects.length; o++) {
            ClassItem cli = ClassRepository.get().getClass(objects[o]);

            dynPopup.addViewItem(objects[o],"View "+cli.getShortName(),
                                 ResourceManager.getIconResource("view_icon"));

            MethodItem[] menu = GuiAC.getMenu(cli);
            logger.debug("  menu for "+objects[o]+":"+Arrays.asList(menu));
      
            if (menu != null) {
                if (menu.length>0) 
                    dynPopup.addSeparator();
                for (int i=0; i<menu.length; i++) {
                    if (menu[i] == null) {
                        dynPopup.addSeparator();
                    } else {
                        String icon = GuiAC.getIcon(menu[i]);
                        if (icon==null) {
                            icon = ResourceManager.getResource("blank_icon");
                        }
                        dynPopup.addMethodItem(objects[o],menu[i],icon);
                    }
                }
            } else {
                Collection meths = cli.getMethods();
                if (meths.size()>0) 
                    dynPopup.addSeparator();
                Iterator it = meths.iterator();
                while (it.hasNext()) {                     
                    MethodItem mi = ((MethodItem[])it.next())[0];
                    // do not show the getters
                    if (mi.isGetter() || mi.isRemover() || mi.isJacMethod()) 
                        continue;
                    String icon = GuiAC.getIcon(mi);
                    if (icon==null) {
                        icon = ResourceManager.getResource("blank_icon");
                    }
                    dynPopup.addMethodItem(objects[o],mi,icon);
                }
            }
            if (o+1<objects.length)
                dynPopup.addSeparator();
        }
        dynPopup.show(event.getComponent(), event.getX(), event.getY());
    }
}
