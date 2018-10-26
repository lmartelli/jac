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


import java.lang.reflect.InvocationTargetException;
import org.objectweb.jac.aspects.distrans.persistence.PersistenceAC;
import org.objectweb.jac.core.ACManager;
import org.objectweb.jac.core.AspectComponent;
import org.objectweb.jac.core.NameRepository;
import org.objectweb.jac.core.Wrapper;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.util.Log;
import org.objectweb.jac.util.Repository;

/**
 * This AC implements some transactional behaviors for business methods.
 * This AC relies:
 * <ul>
 * <li>on JOTM to perform distributed transactions</li>
 * <li>on a AC implementing jac.aspects.distrans.persistence.PersistenceAC
 *     to store persistent data involved into the transactions</li>
 * </ul>
 * 
 * @author Lionel Seinturier <Lionel.Seinturier@lip6.fr>
 * @version 1.0
 */
public class DisTransAC extends AspectComponent {
    
    /** The PersistenceAC associated to this aspect. */
    private PersistenceAC persistenceAC;

    public DisTransAC() {
        
        /**
         * Check whether a AC implementing
         * jac.aspects.distrans.persistence.PersistenceItf has been woven.
         * DisTransAC relies on it to store persistence data involved in the
         * transaction.
         */
        persistenceAC = null;

        ACManager acm = ACManager.getACM();
        Object[] acs = acm.getObjects();
        for (int i = 0; i < acs.length; i++) {
            if ( acs[i] instanceof PersistenceAC )
                persistenceAC = (PersistenceAC)acs[i];
		}
        
        if ( persistenceAC == null )
            throw new RuntimeException(
                "An AC implementing jac.aspects.distrans.persistence.PersistenceItf is mandatory for DisTransAC to work"
            );
    }
    
    
    /**
     * Delimit a transaction.
     * The transaction will begin before the method designated by the pointcut
     * designated by the 3 first parameter, and will end after the pointcut
     * designated by the 3 last ones.
     * 
     * @param txid      the transaction identifier
     * @param beginCNE  begin class name expression
     * @param beginONE  begin object name expression
     * @param beginMNE  begin method name expression
     * @param endCNE    end class name expression
     * @param endONE    end object name expression
     * @param endMNE    end method name expression
     * @param decisionClassName  the name of the class defining the method
     *           for deciding whether the transaction is to be commited
     *           or rollbacked.
     *           This must be a subclass of EndTransactionWrapper. 
     */
    public void delimitTransaction(
        String txid, 
        String beginCNE, String beginONE, String beginMNE,
        String endCNE, String endONE, String endMNE,
        String decisionClassName ) {
            
        BeginTransactionWrapper beginwrapper =
            new BeginTransactionWrapper(this);
        
        ClassItem cl = classes.getClass(decisionClassName);
        
        Wrapper endwrapper = null;
		try {
			endwrapper = (Wrapper)
			    cl.newInstance(
			        new Class[]{AspectComponent.class},
			        new Object[]{this}
			    );
		} catch (Exception e) {
            Log.error("delimitTransaction: Failed to instanciate endwrapper "+decisionClassName);
			e.printStackTrace();
		}
        pointcut(beginONE, beginCNE, beginMNE, beginwrapper, null);
        pointcut(endONE, endCNE, endMNE, endwrapper, null);
    }
    
        
    /**
     * The reference towards the JAC name repository.
     * Needed by setFieldsValueFromDataSource().
     */
    private Repository names = NameRepository.get();

    /**
     * The reference towards the RTTI class repository.
     */
    private ClassRepository classes = ClassRepository.get();
}
