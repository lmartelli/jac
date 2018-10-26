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
*/

package org.objectweb.jac.ide.diagrams;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Color;

import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.DrawingView;

import org.objectweb.jac.ide.Package;
import org.objectweb.jac.ide.Aspect;

public class AspectFigure extends ClassFigure {

   public AspectFigure(org.objectweb.jac.ide.ClassFigure figure, Package pack, 
                       DrawingView view) {
      super(figure,pack,view);
   }

   protected void drawBorder(Graphics g) {
      
      Rectangle r = displayBox();
      
      g.setColor(Color.orange);

      g.fillRect(r.x, r.y, r.width, r.height);
      
      g.setColor(Color.red);
      
      g.drawRect(r.x, r.y, r.width, r.height);
      
      
      Figure f = figureAt(0);
      Rectangle rf = f.displayBox();
      
      g.drawLine(r.x,r.y+rf.height+1,r.x+r.width,r.y+rf.height+1);
      
      if( fieldFigures.size() > 0 ) {
         f = (Figure) fieldFigures.get(0);
         rf = f.displayBox();
         g.drawLine(r.x,rf.y,r.x+r.width,rf.y);
      }
      
   }

   public Aspect getAspect() {
      return (Aspect)getSubstance();
   }

}
