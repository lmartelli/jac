/*
  Copyright (C) 2001-2003 Renaud Pawlak <renaud@aopsys.com>, 
                          Laurent Martelli <laurent@aopsys.com>
  
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashMap;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.apache.log4j.Logger;
import org.objectweb.jac.aspects.gui.*;
import org.objectweb.jac.aspects.gui.InvokeEvent;
import org.objectweb.jac.aspects.session.SessionAC;
import org.objectweb.jac.core.Collaboration;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.MethodItem;
import org.objectweb.jac.util.Strings;

public class Menu extends JMenu implements MenuView, ActionListener {
    static Logger logger = Logger.getLogger("gui.menu");
    static Logger loggerContext = Logger.getLogger("display-context");
    static Logger loggerEvents = Logger.getLogger("gui.events");
   
    String label;
    DisplayContext context;
    ViewFactory factory;

    Object[] parameters;
    String type;

    // label -> AbstractMethodItem
    HashMap actions = new HashMap();

    public Menu(ViewFactory factory, DisplayContext context) {
        logger.debug("new Menu");
        this.factory = factory;
        this.context = context;
    }

    public Menu() {
        logger.debug("new Menu");
    }

    Border viewBorder;
   
    /**
     * Get the value of viewBorder.
     * @return value of viewBorder.
     */
    public Border getViewBorder() {
        return viewBorder;
    }
   
    /**
     * Set the value of viewBorder.
     * @param v  Value to assign to viewBorder.
     */
    public void setViewBorder(Border  v) {
        this.viewBorder = v;
    }
   
    // style used to change display (css for web)
    String style;

    public void setStyle(String style) {
        this.style = style;
    }

    public String getStyle() {
        return style;
    }


    // MenuView interface

    public void addSubMenu(String label, String icon, MenuView submenu) {
        logger.debug("addSubMenu("+label+","+icon+") on "+this);
        if (icon==null)
            icon = ResourceManager.getResource("blank_icon");
        AbstractButton button = (AbstractButton)submenu;
        button.setText(label);
        button.setMnemonic(
            MenuBar.getMnemonic(
                getPopupMenu(),
                label));
        button.setIcon(ResourceManager.getIcon(icon));
        add((JComponent)submenu);
    }

    public void addAction(String label, String icon, Callback callback) {
        logger.debug("addAction("+label+","+icon+","+callback+") on "+this);
        JMenuItem item = new JMenuItem(label,ResourceManager.getIcon(icon));
        item.setActionCommand(label);
        item.addActionListener(this);
        item.setMnemonic(
            MenuBar.getMnemonic(
                getPopupMenu(),
                GuiAC.getMnemonics(callback.getMethod())+label));
        actions.put(label,callback);
        add(item);
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
   

    // View interface

    String description;
   
    /**
     * Get the value of description.
     * @return value of description.
     */
    public String getDescription() {
        return description;
    }
   
    /**
     * Set the value of description.
     * @param v  Value to assign to description.
     */
    public void setDescription(String  v) {
        this.description = v;
    }
   
    View parentView;
   
    /**
     * Get the value of parentView.
     * @return value of parentView.
     */
    public View getParentView() {
        return parentView;
    }
   
    /**
     * Set the value of parentView.
     * @param v  Value to assign to parentView.
     */
    public void setParentView(View  v) {
        this.parentView = v;
    }

    public View getRootView() {
        if (parentView==null)
            return this;
        return parentView.getRootView();
    }

    public boolean isDescendantOf(View ancestor) {
        if (this==ancestor)
            return true;
        else if (parentView==null)
            return false;
        else
            return parentView.isDescendantOf(ancestor);
    }

    MethodItem message;

    /**
     * Get the value of message.
     * @return value of message.
     */
    public MethodItem getMessage() {
        return message;
    }
   
    /**
     * Set the value of message.
     * @param v  Value to assign to message.
     */
    public void setMessage(MethodItem  v) {
        this.message = v;
    }

    public void setContext(DisplayContext context) {
        loggerContext.debug("setContext on "+this);
        this.context = context;
    }

    public DisplayContext getContext() {
        return context;
    }

    public void setFactory(ViewFactory factory) {
        this.factory = factory;
    }

    public ViewFactory getFactory() {
        return factory;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setSize(Length width, Length height) {
        if (width!=null || height!=null)
            logger.warn("MenuBar does not support setSize");
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public boolean equalsView(ViewIdentity view) {
        return 
            ( ( type!=null && 
                type.equals(view.getType()) )
              || (type==null && view.getType()==null ) )
            && ( ( parameters!=null && 
                   Arrays.equals(parameters,view.getParameters()) ) 
                 || (parameters==null && view.getParameters()==null) );
    }

    public boolean equalsView(String type, Object[] parameters) {
        return this.type.equals(type)
            && Arrays.equals(this.parameters,parameters);
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }
   
    public Object[] getParameters() {
        return parameters;
    }


    public void close(boolean validate) {
        closed = true;
    }

    boolean closed = false;

    public boolean isClosed() {
        return closed;
    }

    public void setFocus(FieldItem field, Object option) {
    }

    // implementation of java.awt.event.ActionListener interface

    public void actionPerformed(ActionEvent event)
    {
        loggerEvents.debug("Menu.actionPerformed("+event.getActionCommand()+")");
        Collaboration.get().addAttribute(
            SessionAC.SESSION_ID, GuiAC.getLocalSessionID());
        Callback callback = (Callback)actions.get(event.getActionCommand());
        callback.invoke(context,this);
    }

    public String toString() {
        return Strings.hex(this);
    }

}

