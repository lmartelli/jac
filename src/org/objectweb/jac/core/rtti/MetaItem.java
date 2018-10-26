/*
  Copyright (C) 2001-2002 Renaud Pawlak, Laurent Martelli
  
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

package org.objectweb.jac.core.rtti;

import java.lang.reflect.*;
import java.util.*;
import org.objectweb.jac.core.ACManager;
import org.objectweb.jac.util.ExtArrays;

/**
 * This class defines the super class for all the meta items whithin
 * the rtti aspect.<p>
 *
 * A meta item encapsulates a <code>java.lang.reflect</code> item so
 * that the user of this item can add extra informations
 * (attributes). Typically this feature can be used by an aspect to
 * tag an element of the model to react to this tag later on.<p>
 *
 * Examples:<p> <ul> 
 *
 * <li>A persistence aspect can tag some field persistent, add methods
 * that change the object states even if they do not fit naming
 * conventions...<p>
 *
 * <li>A GUI can tag a given field to be invisible or a class to be
 * displayed by a special view (eg a given Swing component)...
 *
 * </ul>
 *
 * @author Renaud Pawlak
 * @author Laurent Martelli
 */

public abstract class MetaItem {

    /** A correspondance table between the RRTI attributes and the
        aspect components that set them. */
    protected static HashMap attrACs = new HashMap();

    /**
     * Unsets all the attributes of all the RTTI items that have been
     * set by a given aspect component.<p>
     *
     * @param acName the aspect component name
     * @see #unsetAttribute(String) */
   
    public static void unsetAttributesFor( String acName ) {}

    /* A <code>name -> value</code> container. */
    private HashMap attributes = new HashMap();

    static ACManager acManager;
    static Method acManagerGetObjectMethod;
    static Method acManagerGetMethod;
   
    static {
        try {
            acManagerGetMethod = 
                Class.forName("org.objectweb.jac.core.ACManager").getMethod("get",new Class[0]);
            acManagerGetObjectMethod =
                Class.forName("org.objectweb.jac.core.ACManager").getMethod(
                    "getObject",new Class[]{String.class});
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    Object getAC(String name) {
        Object ac = null;
        try {
            if (acManager==null) {
                acManager = 
                    (ACManager)acManagerGetMethod.invoke(null,ExtArrays.emptyObjectArray);
            }
            ac = acManagerGetObjectMethod.invoke(acManager,new Object[]{name});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ac;
    }

    boolean isRegisteredAC( String name ) {
        return getAC(name)!=null;
    }

    static Method collabGetMethod = null;
    static Method collabGetCurACMethod = null;

    String getCurAC() {
        /*
          Object acName = null;
          try {
          Class collaboration = Class.forName("org.objectweb.jac.core.Collaboration");
          if (collabGetMethod==null) {
          collabGetMethod = collaboration.getMethod("get",ExtArrays.emptyClassArray);
          }
          Object coll = collabGetMethod.invoke(null,ExtArrays.emptyObjectArray);
          if (collabGetCurACMethod==null) {
          collabGetCurACMethod = 
          coll.getClass().getMethod("getCurAC",ExtArrays.emptyClassArray);
          }
          acName = collabGetCurACMethod.invoke(coll,ExtArrays.emptyObjectArray);
          } catch (Exception e) {
          e.printStackTrace();
          }
          return (String)acName;
        */
        return null;
    }

    static AttributeController accessController = null;

    /**
     * Registers a new access controller for this application.
     *
     * @param controller the controller object
     */
    public static void registerAccessController(AttributeController controller) {
        accessController = controller;
    }

    Object controlledAccess(Object substance, String name, Object value) {
        if ( accessController==null || 
             ! (name.equals("GuiAC.VISIBLE") ||
                name.equals("GuiAC.EDITABLE") ||
                name.equals("GuiAC.ADDABLE") ||
                name.equals("GuiAC.CREATABLE") ||
                name.equals("GuiAC.REMOVABLE")) ) 
            return value;

        Object result = null;
        try {
            result = accessController.controlAttribute(substance,this,name,value);
        } catch(Exception e) {
            e.printStackTrace();
            return value;
        }
        return result;
    }


    /**
     * Gets the value of an attribute.
     *
     * @param name the name of the attribute
     * @return the value of the attribute
     */

    public Object getAttribute(String name) {
        return getAttribute(name,false);
    }

    /**
     * Gets the value of an attribute even if the aspect if not yet
     * configured and weaved.
     *
     * @param name the name of the attribute
     * @return the value of the attribute
     */
    public Object getAttributeAlways(String name) {
        return getAttribute(name,true);
    }

    /**
     * Gets the value of an attribute.
     *
     * @param name the name of the attribute
     * @param always if true, return a value even the aspect is not weaved
     * @return the value of the attribute
     *
     */
    public Object getAttribute(String name, boolean always) {
        String acName = (String)attrACs.get(name);
        if (!always && (acName!=null && (!isRegisteredAC(acName))) ) {
            return null;
        }
        Object value=attributes.get(name);
        if (value==null && itemClass!=null) {
            value = itemClass.getAttribute(name);
        }
        return controlledAccess(null,name,value);
    }

    /**
     * Gets the value of a boolean attribute.
     *
     * @param name the name of the attribute
     * @param defValue default value for the attribute if it is not set
     * @return the value of the attribute
     */
    public boolean getBoolean(String name, boolean defValue) {
        Boolean value = (Boolean)getAttribute(name);
        if (value==null)
            return defValue;
        else
            return value.booleanValue();
    }

    /**
     * Gets the value of an attribute.
     *
     * @param substance
     * @param name the name of the attribute
     * @return the value of the attribute
     */

    public Object getAttribute(Object substance, String name) {
        String acName = (String)attrACs.get(name);
        if( acName!=null && (!isRegisteredAC(acName)) ) {
            return null;
        }
        Object value=attributes.get(name);
        if (value==null && itemClass!=null) {
            value = itemClass.getAttribute(name);
        }
        return controlledAccess(substance,name,value);
    }

    /**
     * Sets the value of an attribute.
     *
     * @param name the name of the attribute
     * @param value the value of the attribute
     */

    public final void setAttribute(String name, Object value) {
        if (name == null) 
            return;
        String acName = (String)getCurAC();
        if (acName != null) {
            if ( !attrACs.containsKey(name) ) {
                attrACs.put(name,acName);
            }
        }
        attributes.put(name,value);
    }

    /**
     * Unsets the value of an attribute.
     *
     * @param name the name of the attribute to unset
     */

    public void unsetAttribute(String name) {
        attributes.remove(name);
    }

    /**
     * This method gets the name of the meta item by delegating to the
     * actual <code>java.lang.reflect</code> meta item.<p>
     *
     * @return the item name */

    public abstract String getName();

    MetaItem itemClass;
    public void setItemClass(MetaItem itemClass) {
        this.itemClass = itemClass;
    }
    public MetaItem getItemClass() {
        return itemClass;
    }
}
