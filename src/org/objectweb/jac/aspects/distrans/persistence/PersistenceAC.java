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

import org.objectweb.jac.aspects.distrans.JOTMHelper;
import org.objectweb.jac.core.AspectComponent;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.transaction.TransactionManager;

import org.enhydra.jdbc.standard.StandardXADataSource;

/**
 * Transaction-enabled persistence storage.
 * An instance this AC is mandatory with DisTransAC.
 * This class delegates most of the work (apart from data sources
 * registering) to a technical implementation of the persistence API
 * PersistenceItf.
 * Current implementations of this API: SimpleDbPersistence.
 *  
 * Relies on jac.aspects.distrans.JOTMHelper
 * to retrieve the JOTM instance used by JAC.
 * 
 * @author Lionel Seinturier <Lionel.Seinturier@lip6.fr>
 * @version 1.0
 */
public class PersistenceAC extends AspectComponent {
    
    public PersistenceAC() {}

    /** A map storing data sources. */
    private Map sources = new HashMap();
    
    /**
     * Define a data source name that will be later on used by the remaining
     * configuration methods of this AC.
     * 
     * @param sourceName  the data source name
     * @param driver    the JDBC driver name (eg org.postgresql.Driver)
     * @param url       the JDBC URL (eg jdbc:postgresql://localhost/test)
     * @param user      the login to use
     * @param password  the password to use
     */
    public void defineDataSource(
        String sourceName,
        String driver, String url, String user, String password ) {
            
        StandardXADataSource xads = new StandardXADataSource();
        try {
			xads.setDriverName(driver);
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
        xads.setUrl(url);
        xads.setUser(user);
        xads.setPassword(password);
        TransactionManager tm = JOTMHelper.get().getTransactionManager();
        xads.setTransactionManager(tm);
        
        sources.put( sourceName, xads );
    }
    
    
    /**
     * The instance implementing the technical API for persistence.
     * SimpleDbPersistence implements it.
     */
    private PersistenceItf storage;
    
    public void setStorageType( String classname ) {
        try {
			_setStorageType(classname);
            return;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
        System.exit(1);
    }
    
    private void _setStorageType( String classname )
        throws ClassNotFoundException, InstantiationException, IllegalAccessException {

        Class storageClass = Class.forName(classname);
        if (!PersistenceItf.class.isAssignableFrom(storageClass))
            throw new RuntimeException(classname+" must implement jac.aspects.distrans.persistence.PersistenceItf");
        
        storage = (PersistenceItf) storageClass.newInstance();
    }
    
    
    /**
     * Initialize the persistence storage.
     * If the storage already exists, do not reinitialize it. 
     * 
     * @param className   the class name for which we want to create a storage
     * @param sourceName  the data source name
     */
    public void initStorageIfNeeded( String className, String sourceName ) {

        if ( storage == null )
            throw new RuntimeException("setStorageType() must be called first");
        
        StandardXADataSource ds = (StandardXADataSource) sources.get(sourceName);
        if ( ds == null )
            throw new RuntimeException("Unknown data source "+sourceName);

        storage.initStorageIfNeeded(className,ds);    
    }
    
    /**
     * Initialize the persistence storage.
     * If the storage already exists, reinitialize it. 
     * 
     * @param className   the class name for which we want to create a storage
     * @param sourceName  the data source name
     */
    public void initStorage( String className, String sourceName ) {

        if ( storage == null )
            throw new RuntimeException("setStorageType() must be called first");
        
        StandardXADataSource ds = (StandardXADataSource) sources.get(sourceName);
        if ( ds == null )
            throw new RuntimeException("Unknown data source "+sourceName);
    
        storage.initStorage(className,ds);    
    }

    /**
     * All objects matching the objectNameExpression
     * are made persistent to a SQL database represented by the data source.
     * These objects are ressources that will potentially be used
     * later on in transactions.
     * 
     * Even if the objectNameExpression can be any regular expression,
     * it is assumed to designate instances storable in existing
     * storages (eventually call initStorageIfNeeded before).
     * 
     * @param objectNameExpression  the object name expression
     * @param sourceName            the source name
     */
    public void registerPersistentRessource(
        String objectNameExpression, String sourceName ) {
               
        if ( storage == null )
            throw new RuntimeException("setStorageType() must be called first");
        
        StandardXADataSource ds = (StandardXADataSource) sources.get(sourceName);
        if ( ds == null )
            throw new RuntimeException("Unknown data source "+sourceName);
        
        /**
         * Wrap methods that perform write operations.
         * Modifier methods in all the classes
         * for all the objects matching objectNameExpression.
         */
        WriteWrapper pw = new WriteWrapper(this,storage,ds);         
        pointcut(
            objectNameExpression, "ALL", "MODIFIERS",
            pw, null
        );
        
        /**
         * Wrap methods that perform read operations.
         * Accessor and modifier methods in all the classes
         * for all the objects matching objectNameExpression.
         * Note: modifiers read fields before modifying them.
         */
        ReadWrapper rw = new ReadWrapper(this,storage,ds);      
        pointcut(
            objectNameExpression, "ALL", "MODIFIERS || ACCESSORS",
            rw, null
        );
    }
    
}
