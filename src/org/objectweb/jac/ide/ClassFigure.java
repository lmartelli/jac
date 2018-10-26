/*
  Copyright (C) 2002-2003 Laurent Martelli <laurent@aopsys.com>

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
 * A class figure of a diagram.
 * @see Diagram
 * @see Class
 */

public class ClassFigure extends GenericFigure {

    public ClassFigure() {
    }

    /**
     * Creates a new class figure
     *
     * @param cl the class represented by this figure
     * @param corner the upperleft corner of the figure
     */
    public ClassFigure(Class cl, Point corner) {
        super(cl,corner);
        this.element = cl;
        this.corner = corner;
    }

    public ClassFigure(Class cl) {
        super(cl);
        this.element = cl;
        this.corner = new Point();
    }

    /**
     * Get the value of cl.
     * @return value of cl.
     */
    public Class getCl() {
        return (Class)element;
    }

    boolean hideMethods;
    public boolean isHideMethods() {
        return hideMethods;
    }
    public void setHideMethods(boolean newHideMethods) {
        this.hideMethods = newHideMethods;
    }

    boolean hideFields;
    public boolean isHideFields() {
        return hideFields;
    }
    public void setHideFields(boolean newHideFields) {
        this.hideFields = newHideFields;
    }

   
}
