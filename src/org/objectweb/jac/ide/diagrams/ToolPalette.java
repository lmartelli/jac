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

  You should have received a copy of the GNU Lesser General Public
  License along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
*/

package org.objectweb.jac.ide.diagrams;

import CH.ifa.draw.framework.Tool;
import CH.ifa.draw.util.PaletteListener;
import org.objectweb.jac.aspects.gui.ResourceManager;
import java.awt.Point;
import javax.swing.JPanel;


public class ToolPalette extends JPanel {

   public ToolPalette() {
      setLayout(new PaletteLayout(2,new Point(2,2)));
   }


   /**
    * Add a tool button with the given image, tool, and text
    *
    * @param iconName name of the icon of the tool
    * @param toolName name of the tool
    * @param tool the tool
    */
   public ToolButton addToolButton(PaletteListener paletteListener, 
                                   String iconName, String toolName, 
                                   Tool tool) {
      ToolButton button = 
         new ToolButton(paletteListener, 
                        ResourceManager.getIconResource(iconName), 
                        toolName, 
                        tool);
      add(button);
      return button;
   }

   ToolButton defaultToolButton;
   public void setDefaultToolButton(ToolButton defaultToolButton) {
      this.defaultToolButton = defaultToolButton;
   }
   public ToolButton  getDefaultToolButton() {
      return defaultToolButton;
   }
}
