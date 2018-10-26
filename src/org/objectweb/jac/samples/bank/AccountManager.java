/*
  Renaud Pawlak, pawlak@cnam.fr, CEDRIC Laboratory, Paris, France.
  Lionel Seinturier, Lionel.Seinturier@lip6.fr, LIP6, Paris, France.

  JAC-Core is free software. You can redistribute it and/or modify it
  under the terms of the GNU Library General Public License as
  published by the Free Software Foundation.
  
  JAC-Core is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

  This work uses the Javassist system - Copyright (c) 1999-2000
  Shigeru Chiba, University of Tsukuba, Japan.  All Rights Reserved.  */

package org.objectweb.jac.samples.bank;

import org.objectweb.jac.lib.java.util.*;

/**
 * Defines a manager for all the created banks and accounts of the
 * bank sample.<p>
 *
 * @see Account
 * @see Bank
 *
 * @author <a href="mailto:maxime@aopsys.com">Maxime Pawlak</a>
 * @author <a href="mailto:pawlak@cnam.fr">Renaud Pawlak</a> */

public class AccountManager {

   /** Stores the banks that are managed by this manager. */
   protected Hashtable banks = new Hashtable();
   
   /**
    * Returns a table that links the banks with their numbers.<p>
    *
    * @return a hashtable (numbers -> banks)
    */

   public Hashtable getBanks () {
      return banks; 
   }

   /**
    * Adds an existing bank to this manager.<p>
    *
    * @param bank the bank to add */

   public void addBank (Bank bank) {
      banks.put(new Long(bank.bankNumber),bank);
   }
		
   /**
    * Makes a transfer between two accounts.<p>
    *
    * The source account is debited from the amount wilst the
    * destination account is credited with the same amount.<p>
    *
    * @param asrc the source account
    * @param adst the destination account
    * @param amount the amount to tranfer */

   public void transfer (Account asrc, Account adst, double amount) {
      adst.credit(amount);
      asrc.debit(amount);
   }

}
