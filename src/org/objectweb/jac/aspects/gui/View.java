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

import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.MethodItem;

public interface View extends ViewIdentity {
    /**
     * Sets the label (the identifier) of the view.
     *
     * @param label a string that identifies the view 
     */
    void setLabel(String label);

    /**
     * Gets the view's label (identifier).
     *
     * @return the view's label 
     */
    String getLabel();

    /**
     * Sets a dynamic message to be displayed by the view when opened.
     *
     * @param method the method that returns a string to be dislayed
     * @see #setDescription(String) 
     */
    void setMessage(MethodItem method);
   
    /**
     * Gets the dynamic message.
     *
     * @return the dynamic message 
     */
    MethodItem getMessage();

    /**
     * Sets a static message to be display by the view when opened.
     *
     * @param description a string to be displayed as is
     * @see #setMessage(MethodItem) 
     */
    void setDescription(String description);

    /**
     * Gets the description of this view. 
     *
     * @return the description 
     */
    String getDescription();

    /**
     * Sets the display context for this view.
     *
     * <p>The display context contains the factory that is used for
     * this view. 
     */
    void setContext(DisplayContext context);

    /**
     * @return the DisplayContext of the view
     */
    DisplayContext getContext();

    /**
     * Sets the factory for this view (WEB, SWING or other supported
     * factory). 
     */
    void setFactory(ViewFactory factory);

    /**
     * Gets the factory. 
     */
    ViewFactory getFactory();

    /**
     * Sets the preferred size for this view. 
     *
     * @param width the preferred width. If null, the preferred width is not changed.
     * @param height the preferred height. If null, the preferred height is not changed.
     */
    void setSize(Length width, Length height);

    /**
     * Close this view. This should be upcalled when the view is closed
     * in order to free resources or close other dependant views. 
     *
     * @param validate wether to validate any value contained in editors.
     */
    void close(boolean validate);

    /**
     * Tells if this view has been closed by the GUI or the user. 
     */
    boolean isClosed();

    /**
     * Validate this view: its content may be saved when this method is
     * upcalled. 
     */
    //void validate();

    /**
     * Sets the view style. The style can be exploited in different
     * maners depending on the GUI supports (e.g. with CSS for the
     * WEB). Styles are user-defined.
     */
    void setStyle(String style);
   
    /**
     * Gets the user-defined style for this view.
     *
     * @return the style, null if undefined 
     */ 
    String getStyle();

    /**
     * Sets a border for this view. 
     */
    void setViewBorder(Border border);

    /**
     * Gets the border for this view.
     *
     * @return the border, null if undefined 
     */ 
    Border getViewBorder();
   
    /**
     * Sets the parent view of this view (a composite view).
     *
     * @see CompositeView 
     */
    void setParentView(View view);

    /**
     * Gets the parent view of this view.
     *
     * @see CompositeView 
     */
    View getParentView();

    /**
     * Gets the ancestor view whose parent is null
     */
    View getRootView();

    /**
     * Tells wether this view has a given view in its ancestors
     * @param ancestor the ancestor
     */
    boolean isDescendantOf(View ancestor);

    /**
     * Focus a field of an object's view.
     *
     * @param field the field to focus
     * @param option an extra option
     */
    void setFocus(FieldItem field, Object option);
}
