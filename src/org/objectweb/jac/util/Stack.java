/*
  Copyright (C) 2001 Laurent Martelli

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

package org.objectweb.jac.util;

public class Stack extends java.util.Stack {
    /**
     * Returns the i-th element from the top of the stack
     *
     * @param i index of element to get (0 is the top of the stack,
     * size()-1 is the bottom)
     *
     * @see #poke(int,Object)
     * @see #top()
     */
    public Object peek(int i) {
        return get(size()-1-i);
    }

    /**
     * Returns the top element of the stack
     */
    public Object top() {
        return peek(0);
    }

    /**
     * Returns the top element of the stack if it's not empty, null
     * otherwise.
     */
    public Object safeTop() {
        return empty()?null:peek(0);
    }

    /**
     * Sets the value of an element of the stack
     *
     * @param i index of element to set (0 is the top of the stack,
     * size()-1 is the bottom)
     * @param value the new value
     *
     * @see #peek(int)
     */
    public void poke(int i, Object value) {
        set(size()-1-i,value);
    }
    /**
    * Pops n elements from the top of the stack
    * @param n number of elements to pop off the stack
    */
    public void pop(int n) {
        for (;n>0; n--) {
            pop();
        }
    }
    /**
    * swap peek() and peek(1)
    */
    public void swap() {
        Object tmp = peek();
        poke(0,peek(1));
        poke(1,tmp);
    }
}
