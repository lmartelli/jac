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
import java.awt.Point;
import java.awt.event.MouseEvent;
import org.objectweb.jac.aspects.gui.DisplayContext;
import org.objectweb.jac.aspects.gui.EventHandler;
import org.objectweb.jac.aspects.gui.InvokeEvent;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.ide.Class;
import org.objectweb.jac.ide.Diagram;

public class NewClassFigureCreationTool extends CreationTool {

    DisplayContext context;
    int count = 0;

    public NewClassFigureCreationTool(DrawingEditor newDrawingEditor,
                                      DisplayContext context) {
        super(newDrawingEditor);
        this.context = context;
    }

    Point anchorPoint;

    public void mouseDown(MouseEvent e, int x, int y) {
        anchorPoint = new Point(x,y);
        EventHandler.get().onInvoke(
            context, 
            new InvokeEvent(null,
                            this, 
                            ClassRepository.get().getClass(getClass())
                            .getMethod("createNewClass(java.lang.String)")));
        // (view().add(getCreatedFigure())).displayBox(anchorPoint, anchorPoint);
    }

    public void mouseUp(MouseEvent e, int x, int y) {
    }

    public void createNewClass(String name) {
        if (name!=null) {
            Class substance = new Class();         
            substance.setName(name);
            Diagram diagram = (Diagram)((DiagramView)editor()).getSubstance();
            // Add the class to the package of the diagram
            diagram.getContainer().addClass(substance);
            // Add the class to the diagram
            ((DiagramView)editor()).addClass(substance,anchorPoint);
        }
        editor().toolDone();
    }
}

