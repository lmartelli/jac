/*
  Copyright (C) 2001-2003 Renaud Pawlak <renaud@aopsys.com>

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 2 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  USA */

package org.objectweb.jac.core;

import gnu.regexp.RE;
import java.util.Collection;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.objectweb.jac.util.*;

/**
 * Provides a naming repository within a running JAC system.
 *
 * <p>All the JAC objects are seamlessly registered by
 * <code>NamingAC</code> when they are instantiated.
 *
 * @see org.objectweb.jac.aspects.naming.NamingAC */

public class NameRepository extends WeakRepository {
    static Logger logger = Logger.getLogger("repository");

    /**
     * Get the sole instance of name repository.
     * 
     * @return the name repository */
   
    public static Repository get() {
        if (nameRepository == null) 
            return new NameRepository();
        return nameRepository;
    }
   
    /**
     * Store the sole instance of name repository. */
    protected static NameRepository nameRepository = null;

    /**
     * The default constructor will set the nameRepository field to the
     * right value. */

    public NameRepository() {
        nameRepository = this;
        register("JAC_name_repository", this);
        // HACK!
        /*
          if (Wrapping.acm != null ) {
          register ( "JAC_ac_manager", Wrapping.acm );
          } else {
          register ( "JAC_ac_manager", ACManager.get() );
          }
        */
        register("JAC_ac_manager",ACManager.get());
        register("JAC_application_repository", ApplicationRepository.get());
    }

    public Object getObject(String logicalName) {
        if (logicalName == null) 
            return null;
        Object ret = objects.get(logicalName);
        if (ret == null) {
            ((ACManager)ACManager.get()).whenObjectMiss(logicalName);
            ret = Collaboration.get().getAttribute(BaseProgramListener.FOUND_OBJECT);
        }
        logger.debug("getObject("+logicalName+") -> "+
                     (ret==null?"null":ret.getClass().getName()));
        return ret;
    }

    /**
     * Gets the set of JAC objects whose names match an expression.
     *
     * @param expr a regular expression
     * @return the objects set 
     */
    public static Collection getObjects(String expr) {
        NameRepository nr = (NameRepository)get();
        String[] names = nr.getNames();
        Collection res = new Vector();
        RE re;
        try {
            re = new RE(expr);
        } catch (Exception e) {
            logger.error("Bad regular expression '"+expr+"'",e);
            return null;
        }
        for (int i=0; i<names.length; i++) {
            String name = names[i];
            if (name==null) continue;
            if (re.isMatch(name)) {
                res.add(nr.getObject(name));
            }
        }
        logger.debug("getObjects("+expr+") -> "+res);
        return res;
    }


    public static Collection getObjects(String[] exprs) {
        return getObjects(Strings.join(exprs,"|"));
    }

    public static Collection getObjects(Collection exprs) {
        return getObjects(Strings.join(exprs,"|"));
    }
}
