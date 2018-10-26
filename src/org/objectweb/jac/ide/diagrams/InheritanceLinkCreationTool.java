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
import org.objectweb.jac.ide.Class;
import org.objectweb.jac.ide.Diagram;
import org.objectweb.jac.ide.ModelElement;
import org.objectweb.jac.util.Log;
import org.objectweb.jac.aspects.gui.Utils;

public  class InheritanceLinkCreationTool extends RelationLinkCreationTool {

    public InheritanceLinkCreationTool(DrawingEditor newDrawingEditor) {
        super(newDrawingEditor);
    }

    protected LinkFigure createLinkFigure() {
        return new InheritanceLinkFigure();
    }

    protected void createRelation(Class source, Class target) {
        Log.trace("figures","creating a new relation link between "+
                  source+" and "+target);

        if (source != null && target != null ) {
            org.objectweb.jac.ide.InheritanceLink rel = 
                new org.objectweb.jac.ide.InheritanceLink(source,target);
            Log.trace("diagram","1. end="+rel.getEnd()+"===> substance="+target);
            InheritanceLinkFigure linkFigure = 
                (InheritanceLinkFigure)myConnection;
            org.objectweb.jac.ide.LinkFigure linkFig = 
                new org.objectweb.jac.ide.LinkFigure(rel);
            linkFigure.setLinkFigure(linkFig);

            Log.trace("diagram","2. end="+rel.getEnd());

            source.setSuperClass(target);

            Diagram diagram = (Diagram)((DiagramView)editor()).getSubstance();

            diagram.addFigure(linkFig);

            Utils.registerObject(rel,myConnection);

            Log.trace("diagram","3. end="+rel.getEnd());
        }
    }

}
