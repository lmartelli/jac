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

package org.objectweb.jac.aspects.gui.swing;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.table.TableCellRenderer;
import org.apache.log4j.Logger;
import org.objectweb.jac.aspects.gui.GuiAC;

public class ToStringTableCellRenderer extends JLabel implements TableCellRenderer
{
    static Logger logger = Logger.getLogger("gui.table");

    public ToStringTableCellRenderer() 
    {}

    public Component getTableCellRendererComponent(
        javax.swing.JTable table, Object value, 
        boolean isSelected, boolean hasFocus, 
        int row, int column) 
    {
        logger.debug(
            "ToStringTableCellRenderer.getTableCellRendererComponent("+
            row+","+column+","+isSelected+")");

        setOpaque(true); // so that the background is really drawn
         
        if (isSelected) {
            setForeground(table.getSelectionForeground());
            setBackground(table.getSelectionBackground());
        } else {
            setForeground(table.getForeground());
            setBackground(table.getBackground());
        }
        setFont(null);

        setText(GuiAC.toString(value)); 

        return this;
    }

}
