/*
  Copyright (C) 2001-2002 Laurent Martelli <laurent@aopsys.com>
  
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

package org.objectweb.jac.aspects.gui.swing;

import java.awt.Component;
import java.util.List;
import java.util.Vector;
import org.objectweb.jac.aspects.gui.Constants;
import org.objectweb.jac.aspects.gui.EditorContainer;
import org.objectweb.jac.aspects.gui.FieldEditor;
import java.util.Iterator;

public class SwingEditorContainer extends SwingContainerView 
    implements EditorContainer
{
    Vector editors = new Vector();
    boolean showButtons;
    /**
     * @param showButtons unused parameter
     */
    public SwingEditorContainer(boolean showButtons) {
        super(Constants.VERTICAL);
    }

    public void addEditor(Object editor) {
        editors.add(editor);
    }
    public void removeEditor(Object editor) {
        editors.remove(editor);
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
                return true;
            }
        }
        return false;
    }

    public void setShowButtons(boolean showButtons) {
        this.showButtons = showButtons;
    }
    public boolean showButtons() {
        return showButtons;
    }

    public void requestFocus() {
        loggerFocus.debug("requestFocus on "+this);
        if (!editors.isEmpty()) {
            Component component = (Component)editors.get(0);
            loggerFocus.debug("passing focus to "+component);
            component.requestFocus();
        }
    }
}
