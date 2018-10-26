package org.objectweb.jac.samples.bank;

/**
 * The default launcher for the bank program.<p>
 *
 * It creates a root account manager that creates a bank with the 1
 * number.<p>
 *
 * @see AccountManager
 * @see Bank
 *
 * @author <a href="mailto:maxime@aopsys.com">Maxime Pawlak</a>
 * @author <a href="mailto:pawlak@cnam.fr">Renaud Pawlak</a> 
 */
public class Run {

    /**
     * The program entry point.<p>
     *
     * @param args the command-line parameters (unused)
     */
    public static void main(String[] args) {
        am = new AccountManager();
    }
    static AccountManager am;
}
