/*
  Copyright (C) 2001 Renaud Pawlak <renaud@aopsys.com>

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

package org.objectweb.jac.wrappers;

import org.aopalliance.intercept.ConstructorInvocation;
import org.aopalliance.intercept.MethodInvocation;
import org.objectweb.jac.core.AspectComponent;
import org.objectweb.jac.core.Interaction;
import org.objectweb.jac.core.Wrapper;

import java.util.Vector;

/**
 * <code>CacheWrapper</code> implements a wrapper that caches the wrappee
 * methods results so that it can avoid the wrappee computations. It
 * can improve performances of complex computations.
 *
 * <p>Please make sure to use the rigth wrapping method.
 * 
 * <p>For example, if you have a <code>Matrix</code> class:
 *
 * <ul><pre>
 * public class Matrix {
 *   invert();
 *   ...
 * }
 * </pre></ul>
 * 
 * <p>An instance of <code>Matrix</code> must be wrapped with
 * <code>stateCache</code> since the result depends on the matrix
 * state:
 * 
 * <ul><pre>
 * CacheWrapper cw = new CacheWrapper();
 * a_matrix.wrap(cw, "stateCache", "invert");
 * </pre></ul>
 * 
 * <p>However, when the state of the matrix is modified, the cache
 * should be cleared since its values are not valid anymore. This can
 * be done by using the wrapping method <code>clearCache</code>.
 *
 * <ul><pre>
 * a_matrix.wrap(cw, "clearCache", new String[] { "set", "mul", "div", "add" } );
 * </pre></ul>
 */

public class CacheWrapper extends Wrapper {

   /** Store the keys for the cache. */
   protected Vector cacheKeys = new Vector();
   /** Store the cached values. */
   protected Vector cacheValues = new Vector();

   public CacheWrapper (AspectComponent ac) {
      super(ac);
   }
   
   /**
    * This wrapping method seeks in the cache wether the couple
    * (method, args) has already been called on the wrappee
    * object. If yes, it returns the value from the cache. Otherwise
    * it calls the wrappee object and memorizes the result in the
    * cache.
    *
    * <p>This wrapper can be used on objects that have complex, but
    * state independant computation to perform. For instance,
    * <code>cache</code> could wrap a computational object that would
    * be able to invert a matrix so that the inversion of a given
    * matrix would be done only once.
    *
    * <p>NOTE: if the wrappee function depends on the the object
    * state the values returned by the <code>statelessCache</code>
    * will be wrong. In this case, use the <cache>stateCache</cache>
    * wrapping method.
    * 
    * @see CacheWrapper#stateCache(Interaction)
    */
    
   public Object statelessCache(Interaction interaction) {
      Object[] key = new Object[] { interaction.method, interaction.args };
      int i;
      if((i = isCacheHit(key)) != -1) {
         return getCacheValue(i);
      }
      Object ret;
      setCacheValue(key, ret = proceed(interaction));
      return ret;
   }

   /**
    * This wrapping method seeks in the cache wether the triple
    * (wrappe, method, args) has already been called on the wrappee
    * object when wrappee was in the same state. If yes, it returns
    * the value from the cache. Otherwise it calls the wrappee object
    * and memorizes the result in the cache.
    *
    * <p>This wrapper can be used on objects that have complex state
    * dependant computation to perform. For instance,
    * <code>cache</code> could wrap the invert method of a matrix
    * object. If you invert this matrix three times, then the third
    * inversion result will be found in the cache.
    *
    * <p>NOTE: if the wrappee method computation does not depend on
    * the wrappee state, then you should use the
    * <code>statelessCache</code> method for better performance and
    * hits rate.
    * 
    * @see CacheWrapper#statelessCache(Interaction)
    */
    
   public Object stateCache(Interaction interaction) {
      System.out.println("-> Testing the cache...");
      Object[] key = new Object[] { interaction.method, interaction.args, 
                                    interaction.wrappee };
      int i;
      if((i = isCacheHit(key)) != -1) {
         System.out.println("-> Cache hit!");
         return getCacheValue(i);
      }
      System.out.println("-> Cache miss.");
      Object ret;
      setCacheValue(key, ret = proceed(interaction));
      return ret;
   }

   /**
    * Clear the cache.
    *
    * <p>This method should wrap all the methods that change the state
    * of the wrappee in a case of a state cache.
    *
    * @return the value returned by the wrapped method
    */

   public Object clearCache(Interaction interaction) {
      cacheKeys.clear();
      cacheValues.clear();
      return proceed(interaction);
   }

   /**
    * Add a (method, args) key and its value into the cache.
    *
    * @param key the key to find the value in the cache
    * @param value the cached value
    *
    * @see CacheWrapper#getCacheValue(Object[])
    */
   
   protected void setCacheValue(Object[] key, Object value) {
      cacheKeys.add(key);
      cacheValues.add(value);
   }

   /**
    * Get the memorized value of the method when called with the
    * given (method, args) key.
    *
    * @param key the key
    * @return the cached value that matches the key
    *
    * @see CacheWrapper#setCacheValue(Object[],Object)
    */
   protected Object getCacheValue(Object[] key) {
      return getCacheValue(isCacheHit(key));
   }

   /**
    * Get a cached value.
    *
    * @param index the index in the cache
    * @return the cached value
    */

   protected Object getCacheValue(int index) {
      if (index == -1) return null;
      return cacheValues.get(index);
   }

   /**
    * Returns the index of a key in the cache, -1 if not found.
    *
    * @param key a complex key
    * @return the location of the key
    */

   protected int isCacheHit(Object[] key) {
      for (int i = 0; i < cacheKeys.size(); i ++) {
         boolean same = true;
         Object[] cur_key = (Object[])cacheKeys.get(i);
         if (cur_key.length != key.length) {
            System.out.println("A");
            same = false;
            break;
         }  
         if(!cur_key[0].equals(key[0])) {
            System.out.println("B");
            same = false;
            break;
         }
         if (((Object[])cur_key[1]).length != ((Object[])key[1]).length) {
            System.out.println("C");
            same = false;
            break;
         }  
         for (int j = 0; j < ((Object[])cur_key[1]).length; j++) {
            if(!((Object[])cur_key[1])[j].equals(((Object[])key[1])[j])) {
               System.out.println("D" + ((Object[])cur_key[1])[j] + ((Object[])key[1])[j]);
               same = false;
               break;
            }
         }
         if (key.length == 3) {
            if(cur_key[2] != key[2]) {
               System.out.println("E");
               same = false;
               break;
            }
         }
         if (same) {
            return i;
         }   
      }
      return -1;
   }

/* (non-Javadoc)
 * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
 */
public Object invoke(MethodInvocation invocation) throws Throwable {
	// TODO Auto-generated method stub
	return null;
}

/* (non-Javadoc)
 * @see org.aopalliance.intercept.ConstructorInterceptor#construct(org.aopalliance.intercept.ConstructorInvocation)
 */
public Object construct(ConstructorInvocation invocation) throws Throwable {
	// TODO Auto-generated method stub
	return null;
}   

}





