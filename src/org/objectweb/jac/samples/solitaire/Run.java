package org.objectweb.jac.samples.solitaire;

import org.objectweb.jac.lib.java.util.Vector;
import java.util.*;
import org.objectweb.jac.core.*;
import org.objectweb.jac.aspects.naming.*;

public class Run{
    public static void main(String[] args) {

        ((ACManager)ACManager.get()).declareAC(
            "optimization",
            "org.objectweb.jac.samples.solitaire.optimization.OptimizingAC");

        Application app = new Application("solitaire",null,
                                          "Run",null);

        app.addAcConfiguration(
            new ACConfiguration( app, "deployment", 
                                 "file:deployment.acc", true ) );

        app.addAcConfiguration(
            new ACConfiguration( app, "optimization", 
                                 null, false ) );

        app.addAcConfiguration(
            new ACConfiguration( app, "tracing", 
                                 "file:tracing.acc", true ) );

        ApplicationRepository.get().addApplication( app );


        byte[][] game = new byte[][] {
            new byte[] {2,2,2,2,2,2,2,2,2,2,2} ,
            new byte[] {2,2,2,2,2,2,2,2,2,2,2} ,
            new byte[] {2,2,2,2,0,0,0,2,2,2,2} , 
            new byte[] {2,2,2,2,0,0,0,2,2,2,2} ,
            new byte[] {2,2,0,0,0,1,0,0,0,2,2} ,
            new byte[] {2,2,0,0,1,1,1,0,0,2,2} ,
            new byte[] {2,2,0,0,1,1,1,0,0,2,2} ,
            new byte[] {2,2,2,2,0,0,0,2,2,2,2} , 
            new byte[] {2,2,2,2,0,0,0,2,2,2,2} ,
            new byte[] {2,2,2,2,2,2,2,2,2,2,2} ,
            new byte[] {2,2,2,2,2,2,2,2,2,2,2} 
        } ;
        Vector v = new Vector();
        Configuration root= new Configuration(32,game,null);
        Date d3 = new Date();
        root.createChildren();
        Date d4 = new Date();
        System.out.println("Temps total mis : " + (d4.getTime()-d3.getTime()));

        root.printTree();

        System.out.println("-------- END ---------");
        //root.printTree();

      //      if ( true ) return;
      
      /*      new Vector().add( new Vector() );

      java.util.Vector v = new java.util.Vector();
      
      Date d3 = new Date();
      for ( int i=0; i<1000; i ++ ) {
         v.add( new Vector() );
      }
      Date d4 = new Date();
      System.out.println("Temps total mis : " + (d4.getTime()-d3.getTime()));*/
      /*
      Vector current = new Vector();
      System.out.println( "********************************" );
      current.add( new Configuration(32,game,null) );
      System.out.println( "********************************" );
      Vector next = new Vector();
      int x = 0;
      boolean finish = true;
      do {
         x++;
         Date d1 = new Date();
         //System.out.println( "******************************** Next step for :" );
          // System.out.println( current );
         for ( int i=0; i < current.size(); i++ ) {
            Configuration curC = (Configuration)current.get(i);
            System.out.println("creating childrens for ");
            curC.printGame();
            next.addAll ( curC.createChildren() );
            //System.out.println( "****************** taille : " + next.size() );
         }
         //System.out.println( next.toString() );
         Date d2 = new Date();
         System.out.println( next.size() );
         System.out.println("Temps total mis : " + (d2.getTime()-d1.getTime()));
         finish = true;
         if (next.size() > 0) {
            current = next;
            finish = false;
         }
         next = new Vector();
      } while ( finish == false );
      // System.out.println( current );
            
      Configuration affichage = new Configuration(0, game , null);*/
      
      /*      for (int b=0; b < current.size(); b++) {	
         affichage = (Configuration)current.get(b);
         System.out.println("--------------------------------------------------" );
         for ( int a=0; a < x; a++ )		{			
            System.out.println( affichage );
            affichage = affichage.parent;
         }
         }*/
    }
   
}


