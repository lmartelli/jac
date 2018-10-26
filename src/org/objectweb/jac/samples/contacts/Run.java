
package org.objectweb.jac.samples.contacts;

public class Run {

    /**
     * This sample is a contact list, showing how the GUI, persistence
     * and session components can work.
     */
    public static void main(String[] params) {
        ContactRepository cr = new ContactRepository();
        contacts = new FilteredContacts(cr);
    }
    static FilteredContacts contacts;
}

