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
  USA. */

package org.objectweb.jac.ide.diagrams;

import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.Figure;
import org.objectweb.jac.aspects.gui.DisplayContext;
import org.objectweb.jac.aspects.gui.EventHandler;
import org.objectweb.jac.aspects.gui.InvokeEvent;
import org.objectweb.jac.core.rtti.ClassRepository;

public class MethodCreationTool extends AbstractActionTool {

   public MethodCreationTool(DrawingEditor newDrawingEditor,
                             DisplayContext context) {
      super(newDrawingEditor,context,ClassFigure.class);
   }

   public void action(Figure figure) {
      EventHandler.get().onInvoke(
         context, 
         new InvokeEvent(
             null,
             ((ClassFigure)figure).getSubstance(), 
             ClassRepository.get().getClass(org.objectweb.jac.ide.Class.class)
             .getMethod("addMethod(org.objectweb.jac.ide.Method)")));
   }

}
