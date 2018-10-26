/*
  Copyright (C) 2002 Laurent Martelli <laurent@aopsys.com>

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

package org.objectweb.jac.aspects.i18n;

import org.aopalliance.intercept.ConstructorInvocation;
import org.aopalliance.intercept.MethodInvocation;
import org.objectweb.jac.core.AspectComponent;
import org.objectweb.jac.core.Interaction;
import org.objectweb.jac.core.Wrapper;
import org.objectweb.jac.util.Log;
import java.util.HashMap;

/**
 * Internationalisation aspect. It allows to translate parameters or
 * return values.
  */

public class I18nAC extends AspectComponent {
   TranslatorWrapper translator = new TranslatorWrapper(this);
   /**
    * Translate string parameters of methods.
    */
   public void translateParameters(String classExpr, String methodExpr) {
      Log.trace("i18n","translateParameters "+classExpr+"."+methodExpr);
      pointcut( "ALL", classExpr, methodExpr,
                translator, null );
   }

   /**
    * Translate the returned value.
    */
   public void translateReturnedValue(String classExpr, String methodExpr) {
      Log.trace("i18n","translateReturnedValue "+classExpr+"."+methodExpr);
      pointcut( "ALL", classExpr, methodExpr,
                translator, null );
   }

   HashMap dict = new HashMap();

   public void addTranslation(String key, String translation) {
      Log.trace("i18n","addTranslation "+key+"->"+translation);
      dict.put(key,translation);
   }

   public class TranslatorWrapper extends Wrapper {
      public TranslatorWrapper(AspectComponent ac) {
         super(ac);
      }

      public Object translateParameters(Interaction interaction) {
         Log.trace("i18n","translate parameters for "+interaction.method);
         Log.trace("i18n",3,"dict: "+dict);
         Object[] args = interaction.args;
         Class[] argTypes = interaction.method.getParameterTypes();
         for (int i=0; i<args.length; i++) {
            if (argTypes[i]==String.class) {
               Log.trace("i18n","Arg "+i+"="+args[i]);
               args[i] = translate(args[i]);
            }
         }
         return proceed(interaction);
      }

      public Object translateReturnedValue(Interaction interaction) {
         Log.trace("i18n","translate return value "+interaction.method);
         Object returnedValue = proceed(interaction);
         Log.trace("i18n",3,"dict: "+dict);
         Log.trace("i18n","Returnedvalue="+returnedValue);
         return translate(returnedValue);
      }

      public Object translate(Object value) {
         if (dict.containsKey(value)) {
            Log.trace("i18n",2,"translating "+value+"->"+dict.get(value));
            return dict.get(value);
         } else {
            return value;
         }
      }

	// TODO: implement translate return value

	public Object invoke(MethodInvocation invocation) throws Throwable {
		return translateParameters((Interaction)invocation);
	}

	public Object construct(ConstructorInvocation invocation) throws Throwable {
		return translateParameters((Interaction)invocation);
	}
   }
}   
