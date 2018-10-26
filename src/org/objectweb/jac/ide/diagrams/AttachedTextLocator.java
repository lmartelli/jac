/*
  Copyright (C) 2002-2003 Renaud Pawlak <renaud@aopsys.com>,
                          Laurent Martelli <laurent@aopsys.com>

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

import CH.ifa.draw.standard.AbstractLocator;
import CH.ifa.draw.standard.ChopBoxConnector;
import CH.ifa.draw.framework.Figure;
import java.awt.Point;
import java.awt.Rectangle;
import org.objectweb.jac.util.Log;

public class AttachedTextLocator extends AbstractLocator {

   public Point locate(Figure owner,Figure locatedObject) {
      //if(DiagramView.init) return new Point(0,0);
      Log.trace("locator","locate text "+locatedObject+" owned by "+owner);

      LinkFigure f = (LinkFigure)owner;
      Point center = owner.center();
      
      if (f.endFigure()==null 
          || f.startFigure()==null
          || locatedObject==null) 
         return center;
      
      AttachedTextFigure text = (AttachedTextFigure)locatedObject;
      
      if (text.getType()==AttachedTextFigure.START_ROLE) {

         Log.trace("diagram",2,"locating end role");
         ChopBoxConnector chopper = new ChopBoxConnector(f.endFigure());
         return locate(chopper.findEnd(f),f.endFigure(),false);

      } else if (text.getType()==AttachedTextFigure.END_ROLE) {
         
         Log.trace("diagram",2,"locating start role");
         ChopBoxConnector chopper = new ChopBoxConnector(f.startFigure());
         return locate(chopper.findStart(f),f.startFigure(),true);

      } else if (text.getType()==AttachedTextFigure.START_CARDINALITY) {
         
         Log.trace("locator","locating end cardinality "+f.endFigure().center());
         ChopBoxConnector chopper = new ChopBoxConnector(f.endFigure());
         return locate(chopper.findEnd(f),f.endFigure(),true);

      } else if (text.getType()==AttachedTextFigure.END_CARDINALITY) { 
         
         Log.trace("locator","locating start cardinality "+f.startFigure().center());
         ChopBoxConnector chopper = new ChopBoxConnector(f.startFigure());
         return locate(chopper.findStart(f),f.startFigure(),false);

      } else if (text.getType()==AttachedTextFigure.NAME) {

         Rectangle r = owner.displayBox();
         Rectangle r2 = locatedObject.displayBox();
         Point p = owner.center();
         if (r.getWidth()>r.getHeight()) {
            return new Point(p.x, p.y+10); // hack
         } else {
            return new Point((int)(p.x-(r2.getWidth()/2)-5), p.y-10); // hack
         }
      }
      return center;
   }

   /**
    * Locate attached text
    * @param ep starting point (intersection of line with class)
    * @param f class figure the text is attached to
    * @param flip if true, flip sides
    */
   Point locate(Point ep, Figure f, boolean flip) {
      Point result = null;
      Point center = f.center();
      double dY = (double)(ep.y - center.y);
      double dX = (double)(ep.x - center.x);
      double c = 20/Math.sqrt(dY*dY+dX*dX);
      double dx = dX*c;
      double dy = dY*c;
      if (!flip) {
         result = new Point((int)(ep.x+dx-dy),(int)(ep.y+dy+dx));
      } else {
         result = new Point((int)(ep.x+dx+dy),(int)(ep.y+dy-dx));
      }
      Log.trace("locator","ep="+ep+" center="+center+
                " dx,dy="+(int)dx+","+(int)dy+" -> "+result);
      return result;
   }
}


