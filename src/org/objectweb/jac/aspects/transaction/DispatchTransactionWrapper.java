/*
  Copyright (C) 2001 Renaud Pawlak

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

package org.objectweb.jac.aspects.transaction;



import java.util.Hashtable;
import java.util.Vector;
import org.aopalliance.intercept.ConstructorInvocation;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.AspectComponent;
import org.objectweb.jac.core.Interaction;
import org.objectweb.jac.core.Wrappee;
import org.objectweb.jac.core.Wrapper;
import org.objectweb.jac.core.Wrapping;
import org.objectweb.jac.util.Log;

/**
 * This wrapper wraps all JAC objects that can be part of a
 * transaction in order to dispatch the transactions to local clones
 * and to commit them. */

public class DispatchTransactionWrapper extends Wrapper {
    static Logger logger = Logger.getLogger("transaction");

    Hashtable clones = new Hashtable();
    Hashtable originals = new Hashtable();

    public DispatchTransactionWrapper(AspectComponent ac) {
        super(ac);
    }

    /**
     * Dispatches a call on a wrappee transaction-clone depending on
     * the current transaction if any (if not, it performs a regular
     * call). */

    public Object dispatch(Interaction interaction) {
        Integer id = (Integer)attr("Transaction.id");
        Integer commit = (Integer)attr("Transaction.commit");
        Integer rollback = (Integer)attr("Transaction.rollback");
        // none transaction is active => local call
        if( id == null ) {
            return proceed(interaction);
        } else if( commit != null ) {
            if( commit.equals( id ) ) {
                // this transaction is committing...
                return proceed(interaction);
            }
        } else if( rollback != null ) {
            if( rollback.equals( id ) ) {
                // this transaction is rollbacking...
                return proceed(interaction);
            }
        } else {
            if( ! clones.containsKey(id) ) {
                logger.debug("creating a new clone for transaction "+id);
                // creates a new original object
                originals.put(id,Wrapping.clone(interaction.wrappee));
                // creates a new clone
                clones.put(id,Wrapping.clone(interaction.wrappee));
                // memorize that the object is part of the transaction
                logger.debug(interaction.wrappee+" is part of a transaction");
                Vector affectedObjects = (Vector)attr("Transaction"+id+".affectedObjects");
                affectedObjects.add(interaction.wrappee);
            }
            logger.debug("delegating to the clone "+id);
            // delegate to the transaction's clone
            return interaction.invoke(clones.get(id));
        }
        return proceed(interaction);
    }

    /**
     * Commits the transaction on the wrappee (role method).
     *
     * <p>This method copies the transaction-clone state to the
     * original object.
     * 
     * @param transactionId the transaction to be commited */

    public void commit(Wrappee wrappee, Integer transactionId) throws Exception {
        logger.debug("committing transaction "+transactionId+
                     " on object "+wrappee);
        if( clones.containsKey(transactionId) ) {
            // remove the transaction state
            Wrappee clone = (Wrappee)clones.get(transactionId);
            Wrappee original = (Wrappee)originals.get(transactionId);
            clones.remove(transactionId);
            originals.remove(transactionId);
            Merging.merge(wrappee,original,clone);
        }
    }

    /**
     * Rollbacks the transaction on the wrappee (role method).
     *
     * @param transactionId the transaction to be rollbacked */

    public void rollback(Wrappee wrappee, Integer transactionId) {
        logger.debug("rollbacking transaction "+transactionId+
                     " on object "+wrappee);
        if( clones.containsKey(transactionId) ) {
            // remove the transaction state
            clones.remove(transactionId);
            originals.remove(transactionId);
        }            
    }

    /* (non-Javadoc)
     * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
     */
    public Object invoke(MethodInvocation invocation) throws Throwable {
        // TODO Auto-generated method stub
        throw new Exception("Unimplemented method: invoke(MethodInvocation invocation)");
    }

    /* (non-Javadoc)
     * @see org.aopalliance.intercept.ConstructorInterceptor#construct(org.aopalliance.intercept.ConstructorInvocation)
     */
    public Object construct(ConstructorInvocation invocation) throws Throwable {
        // TODO Auto-generated method stub
        throw new Exception("Unimplemented method: construct(MethodInvocation invocation)");
    }

} 


