/*
  Copyright (C) 2002 Renaud Pawlak

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
import CH.ifa.draw.standard.ActionTool;
import org.objectweb.jac.aspects.gui.DisplayContext;
import org.objectweb.jac.core.Wrapping;
import org.objectweb.jac.core.rtti.ClassRepository;
import java.util.List;

public class PointcutLinkShowTool extends ActionTool {
   DisplayContext context;
   public PointcutLinkShowTool(DrawingEditor newDrawingEditor, 
                               DisplayContext context) {
      super(newDrawingEditor);
      this.context = context;
   }

   // gui related infos and methods

   static org.objectweb.jac.ide.Aspect curAspect=null;

   public static List pointcuts(Object o) {
      if(curAspect!=null) {
         return curAspect.getPointcutLinks();
      } else {
         return null;
      }
   }

   public void choosePointcut(org.objectweb.jac.ide.PointcutLink pointcut) {
   }

   public void action(Figure figure) {
      if(! (figure instanceof AspectFigure) ) return;
      
      curAspect=(org.objectweb.jac.ide.Aspect)((AspectFigure)figure).getSubstance();
      Object[] parameters = new Object[] {null};
      boolean result = context.getDisplay().showInput(
         this,ClassRepository.get().getClass(getClass()).getMethod("choosePointcut"),
         parameters);
      Figure cf = null;
      if( result ) {
         org.objectweb.jac.ide.PointcutLink rel = (org.objectweb.jac.ide.PointcutLink)parameters[0];
         if( rel == null || rel.getEnd() == null ) return;
         
         if( rel.getEnd() instanceof org.objectweb.jac.ide.TypedElement ) {
            cf = ((DiagramView)editor()).findElement((org.objectweb.jac.ide.TypedElement)rel.getEnd());
         }
         if( cf == null ) {
            // end is not found in the diagram... import it!
            context.getDisplay().showMessage(
               "You must import '"+
               rel.getEnd()+
               "' before this action.",
               "Error",false,false,true);
         } else {

            PointcutLinkFigure relf = new PointcutLinkFigure();
            relf.startPoint(figure.center().x,figure.center().y);
            relf.endPoint(cf.center().x,cf.center().y);
            relf.connectStart(figure.connectorAt(figure.center().x,figure.center().y));
            relf.connectEnd(cf.connectorAt(cf.center().x,cf.center().y));

            // ***            relf.setSubstance(rel);

            view().add(relf);

            view().add(relf.createAttachedFigure(
               AttachedTextFigure.NAME));
            view().add(relf.createAttachedFigure(
               AttachedTextFigure.START_ROLE));
            view().add(relf.createAttachedFigure(
               AttachedTextFigure.END_ROLE));
            view().add(relf.createAttachedFigure(
               AttachedTextFigure.START_CARDINALITY));
            view().add(relf.createAttachedFigure(
               AttachedTextFigure.END_CARDINALITY));
            
            relf.updateConnection();
            Wrapping.invokeRoleMethod((org.objectweb.jac.core.Wrappee)rel,"addView",new Object[]{relf});

            editor().toolDone();
         }
      }
   }

}

