/*
  Copyright (C) 2001 Renaud Pawlak <renaud@aopsys.com>

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 2 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA */

package org.objectweb.jac.aspects.tracing;

import org.objectweb.jac.core.*;
import java.util.*;
import java.io.*;

/**
 * This class is a simple debugger that can by used by a program to
 * step the called methods and print some informations.
 *
 * <p>It is used by the debugging wrapper.
 *
 * @see DebuggingWrapper
 * @see DebuggingWrapper#step(Interaction) */

public class Debugger {

   /** Constant for stepping. */
   public static final int STEP = 0;
   /** Constant for stepping into */
   public static final int STEP_INTO = 1;

   /** Store the current debugging mode. */
   public int mode = STEP;
   /** Store if the debugger must step or not. */
   public boolean stepping = true;
   /** A stack that allows step into to stop. */
   public transient Stack stepIntoStack;
   
   /** The debugger constructor. */
   
   public Debugger() {
      stepIntoStack = new Stack();
   }

   /**
    * Set the debugging mode of the debugger. Can be "step",
    * "step_into", or "run".
    *
    * @param mode the new mode
    *
    * @see #getDebuggingMode() */

   public void setDebuggingMode( int mode ) {
      this.mode = mode;
   }

   /**
    * The getter for the debugging mode.
    *
    * @return the current debugging mode
    *
    * @see #setDebuggingMode(int)
    */

   public int getDebuggingMode() {
      return mode;
   }

   /**
    * Disable stepping.
    * 
    * <p>If this method is called, the debugger enters a run mode but
    * is still active (a stepping mode can be recovered).
    * 
    * @see #isStepping()
    * @see #enableStepping() */
   
   public void disableStepping() {
      stepping = false;
   }

   /**
    * Enable stepping
    * 
    * <p>If this method is called and that the stepping was disabled,
    * the debugger enters a stepping mode.
    * 
    * @see #isStepping()
    * @see #disableStepping() */

   
   public void enableStepping() {
      stepping = true;
   }   

   /**
    * Tell if in stepping mode.
    *
    * @return true if stepping
    *
    * @see #enableStepping()
    * @see #disableStepping() */
   
   public boolean isStepping() {
      return stepping;
   }

   /**
    * Must be called when a new method is called.
    *
    * <p>If the debugger is in step mode, then, the program stops and
    * the user is asked to press a key to continue.
    *
    * @param container the name of the container that runs the method
    * @param objectName the name of the called object
    * @param method the name of the called method
    * @param args the arguments of the called method */

   public void startOfMethod( String container,
                              String objectName, 
                              String method, 
                              Object[] args ) {

      if( isStepping() && getDebuggingMode() == STEP_INTO ) {
         if( stepIntoStack.isEmpty() ) {
            setDebuggingMode( STEP );
         } else {
            stepIntoStack.push( "" );
         }
      }

      if( isStepping() && getDebuggingMode() == STEP && 
          Collaboration.get().getAttribute( "step_into" ) == null ) {

         System.out.println( "Debugging is calling " + method + " (on container " + container + "):" );
         System.out.println( "wrappee  = " + objectName );
         System.out.println( "args     = " + Arrays.asList( args ) );

         BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
         boolean ok = false;
         String answer = "";
         
         while (!ok) {
            
            System.out.println(" - [s]tep");
            System.out.println(" - step [i]nto");
            System.out.println(" - [r]un");
            System.out.println(" - [q]uit");
            System.out.print("> ");
            
            try {
               answer = in.readLine();

               if (answer.equals("q")) {
                  System.out.println("Ciao!");
                  System.exit(0);
               } else if (answer.equals("i")) {
                  stepIntoStack.push( "" );
                  setDebuggingMode( STEP_INTO );
                  ok = true;
               } else if (answer.equals("s")) {
                  setDebuggingMode( STEP );
                  ok = true;
               } else if (answer.equals("r")) {
                  disableStepping();
                  ok = true;
               }
               
            } catch (Exception e) {
               e.printStackTrace();
            }
            
         }
      }
   }

   /**
    * This must be called at the end of a stepped method to print the
    * execution informations of the method.
    *
    * @param container the name of the container that runs the method
    * @param objectName the name of the called object
    * @param method the name of the called method
    * @param args the arguments of the called method
    * @param ret the value returned by the called method
    * @param executionTime the method call duration */

   public void endOfMethod( String container,
                            String objectName, 
                            String method, 
                            Object[] args,
                            Object ret,
                            long executionTime ) {

      if( isStepping() && getDebuggingMode() == STEP_INTO ) {
         if( stepIntoStack.isEmpty() ) {
            setDebuggingMode( STEP );
         } else {
            stepIntoStack.pop();
         }
      }

      if( isStepping() && getDebuggingMode() == STEP && 
          Collaboration.get().getAttribute( "step_into" ) == null ) {

         System.out.println( "Debugging is returning from " + method + " (on container " + container + "):" );
         System.out.println( "wrappee  = " + objectName );
         System.out.println( "args     =" + Arrays.asList( args ) );
         System.out.println( "return   =" + ret );
         System.out.println( "duration = " + executionTime + " ms"
         );
      }
   }
}


   





