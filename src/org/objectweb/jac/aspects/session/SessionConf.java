/*
  Copyright (C) 2003 Renaud Pawlak

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

package org.objectweb.jac.aspects.session;

/**
 * This is the configuration interface of the session aspect.
 *
 * @see SessionAC
 *
 * @author Renaud Pawlak */

public interface SessionConf {

   /**
    * This configuration method tells which methods of which objects
    * must handdle the sessions (wrapped by the session wrapper).
    *
    * @param classExpr a class pointcut expression
    * @param methodExpr a method pointcut expression
    * @param objectExpr an object pointcut expression
    *
    * @see org.objectweb.jac.aspects.session.SessionWrapper */

   void defineSessionHandlers(String classExpr, String methodExpr, String objectExpr);

   /**
    * This configuration method defines per-session objects.
    *
    * <p>A per-session object is an object that have a different state
    * depending on the session. Each user will see a different state
    * for the same object.
    *
    * <p>Warning: this feature is not compatible with persistent
    * objects!
    *
    * @param classExpr a class pointcut expression
    * @param objectExpr an object pointcut expression
    *
    * @see org.objectweb.jac.aspects.session.SessionWrapper */

   void definePerSessionObjects(String classExpr, String objectExpr);

   /**
    * Add some attributes to the list of attributes of the context to
    * be saved and restored by the session aspect.
    * @param attributes names of attributes 
    */
   void declareStoredAttributes(String attributes[]);

}
