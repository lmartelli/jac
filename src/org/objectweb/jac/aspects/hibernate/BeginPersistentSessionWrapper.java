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

import java.util.Iterator;
import java.util.List;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.ObjectNotFoundException;
import net.sf.hibernate.Session;

import org.aopalliance.intercept.ConstructorInvocation;
import org.aopalliance.intercept.MethodInvocation;
import org.objectweb.jac.core.AspectComponent;
import org.objectweb.jac.core.Interaction;
import org.objectweb.jac.core.NameRepository;
import org.objectweb.jac.core.Wrapper;
import org.objectweb.jac.util.Repository;

/**
 * This wrapper delimits the begining of a persistent session.
 * 
 * @author Lionel Seinturier <Lionel.Seinturier@lip6.fr>
 * @version 1.0
 */
public class BeginPersistentSessionWrapper extends Wrapper {

    /** The gateway instance to Hibernate. */
    private HibernateHelper hh = HibernateHelper.get();
    
    public BeginPersistentSessionWrapper( AspectComponent ac ) {
        super(ac);
    }
    
    /**
     * Wrapping method around pointcuts
     * where a persistent session begins.
     */
    public Object invoke( MethodInvocation invocation ) {
        Interaction interaction = (Interaction) invocation;
        try {
			return _begin(interaction);
		} catch (HibernateException e) {
			e.printStackTrace();
			System.exit(1);
		}
        return null;
    }
    private Object _begin( Interaction interaction ) throws HibernateException {
        
        hh.openSessionAndBeginTx();
        Session session = hh.getSession();
            
        /**
         * Load all persistent objects.
         * If they do not exist yet in the storage,
         * create an entry for them (save).
         */
        HibernateAC hac = (HibernateAC) ac;
        List pObjects = hac.getPersistentObjects();
        for ( Iterator iter=pObjects.iterator() ; iter.hasNext() ; ) {
            
            String name = (String) iter.next();
            Object object = names.getObject(name);
            
            try {
				session.load(object,name);
            } catch (ObjectNotFoundException e) {
                session.save(object,name);
			}
		}            
                
        /**
         * Proceed the interaction.
         */
        return proceed(interaction);
    }
    

    /**
     * The reference towards the JAC name repository.
     */
    private Repository names = NameRepository.get();

    public Object construct(ConstructorInvocation invocation)
        throws Throwable {
        throw new Exception("This wrapper does not support constructor wrapping");
    }
}
