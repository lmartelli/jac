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

package org.objectweb.jac.aspects.distrans;


/**
 * A struct to hold data source name data.
 * 
 * @author Lionel Seinturier <Lionel.Seinturier@lip6.fr>
 * @version 1.0
 */

public class DataSource {
    
    public String driver;
    public String url;
    public String user;
    public String password;
    
    public DataSource(
        String driver, String url, String user, String password ) {
        
        this.driver = driver;
        this.url = url;
        this.user = user;
        this.password = password;
    }
    
    public boolean equals( Object other ) {
        if ( this == other )  return true;
        if ( ! (other instanceof DataSource) )  return false;

        DataSource dsother = (DataSource) other;
        return ( dsother.driver.equals(driver) &&
                 dsother.url.equals(url) &&
                 dsother.user.equals(user) &&
                 dsother.password.equals(password) );
    }

    public String toString() {
        return
            super.toString() +
            "[" + driver + "," + url + "," + user + "," + password + "]";
    }

    public int hashCode() {

        int h1 = (driver==null) ? 0 : driver.hashCode() & 255;
        int h2 = (url==null) ? 0 : url.hashCode() & 255;
        int h3 = (user==null) ? 0 : user.hashCode() & 255;
        int h4 = (password==null) ? 0 : password.hashCode() & 255;
        
        return (h1<<24) + (h2<<16) + (h3<<8) + h4;
    }
}
