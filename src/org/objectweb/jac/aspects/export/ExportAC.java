/*
  Copyright (C) 2003-2004 Laurent Martelli <laurent@aopsys.com>

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

package org.objectweb.jac.aspects.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.HashSet;
import org.objectweb.jac.core.AspectComponent;
import org.objectweb.jac.core.NameRepository;

public class ExportAC extends AspectComponent implements ExportConf {
    HashSet roots = new HashSet();

    NameRepository nr;
    public ExportAC() {
        nr = (NameRepository)NameRepository.get();
    }

    /**
     * Declare some objects as roots to start exporting from.
     * @param nameExpr a regular expression matching the name of root objects
     */
    public void addRoot(String nameExpr) {
        roots.add(nameExpr);
    }

    HashSet allow = new HashSet();
    public void allowExport(String classExpr) {
        allow.add(classExpr);
    }

    HashSet deny = new HashSet();
    public void denyExport(String classExpr) {
        deny.add(classExpr);
    }

    public static final String DEFAULT_ENCODING = "UTF-8";

    /**
     * Exports all objects to a stream
     *
     * @param out stream to export to
     * @param encoding charset encoding to use (UTF-8 ,iso-8859-1,...)
     *
     * @see #export(OutputStream)
     * @see #export(File)
     * @see #export(File,String) 
     */
    public void export(OutputStream out, String encoding) throws IOException {
        Exporter exporter = new Exporter(roots,allow,deny);
        exporter.export(out, encoding);
    }

    /**
     * Exports all objects to a stream with the default charset
     * encoding, which is UTF-8.
     *
     * @param out stream to export to
     *
     * @see #export(OutputStream,String)
     * @see #export(File) 
     * @see #export(File,String) 
     */
    public void export(OutputStream out) throws IOException {
        export(out,DEFAULT_ENCODING);
    }

    /**
     * Exports all objects to a file with the default charset
     * encoding, which is UTF-8.
     *
     * @param file file to export to
     *
     * @see #export(File,String)
     * @see #export(OutputStream) 
     * @see #export(OutputStream,String) 
     */
    public void export(File file) throws IOException {
        export(file,DEFAULT_ENCODING);
    }


    /**
     * Exports all objects to a file with the default charset
     * encoding, which is UTF-8.
     *
     * @param file file to export to
     * @param encoding charset encoding to use (UTF-8 ,iso-8859-1,...)
     *
     * @see #export(File)
     * @see #export(OutputStream) 
     * @see #export(OutputStream,String) 
     */
    public void export(File file, String encoding) throws IOException {
        FileOutputStream writer = new FileOutputStream(file);
        try {
            export(writer,encoding);
        } finally {
            writer.close();
        }
    }
}
