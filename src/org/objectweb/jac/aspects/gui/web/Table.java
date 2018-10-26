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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.objectweb.jac.aspects.gui.*;
import org.objectweb.jac.core.Naming;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.CollectionItem;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.MemberItem;
import org.objectweb.jac.core.rtti.MethodItem;
import org.objectweb.jac.util.ExtArrays;

public class Table extends AbstractCollection
    implements HTMLViewer, TableListener
{
    static Logger logger = Logger.getLogger("gui.table");
    static Logger loggerEvents = Logger.getLogger("gui.events");

    HTMLViewer[] cellViewers;
    ExtendedTableModel tableModel;

    public Table(ViewFactory factory, DisplayContext context,
                 CollectionItem collection, Object substance,
                 ExtendedTableModel model, 
                 org.objectweb.jac.aspects.gui.CollectionItemView itemView) {
        super(factory,context,collection,substance,model,itemView);
        this.tableModel = model;
        this.multiLineCollection = itemView.getMultiLineCollection();
        this.groupBy = itemView.getGroupBy();
        if (model!=null) {
            this.cellViewers = new HTMLViewer[tableModel.getHeaders().length];
            setCellRenderers();
        }
    }

    CollectionItem multiLineCollection;
    FieldItem groupBy;

    public void setColumnsInfo(String[] headers, FieldItem[] fields, 
                               ClassItem[] classes, ClassItem[] viewerClasses) {
        if (!(headers.length==classes.length && 
              classes.length==viewerClasses.length &&
              viewerClasses.length==fields.length)) {
            throw new RuntimeException("headers, fields, classes and "+
                                       "viewerClasses must be the same size");
        }
        this.cellViewers = new HTMLViewer[headers.length];
    }

    protected void setCellRenderers() {
        MemberItem[] members = tableModel.getMembers();
        String[] headers = tableModel.getHeaders();

        for (int i=0; i<members.length; i++) {
            if (members[i] instanceof FieldItem) {
                cellViewers[i] = 
                    (HTMLViewer)tableModel.getCellRenderer(this,i,factory,context);
            } else if (members[i] instanceof MethodItem) {
                MethodItem method = (MethodItem)members[i];
                try {
                    cellViewers[i] = (HTMLViewer)factory.createView(
                        method.getName(),
                        "Method",
                        new Object[] {substance,method},context);
                } catch (Exception e) {
                    logger.error("Failed to instanciate TableCellRenderer "+
                                 method.getName()+" for column "+headers[i],e);
                }
            }
            if (cellViewers[i] instanceof FieldView)
                ((FieldView)cellViewers[i]).setAutoUpdate(false);
            if (cellViewers[i] instanceof CollectionView)
                ((CollectionView)cellViewers[i]).setEditor(false);
        }      
    }

    public void sort() {
        if (sorter!=null)
            sorter.sort(this);
    }

    protected boolean showRefreshButton() {
        return super.showRefreshButton() 
            || (filterEditors!=null && !filterEditors.isEmpty());
    }

    public void onRefreshCollection() {
        if (filterEditors != null) {
            JacRequest request = ((WebDisplay)context.getDisplay()).getRequest();        
            Iterator it = filterEditors.values().iterator();
            while (it.hasNext()) {
                ObjectChooser chooser = (ObjectChooser)it.next();
                chooser.readValue(request.getParameter(chooser.getLabel()));
            }
        }
        checkRange();
        super.onRefreshCollection();
    }

    protected void init() {
        if (model!=null) {
            sorter = ((ExtendedTableModel)model).getSorter();
            filter = ((ExtendedTableModel)model).getFilter();
            if (filter!=null) {
                filterEditors = filter.buildFilterEditors(factory,context);
                Iterator it = filterEditors.values().iterator();
                while (it.hasNext()) {
                    ObjectChooser chooser = (ObjectChooser)it.next();
                    chooser.setParentView(this);
                }
            }
        }
    }

    // FieldItem -> HTMLViewer
    Map filterEditors;

    /**
     * Removes editors of embedded added object
     */
    protected void clearFilterEditors() {
        if (filterEditors!=null) {
            Iterator it = filterEditors.values().iterator();
            while (it.hasNext()) {
                FieldEditor editor = (FieldEditor)it.next();
                context.removeEditor(editor);
            }
            filterEditors.clear();
        }
    }

    /**
     * Sorts the collection.
     *
     * @param column the index of the column used to sort
     */
    public void sort(int column) {
        sorter.sortByColumn(column, true);
    }

    // HTMLViewer interface

    public void genHTML(PrintWriter out) throws IOException {
        MethodItem adder = collection.getAdder();
        boolean embeddedAdder = 
            adder!=null && GuiAC.getView(adder,itemView.getName()).isEmbedded() && 
            GuiAC.isAutoCreate(collection);
        if (model.getRowCount()==0 && !embeddedAdder) {
            out.println("none");
        }

        genHeader(out);

        if (model.getRowCount()==0 && !embeddedAdder && filter==null) {
            return;
        }

        logger.debug(itemView+".Embedded editors="+itemView.isEmbeddedEditors());

        String[] headers = tableModel.getHeaders();

        out.println("<table class=\"table\">");
        out.println("  <thead>");
        out.println("    <tr>");
        if (showRowNumbers) {
            out.println("      <th style=\"width:"+
                        ((""+model.getRowCount()).length())+"ex\" class=\"empty\"></th>");
        }
        for (int i=0; i<headers.length; i++) {
            if (headers[i]!=null) {
                out.print("      <th>");
                out.print(sortLink(i,headers[i]));
                out.println("</th>");
            } else {
                out.print("      <th class=\"empty\"></th>");
            }
        }
        if (viewableItems)
            out.println("      <th style=\"width:1px\" class=\"empty\"></th>"); // view button
        if (GuiAC.isRemovable(collection))
            out.println("      <th style=\"width:1px\" class=\"empty\"></th>"); // remove button
        out.println("    </tr>");
        out.println("  </thead>");

        boolean bodyOpened = false;

        Object defaultsObject = null;
   
        // Column filters
        if (filter!=null) {
            genColumnFilters(out);
        }

        bodyOpened = false;
        // Default value editors in the table
        if (GuiAC.hasEditableDefaultValues(collection) && embeddedAdder) {
            try {
                ClassItem componentType = collection.getComponentType();
                defaultsObject = Naming.getObject("defaults."+collection.getLongName());
                if (defaultsObject==null) {
                    Naming.setName("defaults."+collection.getLongName());
                    defaultsObject = componentType.newInstance();
                    clearDefaultEditors();
                }
                out.println("  <tbody class=\"defaultObject\">");
                out.println("    <tr class=\"vspace\"></tr>");
                out.println("    <tr class=\"defaultObject\">");
                FieldItem[] defaultsFields = GuiAC.getDefaultsAttributesOrder(componentType);
                if (showRowNumbers) {
                    out.println("      <td></td>");
                }
                for (int col=0; col<tableModel.getColumnCount(); col++) {
                    out.print("      <td>");
                    MemberItem member = tableModel.getMembers()[col];
                    if (defaultsFields!=null && !ExtArrays.contains(defaultsFields,member)) {
                        out.println("</td>");
                        continue;
                    }
                    GuiAC.pushGraphicContext(member);
                    try {
                        if (member instanceof FieldItem && 
                            !(member instanceof CollectionItem)) 
                        {
                            MethodItem setter = ((FieldItem)member).getSetter();
                            if (GuiAC.isCreationAttribute((FieldItem)member) &&
                                setter!=null) {
                                // <BAD>WE SHOULD ONLY BUILD THE EDITORS ONCE</BAD>
                                FieldEditor editor = GenericFactory.getEditorComponent(
                                    factory, context, defaultsObject, setter, 0, true, null);
                                defaultsEditors.add(editor);
                                context.addEditor(editor);
                                ((HTMLViewer)editor).genHTML(out);
                                editor.setEmbedded(true);
                                //editorContainer.addEditor(editor);
                                //container.addView(editor);
                            }
                        } else if (member instanceof CollectionItem) {
                            if (GuiAC.isCreationAttribute((FieldItem)member)) {
                                CollectionItem collection = (CollectionItem)member;
                                HTMLViewer view = (HTMLViewer)
                                    GenericFactory.getCollectionPane(
                                        factory,context,defaultsObject,null,null,collection);
                                view.genHTML(out);
                            }
                        }
                    } finally {
                        GuiAC.popGraphicContext();
                    }
                    out.println("</td>");
                }
                out.println("      <td>"+eventURL("set","onSetDefaults","")+"</td>");
                out.println("    </tr>");
                out.println("    <tr class=\"vspace\"></tr>");
                out.println("  </tbody>");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        logger.debug("multiLineCollection="+
                     (multiLineCollection!=null?multiLineCollection.getName():"none"));

        // Actual rows
        Object currentGroup = null;
        int groupSpan = 1;
        MemberItem[] members = tableModel.getMembers();
        boolean first = true; // First line of multi-line collection or groupBy?
        int groupIndex = 0;
        for (int index=startIndex; 
             (!split || index<startIndex+rowsPerPage) && index<model.getRowCount(); 
             index++) 
        {
            Object substance = tableModel.getObject(index);
            if (!bodyOpened) {
                openTBody(out);
                bodyOpened = true;
            }
            if (multiLineCollection!=null) {
                first = true; // First line of multi-line collection ?
                Collection multi = multiLineCollection.getActualCollectionThroughAccessor(substance);
                String rowspan = " rowspan=\""+multi.size()+"\"";
                Iterator it = multi.iterator();
                if (it.hasNext()) {
                    while (it.hasNext()) {
                        if (first)
                            groupIndex++;
                        openRow(out,index,groupIndex%2==0);
                        Object multiSubstance = it.next();
                        if (first && showRowNumbers) {
                            out.println("      <td class=\"index\""+rowspan+">"+(index+1)+"</td>");
                        }
                        for (int col=0; col<tableModel.getColumnCount(); col++) {
                            MemberItem member = members[col];
                            if (member instanceof FieldItem 
                                && ((FieldItem)member).startsWith(multiLineCollection)) {
                                FieldItem multiField = 
                                    ((FieldItem)member).getRelativeField(multiLineCollection);
                                genCell(out,index,col,multiSubstance,
                                        multiField,
                                        multiField.getThroughAccessor(multiSubstance),
                                        "");
                            } else if (first) {
                                genCell(out,index,col,substance,member,
                                        tableModel.getValueAt(index,col),rowspan);
                            }
                        }
                        if (first) {
                            if (viewableItems)
                                genViewCell(out,index,rowspan);
                            genRemoveCell(out,index,rowspan);
                        }
                        out.println("    </tr>");
                        first = false;
                    }
                } else {
                    genRow(out,index,substance,members);
                }
            } else if (groupBy!=null) {
                Object groupValue = groupBy.getThroughAccessor(substance);
                if (currentGroup!=groupValue) {
                    logger.debug(index+": New group "+groupValue);
                    currentGroup = groupValue;
                    groupSpan = 1;
                    first = true;
                    for (int i=index+1; 
                         (!split || i<startIndex+rowsPerPage) && i<model.getRowCount(); 
                         i++) { 
                        if (groupBy.getThroughAccessor(tableModel.getObject(i))!=currentGroup)
                            break;
                        groupSpan++;
                    }
                }
                if (first)
                    groupIndex++;
                openRow(out,index,groupIndex%2==1);
                if (showRowNumbers) {
                    out.println("      <td class=\"index\">"+(index+1)+"</td>");
                }
                for (int col=0; col<tableModel.getColumnCount(); col++) {
                    MemberItem member = members[col];
                    if (member instanceof FieldItem 
                        && (((FieldItem)member).startsWith(groupBy) || member==groupBy)) {
                        if (first) {
                            String rowspan = groupSpan>1 ? (" rowspan=\""+groupSpan+"\"") : "";
                            genCell(out,index,col,substance,member,
                                    tableModel.getValueAt(index,col),rowspan);
                        }
                    } else {
                        genCell(out,index,col,substance,member,
                                tableModel.getValueAt(index,col),"");
                    }
                }
                if (viewableItems)
                    genViewCell(out,index,"");
                genRemoveCell(out,index,"");
                out.println("    </tr>");
                first = false;
            } else {
                genRow(out,index,substance,members);
            }

        }
        if (bodyOpened) {
            out.println("  </tbody>");
            bodyOpened = false;
        }

        // Embedded adder in the table
        String focusId = null; // Id of HTML element to focus (if any)
        if (GuiAC.isAddable(substance,collection)) {
            try {
                ClassItem componentType = collection.getComponentType();
                if (embeddedAdder) {
                    if (addedObject==null) {
                        addedObject = componentType.newInstance();
                        EventHandler.initAutocreatedObject(
                            addedObject,substance,collection);
                        initAddedObject(addedObject,defaultsObject);
                        clearEmbeddedEditors(true);
                    }
                    out.println("  <tbody>");
                    out.println("    <tr class=\"vspace\"></tr>");
                    out.println("    <tr class=\"addedObject\">");
                    if (showRowNumbers) {
                        out.println("      <td></td>");
                    }
                    for (int col=0; col<tableModel.getColumnCount(); col++) {
                        out.print("      <td>");
                        MemberItem member = tableModel.getMembers()[col];
                        GuiAC.pushGraphicContext(member);
                        String keyPressEvent = 
                            "return commitFormOnEnter(event,this,'event=onAddEmbedded&amp;source="+getId()+"')\"";
                        try {
                            if (member instanceof FieldItem && 
                                !(member instanceof CollectionItem))
                            {
                                MethodItem setter = ((FieldItem)member).getSetter();
                                if (GuiAC.isCreationAttribute((FieldItem)member) && 
                                    setter!=null) {
                                    FieldEditor editor = GenericFactory.getEditorComponent(
                                        factory, context, addedObject, setter, 0, true, null);
                                    if (focusId==null)
                                        focusId = editor.getLabel();
                                    embeddedEditors.add(editor);
                                    ((HTMLEditor)editor).setAttribute("onkeypress",keyPressEvent);
                                    ((HTMLViewer)editor).genHTML(out);
                                    editor.setEmbedded(true);
                                    context.addEditor(editor);
                                    //editorContainer.addEditor(editor);
                                    //container.addView(editor);
                                }
                            } else if (member instanceof CollectionItem) {
                                if (GuiAC.isCreationAttribute((FieldItem)member)) {
                                    CollectionItem coll = (CollectionItem)member;
                                    CollectionItem index = (CollectionItem)
                                        coll.getComponentType().getAttribute(GuiAC.INDEXED_FIELD_SELECTOR);
                                    if (index==null) {
                                        HTMLViewer view = (HTMLViewer)
                                            GenericFactory.getCollectionPane(
                                                factory,context,addedObject,null,null,coll);
                                        view.genHTML(out);
                                    } else {
                                        FieldEditor editor = factory.createEditor(
                                            "editor "+Naming.getName(substance)+"."+coll.getName(),
                                            "IndicesSelector",
                                            new Object[] {coll,addedObject,
                                                          new ListModel(coll,addedObject),
                                                          GuiAC.getView(coll,itemView.getName())},
                                            context);
                                        embeddedEditors.add(editor);
                                        ((HTMLEditor)editor).setAttribute("onkeypress",keyPressEvent);
                                        ((HTMLViewer)editor).genHTML(out);
                                        editor.setEmbedded(true);
                                        context.addEditor(editor);
                                    }
                                }
                            }
                        } catch(Exception e) {
                            logger.error("Failed to gen HTML for embedded object to add, column "+
                                         tableModel.getHeaders()[col],e);
                        } finally {
                            GuiAC.popGraphicContext();
                        }
                        out.println("</td>");
                    }
                    out.println("      <td>"+eventURL(GuiAC.getLabelAdd(),"onAddEmbedded","")+"</td>");
                    out.println("    </tr>");
                    out.println("  </tbody>");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        FieldItem additionalRow = itemView.getAdditionalRow();
        if (additionalRow!=null) {
            out.println("  <tbody class=\"additionalRow\">");
            out.println("    <tr>");
            if (showRowNumbers) {
                out.println("      <td class=\"empty\"></td>");
            }
            Object row = additionalRow.getThroughAccessor(substance);
            for (int col=0; col<tableModel.getColumnCount(); col++) {
                MemberItem member = members[col];
                if (member instanceof FieldItem && row!=null) {
                    genCell(out,-1,col,row,member,((FieldItem)member).getThroughAccessor(row),"");
                } else {
                    genCell(out,-1,col,row,member,null,"");
                }
            }
            if (viewableItems) {
                out.println("      <td class=\"empty\"></td>");
            }
            if (GuiAC.isRemovable(collection)) {
                out.println("      <td class=\"empty\"></td>");
            }
            out.println("    </tr>");            
            out.println("  </tbody>");
        }

        out.println("</table>");

        if (focusId!=null) {
            out.println("<script type=\"text/javascript\">"+
                        "element=getElementById('"+focusId+"');"+
                        "element.focus();"+
                        "window.scroll(0,10000);"+
                        "</script>");
        }
    }

    protected void genRow(PrintWriter out, int index, Object substance,
                          MemberItem[] members) {
        openRow(out,index,index%2==0);
        if (showRowNumbers) {
            out.println("      <td class=\"index\">"+(index+1)+"</td>");
        }
        for (int col=0; col<tableModel.getColumnCount(); col++) {
            MemberItem member = members[col];
            if (multiLineCollection!=null 
                && member instanceof FieldItem 
                && ((FieldItem)member).startsWith(multiLineCollection)) {
                out.println("      <td></td>");
            } else {
                genCell(out,index,col,substance,member,
                        tableModel.getValueAt(index,col),"");
            }
        }
        if (viewableItems)
            genViewCell(out,index,"");
        genRemoveCell(out,index,"");
        out.println("    </tr>");
    }

    protected void genCell(PrintWriter out, int index, int col, 
                           Object substance, MemberItem member, Object value,
                           String rowspan) 
    {
        out.print("      <td"+rowspan+">");
        try {
            HTMLViewer viewer = null;
            if (member instanceof FieldItem) {
                FieldItem field = (FieldItem)member;
                if (itemView.isEmbeddedEditors(field))
                    viewer = getFieldEditor(field,substance);
            }
            if (viewer!=null) {
                viewer.genHTML(out);
            } else {
                if (cellViewers[col] instanceof Method) {
                    out.println("      <a href=\""+eventURL("onTableInvoke")+
                                "&amp;index="+index+"&amp;method="+
                                (member).getName()+"\">"+
                                (GuiAC.getLabel(member))+"</a>");
                  
                } else {
                    FieldView cellViewer = (FieldView)cellViewers[col];
                    if (cellViewer!=null) {
                        if (cellViewer instanceof TableCellViewer) {
                            ((TableCellViewer)cellViewer).setRow(index);
                            ((TableCellViewer)cellViewer).setColumn(col);
                        }
                        if (cellViewer instanceof LinkGenerator) {
                            ((LinkGenerator)cellViewer).setEnableLinks(itemView.areLinksEnabled());
                        }
                        if (cellViewer instanceof CollectionView) 
                            ((CollectionView)cellViewer).updateModel(substance);
                        else {
                            cellViewer.setSubstance(substance);
                            cellViewer.setValue(value);
                        }
                        if (cellViewer instanceof ReferenceView) {
                            ((ReferenceView)cellViewer).setEventURL(
                                eventURL("onCellSelection")+"&amp;row="+index+"&amp;col="+col);
                        }
                        ((HTMLViewer)cellViewer).genHTML(out);
                    } else {
                        if (value!=null) {
                            out.print(GuiAC.toString(value));
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(
                "Failed to genHTML for cell["+
                tableModel.getHeaders()[col]+","+index+
                "] of "+collection.getName(),e);
        }
        out.println("</td>");
    }

    protected void genColumnFilters(PrintWriter out) throws IOException {
        out.println("  <tbody class=\"filters\">");
        out.println("    <tr class=\"filters\">");
        if (showRowNumbers) {
            out.println("      <td></td>");
        }
        for (int col=0; col<tableModel.getColumnCount(); col++) {
            MemberItem member = tableModel.getMembers()[col];
            if (member instanceof FieldItem &&
                filter.isFiltered((FieldItem)member)) {
                out.print("      <td>");
                ((HTMLViewer)filterEditors.get(member)).genHTML(out);
                out.println("      </td>");
            } else {
                out.println("      <td></td>");
            }
        }
        out.println("    </tr>");
        out.println("  </tbody>");            
    }

    protected void genViewCell(PrintWriter out, int index, String rowspan) {
        out.print("      <td"+rowspan+">");
        if (collection.getAttribute(GuiAC.NEW_WINDOW)!=null) {
            out.print("<a target=\""+collection.getName()+"\""+
                      " href=\""+eventURL("onView")+
                              "&amp;index="+index+"&amp;\">"+
                      "details</a>");
        } else {
            out.print(viewLink(index));
        }
        out.println("</td>");
    }

    protected void genRemoveCell(PrintWriter out, int index, String rowspan) {
        if (GuiAC.isRemovable(collection)) {
            out.print("<td"+rowspan+">");
            if (isEditor)
                out.print(removeLink(index));
            out.println("</td>");
        }
    }

    protected boolean viewOnDoubleClick = true;
    /**
     * Print opening tag for a row
     *
     * @param out where to wrte the HTML code
     * @param index index of the row to open
     * @param even wether the <tr> should have the "even" or "odd" class
     */
    protected void openRow(PrintWriter out, int index, boolean even) {
        String event = "";
        String cls = "";
        if (viewOnDoubleClick && viewableItems) {
            event = "ondblclick=\"openURL('"+eventURL("onView")+"&amp;index="+index+"')\"";
            cls = " highlight";
        }
        if (selected==index)
            out.println("    <tr class=\"selected"+cls+"\" "+event+">");
        else
            out.println("    <tr class=\""+
                        (even?"even":"odd")+cls+"\" "+event+">");
    }

    /**
     * Print opening TBODY tag containg rows
     * @param out
     */
    protected void openTBody(PrintWriter out) {
        out.println("  <tbody"+((viewOnDoubleClick&&viewableItems)?" class=\"highlight\"":"")+">");
    }

    /**
     * Returns a cell editor for a field of an object. Editors are
     * cached, so you'll always get the same object for the same field
     * and substance, unless {@link #clearCellEditors(boolean)} is called.
     *
     * @param field field to get an editor for
     * @param substance object holding the field
     *
     * @see #clearCellEditors(boolean)
     */
    protected HTMLViewer getFieldEditor(FieldItem field, Object substance) {
        HTMLViewer editor = null;
        Map fieldEditors = (Map)cellEditors.get(field);
        if (fieldEditors!=null) {
            editor = (HTMLViewer)fieldEditors.get(substance);
        } else {
            fieldEditors = new HashMap();
            cellEditors.put(field,fieldEditors);
        }
        if (editor==null) {
            if (!(field instanceof CollectionItem))
            {
                MethodItem setter = field.getSetter();
                if (setter!=null) {
                    editor = (HTMLViewer)GenericFactory.getEditorComponent(
                        factory, context, field.getSubstance(substance), setter, 0, true, null);
                }
            } else  {
                CollectionItem coll = (CollectionItem)field;
                CollectionItem index = (CollectionItem)
                    coll.getComponentType().getAttribute(GuiAC.INDEXED_FIELD_SELECTOR);
                if (index==null) {
                    editor = (HTMLViewer)GenericFactory.getCollectionPane(
                        factory,context,substance,null,itemView,coll);
                } else {
                    editor = (HTMLViewer)factory.createEditor(
                        "editor "+Naming.getName(substance)+"."+coll.getName(),
                        "IndicesSelector",
                        new Object[] {coll,substance,
                                      new ListModel(coll,substance),
                                      GuiAC.getView(coll,itemView.getName())},
                        context);
                }
            }
            ((View)editor).setParentView(this);
            fieldEditors.put(substance,editor);
            if (editor instanceof FieldEditor) {
                ((FieldEditor)editor).setEmbedded(true);
                context.addEditor((FieldEditor)editor);
            }
            if (editor instanceof ReferenceEditor) {
                ((ReferenceEditor)editor).setEditable(false);
            }
        }
        if (editor instanceof HTMLEditor) {
            ((HTMLEditor)editor).setAttribute("ondblclick","event.stopPropagation();");
        }
        logger.debug("editor["+field.getName()+","+substance+"] -> "+editor);
        return editor;
    }

    /**
     * Removes editors of embedded added object
     */
    protected void clearEmbeddedEditors(boolean validate) {
        Iterator it = embeddedEditors.iterator();
        while (it.hasNext()) {
            FieldEditor editor = (FieldEditor)it.next();
            editor.close(validate);
            context.removeEditor(editor);
        }
        embeddedEditors.clear();
    }

    /**
     * Removes editors of embedded added object
     */
    protected void clearDefaultEditors() {
        Iterator it = defaultsEditors.iterator();
        while (it.hasNext()) {
            FieldEditor editor = (FieldEditor)it.next();
            context.removeEditor(editor);
        }
        defaultsEditors.clear();
    }

    // FieldItem -> (Object -> FieldEditor)
    Hashtable cellEditors = new Hashtable();

    /**
     * Removes editors of embedded added object
     */
    protected void clearCellEditors(boolean validate) {
        Iterator it = cellEditors.values().iterator();
        while (it.hasNext()) {
            Iterator editors = ((Map)it.next()).values().iterator();
            while (editors.hasNext()) {
                View editor = (View)editors.next();
                if (editor!=null)
                    editor.close(validate);
                context.removeEditor(editor);
            }
        }
        cellEditors.clear();
    }

    public void close(boolean validate) {
        super.close(validate);
        clearEmbeddedEditors(validate);
        clearDefaultEditors();
        clearCellEditors(validate);
        clearFilterEditors();
    }

    /**
     * Initialize fields of added object from one of defaultsObject
     */
    protected void initAddedObject(Object addedObject, Object defaultsObject) {
        if (defaultsObject==null || addedObject==null) {
            return;
        }
        for (int col=0; col<tableModel.getColumnCount(); col++) {
            MemberItem member = tableModel.getMembers()[col];
            if (member instanceof FieldItem && !(member instanceof CollectionItem)) {
                FieldItem field = (FieldItem)member;
                MethodItem setter = field.getSetter();
                if (GuiAC.isCreationAttribute(field) && setter!=null) {
                    Object value = field.getThroughAccessor(defaultsObject);
                    try {
                        field.set(addedObject,value);
                    } catch (Exception e) {
                        logger.error("Failed to set default value for field "+field,e);
                    }
                }
            }
        }
    }

    public void onCellSelection(int row, int col) {
        MemberItem[] members = tableModel.getMembers();
        EventHandler.get().onSelection(context,(FieldItem)members[col],
                                       tableModel.getObject(row,col),
                                       null,null,true);
    }

    List embeddedEditors = new Vector();
    Object addedObject = null;

    public void onEmbeddedAddToCollection() {
        JacRequest request = WebDisplay.getRequest();
        loggerEvents.debug("onEmbeddedAddToCollection "+collection.getName()+" "+request);
        for (int i=0; i<embeddedEditors.size(); i++) {
            FieldEditor editor = (FieldEditor)embeddedEditors.get(i);
            editor.close(true);
        }
        EventHandler.get().onInvoke(
            context,
            new InvokeEvent(
                this,
                substance,collection.getAdder(),
                new Object[] {addedObject}),
            false,
            null,null);
        clearEmbeddedEditors(true);
        /**
           // onView(addedObject); 

           It does not work because 

           a) onInvoke() above is asynchronous and starts its own
           thread, so we are not sure addedObject is added
           "completely" (the table model may not be up to date for
           instance)

           b) InvokeThread already calls WebDisplay.refresh(), so
           calling it again in onView() will fail

        */
        addedObject = null;
    }
   
    List defaultsEditors = new Vector();
   
    public void onSetDefaults() {
        loggerEvents.debug("onSetDefaults "+collection.getName());
        initAddedObject(addedObject,
                        Naming.getObject("defaults."+collection.getLongName()));
        context.getDisplay().refresh();
    }
   
    public void onHeaderClick(int column) {
        SortCriteria criteria = sorter.getSortCriteria(column);
        if (criteria!=null) {
            if (criteria.isAscending()) {
                criteria.toggleAscending();
                sorter.sortByColumn(column,criteria.isAscending());
            } else {
                sorter.sortByColumn(-1,criteria.isAscending());            
            }
        } else {
            sorter.sortByColumn(column,true);
        }
        context.getDisplay().refresh();
    }

    public View onRowEvent(int row, int col) {
        FieldView cellViewer = 
            (FieldView)tableModel.getCellRenderer(this,col,factory,context);
        cellViewer.setValue(tableModel.getValueAt(row,col));
        return cellViewer;
    }

}
