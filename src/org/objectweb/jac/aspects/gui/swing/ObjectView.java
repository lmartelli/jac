/*
  Copyright (C) 2002 Laurent Martelli <laurent@aopsys.com>
  
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

import org.objectweb.jac.aspects.gui.Constants;
import org.objectweb.jac.aspects.gui.FieldEditor;
import org.objectweb.jac.aspects.gui.GuiAC;
import org.objectweb.jac.aspects.gui.TabsView;
import org.objectweb.jac.aspects.gui.View;
import org.objectweb.jac.core.rtti.FieldItem;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class ObjectView extends SwingContainerView 
{
   Vector editors = new Vector();
   boolean showButtons = true;

   public ObjectView() {
      super(Constants.VERTICAL);      
   }

   public void addEditor(FieldEditor editor) {
      editors.add(editor);
   }
   
   public List getEditors() {
      return editors;
   }

   public void setShowButtons(boolean value) {
      this.showButtons = value;
   }

   public void setFocus(FieldItem field, Object option) {
      String[] categories = GuiAC.getCategories(field);
      if (categories!=null && categories.length>0) {
         Iterator it = getViews().iterator();
         while(it.hasNext()) {
            View view = (View)it.next();
            if (view instanceof TabsView) {
               ((TabsView)view).select(categories[0]);
            }
         }
      }
   }

}
