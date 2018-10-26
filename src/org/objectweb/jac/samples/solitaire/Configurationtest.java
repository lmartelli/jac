
package org.objectweb.jac.samples.solitaire;

import java.util.*;

public class Configuration {
   public int nBalls ;
   public byte[][] game ;
   public Configuration parent ;

   public Configuration(int nBalls, byte[][] game, Configuration parent){
      this.nBalls = nBalls;
      this.game = game;
      this.parent = parent;
   }
   public String toString() {
      return toString(game);
   }
   public String toString(byte[][] game) {
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

     public boolean equals(Object entree) {
      boolean result;
      result = true;

      //return super.equals( entree );

      if ( entree == null ) return false;
      if ( ! (entree instanceof Configuration) ) return false;
      if ( ((Configuration)entree).game == null ) return false;

      System.out.println("dans equals!!" + toString(game) + ((Configuration)entree).game );
      System.out.println("dans equals!!" + toString(((Configuration)entree).game) + toString(game) + result);

      for(int k = 0 ; k < 11 && result == true; k++){
         for(int l = 0 ; l < 11 && result == true; l++){
            if(((Configuration)entree).game[k][l] != this.game[k][l] )
               result = false;}}
      return result;
   }
	 
   public Vector createChildren () {
      System.out.println( "fejffkfjeklj" );
   
      Vector result = new Vector();
      for(int i = 2 ; i<9 ;i++) {
         for(int j = 2 ; j<9 ; j++) {
            System.out.print( "ij=" + i + "," + j + ":" + game[i][j] + " ");
            if (game[i][j] == 0) {
               /*	System.out.println("vide pour " + i + ":" + j);
                  System.out.println( this );  */

               System.out.print( " (CAS 1 ) ");
               System.out.print( game[i-2][j] + ":" + game[i-1][j]);
               if (game[i-2][j] == 1 && game[i-1][j] == 1) {

                                    byte[][] element = new byte[game.length][];
                  for(int k = 0 ; k < game.length ; k++)
                     element[k] = (byte[]) game[k].clone();

						
                  element[i][j] = 1;
                  element[i-2][j] = 0;
                  element[i-1][j] = 0;

                  /*byte[][] copy1 = new byte[element.length][];
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
                  copy7[10 - k] = (byte[]) copy6[k].clone();}*/

                  /*	System.out.println( toString(element) + "@@ " + toString(copy1) + "@@ " + toString(copy2) + "@@ " + toString(copy3) + "@@ " + toString(copy4) + "@@ " + toString(copy5) + "@@ " + toString(copy6) + "@@ " + toString(copy7));  */
                  //   if (!result.contains(new Configuration(nBalls - 1, element, this)) && !result.contains(new Configuration(nBalls - 1, copy1, this)) && !result.contains(new Configuration(nBalls - 1, copy2, this)) && !result.contains(new Configuration(nBalls - 1, copy3, this)) && !result.contains(new Configuration(nBalls - 1, copy4, this)) && !result.contains(new Configuration(nBalls - 1, copy5, this)) && !result.contains(new Configuration(nBalls - 1, copy6, this)) && !result.contains(new Configuration(nBalls - 1, copy7, this))) 
                  result.add(new Configuration(nBalls - 1, element, this));
               }

               System.out.print( " (CAS 2 ) ");
               System.out.print( game[i+2][j] + ":" + game[i+1][j] );               
               if (game[i+2][j] == 1 && game[i+1][j] == 1) {


                  byte[][] element = new byte[game.length][];
                  for(int k = 0 ; k < game.length ; k++)
                     element[k] = (byte[]) game[k].clone();

                  element[i][j] = 1;
                  element[i+2][j] = 0;
                  element[i+1][j] = 0;

                  /*    byte[][] copy1 = new byte[element.length][];
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
                  copy7[10 - k] = (byte[]) copy6[k].clone();} */

                  /*	System.out.println( element + "@" + copy1 + "@" + copy2 + "@" + copy3 + "@" + copy4 + "@" + copy5 + "@" + copy6 + "@" + copy7 + "@" + copy4[4]); */

                  /*	System.out.println(toString(element) + "@@ " + toString(copy1) + "@@ " + toString(copy2) + "@@ " + toString(copy3) + "@@ " + toString(copy4) + "@@ " + toString(copy5) + "@@ " + toString(copy6) + "@@ " + toString(copy7)); */
                  //      if (!result.contains(new Configuration(nBalls - 1, element, this)) && !result.contains(new Configuration(nBalls - 1, copy1, this)) && !result.contains(new Configuration(nBalls - 1, copy2, this)) && !result.contains(new Configuration(nBalls - 1, copy3, this)) && !result.contains(new Configuration(nBalls - 1, copy4, this)) && !result.contains(new Configuration(nBalls - 1, copy5, this)) && !result.contains(new Configuration(nBalls - 1, copy6, this)) && !result.contains(new Configuration(nBalls - 1, copy7, this))) 
                  result.add(new Configuration(nBalls - 1, element, this));
               }

               System.out.print( " (CAS 3 ) ");
               System.out.print( game[i][j-2] + ":" + game[i][j-1]);               

               if (game[i][j-2] == 1 && game[i][j-1] == 1) {

                  System.out.print( " (CAS 3 ) ");

                  byte[][] element = new byte[game.length][];
                  for(int k = 0 ; k < game.length ; k++)
                     element[k] = (byte[]) game[k].clone();

                  element[i][j] = 1;
                  element[i][j-2] = 0;
                  element[i][j-1] = 0;

                  /*   byte[][] copy1 = new byte[element.length][];
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
                  copy7[10 - k] = (byte[]) copy6[k].clone();}*/


                  /*	System.out.println( toString(copy) + "@@ " + toString(copyy) + "@@ " + toString(copyyy) + "@@ " + toString(copyyyy));   */
                  //if (!result.contains(new Configuration(nBalls - 1, element, this)) && !result.contains(new Configuration(nBalls - 1, copy1, this)) && !result.contains(new Configuration(nBalls - 1, copy2, this)) && !result.contains(new Configuration(nBalls - 1, copy3, this)) && !result.contains(new Configuration(nBalls - 1, copy4, this)) && !result.contains(new Configuration(nBalls - 1, copy5, this)) && !result.contains(new Configuration(nBalls - 1, copy6, this)) && !result.contains(new Configuration(nBalls - 1, copy7, this))) 
                  result.add(new Configuration(nBalls - 1, element, this));
               }

               System.out.print( " (CAS 4 ) ");
               System.out.print( game[i][j+2] + ":" + game[i][j+1] );               

               if (game[i][j+2] == 1 && game[i][j+1] == 1) {

                  System.out.print( " (CAS 4 ) ");

                  byte[][] element = new byte[game.length][];
                  for(int k = 0 ; k < game.length ; k++)
                     element[k] = (byte[]) game[k].clone();

                  element[i][j] = 1;
                  element[i][j+2] = 0;
                  element[i][j+1] = 0;

                  /*                  byte[][] copy1 = new byte[element.length][];
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
                  copy7[10 - k] = (byte[]) copy6[k].clone();}*/


						
                  /*		System.out.println( toString(copy) + "@@ " + toString(copyy) + "@@ " + toString(copyyy) + "@@ " + toString(copyyyy));  */
                  //if (!result.contains(new Configuration(nBalls - 1, element, this)) && !result.contains(new Configuration(nBalls - 1, copy1, this)) && !result.contains(new Configuration(nBalls - 1, copy2, this)) && !result.contains(new Configuration(nBalls - 1, copy3, this)) && !result.contains(new Configuration(nBalls - 1, copy4, this)) && !result.contains(new Configuration(nBalls - 1, copy5, this)) && !result.contains(new Configuration(nBalls - 1, copy6, this)) && !result.contains(new Configuration(nBalls - 1, copy7, this))) 
                  result.add(new Configuration(nBalls - 1, element, this));
               }

               System.out.println( "Fin " );

            }
         }
      }
      /* System.out.println( result); */
      return result;
   }

}
