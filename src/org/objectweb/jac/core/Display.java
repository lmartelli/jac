/*
  Copyright (C) 2001-2002 Renaud Pawlak. <renaud@aopsys.com>

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

package org.objectweb.jac.core;

import org.objectweb.jac.core.rtti.*;
import java.util.Map;

/**
 * This interface is an abstract description of how a display is
 * defined in org.objectweb.jac.
 *
 * <p>In JAC, a GUI that needs to allow aspects to interfere with
 * itself (with output and input data) must implement this
 * interface. When the GUI invokes a non-gui object, it must then
 * define the "Gui.display" attribute to be itself so that internal
 * component know what display to use. If this display attribute is
 * not defined by the GUI, then the objects that need display
 * operations can use the default textual display (that uses
 * <code>System.out</code> and <code>System.in</code> for output and
 * input).
 *
 * @see org.objectweb.jac.core.Collaboration#get()
 * @see org.objectweb.jac.core.Collaboration
 */

public interface Display {

    /**
     * Gets the ID (a unique string identifier) of the current
     * display.
     *
     * @return a string 
     */
    String getDisplayID();

    /**
     * Sets the ID (a unique string identifier) of the current
     * display.
     *
     * @param displayID a string (should be unique) 
     */
    void setDisplayID(String displayID);

    /**
     * Shows the given object on the display.
     *
     * <p>The showed object is considered as a result.
     *
     * <p>On contrary to <code>showModal</code>, this method does not
     * stop the client thread execution.
     *
     * @param object the object to show 
     */
    void show(Object object);

    /**
     * Shows a view of an object.
     *
     * <p>The showed object is considered as a result.
     *
     * <p>On contrary to <code>showModal</code>, this method does not
     * stop the client thread execution.
     *
     * @param object the object to show 
     * @param viewType the type of the view to display
     * @param viewParams parameters to give the view constructor
     */
    void show(Object object,
              String viewType, Object[] viewParams);

    /**
     * Shows the given object on the display by opening a new core
     * view.
     *
     * <p>On contrary to <code>showModal</code>, this method does not
     * stop the client thread execution.
     *
     * @param object the object to show 
     */
    void openView(Object object);

    /**
     * Shows the given object on the display and waits for a user
     * input.
     *
     * <p>On contrary to <code>show</code>, this method stops the
     * client thread execution and waits for a user input to continue
     * (as an OK button click or a key pressing).
     *
     * @param object the object to show
     * @param title the window title if a window is opened
     * @param header a header message
     * @param parent the parent window 
     * @param canValidate if true, a validation button is added
     * @param canCancel if true, a cancellation button is added
     * @param canClose if true, a closing button is added
     * @return false if the user canceled 
     *
     * @see #showModal(Object,String,Object[],String,String,Object,boolean,boolean,boolean)
     */
    boolean showModal(Object object, String title, String header,  
                      Object parent,
                      boolean canValidate, boolean canCancel, 
                      boolean canClose);

    /**
     * Shows the given object on the display and waits for a user
     * input.
     *
     * @param object the object to show
     * @param viewType the type of view to build or the object
     * @param viewParams parameters to give the view constructor
     * @param title the window title if a window is opened
     * @param header a header message
     * @param parent the parent window 
     * @param okButton if true, a validation button is added
     * @param cancelButton if true, a cancellation button is added
     * @param closeButton if true, a closing button is added
     * @return false if the user canceled 
     *
     * @see #showModal(Object,String,String,Object,boolean,boolean,boolean)
     */
    boolean showModal(Object object, 
                      String viewType, Object[] viewParams,
                      String title, String header, 
                      Object parent,
                      boolean okButton, boolean cancelButton, 
                      boolean closeButton);

    /**
     * Show a customized Gui.
     *
     * @param id the id of the customized GUI to show
     * @param customized the CustomizedGUI to show
     *
     * @see org.objectweb.jac.aspects.gui.CustomizedGUI
     */
    void showCustomized(String id, Object customized);

    /**
     * Show a customized Gui.
     *
     * @param id the id of the customized GUI to show
     * @param customized the CustomizedGUI to show
     * @param panels contents of panels ( panelID -> object)
     *
     * @see org.objectweb.jac.aspects.gui.CustomizedGUI
     */
    void showCustomized(String id, Object customized, Map panels);

    /**
     * Rebuilds all customized GUI windows.
     */
    void fullRefresh();

    /**
     * Asks the user to fill the parameters to prepare the invocation
     * of the given method.
     *
     * <p>This operation stops the client thread.
     *
     * @param object the object that contains the method (null if a
     * constructor)
     * @param method the method to fill the parameters o
     * @param parameters the parameters values; as an input, they can
     * be set by the client to fill default values for these
     * parameters; as an output, they must be used by the client as the
     * actual parameter values to call the given method
     * @return false if the user canceled 
     */
    boolean showInput(Object object, AbstractMethodItem method, 
                      Object[] parameters);

    /**
     * Refresh the display.
     *
     * <p>This method is useful for some kind of displays when the
     * refresh operation cannot be done automatically when the
     * displayed objects states change (e.g. a web display).
     *
     * @see org.objectweb.jac.aspects.gui.web.WebDisplay 
     */
    void refresh();

    /**
     * Displays a message to the user.
     *
     * @param title the window title if a window is opened
     * @param message a header message
     * @param canValidate if true, a validation button is added
     * @param canCancel if true, a cancellation button is added
     * @param canClose if true, a closing button is added
     * @return false if the user canceled 
     */
    boolean showMessage(String message, String title,
                        boolean canValidate, boolean canCancel, 
                        boolean canClose);

    /**
     * Shows a message to the user.
     *
     * @param title the title of the window
     * @param message the message
     *
     * @see #showError(String,String)
     */
    void showMessage(String title, String message);

    /**
     * Shows a message to the user.
     *
     * @param title the title of the window
     * @param message the message
     * @return the auto-refreshed window
     *
     * @see #showError(String,String)
     */
     Object showRefreshMessage(String title, String message);

    /**
     * Show an error message to the user.
     *
     * @param title the title of the window
     * @param message the error message
     *
     * @see #showMessage(String,String)
     */
    void showError(String title, String message);

    /**
     * Notifies the display that a new application has just started.
     *
     * <p>At this step, all the root objects of the application have
     * been created and it is time to open the main view if any. */
   
    void applicationStarted();

    /**
     * Closes this display. 
     */
    void close();

    /**
     * Called before interactively calling a method with parameters,
     * so that the display can set some of them.
     * @return false if there some parameters are still unassigned 
     */
    boolean fillParameters(AbstractMethodItem method, Object[] parameters);

    /**
     * Called after interactively calling a method with parameters.
     */
    void onInvocationReturn(Object substance, AbstractMethodItem method);
}



