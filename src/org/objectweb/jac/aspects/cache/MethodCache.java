/*
  Copyright (C) 2004 Laurent Martelli <laurent@aopsys.com>

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
import org.apache.log4j.Logger;
import org.objectweb.jac.aspects.timestamp.Timestamps;
import org.objectweb.jac.util.ObjectArray;

public class MethodCache extends Hashtable {
    static final Logger logger = Logger.getLogger("cache");

    public MethodCache(Timestamps stamps) {
        this.stamps = stamps;
    }

    Timestamps stamps;
    public Entry getEntry(ObjectArray args, int[] ignoredArgs) {
        Object[] argsArray = args.getArray();
        if (ignoredArgs!=null) {
            for (int i=0; i<ignoredArgs.length; i++) {
                argsArray[ignoredArgs[i]] = null;
            }
        }
        Entry entry = (Entry)get(args);
        if (entry!=null) {
            logger.debug("  cache hit "+args.hashCode());
            if (stamps!=null) {
                int argCount = argsArray.length;
                long entryTime = entry.time;
                int i;
                for (i=0; i<argCount; i++) {
                    Object arg = argsArray[i];
                    if (arg!=null) {
                        long mtime = stamps.getStamp(arg);
                        if (mtime > entryTime) {
                            logger.debug(
                                "  outdated for arg "+i+"("+arg+"): "+
                                mtime+">"+entryTime);
                            break;
                        }
                    }
                }
                if (i==argCount) // we didn't break in the middle
                    return (Entry)get(args);
                else
                    return null;
            } else {
                return ((Entry)get(args));
            }
        } else {
            logger.debug("  cache miss "+args.hashCode());
            return null;
        }
    }

    public void putEntry(ObjectArray args, Object value) {
        put(args,new Entry(value));
    }

    public static class Entry {
        long time;
        public Object value;
        public Entry(Object value) {
            this.value = value;
            this.time = System.currentTimeMillis();
        }
    }
}
