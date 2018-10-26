/*
  Copyright (C) 2003 Laurent Martelli <laurent@aopsys.com>
  
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

package org.objectweb.jac.aspects.gui;

import java.util.Collection;
import org.objectweb.jac.core.rtti.CollectionItem;
import org.objectweb.jac.core.rtti.FieldItem;

public class FilterCriteria {
    public FilterCriteria(int column, FieldItem field) {
        this.column = column;
        this.field = field;
    }

    /**
     * Tells wether a row of table model matches the filter
     * @param model the table model
     * @param row row index in the table model
     */
    public boolean match(ExtendedTableModel model, int row) {
        Object modelValue = model.getObject(row,column);
        if (field instanceof CollectionItem) {
            return !active || 
                (modelValue!=null && ((Collection)modelValue).contains(value));
        } else {
            return !active || 
                (value==null && modelValue==null) ||
                (value!=null && value.equals(modelValue));
        }
    }

    /** The column to filter */
    int column;
    public int getColumn() {
        return column;
    }

    FieldItem field;
    public FieldItem getField() {
        return field;
    }

    /** If false, the filer is inactive */
    boolean active = false;
    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }

    Object value;
    public void setValue(Object value) {
        this.value = value;
    }
    public Object getValue() {
        return value;
    }

    public String toString() {
        return column+(active?("=="+value):"(off)");
    }

    public boolean equals(Object o) {
        if (!(o instanceof FilterCriteria))
            return false;
        FilterCriteria criteria = (FilterCriteria)o;
        return criteria.column==column && criteria.active==active;
    }

    public int hashCode() {
        return column ^ (active ? 0 : 2^31);
    }
}
