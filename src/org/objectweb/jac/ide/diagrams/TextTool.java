/*
 * @(#)TextTool.java
 *
 * Project:		JHotdraw - a GUI framework for technical drawings
 *				http://www.jhotdraw.org
 *				http://jhotdraw.sourceforge.net
 * Copyright:	© by the original author(s) and all contributors
 * License:		Lesser GNU Public License (LGPL)
 *				http://www.opensource.org/licenses/lgpl-license.html
 */

package org.objectweb.jac.ide.diagrams;

import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.standard.TextHolder;
import org.objectweb.jac.util.Log;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;

/**
 * Tool to create new or edit existing text figures.
 * The editing behavior is implemented by overlaying the
 * Figure providing the text with a FloatingTextField.<p>
 * A tool interaction is done once a Figure that is not
 * a TextHolder is clicked.
 *
 * @see TextHolder
 * @see FloatingTextField
 *
 * @version <$CURRENT_VERSION$>
 */
public class TextTool extends CreationTool 
   implements ActionListener, KeyListener
{

   private FloatingTextField   fTextField;
   private TextHolder  fTypingTarget;

   public TextTool(DrawingEditor newDrawingEditor, Figure prototype) {
      super(newDrawingEditor, prototype);
   }

   /**
    * If the pressed figure is a TextHolder it can be edited otherwise
    * a new text figure is created.
    */
   public void mouseDown(MouseEvent e, int x, int y)
   {
      TextHolder textHolder = null;
      Figure pressedFigure = drawing().findFigureInside(x, y);
      if (pressedFigure instanceof TextHolder) {
         textHolder = (TextHolder) pressedFigure;
         if (!textHolder.acceptsTyping())
            textHolder = null;
      }
      if (textHolder != null) {
         beginEdit(textHolder);
         return;
      }
      if (getTypingTarget() != null) {
         endEdit();
         editor().toolDone();
      } else {
         super.mouseDown(e, x, y);
         // update view so the created figure is drawn before the floating text
         // figure is overlaid. (Note, fDamage should be null in StandardDrawingView
         // when the overlay figure is drawn because a JTextField cannot be scrolled)
         view().checkDamage();
         textHolder = (TextHolder)getCreatedFigure();
         beginEdit(textHolder);
      }
   }

   public void mouseDrag(MouseEvent e, int x, int y) {
   }

   public void mouseUp(MouseEvent e, int x, int y) {
   }

   /**
    * Terminates the editing of a text figure.
    */
   public void deactivate() {
      endEdit();
      super.deactivate();
   }

   /**
    * Sets the text cursor.
    */
   public void activate() {
      super.activate();
      view().clearSelection();
      // JDK1.1 TEXT_CURSOR has an incorrect hot spot
      //view().setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
   }

   /**
    * Test whether the text tool is currently activated and is displaying
    * a overlay TextFigure for accepting input.
    *
    * @return true, if the text tool has a accepting target TextFigure for its input, false otherwise
    */
   public boolean isActivated() {
      return getTypingTarget() != null;
   }
	
   // ActionListener
   public void actionPerformed(ActionEvent e) {
      endEdit();
   }

   // KeyListener
   public void keyPressed(KeyEvent e) {
      int code = e.getKeyCode();
      if (code==KeyEvent.VK_ESCAPE) {
         abortEdit();
      }
   }
   public void keyReleased(KeyEvent e) {}
   public void keyTyped(KeyEvent e) {}

   protected void beginEdit(TextHolder figure) {
      if (fTextField == null) {
         fTextField = new FloatingTextField();
      }
           
      if (figure != getTypingTarget() && getTypingTarget() != null) {
         endEdit();
      }

      fTextField.addActionListener(this);
      fTextField.addKeyListener(this);
      fTextField.createOverlay((Container)view(), figure.getFont());
      fTextField.setBounds(fieldBounds(figure), figure.getText());

      setTypingTarget(figure);
   }

   protected void endEdit() {
      Log.trace("diagram","End edit");
      if (getTypingTarget() != null) {
         if (fTextField.getText().length() > 0) {
            getTypingTarget().setText(fTextField.getText());
         } else {
            drawing().orphan((Figure)getAddedFigure());
         }
              
         setTypingTarget(null);
         fTextField.endOverlay();
         fTextField.removeActionListener(this);
         fTextField.removeKeyListener(this);
         //	        view().checkDamage();
      }
   }

   protected void abortEdit() {
      Log.trace("diagram","Abort edit");
      if (getTypingTarget() != null) {
         setTypingTarget(null);
         fTextField.endOverlay();
         fTextField.removeActionListener(this);
         fTextField.removeKeyListener(this);
         //	        view().checkDamage();
      }
   }

   private Rectangle fieldBounds(TextHolder figure) {
      Rectangle box = figure.textDisplayBox();
      int nChars = figure.overlayColumns();
      Dimension d = fTextField.getPreferredSize(nChars);
      return new Rectangle(box.x, box.y, d.width, d.height);
   }
	
   protected void setTypingTarget(TextHolder newTypingTarget) {
      fTypingTarget = newTypingTarget;
   }
	
   protected TextHolder getTypingTarget() {
      return fTypingTarget;
   }

}

