/*
  Copyright (C) 2002-2003 Renaud Pawlak <renaud@aopsys.com>, 
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

package org.objectweb.jac.aspects.gui.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.objectweb.jac.aspects.gui.*;
import org.objectweb.jac.aspects.gui.Menu;

public class Customized extends AbstractCompositeView 
    implements CustomizedView, HTMLViewer, WindowListener
{
    static Logger logger = Logger.getLogger("web");
    static Logger loggerContext = Logger.getLogger("display.context");
    static Logger loggerEditor = Logger.getLogger("gui.editor");

    CustomizedGUI customized;
    //   JPanel contentPanel;
    PanelView mainView;

    /**
     * Builds a customized view.
     * @param factory the view factory
     * @param context the display context
     * @param customized the customized GUI to build a view of
     * @param panels if not null, overrides the content of the view
     * (panelID -> PanelContent) 
     */
    public Customized(ViewFactory factory, DisplayContext context,
                      CustomizedGUI customized, Map panels) {
        this.factory = factory;
        this.customized = customized;
        this.context = context;
        context.setCustomizedView(this);

        logger.debug("building cutomized...");
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
        } catch (ViewFactory.UnhandledViewTypeException e) {
            e.printStackTrace();
        }

        //      setPosition(customized.getLeft(),customized.getUp(),
        //                  customized.getWidth(),customized.getHeight());
        try {
            GenericFactory.initCustomized(factory, context, mainView, 
                                          customized, panels);
            if (customized.hasMenuBar())
                GenericFactory.setMenuBars(factory, context, 
                                           this, customized.getMenus());
            if (customized.hasToolBar())
                GenericFactory.setToolBar(factory, context, 
                                          this, customized.getToolbar());
            if (customized.hasStatusBar())
                GenericFactory.setStatusBar(factory, context, 
                                            this, customized.getStatusBarMethod(), 
                                            customized.getStatusPosition());

        } catch (Exception e) {
            logger.error("Customized("+customized+")",e);
        }
        logger.debug("building cutomized DONE");
    }

    public void close(boolean validate) {
        mainView.close(validate);
    }   

    // View interface

    public void setContext(DisplayContext context) {
        super.setContext(context);
        loggerContext.debug("setContext on "+getClass().getName());
        // recursively set the display of inner components
        Iterator i = mainView.getViews().iterator();
        while (i.hasNext()) {
            View view = (View)i.next();
            loggerContext.debug("set context on subView "+view);
            view.setContext(context);
        }
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

    public void removeAllViews(boolean validate) {
        mainView.removeAllViews(validate);
    }

    // CustomizedView interface

    public CustomizedGUI getCustomizedGUI() {
        return customized;
    }

    Vector menuBars = new Vector();
    MenuView topMenuBar;
    MenuView bottomMenuBar;
    MenuView leftMenuBar;
    MenuView rightMenuBar;

    public void setMenuBar(MenuView menuBar,String position) {
        if (position==null)
            position = Menu.LEFT;
        menuBar.setPosition(position);
        if (position.equals(Menu.TOP)) {
            topMenuBar=menuBar;
        } else if(position.equals(Menu.BOTTOM)) {
            bottomMenuBar=menuBar;
        } else if(position.equals(Menu.LEFT)) {
            leftMenuBar=menuBar;
        } else if(position.equals(Menu.RIGHT)) {
            rightMenuBar=menuBar;
        }
        this.menuBars.add(menuBar);
    }

    public MenuView getTopMenuBar() {
        return topMenuBar;
    }

    public MenuView getBottomMenuBar() {
        return bottomMenuBar;
    }

    public MenuView getLeftMenuBar() {
        return leftMenuBar;
    }

    public MenuView getRightMenuBar() {
        return rightMenuBar;
    }

    MenuView toolBar;
    public void setToolBar(MenuView toolBar) {
        this.toolBar = toolBar;
    }

    StatusView statusBar;
    public void setStatusBar(StatusView statusBar,String position) {
        this.statusBar=statusBar;
        statusBar.setPosition(position);
    }

    public void showStatus(String message) {
        statusBar.showMessage(message);
    }

    public PanelView getPanelView() {
        return mainView;
    }

    public void requestFocus() {
    }

    // HTMLViewer interface
   
    public void genHTML(PrintWriter out) throws IOException {
        out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\">");
        out.println("<html>");
        out.println("  <head>");
        out.println("  <title>"+label+"</title>");
        out.println("  <meta name=\"Author\" content=\"JAC web-gui server\">" );
        out.println("  <script type=\"text/javascript\" src=\""+javascript+"\"></script>");
        if (customized.getIcon()!=null) {
            String resource = ResourceManager.getResource(customized.getIcon());
            if (resource!=null)
                out.println("  <link rel=\"icon\" href=\"resources/"+resource+"\" />");
        }
        genStyleSheets(out,context.getCustomizedView());
        out.println("  </head>");
        out.println("  <body>");
        genBody(out);
        out.println("  </body>");
        out.println("</html>");
    }

    protected void genBody(PrintWriter out) throws IOException {
        openForm(out);
        out.println("<table class=\"customized\">");
        if (statusBar!=null && statusBar.getPosition().equals(CustomizedGUI.TOP)) {
            out.println("  <tr>\n    <td class=\"statusBar\" colspan=\"3\">");         
            ((HTMLViewer)statusBar).genHTML(out);
            out.println("    </td>\n  </tr>");
        }
        if (topMenuBar!=null) {
            out.println("  <tr>\n    <td class=\"menuBarT\" colspan=\"3\">");
            ((HTMLViewer)topMenuBar).genHTML(out);
            if (toolBar!=null) {
                ((HTMLViewer)toolBar).genHTML(out);
            }
            out.println("    </td>\n  </tr>");
        }
        out.println("  <tr>");
        int colspan = 1;
        if (leftMenuBar==null)
            colspan++;
        if (rightMenuBar==null)
            colspan++;
        if (leftMenuBar!=null) {
            out.println("    <td class=\"menuBarL\">");
            ((HTMLViewer)leftMenuBar).genHTML(out);
            out.println("    </td>");
        }
        out.println("    <td"+(colspan>1?(" colspan=\""+colspan+"\""):"")+" class=\"mainView\"><div class=\"mainView\">");
        //      out.println("  ");
        ((HTMLViewer)mainView).genHTML(out);
        out.println("  \n</div>\n    </td>");
        if (rightMenuBar!=null) {
            out.println("    <td class=\"menuBarR\">");
            ((HTMLViewer)rightMenuBar).genHTML(out);
            out.println("    </td>");
        }
        out.println("  </tr>");
        if (bottomMenuBar!=null) {
            out.println("  <tr>\n    <td class=\"menuBarB\" colspan=\"3\">");
            ((HTMLViewer)bottomMenuBar).genHTML(out);
            out.println("    </td>\n  </tr>");
        }
        if (statusBar!=null && statusBar.getPosition().equals(CustomizedGUI.BOTTOM)) {
            out.println("  <tr>\n    <td class=\"statusBar\" colspan=\"3\">");         
            ((HTMLViewer)statusBar).genHTML(out);
            out.println("    </td>\n  </tr>");
        }
        out.println("</table>");

        if (context.hasEnabledEditor()) {
            out.println("<div id=\"buttons\">");
            loggerEditor.debug("editors = "+context.getEditors());
            showButton(out,null,GuiAC.getLabelOK(),"onOK");
            showButton(out,null,GuiAC.getLabelCancel(),"onCancel");
            out.println("</div>");
        }
        
        closeForm(out);
    }

    // WindowListener interface

    public void onOK(JacRequest request) {
        WebDisplay display = (WebDisplay)context.getDisplay();
        WebDisplay.readValuesAndRefresh(context,request,true);
    }

    public void onRefresh(JacRequest request) {
        WebDisplay display = (WebDisplay)context.getDisplay();
        WebDisplay.readValuesAndRefresh(context,request,true);
    }

    public void onCancel() {
        ((WebDisplay)context.getDisplay()).refresh();
    }

    public void onValidate(JacRequest request) {
        WebDisplay.readValues(context,request,true);
    }

}
 
