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

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.objectweb.jac.util.Strings;

/**
 * This class implements a display context.
 *
 * <p>A display context contains a display (i.e. means to interact
 * with the user and to create new view in customized vindows), and a
 * customized view (i.e a root window of a GUI).</p>
 *
 * <p>A display context is passed in the interaction's flow so that
 * each element of the GUI can construct the right GUI elements using
 * the contextual factory. It is a defined as a collaboration
 * attribute used by aspects needing to interact with the GUI
 * (e.g. authentication, confirmation).
 *
 * @see org.objectweb.jac.core.Collaboration */

public class DisplayContext implements EditorContainer {
    static Logger logger = Logger.getLogger("gui.context");
    static Logger loggerEdit = Logger.getLogger("gui.editor");

    CustomizedDisplay display;
    CustomizedView customizedView;
    // the current window should be garbaged when closed 
    WeakReference window;
    Vector editors = new Vector();
    boolean showButtons = false;

    /**
     * Construct a new display context from a display and a
     * customized.
     *
     * @param display the display
     * @param customizedView the customized */
    public DisplayContext(CustomizedDisplay display, 
                          CustomizedView customizedView) {
        this.display = display;
        this.customizedView = customizedView;
    }

    /**
     * Construct a new display context from a display and an existing
     * window that can be of any type.
     *
     * @param display the display
     * @param window a window */

    public DisplayContext(CustomizedDisplay display, 
                          Object window) {
        this.display = display;
        this.window = new WeakReference(window);
    }

    /**
     * Returns the display for this context.
     *
     * <p>A display is an GUI entity that is used by the program to
     * interact with the GUI users.
     *
     * @return the display */

    public CustomizedDisplay getDisplay() {
        return display;
    }

    /**
     * Gets the current customized view.
     * 
     * <p>A customized is a root window for a GUI. A GUI may contain
     * several customized.
     *
     * @return the current customized */

    public CustomizedView getCustomizedView() {
        return customizedView;
    }
   
    /**
     * Sets the customized of this display context.
     *
     * @param customizedView the new customized */
   
    public void setCustomizedView(CustomizedView customizedView) {
        this.customizedView = customizedView;
    }

    /**
     * Sets the window for this display context.
     *
     * @param window the window */

    public void setWindow(Object window) {
        this.window = new WeakReference(window);
    }

    /**
     * Gets the window for this display context.
     *
     * @return the window */

    public Object getWindow() {
        if (customizedView!=null)
            return customizedView;
        else
            return window==null ? null : window.get();
    }

    // EditorContainer interface

    public void addEditor(Object editor) {
        editors.add(editor);
        logger.debug("addEditor "+editor+" -> size="+editors.size());
    }
    public void removeEditor(Object editor) {
        editors.remove(editor);
        logger.debug("removeEditor "+editor+" -> size="+editors.size());
    }
    public List getEditors() {
        return (List)editors.clone();
    }

    public boolean hasEnabledEditor() {
        Iterator it = editors.iterator();
        while (it.hasNext()) {
            Object view = it.next();
            if (view instanceof FieldEditor && 
                ((FieldEditor)view).isEnabled()) {
                loggerEdit.debug("Found enabled editor "+view+
                                 "("+((FieldEditor)view).getField()+")");
                return true;
            }
        }
        return false;
    }

    public void setShowButtons(boolean value) {
        this.showButtons = value;
    }
    public boolean showButtons() {
        return showButtons;
    }

    /**
     * A default string representation of the display context. */
    public String toString() {
        return "{display="+display+
            ",customized="+customizedView+
            ",window="+Strings.hex(window==null ? null : window.get())+"}";
    }
}

