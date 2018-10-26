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

  You should have received a copy of the GNU Lesser General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA */

package org.objectweb.jac.aspects.tracing;

import java.util.*;
import org.aopalliance.intercept.ConstructorInvocation;
import org.aopalliance.intercept.MethodInvocation;
import org.objectweb.jac.core.*;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.ClassRepository;

/**
 * This counter must wrap the methods of which calls have to be
 * counted. It extends the simple counting wrapper to provide 2
 * optimization methods when client methods call several times the
 * method wrapped by <code>incr</code>. In these cases, the counter is
 * direcly incremented by the number of times the <code>incr</code>
 * method has to be called.
 *
 * <p>In order to avoid redundancy, <code>incr</code> must not be
 * called if <code>incrWithArg</code> or <code>incrWithField</code>
 * have already been called. To perform this contextual test, use the
 * before and after running wrapper methods of the aspect
 * component. */

public class OptimizedCountingWrapper extends SimpleCountingWrapper {

   /** The field on which the optimization can be done. */
   String field = null;

   /** The argument on which the optimization can be done. */
   int arg = 0;
   
   /**
    * Create the counter and parametrize it regarding the base program
    * shape.
    *
    * @param c the used counter
    * @param field the field that is used to optimize the counting */

   public OptimizedCountingWrapper(AspectComponent ac, Counter c, String field) {
      super(ac,c);
      this.field = field;
   }

   /**
    * Create the counter and parametrize it regarding the base program
    * shape.
    *
    * @param c the used counter 
    * @param arg the argument number that used to optimize the
    * counting */

   public OptimizedCountingWrapper(AspectComponent ac, Counter c, int arg) {
      super(ac,c);
      this.arg = arg;
   }

   /**
    * This wrapping method increments the counter with the field when
    * the wrapped method is called. It is an optimization for the incr
    * method.
    *
    * @return the return value of the wrapped method
    * @see SimpleCountingWrapper#incr(Interaction) */

   public Object incrWithField(Interaction interaction) {
      attrdef( "tracing.globalIncr", "" );
      Object ret = proceed(interaction);
      ClassItem cl=ClassRepository.get().getClass(interaction.wrappee);
      Object fval = cl.getField(field).get(interaction.wrappee);
      if (fval == null) {
         System.out.println( "<<< Counting aspect says: the field to count (" + 
                             field + ") is null or does not exist... >>>" );
         return ret;
      }
      if (fval.getClass().isArray()) {
         counter.incr(((Object[])fval).length);
      } else if ( fval instanceof Collection ) {
         counter.incr(((Collection)fval).size());
      } else {
         /** this type is not supported... */
         System.out.println( "<<< Counting aspect says: the field to count (" + 
                             field + ") is not of a supported type... >>>" );
         return ret;
      }
      printCounter();
      return ret;
   }

   /**
    * This wrapping method increments the counter with the argument
    * value when the wrapped method is called. It is an optimization
    * for the <code>incr</code> method.
    *
    * @return the return value of the wrapped method
    * @see SimpleCountingWrapper#incr(Interaction) */

   public Object incrWithArg(Interaction interaction) {
      attrdef( "tracing.globalIncr", "" );
      Object ret = proceed(interaction);
      counter.incr( ((Integer)interaction.args[arg]).intValue() );
      printCounter();
      return ret;
   }

    public Object invoke(MethodInvocation invocation) throws Throwable {
        if (field==null)
            return incrWithArg((Interaction)invocation);
        else
            return incrWithField((Interaction)invocation);
    }

    public Object construct(ConstructorInvocation invocation) throws Throwable {
        if (field==null)
            return incrWithArg((Interaction)invocation);
        else
            return incrWithField((Interaction)invocation);
    }  

}
