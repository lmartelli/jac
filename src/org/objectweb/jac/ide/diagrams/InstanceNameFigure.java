/*
  Copyright (C) 2002 Renaud Pawlak

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
  USA. */

package org.objectweb.jac.ide.diagrams;

import CH.ifa.draw.figures.TextFigure;

public class InstanceNameFigure extends TextFigure {

   org.objectweb.jac.ide.Instance substance;
   
   /**
    * Get the value of substance.
    * @return value of substance.
    */
   public org.objectweb.jac.ide.Instance getSubstance() {
      return substance;
   }
   
   /**
    * Set the value of substance.
    * @param v  Value to assign to substance.
    */
   public void setSubstance(org.objectweb.jac.ide.Instance  v) {
      this.substance = v;
   }
   
   public String getName() {
      return getText();
   }

   public void setText(String s) {
      super.setText(s);
      if( substance != null && !DiagramView.init ) {
         if( s.indexOf(":") != -1 ) {
            substance.setName(s.substring(0,s.indexOf(":")));
         } else {
            substance.setName(s);
         }
      }
   }

}
