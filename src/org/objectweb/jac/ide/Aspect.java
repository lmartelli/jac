/*
  Copyright (C) 2002 Renaud Pawlak <renaud@aopsys.com>

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 2 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  USA */

package org.objectweb.jac.ide;

import java.util.Vector;
import java.util.List;

public class Aspect extends Class {

   List pointcutLinks=new Vector();
   
   /**
    * Get the value of pointcutLinks.
    * @return value of pointcutLinks.
    */
   public List getPointcutLinks() {
      return pointcutLinks;
   }
   
   public void addPointcutLink(PointcutLink l) {
      pointcutLinks.add(l);
      l.setStart(this);
   }   

   public void removePointcutLink(PointcutLink l) {
      pointcutLinks.remove(l);
   }   

   List aspectMethods=new Vector();
   
   /**
    * Get the value of aspectMethods.
    * @return value of aspectMethods.
    */
   public List getAspectMethods() {
      return aspectMethods;
   }
   
   public void addAspectMethod(AspectMethod m) {
      aspectMethods.add(m);
   }

   public void removeAspectMethod(AspectMethod m) {
      aspectMethods.remove(m);
   }

}
