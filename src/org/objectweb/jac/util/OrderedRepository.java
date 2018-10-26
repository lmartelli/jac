/*
  Copyright (C) 2001 Renaud Pawlak

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

package org.objectweb.jac.util;

import java.util.Vector;
import org.objectweb.jac.util.ExtArrays;

/**
 * @author <a href="http://cedric.cnam.fr/~pawlak/index-english.html">Renaud Pawlak</a>
 */

/**
 * This class defines a repository that provides order on the
 * registered objects.
 *
 * <p>The order corresponds to the order the objects where registered
 * into the repository. */

public class OrderedRepository extends Repository {

    /**
     * Get the sole repository instance for this class. Creates it if
     * it does not exist yet.
     *
     * <p>NOTE: this method MUST be defined by all subclasses.
     */
    public static Repository get() {
        if (repository == null) 
            repository = new OrderedRepository();
        return repository;
    }
   
    /**
     * Store the sole instance of repository.
     *
     * <p>NOTE: this field MUST be defined by all subclasses.
     * 
     * @see #get()
     */
    protected static Repository repository = null;

    /**
     * Vector for the ordered objects. */
    public Vector orderedObjects = new Vector();

    /**
     * Vector for the ordered names. */
    public Vector orderedNames = new Vector();

    /**
     * Register a new object into the repository.
     *
     * @param logicalName the key that allows to find the object
     * @param object the object to register
     * @return true if the object registered, false if already
     * registered
     *
     * @see #unregister(String) 
     */
    public boolean register(String logicalName, Object object) {
        int index = 0;
        index = orderedNames.indexOf(logicalName);
        if (index != -1) {
            orderedNames.remove(index);
            orderedObjects.remove(index);
        }
        orderedObjects.add(object);      
        orderedNames.add(logicalName);
        super.register(logicalName, object);
        return true;
    }

    /**
     * Unregister a new JacObject into the repository.
     *
     * @param logicalName the key that allows to find the object
     *
     * @see #register(String,Object) 
     */
    public void unregister(String logicalName) {
        int index = 0;
        index = orderedNames.indexOf(logicalName);
        if (index == -1) {
            return;
        }
        orderedNames.remove(index);
        orderedObjects.remove(index);
        super.unregister(logicalName);
    }

    /**
     * Return all the ordered registered objects as an array.
     *
     * <p>Reverse operation is <code>getNames()</code>.
     *
     * @return the registered objects in this repository
     *
     * @see #register(String,Object)
     * @see #getNames() 
     */
    public Object[] getObjects() {
        return orderedObjects.toArray();
    }

    /**
     * Return the ordered names of the registered objects as an array.
     *
     * <p>The given order is the registering order of the objects.
     *
     * <p>Reverse operation is <code>getObjects()</code>.
     *
     * @return the registered object names in this repository
     *
     * @see #register(String,Object)
     * @see #getObjects() 
     */
    public String[] getNames() {
        return (String[])orderedNames.toArray(ExtArrays.emptyStringArray);
    }

    public String getPrintableString() {
        String s="";
        for (int i=0; i<orderedNames.size(); i++) {
            s = s+" - "+orderedNames.get(i)+" : "+orderedObjects.get(i)+"\n";
        }
        return s;
    }

}


