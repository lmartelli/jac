/*
  Copyright (C) 2003 Laurent Martelli <laurent@aopsys.com>

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 2 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  USA */

package org.objectweb.jac.aspects.gui;

/**
 * Represents the content of a panel.
 */
public class PanelContent { 
   String type;
   String[] args;

   /**
    * Creates a new PanelContent
    * @param type the type of the view of panel
    * @param args arguments of the view
    */
   public PanelContent(String type, String[] args) {
      this.args = args;
      this.type = type;
   }

   /**
    * Gets the arguments of the view
    * @return the arguments of the view
    */
   public String[] getArgs() {
      return args;
   }

   /**
    * Gets the type of the view
    * @return the type of the view
    */
   public String getType() {
      return type;
   }
}
