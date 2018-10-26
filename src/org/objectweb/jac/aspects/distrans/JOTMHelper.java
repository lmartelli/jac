/*
  Copyright (C) 2001-2003 Lionel Seinturier <Lionel.Seinturier@lip6.fr>

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

package org.objectweb.jac.aspects.distrans;

import javax.naming.NamingException;

import org.objectweb.jotm.Jotm;
import org.objectweb.jotm.TraceTm;

/**
 * Helper class used to retrieve the JOTM singleton instance
 * used by JAC.
 * 
 * @author Lionel Seinturier <Lionel.Seinturier@lip6.fr>
 * @version 1.0
 */
public class JOTMHelper {

    /** The singleton instance of JOTM. */
    private static Jotm jotm;    

    public static Jotm get() {
        
        if ( jotm != null )
            return jotm;
        
        /**
         * Creates an instance of JOTM with a local transaction factory
         * which is not bound to a registry. 
         */
        try {
            jotm = new Jotm(true,false);
        } catch (NamingException ne) {
            ne.printStackTrace();
            System.exit(1);
        }
        TraceTm.configure();
        
        return jotm;
    }

}
