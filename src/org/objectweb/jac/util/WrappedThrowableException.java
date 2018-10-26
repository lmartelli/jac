/*
  Copyright (C) 2001 Renaud Pawlak <renaud@aopsys.com>

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

package org.objectweb.jac.util;

import java.lang.reflect.InvocationTargetException;



/**
 * This runtime exeption wraps a regular exception so that JAC objects
 * can also send non-runtime exceptions.
 *
 * @author <a href="mailto:pawlak@cnam.fr">Renaud Pawlak</a> 
 */

public class WrappedThrowableException extends RuntimeException {
   
    /** Strores the throwable object that is wrapped by this runtime
        exception. */
    private Throwable wrappedThrowable;
   
    /**
     * Creates the wrapping exception.<p>
     *
     * @param wrappedThrowable the throwable that is wrapped by this
     * exception 
     */
    public WrappedThrowableException(Throwable wrappedThrowable) {
        if (wrappedThrowable instanceof InvocationTargetException) {
            this.wrappedThrowable = 
                ((InvocationTargetException)wrappedThrowable).getTargetException();
        } else {
            this.wrappedThrowable = wrappedThrowable;
        }
    }

    /**
     * Gets the wrapped throwable.
     *
     * @return the wrapped throwable 
     */
    public Throwable getWrappedThrowable() {
        return wrappedThrowable;
    }
   
    /** 
     * Returns the string depicting the exception. It is the message of
     * the wrapped throwable.<p>
     *
     * @return the printable representation of the wrapped exception 
     */
    public String toString() {
        return "WrappedThrowableException("+wrappedThrowable.toString()+")";
    }

    /**
     * Prints the stack trace that has been filled when the exception
     * was created.<p>
     *
     * This method delegates to the wrapped throwable.<p> 
     */
    public void printStackTrace() { 
        wrappedThrowable.printStackTrace();
    }
   
    /**
     * Prints the wrapped throwable and its backtrace to the 
     * specified print stream.
     *
     * @param s the stream 
     */
    public void printStackTrace(java.io.PrintStream s) { 
        wrappedThrowable.printStackTrace(s);
    }

    /**
     * Prints the wrapped throwable and its backtrace to the 
     * specified print writer.
     *
     * @param s the writer 
     */
    public void printStackTrace(java.io.PrintWriter s) {  
        wrappedThrowable.printStackTrace(s);
    }

    /**
     * Returns the error message string of the wrapped throwable
     * object.<p>
     *
     * @return the error message string 
     */
    public String getMessage() {
        return wrappedThrowable.getMessage();
    }

    public String getLocalizedMessage() {
        return wrappedThrowable.getLocalizedMessage();
    }
    
    public Throwable getCause() {
        return wrappedThrowable;
    }
}
