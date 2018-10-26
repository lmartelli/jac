/*
  Copyright (C) 2001-2002 Renaud Pawlak, Laurent Martelli
  
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

import org.objectweb.jac.aspects.gui.ResourceManager;
import org.objectweb.jac.aspects.gui.TabsView;
import org.objectweb.jac.aspects.gui.View;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Arrays;
import java.util.Collection;
import javax.swing.JTabbedPane;

public class SwingTabbedView extends AbstractCompositeView implements TabsView
{
    JTabbedPane tabbedPane;

    public SwingTabbedView() {
        setLayout(new BorderLayout());
        tabbedPane = new JTabbedPane();
        add(tabbedPane);
    }

    /*
     * Add a tab
     *
     * @param extraInfos a String which is the title of the pane
     */
    public void addView(View view, Object extraInfos) {
        addTab(view,(String)extraInfos,null);
        view.setParentView(this);
    }

    public View getView(Object id) {
        if (id instanceof String)
            try {
                return (View)tabbedPane.getComponent(Integer.parseInt((String)id));
            } catch (NumberFormatException e) {
                return (View)tabbedPane.getComponent(
                    tabbedPane.indexOfTab((String)id));
            }
        else if (id instanceof Integer)
            return (View)tabbedPane.getComponent(((Integer)id).intValue());
        else
            throw new RuntimeException("getView(): bad id "+id);
    }

    public void select(String tab) {
        tabbedPane.setSelectedIndex(tabbedPane.indexOfTab(tab));
    }

    public Collection getViews() {
        return Arrays.asList(tabbedPane.getComponents());
    }

    public void removeAllViews() {
        close(true);
        tabbedPane.removeAll();
        validate();
    }

    public void addTab(View component, String category, String icon) {
        tabbedPane.addTab(category,ResourceManager.getIcon(icon),
                          (Component)component);
        validate();
    }
}
