/*
  Copyright (C) 2002 Laurent Martelli <laurent@aopsys.com>

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

package org.objectweb.jac.aspects.cache;

import org.objectweb.jac.core.rtti.AbstractMethodItem;

/**
 * This aspect handle caching of method results.
 */

public interface CacheConf {
    /**
     * Specifies that the result of some methods should be cache. 
     *
     * <p>If cached method is called twice on the same object with the
     * same parameters, (according to equals()), the method won't be
     * called the second time, and the result of the first invocation
     * will be returned.</p>
     *
     * @param classExpr which classes' method to cache
     * @param methodExpr which methods to cache 
     *
     * @see #cacheWithTimeStamps(String,String,String)
     */
    void cache(String classExpr, String methodExpr);

    /**
     * Specifies that the result of some methods should be cache. 
     *
     * <p>Same as <code>cache()</code>, but the cache is invalidated
     * if one of the parameters changed (according to the timestamp
     * aspect) since the cached value was stored.</p>
     *
     * @param classExpr which classes' method to cache
     * @param methodExpr which methods to cache 
     * @param stampsName name of the timestamp repository object to
     * use (e.g "timestamps#0")
     *
     * @see #cache(String,String)
     * @see org.objectweb.jac.aspects.timestamp.TimestampConf */
    void cacheWithTimeStamps(
        String classExpr, String methodExpr,
        String stampsName);
    
    /**
     * Tells the cache aspect that some parameters of a method should
     * be ignored for all cache operations.
     *
     * <p>As far as the cache is concerned, they will be null.</p>
     *
     * @param method the method to configure
     * @param ignored the indexes of parameters to be ignored
     * (starting at 0) 
     */
    void setIgnoredParameters(AbstractMethodItem method, int[] ignored);

}
