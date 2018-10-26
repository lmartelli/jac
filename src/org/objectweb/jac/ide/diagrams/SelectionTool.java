/*
  Copyright (C) 2002 Laurent Martelli <laurent@aopsys.com>

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

import CH.ifa.draw.contrib.DragNDropTool;
import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.Handle;
import CH.ifa.draw.framework.Tool;
import CH.ifa.draw.standard.HandleTracker;
import CH.ifa.draw.standard.SelectAreaTracker;
import org.objectweb.jac.aspects.gui.DisplayContext;
import java.awt.event.MouseEvent;


public class SelectionTool extends AbstractTool {

   protected Tool fChild = null;
   protected DisplayContext context;

   public SelectionTool(DrawingEditor newDrawingEditor, DisplayContext context) {
      super(newDrawingEditor);
      this.context = context;
   }

   /**
    * Handles mouse down events and starts the corresponding tracker.
    */
   public void mouseDown(MouseEvent e, int x, int y) {
      // on MS-Windows NT: AWT generates additional mouse down events
      // when the left button is down && right button is clicked.
      // To avoid dead locks we ignore such events
      if (fChild != null) {
         return;
      }

      view().freezeView();

      Handle handle = view().findHandle(e.getX(), e.getY());
      if (handle != null) {
         fChild = createHandleTracker(view(), handle);
      } else {
         Figure figure = drawing().findFigureInside(e.getX(), e.getY());
         if (figure instanceof Selectable) {
            ((Selectable)figure).onSelect(context);
         }
         figure = drawing().findFigure(e.getX(), e.getY());
         if (figure != null) {
            fChild = createDragTracker(figure);
         } else {
            if (!e.isShiftDown()) {
               view().clearSelection();
            }
            fChild = createAreaTracker();
         }
      }
      fChild.mouseDown(e, x, y);
      fChild.activate();
      view().repairDamage();
   }

   /**
    * Handles mouse moves (if the mouse button is up).
    * Switches the cursors depending on whats under them.
    */
   public void mouseMove(MouseEvent evt, int x, int y) {
      DragNDropTool.setCursor(evt.getX(), evt.getY(), view());
      ((DiagramView)editor()).setCoord(x,y);
      view().repairDamage();
   }

   /**
    * Handles mouse drag events. The events are forwarded to the
    * current tracker.
    */
   public void mouseDrag(MouseEvent e, int x, int y) {
      if (fChild != null) { // JDK1.1 doesn't guarantee mouseDown, mouseDrag, mouseUp
         fChild.mouseDrag(e, x, y);
      }
      view().repairDamage();
   }

   /**
    * Handles mouse up events. The events are forwarded to the
    * current tracker.
    */
   public void mouseUp(MouseEvent e, int x, int y) {
      view().unfreezeView();
      if (fChild != null) { // JDK1.1 doesn't guarantee mouseDown, mouseDrag, mouseUp
         fChild.mouseUp(e, x, y);
         fChild.deactivate();
         fChild = null;
      }
      view().repairDamage();
   }

   /**
    * Factory method to create a Handle tracker. It is used to track a handle.
    */
   protected Tool createHandleTracker(DrawingView view, Handle handle) {
      return new HandleTracker(editor(), handle);
   }

   /**
    * Factory method to create a Drag tracker. It is used to drag a figure.
    */
   protected Tool createDragTracker(Figure f) {
      return new DragTracker(editor(), f);
   }

   /**
    * Factory method to create an area tracker. It is used to select an
    * area.
    */
   protected Tool createAreaTracker() {
      return new SelectAreaTracker(editor());
   }

}
