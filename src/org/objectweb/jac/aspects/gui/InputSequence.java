/*

  Copyright (C) 2001 Renaud Pawlak, Laurent Martelli
  
  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 2 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  USA */

package org.objectweb.jac.aspects.gui;

import org.objectweb.jac.core.Display;
import org.objectweb.jac.core.rtti.AbstractMethodItem;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.util.Stack;

/**
 * This class allows a GUI programmer to create input sequences to ask
 * the user to fill a set of parameters when invoking a method.
 *
 * <p>By default, when invoking a method through the GUI, an
 * <code>InputWrapper</code> opens a dialog to fill the parameters
 * values when needed. If an input sequence is attached to this method
 * (see <code>GuiAC.setInputSequence</code>), then the input wrappers
 * will ask for the parameters using several input dialogs, each one
 * corresponding to a step of the input sequence.
 *
 * <p>Defining a new input sequence is done by concretizing this
 * class. For instance, the following class defines a sequence with
 * two steps that open input dialogs from some prototypes defined in
 * the class. The first steps asks for a boolean value that will
 * dinamically determines the second step input.
 *
 * <pre>
 * public class MyInputSequence extends InputSequence {
 *    public MyInputSequence( Display display, 
 *                            AbstractMethodItem method, 
 *                            Object[] parameters ) {
 *       super(display,method,parameters);
 *    }
 *    public int getNbSteps() {
 *       return 2;
 *    }
 *    public void init() {}
 *    public void validate() {
 *       Object[] values = getStepValues(2);
 *       setParameterValue(0, values[0]);
 *       Boolean firstStepResult = getStepValues(1)[0];
 *       if ( firstStepResult.booleanValue() ) {
 *          setParameterValue(1, values[1]);
 *       } else {
 *          setParameterValue(1, null);
 *       }
 *    }
 *    public AbstractMethodItem getStepMethod( int step ) {
 *       if( step == 1 ) {
 *          return getLocalInputMethod( "myPrototype1" );
 *       } else if ( step == 2 ) {
 *          Object[] values = getStepValues(1);
 *          Boolean firstStepResult = values[0];
 *          if ( firstStepResult.booleanValue() ) {
 *             return getLocalInputMethod( "myPrototype2" );
 *          } else {
 *             return getLocalInputMethod( "myPrototype3" );
 *       } else {
 *          return null; 
 *       }
 *    }
 *    public void myPrototype1( boolean b ) {}
 *    public void myPrototype2( String s ) {}
 *    public void myPrototype3( String s1, String s2 ) {}
 * }
 * </pre>
 *
 * @see InputWrapper
 *
 * @author <a href="mailto:pawlak@cnam.fr">Renaud Pawlak</a> */

public abstract class InputSequence {

   AbstractMethodItem method;
   Object[] parameters;
   Display display;

   Stack stepParameters = new Stack();
   int currentStep = 0;

   /**
    * Creates a user-defined input sequence.
    *
    * @param display the display to use to show the input boxes
    * @param method the method that will be finally invoked at the end
    * of the input sequence
    * @param parameters the array that contains the parameters of the
    * invoked method */

   public InputSequence( Display display, AbstractMethodItem method, 
                         Object[] parameters ) {
      this.display = display;
      this.method = method;
      this.parameters = parameters;
   }

   /**
    * This method is called when a new invocation is performed on the
    * method.
    *
    * <p>Define it if some objects must be dynamically constructed to
    * handle the sequence. */

   public abstract void init();

   /**
    * This method is called when the input sequence is finished and
    * when the user validates the last step input.
    *
    * <p>Define this method to fill the method parameters from the
    * values found in all the performed steps.
    *
    * @return true is the input is valid (false cancels the
    * invocation)
    * @see #getStepValues(int)
    * @see #setParameterValue(int,Object) */

   public abstract boolean validate();

   /**
    * Define this method to return the number of steps (can
    * dynamically change regarding the inputted vaules of the steps).
    *
    * @return the number of steps of the input sequence */

   public abstract int getNbSteps();

   /**
    * Returns the current step (indexed from 1).
    *
    * @return the current step */

   public final int getCurrentStep() {
      return currentStep;
   }

   /**
    * Tells if the sequence has a next step to perform after the
    * current one.
    *
    * @return true if a next step to perform */

   public final boolean hasNextStep() {
      return currentStep < getNbSteps();
   }

   /**
    * Returns the method that is used to define the input box for a
    * given step.
    *
    * <p>This is the most important method since it defines the shape
    * of an input box step. You should define a set of local method
    * with the right prototype and get their correponding method item
    * with the <code>getLocalInputMethod</code> method.
    *
    * <p>Note that the method is not actually called but is only used
    * through the <code>Display.showIntput</code> method.
    * 
    * @param step the step (indexed from 1)
    * @return the method that is used to create the input
    * @see org.objectweb.jac.core.Display#showInput(Object,AbstractMethodItem,Object[])
    * @see #getLocalInputMethod(String) */

   public abstract AbstractMethodItem getStepMethod( int step );      

   /**
    * Call this method on a new input sequence to process the inputs.
    *
    * @return false if some error happened or if an input step was
    * cancelled by the user */

   public final boolean proceedInputs() {
      init();
      while( hasNextStep() ) {
         if ( ! nextStep() ) return false;
      }
      return validate();
   }
      
   /**
    * Process the next step.
    * @return true if ok */

   public final boolean nextStep() {
      currentStep++;
      AbstractMethodItem stepMethod = getStepMethod( currentStep );
      Object[] params = null;
      stepParameters.push( params = new Object[stepMethod.getParameterTypes().length] );
      return display.showInput( null, stepMethod, params );
   }

   /**
    * Process the previous step back.
    * @return true if ok */

   public boolean previousStep() {
      stepParameters.pop();
      currentStep--;
      AbstractMethodItem stepMethod = getStepMethod( currentStep );
      return display.showInput( null, stepMethod, (Object[])stepParameters.peek() );
   }

   /**
    * Returns the method item that corresponds to a method defined in
    * the user-defined input sequence.
    *
    * @param name the method name 
    * @return the corresponding method item */

   protected AbstractMethodItem getLocalInputMethod( String name ) {
      return ClassRepository.get().getClass( this.getClass() ).getMethod( name );
   }

   /**
    * Returns the values that were entered by the user for a given
    * step.
    *
    * @param step the step number (indexed from 1) 
    * @return the user-inputted values */

   protected Object[] getStepValues( int step ) {
      if( currentStep < step ) {
         throw new RuntimeException("Step "+step+" result is not available yet");
      }
      return (Object[])stepParameters.get(step-1);
   }

   /**
    * Sets the parameter value for the final call of the method that
    * will be invoked at the end of the sequence.
    *
    * @param i the parameter index
    * @param value the value */
   
   protected void setParameterValue( int i, Object value ) {
      parameters[i] = value;
   }
}


