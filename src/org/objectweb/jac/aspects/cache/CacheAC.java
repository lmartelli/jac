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

import java.util.Iterator;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.AspectComponent;
import org.objectweb.jac.core.rtti.AbstractMethodItem;

/**
 * This aspect handle caching of method results.
 */

public class CacheAC extends AspectComponent implements CacheConf {
    static final Logger logger = Logger.getLogger("cache");

    public static final String IGNORED_PARAMETERS = "CacheAC.IGNORED_PARAMETERS";

    /**
     * Cache a method result.
     *
     * @param classExpr the classes
     * @param methodExpr the methods cached
     */
    public void cache(String classExpr, String methodExpr)
    {
        logger.info("cache "+classExpr+"."+methodExpr);

        pointcut("ALL",classExpr,methodExpr,
                 "org.objectweb.jac.aspects.cache.CacheWrapper",
                 null,NOT_SHARED);
    }

    public void cacheWithTimeStamps(
        String classExpr, String methodExpr,
        String stampsName)
    {
        logger.info("cache "+classExpr+"."+methodExpr);

        pointcut(
            "ALL", classExpr, methodExpr,
            "org.objectweb.jac.aspects.cache.CacheWrapper",
            new Object[] {stampsName},
            null,
            NOT_SHARED);
    }

    public void setIgnoredParameters(AbstractMethodItem method, 
                                     int[] ignored) 
    {
        method.setAttribute(IGNORED_PARAMETERS,ignored);
    }

    public void invalidateCache() {
        Iterator i = wrappers.iterator();
        while(i.hasNext()) {
            CacheWrapper wrapper = (CacheWrapper)i.next();
            wrapper.invalidate();
        }
    }
}
