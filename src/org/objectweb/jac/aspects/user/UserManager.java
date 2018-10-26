/*
  Copyright (C) 2002-2003 Renaud Pawlak <renaud@aopsys.com>

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
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA */

package org.objectweb.jac.aspects.user;

import java.util.Hashtable;
import java.util.Map;

/**
 * Binding with profiles
 */

public class UserManager {

    public UserManager() {
    }
   
    Hashtable profiles = new Hashtable();

    /**
     * Get a profile with its name.
     * 
     * @param name the name of the profile.
     */
    public Profile getProfile(String name) {
        return (Profile)profiles.get(name);
    }

    public Map getProfiles() {
        return profiles;
    }

    /**
     * Add a news profile.
     * 
     * @param profile the profile to add
     */
    public void addProfile(Profile profile) {
        profiles.put(profile.getName(),profile);
    }

    Object defaultAdmin;
   
    /**
     * Get the value of defaultAdmin.
     * @return value of defaultAdmin.
     */
    public Object getDefaultAdmin() {
        return defaultAdmin;
    }
   
    /**
     * Set the value of defaultAdmin.
     * @param v  Value to assign to defaultAdmin.
     */
    public void setDefaultAdmin(Object  v) {
        this.defaultAdmin = v;
    }
      
}
