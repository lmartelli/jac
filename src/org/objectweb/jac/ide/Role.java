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

package org.objectweb.jac.ide;

import org.objectweb.jac.util.Strings;


public class Role extends ModelElement implements Typed {

    public Role(Link link) {
        this.link = link;
    }

    public Role() {
    }

    /** The link this role belongs to */
    Link link;
    public Link getLink() {
        return link;
    }
    
    public void setLink(Link link) {
        this.link = link;
    }
    

    ModelElement end;
    /**
     * Gets the model element on which the link ends.
     * 
     * @return ending element */
    public ModelElement getEnd() {
        return end;
    }   
    /**
     * Sets the value of the ending element.
     */
    public void setEnd(ModelElement end) {
        this.end = end;
    }

    ModelElement start;
    /**
     * Gets the model element from which the link starts.
     * 
     * @return starting element */
    public ModelElement getStart() {
        return start;
    }   
    /**
     * Sets the value of the starting element.
     */
    public void setStart(ModelElement start) {
        this.start = start;
    }

    public Role oppositeRole() {
        if (link.getStartRole()==this)
            return link.getEndRole();
        else
            return link.getStartRole();
    }

    public boolean isStartRole() {
        return link.getStartRole()==this;
    }

    public boolean isEndRole() {
        return link.getEndRole()==this;
    }

    public String getGenerationName() {
        return Strings.toUSAscii(getName());
    }

    public String getGenerationFullName() {
        return Strings.toUSAscii(getFullName());
    }

}
