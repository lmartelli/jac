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

import java.io.PrintWriter;
import java.net.URLEncoder;
import org.apache.log4j.Logger;
import org.objectweb.jac.aspects.gui.*;
import org.objectweb.jac.core.NameRepository;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.core.rtti.CollectionItem;
import org.objectweb.jac.core.rtti.FieldItem;

/**
 * Base class for collection views (list, table, etc)
 */
public abstract class AbstractCollection extends AbstractView
    implements CollectionListener, CollectionView, HTMLViewer
{
    static Logger loggerEvents = Logger.getLogger("gui.events");
    static Logger loggerEditor = Logger.getLogger("gui.editor");

    protected CollectionItem collection;
    protected Object substance; 
    protected CollectionModel model;
    protected TableSorter sorter;
    protected TableFilter filter;

    protected boolean autoUpdate = true;
    protected boolean isEditor = true;

    /* index of selected row (-1 if no row if selection is empty) */
    int selected = -1;

    /* number of rows to display on each page */
    int rowsPerPage = 10;
    protected void setRowsPerPage(int rowsPerPage) {
        this.rowsPerPage = rowsPerPage;
        split = rowsPerPage>0;
    }

    /* do we split the collection in multiple pages */
    boolean split;

    /* do we show a column with row numbers */
    boolean showRowNumbers;
   
    /* current offset */
    int startIndex = 0;

    protected org.objectweb.jac.aspects.gui.CollectionItemView itemView;

    protected boolean viewableItems; // shortcut for itemView.isViewableItems()

    protected ObjectChooser rowsPerPageChooser;

    public AbstractCollection(ViewFactory factory, DisplayContext context,
                              CollectionItem collection, Object substance,
                              CollectionModel model, 
                              org.objectweb.jac.aspects.gui.CollectionItemView itemView) 
    {
        super(factory,context);

        this.collection = collection;
        this.substance = substance;
        this.model = model;
        setRowsPerPage(GuiAC.getNumRowsPerPage(collection));
        this.startIndex = GuiAC.getStartIndex(collection);
        this.showRowNumbers = GuiAC.isShowRowNumbers(collection);
        this.itemView = itemView;
        if (itemView!=null)
            this.viewableItems = itemView.isViewableItems();
        init();
        sort();

        int[] availableNumRowsPerPage = GuiAC.getAvailableNumRowsPerPage(collection);
        if (availableNumRowsPerPage!=null) {
            ComboBoxModel comboModel = new ComboBoxModel();
            for (int i=0; i<availableNumRowsPerPage.length; i++) {
                int n = availableNumRowsPerPage[i];
                comboModel.addObject(new Integer(n), n!=0?(""+n):GuiAC.getLabelAll());
            }
            rowsPerPageChooser = new ObjectChooser(null,null,comboModel,false);
            rowsPerPageChooser.setLabel(label+"_rowsPerPageChooser");
            comboModel.setSelectedItem(""+availableNumRowsPerPage[0]);
            setRowsPerPage(availableNumRowsPerPage[0]);
        }
    }

    public AbstractCollection() {
    }

    public void setSubstance(Object substance) {
        this.substance = substance;
    }

    public Object getSubstance() {
        return substance;
    }

    public CollectionModel getCollectionModel() {
        return model;
    }

    public boolean isEditor() {
        return isEditor;
    }

    public void setEditor(boolean isEditor) {
        this.isEditor = isEditor;
    }

    public void setAutoUpdate(boolean autoUpdate) {
        this.autoUpdate = autoUpdate;
    }

    /**
     * Generate HTML code for a collection's event (add, remove, ...)
     * @param out write HTML to this writer
     * @param event name of the associated event
     * @param icon name of icon resource to display
     * @param label text to display
     */
    void genCollectionEvent(PrintWriter out,
                            String event,
                            String icon,
                            String label) 
    {
        JacRequest request = WebDisplay.getRequest();
        if (request.isIEUserAgent()) {
            out.println(
                "<table class=\"method\"><td>"+
                iconElement(ResourceManager.getResource(icon),"")+
                eventURL(label,event,"").toString()+
                "</td></table>");
        } else {
            out.println(
                eventURL(" "+label,event,"")
                .add(0,iconElement(ResourceManager.getResource(icon),"").addCssClass("first"))
                .cssClass("method").toString());
        }
    }

    /**
     * Sorts the collection with the column index stored in the context
     * if any.
     */
    public abstract void sort();

    /**
     * Initialization to be performed before sort()
     */
    protected void init() {}

    public void onView(Object object) {
        loggerEvents.debug(toString()+".onView("+object+")");
        selected = model.indexOf(object);

        // used for CollectionItemView
        CollectionPosition extra = new CollectionPosition(
            this,
            collection,selected,substance);

        EventHandler.get().onSelection(context,collection,object,
                                       null,extra,true);
    }

    // HTMLViewer interface

	protected void genHeader(PrintWriter out) {
		genHeader(out, true);
	}
	
    /**
     * Generate HTML for adder, remover, prev/next buttons, ...
     * @param out print HTML code to this writer 
     */
    protected void genHeader(PrintWriter out, boolean div) {
        if (collection.getSubstance(substance)==null)
            return;

        boolean addable = GuiAC.isAddable(substance,collection);
        boolean removable = GuiAC.isRemovable(substance,collection);
        boolean refreshable = showRefreshButton();
        if (!(addable || refreshable ||
              (model.getRowCount()>0 && removable) ||
              (startIndex>0) ||
              (split && startIndex+rowsPerPage<model.getRowCount()))) {
            return;
        }
        if (div) {
            out.println("<div class=\"methods\">");
        }
        if (addable && isEditor) {
            if (GuiAC.isAutoCreate(collection)) {
                genCollectionEvent(out,"onAddToCollection","new_icon",GuiAC.getLabelAdd());
                /*
                  genCollectionEvent(out,"onAddToCollection","new_icon","add new");
                  genCollectionEvent(out,"onAddExistingToCollection","new_icon","add existing");
                */
            } else {
                genCollectionEvent(out,"onAddToCollection","new_icon",GuiAC.getLabelAdd());
            }
        }
        if (startIndex>0) {
            out.println("<a href=\""+eventURL("onFirst")+"\">"+
                        iconElement(ResourceManager.getResource("first_icon"),"first")+
                        "</a>");
            out.println("<a href=\""+eventURL("onPrevious")+"\">"+
                        iconElement(ResourceManager.getResource("previous_icon"),"previous")+
                        "</a>");
        }
        if (rowsPerPage>0 && model.getRowCount()>0 && 
            (startIndex>0 || startIndex+rowsPerPage<model.getRowCount())) {
            out.println("["+(startIndex+1)+"-"+
                        Math.min(startIndex+rowsPerPage,model.getRowCount())+"/"+
                        model.getRowCount()+"]");
        }
        if (split && startIndex+rowsPerPage<model.getRowCount()) {
            out.println("<a href=\""+eventURL("onNext")+"\">"+
                        iconElement(ResourceManager.getResource("next_icon"),"next")+
                        "</a>");
            out.println("<a href=\""+eventURL("onLast")+"\">"+
                        iconElement(ResourceManager.getResource("last_icon"),"last")+
                        "</a>");
        }
        if (split && model.getRowCount()>rowsPerPage) {         
            out.println("["+(startIndex/rowsPerPage+1)+"/"+
                        (int)Math.ceil((float)model.getRowCount()/(float)rowsPerPage)+"]");
        }

        if (rowsPerPageChooser!=null) {
            out.print("Display ");
            rowsPerPageChooser.genHTML(out);
            out.print(" rows");
        }
        if (showRefreshButton())
            genEventAndActionButton(out,"onRefreshCollection");

		if (div) {
            out.println("</div>");
		}
    }

    /**
     * Tells whether a refesh button must be shown
     */
    protected boolean showRefreshButton() {
        return rowsPerPageChooser!=null;
    }

    /**
     * Gets object at a given index
     */
    protected Object getObject(int index) {
        return model.getObject(index);
    }

    // CollectionView interface

    public void setSelected(int index) {
        selected = index;      
    }

    public void setField(FieldItem field) {
        collection = (CollectionItem)field;
    }

    public FieldItem getField() {
        return collection;
    }

    public void setValue(Object value) {
    }

    public void updateModel(Object substance) {
    }

    // CollectionListener interface

    public void onView(int index) {
        loggerEvents.debug(toString()+".onView("+index+")");
        selected = index;

        // used for CollectionItemView
        CollectionPosition extra = new CollectionPosition(
            this,
            collection,index,substance);

        EventHandler.get().onSelection(context,collection,getObject(index),
                                       null,extra,true);
        loggerEvents.debug("  ending "+toString()+".onView("+index+"), selected="+selected);
    }

    public void onViewObject(String name) {
        Object object = NameRepository.get().getObject(name);
        EventHandler.get().onSelection(context,collection,object,
                                       null,null,true);
    }

    public void onRemove(int index) {
        loggerEvents.debug(toString()+".onRemove("+index+")");
        Object toRemove = getObject(index);
      
        EventHandler.get()
            .onRemoveFromCollection(
                context, 
                new RemoveEvent(
                    this,
                    collection.getSubstance(substance), 
                    collection, 
                    toRemove), 
                false);
    }

    public void onTableInvoke(int index,String methodName) {
        loggerEvents.debug(toString()+".onTableInvoke("+index+")");
        selected = index;
        EventHandler.get().onInvoke(
            context,
            new InvokeEvent(
                this,
                getObject(index),
                ClassRepository.get().getClass(getObject(index)).getMethod(methodName)));
    }
   
    public void onAddToCollection() {
        EventHandler.get().onAddToCollection(
            context,
            new AddEvent(
                this,
                collection.getSubstance(substance),
                collection,
                null),
            false);
        sort();
    }

    public void onAddExistingToCollection() {
        EventHandler.get().onAddToCollection(
            context,
            new AddEvent(this,collection.getSubstance(substance),collection,null),
            true);
        sort();
    }

    public void onRemoveFromCollection() {
        EventHandler.get().onRemoveFromCollection(
            context,
            new RemoveEvent(this,collection.getSubstance(substance),collection,null),
            true);
        sort();
    }

    public void onNext() {
        if (startIndex+rowsPerPage<model.getRowCount())
            startIndex += rowsPerPage;
        context.getDisplay().refresh();
    }

    public void onLast() {
        startIndex = model.getRowCount() - model.getRowCount()%rowsPerPage;
        if (startIndex==model.getRowCount())
            startIndex -= rowsPerPage;
        context.getDisplay().refresh();
    }

    public void onPrevious() {
        startIndex -= rowsPerPage;
        if (startIndex<0)
            startIndex = 0;
        context.getDisplay().refresh();
    }

    public void onFirst() {
        startIndex = 0;
        context.getDisplay().refresh();
    }

    public void onRefreshCollection() {
        if (rowsPerPageChooser!=null) {
            rowsPerPageChooser.readValue(
                ((WebDisplay)context.getDisplay())
                	.getRequest().getParameter(rowsPerPageChooser.getLabel()));
            setRowsPerPage(((Number)rowsPerPageChooser.getValue()).intValue());
        }
        context.getDisplay().refresh();
    }
    
    /**
     * Ensures that startIndex is not ouf of range
     */
    protected void checkRange() {
        if (startIndex>=model.getRowCount()) {
            if (split) {
                startIndex = model.getRowCount() - model.getRowCount()%rowsPerPage;
                if (startIndex==model.getRowCount())
                    startIndex = 0;
            } else
                startIndex = 0;
        }
    }

    /**
     * Returns an HTML link to remove the element at a given position.
     */
    protected String removeLink(int position) {
        // We should use eventURL
        return "<a href=\""+eventURL("onRemove")+
            "&amp;index="+position+"\">"+
            iconElement(ResourceManager.getResource("remove_icon"),"remove")+"</a>";
    }

    protected String viewLink(int position) {
        // We should use eventURL
        return "<a href=\""+eventURL("onView")+
            "&amp;index="+position+"\">"+
            iconElement(ResourceManager.getResource("view_icon"),"view")+"</a>";
    }

    /**
     * Build an HTML link with an image showing if a column is used to
     * sort the collection
     * @param column the index of the column 
     * @param text additional text to put in the link
     * @return the HTML code of the link
     */
    protected String sortLink(int column,String text) {
        SortCriteria criteria = sorter.getSortCriteria(column);
        String iconResource;
        if (criteria==null) {
            iconResource = "small_down_icon";
        } else {
            if (criteria.isAscending()) 
                iconResource = "small_down_icon_selected";
            else 
                iconResource = "small_up_icon_selected";
        }
        return 
            "<a href=\""+eventURL("onHeaderClick")+"&amp;col="+column+"\""
            + " title=\"sort\">"
            + iconElement(ResourceManager.getResource(iconResource),"sort")
            + " " + text
            + "</a>";
    }

    // When disabled == true, readValue() does nothing
    boolean disabled = false;
    public boolean isEnabled() {
        return !disabled;
    }
    public void setEnabled(boolean enabled) {
        loggerEditor.debug((enabled?"enable ":"disable ")+this);
        this.disabled = !enabled;
    }
}
