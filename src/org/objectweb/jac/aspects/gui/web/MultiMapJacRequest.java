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

import java.util.Iterator;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.mortbay.util.MultiMap;

/**
 * This class represents a multi-part HttpRequest.
 */
public class MultiMapJacRequest extends AbstractJacRequest implements JacRequest {
    MultiMap parameters;

    /**
     * Parameters are initialized from the given map.
     */
    public MultiMapJacRequest(MultiMap map, HttpServletRequest servletRequest) {
        super(servletRequest);
        this.parameters = map;
        readParamsFromRequest(servletRequest);
    }

    public MultiMapJacRequest(MultiMap map, HttpServletRequest servletRequest,
                              JacRequest parent) {
        this(map,servletRequest);
        this.parent = parent;
    }

    /**
     * Parameters are copied from the HttpServletRequest.
     */
    public MultiMapJacRequest(HttpServletRequest servletRequest) {
        super(servletRequest);
        parameters = new MultiMap();
        readParamsFromRequest(servletRequest);
    }

    protected void readParamsFromRequest(HttpServletRequest servletRequest) {
        // We must copy the parameters because HttpServletRequest
        // objects are recycled by Jetty
        Map servletParameters = servletRequest.getParameterMap();
        Iterator it = servletParameters.keySet().iterator();
        while(it.hasNext()) {
            String name = (String)it.next();
            String[] values = (String[])servletParameters.get(name);
            parameters.putValues(name,values);
        }
    }

    public boolean contains(String name) {
        if (parameters.containsKey(name))
            return true;
        else if (parent!=null)
            return parent.contains(name);
        else
            return false;
    }

    public Object getParameter(String name) {
        if (parameters.containsKey(name))
            return parameters.getString(name);
        else if (parent!=null)
            return parent.getParameter(name);
        else
            return null;
    }

    public Object[] getParameters(String name) {
        if (parameters.containsKey(name))
            return parameters.getValues(name).toArray(new String [0]);
        else if (parent!=null)
            return parent.getParameters(name);
        else
            return null;
    }

    public String toString() {
        return parameters.toString();
    }
    
}
