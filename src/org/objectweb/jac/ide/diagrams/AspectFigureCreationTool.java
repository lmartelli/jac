/*
  Copyright (C) 2002 Renaud Pawlak <renaud@aopsys.com>

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
import java.awt.Point;
import java.awt.event.MouseEvent;
import org.objectweb.jac.aspects.gui.DisplayContext;
import org.objectweb.jac.aspects.gui.EventHandler;
import org.objectweb.jac.aspects.gui.InvokeEvent;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.ide.Aspect;
import org.objectweb.jac.ide.Diagram;
import org.objectweb.jac.util.Log;

public  class AspectFigureCreationTool extends CreationTool {

   DisplayContext context;

   public AspectFigureCreationTool(DrawingEditor newDrawingEditor, 
                                  DisplayContext context) {
      super(newDrawingEditor);
      this.context = context;
   }

   Point anchorPoint;

   /**
    * Creates a new figure by cloning the prototype.
    */
   public void mouseDown(MouseEvent e, int x, int y) {
      anchorPoint = new Point(x,y);
      EventHandler.get().onInvoke(
         context,
         new InvokeEvent(
             null, 
             this, 
             ClassRepository.get().getClass(getClass())
             .getMethod("importAspect(org.objectweb.jac.ide.Aspect)")));
         // (view().add(getCreatedFigure())).displayBox(anchorPoint, anchorPoint);
   }

   public void mouseUp(MouseEvent e, int x, int y) {
   }

   public void importAspect(Aspect cl) {
      Log.trace("figures","createFigure for "+cl);
      if (cl!=null) {
         AspectFigure cf = null;
         org.objectweb.jac.ide.ClassFigure figure = 
            new org.objectweb.jac.ide.ClassFigure(cl);
         Diagram diagram = (Diagram)((DiagramView)editor()).getSubstance();
         diagram.addFigure(figure);
         Log.trace("diagram","creating new figure "+figure+","+
                   diagram.getContainer());
         cf = new AspectFigure(figure,diagram.getContainer(),
                               ((DiagramView)editor()).view());
         (view().add(cf)).displayBox(anchorPoint, anchorPoint);
      }
      editor().toolDone();
   }

}


