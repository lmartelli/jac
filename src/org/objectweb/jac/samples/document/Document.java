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

package org.objectweb.jac.samples.document;

/**
 * A simple document class.
 */

public class Document {
   
   /** Store the line */
   public String[] lines;

   /** Creates a document with the given lines */
   public Document( String[] lines ) {
      this.lines = lines;
   }

   /** Print the whole document -- uses printLine() */
   public void printAll () {
      for (int i = 0; i < lines.length; i++)
         printLine( i );
   }

   /** Print n lines of the document */
   public void printLines (int startLine, int n) {
      for (int i = startLine; i < n + startLine; i++)
         printLine( i );
   }      
   
   /** Print a line of the document (can be used directly) */
   public void printLine (int i) {
      System.out.println( lines[i] );
   }

    /** Set a line value */
    public void setLine( int number, String s ) {
	lines[number] = s;
    }

    /** Set a line value */
    public void setLines( String[] lines ) {
	this.lines = lines;
    }

   /** Return the number of lines */
   public int countLines() {
      return lines.length;
   }

   /** Prints out some lines. */ 
   public void test() {
       System.out.println("-- Printing the first line");
       printLine(0);
       System.out.println("-- Printing the lines 3 and 4");
       printLines(2,2);
       System.out.println("-- Printing all the document");
       printAll();
   }   

}




