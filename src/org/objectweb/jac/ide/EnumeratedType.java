/*
  Copyright (C) 2002-2003 Laurent Martelli <laurent@aopsys.com>

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 2 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  USA */

package org.objectweb.jac.ide;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Vector;


/**
 * This class represents an enumerated type. Enumrated types allow to
 * define a set of allowed values for an integer.
 */
public class EnumeratedType extends Type {

   public EnumeratedType() {
   }

   public String getGenerationName() {
      return "int";
   }

   public String getGenerationFullName() {
      return "int";
   }

   /** first integer value */
   int startValue = 0;
   public void setStartValue(int startValue) {
      this.startValue = startValue;
   }
   public int getStartValue() {
      return startValue;
   }

   /** step separating each value */
   int step = 1;
   public int getStep() {
      return step;
   }
   public void setStep(int  v) {
      this.step = v;
   }

   /** Name of values */
   List names = new Vector();
   public void addName(String name) {
      names.add(name);
   }
   public void removeName(String name) {
      names.remove(name);
   }
   public List getNames() {
      return names;
   }

   /**
    * Imports names from a file (one name per line)
    * @param source file to import from
    */
   public void importFromFile(File source) throws IOException {
      BufferedReader reader = new BufferedReader(new FileReader(source));
      String line;
      while ((line=reader.readLine())!=null) {
         line = line.trim();
         if (!line.equals(""))
            addName(line);
      }
   }

   /**
    * Gets the value associated to a name. Raises an exception if the
    * name does not exists.
    * @param name the name
    * @return the integer value associated with the name
    */
   public int nameToValue(String name) throws Exception {
      int value = startValue;
      for (int i=0; i<names.size(); i++) {
         if (names.get(i).equals(name))
            return value;
         value += step;
      }
      throw new Exception("No such name \""+name+"\" in "+name);
   }

   /**
    * Gets the name associated with a value. Raises an exception if the
    * there's no name with such a value.
    * @param value the value
    * @return the name value associated with the value
    */
   public String valueToName(int value) throws Exception {
      if ((value-startValue)%step != 0) 
         throw new Exception("No such value "+value+" in "+name);
      return (String)names.get((value-startValue)/step);
   }
}
