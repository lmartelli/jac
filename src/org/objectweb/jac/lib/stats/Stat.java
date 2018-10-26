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

public class Stat {
   public Stat() {
   }

   public Stat(double sum, long count, double min, double max) {
      this.sum = sum;
      this.count = count;
      this.min = min;
      this.max = max;
   }

   double sum;
   public double getSum() {
      return sum;
   }
   public void setSum(double newSum) {
      this.sum = newSum;
   }

   long count;
   public long getCount() {
      return count;
   }
   public void setCount(long count) {
      this.count = count;
   }

   public double getAverage() {
      return count!=0 ? sum / count : Double.NaN;
   }

   double min;
   public double getMin() {
      return min;
   }
   public void setMin(double newMin) {
      this.min = newMin;
   }

   double max;
   public double getMax() {
      return max;
   }
   public void setMax(double newMax) {
      this.max = newMax;
   }
}
