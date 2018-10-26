/*
  Copyright (C) 2002-2003 Laurent Martelli <laurent@aopsys.com>
  
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

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import org.apache.log4j.Logger;
import org.objectweb.jac.aspects.cache.MethodCache;
import org.objectweb.jac.aspects.gui.*;
import org.objectweb.jac.aspects.gui.web.html.*;
import org.objectweb.jac.aspects.timestamp.Timestamps;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.MethodItem;
import org.objectweb.jac.util.Images;
import org.objectweb.jac.util.ObjectArray;
import org.objectweb.jac.util.Strings;

public abstract class AbstractView implements View {
    static Logger logger = Logger.getLogger("web");
    static Logger loggerClose = Logger.getLogger("gui.close");
    static Logger loggerDisplay = Logger.getLogger("display");
    static Logger loggerContext = Logger.getLogger("display.context");
   
    protected String label;
    protected DisplayContext context;
    protected Length width;
    protected Length height;
    ViewFactory factory;

    // style used to change display (css for web)
    String style;

    Object[] parameters;
    String type;

    /** row number, for TableCellViewer */
    protected boolean isCellViewer = false;
    protected int row;
    protected int column;
    protected View table;

    public AbstractView() {
    }

    public AbstractView(ViewFactory factory, DisplayContext context) {
        this.factory = factory;
        this.context = context;
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

    protected String description;
   
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

    protected View parentView;
   
    /**
     * Get the value of parent.
     * @return value of parent.
     */
    public View getParentView() {
        return parentView;
    }
   
    /**
     * Set the value of parent.
     * @param v  Value to assign to parent.
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
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getStyle() {
        return style;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }
   
    public Object[] getParameters() {
        return parameters;
    }

    public void setFocus(FieldItem field, Object option) {
    }

    public void close(boolean validate) {
        loggerClose.debug("closing "+this);
        closed = true;
        ((WebDisplay)context.getDisplay()).unregisterView(this);
    }

    boolean closed = false;

    public boolean isClosed() {
        return closed;
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

    /**
     * Are we in a <FORM> element ?
     */
    protected boolean isInForm() {
        return true;
    }
   
    /**
     * Build an HTML element for an event. It takes into account if we
     * are in a form, and if the browser is MS-IE.
     * @param text text to display for the link
     * @param event the name of the event 
     * @param params additional parameters for the link URL
     * @return an HTML element */
    protected Composite eventURL(String text, String event, String params) {
        JacRequest request = WebDisplay.getRequest();
        String parameters = "event="+event+"&amp;source="+getId()+params;
        if (request.isIEUserAgent()) {
            // workaround for MSIE which does not handle <button> the way it should
            logger.debug("user-agent: "+request.getUserAgent());
            Link link = new Link(
                ((WebDisplay)context.getDisplay()).getServletName()+
                "?"+parameters,
                text);
            link.attribute("onclick","return commitForm(this,'"+parameters+"')");
            return link;
        } else {
            if (isInForm()) {
                Button button = 
                    new Button("submit","eventAndAction",parameters);
                button.add(text);
                return button;
            } else {
                return new Link(
                    ((WebDisplay)context.getDisplay()).getServletName()+
                    "?event="+event+"&amp;source="+getId()+params,
                    text);
            }
        } 
    }

    /**
     * Write HTML code for a button
     *
     * @param out where to write the HTML 
     * @param icon resource name of an icon
     * @param label text of the button
     * @param event the event linked to the button
     */
    protected void showButton(PrintWriter out, String icon, String label, String event) {
        JacRequest request = WebDisplay.getRequest();
        if (request.isIEUserAgent()) {
            out.println(
                "<table class=\"method\"><td>"+
                (icon!=null?iconElement(ResourceManager.getResource(icon),"").toString():"")+
                eventURL(label,event,"").toString()+
                "</td></table>");
        } else {
            out.println(
                eventURL(label,event,"")
                .add(0,(icon!=null?iconElement(ResourceManager.getResource(icon),"").toString():""))
                .cssClass("method")
                .toString());
        }
    }

    public String getOpenBorder() {
        String s = "";
        if (viewBorder==null) 
            return s;
        s += "<div class=\"BORDER_"+Border.i2aStyle(viewBorder.getStyle())+"\">";
        if (viewBorder.hasTitle()) {
            s += "<div class=\"label\">"+viewBorder.getTitle()+"</div>";
        }
        return s;
    }

    public String getCloseBorder() {
        if (viewBorder==null) 
            return "";
        return "</div>";
    }

    protected String getBaseURL() {
        return ((WebDisplay)context.getDisplay()).getServletName();
    }

    /**
     * Build the base URL for an event
     * @param event the name of the event
     */
    protected String eventURL(String event) {
        String base = getBaseURL()+
            "?event="+event+"&amp;source="+getId();
        if (isCellViewer)
            return base+"&amp;tableEventSource="+getId(table)+
                "&amp;row="+row+"&amp;col="+column;
        else
            return base;
    }

    /**
     * Builds an <img> tag for an icon
     *
     * @param icon resource path of icon
     * @param alt alt string for <img> HTML tag
     */ 
    protected Element iconElement(String icon, String alt) {
        return iconElement(icon,alt,true);
    }

    static MethodCache iconCache = new MethodCache(null/*new Timestamps()*/);

    /**
     * Builds an <img> tag for an icon
     *
     * @param icon resource path of icon
     * @param alt alt string for <img> HTML tag
     * @param showAlt if true, return alt if icon is null
     */ 
    protected Element iconElement(String icon, String alt, boolean showAlt) {
        if (Strings.isEmpty(icon)) {
            return new Text(showAlt?alt:"");
        } else {
            Dimension size = null;
            File file = new File(icon);
            ObjectArray args = new ObjectArray(new Object[]{file});
            MethodCache.Entry entry = iconCache.getEntry(args,null);
            if (entry!=null) {
                size = (Dimension)entry.value;
            } else {
                try {
                    size = Images.getImageFileSize(file);
                    logger.debug("size of "+icon+": "+size.width+"x"+size.height);
               } catch (Exception e) {
                    logger.warn("Could not determine size of icon "+icon,e);
                }
            }
            if (size==null) {
                logger.warn("Could not determine size of icon "+icon);
            } else {
                 iconCache.putEntry(args,size);
            }
            return new Image("resources/"+icon,alt,size).cssClass("icon");
        }
    }

    protected String getId() {
        return ((WebDisplay)context.getDisplay()).registerView(this);
    }

    protected String getId(View view) {
        return ((WebDisplay)context.getDisplay()).registerView(view);
    }

    // HTMLViewer interface

    /* the style sheets */
    String styleSheet = "resources/org/objectweb/jac/aspects/gui/web/style.css";
    String styleSheetIE = "resources/org/objectweb/jac/aspects/gui/web/ie.css";
    String styleSheetKonqueror = "resources/org/objectweb/jac/aspects/gui/web/konqueror.css";

    String javascript = "resources/org/objectweb/jac/aspects/gui/web/script.js";

    public void setStyleSheet(String styleSheet) {
        this.styleSheet = styleSheet;
    }

    /**
     * Generate an HTML page, with full headers
     * @see #genBody(PrintWriter)
     */
    protected void genPage(PrintWriter out) throws IOException {
        out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\">");
        out.println("<html>");
        out.println("  <head>");
        out.println("  <title>"+label+"</title>");
        out.println("  <meta name=\"Author\" content=\"JAC web-gui server\">" );
        out.println("  <script type=\"text/javascript\" src=\""+javascript+"\"></script>");
        genStyleSheets(out,context.getCustomizedView());
        out.println("  </head>");
        out.println("  <body>");
        genBody(out);
        out.println("  </body>");
        out.println("</html>");
    }

    protected void genStyleSheets(PrintWriter out, CustomizedView customized) {
        out.println("  <link rel=\"stylesheet\" type=\"text/css\" "+
                    "href=\""+styleSheet+"\" title=\"JAC\">");
        JacRequest request = WebDisplay.getRequest();
        if (request.isIEUserAgent()) {
            out.println("  <link rel=\"stylesheet\" type=\"text/css\" "+
                        "href=\""+styleSheetIE+"\">");
        }
        if (request.userAgentMatch("Konqueror")) {
            out.println("  <link rel=\"stylesheet\" type=\"text/css\" "+
                        "href=\""+styleSheetKonqueror+"\">");
        }

        out.println("  <style type=\"text/css\">");
        Iterator it;
        if (customized!=null) {
            it = customized.getCustomizedGUI().getStyleSheetURLs().iterator();
            while (it.hasNext()) {
                String url = (String)it.next();
                out.println("    @import \""+url+"\";");         
            }
        }
        it = GuiAC.getStyleSheetURLs().iterator();
        while (it.hasNext()) {
            String url = (String)it.next();
            out.println("    @import \""+url+"\";");         
        }
        out.println("  </style>");
    }

    /**
     * Override this method to generate the body of an HTML page.
     * @see #genPage(PrintWriter)
     */
    protected void genBody(PrintWriter out) throws IOException {
        out.println("     <p>Empty page</p>");
    }

    // TableCellViewer interface

    public void setTable(View table) {
        this.table = table;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    Hashtable attributes = new Hashtable();
    public void setAttribute(String name, String value) {
        attributes.put(name,value);
    }
    protected void printAttributes(PrintWriter out) {
        Iterator it = attributes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            out.write(" "+entry.getKey()+"=\""+entry.getValue()+"\"");
        }
    }

    protected void openForm(PrintWriter out) {
        out.println("  <form action=\""+
                    ((WebDisplay)context.getDisplay()).getServletName()+"\" "+
                    "method=\"post\" accept-charset=\""+GuiAC.getEncoding()+"\" "+
                    "enctype=\"multipart/form-data\">");
    }

    protected void closeForm(PrintWriter out) {
        out.println("  </form>");
    }

    protected void showFormButtons(PrintWriter out, boolean dialog) {
        out.println("  <div class=\"actions\">");
        out.println("    <input type=\"hidden\" name=\"source\" value=\""+getId()+"\">");
        if (dialog) {
            showButton(out,null,GuiAC.getLabelOK(),"onOK");
            showButton(out,null,GuiAC.getLabelCancel(),"onCancel");
        } else {
            showButton(out,null,GuiAC.getLabelClose(),"onOK");
        }
        if (context.hasEnabledEditor()) {
            showButton(out,null,"Refresh","onRefresh");
        }
        out.println("  </div>");
    }

    protected void showFormButtons(PrintWriter out) {
        showFormButtons(out,true);
    }

    protected void genEventAndActionButton(PrintWriter out, String event) {
        showButton(out,null,"Refresh",event);
    }
}
