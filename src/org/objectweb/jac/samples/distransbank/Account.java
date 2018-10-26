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

/**
 * Sample program using the distrans AC.
 * 
 * @author Lionel Seinturier
 * @version 1.0
 */
public class Account {
    
    private String name;
    private double balance;

	public static void main(String[] args) throws Exception {
        
        Account bob = new Account();
        Account robert = new Account();
        
        bob.setName("Bob");
        robert.setName("Robert");
        
        double initial = (int) (Math.random()*100) * 2;
        
        System.out.println("Set balance Bob: "+initial);
        bob.setBalance(initial);
        System.out.println(bob);
        System.out.println(robert);
        System.out.println();
        
        System.out.println("Test for commit");
        System.out.println("Transfert from Bob to Robert: "+initial/2);
        transfert(bob,robert,initial/2);        
        System.out.println();
        
        System.out.println("After transaction");
        System.out.println(bob);
        System.out.println(robert);
        System.out.println();
        
        System.out.println("Test for rollback");
        System.out.println("Transfert from Bob to Robert: "+initial*2);
        transfert(bob,robert,initial*2);
        System.out.println();
        
        System.out.println("After transaction");
        System.out.println(bob);
        System.out.println(robert);
        System.out.println();
        
        System.exit(1);
	}
    
    public void setName(String name){
        this.name = name;
    }
    
    public void setBalance(double balance) { this.balance = balance; }
    public double getBalance() { return balance; }
    
    public void credit( double amount ) {
        System.out.print(name+": "+balance+" -> ");
        balance += amount;
        System.out.println(balance);
    }
    
    public void withdraw( double amount ) {
        System.out.print(name+": "+balance+" -> ");
        balance -= amount;
        System.out.println(balance);
    }
    
    public static void transfert( Account from, Account to, double amount ) {
        to.credit(amount);
        from.withdraw(amount);
    }
    
    public String toString() {
        /**
         * Accessing directly the fields (eg balance) may lead
         * to inconsistencies after a rollback
         * (the initial state is restored in
         * the persistant storage but not in the objects).
         * Calling a getter forces the loading of the state
         * from the persistant storage
         * (getters are wrapped by ReadWrapper instances).
         */
        return "Account: "+name+", balance: "+getBalance();
    }
}
