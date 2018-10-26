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

package org.objectweb.jac.aspects.authentication;

/**
 * This exception is thrown when the access to a given method is
 * denied for the current user.
 *
 * <p>The authentication process may fail for other reasons, then the
 * <code>AuthenticationFailedException</code> is thrown.
 * 
 * @see AuthenticationFailedException */

public class AccessDeniedException extends Exception {

    /**
     * Constructs a new exception with no particular message.
     */

    public AccessDeniedException() {}

    /**
     * Constructs a new exception.
     *
     * @param message an associated message */

    public AccessDeniedException( String message ) {
        super( message );
    }


}
