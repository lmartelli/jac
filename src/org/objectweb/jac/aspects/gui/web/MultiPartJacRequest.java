/*
  Copyright (C) 2001-2003 Laurent Martelli <laurent@aopsys.com>

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

import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;

/**
 * This class represents a multi-part HttpRequest.
 */
public class MultiPartJacRequest extends AbstractJacRequest implements JacRequest {
    static Logger logger = Logger.getLogger("web.servlet");

    MultiPartRequest request;
    public MultiPartJacRequest(MultiPartRequest request, 
                               HttpServletRequest servletRequest) {
        super(servletRequest);
        this.request = request;
    }

    public boolean contains(String name) {
        return request.contains(name);
    }

    public Object getParameter(String name) {
        if (request.getFilename(name)!=null) {
            logger.debug("Part "+name+": "+request.getParams(name));
            return new RequestPart(name,request.getFilename(name),
                                   request.getInputStream(name), 
                                   request.getParams(name));
        } else {
            logger.debug("Part "+name+": "+request.getParams(name)+
                         " -> "+request.getString(name));
            return request.getString(name);
        }
    }

    public Object[] getParameters(String name) {
        if (request.getFilename(name)!=null) {
            //  *** TODO!!!
            return null;
        } else {
            logger.debug("Part "+name+": "+request.getParams(name)+
                         " -> "+request.getStrings(name));
            return request.getStrings(name);
        }
    }

    public String toString() {
        return request.toString();
    }
}
