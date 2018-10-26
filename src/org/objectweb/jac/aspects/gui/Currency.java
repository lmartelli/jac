/*
  Copyright (C) 2001-2002 Laurent Martelli
  
  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 2 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  USA */

package org.objectweb.jac.aspects.gui;

/**
 * Holds information about a currency: its name, display precision
 * and change rate.
 */
public class Currency 
{
   String name;
   int precision;
   double rate;

   /**
    * Constructs the currency.
    *
    * @param name the currency name
    * @param precision the currency precision (decimal number)
    * @param rate the exchange rate regarding the default currency */

   public Currency(String name, int precision, double rate) {
      this.name = name;
      this.precision = precision;
      this.rate = rate;
   }
   
   /**
    * Gets the currency name. */
   public String getName() {
      return name;
   }

   /**
    * Gets the currency precision. */
   public int getPrecision() {
      return precision;
   }
   
   /**
    * Gets the exchange rate regarding the default currency. */
   public double getRate() {
      return rate;
   }
}
