
package org.objectweb.jac.samples.contacts;

import java.util.Random;

public class Create {
   
    /**
     * A benchmark to create a bunch of contacts easily. Useful if
     * you don't want to create contacts one by one to test the sample.
     */
    public static void main( String[] params ) {
        int n = new Integer(params[0]).intValue();
        String firstNames[] = new String[] { 
            "Laurent", "Renaud", "Maxime", "Matthieu", "Jérôme",
            "Clément", "Thibault", "Carole", "Sonia", "Marie", "Mathilde", 
            "Frédéric", "Jean-Baptiste", "Nicolas", "Nathalie", "Grégoire",
            "Marek", "Ania", "Sébastien", "Xavier", "Marta", "Annie",
            "Philippe", "Béatrice", "Vincent" };
        System.out.println( "Creating "+n+" contacts..." );
        ContactRepository cr = new ContactRepository();
        new FilteredContacts(cr);
        Random rnd = new Random();
        for (int i=0; i<n; i++) {
            System.out.print( ""+i+" " );
            cr.addContact(
                new Person("Contact"+i,
                           firstNames[rnd.nextInt(firstNames.length)],
                           "",
                           "contact"+i+"@contacts.org"
                ));
        }
    }
}
