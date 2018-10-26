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
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA */

package org.objectweb.jac.aspects.gui.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.objectweb.jac.aspects.gui.*;
import org.objectweb.jac.util.ExtArrays;

/**
 * Base class for composite views
 */
public class AbstractCompositeView extends AbstractView 
    implements CompositeView,HTMLViewer
{
    static Logger logger = Logger.getLogger("gui.close");
   
    Vector components = new Vector();

    public AbstractCompositeView() {
    }

    public void setContext(DisplayContext context) {
        super.setContext(context);
        // recursively set the display of inner components
        Iterator i = getViews().iterator();
        while (i.hasNext()) {
            View component = (View)i.next();
            component.setContext(context);
        }
    }

    public void addHorizontalStrut(int width) {}
    public void addVerticalStrut(int height) {}

    public void addView(View view, Object extraInfo) {
        view.setContext(context);
        components.add(view);
        view.setParentView(this);
    }

    public void addView(View view) {
        addView(view,null);
    }

    public Collection getViews() {
        return components;
    }

    public View getView(Object id) {
        if (id instanceof String)
            return (View)components.get(Integer.parseInt((String)id));      
        else if (id instanceof Integer)
            return (View)components.get(((Integer)id).intValue());
        else
            throw new RuntimeException("getView(): bad id "+id);
    }

    public boolean containsView(String viewType, Object[] parameters) {
        Iterator it = components.iterator();
        while (it.hasNext()) {
            View view = (View)it.next();
            if (view.equalsView(viewType,parameters))
                return true;
        }
        return false;
    }

    public void removeView(View component, boolean validate) {
        component.close(validate);
        components.remove(component);
    }

    public void removeAllViews(boolean validate) {
        closeAllViews(validate);
        components.clear();
    }

    public void close(boolean validate) {
        super.close(validate);
        closeAllViews(validate);
    }

    protected void closeAllViews(boolean validate) {
        logger.debug("closing "+components.size()+" components of "+this+": "+components);
        Iterator i = ((Vector)components.clone()).iterator();
        while (i.hasNext()) {
            ((View)i.next()).close(validate);
        }
    }

    protected void add(View component) {
        component.setParentView(this);
        components.add(component);
    }

    public void genDescription(PrintWriter out) {
        if (description!=null) {
            if(!(this instanceof ObjectView && 
                 (parentView!=null && parentView.getClass()==Dialog.class))) {
                out.println("<div class=\"description\">"+description+"</div>");
            }
        }
    }

    public void genMessage(PrintWriter out) {
        if(message!=null) {
            String msg=(String)message.invoke(null,ExtArrays.emptyObjectArray);
            out.println("<div class=\"message\">"+msg+"</div>");
        }
    }

    public void genHTML(PrintWriter out) throws IOException {
        Iterator i = components.iterator();
        while (i.hasNext()) {
            HTMLViewer component = (HTMLViewer)i.next();
            out.println("<div class=\""+type+
                        "\" id=\""+((View)component).getLabel()+"\">");
            component.genHTML(out);
            out.println("</div>");
        }
    }
}

