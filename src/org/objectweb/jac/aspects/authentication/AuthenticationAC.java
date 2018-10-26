/*
  Copyright (C) 2001-2002 Renaud Pawlak <renaud@aopsys.com>
                          Laurent Martelli <laurent@aopsys.com>
  
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

import java.util.HashSet;
import java.util.Set;
import org.objectweb.jac.core.AspectComponent;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.MethodItem;
import org.objectweb.jac.util.ExtArrays;
import org.objectweb.jac.util.Log;

/**
 * This AC weaves the authentication aspect.
 *
 * <p>The authentication ensures that the authenticated method are
 * called only when the user is known in the context. An external
 * controller (such as the one defined by the user aspect) can by used
 * to actually grant of refuse the access.
 *
 * @see AuthenticationWrapper
 * @see org.objectweb.jac.aspects.user.UserAC */

public class AuthenticationAC extends AspectComponent 
    implements AuthenticationConf {   

    /** The contextual attribute that contains the authenticated user
        if any. */
    public static final String USER = "AuthenticationAC.USER";

    /** Stores the trusted users. */
    protected HashSet trustedUsers = new HashSet();
   
    /**
     * Tells if a given user is trusted or not.
     *
     * @param username the user's name
     * @return true if the user has been added to the trusted users
     * list */
   
    public boolean isTrustedUser(String username) 
    {
        Log.trace("authentication","isTrustedUser("+username+")");
        return trustedUsers.contains(username);
    }

    /**
     * Returns all the declared trusted users.
     *
     * @see #addTrustedUser(String) */

    public Set getTrustedUsers() {
        return trustedUsers;
    }

    AuthenticationWrapper wrapper;
    AuthenticationWrapper getWrapper() {
        if (wrapper==null) {
            wrapper = new AuthenticationWrapper(this,authenticator,null);
            //wrapper.setAspectComponent(ACManager.get().getName(this));
        }
        return wrapper;
    }

    // AuthenticationConf interface

    public void addTrustedUser(String username) {
        Log.trace("authentication","addTrustedUser("+username+")");
        trustedUsers.add(username);
    }

    public void setController(String classes,
                              String methods,
                              MethodItem controller) {
        Log.trace("authentication","setController("+
                  classes+","+methods+","+controller+")");
        getWrapper().setController(controller);
        //wrapper.setAspectComponent(ACManager.get().getName(this));
        pointcut("ALL",classes,methods,
                 wrapper,null);
    }

    public void setDisplayController(MethodItem controller) {
        setController("org.objectweb.jac.core.Display",
                      ".*showCustomized.* || .*fullRefresh.*",
                      controller);
    }

    public void setAccessDeniedMessage(String message) {
        getWrapper().setAccessDeniedMessage(message);
    }

    public void addRestrictedMethods(String classes,
                                     String methods,
                                     String objects ) {
        Log.trace("authentication","addRestrictedMethods("+
                  classes+","+methods+","+objects+")");
        pointcut(objects,classes,methods,
                 getWrapper(),null);
    }

    public void addRestrictedObjects(String objects) {
        pointcut(objects,"ALL","ALL",
                 getWrapper(),null);
    }

    public void addRestrictedObjects(String objects, String classes) {
        pointcut(objects,classes,"ALL",
                 getWrapper(),null);
    }

    Authenticator authenticator;

    public void setAuthenticator(ClassItem authenticatorClass) {      
        setAuthenticator(authenticatorClass, ExtArrays.emptyStringArray);
    }   

    public void setAuthenticator(ClassItem authenticatorClass, String[] parameters) {
        Log.trace("authentication","setAuthenticator("+authenticatorClass+")");
        try {
            authenticator = (Authenticator)authenticatorClass.newInstance(parameters);
        } catch(Exception e) {
            throw new RuntimeException("Failed to instanciate authenticator "+
                                       authenticatorClass+": "+e);
        }
        getWrapper().setAuthenticator(authenticator);
    }   

}
