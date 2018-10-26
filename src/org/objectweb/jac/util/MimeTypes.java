/*
  Copyright (C) 2002-2004 Laurent Martelli <laurent@aopsys.com>

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Vector;
import org.apache.log4j.Logger;

/**
 * Maps mime types to their extensions
 */
public class MimeTypes
{
    static final Logger logger = Logger.getLogger("mime");

    public MimeTypes() {
    }

    /**
     * Initialize with defaults builtin values
     */
    public void readDefaults() {
        String path = "org/objectweb/jac/util/mime.types";
        try {
            InputStream input = 
                this.getClass().getClassLoader().getResourceAsStream(path);
            if (input!=null)
                read(new InputStreamReader(input));
            else
                logger.warn("Resource not found: '"+path+"'");
        } catch (Exception e) {
            logger.error("Failed to read default mime.types from '"+path+"'",e);
        }
    }

    // extension -> mimetype
    Hashtable extensions = new Hashtable();
    // mimetype -> extension[]
    Hashtable types = new Hashtable();

    /**
     * Read mime types definitions from a stream. 
     *
     * <p>The format of the stream must be:</p>
     * <pre>mime-type [extension ...]</pre>
     * <p>Tabulations are not supported as separators!!!</p>
     */
    public void read(Reader in) throws IOException {
        BufferedReader reader = new BufferedReader(in);
        String line;
        while ((line=reader.readLine())!=null) {
            line = line.trim();
            int index = line.indexOf(' ');
            if (index!=-1) {
                String mimeType = line.substring(0,index).trim();
                Vector ext = new Vector();
                line = line.substring(index+1);
                while((index=line.indexOf(' '))!=-1) {
                    String extension = line.substring(0,index);
                    ext.add(extension);
                    extensions.put(extension,mimeType);
                    line = line.substring(index+1).trim();
                }
                ext.add(line);
                extensions.put(line,mimeType);
                String[] array = ExtArrays.emptyStringArray;
                types.put(mimeType,ext.toArray(array));
            }
        }
    }

    /**
     * Returns the mime type associated with the extension of a filename
     *
     * @return the mime type of null.
     */
    public String getMimeType(String filename) {
        String mimeType = null;
        int index = filename.lastIndexOf('.');
        if (index!=-1) {
            mimeType = (String)extensions.get(filename.substring(index+1));
        }
        return mimeType;
    }

    /**
     * Returns all known mime types
     */
    public Collection getMimeTypes() {
        return types.keySet();
    }
}
