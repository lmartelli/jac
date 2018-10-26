/*

  Copyright (C) 2001 Renaud Pawlak

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

package org.objectweb.jac.wrappers;

/**
 * Signals that something is wrong with the limits of the limiter wrapper.
 *
 * @see LimiterWrapper
 * @see LimiterWrapper#testMax(Interaction)
 * @see LimiterWrapper#testMin(Interaction)
 */

public class LimiterException extends Exception {

   /**
    * A constructor for this exception.
    *
    * @param msg the error message to print */

    public LimiterException(String msg) {
        super(msg);
    }

   /**
    * A constructor for this exception.
    *
    * @param msg the error message to print
    * @param e the exception that caused the limiter exception */

    public LimiterException(String msg, Exception e) {
        super(msg + " because of " + e.toString());
    }
}

