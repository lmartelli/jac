/*
  Copyright (C) 2001 Renaud Pawlak

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

package org.objectweb.jac.aspects.tracing;

import org.objectweb.jac.core.*;

/**
 * Implements a simple debugging aspect for JAC applications. The
 * actual functionalities of the debugging are externalized within the
 * <code>Debugger</code> class.
 *
 * <p>Here is a sample configuration file that steps all the methods
 * that modify the instances of class <code>A</code> and
 * <code>B</code>, excepted the setter for the field called f.
 *
 * <pre class=code>
 * step ".*" "A || B" "MODIFIERS && !SETTER(f)"
 * </pre>
 *
 * @see DebuggingWrapper
 * @see Debugger */

public class DebuggingAC extends AspectComponent {

   /** 
    * This configuration method allows the programmer to define the
    * set of objects, classes, and methods that must be stepped when a
    * method is invoked.
    *
    * @param objects a pointcut expression on the name of the debugged
    * objects
    * @param classes a pointcut expression on the name of the debugged
    * classes
    * @param methods a pointcut expression on the name of the debugged
    * methods */

   public void step(String objects, String classes, String methods) { 
      pointcut( objects, classes+" && !org.objectweb.jac.aspects.tracing.Debugger", methods,
                DebuggingWrapper.class.getName(), "step", null, false );
   }

   /**
    * This configuration method must be used if the programmer wants
    * to step ALL the methods of all the applications objects.  */

   public void stepAll() { 
      pointcut( ".*", 
                ".* && !org.objectweb.jac.aspects.tracing.Debugger", 
                ".*",
                DebuggingWrapper.class.getName(), "step", null, false );
   }

}
