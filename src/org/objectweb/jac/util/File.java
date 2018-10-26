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

import java.io.FilenameFilter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Provides recursive file listing, and replaces leading ~ by the
 * user's home directory.  
 */
public class File extends java.io.File {
    public File(java.io.File file) {
        this(file.getPath());
    }
    public File(String pathname) {
        super(Files.expandFileName(pathname));
    }
    public File(File parent, String child) {
        this(parent.getPath(),child);
    }
    public File(java.io.File parent, String child) {
        this(parent.getPath(),child);
    }
    public File(String parent, String child) {
        super(Files.expandFileName(parent),child);
    }

    /**
     * Recursively list files matching a filter
     * @param filter list files matching this filter
     * @return a List of File matching the filter
     * @see #listFilesRecursively(FilenameFilter,List)
     */
    public List listFilesRecursively(FilenameFilter filter) {
        LinkedList files = new LinkedList();
        listFilesRecursively(filter,files);
        return files;
    }

    /**
     * Recursively list files matching a filter
     * @param filter list files matching this filter
     * @param files add matching files to this list
     * @see #listFilesRecursively(FilenameFilter)
     */
    public void listFilesRecursively(FilenameFilter filter, List files) {
        String[] names = list();
        if(names==null) return;
        for (int i=0; i<names.length; i++) {
            File file = new File(this,names[i]);
            if (filter.accept(this,names[i]))
                files.add(file);
            if (file.isDirectory())
                file.listFilesRecursively(filter,files);
        }
    }

    /**
     * Gets a path relative to a parent directory of the file. 
     *
     * @param parent the directory to give a path relative to
     * @return a path relative to parent, or getPath() if parent is
     * not a parent of the file.
     */
    public String getRelativePath(File parent) throws IOException {
        String parentPath = parent.getCanonicalPath();
        String path = getCanonicalPath();
        if (path.startsWith(parentPath)) {
            return path.substring(parentPath.length()+1);
        } else {
            return getPath();
        }
    }

    public java.io.File[] listDirectories() {
        return listFiles(Files.directoryFilter);
    }

    public java.io.File[] listNonHiddenFiles() {
        return listFiles(Files.nonHiddenFilter);
    }
}

