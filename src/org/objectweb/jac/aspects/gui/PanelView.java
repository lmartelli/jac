/*
  Copyright (C) 2002 Laurent Martelli
  
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

package org.objectweb.jac.aspects.gui;

public interface PanelView extends CompositeView {
   /**
    * Sets a splitter location.
    *
    * <p>The splitter is referenced by its index going from the
    * front-end splitter to the back-end splitters. For instance, in
    * the case of a 3 sub-panel window, the 0 index references the
    * splitter that splits the main window in two, the 1 index, the
    * one that splits the half-window in two other smaller parts.
    *
    * @param splitterId the splitter's index
    * @param location the position in pixel, regarding to the top/left
    * component, a negative value means that the splitter should be
    * set at the preferred sized of the inner components 
    */
   void setSplitterLocation(int splitterId, float location);

   /** The panel is on the top (when 2-3 panels) */
   String UPPER = "upper";
   /** The panel is on the bottom (when 2-3 panels) */
   String LOWER = "lower";
   /** The panel is on the right (when 2-3 panels) */
   String RIGHT = "right";
   /** The panel is on the left (when 2-3 panels) */
   String LEFT = "left";
   /** The panel is on the top-left (when 3-4 panels) */
   String UPPER_LEFT = "upper_left";
   /** The panel is on the top-right (when 3-4 panels) */
   String UPPER_RIGHT = "upper_right";
   /** The panel is on the bottom-left (when 3-4 panels) */
   String LOWER_LEFT = "lower_left";
   /** The panel is on the bottom-right (when 3-4 panels) */
   String LOWER_RIGHT = "lower_right";

}
