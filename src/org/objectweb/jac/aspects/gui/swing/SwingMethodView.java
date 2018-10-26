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

package org.objectweb.jac.aspects.gui.swing;

import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import org.apache.log4j.Logger;
import org.objectweb.jac.aspects.gui.Border;
import org.objectweb.jac.aspects.gui.DisplayContext;
import org.objectweb.jac.aspects.gui.EventHandler;
import org.objectweb.jac.aspects.gui.GuiAC;
import org.objectweb.jac.aspects.gui.InvokeEvent;
import org.objectweb.jac.aspects.gui.Length;
import org.objectweb.jac.aspects.gui.MethodUpdate;
import org.objectweb.jac.aspects.gui.MethodView;
import org.objectweb.jac.aspects.gui.ResourceManager;
import org.objectweb.jac.aspects.gui.Utils;
import org.objectweb.jac.aspects.gui.View;
import org.objectweb.jac.aspects.gui.ViewFactory;
import org.objectweb.jac.aspects.gui.ViewIdentity;
import org.objectweb.jac.aspects.session.SessionAC;
import org.objectweb.jac.core.Collaboration;
import org.objectweb.jac.core.rtti.AbstractMethodItem;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.MethodItem;
import org.objectweb.jac.util.Strings;

public class SwingMethodView extends JButton 
    implements MethodView, ActionListener, TableCellRenderer, MethodUpdate
{
    static Logger loggerEvents = Logger.getLogger("gui.events");

    DisplayContext context;
    Length width;
    Length height;
    ViewFactory factory;
    Object[] parameters;
    String type;

    Object substance;
    AbstractMethodItem method;
    MethodItem condition;


    // style used to change display (css for web)
    String style;

    public void setStyle(String style) {
        this.style = style;
    }

    public String getStyle() {
        return style;
    }


    public SwingMethodView(Object substance, AbstractMethodItem method) {
        this.substance = substance;
        setMethod(method);
        setMargin(new Insets(1,1,1,1));
        addActionListener(this);
    }

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
        this.context = context;
    }

    public DisplayContext getContext() {
        return context;
    }

    public String getLabel() {
        return getText();
    }

    public void setLabel(String label) {
        setText(label);
        //      setToolTipText(label);
    }

    public String getText() {
        if (method instanceof MethodItem && 
            ((MethodItem)method).isSetter() && getIcon()!=null)
            return "";
        else
            return super.getText();
    }

    public void setIcon(String icon) {
        setIcon(ResourceManager.getIcon(icon));
    }

    boolean onlyIcon = false;
    public void setOnlyIcon(boolean onlyIcon) {
        this.onlyIcon = onlyIcon;
    }

    public void setSize(Length width, Length height) {
        this.width = width;
        this.height = height;
        SwingUtils.setSize(this,width,height);
    }

    public void setMethod(AbstractMethodItem method) {
        if (condition!=null) {
            Utils.unregisterMethod(substance,condition,this);
        }
        this.method = method;
        condition = GuiAC.getCondition(method);
        if (condition!=null) {
            setEnabled(GuiAC.isEnabled(method,substance));
            Utils.registerMethod(substance,condition,this);
        }
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

    public void setSubstance(Object substance) {
        this.substance = substance;
    }

    public void close(boolean validate) {
        closed = true;
    }

    boolean closed = false;

    public boolean isClosed() {
        return closed;
    }

    public ViewFactory getFactory() {
        return factory;
    }

    public void setFactory(ViewFactory factory) {
        this.factory = factory;
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

    public void setFocus(FieldItem field, Object option) {
    }

    public String toString() {
        return Strings.hex(this);
    }


    // ActionListener interface

    public void actionPerformed(ActionEvent action) {
        loggerEvents.debug("action performed on MethodView "+method.getName());
        Collaboration collab = Collaboration.get();
        collab.addAttribute(GuiAC.DISPLAY_CONTEXT,context);
        collab.addAttribute(SessionAC.SESSION_ID,
                            GuiAC.getLocalSessionID());
        EventHandler.get().onInvoke(
            context,new InvokeEvent(this,substance,method));
    }

    // TableCellRenderer
    public Component getTableCellRendererComponent(
        JTable table, Object value, 
        boolean isSelected, boolean hasFocus, 
        int row, int column) 
    {
        JComponent component = this;//getComponent();
      
        if (component!=null) {
            component.setOpaque(true); // so that the background is really drawn
        }
        setOpaque(true); // so that the background is really drawn
         
        if (isSelected) {
            if (component!=null) {
                component.setForeground(table.getSelectionForeground());
                component.setBackground(table.getSelectionBackground());
            }
            setForeground(table.getSelectionForeground());
            setBackground(table.getSelectionBackground());
        } else {
            if (component!=null) {
                component.setForeground(table.getForeground());
                component.setBackground(table.getBackground());
            }
            setForeground(table.getForeground());
            setBackground(table.getBackground());
        }
        if (component!=null) {
            component.setFont(null);
        }
        setFont(null);

        //setValue(value); 

        return this;
    }

    // MethodUpdate interface
    public void methodUpdated(Object substance, MethodItem method, Object param) {
        setEnabled(GuiAC.isEnabled(this.method,this.substance));
    }
}
