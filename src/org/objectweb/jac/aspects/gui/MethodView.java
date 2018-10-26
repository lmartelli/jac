/*
  Copyright (C) 2002 Renaud Pawlak, Laurent Martelli
  
  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 2 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA */

package org.objectweb.jac.aspects.gui;

import org.objectweb.jac.core.rtti.AbstractMethodItem;

/**
 * Displays a button for a method
 */

public interface MethodView extends View {
    /**
     * Sets the substance method. */
    void setMethod(AbstractMethodItem method);

    /**
     * Sets the icon. */
    void setIcon(String icon);

    void setOnlyIcon(boolean onlyIcon);
}
