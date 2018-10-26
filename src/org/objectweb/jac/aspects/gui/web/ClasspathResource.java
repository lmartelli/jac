/*
  Copyright (C) 2001-2003 Laurent Martelli <laurent@aopsys.com>

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

package org.objectweb.jac.aspects.gui.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import org.apache.log4j.Logger;
import org.mortbay.util.Resource;
import org.objectweb.jac.util.ExtArrays;
 
public class ClasspathResource extends Resource {
    static Logger logger = Logger.getLogger("web");

    long lastModified = new Date().getTime();
    String path;
    URLConnection connection;
    File file;
    boolean isFile;
    public ClasspathResource(String path) throws IOException {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        this.path = path;
        URL url = getClass().getClassLoader().getResource(path);
        if (url!=null) {
            file = new File(url.getFile());
            connection = url.openConnection();
            if (connection instanceof JarURLConnection) {
                file = new File(((JarURLConnection)connection).getJarFileURL().getFile());
            } 
            logger.debug("New classpath resource: "+url+" isFile="+file.isFile());
        } else {
            //throw new RuntimeException("Resource not found: "+path);
            logger.debug("Resource not found: "+path);
        }
    }
    public ClasspathResource() {
        path = null;
    }
    public void release() {
    }
    public boolean exists() {
        boolean exists =  isFile ? file.exists() : getInputStream()!=null;
        logger.debug("exists "+path+"? -> "+exists);
        return exists;            
    }
    public boolean isDirectory() {
        return false;
    }
    public long lastModified() {
        if (file.exists())
            return file.lastModified();
        else
            return lastModified;
    }
    public long length() {
        try {
            if (isFile) {
                return file.length();
            } else {
                InputStream is = getInputStream();
                if (is!=null)
                    return getInputStream().available();
                else 
                    return 0;
            }
        } catch(Exception e) {
            logger.error("Failed to get length of resource: "+path,e);
            return 0;
        }
    }
    public URL getURL() {
        return null;
    }
    public File getFile() {
        return file;
    }
    public String getName() {
        return path;
    }
    public InputStream getInputStream() {
        logger.debug("getInputStream "+file);
        if (isFile) {
            try {
                return new FileInputStream(file);
            } catch (IOException e) {
                logger.error("getInputStream "+path,e);
                return null;
            }
        } else {
            return getClass().getClassLoader().getResourceAsStream(path);
        }
    }
    public OutputStream getOutputStream() {
        return null;
    }
    public boolean delete() {
        return false;
    }
    public boolean renameTo(Resource newName) {
        return false;
    }
    public String[] list() {
        return ExtArrays.emptyStringArray;
    }
    public Resource addPath(String addedPath) throws IOException {
        if (path==null)
            return new ClasspathResource(addedPath);
        else 
            return this;
    }
    public String toString() {
        return path;
    }

}
