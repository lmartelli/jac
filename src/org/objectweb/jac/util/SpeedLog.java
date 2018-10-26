/*
  Copyright (C) 2004 Laurent Martelli <laurent@aopsys.com>

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

package org.objectweb.jac.util;

import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import net.sf.just4log.JustLog;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.objectweb.jac.util.File;
import org.objectweb.jac.util.Files;

/**
 * Command line tool to perform log optimization with just4log
 */
public class SpeedLog {
    /**
     * Usage: <code>java org.objectweb.jac.util.SpeedLog <dir> [<dir> ...]</code>
     *
     * <p>Recursively optimize the loger invocations of in all .class files.</p> 
     */
    public static void main(String[] args) throws IOException {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.WARN);

        for (int i=0; i<args.length; i++) 
        {
            File file = new File(args[i]);
            if (file.isDirectory()) {
                 List classes = 
                    file.listFilesRecursively(
                        Files.extensionFilenamFilter(".class"));
                Iterator it = classes.iterator();
                while(it.hasNext()) {
                    File classFile = (File)it.next();
                    try {
                        JustLog.speedup(classFile,classFile);
                    } catch(Exception e) {
                        System.out.println("Failed to speed "+classFile);
                    }
                }
            } else {
                try {
                    JustLog.speedup(file,file);
                } catch(Exception e) {
                    System.out.println("Failed to speed "+file);
                }
            }
        }
    }
}
