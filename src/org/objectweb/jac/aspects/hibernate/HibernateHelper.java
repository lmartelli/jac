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
import net.sf.hibernate.MappingException;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.cfg.Configuration;
import net.sf.hibernate.tool.hbm2ddl.SchemaExport;

/**
 * This class acts as a gateway between the AC HibernateAC
 * and the Hibernate 2.0 framework.
 * This class manages a singleton instance of itself.
 * 
 * @author Lionel Seinturier <Lionel.Seinturier@lip6.fr>
 * @version 1.0
 */
public class HibernateHelper {
    
    private static HibernateHelper singleton = new HibernateHelper();
    
    /**
     * @return  the singleton instance of this class
     */
    public static HibernateHelper get() {
        return singleton;
    }

    /**
     * The Hibernate configuration.
     * "Side effect" of creating this object:
     * the ressource /hibernate.properties is loaded.
     */
    private Configuration cfg = new Configuration();
    
    /**
     * Add a class to the Hibernate configuration.
     * Given a class named apackage.ClassA,
     * this triggers the loading of the property /apackage/ClassA..hbm.xml
     */
    public void addClass( Class cl ) throws MappingException {
        cfg.addClass(cl);
        rebuildsf = true;
    }
    
    /**
     * Export to the database the table schema for persistent classes.
     */
    public void schemaExport() throws HibernateException {
        new SchemaExport(cfg).create(false,true);
    }
    
    /**
     * Flag to know whether the configuration has changed,
     * and if a new SessionFactory has to be constructed.
     */
    private boolean rebuildsf = true;
    private SessionFactory sf;
    
    /**
     * @return  the session factory associated to the Hibernate configuration
     */
    private SessionFactory getSessionFactory() throws HibernateException {
        if (rebuildsf) {
            sf = cfg.buildSessionFactory();
            rebuildsf = false;
        }
        return sf;
    }
    
    /**
     * Open an Hibernate session, and start a transaction.
     * Users call session.save() or session.load() any number of times,
     * commit or rollback the transaction,
     * and close the session.
     */
    public void openSessionAndBeginTx() throws HibernateException {
        SessionFactory sf = getSessionFactory();
        session = sf.openSession();
        tx = session.beginTransaction();
    }
    
    /** The current Hibernate session for saving persistent data. */
    private Session session;
    public Session getSession() {
        if ( session == null || !session.isOpen() )
            throw new RuntimeException("openSessionAndBeginTx() should have been called first");
        return session;
    }
    
    /** The current Hibernate transaction for saving persistent data. */
    private Transaction tx;
    public Transaction getTx() throws HibernateException {
        if ( tx == null || tx.wasCommitted() || tx.wasRolledBack() )
            throw new RuntimeException("openSessionAndBeginTx() should have been called first");
        return tx;
    }
}
