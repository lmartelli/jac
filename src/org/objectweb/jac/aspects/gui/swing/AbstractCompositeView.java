/*
  Copyright (C) 2001-2003 Renaud Pawlak <renaud@aopys.com>, 
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
import java.awt.Dimension;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.Box;
import org.objectweb.jac.aspects.gui.CommitException;
import org.objectweb.jac.aspects.gui.CompositeView;
import org.objectweb.jac.aspects.gui.DisplayContext;
import org.objectweb.jac.aspects.gui.View;

public class AbstractCompositeView extends AbstractView 
    implements CompositeView {
   
    public AbstractCompositeView() {
    }

    public void setContext(DisplayContext context) {
        super.setContext(context);
        // recursively set the display of inner components
        for (int i=0; i<getComponentCount(); i++) {
            Component component = getComponent(i);
            if (component instanceof View) {
                ((View)component).setContext(context);
            }
        }
    }

    public void addView(View view, Object extraInfo) {
        view.setContext(context);
        add((Component)view);
        view.setParentView(this);
        validate();
    }

    public void addView(View view) {
        addView(view,null);
    }

    public void addHorizontalStrut(int width) {
        add(Box.createRigidArea(new Dimension(width,1)));
    }

    public void addVerticalStrut(int height) {
        add(Box.createRigidArea(new Dimension(1,height)));
    }

    public Collection getViews() {
        Object[] components = getComponents();
        Vector views = new Vector();
        // Filter out non View instances because some
        // javax.swing.Box$Filler are sometimes added behind our back
        for (int i=0; i<components.length; i++) {
            if (components[i] instanceof View) {
                views.add(components[i]);
            }
        }
        return views;
    }

    public View getView(Object id) {
        if (id instanceof String)
            return (View)getComponent(Integer.parseInt((String)id));      
        else if (id instanceof Integer)
            return (View)getComponent(((Integer)id).intValue());
        else
            throw new RuntimeException("getView(): bad id "+id);
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

    public void removeView(View component, boolean validate)
    {
        component.close(validate);
        remove((Component)component);
        validate();
    }

    public void removeAllViews(boolean validate) {
        close(validate);
        removeAll();
    }

    public void close(boolean validate) {
        super.close(validate);
        Iterator i = getViews().iterator();
        while (i.hasNext()) {
            Object view = i.next();
            if (view instanceof View) {
                try {
                    ((View)view).close(validate);
                } catch (CommitException e) {
                    throw e;
                } catch (Exception e) {
                    loggerClose.error("AbstractCompositeView.close: failed to close "+view,e);
                }
            }
        }
    }
}

