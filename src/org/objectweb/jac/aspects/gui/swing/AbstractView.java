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

import java.awt.Color;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import org.apache.log4j.Logger;
import org.objectweb.jac.aspects.gui.Border;
import org.objectweb.jac.aspects.gui.DialogView;
import org.objectweb.jac.aspects.gui.DisplayContext;
import org.objectweb.jac.aspects.gui.GuiAC;
import org.objectweb.jac.aspects.gui.InvokeEvent;
import org.objectweb.jac.aspects.gui.InvokeThread;
import org.objectweb.jac.aspects.gui.Length;
import org.objectweb.jac.aspects.gui.View;
import org.objectweb.jac.aspects.gui.ViewFactory;
import org.objectweb.jac.aspects.gui.ViewIdentity;
import org.objectweb.jac.aspects.session.SessionAC;
import org.objectweb.jac.core.Collaboration;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.MethodItem;
import org.objectweb.jac.util.Strings;

public abstract class AbstractView extends JPanel implements View {
    static Logger loggerEvents = Logger.getLogger("gui.events");
    static Logger loggerClose = Logger.getLogger("gui.close");
    static Logger loggerContext = Logger.getLogger("display-context");
    static Logger loggerDnd = Logger.getLogger("gui.dnd");

    String label;
    DisplayContext context;
    protected Length width;
    protected Length height;
    ViewFactory factory;

    Object[] parameters;
    String type;

    public AbstractView() {
        setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
    }

    public AbstractView(ViewFactory factory, DisplayContext context) {
        this.factory = factory;
        this.context = context;
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

    public void setContext(DisplayContext context) {
        loggerContext.debug("setContext on "+this);
        this.context = context;
    }

    public DisplayContext getContext() {
        return context;
    }

    // style used to change display (css for web)
    String style;

    public void setStyle(String style) {
        this.style = style;
    }

    public String getStyle() {
        return style;
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
        this.width = width;
        this.height = height;
        SwingUtils.setSize(this,width,height);
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

    protected boolean closed = false;

    public void close(boolean validate) {
        closed = true;
        parameters = null;
    }

    public boolean isClosed() {
        return closed;
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
     * @param border  Value to assign to viewBorder.
     */
    public void setViewBorder(Border  border) {
        javax.swing.border.Border swingBorder;
        this.viewBorder = border;
        switch(border.getStyle()) {
            case Border.LINE:
                swingBorder=BorderFactory.createLineBorder(Color.black);
                break;
            case Border.ETCHED:
                swingBorder=BorderFactory.createEtchedBorder();
                break;
            case Border.RAISED:
                swingBorder=BorderFactory.createRaisedBevelBorder();
                break;
            case Border.LOWERED:
                swingBorder=BorderFactory.createLoweredBevelBorder();
                break;
            default:
                swingBorder=BorderFactory.createLineBorder(Color.black);
        }
        if(border.hasTitle()) {
            swingBorder=BorderFactory.createTitledBorder(
                swingBorder,border.getTitle());
            switch(border.getAlignment()) {
                case Border.RIGHT:
                    ((TitledBorder)swingBorder).setTitleJustification(
                        TitledBorder.RIGHT);
                    break;
                case Border.CENTER:
                    ((TitledBorder)swingBorder).setTitleJustification(
                        TitledBorder.CENTER);
            }
        }
        this.setBorder(swingBorder);
    }
   
    public String toString() {
        return Strings.hex(this);
    }

    void setContext() {
        Collaboration.get().addAttribute(
            GuiAC.DISPLAY_CONTEXT,context);
        Collaboration.get().addAttribute(
            SessionAC.SESSION_ID,GuiAC.getLocalSessionID());
    }

    public void setFocus(FieldItem field, Object option) {
    }

    /**
     * Invoke a method with correct attributes
     * (DISPLAY_CONTEXT,SESSION_ID and all attributes stored by the
     * current window) with InvokeThread 
     *
     * @param substance the object on which to invoke the method
     * @param methodName the method to invoke
     * @param parameters parameters to pass to the method
     */
    protected void invokeInContext(Object substance,String methodName,
                                   Object[] parameters) 
    {
        MethodItem method = ClassRepository.get().getClass(substance)
            .getMethod(methodName);
        String[] names = new String[2];
        Object[] values = new Object[2];
        names[0] = GuiAC.DISPLAY_CONTEXT;
        values[0] = context;
        names[1] = SessionAC.SESSION_ID;
        values[1] = GuiAC.getLocalSessionID();
        //names[2] = "Gui.askForParameters";
        //values[2] = method;
        Object window = context.getWindow();

        if (window instanceof DialogView) {
            ((DialogView)window).restoreContext();
        } else if (window instanceof ObjectViewDialog) {
            ((ObjectViewDialog)window).restoreContext();
        }
        InvokeThread.run(new InvokeEvent(this,substance, method, parameters), 
                         null, null, names, values);
    }
}
