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

package org.objectweb.jac.samples.hibernate;

/**
 * Sample program using the Hibernate AC.
 * 
 * Hibernate requires that persistent classes provide:
 * - getters/setters for all persistent fields
 * - an empty constructor (Hibernate uses Constructor.newInstance())
 * 
 * HibernateAC uses JAC object names as a primary key to identify
 * objects stored in the database. Hence persistent class must
 * provide a String field (here id) declared as the primary key
 * in the associated .hbm.xml file.
 * 
 * Note also that accessing fields directly the (eg balance)
 * may lead to inconsistencies.
 * Calling getters and setters ensures that the most recent
 * values are always fetched from the database.
 *
 * @author Lionel Seinturier
 * @version 1.0
 */
public class Account {
    
    private String id;
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
        
        System.out.println("Transfert from Bob to Robert: "+initial/2);
        transfert(bob,robert,initial/2);        
        System.out.println();
        
        System.out.println("Transfert from Bob to Robert: "+initial*2);
        transfert(bob,robert,initial*2);
        System.out.println();
        
        System.exit(1);
	}
    
    public void setId(String id){ this.id = id; }
    public String getId() { return id; }
    
    public void setName(String name){ this.name = name; }
    public String getName() { return name; }
    
    public void setBalance(double balance) { this.balance = balance; }
    public double getBalance() { return balance; }
    
    public void credit( double amount ) {
        String name = getName();
        double balance = getBalance();
        
        System.out.print(name+": "+balance+" -> ");
        balance += amount;
        System.out.println(balance);
        
        setBalance(balance);
    }
    
    public void withdraw( double amount ) {
        String name = getName();
        double balance = getBalance();
        
        System.out.print(name+": "+balance+" -> ");
        balance -= amount;
        System.out.println(balance);
        
        setBalance(balance);
    }
    
    public static void transfert( Account from, Account to, double amount ) {
        to.credit(amount);
        from.withdraw(amount);
    }
    
    public String toString() {
        return "Account: "+getName()+", balance: "+getBalance();
    }
}
