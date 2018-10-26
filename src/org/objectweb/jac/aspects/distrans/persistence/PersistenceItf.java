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

package org.objectweb.jac.aspects.distrans.persistence;

import org.enhydra.jdbc.standard.StandardXADataSource;

/**
 * @author Lionel Seinturier <Lionel.Seinturier@lip6.fr>
 * @version 1.0
 */
public interface PersistenceItf {

    /**
     * Initialize the persistence storage.
     * If the storage already exists, reinitialize it. 
     * 
     * @param className   the class name for which we want to create a storage
     * @param ds          the data source
     */
    public void initStorage( String className, StandardXADataSource ds );
    
    /**
     * Initialize the persistence storage.
     * If the storage already exists, do not reinitialize it. 
     * 
     * @param className   the class name for which we want to create a storage
     * @param ds          the data source
     */
    public void initStorageIfNeeded( String className, StandardXADataSource ds );
    
    /**
     * Store an object into the persistence storage.
     * 
     * @param wrappee  the object to store
     * @param name     the identifier for the object
     * @param ds       the data source
     */
    public void load( Object wrappee, String name, StandardXADataSource ds )
        throws Exception;

    /**
     * Update an object with the values retrieved from the persistent
     * storage.
     * 
     * @param wrappee  the object to update
     * @param name     the identifier for the object
     * @param ds       the data source
     */
    public void store( Object wrappee, String name, StandardXADataSource ds )
        throws Exception;

}
