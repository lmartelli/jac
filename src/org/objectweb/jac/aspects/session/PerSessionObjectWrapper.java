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

package org.objectweb.jac.aspects.session;

import java.util.*;
import org.aopalliance.intercept.ConstructorInvocation;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.*;
import org.objectweb.jac.util.*;

/**
 * This wrapper handles per-session objects within the JAC system.
 *
 * <p>For each session, it uses a different copy of the original
 * object so that each client see a different state.
 *
 * @see #handlePerSessionObject(Interaction)
 * @author Renaud Pawlak */

public class PerSessionObjectWrapper extends Wrapper {
    static Logger logger = Logger.getLogger("session");

    Hashtable sessionObjects = new Hashtable(); 

    public PerSessionObjectWrapper(AspectComponent ac) {
        super(ac);
    }

    /**
     * This wrapping method handles an hashtable of copied objects.
     *
     * <p>There is one copied object per session with possibly a
     * different state from the original. The call is thus forwarded to
     * the copy that corresponds to the session.
     *
     * <p>The first time, the original object is cloned so that its
     * state is that same as the original.
     *
     * @return the result given by the per-session copy */

    public Object handlePerSessionObject(Interaction interaction) {

        logger.debug("handling per-session object for " + interaction.wrappee);

        String sid = (String) attr( "Session.sid" );
      
        if ( sid == null || sid.startsWith("Swing") ) {
            logger.debug("session is not defined by client");
            return proceed(interaction);
        } 

        logger.debug("found session " + sid);
      
        Object sessionObject = null;
      
        if ( sessionObjects.containsKey( sid ) ) {
            sessionObject = sessionObjects.get( sid );
        } else {
            // the initial session object has the same state as the original
            // object
            logger.debug("cloning object " + interaction.wrappee);
            sessionObject = Wrapping.clone(interaction.wrappee);
            sessionObjects.put(sid,sessionObject);
        }

        Object result = null;
        if( sessionObject == interaction.wrappee ) {
            // we are the session object, this should not happend if
            // the session aspect is correctly defined
            logger.warn(interaction.wrappee+
                        " is a session object and is wrapped.");
            result = proceed(interaction);
        } else {
            // we forward the call to the session object 
            // (and we do not use the original)
            logger.debug(interaction.wrappee+" forwarding to session object " + 
                         sessionObject+"."+interaction.method);
            result = interaction.invoke(sessionObject);
        }
        return result;
    }

    public Object invoke(MethodInvocation invocation) throws Throwable {
        return handlePerSessionObject((Interaction) invocation);
    }

    public Object construct(ConstructorInvocation invocation)
        throws Throwable {
        throw new Exception("Wrapper "+this+" does not support construction interception.");
    }

}
