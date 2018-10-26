/*
  Copyright (C) 2001-2002 Renaud Pawlak, Eddy Truyen.

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 2 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  USA */

package org.objectweb.jac.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

/**
 * This class represents a set of contextual informations for a given
 * thread of a running JAC system.
 * 
 * <p>For each method call on a JAC object, a new interaction is
 * started on the current collaboration. At any point of the program,
 * the programmer can get the current collaboration for the current
 * thread by using <code>get()</code>.
 *
 * <p>The <code>CollaborationParticipant</code> interface provides a
 * more friendly interface to access the current collaboration. In a
 * clean design, only the classes that implement this interface may
 * access the collaboration.
 *
 * @see Collaboration#get()
 * @see CollaborationParticipant
 *
 * @author Renaud Pawlak
 * @author Eddy Truyen
 */

public class Collaboration implements java.io.Serializable {

    /** The attribute is global to all the sites (it is serialized and
        transmitted with the collaboration) */
    public static Object GLOBAL=new Integer(1); 

    transient static Hashtable attrTypes=new Hashtable();

    /**
     * Returns true if the attribute is global. The attribute is
     * then transmitted to the remote sites reached by the
     * collaboration. Note that a global attribute must always be
     * serializable. If the attribute is not global, it
     * always stays on the site where it has been defined.
     *
     * <p>By default, if the <code>setGlobal</code> method is not
     * called, all the attributes are local.
     *
     * @see #setGlobal(String) 
     */
    public static boolean isGlobal(String attrName) {
        if(attrTypes.get(attrName)==GLOBAL) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Sets an attribute to be global.
     *
     * <p>This method should only be called once for each attribute and
     * before the first initialization of the attribute value in the
     * collaboration since the globality is an inherent caracteristic
     * of an attribute. 
     */
    public static void setGlobal(String attrName) {
        attrTypes.put(attrName,GLOBAL);
    }
   
    /** Stores collaborations for all threads. */
    transient static Hashtable collaborations = new Hashtable();

    /** Static initialization of the collaborations. */

    static {
        Thread current = Thread.currentThread();
        collaborations.put ( current, new Collaboration() );
    }

    /**
     * Get the collaboration for the current thread.
     *
     * @return the current collaboration 
     */
    public static Collaboration get() {
        Thread current = Thread.currentThread();
        Collaboration ret = (Collaboration) collaborations.get(current);
        if ( ret == null ) {
            collaborations.put ( current, ret = new Collaboration() );
        }
        return ret;
    }

    /**
     * Set a new collaboration for the current thread.
     *
     * @param collaboration the collaboration 
     */
    public static void set(Collaboration collaboration) {
        Thread current = Thread.currentThread();
        collaborations.put(current, collaboration);
    }
   
    /** Store the attributes of the interaction. */
    private HashMap attrs;

    /**
     * Creates an new collaboration.
     * 
     * <p>The programmer should not explicitly create a new
     * collaboration since this is automatically done by the system for
     * any new thread.
     *
     * <p> At any point of the program, the programmer can get the
     * current collaboration for the current thread by using
     * <code>Collaboration.get()</code>.
     *
     * @see Collaboration#get() 
     */
    public Collaboration() {
        reset();
    }

    /**
     * Create a new Collaboration and initialiaze it's attribute from a
     * parent Collaboration.
     *
     * @param parent the parent collaboration
     */
    public Collaboration(Collaboration parent) {
        reset();
        if (parent!=null) {
            Iterator it = parent.attributeNames().iterator();
            while (it.hasNext()) {
                String attrName = (String)it.next();
                addAttribute(
                    attrName,parent.getAttribute(attrName));
            }
        }
    }

    /**
     * Returns a collection of all attribute names
     */
    public Collection attributeNames() {
        HashSet names = new HashSet();
        names.addAll(attrs.keySet());
        return names;
    }

    public static Collection globalAttributeNames() {
        HashSet names = new HashSet();
        names.addAll(attrTypes.keySet());
        return names;
    }

    /**
     * Returns the map of attribute's name -> value
     */
    public Map getAttributes() {
        return (Map)attrs.clone();
    }

    /**
     * Set some attributes. Do not override current attributes.
     * @param attributes map of name->value of attributes to set.
     */
    public void setAttributes(Map attributes) {
        Iterator it = attributes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            if (!attrs.containsKey(entry.getKey()))
                attrs.put(entry.getKey(),entry.getValue());
        }
    }

    /**
     * Reset the Collaboration. 
     * <p>Clears interactions, and all attributes.
     */
    public void reset() {
        attrs = new HashMap();
    }

    /**
     * Add a persistent attribute to the collaboration (can be done by
     * any Jac object).
     * 
     * <p>NOTE: a persitent attribute is visible for all the objects of a
     * collaboration.
     *
     * @param name the name of the attribute
     * @param att the value of the attribute 
     */
    public Object addAttribute(String name, Object att) {
        if ( name != null ) {
            attrs.put(name, att);
        }
        return att;
    }

    /**
     * Return the value of an attribute for the current collaboration.
     * 
     * <p>This method first seeks into the persistent attributes. If
     * none matches, it seeks into the transient attributes. If still
     * not found, finally seeks into the transient local attributes. If
     * all the lookups failed, return null.
     *
     * @param name the name of the attribute
     * @return the value of the attribute 
     */
    public Object getAttribute(String name) {
        if (attrs.containsKey(name)) {
            return attrs.get(name);
        }
        return null;
    }

    /**
     * Removes the attribute from the current collaboration. 
     */
    public void removeAttribute(String name) {
        attrs.remove(name);
    }

    String cur_App;

    public final void setCurApp(String app) {
        this.cur_App = app;
    }

    public final String getCurApp() {
        return cur_App;
    }
   
    /**
     * Returns a textual representation of the collaboration. 
     */
    public String toString() {
        return "Collaboration: \n" + 
            "attributes = " + attrs.toString();
    }
   
   
}
