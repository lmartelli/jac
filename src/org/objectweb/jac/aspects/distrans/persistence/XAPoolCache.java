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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.enhydra.jdbc.pool.StandardXAPoolDataSource;
import org.enhydra.jdbc.standard.StandardXADataSource;

/**
 * This class implements a cache of connections
 * towards multiple XADataSource.
 * Contrary to a simple pool that manages multiple connections
 * towards a single XADataSource, this cache manages connections
 * for each XADataSource registered in the cache.
 * Hence, this is a pool of pools.
 * 
 * @author Lionel Seinturier <Lionel.Seinturier@lip6.fr>
 * @version 1.0
 */
public class XAPoolCache {

    /**
     * Cache of connections:
     * - keys are StandardXADataSource objects
     * - values are Connection objects
     */
    private static Map pools = new HashMap();
    
    /** Size of each pool. */
    private final static int POOL_SIZE = 4;
    
    
    /**
     * Get a connection for a XADataSource.
     * Either return the reference towards an already
     * existing connection, or create a new one.
     * 
     * @param ds  the XADataSource instance
     * @return    a SQL connection
     */
    public static Connection getConnection( StandardXADataSource ds )
        throws SQLException {

        /** Check whether the connection is pooled and still open. */
        Connection connection = (Connection) pools.get(ds);
        if ( connection!= null && !connection.isClosed() ) {
            return connection;
        }
        
        StandardXAPoolDataSource pool =
            new StandardXAPoolDataSource(POOL_SIZE);
        pool.setUser( ds.getUser() );
        pool.setPassword( ds.getPassword() );
        pool.setTransactionManager( ds.getTransactionManager() );
        pool.setDataSource(ds);
        
        connection = pool.getConnection();
        pools.put(ds,connection);

        return connection;
    }
    
}
