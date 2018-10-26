/*
  Copyright (C) 2002-2003 Renaud Pawlak <renaud@aopsys.com>

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


import org.objectweb.jac.ide.InheritanceLink;
import org.objectweb.jac.util.Log;
import java.awt.Point;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

public class Diagram extends ModelElement {

    public Diagram() {
    }

    public Diagram(String name) {
        this.name = name;
    }

    Package container;
   
    /**
     * Get the value of container.
     * @return value of container.
     */
    public Package getContainer() {
        return container;
    }
   
    /**
     * Set the value of container.
     * @param v  Value to assign to container.
     */
    public void setContainer(Package  v) {
        this.container = v;
    }

    /* the figures (classes, links, etc) contained in this diagram */
    HashSet figures = new HashSet();

    public Set getFigures() {
        return figures;
    }

    public void addFigure(Figure figure) {
        figures.add(figure);
    }

    public void removeFigure(Figure figure) {
        figures.remove(figure);
    }
 
    /**
     * Removes the figure of an element.
     * @param element the element whose figure shall be removed
     */
    public void removeElement(ModelElement element) {
        Iterator it = figures.iterator();
        while (it.hasNext()) {
            Figure figure = (Figure)it.next();
            if (figure.getElement()==element) {
                removeFigure(figure);
                return;
            }
        }
    }

    /**
     * Removes an inheritance link between two classes
     * @param cl the subclass
     * @param superClass the superclass
     */
    public void removeInheritanceLink(Class cl, Class superClass) {
        Iterator it = figures.iterator();
        while (it.hasNext()) {
            Figure figure = (Figure)it.next();
            if (figure.getElement() instanceof InheritanceLink) {
                InheritanceLink link = (InheritanceLink)figure.getElement();
                if (link.getStart()==cl && link.getEnd()==superClass) {
                    removeFigure(figure);
                    return;
                }
            }
        }
    }

    /**
     * Tells if the diagram contains a figure that represents the given
     * model element. */
    public boolean contains(ModelElement element) {
        Iterator it = figures.iterator();
        while (it.hasNext()) {
            Figure figure = (Figure)it.next();
            if (figure.getElement()==element) {
                return true;
            }
        }
        return false;
    }

    /**
     * Create a new figure for an existing class
     */
    public void importClass(Class cl, Point corner) {
        figures.add(new ClassFigure(cl,corner));
    }

    /**
     * Gets relations of a class with other classes on the diagram
     * which are not on the diagram.
     * @param cl the class
     * @return a list of Link
     */
    public List getMissingRelations(Class cl) {
        List relations = new Vector();

        // find relation links
        Iterator it = cl.getRelationLinks().iterator();
        while (it.hasNext()) {
            RelationLink relation = (RelationLink)it.next();
            if (!contains(relation) && 
                contains(relation.getEnd()) && contains(relation.getStart())) {
                relations.add(relation);
            }
        }

        // find inheritance links
        Type superClass = cl.getSuperClass();
        if (superClass instanceof Class && contains(superClass)) {
            relations.add(new InheritanceLink(cl,(Class)superClass));
        }
        it = figures.iterator();
        while (it.hasNext()) {
            Figure figure = (Figure)it.next();
            if (figure.getElement() instanceof Class) {
                Class otherClass = (Class)figure.getElement();
                superClass = otherClass.getSuperClass();
                if (superClass instanceof Class && superClass==cl) {
                    relations.add(new InheritanceLink(otherClass,cl));
                }
            }
        }

        return relations;
    }
}
