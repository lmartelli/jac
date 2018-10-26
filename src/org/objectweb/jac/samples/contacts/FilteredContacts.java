
package org.objectweb.jac.samples.contacts;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class FilteredContacts extends ContactRepository {
   
    ContactRepository contactRepository = null;
    CompanyRepository companyRepository = null;

    public FilteredContacts() {
    }

    public FilteredContacts(ContactRepository repository) {
        contactRepository = repository;
        contacts.addAll(contactRepository.getContacts());
    }

    public ContactRepository getContactRepository() {
        return contactRepository;
    }
    public void setContactRepository(ContactRepository cr) {
        this.contactRepository = cr;
    }

    /**
    * Add a contact to the contact repository.
    * @param contact the contact added.
    */
    public void addContact(Person contact) {
        contactRepository.addContact(contact);
        contacts.add(contact);
    }

    /**
    * Remove a contact from contact repository.
    * @param contact the contact removed.
    */
    public void removeContact(Person contact) {
        contactRepository.removeContact(contact);
        contacts.remove(contact);
    }

    public void showAll() {
        search(".*", null);
    }

    public CompanyRepository companies() {
        if (companyRepository == null)
            companyRepository = new CompanyRepository(contactRepository);
        companyRepository.showAll();
        return companyRepository;
    }
   
    /**
     * Filter the contacts of the contact repository regarding a
     * searching criteria.
     *
     * @param criteria can be a regexp 
     */
    public void search(String criteria, Company company) {
        contacts.clear();
        String name = "";
        if (company!=null) {
            name = company.getName();
        }
        List foundContacts = contactRepository.find(criteria,name);
        Iterator it = foundContacts.iterator();
        while (it.hasNext()) {
            Person contact = (Person) it.next();
            if (!contacts.contains(contact))
                contacts.add(contact);
        }

      /*
      java.util.Iterator it = contactRepository.getContacts().iterator();
      if( criteria.equals("") ) criteria = ".*";
      RE recrit = null;
      try {
         recrit = new RE(criteria);
      } catch( Exception e ) {
         e.printStackTrace();
         return;
      }
      while( it.hasNext() ) {
         Person contact = (Person) it.next();
         if( company!=null && !company.getName().equals("") ) { 
            if( company != contact.getCompany() ) {
               continue;
            }
         }
         if( (!contacts.contains(contact)) && 
             recrit.isMatch( contact.getLastName() ) ) {
            contacts.add(contact);
            System.out.println("Contact "+contact+" added to filtered");
         }
         if( (!contacts.contains(contact)) && 
             recrit.isMatch( contact.getFirstName() ) ) {
            contacts.add(contact);
            System.out.println("Contact "+contact+" added to filtered");
         }
         }*/
    }

}
