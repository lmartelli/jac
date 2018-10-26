
package org.objectweb.jac.samples.contacts;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.List;

public class CompanyRepository {

    ContactRepository contactRepository = null;
   
    public CompanyRepository(ContactRepository cr) {
        contactRepository = cr;
    }

    HashSet companies = new HashSet();

    /**
     * Get the value of companies.
     * @return value of companies.
     */
    public Set getCompanies() {
        return companies;
    }
   
    /**
     * Add a company to companies.
     * @param company the company added.
     */
    public void addCompany( Company company ) {
        companies.add( company );
    }

    public void showAll() {
        List contacts = contactRepository.getContacts();
        Iterator it = contacts.iterator();
        while(it.hasNext()) {
            Person cur = (Person)it.next();
            Company company = cur.getCompany();
            if (company == null) 
                continue;
            companies.add(company);
        }
    }
}
