/*
  Copyright (C) 2001-2003 Renaud Pawlak, Laurent Martelli
  
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

import org.objectweb.jac.core.rtti.ClassItem;



/**
 * This interface is implemented to define different value editors
 * depending on the type of the value to edit. */

public interface FieldEditor extends FieldView {

    /**
     * Gets the value of the edited object.
     *
     * @return an object of the edited type */

    Object getValue();

    /**
     * Sets wether the editor is embedded in a view.
     */
    void setEmbedded(boolean isEmbedded);
   
    /**
     * Called when the focus is given to the editor
     *
     * @param extraOption an optional parameter
     */
    void onSetFocus(Object extraOption);

    /**
     * Commits the changes in this editor. 
     */
    void commit();

    boolean isEnabled();
    void setEnabled(boolean enabled);

    /**
     * Sets the type of the edited value
     * @param type the type of the edited value
     */
    void setEditedType(ClassItem type);
}

