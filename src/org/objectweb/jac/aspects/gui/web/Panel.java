/*
  Copyright (C) 2002-2003 Laurent Martelli <laurent@aopsys.com>,
                          Renaud Pawlak <renaud@aopsys.com>
  
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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import org.objectweb.jac.aspects.gui.*;
import org.objectweb.jac.util.ExtArrays;

public class Panel extends AbstractCompositeView 
    implements PanelView, HTMLViewer 
{
   
    int geometry;
    int subPanesCount;
    boolean[] scrollings;

    Vector subPanes = new Vector();
    Map splitterLocations;
    Map paneContainers;

    public Panel(ViewFactory factory, 
                 int subPanesCount, int geometry, 
                 Map paneContainers,
                 boolean[] scrollings, Map splitterLocations) {
        this.factory = factory;
        this.geometry = geometry;
        this.subPanesCount = subPanesCount;
        this.scrollings = scrollings;
        this.paneContainers = paneContainers;
        this.splitterLocations=splitterLocations;
        construct();
    }

    protected void construct() 
    {
        subPanes.clear();
        components.clear();
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
            subPanes.add(subPane);
            add(subPane);
            /*
              JScrollPane subScrollPane = null;
              if( scrollings[i] == true ) {
              subScrollPane = new JScrollPane((Component)subPane);
              }         
              subPanes.add( subPane );
              if( subScrollPane == null ) {
              subScrollPanes.add(subPane);
              } else {
              subScrollPanes.add(subScrollPane);
              }
            */
        }
    }

    public void setSplitterLocation(int splitId, float location) {
        if (splitterLocations==null) 
            splitterLocations = new Hashtable();
        splitterLocations.put(new Integer(splitId),new Float(location));
    }

    /**
     * Returns a splitter's loctation as a percentage between 0 and 100.
     */
    protected float getSplitterLocation(int splitId) {
        if (splitterLocations==null) {
            return 50;
        } else {
            Float value = (Float)splitterLocations.get(new Integer(splitId));
            if (value!=null)
                return value.floatValue()*100;
            else 
                return 50;
        }
    }

    /**
     * Adds a view.
     *
     * @param component the view to add
     * @param extraInfo the panel ID
     */ 

    public void addView(View component, Object extraInfo) {
        int i=new Integer((String)extraInfo).intValue();
        CompositeView subPane=(CompositeView)subPanes.get(i);
        subPane.addView(component);
    }

    public Collection getViews() {
        return subPanes;
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

    // HTMLViewer interface

    public void genHTML(PrintWriter out) throws IOException {
        switch(subPanesCount) {
            case 1:
                out.println("<div class=\""+getClass().getName()+"\" id=\""+label+"\">");
                //out.println("<div>");
                ((HTMLViewer)components.get(0)).genHTML(out);
                out.println("</div>");
                break;
            case 2:
                if (geometry==Constants.HORIZONTAL) {
                    out.println("<table style=\"width:100%;height:100%\">"+
                                "<tr class=\"up\" style=\"height:"+
                                (100-getSplitterLocation(0))+"%\"><td>");
                    ((HTMLViewer)components.get(0)).genHTML(out);
                    out.println("</td></tr>");
                    out.println("<tr class=\"down\" height=\""+
                                getSplitterLocation(0)+"%\"><td>");
                    ((HTMLViewer)components.get(1)).genHTML(out);
                    out.println("</td></tr></table>");
                } else {
                    out.println("<table style=\"width:100%;height:100%\">"+
                                "<tr><td class=\"left\" width=\""+
                                getSplitterLocation(0)+"%\">");
                    ((HTMLViewer)components.get(0)).genHTML(out);
                    out.println("</td>");
                    out.println("<td class=\"right\" width=\""+
                                (100-getSplitterLocation(0))+"%\">");
                    ((HTMLViewer)components.get(1)).genHTML(out);
                    out.println("</td></tr></table");
                }
                break;
            case 3:
                if (geometry<Constants.VERTICAL) {
                    if (geometry==Constants.HORIZONTAL_UP) {
                        out.println("<div class=\"upper\" style=\"bottom:"+
                                    (100-getSplitterLocation(0))+"%\">");
                        out.println("<div class=\"left\" style=\"right:"+
                                    (100-getSplitterLocation(1))+"%\">");
                        ((HTMLViewer)components.get(0)).genHTML(out);
                        out.println("</div>");
                        out.println("<div class=\"right\" style=\"left:"+
                                    getSplitterLocation(1)+"%\">");
                        ((HTMLViewer)components.get(1)).genHTML(out);
                        out.println("</div>");
                        out.println("</div>");
                        out.println("<div class=\"lower\" style=\"top:"+
                                    getSplitterLocation(0)+"%\">");
                        ((HTMLViewer)components.get(2)).genHTML(out);
                        out.println("</div>");
                    } else {
                        out.println("<div class=\"upper\" style=\"bottom:"+
                                    (100-getSplitterLocation(0))+"%\">");
                        ((HTMLViewer)components.get(2)).genHTML(out);
                        out.println("</div>");
                        out.println("<div class=\"lower\" style=\"top:"+
                                    getSplitterLocation(0)+"%\">");
                        out.println("<div class=\"left\" style=\"right:"+
                                    (100-getSplitterLocation(1))+"%\">");
                        ((HTMLViewer)components.get(0)).genHTML(out);
                        out.println("</div>");
                        out.println("<div class=\"right\" style=\"left:"+
                                    getSplitterLocation(1)+"%\">");
                        ((HTMLViewer)components.get(1)).genHTML(out);
                        out.println("</div>");
                        out.println("</div>");
                    }
                } else {
                    if (geometry==Constants.VERTICAL_LEFT) {
                        out.println("<div class=\"left\" style=\"right:"+
                                    (100-getSplitterLocation(0))+"%\">");
                        out.println("<div class=\"upper\" style=\"bottom:"+
                                    (100-getSplitterLocation(1))+"%\">");
                        ((HTMLViewer)components.get(0)).genHTML(out);
                        out.println("</div>");
                        out.println("<div class=\"lower\" style=\"top:"+
                                    getSplitterLocation(1)+"%\">");
                        ((HTMLViewer)components.get(1)).genHTML(out);
                        out.println("</div>");
                        out.println("</div>");
                        out.println("<div class=\"right\" style=\"left:"+
                                    getSplitterLocation(0)+"%\">");
                        ((HTMLViewer)components.get(2)).genHTML(out);
                        out.println("</div>");
                    } else {
                        out.println("<div class=\"left\" style=\"right:"+
                                    (100-getSplitterLocation(0))+"%\">");
                        ((HTMLViewer)components.get(2)).genHTML(out);
                        out.println("</div>");
                        out.println("<div class=\"right\" style=\"left:"+
                                    getSplitterLocation(0)+"%\">");
                        out.println("<div class=\"upper\" style=\"bottom:"+
                                    (100-getSplitterLocation(1))+"%\">");
                        ((HTMLViewer)components.get(0)).genHTML(out);
                        out.println("</div>");
                        out.println("<div class=\"lower\" style=\"top:"+
                                    getSplitterLocation(1)+"%\">");
                        ((HTMLViewer)components.get(1)).genHTML(out);
                        out.println("</div>");
                        out.println("</div>");
                    }
                }
                break;
            case 4:
                if (geometry==Constants.VERTICAL) {
                    out.println("<div class=\"left\" style=\"right:"+
                                (100-getSplitterLocation(0))+"%\">");
                    out.println("<div class=\"upper\" style=\"bottom:"+
                                (100-getSplitterLocation(1))+"%\">");
                    ((HTMLViewer)components.get(0)).genHTML(out);
                    out.println("</div>");
                    out.println("<div class=\"lower\" style=\"top:"+
                                getSplitterLocation(1)+"%\">");
                    ((HTMLViewer)components.get(1)).genHTML(out);
                    out.println("</div>");
                    out.println("</div>");
                    out.println("<div class=\"right\" style=\"left:"+
                                getSplitterLocation(0)+"%\">");
                    out.println("<div class=\"upper\" style=\"bottom:"+
                                (100-getSplitterLocation(2))+"%\">");
                    ((HTMLViewer)components.get(2)).genHTML(out);
                    out.println("</div>");
                    out.println("<div class=\"lower\" style=\"top:"+
                                getSplitterLocation(2)+"%\">");
                    ((HTMLViewer)components.get(3)).genHTML(out);
                    out.println("</div>");
                    out.println("</div>");
                } else {
                    out.println("<div class=\"upper\" style=\"bottom:"+
                                (100-getSplitterLocation(0))+"%\">");
                    out.println("<div class=\"left\" style=\"right:"+
                                (100-getSplitterLocation(1))+"%\">");
                    ((HTMLViewer)components.get(0)).genHTML(out);
                    out.println("</div>");
                    out.println("<div class=\"right\" style=\"left:"+
                                getSplitterLocation(1)+"%\">");
                    ((HTMLViewer)components.get(1)).genHTML(out);
                    out.println("</div>");
                    out.println("</div>");
                    out.println("<div class=\"lower\" style=\"top:"+
                                getSplitterLocation(0)+"%\">");
                    out.println("<div class=\"left\" style=\"right:"+
                                (100-getSplitterLocation(2))+"%\">");
                    ((HTMLViewer)components.get(2)).genHTML(out);
                    out.println("</div>");
                    out.println("<div class=\"right\" style=\"left:"+
                                getSplitterLocation(2)+"%\">");
                    ((HTMLViewer)components.get(3)).genHTML(out);
                    out.println("</div>");
                    out.println("</div>");
                }
                break;
        }
    }
}
