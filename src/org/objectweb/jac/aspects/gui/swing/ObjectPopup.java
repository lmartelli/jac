/*
  Copyright (C) 2002-2003 Renaud Pawlak <renaud@aopsys.com>, 
                          Laurent Martelli <laurent@aopsys.com>
  
  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 2 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  USA */

package org.objectweb.jac.aspects.gui.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.apache.log4j.Logger;
import org.objectweb.jac.aspects.gui.DisplayContext;
import org.objectweb.jac.aspects.gui.EventHandler;
import org.objectweb.jac.aspects.gui.GuiAC;
import org.objectweb.jac.aspects.gui.InvokeEvent;
import org.objectweb.jac.aspects.gui.ResourceManager;
import org.objectweb.jac.core.rtti.MethodItem;
import org.objectweb.jac.core.rtti.NamingConventions;

public class ObjectPopup extends JPopupMenu {
    static Logger loggerEvents = Logger.getLogger("gui.events");

    DisplayContext context;

    public ObjectPopup(DisplayContext context) {
        this.context = context;
    }

    public void addMethodItem(Object substance, MethodItem method, String icon) {
        MenuItem item = new MenuItem(substance,method,ResourceManager.getIcon(icon));
        item.addActionListener(item);
        String label = item.getText();
        if (method.isSetter()) 
            label = method.getSetField().getName();
        else if (method.isAdder())
            label = method.getAddedCollection().getName();
        item.setMnemonic(
            MenuBar.getMnemonic(
                this,
                GuiAC.getMnemonics(method)+label));
        add(item);
        boolean enabled = GuiAC.isEnabled(method,substance);
        if (!enabled) {
            item.setEnabled(enabled);
        }
    }

    public void addViewItem(Object substance, String label, ImageIcon icon) {
        MenuItem item = new MenuItem(substance,label,icon);
        item.setActionCommand("view");
        item.addActionListener(item);
        add(item);
    }

    class MenuItem extends JMenuItem implements ActionListener {
        MethodItem method;
        Object substance;
        boolean toggle;
        public MenuItem(Object substance, MethodItem method, ImageIcon icon) {
            super(method.isSetter() && method.getSetField().getType()==boolean.class 
                  ? "Toggle "+NamingConventions.textForName(method.getSetField().getName())
                  : NamingConventions.textForName(method.getName()),
                  icon);
            this.toggle = method.isSetter() && method.getSetField().getType()==boolean.class;
            this.method = method;
            this.substance = substance;
        }
        public MenuItem(Object substance, String label, ImageIcon icon) {
            super(label,icon);
            this.method = null;
            this.substance = substance;
        }
        public void actionPerformed(ActionEvent event) {
            loggerEvents.debug("ObjectPopup.actionPerformed from "+
                               event.getSource().getClass().getName());
            if ("view".equals(event.getActionCommand())) {
                EventHandler.get().onView(context,null,substance,null,null);
            } else if (toggle) {
                if (method!=null)
                    EventHandler.get().onInvoke(
                        context, 
                        new InvokeEvent(
                            null,
                            substance, 
                            method, 
                            new Object[] {((Boolean)method.getSetField().getThroughAccessor(substance)).booleanValue() 
                                          ? Boolean.FALSE : Boolean.TRUE }), 
                        false,
                        null, null);
            } else {
                if (method!=null)
                    EventHandler.get().onInvoke(
                        context, 
                        new InvokeEvent(null, substance, method));
            }
        }
    }
}
