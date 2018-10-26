/*

  Copyright (C) 2001 Renaud Pawlak, Laurent Martelli
  
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

package org.objectweb.jac.core.rtti;

/**
 * This exception is thrown when the user tries to construct a new
 * meta item that is not matching the
 * <code>java.lang.reflect</code> element it delegates to.<p>
 *
 * @author <a href="mailto:laurent@aopsys.com">Laurent Martelli</a> 
 */
public class InvalidDelegateException extends Exception {
    /**
     * @param delegate the delegate that caused the exception
     * @param message a message explaining the error
     */
    public InvalidDelegateException(Object delegate, String message) {
        super(delegate+": "+message);
        this.delegate = delegate;
    }
    Object delegate;
    public Object getDelegate() {
        return delegate;
    }
}


