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
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA */

package org.objectweb.jac.aspects.gui.swing;

import org.objectweb.jac.aspects.gui.Callback;
import org.objectweb.jac.aspects.gui.GuiAC;
import org.objectweb.jac.aspects.gui.MenuView;
import org.objectweb.jac.aspects.gui.MethodUpdate;
import org.objectweb.jac.aspects.gui.ResourceManager;
import org.objectweb.jac.aspects.gui.Utils;
import org.objectweb.jac.aspects.session.SessionAC;
import org.objectweb.jac.core.Collaboration;
import org.objectweb.jac.core.rtti.MethodItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.Iterator;
import javax.swing.JButton;
import javax.swing.JToolBar;

public class ToolBar extends AbstractCompositeView 
    implements MenuView, ActionListener, MethodUpdate
{
   
    JToolBar toolbar;

    // action command -> AbstractMethodItem
    Hashtable actions = new Hashtable();
   
    // actoin command -> JButton
    Hashtable buttons = new Hashtable();
   
    public ToolBar() {
        toolbar = new JToolBar();
        add(toolbar);
    }

    // MenuView interface

    public void addSubMenu(String label, String icon, MenuView submenu) {
        // do nothing
    }

    public void addAction(String label, String icon,
                          Callback callback) {
        JButton button = icon!=null? 
            new JButton(ResourceManager.getIcon(icon)) : 
            new JButton(label);
        String actionCommand = callback.toString();
        button.setActionCommand(actionCommand);
        button.addActionListener(this);
        button.setMnemonic(
            MenuBar.getMnemonic(
                toolbar,
                GuiAC.getMnemonics(callback.getMethod())+label));
        button.setToolTipText(GuiAC.getLabel(callback.getMethod()));
        actions.put(actionCommand,callback);
        buttons.put(actionCommand,button);
        updateEnabled(button,callback);
        toolbar.add(button);
        MethodItem condition = (MethodItem)callback.getMethod().getAttribute(GuiAC.CONDITION);
        if (condition!=null)
            Utils.registerMethod(callback.getObject(),condition,this,actionCommand);
    }
   

    public void addSeparator() {
        toolbar.addSeparator();
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

    public void close(boolean validate) {
        super.close(validate);
        // Unregister from all events
        Iterator it = actions.keySet().iterator();
        while (it.hasNext()) {
            Callback callback = (Callback)it.next();
            MethodItem condition = (MethodItem)callback.getMethod().getAttribute(GuiAC.CONDITION);
            if (condition!=null)
                Utils.unregisterMethod(null,condition,this);
        }
    }
   
    public String toString() {
        return getClass().getName()+"@"+Integer.toString(hashCode());
    }

    // ActionListener interface
   
    public void actionPerformed(ActionEvent event) {
        Collaboration.get().addAttribute(
            SessionAC.SESSION_ID, GuiAC.getLocalSessionID());
        Callback callback = (Callback)actions.get(event.getActionCommand());
        callback.invoke(context,this);
    }

    protected void updateEnabled(JButton button, Callback callback) {
        button.setEnabled(GuiAC.isEnabled(callback.getMethod(), callback.getObject()));      
    }

    // MethodUpdate interface
    public void methodUpdated(Object substance, MethodItem method, Object param) {
        updateEnabled((JButton)buttons.get((String)param),
                      (Callback)actions.get((String)param));
    }
}
