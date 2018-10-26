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

import org.objectweb.jac.samples.contacts.Person;

/**
 * Defines the accounts in the banks of the bank sample.<p>
 *
 * Accounts belong to banks.<p>
 *
 * @see Bank
 *
 * @author <a href="mailto:maxime@aopsys.com">Maxime Pawlak</a>
 * @author <a href="mailto:pawlak@cnam.fr">Renaud Pawlak</a>
 */

public class Account {

   /** allowed deficit */
   protected double allowedDeficit = 0;
   
   /** The current balance of the account. */
   protected double balance;

   /** The account number. */
   protected long accountNumber;

   Bank bank=null;
   
   public void setBank(Bank bank) {
      this.bank=bank;
   }

   /** the owner */
   Person owner;

   public Person getOwner() {
      return owner;
   }

   public void setOwner(Person owner) {
      this.owner=owner;
      if( bank != null ) {
         bank.addUser(owner);
      }
   }

   public void setAllowedDeficit(double allowedDeficit) {
      this.allowedDeficit = allowedDeficit;
   }

   public double getAllowedDeficit() {
      return allowedDeficit;
   }

   /**
    * Creates a new account.<p>
    *
    * @param accountNumber the new account number
    * @param bankNumber the bank number the new account will belong to
    */

   public Account(long accountNumber){
      this.accountNumber = accountNumber;
      balance = 0;
   }

   /**
    * Returns a textual representation of the account.<p>
    *
    * @return a textual representation */

   public String toString() {
      return accountNumber+" ("+balance+")";
   }

   /**
    * Credits the account balance with the given amount.<p>
    *
    * @param amount the amount to credit with
    */

   public void credit(double amount) {
      if( balance + amount > allowedDeficit ) {
         balance += amount; 
      } else {
         throw new RuntimeException("Allowed deficit is not sufficient for account "+this);
      }
   }

   /**
    * Debits the account balance with the given amount.<p>
    *
    * @param amount the amount to debit with
    */

   public void debit(double amount) {
      if( balance - amount > allowedDeficit ) {
         balance -= amount; 
      } else {
         throw new RuntimeException("Allowed deficit is not sufficient for account "+this);
      }
   }

   /**
    * Gets the current balance of the account.<p>
    *
    * @return the balance */
 
   public double getBalance(){
      return balance;
   }

   public void setBalance(double balance){
      this.balance=balance;
   }

   /**
    * Gets the account number.<p>
    *
    * @return the account number */

   public long getAccountNumber(){
      return accountNumber;
   }

   public void setAccountNumber(long number) {
      this.accountNumber = number;
   }

}
