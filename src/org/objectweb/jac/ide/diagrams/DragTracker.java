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

import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.standard.AbstractTool;
import java.awt.event.MouseEvent;
import java.util.HashSet;

/**
 * This drag tracker handles figures attached to links so that they do
 * not drift.
 */
public class DragTracker extends AbstractTool {

   Figure  anchorFigure;
   int     lastX, lastY;      // previous mouse position

   public DragTracker(DrawingEditor newDrawingEditor, Figure anchor) {
      super(newDrawingEditor);
      anchorFigure = anchor;
   }

   public void mouseDown(MouseEvent e, int x, int y) {
      super.mouseDown(e, x, y);
      lastX = x;
      lastY = y;

      if (e.isShiftDown()) {
         view().toggleSelection(anchorFigure);
         anchorFigure = null;
      } else if (!view().isFigureSelected(anchorFigure)) {
         view().clearSelection();
         view().addToSelection(anchorFigure);
      }
      view().repairDamage();
   }

   public void mouseDrag(MouseEvent e, int x, int y) {
      super.mouseDrag(e, x, y);
      
      if ((Math.abs(x - fAnchorX) > 4) || (Math.abs(y - fAnchorY) > 4)) {
         org.objectweb.jac.util.Log.trace("tools","MOVED!");

         // Get the link figures
         HashSet linkFigures = new HashSet();
         FigureEnumeration figures = view().selectionElements();
         while (figures.hasMoreElements()) {
            Figure figure = figures.nextFigure();
            if (figure instanceof LinkFigure) {
               linkFigures.add(((LinkFigure)figure).getSubstance());
            }
         }

         figures = view().selectionElements();
         while (figures.hasMoreElements()) {
            Figure figure = figures.nextFigure();
            if (! ((figure instanceof AttachedTextFigure) 
                   && linkFigures.contains(((AttachedTextFigure)figure).getSubstance()))) {
               figure.moveBy(x - lastX, y - lastY);
            }
         }
      }
      lastX = x;
      lastY = y;
   }

   public void activate() {
   }

   public void deactivate() {
   }
}
