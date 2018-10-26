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

import java.util.Hashtable;
import org.aopalliance.intercept.ConstructorInvocation;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.objectweb.jac.aspects.timestamp.Timestamps;
import org.objectweb.jac.core.AspectComponent;
import org.objectweb.jac.core.Interaction;
import org.objectweb.jac.core.NameRepository;
import org.objectweb.jac.core.Wrapper;
import org.objectweb.jac.util.ObjectArray;

/**
 * The wrapper of the CacheAC.
 * @see CacheAC
 */
public class CacheWrapper extends Wrapper {
    static final Logger logger = Logger.getLogger("cache");

    public CacheWrapper(AspectComponent ac) {
        super(ac);
    }

    public CacheWrapper(AspectComponent ac, String stampsName) {
        super(ac);
        this.stampsName = stampsName;
    }

    String stampsName;
    Timestamps stamps;

    public Object invoke(MethodInvocation invocation) throws Throwable {
		return cache((Interaction)invocation);
    }
   
    Hashtable cache = new Hashtable();
   
    public Object cache(Interaction interaction) {
        logger.debug("cache "+interaction+"?");
        // Synchronization is not crucial: we may only loose cached value
        MethodCache methodCache = (MethodCache)cache.get(interaction.method);
        if (methodCache==null) {
            if (stampsName!=null) {
                if (stamps == null)
                    stamps = (Timestamps)NameRepository.get().getObject(stampsName);
            }
            methodCache = new MethodCache(stamps);
            cache.put(interaction.method,methodCache);
        }
        Object[] argsArray = new Object[interaction.args.length];
        System.arraycopy(interaction.args, 0, argsArray, 0, interaction.args.length);
        int[] ignoredArgs = (int[])interaction.method.getAttribute(CacheAC.IGNORED_PARAMETERS);
        ObjectArray args = new ObjectArray(argsArray);

        MethodCache.Entry entry = methodCache.getEntry(args,ignoredArgs);
        if (entry!=null) {
            return entry.value;
        }
        Object result = proceed(interaction);
        methodCache.putEntry(args,result);
        return result;
    }

    /**
     * Invalidates the cache 
     */
    void invalidate() {
        cache.clear();
    }

}
