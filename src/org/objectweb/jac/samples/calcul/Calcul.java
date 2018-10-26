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

import java.util.List;
import java.util.Vector;

public class Calcul 
{

   protected float value = 0;

   /**
    * Adds toadd to value.
    *
    * @param toadd the value to add.
    */
   public void add(int toadd) {
      value+=toadd;
      addOp(new Op("add",""+toadd));
   }

   /**
    * Subs tosub from value.
    *
    * @param tosub the value to sub.
    */
   public void sub(float tosub) {
      value-=tosub;
      addOp(new Op("sub",""+tosub));
   }

   /**
    * Gets current value.
    *
    * @return the current value.
    */
   public float getValue() {
      return value;
   }

   /**
    * Sets current value.
    *
    * @param value the new value.
    */
   public void setValue( float value ) {
      this.value=value;
   }

   /**
    * Execute i additions of 1 to test program.
    *
    * @param i the number of additions to execute.
    */
   public void bench(int i) {
      for (; i>0; i--) {
         add(1);
      }
   }

   List ops=new Vector();
   
   /**
    * Get the value of ops.
    * @return value of ops.
    */
   public List getOps() {
      return ops;
   }
   
   /**
    * Set the value of ops.
    * @param v  Value to assign to ops.
    */
   public void setOps(List  v) {
      this.ops = v;
   }
   
   public void addOp(Op op) {
      ops.add(op);
   }

   public void removeOp(Op op) {
      ops.remove(op);
   }

   public void clearOps() {
      ops.clear();
   }

}

// Local Variables: ***
// c-basic-offset:3 ***
// End: **

