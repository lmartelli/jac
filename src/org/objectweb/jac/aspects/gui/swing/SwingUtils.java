/*
  Copyright (C) 2004 Laurent Martelli <laurent@aopsys.com>

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
  USA */

package org.objectweb.jac.aspects.gui.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import javax.swing.JComponent;
import org.objectweb.jac.aspects.gui.Length;
import org.objectweb.jac.aspects.gui.Unit;

public class SwingUtils {    
    public static int getPixelLength(Length l, Component c) {
        FontMetrics metrics = c.getFontMetrics(c.getFont());
        if (l.unit == Unit.EM) {
            return (int)(l.value * metrics.getHeight());
        } else if (l.unit == Unit.EX) {
            return (int)(l.value * metrics.charWidth('x'));
        } else if (l.unit == Unit.PX) {
            return (int)l.value;
        } else {
            throw new RuntimeException("Unhandled unit: "+l.unit);
        }
    }

    public static void setSize(JComponent c, Length width, Length height) {
        if (width!=null || height!=null) {
            Dimension dim = c.getPreferredSize();
            if (width!=null) 
                dim.width = SwingUtils.getPixelLength(width,c);
            if (height!=null)
                dim.height = SwingUtils.getPixelLength(height,c);
            c.setPreferredSize(dim);
        }
    }

    public static void setColumns(javax.swing.JTextField textField, Length width) {
        if (width != null && width.unit == Unit.EX)
            textField.setColumns((int)width.value);        
    }
}
