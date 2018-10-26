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
*/

package org.objectweb.jac.ide.diagrams;

import java.awt.*;

import CH.ifa.draw.figures.*;

public  class CircleTip implements LineDecoration {

   public void draw(Graphics g, int x1, int y1, int x2, int y2) {
      int radius = 8;
      g.setColor(Color.black);
      g.fillOval(x1-radius/2,y1-radius/2,radius,radius);
   }
}
