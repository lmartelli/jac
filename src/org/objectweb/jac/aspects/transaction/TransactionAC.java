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
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA */

package org.objectweb.jac.aspects.transaction;


import java.util.Iterator;
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
 * This aspect component handle the transaction aspect within JAC
 * applications.
 *
 * @author Renaud Pawlak */

public class TransactionAC extends AspectComponent implements TransactionConf {
    static Logger logger = Logger.getLogger("transaction");

   /**
    * The default constructor. Wraps every application objects with a
    * <code>DispatchTransactionWrapper</code> instance.
    *
    * @see DispatchTransactionWrapper
    * @see DispatchTransactionWrapper#dispatch
    */

   public TransactionAC() {
      pointcut("ALL", "ALL", "ALL", 
               DispatchTransactionWrapper.class.getName(), 
               null, true);      
   }

   public void defineTransactionalMethods(String classExpr, 
                                          String methodExpr, 
                                          String objectExpr) {
      pointcut(objectExpr, classExpr, methodExpr, 
               new TransactionWrapper(this), "handleTransaction", null);
   }

   /**
    * This wrapper defines the transaction wrapper.
    *
    * <p>It defines a wrapping method that starts a transaction when a
    * method is called and that commits it when the method returns
    * normally.
    *
    * <code>Within the transaction, the cloning and the dispatching is
    * ensured by an external
    * <code>DispatchTransactionWrapper</code>.
    *
    * @see DispatchTransactionWrapper */

   public class TransactionWrapper extends Wrapper {
      /** The current transaction count (increments on each
          transaction). */
      int transactionCount=0;

      public TransactionWrapper(AspectComponent ac) {
         super(ac);
      }

      /**
       * This wrapping method wraps a transactional method.
       */
      public Object handleTransaction(Interaction interaction) {
         beginOfTransaction(interaction);
         Object ret = proceed(interaction);
         endOfTransaction(interaction);
         return ret;
      }
      /**
       * Initializes the transaction. */
      public void beginOfTransaction(Interaction interaction) {
         logger.debug("begin of transactional method: "+interaction.method);
         Integer id = new Integer(++transactionCount);
         this.attrdef("Transaction.id",id);
         this.attrdef("Transaction"+id+".affectedObjects",new Vector());
      }
      /**
       * Ends the transaction (when success). */
      public void endOfTransaction(Interaction interaction) {
         logger.debug("end of transactional method: "+interaction.method);
         Integer id = (Integer)this.attr("Transaction.id");
         Vector v = (Vector)this.attr("Transaction"+id+".affectedObjects");
         this.attrdef("Transaction.commit",id);
         Iterator it = v.iterator();
         while( it.hasNext() ) {
            Wrappee w = (Wrappee)it.next();
            Wrapping.invokeRoleMethod(w,"commit",new Object[] {this.attr("Transaction.id")});
         }
         this.attrdef("Transaction"+id+".affectedObjects",null);
      }
      /**
       * Handles an exception within a transaction (rollback).
       */
      public void catchException(Interaction interaction, Exception e) throws Exception {
         logger.debug("exception in transactionnal method: "+interaction.method);
         Integer id = (Integer)this.attr("Transaction.id");
         Vector v = (Vector)this.attr("Transaction"+id+".affectedObjects");
         this.attrdef("Transaction.rollback",id);
         if( id != null && v != null ) {
            Iterator it = v.iterator();
            while( it.hasNext() ) {
               Wrappee w = (Wrappee)it.next();
               Wrapping.invokeRoleMethod(w,"rollback",new Object[] {this.attr("Transaction.id")});
            }
            this.attrdef("Transaction"+id+".affectedObjects",null);
         }
         throw e;
      }

	/* (non-Javadoc)
	 * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
	 */
	public Object invoke(MethodInvocation invocation) throws Throwable {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.aopalliance.intercept.ConstructorInterceptor#construct(org.aopalliance.intercept.ConstructorInvocation)
	 */
	public Object construct(ConstructorInvocation invocation) throws Throwable {
		// TODO Auto-generated method stub
		return null;
	}
   }
}


