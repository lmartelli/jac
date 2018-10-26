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

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import org.objectweb.jac.aspects.gui.*;
import org.objectweb.jac.util.ExtArrays;

public class SwingPanelView extends AbstractCompositeView 
    implements PanelView 
{
   
    int geometry;
    int subPanesCount;
    boolean[] scrollings;

    Vector subPanes = new Vector();
    Vector splitters = new Vector();
    Map splitterLocations;
    Map paneContainers;

    public SwingPanelView(ViewFactory factory, 
                          int subPanesCount, int geometry, 
                          Map paneContainers,
                          boolean[] scrollings, Map splitterLocations) {
        this.factory = factory;
        this.geometry = geometry;
        this.subPanesCount = subPanesCount;
        this.scrollings = scrollings;
        this.paneContainers = paneContainers;
        this.splitterLocations=splitterLocations;
        setLayout(new BorderLayout());
        construct();
    }

    protected void construct() 
    {
        splitters.clear();
        subPanes.clear();
        removeAll();
        Vector subScrollPanes = new Vector();
        for(int i=0;i<subPanesCount;i++) {
            CompositeView subPane;
            if (paneContainers.containsKey(Integer.toString(i))) {
                subPane = factory.createCompositeView(
                    "subpane["+i+"]",(String)paneContainers.get(Integer.toString(i)),
                    new Object[] {},
                    context);
            } else {
                subPane = 
                    factory.createCompositeView("subpane["+i+"]","SingleSlotContainer",
                                                ExtArrays.emptyObjectArray,
                                                context);
            }
            JScrollPane subScrollPane = null;
            if (scrollings[i] == true) {
                subScrollPane = new JScrollPane((Component)subPane);
            }
            subPanes.add(subPane);
            if (subScrollPane == null) {
                subScrollPanes.add(subPane);
            } else {
                subScrollPanes.add(subScrollPane);
            }
        }

        if (subPanesCount == 1) {
            add((Component)subScrollPanes.get(0));
        } else if (subPanesCount == 2) {
            // create a split panel
            JSplitPane splitPane  = new JSplitPane(
                geometry==Constants.HORIZONTAL?
                JSplitPane.VERTICAL_SPLIT:JSplitPane.HORIZONTAL_SPLIT,
                (Component)subScrollPanes.get(0), (Component)subScrollPanes.get(1));
            splitPane.setOneTouchExpandable(true);
            splitters.add(splitPane);
            add(splitPane);
        } else if (subPanesCount == 4) {
            // create two sub-split panels
            JSplitPane subSplitPane1  = new JSplitPane(
                geometry==Constants.HORIZONTAL?
                JSplitPane.HORIZONTAL_SPLIT:JSplitPane.VERTICAL_SPLIT,
                (Component)subScrollPanes.get(0), (Component)subScrollPanes.get(1));
            subSplitPane1.setOneTouchExpandable(true);
            JSplitPane subSplitPane2  = new JSplitPane(
                geometry==Constants.HORIZONTAL?
                JSplitPane.HORIZONTAL_SPLIT:JSplitPane.VERTICAL_SPLIT,
                (Component)subScrollPanes.get(2), (Component)subScrollPanes.get(3));
            subSplitPane2.setOneTouchExpandable(true);
            // create a split panel
            JSplitPane splitPane  = new JSplitPane(
                geometry==Constants.HORIZONTAL?
                JSplitPane.VERTICAL_SPLIT:JSplitPane.HORIZONTAL_SPLIT,
                subSplitPane1, subSplitPane2);
            splitPane.setOneTouchExpandable(true);
            splitters.add(splitPane);
            splitters.add(subSplitPane1);
            splitters.add(subSplitPane2);
            add(splitPane);
        } else if (subPanesCount == 3) {
            // create one sub-split panels
            JSplitPane subSplitPane  = new JSplitPane(
                geometry<Constants.VERTICAL?
                JSplitPane.HORIZONTAL_SPLIT:JSplitPane.VERTICAL_SPLIT,
                (Component)subScrollPanes.get(0), (Component)subScrollPanes.get(1));
            subSplitPane.setOneTouchExpandable(true);
            // create a split panel
            int type;
            int splitType;
            if (geometry<Constants.VERTICAL) {
                type = geometry - Constants.HORIZONTAL;
                splitType = JSplitPane.VERTICAL_SPLIT;
            } else {
                type = geometry - Constants.VERTICAL;
                splitType = JSplitPane.HORIZONTAL_SPLIT;
            }
            JSplitPane splitPane = new JSplitPane(
                splitType,
                type==1 ? subSplitPane : (Component)subScrollPanes.get(2),
                type==1 ? (Component)subScrollPanes.get(2) : subSplitPane);
            splitPane.setOneTouchExpandable(true);
            splitters.add(splitPane);
            splitters.add(subSplitPane);
            add(splitPane);
        }
    }

    public void setSplitterLocation(int splitterId, float location) {
        JSplitPane splitter = (JSplitPane)splitters.get(splitterId);
        splitter.setDividerLocation((double)location);
    }

    /**
     * Adds a view.
     *
     * @param component the view to add
     * @param extraInfo the panel ID
     */ 

    public void addView(View component, Object extraInfo) {
        int i;
        try {
            i = new Integer((String)extraInfo).intValue();
        } catch (NumberFormatException e) {
            switch (geometry) {
                case CustomizedGUI.HORIZONTAL:
                    if (extraInfo.equals(PanelView.UPPER) || 
                        extraInfo.equals(PanelView.UPPER_LEFT))
                        i = 0;
                    else if (extraInfo.equals(PanelView.LOWER) ||
                             extraInfo.equals(PanelView.LOWER_LEFT))
                        i = 1;
                    else if (extraInfo.equals(PanelView.UPPER_RIGHT))
                        i = 2;
                    else if (extraInfo.equals(PanelView.LOWER_RIGHT))
                        i = 3;
                    else
                        throw new RuntimeException("Unknown position: "+extraInfo);
                    break;
                case CustomizedGUI.VERTICAL:
                    if (extraInfo.equals(PanelView.LEFT) || 
                        extraInfo.equals(PanelView.UPPER_LEFT))
                        i = 0;
                    else if (extraInfo.equals(PanelView.RIGHT) || 
                             extraInfo.equals(PanelView.UPPER_RIGHT))
                        i = 1;
                    else if (extraInfo.equals(PanelView.LOWER_LEFT))
                        i = 2;
                    else if (extraInfo.equals(PanelView.LOWER_RIGHT))
                        i = 3;
                    else
                        throw new RuntimeException("Unknown position: "+extraInfo);
                    break;
                case CustomizedGUI.VERTICAL_RIGHT:
                    if (extraInfo.equals(PanelView.LEFT))
                        i = 2;
                    else if (extraInfo.equals(PanelView.UPPER_RIGHT))
                        i = 0;
                    else if (extraInfo.equals(PanelView.LOWER_RIGHT))
                        i = 1;
                    else
                        throw new RuntimeException("Unknown position: "+extraInfo);
                    break;
                case CustomizedGUI.VERTICAL_LEFT:
                    if (extraInfo.equals(PanelView.RIGHT))
                        i = 2;
                    else if (extraInfo.equals(PanelView.UPPER_LEFT))
                        i = 0;
                    else if (extraInfo.equals(PanelView.LOWER_LEFT))
                        i = 1;
                    else
                        throw new RuntimeException("Unknown position: "+extraInfo);
                    break;
                case CustomizedGUI.HORIZONTAL_DOWN:
                    if (extraInfo.equals(PanelView.UPPER))
                        i = 2;
                    else if (extraInfo.equals(PanelView.LOWER_LEFT))
                        i = 0;
                    else if (extraInfo.equals(PanelView.LOWER_RIGHT))
                        i = 1;
                    else
                        throw new RuntimeException("Unknown position: "+extraInfo);
                    break;
                case CustomizedGUI.HORIZONTAL_UP:
                    if (extraInfo.equals(PanelView.LOWER))
                        i = 2;
                    else if (extraInfo.equals(PanelView.UPPER_LEFT))
                        i = 0;
                    else if (extraInfo.equals(PanelView.UPPER_RIGHT))
                        i = 1;
                    else
                        throw new RuntimeException("Unknown position: "+extraInfo);
                    break;
                default:
                    throw new RuntimeException("Invalid geometry: "+geometry);
            }
        }
        CompositeView subPane=(CompositeView)subPanes.get(i);
        subPane.addView(component,null);
        component.setParentView(this);
    }

    public Collection getViews() {
        return subPanes;//Arrays.asList(getComponents());
    }

    public View getView(Object id) {
        return (View)subPanes.get(Integer.parseInt((String)id));
    }

    public void close(boolean validate) {
        Iterator it = subPanes.iterator();
        while(it.hasNext()) {
            View sp = (View)it.next();
            sp.close(validate);
        }
    }

    public void removeAllViews() {
        //
    }
}
