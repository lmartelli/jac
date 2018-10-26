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

import org.objectweb.jac.core.AspectComponent;
import org.objectweb.jac.core.Interaction;
import org.objectweb.jac.core.NameRepository;
import org.objectweb.jac.core.Wrapper;
import org.objectweb.jac.util.Repository;

import org.aopalliance.intercept.ConstructorInvocation;
import org.aopalliance.intercept.MethodInvocation;

/**
 * This class define a wrapping method (read) for wrappees that perform
 * read operations on transactional ressources.
 * This wrapper may wrap several wrappees from different classes.
 * Each wrappee field is mapped onto a SQL table attribute.
 * The SQL tables contain one more attribute which is the name of the wrappee,
 * and which is also the primary key of the table.
 * 
 * @author Lionel Seinturier <Lionel.Seinturier@lip6.fr>
 * @version 1.0
 */
public class ReadWrapper extends Wrapper {

    /**
     * The instance implementing the technical API for persistence.
     * SimpleDbPersistence implements it.
     */
    private PersistenceItf storage;
    
    /**
     * The data source used to open a connection towards the database
     * where the data is stored.
     */
    private StandardXADataSource ds;
    
    /**
     * @param ac       the AC managing this wrapper
     * @param storage  the technical instance for persistence
     * @param ds       the data source used to create a connection towards
     *                 the database where the data is stored
     */
	public ReadWrapper(
        AspectComponent ac, PersistenceItf storage, StandardXADataSource ds ) {
		
        super(ac);
        this.storage = storage;
        this.ds = ds;
	}

    /**
     * Wrapping method for wrappees that perform
     * write operations on transactional ressources.
     * After proceeding the interaction, fields value are saved
     * into the database.
     * Fetching (ie reading) the data before, is a way to let the database
     * manage the blocking mechanism whenever concurrent transactions occur.
     */
	public Object invoke(MethodInvocation invocation) throws Throwable {
        
        Interaction interaction = (Interaction) invocation;

        Object wrappee = interaction.wrappee;
        String wrappeeName = names.getName(wrappee);        
        
        try {
			storage.load(wrappee,wrappeeName,ds);
		} catch (Exception e) {
			e.printStackTrace();
            System.exit(1);
		}
        
        return proceed(interaction);
    }
        
    
    /**
     * The reference towards the JAC name repository.
     * Needed by applyPersistence().
     */
    private Repository names = NameRepository.get();

    public Object construct(ConstructorInvocation invocation)
        throws Throwable {
        throw new Exception("This wrapper does not support constructor wrapping");
    }    
}
