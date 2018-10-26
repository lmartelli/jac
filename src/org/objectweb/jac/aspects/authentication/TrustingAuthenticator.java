/*
  Copyright (C) 2001 Laurent Martelli <laurent@aopsys.com>
  
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

import org.objectweb.jac.aspects.gui.DisplayContext;
import org.objectweb.jac.aspects.gui.GuiAC;
import org.objectweb.jac.core.Collaboration;
import org.objectweb.jac.core.Display;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.core.rtti.MethodItem;
import org.objectweb.jac.util.Log;

/**
 * This Authenticator just ask the username of the user without asking
 * a password. It just trusts the user to enter the correct
 * username. It needs a DisplayContext attribute in order to be able
 * to interact with the user.
 */
public class TrustingAuthenticator implements Authenticator {   
    public String authenticate() throws AuthenticationFailedException {
        DisplayContext context = 
            (DisplayContext)Collaboration.get().getAttribute(GuiAC.DISPLAY_CONTEXT);
        if (context!=null) {
            Display display = context.getDisplay();
            MethodItem method = 
                ClassRepository.get().getClass(this).getMethod("askUsername");
            Object[] parameters = new Object[method.getParameterTypes().length];
            if (!display.showInput(this,method,parameters)) {
                return null;
            } 
            return (String)parameters[0];
               
        } else {
            Log.warning("No display context available, cannot authenticate user");
            return null;
        }
    }
    public void askUsername(String username) {
    }
}
