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

import java.util.ArrayList;
import java.util.List;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.MappingException;

import org.objectweb.jac.core.AspectComponent;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.ClassRepository;

/**
 * Persistence AC relying on Hibernate.
 *
 * @author Lionel Seinturier <Lionel.Seinturier@lip6.fr>
 * @version 1.0
 */
public class HibernateAC extends AspectComponent {
    public HibernateAC() {}

    /** The gateway instance to Hibernate. */
    private HibernateHelper hh = HibernateHelper.get();
        
    /**
     * Declare a new persistent class.
     * 
     * @param className  the persistant class name
     */
    public void registerPersistentClass( String className ) {
        
        ClassItem cli = cr.getClass(className);
        Class cl = cli.getActualClass();
        
        try {
			hh.addClass(cl);
		} catch (MappingException e) {
			e.printStackTrace();
			System.exit(1);
		}
    }
    
    private ClassRepository cr = ClassRepository.get();
    
    
    /**
     * Create tables to hold data for persistent classes.
     */
    public void initStorage() {
        try {
			hh.schemaExport();
		} catch (HibernateException e) {
			e.printStackTrace();
			System.exit(1);
		}        
    }

    
    /** The list of object names declare to be persistent. */
    private List persistentObjects = new ArrayList();
    List getPersistentObjects() { return persistentObjects; }
    
    /**
     * All objects matching the objectNameExpression
     * are made persistent with Hibernate.
     * 
     * Even if the objectNameExpression can be any regular expression,
     * it is assumed to designate instances storable in existing
     * storages (eventually call initStorage before).
     * 
     * @param objectNameExpression  the object name expression
     */
    public void registerPersistentObject( String objectNameExpression ) {        
        persistentObjects.add(objectNameExpression);
    }


	/**
	 * Delimit a persistent session with Hibernate.
	 * The session will begin before the method designated by the pointcut
	 * designated by the 3 first parameter, and will end after the pointcut
	 * designated by the 3 last ones.
	 * 
	 * @param sessionid  the session identifier
	 * @param beginCNE   begin class name expression
	 * @param beginONE   begin object name expression
	 * @param beginMNE   begin method name expression
	 * @param endCNE     end class name expression
	 * @param endONE     end object name expression
	 * @param endMNE     end method name expression
	 */
	public void delimitPersistentSession(
	    String sessionid, 
	    String beginCNE, String beginONE, String beginMNE,
	    String endCNE, String endONE, String endMNE ) {
	        
	    BeginPersistentSessionWrapper beginwrapper =
	        new BeginPersistentSessionWrapper(this);
	    
	    EndPersistentSessionWrapper endwrapper =
            new EndPersistentSessionWrapper(this);
            
	    pointcut( beginONE, beginCNE, beginMNE, beginwrapper, null );
	    pointcut( endONE, endCNE, endMNE, endwrapper, null );
	}
    
}
