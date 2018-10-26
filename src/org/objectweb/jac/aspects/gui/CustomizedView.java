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
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA */

package org.objectweb.jac.aspects.gui;

public interface CustomizedView extends CompositeView {
   /**
    * Returns the customized GUI, i.e. the object that contains all
    * the parametrization of this view as built by the GUI aspect.
    *
    * @return an implementation-independant object */
   CustomizedGUI getCustomizedGUI();

   /**
    * Set a menu bar to the customized.
    *
    * @param menuBar the abstract menu bar
    * @param position (TOP||BOTTOM) 
    */
   void setMenuBar(MenuView menuBar,String position);

   /**
    * Set a tool bar to the customized.
    *
    * @param toolBar the abstract tool bar
    */
   void setToolBar(MenuView toolBar);

   /**
    * Set a status bar to the customized.
    *
    * @param view the abstract status bar
    * @param position (TOP||BOTTOM) 
    */
   void setStatusBar(StatusView view,String position);

   /**
    * Show a message on the customized's status bar.
    *
    * @param message the message to show 
    */
   void showStatus(String message);

   /**
    * Returns the content pane of the customized, i.e. a panel
    * containing some sub-panels.
    *
    * @return the view of subpanes 
    */
   PanelView getPanelView();

   void requestFocus();
}
