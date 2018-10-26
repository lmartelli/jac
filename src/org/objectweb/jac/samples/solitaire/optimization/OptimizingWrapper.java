package org.objectweb.jac.samples.solitaire.optimization;

import org.objectweb.jac.core.*;
import org.objectweb.jac.samples.solitaire.*;
import org.objectweb.jac.lib.java.util.Vector;
import java.util.*;

public class OptimizingWrapper extends Wrapper {

   public boolean optimized = false;

   public boolean isOptimized() {
      System.out.println( "-GO-" );
      return optimized;
   }
   
   public void setOptimized( boolean b ) {
      System.out.println( "-SO-" );
      optimized = b;
   }

   public Object setCaller() {
      attrdef( "caller", wrappee() );
      return proceed();
   }

   public Object cleanEquivalentConfigs() {

      System.out.println( " cleaning equivalent configs... " );
      if (arg(0) instanceof Configuration) {
       
         if (isThere((Vector) wrappee(),(Configuration)arg(0))) {
            return new Boolean(false);
         }
         else return proceed();
         
      }
      Date d1 = new Date();
      if (arg(0) instanceof Vector) {
         Object[] tab = ((Vector)arg(0)).toArray();
         for (int i=0; i<tab.length; i++) {
            if ( ! isThere((Vector) wrappee(),(Configuration)tab[i])) {
               ((Vector) wrappee()).add( tab[i] );
            }
         }
         ((Vector)arg(0)).clear();
      }
      Date d2 = new Date();
      // System.out.println("temps mis :" + (d2.getTime() - d1.getTime()));
      return proceed();
   }
   
               
   
   public boolean isThere(Vector configs, Configuration config) { 
      byte[][] element = config.game;    
      
      
      byte[][] copy1 = new byte[element.length][];
      for(int k = 0 ; k < element.length ; k++){
         copy1[10 - k] = (byte[]) element[k].clone();}
      
      byte[][] copy2 = new byte[element.length][element.length];
      for(int k = 0 ; k < element.length ; k++)
         for(int l = 0 ; l < element.length ; l++){
            copy2[k][l] = element[l][k];}
      
      byte[][] copy3 = new byte[element.length][];
      for(int k = 0 ; k < element.length ; k++){
         copy3[10 - k] = (byte[]) copy2[k].clone();}
      
      byte[][] copy4 = new byte[element.length][element.length];
      for(int k = 0 ; k < element.length ; k++)
         for(int l = 0 ; l < element.length ; l++){
            copy4[k][l] = copy1[l][k];}
      
      byte[][] copy5 = new byte[element.length][];
      for(int k = 0 ; k < element.length ; k++){
         copy5[10 - k] = (byte[]) copy4[k].clone();}
      
      byte[][] copy6 = new byte[element.length][element.length];
      for(int k = 0 ; k < element.length ; k++)
         for(int l = 0 ; l < element.length ; l++){
            copy6[k][l] = copy5[l][k];}
      
      byte[][] copy7 = new byte[element.length][];
      for(int k = 0 ; k < element.length ; k++){
         copy7[10 - k] = (byte[]) copy6[k].clone();}
      
      /*	System.out.println( toString(element) + "@@ " + toString(copy1) + "@@ " + toString(copy2) + "@@ " + toString(copy3) + "@@ " + toString(copy4) + "@@ " + toString(copy5) + "@@ " + toString(copy6) + "@@ " + toString(copy7));  */
      if (containsGame(element) || 
          containsGame(copy1) || 
          containsGame(copy2) || 
          containsGame(copy3) || 
          containsGame(copy4) || 
          containsGame(copy5) || 
          containsGame(copy6) || 
          containsGame(copy7) ) {
         
         return true;         
      }
      return false;
   }
   
   

   public boolean containsGame(byte[][] toTest) {
      Object[] configs = ((Vector)wrappee()).toArray();
      
      for ( int i = 0; i < configs.length; i++ ) {
         if ( confHasGame( (Configuration) configs[i], toTest ) ) return true;
      }
      return false;
   }
   
   public boolean confHasGame( Configuration c, byte[][] game ) {
      
      if ( c == null ) return false;
      if ( c.game == null || game == null ) return false;
      
      for(int k = 0 ; k < 11; k++) {
         for(int l = 0 ; l < 11; l++) {
            if( c.game[k][l] != game[k][l] )
               return false;
         }
      }
      return true;
   }
   
}



