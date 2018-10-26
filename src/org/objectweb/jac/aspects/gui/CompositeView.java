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
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA */

package org.objectweb.jac.aspects.gui;

import java.util.Collection;

/**
 * This view is a composite view (i.e. a view that can contains other
 * view). View and CompositeView follow the GoF composite pattern. */

public interface CompositeView extends View {
    /**
     * Adds a component view in the composite.
     *
     * @param component the component view
     * @param extraInfos some positionning infos on where the component
     * should be added in the composite 
     */
    void addView(View component, Object extraInfos);

    /**
     * Adds a component view in the composite.
     *
     * @param component the component view 
     */
    void addView(View component);

    /**
     * Adds an horizontal separator in the composite in order to insert
     * blanks between components. 
     */
    void addHorizontalStrut(int width);

    /**
     * Adds a vertical separator in the composite in order to insert
     * blanks between components. 
     */
    void addVerticalStrut(int height);

    /**
     * Gets a component view from an id object. 
     */
    View getView(Object id);

    /**
     * Gets all the component views in this composite. 
     */
    Collection getViews();

    /**
     * Tells wether the composite view contains a view with some given
     * view type and paramters
     *
     * @param viewType the type of the view to look for
     * @param parameters the parameters of the view to look for
     * @return true if the composite contains a view with the given
     * type and parameters
     */
    boolean containsView(String viewType, Object[] parameters);

    /**
     * Removes a component view in this composite.
     *
     * @param component the component view to remove
     * @param validate wether to validate values in editors
     */
    void removeView(View component, boolean validate);

    /**
     * Removes all the views in this composite. 
     *
     * @param validate wether to validate values in editors
     */
    void removeAllViews(boolean validate);
}
