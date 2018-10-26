/*
  Copyright (C) 2001-2002 Renaud Pawlak <renaud@aopsys.com>

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 2 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  USA */

package org.objectweb.jac.core;

import org.objectweb.jac.core.rtti.AbstractMethodItem;

/**
 * The classes that implement this interface are objects that can
 * participate to a collaboration.
 * 
 * <p>This includes the ability to get the current wrappee, method and
 * arguments of the call that is currently proceeded, and the ability
 * to define and retrieve attributes from the current collaboration
 * flow.
 *
 * <p>The classes that implement this interface use the
 * <code>Collaboration</code> class.
 * 
 * @see Collaboration
 *
 * @author Renaud Pawlak
 */

public interface CollaborationParticipant {

   /**
    * Add an attribute to the current collaboration.
    *
    * <p>A attribute is an attribute that is visible from all the
    * objects of the local JAC container. I can propagate to remote
    * containers when remote objects are called on this container if
    * it is defined global.
    *
    * @param name the name of the attribute.
    * @param value its value (must be serializable if the attribute is
    * global), null undefines the attribute
    * @see Collaboration#addAttribute(String,Object) */

   void attrdef( String name, Object value );

   /**
    * Get an attribute value for the current collaboration. This
    * attribute can be global or local.
    *
    * @param name the name of the collaboration attribute.
    * @return the value of the attribute
    * @see Collaboration#getAttribute(String) */
    
   Object attr( String name );

   /**
    * Returns the wrappee of the current call, that is to say the base
    * program object that have been called during the current
    * collaboration point (method call).
    * 
    * @return the currently called wrappee
    */

   Wrappee wrappee();
   
   /**
    * Returns the method name that have been called on the wrappee
    * during the current collaboration point (method call).
    *
    * @return the currently called method
    */

   AbstractMethodItem method();
   
   /**
    * Returns the args that have been passed to the method (see
    * <code>method()</code>).
    *
    * @return the array arguments of the currently called method
    */

   Object[] args();

   /**
    * Returns the nth argument of the current collaboration point
    * method (see <code>args()</code>).
    * 
    * @param nth the zero-indexed argument index
    * @return the nth argument of the currently called method
    */

   Object arg( int nth );

   /**
    * Sets the nth argument value.
    *
    * @param nth the zero-indexed argument index
    * @param value the new value
    */

   void setarg( int nth, Object value );

   /**
    * Sets the argument values.
    *
    * @param values the new values
    */

   void setargs( Object[] values );

}









