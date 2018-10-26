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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.zip.GZIPInputStream;
import java.io.FileFilter;

/**
 * Various often used file functions
 */
public class Files {
    /**
     * Returns an input stream or a file. If the file is compressed
     * with gzip, it is decompressed.
     *
     * @param f the file to get an input stream for 
     */
    public static InputStream autoDecompressStream(File f) 
        throws FileNotFoundException, IOException 
    {
        InputStream in = new FileInputStream(f);
        if (Streams.readUShort(in) == GZIPInputStream.GZIP_MAGIC) {
            in.close();
            in = new GZIPInputStream(new FileInputStream(f));
        } else {
            in.close();
            in = new FileInputStream(f);
        }
        return in;
    }

    /**
     * Returns a reader or a file. If the file is compressed with gzip, it is decompressed. 
     *
     * @param f the file to get an input stream for 
     * @param encoding charset encoding to use for the Reader
     */
    public static Reader autoDecompressReader(File f, String encoding) 
        throws FileNotFoundException, IOException 
    {
        return new InputStreamReader(autoDecompressStream(f),encoding);
    }

    /**
     * Creates a writer for a file with a specific encoding
     *
     * @param f the file to create a writer for
     * @param encoding the encoding of the file
     */
    public static Writer newFileWriter(File f, String encoding) 
        throws FileNotFoundException, UnsupportedEncodingException 
    {
        return new OutputStreamWriter(new FileOutputStream(f),encoding);
    }

    /**
     * Creates a FilenameFilter which matches files whose name end
     * with a particular extension
     *
     * @param extension the extension
     * @return a FilenameFilter
     */
    public static FilenameFilter extensionFilenamFilter(final String extension) {
        return 
            new FilenameFilter() {
                    public boolean accept(java.io.File file, String name) {
                        return name.endsWith(extension);
                    }
                };
    }

    /**
     * Replaces leading ~ by the user's home directory
     * @param path file path to expand
     */
    public static String expandFileName(String path) {
        if (path.startsWith("~")) {
            return System.getProperty("user.home") + path.substring(1);
        } else {
            return path;
        }
    }

    /** A filter to list only directories */
    public static final FileFilter directoryFilter = 
        new FileFilter() {
                public boolean accept(File f) {
                    return f.isDirectory();
                }
            };

    /** A filter to list only non hidden files */
    public static final FileFilter nonHiddenFilter = 
        new FileFilter() {
                public boolean accept(File f) {
                    return !f.isHidden();
                }
            };

    public static File[] listDirectories(File dir) {
        return dir.listFiles(directoryFilter);
    }

    public static File[] listNonHiddenFiles(File dir) {
        return dir.listFiles(nonHiddenFilter);
    }
}
