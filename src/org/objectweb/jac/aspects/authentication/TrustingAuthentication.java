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

import org.objectweb.jac.util.*;

/**
 * This class performs a trusting authentication.
 *
 * <p>It is used by the authentication wrapper.
 *
 * @see AuthenticationWrapper
 */

public class TrustingAuthentication {

    /**
     * Creates a new trusting authentication. */

    public TrustingAuthentication() {}   

    /**
     * Performs the authentication of the user given by username.
     *
     * @param userName the name of the user to authenticate */

    public void authenticate( String userName ) {
        Log.trace("authentication","Authenticated user "+userName);
    }
   
    public void authenticateWithPassword(String userName, String password) {
        Log.trace("authentication","Authenticated user "+userName);
    }
}
