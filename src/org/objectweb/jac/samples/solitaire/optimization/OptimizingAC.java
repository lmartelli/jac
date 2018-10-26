package org.objectweb.jac.samples.solitaire.optimization;

import org.objectweb.jac.core.*;
import org.objectweb.jac.samples.solitaire.*;
import org.objectweb.jac.lib.java.util.Vector;

public class OptimizingAC extends AspectComponent {

   //   static public int count = 0;

   public void whenUsingNewInstance() {

      OptimizingWrapper optw = null;
      
      if ( wrappee() instanceof Vector ) {
         //System.out.println( "Wrapping " + wrappee() );
         wrappee().wrap( new OptimizingWrapper(), "cleanEquivalentConfigs", "add" );
         //wrappee().wrap( new OptimizingWrapper(), "cleanEquivalentConfigs", "addAll" );
      }
      if ( wrappee() instanceof Configuration ) {
      }

   }
   
}
