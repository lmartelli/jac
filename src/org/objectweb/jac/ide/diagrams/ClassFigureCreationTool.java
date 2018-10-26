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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import org.objectweb.jac.aspects.gui.DisplayContext;
import org.objectweb.jac.aspects.gui.EventHandler;
import org.objectweb.jac.aspects.gui.InvokeEvent;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.ide.Class;
import org.objectweb.jac.ide.Diagram;
import org.objectweb.jac.ide.Project;
import org.objectweb.jac.util.Log;

public  class ClassFigureCreationTool extends CreationTool {

    DisplayContext context;

    public ClassFigureCreationTool(DrawingEditor newDrawingEditor, 
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
                .getMethod("importClass(org.objectweb.jac.ide.Class,boolean)")));
        // (view().add(getCreatedFigure())).displayBox(anchorPoint, anchorPoint);
    }

    public void mouseUp(MouseEvent e, int x, int y) {
    }

    /**
     * Import class
     * @param cl the class to import
     * @param importRelations wether to also import relations with
     * other classes on the diagram
     */
    public void importClass(Class cl, boolean importRelations) {
        Log.trace("figures","createFigure for "+cl);
        if (cl!=null) {
            DiagramView diagram = ((DiagramView)editor());
            diagram.addClass(cl,anchorPoint);

            if (importRelations) {
                diagram.importRelations(cl);            
            }
        }
        editor().toolDone();
    }

    public Collection importClassChoice() {
        List result = new Vector();
        Diagram diagram = (Diagram)((DiagramView)editor()).getSubstance();
        Project project = diagram.getContainer().getProject();
        Iterator it = project.getClasses().iterator();
        while (it.hasNext()) {
            Class cl = (Class)it.next();
            if (!diagram.contains(cl))
                result.add(cl);
        }
        return result;
    }
}
