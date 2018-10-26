/*
  Copyright (C) 2001-2002 Renaud Pawlak, Laurent Martelli
  
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

import org.objectweb.jac.core.rtti.AbstractMethodItem;

/**
 * This exception is thrown by the input wrapper when an input process
 * is canceled by the user.
 *
 * @see InputWrapper
 */

public class InputCanceledException extends RuntimeException {

   AbstractMethodItem method;

   /**
    * The constructor for this exception.
    *
    * @param method the method that was invoked and that was canceled
    * by the user during its parameter values input process 
    */
    public InputCanceledException(AbstractMethodItem method) {
       super("Input canceled by user for method "+method.getName());
       this.method = method;
    }
}

