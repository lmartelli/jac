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

package org.objectweb.jac.aspects.gui.web;

import java.io.IOException;
import java.io.PrintWriter;


/**
 * This interface defines an object viewer used by the WebGUI. It has
 * a method to generate HTML code.
 *
 * @see HTMLEditor
 */
public interface HTMLViewer {
   /**
    * Generate the HTML code for the editor.
    *
    * @param out the writer where to write the HTML code.
    */

   void genHTML(PrintWriter out) throws IOException;

   /**
    * Set the CSS stylesheet 
    */
   void setStyleSheet(String styelSheet);
}
