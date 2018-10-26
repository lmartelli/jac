/*
  Copyright (C) 2002 Julien van Malderen

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

package org.objectweb.jac.util.preparse;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.IOException;

public class ParseInput extends BufferedReader
{
   String currentLine;
   boolean modified = false;

   public ParseInput(Reader in)
   {
      super(in);
   }

   public ParseInput(Reader in, int sz)
   {
      super(in, sz);
   }

   /**
    * Returns the next line from the input
    */
   public String readLine()
      throws IOException
   {
      if (modified)
         modified = false;
      else
         currentLine = super.readLine();
      return currentLine;
   }

   public boolean isModified()
   {
      return modified;
   }

   /**
    * Skips input until a token is found.
    * @param token skip until this string is found in the input
    */
   public String skipTo(int i, String token)
      throws IOException
   {
      //System.out.println("skipping until "+token);
      String result = new String();
      do
      {
         int lineLength = currentLine.length();
         
         int begin = i;
         int end;
         
         for (end = token.length() + i; end <= lineLength; begin++, end++)
         {
            if (currentLine.substring(begin, end).equals(token))
            {
               //result += currentLine.substring(i, end);
               currentLine = currentLine.substring(end);
               modified = true;
               return result;
            }
         }
         //System.out.println("skipping: "+currentLine);
         //result += currentLine.substring(i);
         result += '\n';
         i = 0;
      }
      while (readLine() != null);
      return result;
   }
}
