/*
  Copyright (C) AOPSYS (http://www.aopsys.com)

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
*/

package org.objectweb.jac.samples.calcul;

import org.aopalliance.intercept.ConstructorInvocation;
import org.aopalliance.intercept.MethodInvocation;
import org.objectweb.jac.core.*;

public class UserDefinedWrapper extends Wrapper  {

   public UserDefinedWrapper(AspectComponent ac) {
      super(ac);
   }

   /*
     public Object confirm()
        throws ActionCanceledException
     {

        if( ! (wrappee() instanceof Calcul) ) return proceed();

        Display display = (Display)attr("Gui.display");
        if (display==null) return proceed();
      
        if( method().equals("add") ) {
  
           if ( ! display.showConfirm( "Are you sure to add " + arg(0) + 
                                       " to " + wrappee() + " ?" ) ) {
              throw new ActionCanceledException();
           }
        }
        return proceed();
     }
   */

   public Object myWrappingMethod(Interaction interaction) {
      Object ret=null;
      System.out.println("Before "+interaction.method+":"+interaction.args[0]);
      ret=proceed(interaction);
      System.out.println("After "+interaction.method);
      return ret;
   }

/* (non-Javadoc)
 * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
 */
public Object invoke(MethodInvocation invocation) throws Throwable {
	// TODO Auto-generated method stub
	return null;
}

/* (non-Javadoc)
 * @see org.aopalliance.intercept.ConstructorInterceptor#construct(org.aopalliance.intercept.ConstructorInvocation)
 */
public Object construct(ConstructorInvocation invocation) throws Throwable {
	// TODO Auto-generated method stub
	return null;
}

}

class ActionCanceledException extends Exception {}








