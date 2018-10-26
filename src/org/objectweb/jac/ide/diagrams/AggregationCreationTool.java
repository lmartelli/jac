/*
  Copyright (C) 2002-2003 Renaud Pawlak <renaud@aopsys.com>

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

public  class AggregationCreationTool extends RelationLinkCreationTool {
   
   public AggregationCreationTool(DrawingEditor newDrawingEditor) {
      super(newDrawingEditor);
   }

   /**
    * Create a RelationLink between two classes.
    * @param source start class of the link
    * @param target end class of the link
    */
   protected void createRelation(Class source, Class target) {
      if (source != null && target != null) {
         ((DiagramView)editor()).createRelation(source,target,(RelationLinkFigure)myConnection,true);
      }
   }

}
