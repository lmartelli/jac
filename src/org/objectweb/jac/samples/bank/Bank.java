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

import org.objectweb.jac.lib.java.util.Hashtable;
import org.objectweb.jac.lib.java.util.Vector;
import org.objectweb.jac.samples.contacts.Person;

/**
 * Defines the banks in the bank sample.<p>
 *
 * @see Account
 * @see AccountManager
 *
 * @author <a href="mailto:maxime@aopsys.com">Maxime Pawlak</a>
 * @author <a href="mailto:pawlak@cnam.fr">Renaud Pawlak</a>
 */

public class Bank {

   public long getBankNumber() { return bankNumber; }
   public void setBankNumber( long bankNumber ) { this.bankNumber=bankNumber; } 

   /** The bank identifier. */
   protected long bankNumber;

   /** Stores the accounts within this bank. */
   protected Hashtable accounts = new Hashtable();

   protected Vector users = new Vector();

   public void addUser(Person user) {
      if( !users.contains(user) ) {
         users.add(user);
      }
   }

   /*public void setUsers(Person user) {
      users.add(user);
      }*/
   
   public Vector getUsers() {
      return users;
   }

   /**
    * Creates a new bank with a new number.
    *
    * @param bankNumber the bank number (should be unique) */

   public Bank(long bankNumber) {
      this.bankNumber = bankNumber;
   }

   /**
    * Returns the accounts within this bank.<p>
    *
    * @return a hashtable that links numbers to the accounts 
    */

   public Hashtable getAccounts() {
      return accounts;
   }

   /**
    * Adds an existing account to the accounts list.<p>
    *
    * @param account the new account
    */

   public void addAccount(Account account) {
      accounts.put(new Long (account.accountNumber) , account) ;
      account.setBank(this);
      if( account.getOwner() != null ) {
         users.add(account.getOwner());
      }      
   }

   /**
    * Removes an existing account to the accounts list.<p>
    *
    * @param account the account to remove
    */

   public void removeAccount(Account account) {
      accounts.remove(new Long(account.accountNumber));
   }

   /**
    * Gets an account that belongs to this bank.<p>
    *
    * @param accountNumber the number of the account
    * @return the corresponding account (null if not found)
    */

   public Account getAccount(long accountNumber) {
      Account account;
      account = (Account) accounts.get(new Long (accountNumber));
      return account;
   }

   /**
    * Returns a textual representation of the bank.<p>
    *
    * @return the text for the bank */

   public String toString() {
      return ""+bankNumber;
   }


}
