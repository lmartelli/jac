/*
  Copyright (C) 2001-2002 Laurent Martelli, Renaud Pawlak

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

import org.objectweb.jac.aspects.gui.View;

/**
 * This class defines requests that come from web clients.
 *
 * <p>When a request is performed to a <code>AbstractServer</code>,
 * a new instance of this class is created and is pushed on the
 * request stack of the current session.
 */

public class Request implements java.io.Serializable {

   /* the view for this request (org.objectweb.jac.aspects.gui.View) */
   View view;

   /**
    * A constuctor that initializes the request with all the needed
    * information.
    *
    * @param view
    */

   public Request(View view) {
      this.view = view;
   }

   /**
    * Returns the view that is involved by this request.
    *
    * @return the view that is involved by the request */

   public Object getView() {
      return view;
   }

   /**
    * Returns a printable string representation of the current
    * request.
    *
    * @return a printable string */ 

   public String toString() {
      return "Request:"+view;
   }
}
