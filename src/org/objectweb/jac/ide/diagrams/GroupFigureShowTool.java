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
import org.objectweb.jac.aspects.gui.DisplayContext;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.ide.Group;
import CH.ifa.draw.framework.Figure;

public class GroupFigureShowTool extends CreationTool {
   DisplayContext context;
   public GroupFigureShowTool(DrawingEditor newDrawingEditor, DisplayContext context) {
      super(newDrawingEditor);
      this.context = context;
   }

   public void chooseGroup(Group group) {}

   protected Figure createFigure() {
      Object[] parameters = new Object[] {null};
      boolean result = context.getDisplay().showInput(
         this,ClassRepository.get().getClass(getClass()).getMethod("chooseGroup"),
         parameters);
      GenericObjectFigure figure = null;
      if( result ) {
         if( parameters[0] != null ) {
            
            // figure = new GenericObjectFigure(parameters[0]);
            figure.setShape(GenericObjectFigure.SHAPE_ROUNDRECT);
            figure.setCollection(
               ClassRepository.get().getClass(org.objectweb.jac.ide.Package.class)
               .getCollection("groups"));               
            figure.initFields();
         }
      }
      //cf.linkToClass(((DiagramApplet)editor()).getSubstance());
      editor().toolDone();
      return figure;
   }
}
