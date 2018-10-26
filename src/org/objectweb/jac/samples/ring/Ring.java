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

/**
 * The following example shows a ring structure that works as well in a
 * centralized or in a distributed way.
 *
 * <p>To run this sample:
 * - rmiregistery &
 * - go 3 & (in the org/objectweb/jac/scripts directory to launch 2 jac servers)
 * - jac -G -D Run (you can call roundtrip on any element, with the 
 * number of token exchanched as the argument) */

public class Ring {

    public static void main( String[] args ) {

        RingElement element0 = new RingElement();
        RingElement element1 = new RingElement();
        RingElement element2 = new RingElement();
	
        element0.setPrevious( element2 );
        element1.setPrevious( element0 );
        element2.setPrevious( element1 );
        
        element2.roundTrip( 50 );
    }
}





