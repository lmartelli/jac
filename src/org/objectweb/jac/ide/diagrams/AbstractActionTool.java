/*
  Copyright (C) 2002-2003 Laurent Martelli <laurent@aopsys.com>

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
import CH.ifa.draw.standard.AbstractTool;
import org.objectweb.jac.aspects.gui.DisplayContext;
import java.awt.event.MouseEvent;

public abstract class AbstractActionTool extends AbstractTool {

   protected DisplayContext context;
   Class figureClass;

   /**
    * @param drawingEditor the editor the tool is attached to
    * @param context the display context
    * @param figureClass the type of figures the action applies to
    */
   public AbstractActionTool(DrawingEditor drawingEditor,
                             DisplayContext context,
                             Class figureClass) {
      super(drawingEditor);
      this.context = context;
      this.figureClass = figureClass;
   }

   /**
    * Add the touched figure to the selection an invoke action
    * @see #action
    */
   public void mouseDown(MouseEvent e, int x, int y) {
      Figure target = drawing().findFigure(x, y);
      if (target != null && figureClass.isAssignableFrom(target.getClass())) {
         view().addToSelection(target);
         action(target);
      }
   }
   
   public void mouseUp(MouseEvent e, int x, int y) {
      editor().toolDone();
   }

   public abstract void action(Figure figure);

   public boolean isActive() {
      // The AbstractTool implementation seems to always return false
      // because isUsable()==false
      return editor().tool() == this;
   }
}

