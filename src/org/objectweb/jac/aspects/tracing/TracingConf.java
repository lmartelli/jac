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
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA */

package org.objectweb.jac.aspects.tracing;

/**
 * This sample aspect component traces the calls on all the objects of
 * the program.
 *
 * <p>To be active this aspect component must be configured with the
 * <code>addTrace</code> method.
 * @see TracingAC
 * @see org.objectweb.jac.wrappers.VerboseWrapper */

public interface TracingConf {

   /**
    * This configuration method makes a method call to be traced by a
    * verbose wrapper.
    *
    * By default no method is verbose.
    *
    * @param wrappeeExpr a regular expression that matches the
    * wrappee(s) name(s) that contain the method(s) to be traced
    * @param wrappeeClassExpr a regular expression that matches the
    * wrappee(s) class(es) name(s) that contain the method(s) to be
    * traced
    * @param wrappeeMethodExpr a regular expression that matches the
    * method(s) to be traced (within the classes or objects denoted by
    * the previous parameters)
    * @see org.objectweb.jac.wrappers.VerboseWrapper */

   void addTrace(String wrappeeExpr, 
                 String wrappeeClassExpr, 
                 String wrappeeMethodExpr);

   /**
    * This configuration method makes a method call to be traced. The
    * names of parameters are printed if configured by the Gui aspect.
    *
    * @param wrappeeExpr a regular expression that matches the
    * wrappee(s) name(s) that contain the method(s) to be traced
    * @param wrappeeClassExpr a regular expression that matches the
    * wrappee(s) class(es) name(s) that contain the method(s) to be
    * traced
    * @param wrappeeMethodExpr a regular expression that matches the
    * method(s) to be traced (within the classes or objects denoted by
    * the previous parameters)
    * @see org.objectweb.jac.wrappers.VerboseWrapper */
   void addNamedTrace(String wrappeeExpr, 
                      String wrappeeClassExpr, 
                      String wrappeeMethodExpr);

   /**
    * This configuration method makes a method call to be traced by a
    * verbose wrapper. For each call, the stack is dumped.
    *
    * By default no method is verbose.
    *
    * @param wrappeeExpr a regular expression that matches the
    * wrappee(s) name(s) that contain the method(s) to be traced
    * @param wrappeeClassExpr a regular expression that matches the
    * wrappee(s) class(es) name(s) that contain the method(s) to be
    * traced
    * @param wrappeeMethodExpr a regular expression that matches the
    * method(s) to be traced (within the classes or objects denoted by
    * the previous parameters)
    * @see org.objectweb.jac.wrappers.VerboseWrapper */
   void addStackTrace(String wrappeeExpr, 
                      String wrappeeClassExpr, 
                      String wrappeeMethodExpr);



   /**
    * This configuration method makes a method call to be traced by a
    * verbose wrapper. For each call, the wrapping methods are printed.
    *
    * By default no method is verbose.
    *
    * @param wrappeeExpr a regular expression that matches the
    * wrappee(s) name(s) that contain the method(s) to be traced
    * @param wrappeeClassExpr a regular expression that matches the
    * wrappee(s) class(es) name(s) that contain the method(s) to be
    * traced
    * @param wrappeeMethodExpr a regular expression that matches the
    * method(s) to be traced (within the classes or objects denoted by
    * the previous parameters)
    * @see org.objectweb.jac.wrappers.VerboseWrapper */
   void addWrappersTrace(String wrappeeExpr, 
                         String wrappeeClassExpr, 
                         String wrappeeMethodExpr);

   /**
    * This configuration method creates a new recording on a set of
    * methods.
    *
    * @param wrappeeExpr a pointcut expression that matches the
    * wrappee(s) name(s) that contain the method(s) to be traced
    * @param wrappeeClassExpr a pointcut expression that matches the
    * wrappee(s) class(es) name(s) that contain the method(s) to be
    * traced
    * @param wrappeeMethodExpr a pointcut expression that matches the
    * method(s) to be traced (within the classes or objects denoted by
    * the previous parameters) */

   void addRecording(String wrappeeExpr, 
                     String wrappeeClassExpr, 
                     String wrappeeMethodExpr);

   /**
    * This configuration method makes all the matching methods
    * invocations to be counted.
    *
    * @param name the name of the counter that is used (can be
    * shared)
    * @param wrappeeExpr a regular expression that matches the
    * wrappee(s) name(s) that contain the method(s) to be counted
    * @param wrappeeClassExpr a regular expression that matches the
    * wrappee(s) class(es) name(s) that contain the method(s) to be
    * counted
    * @param wrappeeMethodExpr a regular expression that matches the
    * method(s) to be counted (within the classes or objects denoted
    * by the previous parameters)
    * @see SimpleCountingWrapper 
    */

   void addCounter(String name,
                   String wrappeeExpr,
                   String wrappeeClassExpr, 
                   String wrappeeMethodExpr);
   
   /**
    * This configuration method makes all the matching methods
    * invocations to be counted in an optimized fashion.
    *
    * @param name the name of the counter that is used (can be
    * shared)
    * @param wrappeeExpr a regular expression that matches the
    * wrappee(s) name(s) that contain the method(s) to be counted
    * @param wrappeeClassExpr a regular expression that matches the
    * wrappee(s) class(es) name(s) that contain the method(s) to be
    * counted
    * @param wrappeeMethodExpr a regular expression that matches the
    * method(s) to be counted (within the classes or objects denoted
    * by the previous parameters)
    * @param fieldName the name of the field that is used to optimize
    * the counting (the counter is incremented with its value), an
    * empty string has no effect
    * @param argNumber the argument's index that is used to optimize
    * the counting (the counter is incremented with its value), an
    * empty string has no effect 
    * @see OptimizedCountingWrapper */

   void addOptimizedCounter(String name,
                            String wrappeeExpr, 
                            String wrappeeClassExpr, 
                            String wrappeeMethodExpr,
                            String fieldName,
                            String argNumber);

}






