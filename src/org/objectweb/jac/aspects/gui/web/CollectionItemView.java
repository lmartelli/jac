/*
  Copyright (C) 2002-2003 Julien van Malderen <julien@aopsys.com>
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

package org.objectweb.jac.aspects.gui.web;

import org.objectweb.jac.aspects.gui.*;
import org.objectweb.jac.core.rtti.CollectionItem;
import org.objectweb.jac.util.ExtArrays;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;

/**
 * Component used to display elements of a collection, with "prev" and
 * "next" buttons to go to the previous or next element of the
 * collection easily. Can be useful.
 *
 */

public class CollectionItemView extends AbstractView
    implements HTMLViewer, CollectionItemViewListener, AbstractCollectionItemView
{
    Object substance;
    CollectionItem collection;
    CollectionView collectionView;
    CollectionModel model;
    /** current position in collection */
    int current; 
    ObjectView objectView;
    String viewType;
    String[] viewParams;
    View hiddenView;

    /**
     * @param view the initial embedded view
     * @param coll the initial position in the collection
     * @param viewType the type of the view
     * @param hiddenView the hidden view
     */
    public CollectionItemView(View view,
                              CollectionPosition coll,
                              String viewType, String[] viewParams,
                              View hiddenView) {
        this.objectView = (ObjectView) view;
        this.collection = coll.getCollection();
        this.collectionView = coll.getCollectionView();
        this.model = this.collectionView.getCollectionModel();
        this.current = coll.getIndex();
        this.substance = coll.getSubstance();
        this.viewType = viewType;
        this.viewParams = viewParams;
        this.hiddenView = hiddenView;
    }

    public View getView()
    {
        return objectView;
    }

    public void close(boolean validate) {
        super.close(validate);
        objectView.close(validate);        
    }

    public void setCollection(CollectionItem coll) {
        collection = coll;
    }

    public CollectionItem getCollection() {
        return collection;
    }

    public void setCurrent(int index) {
        current = index;
    }

    public int getCurrent() {
        return current;
    }

    private void genPrevNext(String prev,
                             String next,
                             int total,
                             PrintWriter out) {
        out.println("<div class=\"navTool\">");

        if (prev != null)
        {
            out.println("<div class=\"prev\">");
            out.print("<a href=\""
                      + eventURL("onPreviousInCollection")
                      + "\">"
                      +  "(" + prev + ")"
                      + iconElement(ResourceManager.getResource("previous_icon"),
                                    "previous")
                      + "</a>");
            out.println("&nbsp;");
            out.println("</div>");
        }
      
        int cur = current + 1;
        out.print("[" + cur + " / " + total + "]");

        if (next != null)
        {
            out.println("<div class=\"next\">");
            out.print("<a href=\""
                      + eventURL("onNextInCollection")
                      + "\">"
                      + iconElement(ResourceManager.getResource("next_icon"),
                                    "next")
                      + "(" + next + ")"
                      + "</a>");
            out.println("</div>");
        }

        if (((View)collectionView).isClosed()) {
            out.println("<div class=\"back\">");
            out.print("<a href=\""
                      +eventURL("onBackToCollection")
                      +"\">");
            out.print(iconElement(ResourceManager.getResource("up_icon"),
                                  "back"));
            out.print("</a>");
            out.println("</div>");
        }

        if (GuiAC.isRemovable(collection)) {
            out.println("<div class=\"remove\">");
            out.print("<a href=\""
                      +eventURL("onRemoveInCollection")
                      +"\">"
                      +iconElement(ResourceManager.getResource("remove_icon"),
                                   "remove"));
            out.println("</a>");
            out.println("</div>");
        }

        out.println("</div>");
    }

    public void genHTML(PrintWriter out) throws IOException {
        if (!GuiAC.hasSetNavBar(context.getCustomizedView().getCustomizedGUI(),
                                collection)) {
            objectView.genHTML(out);
            return;
        }

        int size = model.getRowCount();

        String prevStr = 
            (current>0) ? GuiAC.toString(model.getObject(current-1)) : null;
        String nextStr = 
            (current<(size-1)) ? 
            GuiAC.toString(model.getObject(current+1)) : 
            null;

        genPrevNext(prevStr, nextStr, size, out);
        objectView.genHTML(out);
        genPrevNext(prevStr, nextStr, size, out);
    }


    public void onNextInCollection() {
        if (current < model.getRowCount()-1)
        {
            current++;
            if (collectionView!=null)
                collectionView.setSelected(current);
            Object curr = model.getObject(current);
            objectView.close(true);
            objectView = (ObjectView) factory.createView("target[?]", viewType,
                                                         ExtArrays.add(curr,viewParams),
                                                         context);
        }
        context.getDisplay().refresh();
    }

    public void onPreviousInCollection() {
        Collection col = collection.getActualCollection(substance);
        if (current > 0) {
            current--;
            if (collectionView!=null)
                collectionView.setSelected(current);
            Object curr = model.getObject(current);
            objectView.close(true);
            objectView = (ObjectView) 
                factory.createView("target[?]", viewType,
                                   ExtArrays.add(curr,viewParams),
                                   context);
        }
        context.getDisplay().refresh();
    }

    protected CompositeView findPanel() {
        View current = getParentView();
        View last = null;
        while (current!=null && !(current instanceof PanelView)) {
            last = current;
            current = current.getParentView();
        }
        return (CompositeView)last;
    }

    public void onBackToCollection() {
        CompositeView panel = findPanel();
        if (panel!=null) {
            int numRows = GuiAC.getNumRowsPerPage(collection);
            GuiAC.setStartIndex(
                collection,
                numRows>0 ? (current - current%numRows) : 0);
            try {
                panel.addView(
                    factory.createView(substance.getClass().getName(),
                                       "Object",new Object[] {"default",substance},context));
            } finally {
                GuiAC.removeStartIndex(collection);
            }
        }
        context.getDisplay().refresh();
    }

    public void onRemoveInCollection() {
        Collection col = collection.getActualCollection(substance);
        int old = current;

        if (current > 0) {
            current--;
        } else if (col.size() <= 1) {
            col.clear();
            objectView.close(true);
            onBackToCollection();
            return;
        }

        Object curr = null;
        Iterator it = col.iterator();
        for (int i=0; it.hasNext() && i<=old; i++)
            curr = it.next();

        try {
            collection.removeThroughRemover(substance,curr);
        } catch (Exception e) {
            e.printStackTrace();
            current = old;
            context.getDisplay().refresh();
            return;
        }

        Iterator it2 = col.iterator();
        for (int i=0; it2.hasNext() && i<=current; i++) {
            curr = it2.next();
        }
        objectView.close(true);
        objectView = (ObjectView) factory.createView("target[?]", viewType,
                                                     ExtArrays.add(curr,viewParams),
                                                     context);

        if (current > 0)
            collectionView.setSelected(current);

        context.getDisplay().refresh();
    }

}
