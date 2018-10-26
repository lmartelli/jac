/*
  Copyright (C) 2001-2002 Renaud Pawlak, Laurent Martelli
  
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

package org.objectweb.jac.aspects.gui.swing;

import java.awt.Component;
import javax.swing.BoxLayout;
import org.apache.log4j.Logger;
import org.objectweb.jac.aspects.gui.Constants;

public class SwingContainerView extends AbstractCompositeView {
    static Logger loggerFocus = Logger.getLogger("gui.focus");
   
    int geometry;

    public SwingContainerView(int geometry) {
        super();
        this.geometry=geometry;
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setLayout(
            new BoxLayout(
                this,
                geometry==Constants.VERTICAL ? 
                BoxLayout.Y_AXIS : BoxLayout.X_AXIS));
    }

}
