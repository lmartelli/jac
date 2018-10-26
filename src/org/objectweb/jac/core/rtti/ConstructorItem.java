/*
  Copyright (C) 2001-2004 Renaud Pawlak, Laurent Martelli
  
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


import java.lang.NoSuchMethodException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.apache.log4j.Logger;
import org.objectweb.jac.util.WrappedThrowableException;

/**
 * This class defines a meta item that corresponds to the
 * <code>java.lang.reflect.Constructor</code> meta element.<p>
 *
 * @author Laurent Martelli
 * @author Renaud Pawlak
 */

public class ConstructorItem extends AbstractMethodItem {
    static Logger logger = Logger.getLogger("rtti.method");

    /**
     * Transforms a constructor items array into a constructors array
     * containing the <code>java.lang.reflect</code> constructors
     * wrapped by the constructor items.<p>
     *
     * @param constructorItems the Constructor items
     * @return the actual Constructors in <code>java.lang.reflect</code> */

    public static Constructor[] toConstructors( ConstructorItem[] constructorItems ) {
        Constructor[] res = new Constructor[constructorItems.length];
        for ( int i = 0; i < constructorItems.length; i++ ) {
            if ( constructorItems[i] == null ) {
                res[i] = null;
            } else {
                res[i] = constructorItems[i].getActualConstructor();
            }
        }
        return res;
    }

   
    /**
     * Default contructor to create a new constructor item object.<p>
     *
     * @param delegate the <code>java.lang.reflect.Constructor</code> actual
     * meta item */

    public ConstructorItem(Constructor delegate, ClassItem parent) 
        throws InvalidDelegateException 
    {
        super(delegate,parent);
        try {
            delegate.getDeclaringClass().getDeclaredMethod(
                "_org_"+NamingConventions.getShortClassName(delegate.getDeclaringClass()),
                delegate.getParameterTypes());
        } catch(NoSuchMethodException e) {
            //Log.warning("No _org_ method found for "+this);
        }
    }

    /**
     * Get the constructor represented by this constructor item.<p>
     *
     * @return the actual constructor
     */

    public Constructor getActualConstructor() {
        return (Constructor) delegate;
    }

    public String getName() {
        return NamingConventions.getShortConstructorName(this);
    }

    public Class getType() {
        return getParent().getType();
    }

    /**
     * Creates a new instance of the class item that is parent of this
     * constructor item.
     *
     * @param params the parameters needed by this constructor (see the
     * types)
     * @see #getParameterTypes() */

    public Object newInstance(Object [] params) 
        throws InstantiationException, IllegalAccessException, InvocationTargetException 
    {
        return getActualConstructor().newInstance(params);
    }

    /**
     * A nice way to construct a new instance when the constructor does
     * not take any arguements (it throws an exception if it is not the
     * case).
     *
     * @see #getParameterTypes() */

    public Object newInstance() 
        throws InstantiationException, IllegalAccessException, InvocationTargetException 
    {
        if (getClassItem().isAbstract())
            throw new InstantiationException("Cannot instantiate abstract class "+getClassItem());
        return getType().newInstance();
    }

    public Object invoke(Object substance, Object[] params) {
        try {
            return newInstance(params);
        } catch (Exception e) {
            logger.info("Failed to invoke "+this,e);
            throw new WrappedThrowableException(e);
        }
    }

    public Class[] getParameterTypes() {
        return ((Constructor)delegate).getParameterTypes();
    }

    public String toString() {
        if (delegate!=null)
            return getFullName();
        else 
            return "???";
    }

    public String getFullName() {
        String ret = NamingConventions.getShortConstructorName(this) + "(";
        Class[] pts = getParameterTypes();
        for ( int i = 0; i < pts.length; i++ ) {
            ret = ret + NamingConventions.getStandardClassName(pts[i]);
            if ( i < pts.length - 1 ) ret = ret + ",";
        }
        ret = ret + ")";
        return ret;
    }

    public final boolean isAdder() {
        return false;
    }   
    public final CollectionItem[] getAddedCollections() {
        return CollectionItem.emptyArray;
    }
    public final CollectionItem[] getRemovedCollections() {
        return CollectionItem.emptyArray;
    }

    public final boolean isSetter() { return false; }
    public final boolean isRemover() { return false; }   
    public final boolean isAccessor() { return false; }
    public final boolean isWriter() { return false; }
    public final boolean isGetter() { return false; }

    public final boolean isFieldGetter() { return false; }
    public final boolean isFieldSetter() { return false; }

    public final boolean isReferenceGetter() { return false; }
    public final boolean isReferenceSetter() { return false; }
    public final boolean isReferenceAccessor() { return false; }

    public final boolean isCollectionGetter() { return false; }
    public final boolean isCollectionSetter() { return false; }
    public final boolean isCollectionAccessor() { return false; }

    public final FieldItem getSetField() {
        return null;
    }
   
}// ConstructorItem
