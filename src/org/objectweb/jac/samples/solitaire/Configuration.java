package org.objectweb.jac.samples.solitaire;

import org.objectweb.jac.lib.java.util.Vector;
//import java.util.Vector;
import org.objectweb.jac.aspects.naming.*;

public class Configuration {
   public int nBalls ;
   public byte[][] game ;
   public Configuration parent ;
   public Vector children;
   static java.util.Hashtable instancesCounts=new java.util.Hashtable();
   public static int getInstanceCount(Integer level) {
      Integer count = (Integer)instancesCounts.get(level);
      if( count == null ) {
         instancesCounts.put(level,new Integer(0));
         return 0;
      }     
      return count.intValue();
   }
   public static void incr(Integer level) {
      Integer count = (Integer)instancesCounts.get(level);
      if( count == null ) {
         instancesCounts.put(level,new Integer(1));
         return;
      }
      instancesCounts.put(level,new Integer(count.intValue()+1));
   }

   public Configuration(int nBalls, byte[][] game, Configuration parent){
      this.nBalls = nBalls;
      this.game = game;
      this.parent = parent;
      //incr(new Integer(nBalls));
   }
   public void printTree() {
      java.util.Iterator it;
      it = children.iterator();
      System.out.println("---- begin");
      while(it.hasNext()) {
         Configuration conf = (Configuration)it.next();
         System.out.println(conf);
      }
      System.out.println("---- end");
      it = children.iterator();
      while(it.hasNext()) {
         Configuration conf = (Configuration)it.next();
         conf.printTree();
      }
   }

   public void printGame() {
      System.out.println( this.toString() );
   }

   public String toString() {
      return toString(game);
   }
   public String toString(byte[][] game) {
      
      if ( game == null ) return "";
      String result = "\n";
      for(int i = 2 ; i<9 ;i++) {
         for(int j = 2 ; j<9 ; j++) {
            if (game[i][j] == 1) 
               result = result + "O";
            if (game[i][j] == 0)
               result = result + ".";
            if (game[i][j] == 2)
               result = result + " ";
         }
         result = result + "\n";
      }
      return result;
   } 

   public void createChildren () {
      children = new Vector();
      Configuration newConf=null;
      for(int i = 2 ; i<9 ;i++) {
         for(int j = 2 ; j<9 ; j++) {
            if (game[i][j] == 0) {
               //  System.out.println("vide pour " + i + ":" + j);
               //  System.out.println( this );
               if (game[i-2][j] == 1 && game[i-1][j] == 1) {
                  byte[][] copy = new byte[game.length][];
                  for(int k = 0 ; k < game.length ; k++)
                     copy[k] = (byte[]) game[k].clone();
						
                  copy[i][j] = 1;
                  copy[i-2][j] = 0;
                  copy[i-1][j] = 0;
                  //NamingAC.forceName("configuration_"+(nBalls-1)+"_"+
                  //                   getInstanceCount(new Integer(nBalls-1)));
                  newConf=new Configuration(nBalls - 1, copy, this);
                  children.add( newConf );
                  newConf.createChildren();
               }
               if (game[i+2][j] == 1 && game[i+1][j] == 1) {
                  byte[][] copy = new byte[game.length][];
                  for(int k = 0 ; k < game.length ; k++)
                     copy[k] = (byte[]) game[k].clone();
                  copy[i][j] = 1;
                  copy[i+2][j] = 0;
                  copy[i+1][j] = 0;
                  //NamingAC.forceName("configuration_"+(nBalls-1)+"_"+
                  //                   getInstanceCount(new Integer(nBalls-1)));
                  newConf=new Configuration(nBalls - 1, copy, this);
                  children.add( newConf );
                  newConf.createChildren();
               }

               if (game[i][j-2] == 1 && game[i][j-1] == 1) {
                  byte[][] copy = new byte[game.length][];
                  for(int k = 0 ; k < game.length ; k++)
                     copy[k] = (byte[]) game[k].clone();
                  copy[i][j] = 1;
                  copy[i][j-2] = 0;
                  copy[i][j-1] = 0;
                  //NamingAC.forceName("configuration_"+(nBalls-1)+"_"+
                  //                   getInstanceCount(new Integer(nBalls-1)));
                  newConf=new Configuration(nBalls - 1, copy, this);
                  children.add( newConf );
                  newConf.createChildren();
               }
               if (game[i][j+2] == 1 && game[i][j+1] == 1) {
                  byte[][] copy = new byte[game.length][];
                  for(int k = 0 ; k < game.length ; k++)
                     copy[k] = (byte[]) game[k].clone();
                  copy[i][j] = 1;
                  copy[i][j+2] = 0;
                  copy[i][j+1] = 0;
                  //NamingAC.forceName("configuration_"+(nBalls-1)+"_"+
                  //                   getInstanceCount(new Integer(nBalls-1)));
                  newConf=new Configuration(nBalls - 1, copy, this);
                  children.add( newConf );
                  newConf.createChildren();
               }
            }
         }
      }
      // System.out.println( result);
      //      return result;
   }

}



