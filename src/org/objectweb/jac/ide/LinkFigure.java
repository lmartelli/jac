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

import java.awt.Point;
import java.util.List;
import java.util.Vector;

/**
 * A Link figure of a diagram.
 * @see Diagram
 * @see Link
 */

public class LinkFigure extends Figure {

    public LinkFigure() {
    }

    public LinkFigure(Link link) {
        super(link);
        nameCorner = new Point();
        startRoleCorner = new Point();
        startCardinalityCorner = new Point();
        endRoleCorner = new Point();
        endCardinalityCorner = new Point();
        points.add(new Point(0,0));
        points.add(new Point(0,0));
    }

    public LinkFigure(Link link,
                      Point nameCorner,
                      Point startRoleCorner,
                      Point startCardinalityCorner,
                      Point endRoleCorner,
                      Point endCardinalityCorner) {
        super(link);
        this.nameCorner = nameCorner;
        this.startRoleCorner = startRoleCorner;
        this.startCardinalityCorner = startCardinalityCorner;
        this.endRoleCorner = endRoleCorner;
        this.endCardinalityCorner = endCardinalityCorner;
        points.add(new Point(0,0));
        points.add(new Point(0,0));
    }

    public Link getLink() {
        return (Link)element;
    }

    public void translateName(int dx,int dy) {
        this.nameCorner.translate(dx,dy);
    }

    public void translateStartRole(int dx,int dy) {
        this.startRoleCorner.translate(dx,dy);
    }

    public void translateEndRole(int dx,int dy) {
        this.endRoleCorner.translate(dx,dy);
    }

    public void translateStartCardinality(int dx,int dy) {
        this.startCardinalityCorner.translate(dx,dy);
    }

    public void translateEndCardinality(int dx,int dy) {
        this.endCardinalityCorner.translate(dx,dy);
    }

    Point nameCorner = new Point();
   
    /**
     * Get the value of nameCorner.
     * @return value of nameCorner.
     */
    public Point getNameCorner() {
        return nameCorner;
    }
   
    /**
     * Set the value of nameCorner.
     * @param v  Value to assign to nameCorner.
     */
    public void setNameCorner(Point  v) {
        this.nameCorner = v;
    }
   
    Point startRoleCorner;
   
    /**
     * Get the value of startRoleCorner.
     * @return value of startRoleCorner.
     */
    public Point getStartRoleCorner() {
        return startRoleCorner;
    }
   
    /**
     * Set the value of startRoleCorner.
     * @param v  Value to assign to startRoleCorner.
     */
    public void setStartRoleCorner(Point  v) {
        this.startRoleCorner = v;
    }
   
    Point endRoleCorner;
   
    /**
     * Get the value of endRoleCorner.
     * @return value of endRoleCorner.
     */
    public Point getEndRoleCorner() {
        return endRoleCorner;
    }
   
    /**
     * Set the value of endRoleCorner.
     * @param v  Value to assign to endRoleCorner.
     */
    public void setEndRoleCorner(Point  v) {
        this.endRoleCorner = v;
    }
   
    Point startCardinalityCorner;
   
    /**
     * Get the value of startCardinalityCorner.
     * @return value of startCardinalityCorner.
     */
    public Point getStartCardinalityCorner() {
        return startCardinalityCorner;
    }
   
    /**
     * Set the value of startCardinalityCorner.
     * @param v  Value to assign to startCardinalityCorner.
     */
    public void setStartCardinalityCorner(Point  v) {
        this.startCardinalityCorner = v;
    }
   
    Point endCardinalityCorner;
   
    /**
     * Get the value of endCardinalityCorner.
     * @return value of endCardinalityCorner.
     */
    public Point getEndCardinalityCorner() {
        return endCardinalityCorner;
    }
   
    /**
     * Set the value of endCardinalityCorner.
     * @param v  Value to assign to endCardinalityCorner.
     */
    public void setEndCardinalityCorner(Point  v) {
        this.endCardinalityCorner = v;
    }

    List points = new Vector();
   
    /**
     * Get the value of points.
     * @return value of points.
     */
    public List getPoints() {
        return points;
    }
   
    /**
     * Set the value of points.
     * @param v  Value to assign to points.
     */
    public void setPoints(List  v) {
        this.points = v;
    }

    public int getPointCount() {
        return points.size();
    }
   
    public void addPoint(int index,Point p) {
        points.add(index,p);
    }

    public void removePoint(int index) {
        points.remove(index);
    }

    public void setPoint(int index,Point p) {
        points.set(index,p);
    }

    public void translatePoint(int index, int dx, int dy) {
        Point p = (Point)points.get(index);
        p.x += dx;
        p.y += dy;
    }

}
