/*
  Copyright (C) 2001-2003 Renaud Pawlak <renaud@aopsys.com>>, 
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

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashMap;
import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import org.apache.log4j.Logger;
import org.objectweb.jac.aspects.gui.Border;
import org.objectweb.jac.aspects.gui.Callback;
import org.objectweb.jac.aspects.gui.DisplayContext;
import org.objectweb.jac.aspects.gui.EventHandler;
import org.objectweb.jac.aspects.gui.GuiAC;
import org.objectweb.jac.aspects.gui.InvokeEvent;
import org.objectweb.jac.aspects.gui.Length;
import org.objectweb.jac.aspects.gui.Menu;
import org.objectweb.jac.aspects.gui.MenuView;
import org.objectweb.jac.aspects.gui.ResourceManager;
import org.objectweb.jac.aspects.gui.View;
import org.objectweb.jac.aspects.gui.ViewFactory;
import org.objectweb.jac.aspects.gui.ViewIdentity;
import org.objectweb.jac.aspects.session.SessionAC;
import org.objectweb.jac.core.Collaboration;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.MethodItem;
import org.objectweb.jac.util.Strings;

public class MenuBar extends JMenuBar implements MenuView, ActionListener {
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


    // style used to change display (css for web)
    String style;

    public void setStyle(String style) {
        this.style = style;
    }

    public String getStyle() {
        return style;
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
   
    public MenuBar(ViewFactory factory, DisplayContext context) {
        logger.debug("new MenuBar");
        this.factory = factory;
        this.context = context;      
    }

    public MenuBar() {
        logger.debug("new MenuBar");
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

    public String getLabel() {
        return label;
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

    // MenuView interface

    public void addSubMenu(String label, String icon, MenuView submenu) {
        ((AbstractButton)submenu).setText(label);
        ((AbstractButton)submenu).setMnemonic(getMnemonic(this,label));
        add((JComponent)submenu);
    }

    public void addAction(String label, String icon, Callback callback) {
        JMenuItem item = new JMenuItem(label,ResourceManager.getIcon(icon));
        item.setActionCommand(label);
        item.addActionListener(this);
        item.setMnemonic(getMnemonic(this,label));
        actions.put(label,callback);
        add(item);
    }

    /*
     * Workaround for SwingWT bug where JComponent does not extend
     * Container.
     */
    public static char getMnemonic(Component component, String label) {
        if (component instanceof Container) {
            return getMnemonic((Container)component,label);
        } else {
            return label.length()>0 ? label.charAt(0) : (char)-1;
        }
    }

    /**
     * Returns a mnemonic (keyboard shortcut) not already used
     *
     * @param container check for used mnemonic in this container
     * @param label the label to get a mnemonic for
     * @return a mnemonic for the label, 0 if none could be
     * computed. The mnemonic is a character contained in the label
     */
    public static char getMnemonic(Container container, String label) {
        logger.debug("getMnemonic for \""+label+"\" in "+Strings.hex(container));
        StringBuffer usedMnemonics = new StringBuffer();
        for (int i=0; i<container.getComponentCount(); i++) {
            Component component = container.getComponent(i);
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton)component;
                logger.debug("  Mnemonic of "+Strings.hex(component)+
                             ": "+(char)button.getMnemonic());
                if (button.getMnemonic()!=0)
                    usedMnemonics.append(Character.toLowerCase((char)button.getMnemonic()));
            } else {
                logger.debug("  Skipping "+Strings.hex(component));
            }
        }
        logger.debug("  usedMnemonics="+usedMnemonics);
        for (int i=0; i<label.length(); i++) {
            char c = Character.toLowerCase(label.charAt(i));
            if (usedMnemonics.indexOf(""+c)==-1) {
                logger.debug("  -> "+c);
                return label.charAt(i);
            }
        }

        return 0;
    }
   
    String position = Menu.TOP;   
    /**
     * Get the value of position.
     * @return value of position.
     */
    public String getPosition() {
        return position;
    }

    /**
     * Set the value of position.
     * @param v Value to assign to position. If null, use the default
     * position.
     */
    public void setPosition(String  v) {
        logger.debug("setPosition("+v+")");
        this.position = v;
        if (position==null)
            position = Menu.TOP;
        if (position.equals(Menu.TOP)||position.equals(Menu.BOTTOM)) {
            setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
        } else {
            //getLayout(
            setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        } 
    }

    public void addSeparator() {
    }

    public String toString() {
        return Integer.toString(hashCode());
    }

    // implementation of java.awt.event.ActionListener interface

    public void actionPerformed(ActionEvent event)
    {
        loggerEvents.debug("MenuBar.actionPerformed("+event.getActionCommand()+")");
        Collaboration.get().addAttribute(
            SessionAC.SESSION_ID, GuiAC.getLocalSessionID());
        Callback callback = (Callback)actions.get(event.getActionCommand());
        EventHandler.get().onInvoke(
            context,
            new InvokeEvent(this,null,callback.getMethod(),callback.getParameters()),
            true,
            null,null);
    }

}

