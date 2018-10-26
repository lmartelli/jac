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

package org.objectweb.jac.samples.ring;

public class RingElement
{

   /** previousElement is the reference of the previous object on the
       ring */
   public RingElement previousElement;
   
   
   public RingElement() {}
   public RingElement( RingElement previousElement ) {
      this.previousElement = previousElement;
   }
   
   
   /**
    * Set the previous object on the ring.
    *
    * @param previousElement  the reference of the previous object on the ring
    */
   
   public void setPrevious( RingElement previousElement ) {

      System.out.println( "<<< setPrevious() called on " +
      			  toString() + " >>>" );

      this.previousElement = previousElement;
   }
   
   /**
    * Initiate or propagate a round trip on the ring.
    *
    * @param step number of ring elements to visit
    */
   
   public void roundTrip( int step ) {
   
      System.out.println( "<<< roundTrip() called on "+ toString() +
      			  ", step: " + step + " >>>" );

      /**
       * Call roundTrip on the previous element if needed */ 
       
      if( step > 0 ) previousElement.roundTrip( step-1 );
      
      System.out.println( "<<< roundTrip() returned on " +
      			  toString() + " >>>" );
   }

}
