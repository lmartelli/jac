/*
  Copyright (C) 2001-2002 Laurent Martelli <laurent@aopsys.com>

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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * This class contains some utility functions for streams.
 */
public class Streams {

    /**
     * Reads an input stream until it reaches the end and store the data
     * in an array of bytes.
     *
     * <p> this method was extracted "as is" from javassist 1.0
     * from Shigeru Chiba.
     *
     * <p><a href="www.csg.is.titech.ac.jp/~chiba/javassist/">Javassist
     * Homepage</a>
     * 
     * @param fin the input stream to read the class from
     * @return the contents of that input stream 
     * @throws IOException if the size of the file is equal or more than 1Mbyte.
     */

    public static byte[] readStream(InputStream fin) throws IOException {
        byte[][] bufs = new byte[8][];
        int bufsize = 4096;
	
        for (int i = 0; i < 8; ++i) {
            bufs[i] = new byte[bufsize];
            int size = 0;
            int len = 0;
            do {
                len = fin.read(bufs[i], size, bufsize - size);
                if (len >= 0)
                    size += len;
                else {
                    byte[] result = new byte[bufsize - 4096 + size];
                    int s = 0;
                    for (int j = 0; j < i; ++j) {
                        System.arraycopy(bufs[j], 0, result, s, s + 4096);
                        s = s + s + 4096;
                    }
		    
                    System.arraycopy(bufs[i], 0, result, s, size);
                    return result;
                }
            } while (size < bufsize);
            bufsize *= 2;
        }
        throw new IOException("readStream function has too much data to load.");
    }

    /**
     * Reads an unsigned integer in little endian encoding
     * @param in the stream to read the integer from
     */
    public static long readUInt(InputStream in) throws IOException {
        long s1 = readUShort(in);
        long s2 = readUShort(in);
        return (s2 << 16) | s1;
    }
   
    /**
     * Reads unsigned short in little endian encoding
     * @param in the stream to read the short integer from
     */
    public static  int readUShort(InputStream in) throws IOException {
        int b1 = readUByte(in);
        int b2 = readUByte(in);
        return (b2 << 8) | b1;
    }

    /**
     * Reads unsigned byte
     * @param in the stream to read the byte from
     */
    public static int readUByte(InputStream in) throws IOException {
        int b = in.read();
        if (b == -1)
            throw new EOFException();
        return b;
    }

    /**
     * Read data from an InputStream and writes it to an OutputStream
     */
    public static void copy(InputStream in, OutputStream out) 
        throws IOException 
    {
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int read;
        while ((read=in.read(buffer,0,bufferSize))!=-1) {
            out.write(buffer,0,read);
        }
    }
}
