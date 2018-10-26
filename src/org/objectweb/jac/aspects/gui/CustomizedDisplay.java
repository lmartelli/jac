/*
  Copyright (C) 2001-2003 Laurent Martelli <laurent@aopsys.com>
  
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

import org.objectweb.jac.core.Display;
import java.util.Collection;

/**
 * Defines a display which contains customized GUI windows.
 *
 * <p>A display is an GUI entity that is used by the program to
 * interact with the GUI users.
 */
public interface CustomizedDisplay extends Display {
    /**
     * Returns a CustomizedView identified by its id
     *
     * @param customizedID the id of the CustomizedView
     * @return the customized if exists, null otherwise 
     */
    CustomizedView getCustomizedView(String customizedID);

    /**
     * Returns a collection of all CustomizedViews contained in the display.
     *
     * @return all the customized of the display
     */
    Collection getCustomizedViews();

    /**
     * Returns the ViewFactory of the display.
     *
     * <p>A view factory implements the creation methods for different
     * visualisation supports (e.g. SWING, WEB, ...).
     *
     * @return the factory that is used for this display 
     */
    ViewFactory getFactory();

    /**
     * Add a dialog to the list of timedout dialogs
     * @param dialog the timedout dialog
     */
    void addTimedoutDialog(DialogView dialog);

    /**
     * Close a window.
     *
     * @param window view window to close
     * @param validate wether to validate values in editors
     */
    void closeWindow(View window, boolean validate);

}
