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

  You should have received a copy of the GNU Lesser General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA */

package org.objectweb.jac.util;

import java.io.Reader;
import java.io.IOException;


/**
 * A PushbackReader which keeps track of the position in the stream
 *
 * @see #getPosition()
 */

public class PushbackReader extends java.io.PushbackReader
{
   /** current position in stream */
   int position;

   public PushbackReader(Reader in) {
      super(in);
   }

   /**
    * @param in reader to read from
    * @param size bushback buffer size
    * @see java.io.PushbackReader#PushbackReader(Reader,int)
    */
   public PushbackReader(Reader in, int size) {
      super(in,size);
   }

   public int read() throws IOException {
      int c = super.read();
      position++;
      return c;
   }

   public int read(char cbuf[], int off, int len) throws IOException
   {
      int nbRead = super.read(cbuf,off,len);
      if (nbRead!=-1) {
         position += nbRead;
      }
      return nbRead;
   }

    public void unread(int c) throws IOException {
       super.unread(c);
       position--;
    }

    public void unread(char cbuf[], int off, int len) throws IOException {
       super.unread(cbuf,off,len);
       position -= len;
    }

   /**
    * Returns the position in the stream
    */
   public int getPosition() {
      return position;
   }
}
