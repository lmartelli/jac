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
import CH.ifa.draw.framework.Figure;
import java.util.List;
import java.util.Vector;
import org.objectweb.jac.aspects.gui.DisplayContext;
import org.objectweb.jac.aspects.gui.EventHandler;
import org.objectweb.jac.aspects.gui.InvokeEvent;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.ide.Class;
import org.objectweb.jac.ide.InheritanceLink;
import org.objectweb.jac.ide.Link;
import org.objectweb.jac.ide.RelationLink;
import org.objectweb.jac.util.Log;

/**
 * Show an existing relation or inheritance link
 */
public class RelationLinkShowTool extends AbstractActionTool {

   public RelationLinkShowTool(DrawingEditor newDrawingEditor,
                               DisplayContext context) throws ClassNotFoundException {
      super(newDrawingEditor,context,java.lang.Class.forName("org.objectweb.jac.ide.diagrams.ClassFigure"));
   }

   // gui related infos and methods

   Class curClass = null;
   ClassFigure startFig = null;

   /**
    * Returns the list of relations of the current class not yet on
    * the diagram
    * @return a list of RelationLink 
    */
   public List relations() {
      Log.trace("figures","getting relations for class "+curClass);
      List relations = new Vector();
      if (curClass!=null) {
         return ((DiagramView)editor()).getDiagram().getMissingRelations(curClass);
      } else {
         return null;
      }
   }

   public void showRelation(Link link) throws Exception {
      //System.out.println("chooseRelation("+relation+","+relation.getEnd()+")");
      if (link==null || link.getStart()==null || link.getEnd()==null) 
         return;
      if (link instanceof RelationLink)
         ((DiagramView)editor()).importRelation((RelationLink)link);
      else if (link instanceof InheritanceLink)
         ((DiagramView)editor()).importInheritance((InheritanceLink)link);
      view().repairDamage();
      editor().toolDone();            
   }

   public void action(Figure figure) {
      startFig = (ClassFigure)figure;
      curClass = startFig.getClassElement();
      EventHandler.get().onInvoke(
         context, 
         new InvokeEvent(
             null,
             this, 
             ClassRepository.get().getClass(getClass())
             .getMethod("showRelation")));

   }

}

