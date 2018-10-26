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

package org.objectweb.jac.aspects.gui.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.objectweb.jac.aspects.gui.*;
import org.objectweb.jac.aspects.gui.web.html.*;

/**
 * A tabs component.
 */
public class Tabs extends AbstractCompositeView
    implements TabsListener, TabsView
{
    static Logger logger = Logger.getLogger("gui.web");
    static Logger loggerEditor = Logger.getLogger("gui.editor");

    /* name of the tabs */
    Vector tabs = new Vector();

    /* icons */
    Vector icons = new Vector();

    /* the selected tab */
    View selected;

    public Tabs() {
    }

    /**
     * Add a tab
     *
     * @param extraInfos a String which is the title of the pane
     */
    public void addView(View view, Object extraInfos) {
        logger.debug("TabbedPane.addView("+view+","+extraInfos+")");
        add(view);
        tabs.add((String)extraInfos);
        icons.add("");
        if (selected==null) {
            setSelected(view);
        }
    }

    public void addTab(View component, String category, String icon) {
        logger.debug("TabbedPane.addView("+component+","+category+")");
      
        add(component);
        tabs.add((String) category);
        icons.add(icon);
        if (selected==null) {
            setSelected(component);
        }
    }

    public View getView(Object id) {
        if (id instanceof String)
            try {
                return (View)components.get(Integer.parseInt((String)id));      
            } catch (NumberFormatException e) {
                return getTab((String)id);
            }
        else if (id instanceof Integer)
            return (View)components.get(((Integer)id).intValue());
        else
            throw new RuntimeException("getView(): bad id "+id);
    }

    public void select(String tab) {
        setSelected(getTab(tab));
    }

    /**
     * Disable editors which are not an the selected tab
     */
    protected void setSelected(View selected) {
        this.selected = selected;
        loggerEditor.debug("setSelected "+selected);
        Iterator it = context.getEditors().iterator();
        while (it.hasNext()) {
            Object view = it.next();
            if (view instanceof FieldEditor) {
                FieldEditor editor = (FieldEditor)view;
                if (((View)editor).isDescendantOf(selected))
                    editor.setEnabled(true);
                else
                    editor.setEnabled(false);
            }
        }
    }

    /**
     * Returns the tab with a given name
     * @param tab the name of the tab
     */
    public View getTab(String tab) {
        return getView(new Integer(tabs.indexOf(tab)));
    }

    // HTMLViewer interface

    public void genHTML(PrintWriter out) throws IOException {
        Iterator i = tabs.iterator();
        Iterator j = icons.iterator();
        int index = 0;
      
        if (tabs.size() != icons.size())
            throw new RuntimeException("Number of tabs and number" +
                                       " of icons are different");

        out.println("<div class=\""+type+"\">");
        JacRequest request=WebDisplay.getRequest();
        if (request.isIEUserAgent()) {
            //out.println("  <div class=\"ieheader\">");
            out.println("  <table class=\"ieheader\"><tr>");
        } else {
            out.println("  <div class=\"header\">");
        }

        while (i.hasNext()) {
            String icon = (String) j.next();
            String label = (String)i.next(); 
            String str;
            if (icon != null)
                str = iconElement(icon, "") + label;
            else
                str = label;
            Element element = (Element)eventURL(str, "onSelect",
                                                "&amp;index=" + index);
            if (selected==components.get(index)) {
                element.cssClass("selected");
            } 
            try {
                if (request.isIEUserAgent()) {
                    if (selected==components.get(index))
                        out.println("<td class=\"td-selected\">");
                    else
                        out.println("<td class=\"td\">");
                }
                element.write(out);
                if (request.isIEUserAgent()) {
                    out.println("</td>");
                    if (i.hasNext()) {
                        out.println("<td>&nbsp;</td>");
                    }
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
            index++;
        }      
        if (request.isIEUserAgent()) {
            out.println("  </tr></table>");
        } else {
            out.println("  </div>");
        }
        out.println("  <div class=\"body\">");
        if (selected!=null)
            ((HTMLViewer)selected).genHTML(out);
        out.println("  </div>");
        out.println("</div>");
    }

    // TabsListener interface

    public void onSelect(int index) {
        try {
            setSelected((View)components.get(index));
        } finally {
            context.getDisplay().refresh();         
        }
    }
}
