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

import org.objectweb.jac.core.rtti.CollectionItem;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.MethodItem;
import org.objectweb.jac.core.rtti.RttiAC;
import org.objectweb.jac.util.Strings;
import java.io.PrintWriter;

/**
 * A reference editor that uses the value of an index field to select
 * objects.
 */
public class IndexSelector extends AbstractFieldEditor 
    implements HTMLEditor
{
    CollectionItem index;
    FieldItem indexedField;
    Object repository;
    String key;
    MethodItem indexNotFoundHandler;

    public IndexSelector(Object substance, FieldItem field, 
                         CollectionItem index, Object repository, 
                         boolean allowCreation,
                         MethodItem indexNotFoundHandler) {
        super(substance,field);
        this.index = index;
        this.repository = repository;
        indexedField = (FieldItem)index.getAttribute(RttiAC.INDEXED_FIELD);
        this.indexNotFoundHandler = indexNotFoundHandler;
        updateKey();
    }

    public void setValue(Object value) {
        super.setValue(value);
        updateKey();
    }

    protected void updateKey() {
        if (indexedField!=null && value!=null) {
            key = indexedField.getThroughAccessor(value).toString();
        }
    }

    // HTMLEditor interface

    public void genHTML(PrintWriter out) {
        out.print("<input type=\"text\" name=\""+label+
                  "\" size=\"12\" style=\"width:12ex\""+
                  " value=\""+(key!=null?key:"")+"\"");
        printAttributes(out);
        out.println(">");
    }

    protected boolean doReadValue(Object parameter) {
        key = (String)parameter;
        if (Strings.isEmpty(key)) {
            setValue(null);
        } else {
            Object value = index.getMap(repository,key);
            if (value!=null) {
                setValue(value);
            } else {
                if (indexNotFoundHandler!=null && field!=null) {
                    value = indexNotFoundHandler.invokeStatic(
                        new Object[] {field.getTypeItem(),key});
                }
                setValue(value);
            }

        }
        return true;
    }


}

