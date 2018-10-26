/*
  Copyright (C) 2001-2003 Renaud Pawlak <renaud@aopsys.com>

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

package org.objectweb.jac.aspects.session;

import java.util.Arrays;
import java.util.HashSet;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.*;
import org.objectweb.jac.util.*;

/**
 * This aspect component handle the session aspect within JAC
 * applications.
 *
 * <p>The session aspects memorizes some contextual informations as
 * users id or password and link them to the current session id (the
 * "Session.sid" attribute in the context). If the client correctly
 * sets this attribute for each interaction, then the session aspect
 * restores the saved information so that the user will not have to
 * input extra information (such as his password) for each
 * interaction.

 * @see org.objectweb.jac.aspects.session.SessionWrapper
 *
 * @author Renaud Pawlak */

public class SessionAC extends AspectComponent implements SessionConf {
    static Logger logger = Logger.getLogger("session");

    public static final String SESSION_ID = "Session.sid";
    public static final String INITIALIZED = "Session.initialized";

    static {
        Collaboration.setGlobal(SESSION_ID);
        Collaboration.setGlobal(INITIALIZED);
    }

    public String[] getDefaultConfigs() {
        return new String[] {"org/objectweb/jac/aspects/session/session.acc"};
    }

    SessionWrapper wrapper;

    public void clearCurrentSessionAttribute(String name) {
        wrapper.clearCurrentSessionAttribute(name);
    }

    protected SessionWrapper getWrapper() {
        // We must wait until the AC is registered, because the wrapper
        // stores the AC's name (which will be NULL is the AC is not registered)
        if (wrapper==null)
            wrapper = new SessionWrapper(this);
        return wrapper;
    }

    public void defineSessionHandlers(String classExpr, 
                                      String methodExpr, 
                                      String objectExpr) {
        pointcut(objectExpr, classExpr, methodExpr, 
                 getWrapper(), null);
    }

    public void definePerSessionObjects(String classExpr, 
                                        String objectExpr) {
        logger.debug("defining per-session objects: "+
                     classExpr+","+objectExpr );
        pointcut(objectExpr, classExpr, "ALL", 
                 PerSessionObjectWrapper.class.getName(), 
                 null, false);
    }

    HashSet storedAttributes = new HashSet();

    public void declareStoredAttributes(String attributes[]) {
        storedAttributes.addAll(Arrays.asList(attributes));
    }

    /**
     * Stored attributes accessor.
     *
     * @return the stored attributes
     */
    public String[] getStoredAttributes() {
        return (String[])storedAttributes.toArray(new String[] {});
    }

}
