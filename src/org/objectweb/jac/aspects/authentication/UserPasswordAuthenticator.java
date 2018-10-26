/*
  Copyright (C) 2002-2003 Renaud Pawlak <renaud@aopsys.com>
  
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
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA */

package org.objectweb.jac.aspects.authentication;

import org.objectweb.jac.aspects.user.UserAC;
import org.objectweb.jac.core.ACManager;
import org.objectweb.jac.util.Log;

/**
 * This Authenticator asks for a username and password and checks them
 * by using the user aspect.
 *
 * @see org.objectweb.jac.aspects.user.UserAC */

public class UserPasswordAuthenticator extends PasswordAuthenticator {

    UserAC userAC;
    String userAspectName;

    /**
     * Constructor.
     *
     * @param userAspectName the name of the user aspect for the
     * configured application (note that we should implement a means to
     * resolve an aspect). It has the form 
     * &lt;application_name&gt;.&lt;aspect_name&gt; 
     */
    public UserPasswordAuthenticator(String userAspectName) {
        this.userAspectName=userAspectName;
    }

    /**
     * Implements the password checking.
     *
     * <p>This method asks to the user aspect which is the currently
     * user's instance of the current session and checks if the
     * username and password values corresponds to the values of the
     * corresponding fields as declared in the user aspect.
     *
     * @param username the username to check
     * @param password the password to check
     * @return true if matching, false otherwise
     *
     * @see org.objectweb.jac.aspects.user.UserAC
     * @see org.objectweb.jac.aspects.user.UserAC#setUserClass(ClassItem,String,String)
     * @see org.objectweb.jac.aspects.user.UserAC#getUserFromLogin(String)
     * @see org.objectweb.jac.aspects.user.UserAC#getUserLogin(Object)
     * @see org.objectweb.jac.aspects.user.UserAC#getUserPassword(Object) 
     */
    boolean checkPassword(String username, String password) {
        if (userAC==null) {
            userAC=(UserAC)ACManager.get().getObject(userAspectName);
        }
        if (userAC==null) {
            Log.error("UserPasswordAuthenticator: cannot perform "+
                      "password authentication, no user aspect found.");
            return false;
        } else {
            Object user=userAC.getUserFromLogin(username);
            Log.trace("authentication","checking "+username+"=="+
                      userAC.getUserLogin(user)+" && "+password+"=="+
                      userAC.getUserPassword(user)+" (user="+user+")");
            return username.equals(userAC.getUserLogin(user)) &&
                password.equals(userAC.getUserPassword(user));
        }
    }

}
