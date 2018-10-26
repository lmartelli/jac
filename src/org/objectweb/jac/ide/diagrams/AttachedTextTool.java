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
import CH.ifa.draw.figures.TextTool;
import CH.ifa.draw.standard.TextHolder;
import java.awt.event.MouseEvent;

class AttachedTextTool extends TextTool {

   private boolean fConnected = false;
   
   public AttachedTextTool(DrawingEditor editor, Figure prototype) {
      super(editor, prototype);
   }

	/**
	 * If the pressed figure is a TextHolder it can be edited otherwise
	 * a new text figure is created.
	 */
   public void mouseDown(MouseEvent e, int x, int y) {
      super.mouseDown(e, x, y);
      
      Figure pressedFigure =  drawing().findFigureInside(x, y);
      TextHolder textHolder = getTypingTarget();
      if (!fConnected && pressedFigure != null &&
          textHolder != null && pressedFigure != textHolder 
          && (pressedFigure instanceof LinkFigure) ) {
         
         /*
         ((AttachedTextFigure)getCreatedFigure())
         .setSubstance((RelationLink)((LinkFigure)pressedFigure).getSubstance());*/
         textHolder.connect(pressedFigure);
         //         ((ConnectedTextTool.UndoActivity)getUndoActivity()).setConnectedFigure(pressedFigure);
         fConnected = true;
      }
   }
   
   /**
    * If the pressed figure is a TextHolder it can be edited otherwise
    * a new text figure is created.
    */
   public void activate() {
      super.activate();
      fConnected = false;
   }
   
   /**
    * Factory method for undo activity
    */
   /*
   protected Undoable createUndoActivity() {
      return new ConnectedTextTool.UndoActivity(view(), getTypingTarget().getText());
   }
   
   public static class UndoActivity extends TextTool.UndoActivity {
      private Figure myConnectedFigure;
      
      public UndoActivity(DrawingView newDrawingView, String newOriginalText) {
         super(newDrawingView, newOriginalText);
      }
      
      /*
       * Undo the activity
       * @return true if the activity could be undone, false otherwise
       * /
      public boolean undo() {
         if (!super.undo()) {
            return false;
         }
         
         FigureEnumeration fe = getAffectedFigures();
         while (fe.hasMoreElements()) {
            Figure currentFigure = fe.nextFigure();
            if (currentFigure instanceof TextHolder) {
               TextHolder currentTextHolder = (TextHolder)currentFigure;
               // the text figure didn't exist before
               if (!isValidText(getOriginalText())) {
                  currentTextHolder.disconnect(getConnectedFigure());
               }
               // the text figure did exist but was remove
               else if (!isValidText(getBackupText())) {
                  currentTextHolder.connect(getConnectedFigure());
               }
            }
         }
         
         return true;
      }
      
      /*
       * Redo the activity
       * @return true if the activity could be redone, false otherwise
       * /
      public boolean redo() {
         if (!super.redo()) {
            return false;
         }
         
         FigureEnumeration fe = getAffectedFigures();
         while (fe.hasMoreElements()) {
            Figure currentFigure = fe.nextFigure();
            if (currentFigure instanceof TextHolder) {
               TextHolder currentTextHolder = (TextHolder)currentFigure;
               // the text figure did exist but was remove
               if (!isValidText(getBackupText())) {
                  currentTextHolder.disconnect(getConnectedFigure());
               }
               // the text figure didn't exist before
               else if (!isValidText(getOriginalText())) {
                  currentTextHolder.connect(getConnectedFigure());
               }
            }
         }
         
         return true;
      }
      
      public void setConnectedFigure(Figure newConnectedFigure) {
         myConnectedFigure = newConnectedFigure;
      }
      
      public Figure getConnectedFigure() {
         return myConnectedFigure;
      }
   }
   */
}
