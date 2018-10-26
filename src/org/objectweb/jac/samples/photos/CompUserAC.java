/*
  Copyright (C) 2003 Renaud Pawlak

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  USA */

package org.objectweb.jac.samples.photos;

import org.objectweb.jac.util.Log;
import org.objectweb.jac.core.CompositeAspectComponent;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.core.rtti.MethodItem;
import org.objectweb.jac.aspects.authentication.AuthenticationAC;
import org.objectweb.jac.aspects.user.UserAC;

/**
 * This aspect is an example on how to use the composite aspects of JAC.
 *
 * <p>A composite aspect is a kind of facade that factorizes and
 * simplifies the use of a set of sub-aspects (called chidren
 * aspects).
 *
 * <p>Here, we give the example of the composition of two core aspects
 * of the JAC framework: user and authentication. Note that a
 * composite aspect must extend the class CompositeAspectComponent
 * that itself extends the AspectComponent class. 
 */
public class CompUserAC extends CompositeAspectComponent {

    /**
     * In the constructor, the composite aspect will declare its
     * children. */
    public CompUserAC() {
        super();
        // keywords must be declared again even if declared in the
        // children
        blockKeywords = new String[] {"profile"};
        // we compose two aspects (user and authentication)
        // note that order is important
        addChild("user",UserAC.class);
        addChild("authentication",AuthenticationAC.class);
    }

    boolean authenticator=false;

    void setAuthenticator() {
        if(!authenticator) {
            // (declare a default controller which works in 99% of the
            // cases)       
            ((AuthenticationAC)getChild("authentication"))
                .setDisplayController(
                    ClassRepository.get()
                    .getClass("org.objectweb.jac.aspects.user.UserAC")
                    .getMethod("userController"));
            authenticator=true;
        }
        
    }

    /**
     * This methods groups three dependent configuration methods of
     * the child aspects. It simplifies the use of the aspects by
     * allowing only one kind of authentication (the user-class based
     * one), which is on of the more often used. */
    public void setUserClass(ClassItem userClass, 
                             String loginField,
                             String passwordField,
                             String profileField) {
        Log.trace("compuser",
                  "setUserClass("+userClass+","+loginField+","+
                  passwordField+","+profileField+")");
        setAuthenticator();
        // delegate the user class
        ((UserAC)getChild("user")).setUserClass(userClass,
                                                loginField,
                                                passwordField,
                                                profileField);
       
        // automatically makes the link with the user aspect (declare a
        // default authenticator which works in 99% of the cases)
        ((AuthenticationAC)getChild("authentication"))
            .setAuthenticator
            (ClassRepository.get()
             .getClass("org.objectweb.jac.aspects.authentication.UserPasswordAuthenticator"),
             new String[] { getChildActualName("user") });

    }
    
    /** UserAC delegation. */
    public void autoInitClasses(String classExpr) {
        Log.trace("compuser",
                  "autoInitClass("+classExpr+")");
        ((UserAC)getChild("user")).autoInitClasses(classExpr);
    }

    /** UserAC delegation. */
    public void autoInitClasses(ClassItem cl, 
                                String triggerClassExpr,
                                String triggerMethodExpr) {
        Log.trace("compuser",
                  "autoInitClass("+cl+","+triggerClassExpr+","+triggerMethodExpr+")");
        ((UserAC)getChild("user")).autoInitClasses
            (cl,triggerClassExpr,triggerMethodExpr);
    }
    
    /** UserAC delegation. */
    public void declareProfile(String name) {
        Log.trace("compuser",
                  "declareProfile("+name+")");
        ((UserAC)getChild("user")).declareProfile(name);
    }

    /** UserAC delegation. */
    public void declareProfile(String name,String parent) {
        Log.trace("compuser",
                  "declareProfile("+name+","+parent+")");
        ((UserAC)getChild("user")).declareProfile(name,parent);
    }

    /** UserAC delegation. */
    public void addReadable(String profile,String resourceExpr) {
        Log.trace("compuser",
                  "addReadable("+profile+","+resourceExpr+")");
        ((UserAC)getChild("user")).addReadable(profile,resourceExpr);
    }

    /** UserAC delegation. */
    public void addUnreadable(String profile,String resourceExpr) {
        Log.trace("compuser",
                  "addUnreadable("+profile+","+resourceExpr+")");
        ((UserAC)getChild("user")).addUnreadable(profile,resourceExpr);
    }

    /** UserAC delegation. */
    public void addWritable(String profile,String resourceExpr) {
        Log.trace("compuser",
                  "addWritable("+profile+","+resourceExpr+")");
        ((UserAC)getChild("user")).addWritable(profile,resourceExpr);
    }

    /** UserAC delegation. */
    public void addUnwritable(String profile,String resourceExpr) {
        Log.trace("compuser",
                  "addUnwritable("+profile+","+resourceExpr+")");
        ((UserAC)getChild("user")).addUnwritable(profile,resourceExpr);
    }

    /** UserAC delegation. */
    public void addRemovable(String profile,String resourceExpr) {
        Log.trace("compuser",
                  "addRemovable("+profile+","+resourceExpr+")");
        ((UserAC)getChild("user")).addRemovable(profile,resourceExpr);
    }

    /** UserAC delegation. */
    public void addUnremovable(String profile,String resourceExpr) {
        Log.trace("compuser",
                  "addUnremovable("+profile+","+resourceExpr+")");
        ((UserAC)getChild("user")).addUnremovable(profile,resourceExpr);
    }

    /** UserAC delegation. */
    public void addAddable(String profile,String resourceExpr) {
        Log.trace("compuser",
                  "addAddable("+profile+","+resourceExpr+")");
        ((UserAC)getChild("user")).addAddable(profile,resourceExpr);
    }

    /** UserAC delegation. */
    public void addCreatable(String profile,String resourceExpr) {
        Log.trace("compuser",
                  "addCreatable("+profile+","+resourceExpr+")");
        ((UserAC)getChild("user")).addCreatable(profile,resourceExpr);
    }

    /** UserAC delegation. */
    public void addUnaddable(String profile,String resourceExpr) {
        Log.trace("compuser",
                  "addUnaddable("+profile+","+resourceExpr+")");
        ((UserAC)getChild("user")).addUnaddable(profile,resourceExpr);
    }

    /** UserAC delegation. */
    public void defineAdministrator(String login,String password) {
        Log.trace("compuser",
                  "defineAdministrator("+login+","+password+")");
        ((UserAC)getChild("user")).defineAdministrator(login,password);
    }

    /** AuthenticationAC delegation. */
    public void setDisplayController(MethodItem controller) {
        setAuthenticator();
        Log.trace("compuser",
                  "setDisplayController("+controller+")");
        ((AuthenticationAC)getChild("authentication"))
            .setDisplayController(controller);
    }

    /** AuthenticationAC delegation. */
    public void setAccessDeniedMessage(String message) {
        setAuthenticator();
        Log.trace("compuser",
                  "setAccessDeniedMessage("+message+")");
        ((AuthenticationAC)getChild("authentication"))
            .setAccessDeniedMessage(message);
    }

   
    //public String[] getDefaultConfigs() {
    //  return new String[] {"org/objectweb/jac/aspects/user/user.acc"};
    //}
    
}
