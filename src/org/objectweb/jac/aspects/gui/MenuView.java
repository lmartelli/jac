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

/**
 * The GUI target independent menu. Submenus, actions, and separators
 * appears in the order they are added into the menu. */

public interface MenuView extends View {

    /**
     * Adds a sub-menu in this menu.
     *
     * @param label the sub-menu's label
     * @param icon the sub-menu's icon
     * @param submenu the submenu */ 
    void addSubMenu(String label, String icon, MenuView submenu);

    /**
     * Adds an action to this menu.
     *
     * @param label the sub-menu's label
     * @param icon the sub-menu's icon
     * @param callback the actual action */ 
    void addAction(String label, String icon, Callback callback);

    /**
     * Adds a separator in this menu. */
    void addSeparator();

    /**
     * Sets the position of the menu bar (TOP||BOTTOM||LEFT||RIGHT).
     *
     * @param position the position. If null, use the default position
     * @see Menu */
    void setPosition(String position);

    /**
     * Gets the position of the menu bar (TOP||BOTTOM||LEFT||RIGHT).
     *
     * @return the position
     * @see Menu */
    String getPosition();
}
