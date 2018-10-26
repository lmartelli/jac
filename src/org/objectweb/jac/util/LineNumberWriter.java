/*

  Copyright (C) 2001 Laurent Martelli

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

package org.objectweb.jac.util;

import java.io.FilterWriter;
import java.io.Writer;
import java.io.IOException;

/**
 * A writer which counts the numbers of lines that it writes.
 *
 * @see #getLines()
 */

public class LineNumberWriter extends FilterWriter
{
   // last read byte
   int last = -1;
   int lines = 1;

   public LineNumberWriter(Writer out) {
      super(out);
   }

   public void write(int b) throws IOException {
      if ( (b == '\r' && last != '\n') || 
           (b=='\n') ) {
         lines++;
      }
      out.write(b);
      last = b;
   }

   public void write(char[] b, int off, int len) throws IOException
   {
      while (len-- > 0) {
         write(b[off++]);
      }
   }

   public void write(String str, int off, int len) throws IOException
   {
      while (len-- > 0) {
         write(str.charAt(off++));
      }
   }

   /**
    * Returns the number of lines written
    */
   public int getLines() {
      return lines;
   }
}

