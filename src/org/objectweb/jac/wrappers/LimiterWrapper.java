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

package org.objectweb.jac.wrappers;

import org.aopalliance.intercept.ConstructorInvocation;
import org.aopalliance.intercept.MethodInvocation;
import org.objectweb.jac.core.AspectComponent;
import org.objectweb.jac.core.Interaction;
import org.objectweb.jac.core.Wrapper;

/**
 * <code>LimiterWrapper</code> implements a wrapper that tests a
 * counter value before calling the wrappee object. If the counter is
 * not with the max and min limits, it raises the
 * <code>LimiterException</code>
 * 
 * <p>Use this wrapper as follows:
 *
 * <ul><pre>
 * LimiterWrapper lw = new LimiterWrapper(0, 100);
 * a_stack.wrap(lw, "inc", "push");
 * a_stack.wrap(lw, "dec", "pop");
 * a_stack.wrap(lw, "testMax", "push");
 * a_stack.wrap(lw, "testMin", "pop");
 * </pre></ul>
 * 
 * <p>Where <code>a_stack</code> is an instance of a class
 * <code>Stack</code>. When wrapping the stack, it will raise a
 * <code>LimiterException</code> if a push is done and that the
 * counter is greater or equal to 100 or if a pop is done and that the
 * counter is lower or equal to 0.
 *
 * <p> NOTE: this class cannot be used alone. Its instances must 
 * wrap some instances of other objects (possibly other
 * wrappers).
 *
 * @see org.objectweb.jac.core.Wrappee
 * @see org.objectweb.jac.core.Wrapping#wrap(Wrappee,Wrapper,AbstractMethodItem)
 * @see org.objectweb.jac.wrappers.LimiterException */

public class LimiterWrapper extends Wrapper {

   /** Store the maximum bound of the limiter. */
    protected int max;
   /** Store the minimum bound of the limiter. */
    protected int min;
   /** Store the counter of the limiter. */
    protected int counter = 0;

   /**
    * Construct a new limiter and initialize the bounds.
    *
    * @param min the minimum counter value.
    * @param max the maximum counter value.
    */
   public LimiterWrapper(AspectComponent ac, int min, int max) {
      super(ac);
      this.min = min;
      this.max = max;
   }

   /**
    * Return the max bound of the limiter.
    *
    * @return the max bound
    */
   public int getMax() {
      return max;
   }
   
   /**
    * Set the max bound of the limiter.
    *
    * @param max the new max bound
    */
   public void setMax(int max) {
      this.max = max;
   }

   /**
     * Return the current counter of the limiter.
     *
     * @return the counter
     */
   public int getCounter() {
      return counter;
   }

    /**
     * This wrapping method increments the limiter counter and calls
     * the wrappee method.
     *
     * <p>For instance, <code>inc</code> could wrap the
     * <code>push</code> method of a stack so that the counter is
     * incremented when a new element is placed on the top of the
     * stack.
     *
     * <p>NOTE: this method do not test the bounds. Use
     * <code>testMax</code> to do this.
     *
     * @see LimiterWrapper#testMax(Interaction)
     *
     * @return the original method return value
     */
    
    public Object inc(Interaction interaction) {
       counter++;
       return proceed(interaction);
    }

    /**
     * This wrapping method decrements the limiter counter and calls
     * the wrappee method.
     *
     * <p>For instance, <code>dec</code> could wrap the
     * <code>pop</code> method of a stack so that the counter is
     * incremented when an element is removed from it.
     *
     * <p>NOTE: this method do not test the bounds. Use
     * <code>testMin</code> to do this.
     *
     * @see LimiterWrapper#testMin(Interaction)
     *
     * @return the original method return value
     */
     
    public Object dec(Interaction interaction) {
       counter--;
       return proceed(interaction);
    }
    
    /**
     * This wrapping method tests the counter of the limiter and
     * raises the <code>LimiterExeption</code> when when it is over
     * the maximum value.
     *
     * <p>NOTE: this method must be used with the <code>inc</code> and
     * <code>dec</code> wrapping methods that mofify the counter
     * value.
     *
     * @see LimiterWrapper#inc(Interaction)
     * @see LimiterWrapper#dec(Interaction)
     *
     * @return the original method return value or raise an exception
     */
     
    public Object testMax(Interaction interaction)
       throws LimiterException {
       if(counter >= max) {
          throw new LimiterException("counter reached its maximum count");
       }
       return proceed(interaction);
    }

    /**
     * This wrapping method tests the counter of the limiter and
     * raises the <code>LimiterExeption</code> when when it is below
     * the minimum value.
     *
     * <p>NOTE: this method must be used with the <code>inc</code> and
     * <code>dec</code> wrapping methods that mofify the counter
     * value.
     *
     * @see LimiterWrapper#inc(Interaction)
     * @see LimiterWrapper#dec(Interaction)
     *
     * @return the original method return value or raise an exception
     */
     
    public Object testMin(Interaction interaction)
       throws LimiterException {
       if(counter <= min) {
          throw new LimiterException("counter reached its minimum count");
       }
       return proceed(interaction);
    }

	/* (non-Javadoc)
	 * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
	 */
	public Object invoke(MethodInvocation invocation) throws Throwable {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.aopalliance.intercept.ConstructorInterceptor#construct(org.aopalliance.intercept.ConstructorInvocation)
	 */
	public Object construct(ConstructorInvocation invocation) throws Throwable {
		// TODO Auto-generated method stub
		return null;
	}

}
