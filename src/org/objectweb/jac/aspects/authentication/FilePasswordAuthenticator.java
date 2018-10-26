/*
  Copyright (C) 2001-2002 Laurent Martelli <laurent@aopsys.com>
  
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

package org.objectweb.jac.aspects.authentication;

import org.objectweb.jac.util.WrappedThrowableException;
import java.io.File;
import java.io.FileReader;
import java.io.StreamTokenizer;
import java.util.HashMap;

/**
 * This Authenticator ask for a username and password and compares
 * them to declared ones stored in a file. The number of allowed
 * attempts to enter a valid (username,password) is configurable. It
 * needs a DisplayContext attribute in order to be able to interact
 * with the user.  */

public class FilePasswordAuthenticator extends PasswordAuthenticator {   

    File passwordFile;

    /**
     * The constructor.
     *
     * @param retries the number of time the authenticator will ask the
     * user to retype wrong information
     * @param passwordFilename the filename where the users are stored
     */
    public FilePasswordAuthenticator(String retries, String passwordFilename) {
        super(Integer.parseInt(retries));
        passwordFile = new File(passwordFilename);
    }

    HashMap passwords;

    boolean checkPassword(String username, String password) {
        if (passwords==null) {
            // read the password file
            passwords = new HashMap();
            try {
                StreamTokenizer tokens = new StreamTokenizer(new FileReader(passwordFile));
                tokens.resetSyntax();
                tokens.wordChars('\000','\377');
                tokens.whitespaceChars('\n','\n');
                tokens.whitespaceChars('\r','\r');
                while (tokens.nextToken() != StreamTokenizer.TT_EOF) {
                    String line = tokens.sval.trim();
                    int index = line.indexOf(':');
                    if (index!=-1) {
                        passwords.put(line.substring(0,index),
                                      line.substring(index+1));
                    }
                }
            } catch (Exception e) {
                throw new WrappedThrowableException(e);
            }
        }
        Object pass = passwords.get(username);
        return (pass!=null && pass.equals(password));
    }
}
