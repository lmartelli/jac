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

/**
 * The base class of diagram figures.
 * @see Diagram
 */

public class Figure {

    public Figure() {
    }

    /**
     * @param element the model element represented by this figure
     */
    public Figure(ModelElement element) {
        this.element = element;
    }

    Diagram diagram;
   
    /**
     * Get the value of diagram.
     * @return value of diagram.
     */
    public Diagram getDiagram() {
        return diagram;
    }
   
    /**
     * Set the value of diagram.
     * @param v  Value to assign to diagram.
     */
    public void setDiagram(Diagram  v) {
        this.diagram = v;
    }
   
    ModelElement element;
   
    /**
     * Get the value of the model element.
     * @return value of elt.
     */
    public ModelElement getElement() {
        return element;
    }
   
    /**
     * Set the value of the model element.
     * @param element  Value to assign to element.
     */
    public void setElement(ModelElement element) {
        this.element = element;
    }
   
}
