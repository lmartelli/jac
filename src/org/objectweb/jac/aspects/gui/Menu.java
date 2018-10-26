/*
  Copyright (C) 2002 Laurent Martelli.

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 2 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  USA */

package org.objectweb.jac.aspects.gui;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class Menu {
    /** The TOP menu's position */
    public static final String TOP = "TOP";
    /** The BOTTOM menu's position */
    public static final String BOTTOM = "BOTTOM";
    /** The LEFT menu's position */
    public static final String LEFT = "LEFT";
    /** The RIGHT menu's position */
    public static final String RIGHT = "RIGHT";
   
    // key -> [ callback | Menu | null ]
    HashMap map = new HashMap();
    // item order
    Vector keys = new Vector();
    // icon
    String icon;

    /**
     * Sets the icon of the menu. */
    public void setIcon(String icon) {
        this.icon = icon;
    }
    /**
     * Gets the menu icon. */
    public String getIcon() {
        return icon;
    }
    String position = null;
    /**
     * Gets the menu position. */
    public String getPosition() {
        return position;
    }
    /**
     * Sets the menu position. */
    public void setPosition(String position) {
        this.position = position;
    }
    /**
     * Gets an action or sub-menu from its name. */
    public Object get(String key) {
        return map.get(key);
    }
    /**
     * Adds an action or sub-menu. */
    public void put(String key, Object value) {
        if (!map.containsKey(key))
            keys.add(key);
        map.put(key,value);
    }
    /**
     * Gets the contents of this menu. */
    public List getKeys() {
        return keys;
    }
    /**
     * Returns true if this menu contains a given element. */ 
    public boolean containsKey(String key) {
        return map.containsKey(key);
    }
    /**
     * Adds a separator in this menu. */
    public void addSeparator() {
        keys.add(null);
    }
    /**
     * Returns the items count. */
    public int size() {
        return keys.size();
    }
}
