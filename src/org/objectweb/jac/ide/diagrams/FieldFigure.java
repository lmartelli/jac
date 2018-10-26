/*
  Copyright (C) 2002-2003 Renaud Pawlak <renaud@aopsys.com>

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 2 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
*/

package org.objectweb.jac.ide.diagrams;

import org.objectweb.jac.aspects.gui.DisplayContext;
import org.objectweb.jac.aspects.gui.EventHandler;
import org.objectweb.jac.aspects.gui.GuiAC;
import org.objectweb.jac.aspects.gui.ObjectUpdate;
import org.objectweb.jac.aspects.gui.Utils;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.core.rtti.CollectionItem;
import org.objectweb.jac.ide.Class;
import org.objectweb.jac.ide.Field;
import org.objectweb.jac.ide.ModelElement;

public class FieldFigure extends MemberFigure 
    implements ModelElementFigure, ObjectUpdate, Selectable
{
    Field field;

    public FieldFigure(Field field, ClassFigure parentFigure) {
        super(parentFigure);
        this.field = field;
        super.setText(GuiAC.toString(field));
        Utils.registerObject(field,this);
    }
    
    public void close() {
        Utils.unregisterObject(field,this);
    }

    public ModelElement getSubstance() {
        return field;
    }

    public void linkToField(Class cl) {
        field = cl.findField(getName());
    }

    boolean updating = false;

    public void setText(String s) {
        super.setText(s);
        if (field != null && !DiagramView.init) {
            updating=true;
            field.setName(getName());
            field.setType(org.objectweb.jac.ide.Projects.types.resolveType(getType()));
            updating=false;
        }
    }

    // ObjectUpdate interface
    public void objectUpdated(Object object, Object extra) {
        if (!updating) {
            super.setText(GuiAC.toString(field));
        }
    }

    // Selectable interface
    public void onSelect(DisplayContext context) {
        CollectionItem coll = ClassRepository.get().getClass(Class.class)
            .getCollection("fields");
        EventHandler.get().onSelection(
            context,coll,getSubstance(),null,null);
    }
}
