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

package org.objectweb.jac.aspects.gui.swing;

import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;
import org.apache.log4j.Logger;
import org.objectweb.jac.aspects.gui.FieldUpdate; 
import org.objectweb.jac.aspects.gui.Utils; 
import org.objectweb.jac.core.rtti.FieldItem;

public abstract class AbstractFieldView extends AbstractView 
    implements FieldUpdate, TableCellRenderer
{
    static Logger logger = Logger.getLogger("gui.table");

    // substance and field are required so that we can register and
    // unregister ourself from fieldUpdated events on close()
    Object substance;
    FieldItem field;

    boolean isCellViewer = false;

    public AbstractFieldView(Object substance, FieldItem field) {
        this.substance = substance;
        this.field = field;

        Utils.registerField(substance,field,this);
    }

    public AbstractFieldView() {
        substance = null;
        field = null;
    }

    /**
     * Sets the font of the component for use in a table cell
     */
    protected void setTableFont() {
        JComponent component = getComponent();
        if (component!=null) {
            component.setFont(UIManager.getFont("Table.font"));        
        }
    }

    public abstract void setValue(Object value);

    public void setSubstance(Object substance) {
        Utils.unregisterField(this.substance,field,this);
        this.substance = substance;
        Utils.registerField(substance,field,this);
    }

    public Object getSubstance() {
        return substance;
    }

    public void setField(FieldItem field) {
        Utils.unregisterField(substance,this.field,this);
        this.field = field;
        Utils.registerField(substance,field,this);
    }

    public FieldItem getField() {
        return field;
    }

    public void setAutoUpdate(boolean autoUpdate) {
        // TODO ...
    }

    public void close(boolean validate) {
        Utils.unregisterField(substance,field,this);
    }
   
    // FieldUpdate interface
    public void fieldUpdated(Object substance, 
                             FieldItem field, Object value, 
                             Object param) {
        setValue(value);
    }

    // TableCellRenderer
    public Component getTableCellRendererComponent(
        JTable table, Object value, 
        boolean isSelected, boolean hasFocus, 
        int row, int column) 
    {
        logger.debug(
            this+".getTableCellRendererComponent("+row+","+column+","+isSelected+")");
        JComponent component = getComponent();
      
        if (component!=null) {
            component.setOpaque(true); // so that the background is really drawn
        }
        setOpaque(true); // so that the background is really drawn
         
        if (isSelected) {
            if (component!=null) {
                component.setForeground(table.getSelectionForeground());
                component.setBackground(table.getSelectionBackground());
            }
            setForeground(table.getSelectionForeground());
            setBackground(table.getSelectionBackground());
        } else {
            if (component!=null) {
                component.setForeground(table.getForeground());
                component.setBackground(table.getBackground());
            }
            setForeground(table.getForeground());
            setBackground(table.getBackground());
        }
        setValue(value); 

        return this;
    }

    /**
    * Used by getTableCellRendererComponent. setForeground(),
    * setBackground() and setFont() will be called on this component
    * if it is not null; */
    protected JComponent getComponent() {
        return null;
    }
}
