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
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA */

package org.objectweb.jac.aspects.gui.web;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.objectweb.jac.util.Semaphore;

/**
 * This class represents a multi-part HttpRequest.
 */
public abstract class AbstractJacRequest implements JacRequest {
    static Logger logger = Logger.getLogger("web.session");

    Map headers = new Hashtable();
    public AbstractJacRequest(HttpServletRequest servletRequest) {
        Enumeration headerNames = servletRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = (String)headerNames.nextElement();
            headers.put(name,servletRequest.getHeader(name));
        }
    }
    public abstract Object getParameter(String name);
    public boolean isIEUserAgent() {
        String userAgent = getUserAgent();
        if (userAgent!=null && userAgent.indexOf("MSIE")!=-1) {
            return true;
        } else {
            return false;
        }
    }
    public String getUserAgent() {
        return getHeader("User-Agent");
    }
    public boolean userAgentMatch(String s) {
        String userAgent = getHeader("User-Agent");
        if (userAgent!=null && userAgent.indexOf(s)!=-1) {
            return true;
        } else {
            return false;
        }        
    }
    public String getHeader(String name) {
        return (String)headers.get(name);
    }

    JacRequest parent;
    public void setParent(JacRequest parent) {
        this.parent = parent;
    }

    /** The semaphore that blocks the requesting thread until the
        response is available. */
    transient protected Semaphore semaphore = new Semaphore();

    protected static final long DEFAULT_REQUEST_TIMEOUT = 1000*60*30; // 30 minutes
   
    public boolean waitForResponse() {
        logger.debug("wait for response " + this + " (" + semaphore.getCount()+")");
        boolean result = semaphore.acquire(DEFAULT_REQUEST_TIMEOUT);
        if (result) {
            logger.debug("got response " + this + " (" + semaphore.getCount()+")");
            if (semaphore.getCount()>0) {
                logger.warn("Session "+this+": semaphore > 0 ("+semaphore.getCount()+")");
            }
        } else {
            logger.debug("timeout "+this);         
        }
        return result;
    }

    public void setResponse() {
        logger.debug("set response " + this + " (" + semaphore.getCount()+")");
        semaphore.release();
    }

}
