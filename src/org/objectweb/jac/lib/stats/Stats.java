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
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA */

package org.objectweb.jac.lib.stats;

import java.util.Collection;
import java.util.Iterator;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.rtti.FieldItem;

/**
 * This class contains static methods to do statistical computations
 * on collections.
 */
public class Stats {
    static Logger logger = Logger.getLogger("stats");

    /**
     * Compute average, min and max of a the field of collection's items
     *
     * @param stats store result in this structure. Values are not reset to zero.
     * @param items the items to compute the stats on
     * @param field the numerical field (float, double or int) to compute the stats of
     * @return an object containing the average, max and min of field
     * in items. If there's no item in the collection all values are 0.0
     */
    public static void computeStats(Stat stats, Collection items, FieldItem field) {
        double average = 0;
        double sum = 0;
        double max = 0;
        double min = 0;
        long count = 0;
        long pos = 0;
        Iterator it = items.iterator();
        while (it.hasNext()) {
            Object item = it.next();
            if (item!=null) {
                double value = ((Number)field.getThroughAccessor(item)).doubleValue();
                stats.sum += value;
                if (count==0 || value<stats.min)
                    stats.min = value;
                if (count==0 || value>stats.max) 
                    stats.max = value;
                stats.count++;
            } else {
                logger.error("computeStats "+field+": null element in collection at position "+pos);
            }
            pos++;
        }
    }

    public static Stat computeStats(Collection items, FieldItem field) {
        Stat stat = new Stat();
        computeStats(stat,items,field);
        return stat;
    }
}
