/*
  Copyright (C) 2003 Laurent Martelli <laurent@aopsys.com>

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

import java.io.IOException;
import java.io.Writer;

/**
 * Generation state for acc configuration code genaration. It
 * memorizes if a class or attribute block is opened or not.
 */
public class AccGenState {
   /** Is a "class" block opened ? */
   boolean classOpened = false;
   /** Is an "attribute" block opened ? */
   boolean fieldOpened = false;
   Writer output;

   public AccGenState(Writer output) {
      this.output = output;
   }

   /**
    * Start a "class" block
    */
   void openClass(Class cl) 
      throws IOException
   {
      if (!classOpened) {
         output.write("class "+cl.getGenerationFullName()+" {\n");
         classOpened = true;
      }
   }
   /**
    * End a "class" block if any
    */
   void closeClass() 
      throws IOException
   {
      if (classOpened) {
         classOpened = false;
         write("}\n");
      }
   }

   /**
    * Start a "attribute" block
    */
   void openField(Class cl, Field field) 
      throws IOException
   {
      openClass(cl);
      if (!fieldOpened) {
         write("attribute "+field.getGenerationName()+" {\n");
         fieldOpened = true;
      }
   }

   /**
    * Start a "attribute" block
    */
   void openRole(Class cl, RelationRole role) 
      throws IOException
   {
      openClass(cl);
      if (!fieldOpened) {
         write("attribute "+role.getGenerationName()+" {\n");
         fieldOpened = true;
      }
   }

   /**
    * Start a "attribute" block
    */
   void openMethod(Class cl, Method method) 
      throws IOException
   {
      openClass(cl);
      if (!fieldOpened) {
         write("method \""+method.getUniqueName()+"\" {\n");
         fieldOpened = true;
      }
   }
   /**
    * End a "attribute" block if any
    */
   void closeMember() 
      throws IOException
   {
      if (fieldOpened) {
         fieldOpened = false;
         write("}\n");
      }
   }

   /**
    * Write some text to output with correct indentation
    * @param text text to write to output
    */
   void write(String text) throws IOException {
      if (classOpened)
         output.write("    ");
      if (fieldOpened)
         output.write("    ");
      output.write(text);
   }
}
