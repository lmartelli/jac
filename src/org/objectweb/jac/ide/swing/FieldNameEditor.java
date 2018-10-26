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

package org.objectweb.jac.ide.swing;

import java.util.Collection;
import org.objectweb.jac.aspects.gui.swing.PrimitiveFieldEditor;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.ide.Gui;
import org.objectweb.jac.ide.Type;
import org.objectweb.jac.ide.TypedElement;
import org.objectweb.jac.util.Strings;

/**
 * This editor tries to set the type of the field from its name if
 * it's not already set.
 */
public class FieldNameEditor extends PrimitiveFieldEditor {
    TypedElement element;
    public FieldNameEditor(TypedElement substance, FieldItem field) {
        super(substance,field);
        this.element = substance;
    }

    public void setValue(Object value) {
        super.setValue(value);
        if (element.getType()==null && !Strings.isEmpty((String)value)) {
            Collection types = Gui.getMatchingTypes(element,element.getName());
            if (!types.isEmpty()) {
                element.setType((Type)types.iterator().next());
            }
        }
    }
}
