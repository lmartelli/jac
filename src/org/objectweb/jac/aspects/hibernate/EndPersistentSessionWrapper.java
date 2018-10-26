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

package org.objectweb.jac.aspects.hibernate;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Transaction;

import org.aopalliance.intercept.ConstructorInvocation;
import org.aopalliance.intercept.MethodInvocation;
import org.objectweb.jac.core.AspectComponent;
import org.objectweb.jac.core.Interaction;
import org.objectweb.jac.core.Wrapper;

/**
 * This wrapper delimits the end of a persistent session.
 * 
 * @author Lionel Seinturier <Lionel.Seinturier@lip6.fr>
 * @version 1.0
 */
public class EndPersistentSessionWrapper extends Wrapper {

    /** The gateway instance to Hibernate. */
    private HibernateHelper hh = HibernateHelper.get();
    
    public EndPersistentSessionWrapper( AspectComponent ac ) {        
        super(ac);
    }
    
    /**
     * Wrapping method around pointcuts
     * where a persistent session ends.
     */
    public Object invoke( MethodInvocation invocation ) {
        Interaction interaction = (Interaction) invocation;
        
        Object ret = proceed(interaction);
        
        try {
            Transaction tx = hh.getTx();
			tx.commit();
            return ret;
		} catch (HibernateException e1) {
			e1.printStackTrace();
			System.exit(1);
		}
        return null;
    }

    public Object construct(ConstructorInvocation invocation)
        throws Throwable {
        throw new Exception("This wrapper does not support constructor wrapping");
    }
}
