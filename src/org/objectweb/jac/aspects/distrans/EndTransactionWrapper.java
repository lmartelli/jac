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

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.aopalliance.intercept.ConstructorInvocation;
import org.aopalliance.intercept.MethodInvocation;
import org.objectweb.jac.core.AspectComponent;
import org.objectweb.jac.core.Interaction;
import org.objectweb.jac.core.Wrapper;

/**
 * This wrapper delimits the end of a transaction.
 * This is an abstract class that needs an decide() implementation
 * to commit or rollback the transaction.
 * 
 * @author Lionel Seinturier <Lionel.Seinturier@lip6.fr>
 * @version 1.0
 */
public abstract class EndTransactionWrapper extends Wrapper {

    /** The transaction. */
    private UserTransaction usertx;
    
    public EndTransactionWrapper(AspectComponent ac) {
        super(ac);
        usertx = JOTMHelper.get().getUserTransaction();
    }
    
    /**
     * Method to decide whether the transaction is to be commited
     * or rollbacked.
     * 
     * @return true if commit, false if rollback
     */
    public abstract boolean decide();
    
    /* (non-Javadoc)
     * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
     */
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Interaction interaction = (Interaction) invocation;
		try {
			return _end(interaction);
		} catch (Exception e) {
			e.printStackTrace();
		}

        return null;
    }
    
    private Object _end(Interaction interaction)
        throws SecurityException, RollbackException, HeuristicMixedException,
        HeuristicRollbackException, SystemException 
    {
        Object ret = proceed(interaction);
        
        if (decide()) {
            usertx.commit();
        }
        else {
            usertx.rollback();
        }
            
        return ret;
    }
    
    public Object construct(ConstructorInvocation invocation) throws Throwable {
        throw new Exception("This wrapper does not support constructor wrapping");
    }
}
