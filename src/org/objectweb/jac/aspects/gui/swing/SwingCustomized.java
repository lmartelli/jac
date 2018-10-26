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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import org.apache.log4j.Logger;
import org.objectweb.jac.aspects.gui.*;
import org.objectweb.jac.aspects.gui.Menu;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.MethodItem;
import org.objectweb.jac.util.Strings;


public class SwingCustomized extends JFrame implements CustomizedView {
    static Logger logger = Logger.getLogger("display");
    static Logger loggerContext = Logger.getLogger("display-context");

    String label;
    String type;
    DisplayContext context;
    ViewFactory factory;
    Object[] parameters;

    CustomizedGUI customized;
    JPanel contentPanel;
    PanelView mainView;

    public SwingCustomized(ViewFactory factory, DisplayContext context,
                           CustomizedGUI customized) {
        this.factory = factory;
        this.customized = customized;
        this.context = context;
        context.setCustomizedView(this);
        contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        setContentPane(contentPanel);
        try {
            mainView = (PanelView)factory.createCompositeView(
                "main",
                "Panel",
                new Object[] {
                    new Integer(customized.getSubPanesCount()),
                    new Integer(customized.getGeometry()),
                    customized.getPaneContainers(),
                    customized.getScrollings(),
                    customized.getSplitters() },
                context
            );
            contentPanel.add((JComponent)mainView,BorderLayout.CENTER);
        } catch (ViewFactory.UnhandledViewTypeException e) {
            e.printStackTrace();
        }

        GenericFactory.initCustomized(factory, context, mainView, customized, null);
        GenericFactory.setMenuBars(factory,context,this,customized.getMenus());
        GenericFactory.setToolBar(factory,context,this,customized.getToolbar()); 

        if (customized.hasStatusBar())
            GenericFactory.setStatusBar(factory, context, 
                                        this, customized.getStatusBarMethod(), 
                                        customized.getStatusPosition());

        addMouseListener(CollaborationInitializer.get());

        this.addWindowListener(
            new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        close(true);
                        if(getCustomized().getOnCloseHandler()!=null) {
                            EventHandler.get().onInvoke(
                                getContext(),
                                new InvokeEvent(
                                    SwingCustomized.this,
                                    null,
                                    getCustomized().getOnCloseHandler()),
                                false,
                                null,null);
                        }
                        GuiAC.removeDisplay(
                            GuiAC.getDisplay(getCustomized().getId()));
                  
                        //((GuiAC)ACManager.get().getAC("gui"))
                  
                    }
                }
        );

        if (customized.getIcon()!=null) {
            ImageIcon icon = ResourceManager.getIconResource(customized.getIcon());
            if (icon!=null)
                setIconImage(icon.getImage());
        }

        pack();

        if (customized.isGeometrySet()) {
            setPosition(customized.getLeft(),customized.getUp(),
                        customized.getWidth(),customized.getHeight());
        }
    }

    public void addHorizontalStrut(int width) {}
    public void addVerticalStrut(int height) {}

    public void setSplitters() {
        Iterator i = customized.getSplitters().entrySet().iterator();
        while(i.hasNext()) {
            Map.Entry entry = (Map.Entry)i.next();
            mainView.setSplitterLocation(((Integer)entry.getKey()).intValue(),
                                         ((Float)entry.getValue()).floatValue());
        }
    }

    /**
     * Sets the dimensions and position of the window regarding to the
     * main screen.
     *
     * @param left left-border pixel
     * @param up upper-border pixel
     * @param width in percentage regarding the screen
     * @param height in percentage regarding the screen */

    public void setPosition(int left, int up, int width, int height) {
        logger.debug("setPosition("+left+","+up+","+width+","+height+")");
        Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
        int w = (int) ((float)screenDim.getWidth()*width)/100;
        int h = (int) ((float)screenDim.getHeight()*height)/100;
        logger.debug("dimension = "+w+"x"+h);
        setBounds(new Rectangle(
            left,up,
            (int)(((float)screenDim.getWidth()*width)/100),
            (int)(((float)screenDim.getHeight()*height)/100)));
    }


    public void close(boolean validate) {
        mainView.close(validate);
        closed = true;
        dispose();
    }

    boolean closed = false;

    public boolean isClosed() {
        return closed;
    }

    // View interface

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

    public void setLabel(String label) {
        setTitle(label);
    }

    public String getLabel() {
        return getTitle();
    }

    public void setFactory(ViewFactory factory) {
        this.factory = factory;
    }

    public ViewFactory getFactory() {
        return factory;
    }

    public void setContext(DisplayContext context) {
        loggerContext.debug("setContext on "+getClass().getName());
        this.context = context;
        // recursively set the display of inner components
        Iterator i = mainView.getViews().iterator();
        while (i.hasNext()) {
            View view = (View)i.next();
            loggerContext.debug("set context on subView "+view);
            view.setContext(context);
        }
    }

    public void setSize(Length width, Length height) {
        //SwingUtils.setSize(this,width,height);
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }
   
    public Object[] getParameters() {
        return parameters;
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

    public void addView(View view, Object extraInfos) {
        view.setContext(context);
        mainView.addView(view,extraInfos);
    }

    public void addView(View view) {
        addView(view,null);
    }

    public Collection getViews() {
        return mainView.getViews();
    }

    public View getView(Object id) {
        return mainView.getView(id);
    }

    public void removeView(View component, boolean validate)
    {
        mainView.removeView(component, validate);
    }

    public void removeAllViews(boolean validate) {
        mainView.removeAllViews(validate);
    }

    public boolean containsView(String viewType, Object[] parameters) {
        Iterator it = getViews().iterator();
        while (it.hasNext()) {
            View view = (View)it.next();
            if (view.equalsView(viewType,parameters))
                return true;
        }
        return false;
    }

    public void setFocus(FieldItem field, Object option) {
    }

    // CustomizedView interface

    public CustomizedGUI getCustomizedGUI() {
        return customized;
    }

    public void setMenuBar(MenuView menuBar,String position) {
        if (position==null) {
            position = Menu.TOP;
        }
        menuBar.setPosition(position);
        if(position.equals(Menu.TOP)) {
            setJMenuBar((JMenuBar)menuBar);
        } else {
            if(position.equals(Menu.BOTTOM)) {
                getContentPane().add((JMenuBar)menuBar,BorderLayout.SOUTH);
            } else if(position.equals(Menu.LEFT)) {
                getContentPane().add((JMenuBar)menuBar,BorderLayout.WEST);
            } else if(position.equals(Menu.RIGHT)) {
                getContentPane().add((JMenuBar)menuBar,BorderLayout.EAST);
            }
        }
        if(position.equals(Menu.LEFT)||position.equals(Menu.BOTTOM)) {
            // What the hell is this ???
            for(int i=0;i<50;i++) {
                ((JMenuBar)menuBar).add(Box.createVerticalGlue());
            }
        }
    }

    public void setToolBar(MenuView toolBar) {
        contentPanel.add((Component)toolBar,BorderLayout.NORTH);
    }

    StatusView statusView;

    public void setStatusBar(StatusView view, String position) {
        statusView=view;
        contentPanel.add((Component)view,BorderLayout.SOUTH);
    } 

    public void showStatus(String message) {
        statusView.showMessage(message);
    }

    public PanelView getPanelView() {
        return mainView;
    }

    public DisplayContext getContext() {
        return context;
    }

    CustomizedGUI getCustomized() {
        return customized;
    }

    public void requestFocus() {
        show();
        super.requestFocus();
    }

    public String toString() {
        return Strings.hex(this);
    }
}
 
