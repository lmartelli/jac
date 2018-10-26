/*
  Copyright (C) 2001-2003 Renaud Pawlak 

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

import org.aopalliance.intercept.ConstructorInvocation;
import org.aopalliance.intercept.MethodInvocation;
import org.objectweb.jac.core.*;

/**
 * This simple counter must wrap the methods of which calls have to be
 * counted.
 *
 * <p>In some cases, optimizations can be achieved by grouping
 * counts. See <code>OptimizedCountingWrapper</code>.
 *
 * @see OptimizedCountingWrapper */

public class SimpleCountingWrapper extends Wrapper {

    /** Stores the counter. */
    Counter counter = null;

    /**
     * Creates a new wrapper that uses the given counter.
     *
     * @param counter the counter */

    public SimpleCountingWrapper(AspectComponent ac, Counter counter) {
        super(ac);
        this.counter = counter;
    }

    /**
     * This wrapping method increments the counter when the wrapped
     * method is called.
     *
     * @return the return value of the wrapped method */

    public Object incr(Interaction interaction) {
        Object ret = proceed(interaction);
        counter.incr(1);
        printCounter();
        return ret;
    }

    /** Role method: set the counter value.
     *
     * @param value the new counter value
     * @see #getCounter()
     * @see #incr(Interaction) */

    public void setCounter(int value) {
        counter.set(value);
    }
   
    /** Role method: get the counter value.
     *
     * @return the counter value
     * @see #setCounter(int)
     * @see #incr(Interaction) */

    public int getCounter() {
        return counter.get();
    }

    /**
     * Prints the counter in <code>System.out</code>.
     */
    public void printCounter() {
        System.out.println("<<< Counting aspect says : " + 
                           counter.get() + 
                           " line(s) printed. >>>");
    }

    public Object invoke(MethodInvocation invocation) throws Throwable {
        return incr((Interaction)invocation);
    }

    public Object construct(ConstructorInvocation invocation) throws Throwable {
        return incr((Interaction)invocation);
    }  

}



