/*
  Copyright (C) 2001-2003 Renaud Pawlak <renaud@aopsys.com>, 
                          Laurent Martelli <laurent@aopsys.com>
  
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

import org.objectweb.jac.core.rtti.ClassItem;
import java.util.Iterator;
import java.util.Vector;



/**
 * Used to choose a class to instantiate
 */
public class ClassChooser {
   public ClassChooser(ClassItem root) {
      this.root = root;
   }
   ClassItem root;
   public ClassItem getRoot() {
      return root;
   }
   ClassItem choice;
   public void setChoice(ClassItem cl) {
      this.choice = cl;
   }
   public ClassItem getChoice() {
      return choice;
   }

   public Vector getChoices() {
      Vector result = new Vector();
      if (!root.isAbstract())
         result.add(root);
      Iterator it = root.getChildren().iterator();
      while (it.hasNext()) {
         ClassItem cl = (ClassItem)it.next();
         if (!cl.isAbstract())
            result.add(cl);
      }
      return result;
   }
}
