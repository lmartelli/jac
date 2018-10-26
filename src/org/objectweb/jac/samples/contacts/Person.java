
package org.objectweb.jac.samples.contacts;

import java.util.Date;
import java.util.Vector;
import java.util.List;

public class Person {
    String lastName;
    String firstName;
    String phone;
    String notes;
    Date lastContact;
    Date contactAgain;

    public Person() {
    }

    public Person(String lastName, String firstName, String phone, 
                  String email) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.phone = phone;
        this.email = email;
    }

    public String toString() {
        return (lastName==null?"":lastName)+" "+(firstName==null?"":firstName);
    }
   
    List actions = new Vector();

    /**
     * Get the value of actions.
     * @return value of actions.
     */
    public List getActions() {
        return actions;
    }

    /**
     * Add an action from actions.
     * @param action the action added.
     */
    public void addAction(Action action) {
        actions.add(action);
    }

    /**
     * Remove an action from actions.
     * @param action the action removed.
     */
    public void removeAction(Action action) {
        actions.remove(action);
    }

    /**
     * Get the value of lastContact.
     * @return value of lastContact.
     */
    public Date getLastContact() {
        return lastContact;
    }
   
    /**
     * Set the value of lastContact.
     * @param v  Value to assign to lastContact.
     */
    public void setLastContact(Date  v) {
        this.lastContact = v;
    }
   
   
    /**
     * Get the value of contactAgain.
     * @return value of contactAgain.
     */
    public Date getContactAgain() {
        return contactAgain;
    }
   
    /**
     * Set the value of contactAgain.
     * @param v  Value to assign to contactAgain.
     */
    public void setContactAgain(Date  v) {
        this.contactAgain = v;
    }
   
    /**
     * Get the value of notes.
     * @return value of notes.
     */
    public String getNotes() {
        return notes;
    }
   
    /**
     * Set the value of notes.
     * @param v  Value to assign to notes.
     */
    public void setNotes(String  v) {
        this.notes = v;
    }
   
    /**
     * Set the value of lastName.
     * @param lastName Value to assign to lastName.
     */
    public void setLastName( String lastName ) {
        this.lastName = lastName;
    }
   
    /**
     * Get the value of lastName.
     * @return value of lastName.
     */
    public String getLastName() {
        return lastName;
    }
   
    /**
     * Set the value of firstName.
     * @param firstName  Value to assign to firstName.
     */
    public void setFirstName( String firstName ) {
        this.firstName = firstName;
    }
   
    /**
     * Get the value of firstName.
     * @return value of firstName.
     */
    public String getFirstName() {
        return firstName;
    }
   
    /**
     * Set the value of phone.
     * @param phone  Value to assign to phone.
     */
    public void setPhone( String phone ) {
        this.phone = phone;
    }
   
    /**
     * Get the value of phone.
     * @return value of phone.
     */
    public String getPhone() {
        return phone;
    }
   
    String position;
   
    /**
     * Get the value of position.
     * @return value of position.
     */
    public String getPosition() {
        return position;
    }
   
    /**
     * Set the value of position.
     * @param v  Value to assign to position.
     */
    public void setPosition(String  v) {
        this.position = v;
    }
   
    String email;
   
    /**
     * Get the value of email.
     * @return value of email.
     */
    public String getEmail() {
        return email;
    }
   
    /**
     * Set the value of email.
     * @param v  Value to assign to email.
     */
    public void setEmail(String  v) {
        this.email = v;
    }
   

    Company company;
   
    /**
     * Get the value of company.
     * @return value of company.
     */
    public Company getCompany() {
        return company;
    }
   
    /**
     * Set the value of company.
     * @param v  Value to assign to company.
     */
    public void setCompany(Company  v) {
        this.company = v;
    }

    boolean mailing;
   
    /**
     * Get the value of mailing.
     * @return value of mailing.
     */
    public boolean isMailing() {
        return mailing;
    }
   
    /**
     * Set the value of mailing.
     * @param v  Value to assign to mailing.
     */
    public void setMailing(boolean  v) {
        this.mailing = v;
    }
   
}
	
