/*
  Copyright (C) 2002 Laurent Martelli <laurent@aopsys.com>
                     Renaud Pawlak <renaud@aopsys.com>

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 2 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  USA */

package org.objectweb.jac.aspects.user;

import org.objectweb.jac.core.rtti.CollectionItem;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.MethodItem;

public interface UserConf {

   /**
    * Sets the class of the application that must be used as a storage
    * for the users.
    *
    * <p>The users are eventually used to know what user is currently
    * logged into the system so you should define fields that can be
    * used to identify the user.
    *
    * @param userClass the class that represents the application's
    * users
    * @param loginField the field that stores the login (iow the
    * user's id)
    * @param passwordField the field that stores the password (not
    * mandatory it no password authentication is performed)
    * @param profileField the field that contains the profile (if
    * any, can be null)
    * @see org.objectweb.jac.aspects.authentication.UserPasswordAuthenticator */
 
   void setUserClass(ClassItem userClass, 
                     String loginField,
                     String passwordField,
                     String profileField);

   /**
    * This method should affect all the classes that define a
    * reference towards a user of the application and that should be
    * seamlessly initialized to the currently logged user (instead of
    * been filled in interactively).
    *
    * @param classExpr a class pointcut expression that denote all the
    * classes that should be affected by this behavior */

   void autoInitClasses(String classExpr);

   /**
    * Declares a new profile (that has no parent).
    * 
    * <p>A profile is a kind of user type that has some well-defined
    * persmissions to access or to modify elements of the configured
    * application (e.g. the default <code>user.acc</code> defines an
    * <code>administrator</code> profile that grants access and
    * modification of all the elements).
    * 
    * @param profile the profile's name
    * @see #declareProfile(String,String) */

   void declareProfile(String profile);

   /**
    * Use this config method to clear a profile so that it can be
    * reinitialized from the config file.
    * @param name name of the profile to clear
    */
   void clearProfile(String name);

   /**
    * Declares a new profile that inherits from the caracteristics of
    * its parent.
    *
    * <p>All the parent access permissions can be restrained (and only
    * restrained) by the child profile (a permission that is not
    * granted by the parent cannot be granted by the child).
    * 
    * @param profile the profile's name
    * @param parent the parent profile's name
    * @see #declareProfile(String) */

   void declareProfile(String profile,String parent);

   /**
    * Adds a readable resources set to a profile.
    *
    * @param profile the profile's name
    * @param resourceExpr a regular expression that denotes a set of
    * resources (based on <code>package.class.member</code>) */
   
   void addReadable(String profile,String resourceExpr);

   /**
    * Adds a writable resources set to a profile.
    *
    * @param profile the profile's name
    * @param resourceExpr a regular expression that denotes a set of
    * resources (based on <code>package.class.member</code>) */

   void addWritable(String profile,String resourceExpr);

   /**
    * Adds an removable resources set to a profile (collection
    * dedicated).
    *
    * @param profile the profile's name
    * @param resourceExpr a regular expression that denotes a set of
    * resources (based on <code>package.class.member</code>) 
    */
   void addRemovable(String profile,String resourceExpr);

   /**
    * Adds an addable resources set to a profile (collection
    * dedicated).
    *
    * @param profile the profile's name
    * @param resourceExpr a regular expression that denotes a set of
    * resources (based on <code>package.class.member</code>) 
    */
   void addAddable(String profile,String resourceExpr);


   /**
    * Adds a creatable resources set to a profile (collection
    * dedicated).
    *
    * @param profile the profile's name
    * @param resourceExpr a regular expression that denotes a set of
    * resources (based on <code>package.class</code>) 
    */
   void addCreatable(String profile,String resourceExpr);

   /**
    * Create an administrator user. 
    *
    * <p>A user class must have been defined with
    * <code>setUserClass()</code>. The administrator user will be
    * created only if no user with the given login already exist. If
    * created, the administrator user will be given the
    * "administrator" profile.</p>
    *
    * @param login the login name of the administrator
    * @param password the password of the administrator
    * 
    * @see #setUserClass(ClassItem,String,String,String) */
   void defineAdministrator(String login,String password);

   /**
    * Defines a contextual habilitation test (this is a generic method to
    * be used when the habilitation does not fit any simple scheme).
    *
    * @param condition the contextual condition (a static method
    * that takes the substance, the currently tested item, the
    * action's type and that returns true if the habilitation is
    * granted) 
    * @see #defineHabilitation(ClassItem,MethodItem)
    */
   void defineHabilitation(MethodItem condition);

   /**
    * Defines a contextual habilitation test for instances of given
    * class (This is a generic method to be used when the habilitation
    * does not fit any simple scheme).
    *
    * @param cli the class the test applies to
    * @param condition the contextual condition (a static method that
    * takes the substance, the currently tested item, the action's
    * type (one of GuiAC.VISIBLE, GuiAC.EDITABLE, GuiAC.ADDABLE
    * or GuiAC.REMOVABLE) and that returns true if the habilitation is
    * granted)
    * @see #defineHabilitation(MethodItem) 
    */
   void defineHabilitation(ClassItem cli, MethodItem condition);

   /**
    * For the specified collection, users will only see the objects
    * that they own.
    *
    * @param profile apply the filter only if the user has this profile
    * @param cl the class holding the collection
    * @param collectionName name of the collection attribute
    */
   void addOwnerFilter(String profile,ClassItem cl,
                       String collectionName);

   /**
    * For the specified collection, apply a filter on its getter, so
    * that some items can be hidden depending on the user.
    *
    * @param collection the collection to filter 
    * @param filter a static method which takes a Collection (the one
    * to filter), an Object (the holder of the collection), a
    * CollectionItem, and a User and returns the filtered collection.
    */
   void addFilter(CollectionItem collection, MethodItem filter);

   /**
    * Set a contextual profile to a reference or a collection that
    * contains user(s).
    *
    * <p>If one user is added to the given field at runtime, the user
    * will then have the given profile for the current object.</p>
    *
    * @param cl the class that owns the field
    * @param field the profiled field 
    * @param profile the profile to be set contextually */

   void setContextualProfile(ClassItem cl, String field, 
                             String profile);


}
