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

import org.objectweb.jac.core.*;

public class UserDefinedAC extends AspectComponent {

   protected UserDefinedWrapper wrapper = null;

   public void whenUsingNewInstance(Interaction interaction) {

      System.out.println( "When using new instance -- " + 
                          interaction.wrappee.getClass() );

      /*wrappee().wrap( new Wrapper() {
            public Object myWrappingMethod() {
               System.out.println("--- BEFORE: "+this.method());
               Object ret = proceed();
               System.out.println("--- AFTER : "+this.method()+
                                  " args were:"+java.util.Arrays.asList(this.args()));
               return ret;
            }
         }, "myWrappingMethod", "add" );
      */
      /*      if ( wrapper == null )
         wrapper = new UserDefinedWrapper();

      if(! ( wrappee() instanceof Calcul) ) return;

      System.out.println ( "<<<< User-defined AC: " + wrappee() + 
                           " is extended now >>>>" );	
      //wrappee().wrapAll( wrapper, "confirm" );
      wrappee().wrap( wrapper, "myWrappingMethod", "add" ); */
    }

   public void whenNewInstance( Wrappee newInstance ) {
      System.out.println( "When new instance -- " + newInstance.getClass() );
   }   
}







