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
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA */

package org.objectweb.jac.aspects.gui.swing;


import java.util.Arrays;
import javax.swing.JLabel;
import org.objectweb.jac.aspects.gui.Border;
import org.objectweb.jac.aspects.gui.DisplayContext;
import org.objectweb.jac.aspects.gui.Length;
import org.objectweb.jac.aspects.gui.View;
import org.objectweb.jac.aspects.gui.ViewFactory;
import org.objectweb.jac.aspects.gui.ViewIdentity;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.MethodItem;

public class SwingLabel extends JLabel implements View {

    ViewFactory factory;
    DisplayContext context;
    Object[] parameters;
    String type;
   
    public SwingLabel() {
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

    public void setLabel(String label) {
        setText(label);
    }

    public String getLabel() {
        return getText();
    }

    public void setSize(Length width, Length height) {
        SwingUtils.setSize(this,width,height);
    }    

    public void close(boolean validate) {
        closed = true;
    }

    boolean closed = false;

    public boolean isClosed() {
        return closed;
    }

    public void setFactory(ViewFactory factory) {
        this.factory = factory;
    }

    public ViewFactory getFactory() {
        return factory;
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

    public void setFocus(FieldItem field, Object option) {
    }
 
    public String toString() {
        return Integer.toString(hashCode());
    }
}
