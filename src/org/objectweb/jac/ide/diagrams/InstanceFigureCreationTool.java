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
import org.objectweb.jac.aspects.gui.DisplayContext;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.ide.Diagram;
import org.objectweb.jac.ide.Instance;
import CH.ifa.draw.framework.Figure;

public  class InstanceFigureCreationTool extends CreationTool {

   DisplayContext context;

   public InstanceFigureCreationTool(DrawingEditor newDrawingEditor, 
                                     DisplayContext context) {
      super(newDrawingEditor);
      this.context = context;
   }

   public void chooseInstance(org.objectweb.jac.ide.Instance clazz) {}

   protected Figure createFigure() {
      Object[] parameters = new Object[] {null};
      boolean result = context.getDisplay().showInput(
         this,ClassRepository.get().getClass(getClass()).getMethod("chooseInstance"),
         parameters);
      InstanceFigure cf = null;
      if( result ) {
         cf = new InstanceFigure();
         if( parameters[0] != null ) {
            cf.setSubstance((Instance)parameters[0]);
            cf.setContainerPackage(((Diagram)((DiagramView)editor()).getSubstance()).getContainer());

            cf.initInstance();
         }
      }
      editor().toolDone();
      return cf;
   }
}
