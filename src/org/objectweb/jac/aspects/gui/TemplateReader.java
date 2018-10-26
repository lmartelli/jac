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

import java.io.*;
import java.util.Map;

/**
 * This is unused class to create printable templates.
 *
 * <p>Template Reader : a stream reader for Template
 * Substitute %param% with the value of template.getParams().get(param)
 **/

public class TemplateReader extends Reader {

   Map params;
   StringBuffer buffer;
   Reader in;

   /* The char used to delimitate params */
   int delim_char = '%';

   public TemplateReader(Reader in, Map params) 
   {
      this.in = in;
      this.params = params;
      this.buffer = new StringBuffer();
   }

   public int read(char[] b, int off, int len) throws IOException
   {
      /* copied from gnu/regexp/REFilterReader.java */
      int i;
      int ok = 0;
      while (len-- > 0) {
         i = read();
         if (i == -1) return (ok == 0) ? -1 : ok;
         b[off++] = (char) i;
         ok++;
      }
      return ok;
   }
   
   /* Read a single char */
   public int read() throws IOException 
   {
      if (buffer.length()==0) {
         int b = in.read();
         if (b != -1) {
            if (b == delim_char) {
               StringWriter param = new StringWriter();
               b = in.read();
               if (b != delim_char) {
                  while(b!=delim_char && b!=-1) {
                     param.write(b);
                     b = in.read();
                  }
                  buffer.append(params.get(param.toString()));
                  b = buffer.charAt(0);
                  buffer.deleteCharAt(0);
               }
            }
         } 
         return b;
      } else {
         int b = buffer.charAt(0);
         buffer.deleteCharAt(0);
         return b;
      }
   }

   public void close() throws IOException 
   {
      in.close();
   }
}
