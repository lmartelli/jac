
package org.objectweb.jac.samples.contacts;

import java.util.Vector;
import java.util.List;
import java.util.Iterator;
import gnu.regexp.*;

public class ContactRepository {
    List contacts = new Vector();

    /**
     * Get the value of contacts.
     * @return value of contacts.
     */
    public List getContacts() {
        return contacts;
    }
   
    /**
     * Add a contact to contacts.
     * @param contact the contact added.
     */
    public void addContact(Person contact) {
        contacts.add(contact);
    }

    /**
     * Remove a contact from contacts.
     * @param contact the contact removed.
     */
    public void removeContact(Person contact) {
        contacts.remove(contact);
    }

    /**
     * Find contacts in a company matching criteria.
     * @param criteria the criteria to find contacts (regexp).
     * @param company the company in where to find.
     */
    public List find(String criteria,String company) {
        Vector result = new Vector();
        Iterator it = contacts.iterator();
        RE recrit = null;
        try {
            if (!criteria.equals("")) 
                recrit = new RE(criteria,RE.REG_ICASE);
        } catch(Exception e) {
            e.printStackTrace();
            return result;
        }
        while (it.hasNext()) {
            Person contact = (Person) it.next();
            //System.out.println("testing contact "+contact);
            String companyName = "";
            if (!company.equals("")) {
                if (contact.getCompany() != null) {
                    companyName = contact.getCompany().getName();
                    if (company.compareToIgnoreCase(companyName)!=0) {
                        continue;
                    }
                }
            }
            if (recrit==null ||
                recrit.getMatch(contact.getLastName())!=null ||
                recrit.getMatch(contact.getFirstName())!=null) 
            {
                result.add(contact);
            }
        }
        return result;
    }


    /*   public void disconnect() {
         org.objectweb.jac.core.ApplicationRepository.get()
         .unextend("contacts","remote-access");
         org.objectweb.jac.aspects.tracing.Recorder.get().start();
         }

         public void reconnect() {
         org.objectweb.jac.aspects.tracing.Recorder.get().stop();
         org.objectweb.jac.core.ApplicationRepository.get()
         .extend("contacts","remote-access");
         org.objectweb.jac.aspects.tracing.Recorder.get().replay(
         org.objectweb.jac.aspects.tracing.Recorder.get().getNewObjectsClasses(),
         org.objectweb.jac.aspects.tracing.Recorder.get().getCalls());
         }
   
         public void showLocalActions() {
         org.objectweb.jac.aspects.tracing.Recorder.get().printRecordState();
         }*/

    /**
     * Mail to all people in contact list.
     */
    public void mailing() {
        String toMail = "mailto:";
        Iterator it = contacts.iterator();
        boolean first = true;
        while (it.hasNext()) {
            Person c = (Person)it.next();
            if (c.getEmail()!=null && !c.getEmail().equals("") && 
               c.isMailing()) 
            {
                if (first) {
                    toMail = toMail+c.getEmail();
                    first = false;
                } else {
                    toMail = toMail+","+c.getEmail();
                }
            }
        }
        try {
            String command = "mozilla -mail "+toMail;
            System.out.println("launching "+command);
            Runtime.getRuntime().exec(command);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}
