/*
  Copyright (C) 2002 Renaud Pawlak <renaud@aopsys.com>

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 2 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA */

package org.objectweb.jac.aspects.gui.swing;

import javax.swing.JLabel;
import org.objectweb.jac.aspects.gui.*;
import org.objectweb.jac.core.rtti.MethodItem;

public class StatusBar extends AbstractView implements StatusView {
   
   MethodItem method;
   JLabel msgLabel;

   public StatusBar(MethodItem method) {
      this.method=method;
      msgLabel=new JLabel();
      add(msgLabel);
      msgLabel.setText("Welcome");
   }

   // StatusView interface

   String position;
   
   /**
    * Get the value of position.
    * @return value of position.
    */
   public String getPosition() {
      return position;
   }
   
   /**
    * Set the value of position.
    * @param v  Value to assign to position.
    */
   public void setPosition(String  v) {
      this.position = v;
   }

   public void showMessage(String message) {
      msgLabel.setText(message);
   }


}
