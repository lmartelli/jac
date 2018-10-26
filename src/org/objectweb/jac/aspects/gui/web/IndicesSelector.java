/*
  Copyright (C) 2003 Laurent Martelli <laurent@aopsys.com>
  
  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 2 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  USA */

package org.objectweb.jac.aspects.gui.web;

import java.io.PrintWriter;
import org.apache.log4j.Logger;
import org.objectweb.jac.aspects.gui.CollectionModel;
import org.objectweb.jac.aspects.gui.DisplayContext;
import org.objectweb.jac.aspects.gui.FieldEditor;
import org.objectweb.jac.aspects.gui.GuiAC;
import org.objectweb.jac.aspects.gui.ListModel;
import org.objectweb.jac.aspects.gui.ViewFactory;
import org.objectweb.jac.core.Collaboration;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.CollectionItem;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.MethodItem;
import org.objectweb.jac.core.rtti.RttiAC;
import org.objectweb.jac.util.Strings;

/**
 * A collection editor that uses the value of an index field to select
 * objects.  
 */
public class IndicesSelector extends AbstractCollection
    implements FieldEditor, HTMLEditor
{
    static Logger logger = Logger.getLogger("gui.editor");

    CollectionItem index;
    Object repository;
    FieldItem indexedField;
    MethodItem indexNotFoundHandler;
    String indices;
    ClassItem componentType;
    ClassItem type;

    public IndicesSelector(ViewFactory factory, DisplayContext context,
                           CollectionItem collection, Object substance,
                           CollectionModel model,
                           org.objectweb.jac.aspects.gui.CollectionItemView itemView) {
        super(factory,context,collection,substance,model,itemView);
        componentType = collection.getComponentType();
        this.index = (CollectionItem)
            componentType.getAttribute(GuiAC.INDEXED_FIELD_SELECTOR);
        this.repository = GuiAC.getRepository(componentType);
        indexedField = (FieldItem)index.getAttribute(RttiAC.INDEXED_FIELD);
        this.indexNotFoundHandler = (MethodItem)
            componentType.getAttribute(GuiAC.INDEX_NOT_FOUND_HANDLER);
        indices = objectsToString();
    }

    public void setEditedType(ClassItem type) {
        this.type = type;
    }

    public void sort() {
    }

    public void updateModel(Object substance) {
        if (model!=null)
            model.close();
        model = new ListModel(collection,substance);
        indices = objectsToString();
    }

    public void commit() {
        logger.debug(this+": "+collection.getName()+
                     "'s value changed: ");
        collection.clear(substance);
        String[] keys = Strings.split(indices," ");
        for (int i=0; i<keys.length; i++) {
            if (Strings.isEmpty(keys[i].trim()))
                continue;
            Object value = index.getMap(repository,keys[i]);
            if (value==null) {
                if (indexNotFoundHandler!=null) {
                    value = indexNotFoundHandler.invokeStatic(
                        new Object[] {componentType,keys[i]});
                }
            }
            if (value!=null) {
                collection.addThroughAdder(substance,value);
            } else {
                logger.warn("No such "+collection.getComponentType()+
                            " with "+indexedField.getName()+"="+keys[i]);
            }
        }
    }

    // FieldEditor interface

    public Object getValue() {
        return null;
    }

    public void setEmbedded(boolean embedded) {
    }

    public void onSetFocus(Object param) {
    }

    // HTMLEditor interface

    public String objectsToString() {
        StringBuffer res = new StringBuffer();
        for (int i=0; i<model.getRowCount(); i++) {
            if (i!=0)
                res.append(" ");
            res.append(indexedField.getThroughAccessor(model.getObject(i)).toString());
        }
        return res.toString();
    }

    public void genHTML(PrintWriter out) {
        out.print("<input type=\"text\" name=\""+label+
                  "\" size=\"20\" style=\"width:20ex\""+
                  " value=\""+indices+"\"");
        printAttributes(out);
        out.println(">");
    }

    public boolean readValue(Object parameter) {
        indices = (String)parameter;
        /*
          key = (String)parameter;
          if (Strings.isEmpty(key)) 
          setValue(null);
          else
          setValue(index.getMap(repository,key));
        */
        return true;
    }


}

