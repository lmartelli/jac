/*
  Copyright (C) 2001-2002 Renaud Pawlak, Laurent Martelli
  
  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 2 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  USA */

package org.objectweb.jac.aspects.gui;

/**
 * This interface allows views comparison. If both parameters and
 * types are equals for the compared views, then the two view are
 * considered as equal. */

public interface ViewIdentity {

    /**
     * Sets the parameters that were used to construct the view. 
     */
    void setParameters(Object[] parameters);

    /**
     * The parameters that were used to compile the view. 
     */
    Object[] getParameters();

    /**
     * Gets the view type.
     *
     * @return view type string as declared in web.acc or swing.acc
     * depending on the GUI target 
     */
    String getType();

    /**
     * Set the view type. 
     */
    void setType(String type);
    
    /**
     * Tells if a view is the same as another one, ie it has the same
     * type and parameters.
     */
    boolean equalsView(ViewIdentity view);

    boolean equalsView(String type, Object[] parameters);
}

