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

package org.objectweb.jac.samples.distransbank;

import org.objectweb.jac.aspects.distrans.EndTransactionWrapper;
import org.objectweb.jac.core.AspectComponent;
import org.objectweb.jac.core.NameRepository;
import org.objectweb.jac.util.Repository;

/**
 * @author Lionel Seinturier <Lionel.Seinturier@lip6.fr>
 * @version 1.0
 */
public class SampleEndTransactionWrapper extends EndTransactionWrapper {

    /**
     * @param ac  the AC managing this wrapper
     */
    public SampleEndTransactionWrapper( AspectComponent ac ) {
        super(ac);
    }

    /**
     * Method to decide whether the transaction is to be commited
     * or rollbacked.
     * 
     * @return true if commit, false if rollback
     */
    public boolean decide() {

        Repository names = NameRepository.get();
        Account account0 = (Account) names.getObject("account0");
        double balance = account0.getBalance();
        
        return (balance>=0.0);
    }
}
