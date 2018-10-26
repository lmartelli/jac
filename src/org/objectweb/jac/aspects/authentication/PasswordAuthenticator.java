/*
  Copyright (C) 2002 Laurent Martelli <laurent@aopsys.com>
  
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

import org.objectweb.jac.aspects.gui.DisplayContext;
import org.objectweb.jac.aspects.gui.GuiAC;
import org.objectweb.jac.core.Collaboration;
import org.objectweb.jac.core.Display;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.core.rtti.MethodItem;
import org.objectweb.jac.util.Log;

/**
 * This Authenticator ask for a username and password. The number of
 * allowed attempts to enter a valid (username,password) is
 * configurable. It needs a DisplayContext attribute in order to
 * be able to interact with the user. */

public abstract class PasswordAuthenticator implements Authenticator {   

    int retries = 3;

    /**
     * The default constructor with 3 retries in case of failing. */

    public PasswordAuthenticator() {}

    /**
     * This constructor can be used to parametrize the number of
     * retries.
     *
     * @param retries the number of retries */

    public PasswordAuthenticator(int retries) {
        this.retries = retries;
    }

    /**
     * This method perform the authentication by calling the
     * askUserNameAndPassword and checkPassword methods.
     *
     * @return the username if the authentication succeded, null
     * otherwise
     *
     * @see #askUsernameAndPassword(String,String)
     * @see #checkPassword(String,String) */

    public String authenticate() throws AuthenticationFailedException {
        DisplayContext context = 
            (DisplayContext)Collaboration.get().getAttribute(GuiAC.DISPLAY_CONTEXT);
        if (context!=null) {
            Display display = context.getDisplay();
            MethodItem method = 
                ClassRepository.get().getClass(this).getMethod(
                    "askUsernameAndPassword");
            String username = null;
            for (int i=retries; i>0 && username==null; i--) {
                Object[] parameters = new Object[method.getParameterTypes().length];
                if (!display.showInput(this,method,parameters)) {
                    // cancel
                    Log.trace("authentication","Authentication cancelled");
                    return null;
                } 
                if (checkPassword((String)parameters[0],(String)parameters[1])) {
                    username = (String)parameters[0];
                }
            }
            if (username==null) {
                throw new AuthenticationFailedException(
                    "Wrong username and password");
            } else {
                return username;
            }
        } else {
            Log.warning("no display context available, cannot authenticate user");
            return null;
        }
    }
   
    /**
     * This method has to be implemented by subclasses to perform the
     * actual password checking mechanism.
     * 
     * @param username the username
     * @param password the password
     * @return true if the username exists and if the password is valid
     * for this username */

    abstract boolean checkPassword(String username, String password);

    /**
     * This empty method is used to popup a dialog on the current
     * display so that the user can fill in the authentication
     * information.
     *
     * @param username the username
     * @param password the password */

    public void askUsernameAndPassword(String username, String password) {}

}
