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

package org.objectweb.jac.aspects.gui.web;


/**
 * This interface represents an HttpRequest.
 */
public interface JacRequest {
    /**
     * Tells wether there is a parameter with a given name
     * @param name the parameter's name whose presence to test
     */
    boolean contains(String name);

    /**
     * Returns a parameter. The result can be a String or a FileParameter object.
     * @param name the name of the parameter
     * @return the value of the parameter (a String a or FileParameter)
     * if the parameter exists in the request, null otherwise.
     */
    Object getParameter(String name);

    /**
     * Returns a parameter. The result can be a String or a FileParameter object.
     * @param name the name of the parameter
     * @return the value of the parameter (a String a or FileParameter)
     * if the parameter exists in the request, null otherwise.
     */
    Object[] getParameters(String name);

    /**
     * Tells if the user agent of the request is Internet Explorer
     *
     * @return true if the user agent is Internet Explorer, false otherwise
     * @see #userAgentMatch(String)
     */
    boolean isIEUserAgent();

    /**
     * Tells if the user agent contains a given string
     *
     * @param s string to be searched in user agent
     * @return true if the user agent is Internet Explorer, false otherwise
     */
    boolean userAgentMatch(String s);

    /**
     * Returns the user agent of this request
     * @return the user agent of this request
     */
    String getUserAgent();
   
    /**
     * Gets the value of a header.
     *
     * @param name name of the header
     * @return the value of the header
     */
    String getHeader(String name);

    /**
     * Makes the current requesting thread block and wait until the
     * response is available.
     *
     * <p>The thread that call this method waits until a call to
     * <code>setResponse</code> occurs or a timeout occurs.
     *
     * @return false if a timeout occured, true otherwise.
     *
     * @see #setResponse() 
     */
    boolean waitForResponse();

    /**
     * Unblock a thread that was blocked by a <code>waitForResult</code> call.
     *
     * @see #waitForResponse() 
     */
    void setResponse();

}
