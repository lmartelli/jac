/*
  Copyright (C) 2002 Laurent Martelli <laurent@aopsys.com>
  Renaud Pawlak <renaud@aopsys.com>

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

import java.awt.Point;

/**
 * A genric figure of a diagram.
 * @see Diagram
 */

public class GenericFigure extends Figure {

    public GenericFigure() {
    }
   
    /**
     * @param element the model element represented by this figure
     * @param corner the upperleft corner of the figure
     */
    public GenericFigure(ModelElement element, Point corner) {
        super(element);
        this.corner = corner;
    }

    public GenericFigure(ModelElement element) {
        super(element);
        this.corner = new Point();
    }

    Point corner;
   
    /**
     * Get the value of corner.
     * @return value of corner.
     */
    public Point getCorner() {
        return corner;
    }
   
    /**
     * Set the value of corner.
     * @param v  Value to assign to corner.
     */
    public void setCorner(Point  v) {
        this.corner = v;
    }
   
    public void translate(int dx, int dy) {
        corner.translate(dx,dy);
    }
}
