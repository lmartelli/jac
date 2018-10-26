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
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA */

package org.objectweb.jac.aspects.authentication;

import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.MethodItem;


/**
 * This is the configuration interface of the authentication aspect.
 *
 * <p>The authentication aspect's goal is to make sure that a user
 * attribute is defined within the context. Several authentication
 * policies are available and defined in the controller.
 *
 * @see AuthenticationAC
 * @see #setAuthenticator(ClassItem)
 * @see #setAuthenticator(ClassItem,String[]) */

public interface AuthenticationConf {   

   /**
    * This configuration method restricts a given set of methods.
    *
    * <p>When a method is restricted, the collaboration that contains
    * the restricted method invocation must contain an attribute that
    * represents the current user. If not, the authentication aspect
    * ask the user to input its caracteristics.<p>
    *
    * @param classes a class expression
    * @param objects an object expression
    * @param methods an expression matching the methods to restrict
    * @see #addRestrictedObjects(String)
    * @see AuthenticationWrapper
    */
   
   void addRestrictedMethods(String classes, 
                             String methods, 
                             String objects );

   /**
    * This configuration method sets a controller method to a set of
    * base methods.
    *
    * <p>Once the user is authenticated, the controller method is
    * called with the user and the wrappee and the wrapped method as
    * parameters. If the controller returns true, the user is allowed
    * to call the method, otherwise an exception is raised.</p>
    *
    * @param classes a class expression
    * @param methods an expression matching the methods to restrict
    * @param controller the controller method (a static method of the
    * prototype <code>boolean controller(String username,Object
    * wrappee,MethodItem method)</code>)
    *
    * @see #setDisplayController(MethodItem)
    * @see org.objectweb.jac.aspects.user.UserAC#userController(String,Object,MethodItem)
    * @see AuthenticationWrapper#dummyController(String,Object,MethodItem)
    * @see AuthenticationWrapper 
    */
   void setController(String classes, String methods, MethodItem controller);

   /**
    * This configuration method sets a controller on displays so that
    * all users must authenticate themselves before accessing the
    * application.
    *
    * @param controller the controller method (a static method of the
    * prototype <code>boolean controller(String username,Object
    * wrappee,MethodItem method)</code>)
    *
    * @see #setController(String,String,MethodItem)
    * @see org.objectweb.jac.aspects.user.UserAC#userController(String,Object,MethodItem)
    * @see AuthenticationWrapper#dummyController(String,Object,MethodItem)
    * @see AuthenticationWrapper */
   void setDisplayController(MethodItem controller);

   /**
    * Sets the message that is showed to the user when the access to a
    * method is not granted by the controller (if any).
    *
    * @param message the message to popup
    * @see #setController(String,String,MethodItem) 
    */
   void setAccessDeniedMessage(String message);

   /**
    * Restricts some objects for authentication (all their methods).
    *
    * @param objects an object expression
    * @see #addRestrictedMethods(String,String,String)
    * @see AuthenticationWrapper */
   
   void addRestrictedObjects(String objects);

   /**
    * Restricts some objects for authentication.
    *
    * @param classes a class expression
    * @param objects an object expression
    * @see #addRestrictedMethods(String,String,String)
    * @see AuthenticationWrapper 
    */
   void addRestrictedObjects(String objects,String classes);

   /**
    * Sets the authenticator to use.
    *
    * <p>The most used authenticator is the
    * <code>org.objectweb.jac.aspects.authentication.UserPasswordAuthenticator</code>. It
    * opens a popup on the current display to ask the user its login
    * and password.</p>
    *
    * @param authenticatorClass the authenticator's class
    * @param parameters parameters to give to the constructor
    * @see Authenticator
    * @see UserPasswordAuthenticator */

   void setAuthenticator(ClassItem authenticatorClass, String[] parameters);

   /**
    * Sets the authenticator to use when the authenticator's
    * constructor takes no parameters.
    * 
    * <p>The most used authenticator is the
    * <code>org.objectweb.jac.aspects.authentication.UserPasswordAuthenticator</code>. It
    * opens a popup on the current display to ask the user its
    * login and password.</p>
    *
    * @param authenticatorClass the authenticator's class
    * @see #setAuthenticator(ClassItem,String[])
    * @see Authenticator
    * @see UserPasswordAuthenticator */

   void setAuthenticator(ClassItem authenticatorClass);

}
