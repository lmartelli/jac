
package org.objectweb.jac.samples.solitaire;

import java.util.*;

public class Run{
	public static void main(String[] args) {
		byte[][] game = new byte[][] {
			new byte[] {2,2,2,2,2,2,2,2,2,2,2} ,
			new byte[] {2,2,2,2,2,2,2,2,2,2,2} ,
			new byte[] {2,2,2,2,0,0,0,2,2,2,2} , 
			new byte[] {2,2,2,2,0,1,0,2,2,2,2} ,
			new byte[] {2,2,0,0,1,1,1,0,0,2,2} ,
			new byte[] {2,2,0,1,1,0,1,1,0,2,2} ,
			new byte[] {2,2,0,0,1,1,1,0,0,2,2} ,
			new byte[] {2,2,2,2,0,1,0,2,2,2,2} , 
			new byte[] {2,2,2,2,0,0,0,2,2,2,2} ,
			new byte[] {2,2,2,2,2,2,2,2,2,2,2} ,
			new byte[] {2,2,2,2,2,2,2,2,2,2,2} 
		} ;
		
		int x = 0;
		Vector current = new Vector();
		current.add( new Configuration(32,game,null) );
		Vector next = new Vector();
		boolean finish = true;
		do {	x = x+1;
			System.out.println( "Next step for :" );
			//System.out.println( current );
			for ( int i=0; i < current.size(); i++ ) {
				Vector children = new Vector();
				children = ((Configuration)current.get(i)).createChildren();
				
				for (int j=0; j < children.size(); j++) {
					
					byte[][] element = new byte[11][];
						for(int k = 0 ; k < 11 ; k++)
						element[k] = (byte[]) ((Configuration)children.get(j)).game[k].clone();
					
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
					
					
          /*	if (!next.contains(new Configuration(0, element, (Configuration)current.get(i)))
					 && ((!copy1.equals(element)) && !next.contains(new Configuration(0, copy1, (Configuration)current.get(i))))
					 && ((!copy2.equals(element)) && (!copy2.equals(copy1)) && !next.contains(new Configuration(0, copy2, (Configuration)current.get(i))))
					 && ((!copy3.equals(element)) && (!copy3.equals(copy1)) && (!copy3.equals(copy2)) && !next.contains(new Configuration(0, copy3, (Configuration)current.get(i))))
					 && ((!copy4.equals(element)) && (!copy4.equals(copy1)) && (!copy4.equals(copy2)) && (!copy4.equals(copy3)) && !next.contains(new Configuration(0, copy4, (Configuration)current.get(i))))
					 && ((!copy5.equals(element)) && (!copy5.equals(copy1)) && (!copy5.equals(copy2)) && (!copy5.equals(copy3))  && (!copy5.equals(copy4)) && !next.contains(new Configuration(0, copy5, (Configuration)current.get(i))))
					 && ((!copy6.equals(element)) && (!copy6.equals(copy1)) && (!copy6.equals(copy2)) && (!copy6.equals(copy3))  && (!copy6.equals(copy4)) && (!copy6.equals(copy5)) && !next.contains(new Configuration(0, copy6, (Configuration)current.get(i))))
					 && ((!copy6.equals(element)) && (!copy7.equals(copy1)) && (!copy7.equals(copy2)) && (!copy7.equals(copy3))  && (!copy7.equals(copy4)) && (!copy7.equals(copy5))&& (!copy7.equals(copy6)) && !next.contains(new Configuration(0, copy7, (Configuration)current.get(i))))   )*/ 
		/* System.out.println(Configuration.toString(element) + Configuration.toString(copy1) + Configuration.toString(copy2) + Configuration.toString(copy3) + Configuration.toString(copy4) + Configuration.toString(copy5) + Configuration.toString(copy6) + Configuration.toString(copy7)   );	*/		
		next.add ( children.get(j) );
					}		}System.out.println( "taille : " + next.size() );

			finish = true;		
			
			if (next.size() > 0) {
				current = next;
				finish = false;
			}
			next = new Vector();
            } while ( finish == false );
	Configuration affichage = new Configuration(0, game , null);
		
		for (int b=0; b < current.size(); b++) {	
			affichage = (Configuration)current.get(b);
			System.out.println("--------------------------------------------------" );
			for ( int a=0; a < x; a++ )		{			
			System.out.println( affichage );
			affichage = affichage.parent;
			}
	}

} 

}
