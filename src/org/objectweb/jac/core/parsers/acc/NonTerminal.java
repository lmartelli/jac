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
  USA */

package org.objectweb.jac.core.parsers.acc;

import java.util.Iterator;
import java.util.Vector;
import org.apache.log4j.Logger;

/**
 * Represents a non terminal rule
 */
public class NonTerminal extends SyntaxElement {
    static Logger logger = Logger.getLogger("acc.parser");

    Vector children = new Vector();
   
    public NonTerminal(String name, SyntaxElement[] children) {
        super(name,
              children[0].getLeft(),
              children[children.length-1].getRight());
        for (int i=0; i<children.length; i++) {
            addChild(children[i]);
        }
    }

    public NonTerminal(String name, SyntaxElement child) {
        super(name,child.getLeft(),child.getRight());
        addChild(child);
    }

    public NonTerminal(String name) {
        super(name,Integer.MAX_VALUE,-1);
    }

    /**
     * Add child at the end
     */
    public void addChild(SyntaxElement se) {
        children.add(se);
        childAdded(se);
    }

    /**
     * Add child at the beginning
     */
    public void insertChild(SyntaxElement se) {
        children.add(0,se);
        childAdded(se);
    }

    /**
     * Returns the child at a given index
     */
    public SyntaxElement getChild(int index) {
        return (SyntaxElement)children.get(index);
    }

    /**
     * Returns a child with a given name, or null.
     */
    public SyntaxElement getChild(String name) {
        Iterator it = children.iterator();
        while (it.hasNext()) {
            SyntaxElement child = (SyntaxElement)it.next();
            if (child.getName().equals(name)) {
                return child;
            }
        }
        return null;
    }

    protected void childAdded(SyntaxElement se) {
        se.setParent(this);
        if (se.getLeft()<left) 
            left = se.getLeft();
        if (se.getRight()>right) 
            right = se.getRight();
    }

    /**
     * Returns the terminal syntax element of a given position
     * @param position the position
     * @return A Terminal se such that se.getLeft()<=position &&
     * se.getRight()>=position or null if there no such SyntaxElement.
     */
    public Terminal getTerminalAt(int position) {
        Iterator it = children.iterator();
        while (it.hasNext()) {
            SyntaxElement se = (SyntaxElement)it.next();
            if (se.getLeft()<=position && se.getRight()>=position) {
                logger.debug("Found "+se+" at "+position);
                if (se instanceof Terminal)
                    return (Terminal)se;
                else
                    return ((NonTerminal)se).getTerminalAt(position);
            }
        }
        return null;
    }


    /**
     * Returns the deepest syntax element at a given position
     * @param position the position
     * @return A SyntaxElement se such that se.getLeft()<=position &&
     * se.getRight()>=position or null if there no such SyntaxElement.
     */
    public SyntaxElement getSyntaxElementAt(int position) {
        Iterator it = children.iterator();
        while (it.hasNext()) {
            SyntaxElement se = (SyntaxElement)it.next();
            if (se.getLeft()<=position && se.getRight()>=position) {
                logger.debug("Found "+se+" at "+position);
                if (se instanceof Terminal)
                    return se;
                else
                    return ((NonTerminal)se).getSyntaxElementAt(position);
            }
        }
        if (getLeft()<=position && getRight()>=position)
            return this;
        else
            return null;
    }
   
}
