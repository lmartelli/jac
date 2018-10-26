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



import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import org.aopalliance.intercept.ConstructorInvocation;
import org.aopalliance.intercept.MethodInvocation;
import org.objectweb.jac.core.AspectComponent;
import org.objectweb.jac.core.Interaction;
import org.objectweb.jac.core.Wrapper;
import org.objectweb.jac.util.Log;

/**
 * This wrapper delimits the begining of a transaction.
 * 
 * @author Lionel Seinturier <Lionel.Seinturier@lip6.fr>
 * @version 1.0
 */
public class BeginTransactionWrapper extends Wrapper {

	/** The transaction. */
	private UserTransaction usertx;

	/**
	 * @param ac  the AC managing this wrapper
	 */
	public BeginTransactionWrapper(AspectComponent ac) {
		super(ac);
		usertx = JOTMHelper.get().getUserTransaction();
	}

	public Object invoke(MethodInvocation invocation) throws Throwable {
        Interaction interaction = (Interaction) invocation;
        try {
            return _begin(interaction);
        } catch (Exception e) {
            Log.error("Error while beginning transaction");
            e.printStackTrace();
        }
        return null;
	}

	private Object _begin(Interaction interaction)
		throws NotSupportedException, SystemException {

		usertx.begin();

		return proceed(interaction);
	}

    public Object construct(ConstructorInvocation invocation)
        throws Throwable {
        throw new Exception("This wrapper does not support constructor wrapping");
    }
}
