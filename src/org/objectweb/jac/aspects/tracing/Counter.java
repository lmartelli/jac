/*
  Copyright (C) 2001 Renaud Pawlak

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

/**
 * The functional part of the counting aspect: a counter object.
 *
 * <p>This component is used by the SimpleCountingWrapper and the
 * OptimizedCountingWrapper to count the invocations performed on the
 * counted methods.
 *
 * @see SimpleCountingWrapper
 * @see OptimizedCountingWrapper */

public class Counter {

   /** Stores the counter value. */
   int counter = 0;

   /**
    * Increments the counter with a given value.
    */

   public void incr ( int value ) {
      counter+=value;
   }

   /**
    * Sets the counter value.
    *
    * @param value the new counter value
    * @see #get()
    * @see #incr(int) */

   public void set( int value ) {
      counter = value;
   }
   
   /** 
    * Gets the counter value.
    *
    * @return the counter value
    * @see #set(int)
    * @see #incr(int) */

   public int get() {
      return counter;
   }

   /**
    * Print the counter in <code>System.out</code>.
    */
   public void printCounter() {
      System.out.println("<<< Counting aspect says : " + counter + 
                         " line(s) printed. >>>");
   }  

}



