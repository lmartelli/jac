/*
  Copyright (C) 2003 Laurent Martelli <laurent@aopsys.com>

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
*/

package org.objectweb.jac.ide.diagrams;

import java.awt.Color;
import org.objectweb.jac.ide.ModelElement;

public abstract class MemberFigure extends TextFigure 
   implements ModelElementFigure
{
   ClassFigure parentFigure;

   public MemberFigure(ClassFigure parentFigure) {
      this.parentFigure = parentFigure;
   }

   public Color getTextColor() {
      return parentFigure.getColor();
   }

   public String getType() {
      String text = getText();
      int sep = text.indexOf(':');
      if (sep == -1) {
         return "";
      } else {
         return text.substring(sep+1).trim();
      }      
   }

   public String getName() {
      String text = getText();
      int sep = text.indexOf(':');
      if (sep == -1) {
         return text.trim();
      } else {
         return text.substring(0,sep).trim();
      }      
   }

   public abstract ModelElement getSubstance();
}
