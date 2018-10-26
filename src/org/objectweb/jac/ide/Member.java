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

import org.objectweb.jac.util.Strings;


/**
 * A member item of a class such as a field or a method.
 */
public abstract class Member extends TypedElement implements Visibility {

    Class parent;
   
    /**
     * Get the value of parent.
     * @return value of parent.
     */
    public Class getParent() {
        return parent;
    }
   
    /**
     * Set the value of parent.
     * @param v  Value to assign to parent.
     */
    public void setParent(Class  v) {
        this.parent = v;
    }
      
    public abstract String getPrototype();

    /** Flag indicating if the member is static or not*/
    boolean isStatic = false;
    /** Returns value of isStatic field */
    public boolean isStatic() {
        return isStatic;
    }
    /** Sets value of isStatic field */
    public void setStatic(boolean isStatic) {
        this.isStatic = isStatic;
    }

    /**
     * Returns a string of all the modifiers of a member item (field or
     * method)
     * @return a String with the modifiers, seperated by spaces
     * @see #isStatic()
     * @see #getVisibility()
     */
    public String getModifiers() {
        String modifiers = "";
        switch(visibility) {
            case PUBLIC: modifiers = "public"; break;
            case PROTECTED: modifiers = "protected"; break;
            case PRIVATE: modifiers = "private"; break;
        }
        if (isStatic) {
            modifiers += " static";
        }
        return modifiers;
    }

    public String getFullName() {
        return parent.getFullName()+"."+getName();
    }

    public String getGenerationName() {
        return Strings.toUSAscii(getName());
    }

    public String getGenerationFullName() {
        return Strings.toUSAscii(getFullName());
    }

    public Project getProject() {
        if (parent!=null)
            return parent.getProject();
        else
            return null;
    }

    int visibility = PUBLIC;
    public int getVisibility() {
        return visibility;
    }
    public void setVisibility(int newVisibility) {
        this.visibility = newVisibility;
    }
   
}
