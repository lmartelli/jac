/*
  Copyright (C) 2001-2002 Renaud Pawlak <renaud@aopsys.com>

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

import java.util.Stack;

/**
 * This class defines a session for thin client servers.
 *
 * @see Request 
 */

public class Session implements java.io.Serializable {

   /** The requests stack for this session. */
   transient protected Stack requests = new Stack();

   /** This session's ID. */
   protected String sid;

   /**
    * The constructor for a session with a given ID. */
 
   public Session(String sid) {
      this.sid = sid;
   }

   /**
    * Returns the session's ID.
    *
    * @return the ID */

   public String getId() {
      return sid;
   }  

   /**
    * Returns the stack of the requests for this session.
    *
    * <p><code>getRequests().peek()</code> is the request that is
    * currently treated for this session.
    *
    * @return the requests stack */

   public Stack getRequests() {
      return requests;
   }

   /**
    * Returns the number of active requests on the requests stack for
    * this session.
    * 
    * @return requests stack count */

   public int getRequestCount() {
      return requests.size();
   }

   /**
    * Creates a new request for this session (pushes it on the
    * requests stack). The newly created request becomes the current
    * one of the session.
    *
    * @param request the request to push
    * @see #getCurrentRequest() 
    * @see #endCurrentRequest() */

   public void newRequest(Request request) {
      getRequests().push(request);
   }

   /**
    * Returns the current request of this session (same as
    * <code>getRequests().peek()</code>).
    *
    * @return the current request */

   public Request getCurrentRequest() {
      return (Request)getRequests().peek();
   }

   /**
    * Returns the previous request of this session (ie the one that
    * was achieved before the current one).
    *
    * @return the previous request, null if no previous request is
    * available */

   public Request getPreviousRequest() {
      Request prevRequest = null;
      if (requests.size() > 1) {
         prevRequest = (Request)requests.get(requests.size()-2);
      }
      return prevRequest;
   }

   /**
    * Ends the current request (same as
    * <code>getRequests().pop()</code>).
    *
    * @return the request that has just been ended */

   public Request endCurrentRequest() {
      return (Request)getRequests().pop();
   }   

   /**
    * Gets a humain-readable string representation of the session. 
    * @return a string */

   public String toString() {
      return "session " + sid + ", requests stack = " + requests;
   }

}
