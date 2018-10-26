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

import gnu.regexp.RE;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.StringBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.objectweb.jac.aspects.naming.NamingAC;
import org.objectweb.jac.aspects.persistence.ValueConverter;
import org.objectweb.jac.core.ACManager;
import org.objectweb.jac.core.MethodPointcut;
import org.objectweb.jac.core.NameRepository;
import org.objectweb.jac.core.Wrappee;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.core.rtti.CollectionItem;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.util.Strings;

public class Exporter {
    static Logger logger = Logger.getLogger("export");

    Collection roots;
    Collection allow;
    Collection deny;

    RE[] allowRegexps;
    RE[] denyRegexps;

    Map toExport = new HashMap(); // Objects not exported yet (OPath -> Object)
    Set exported = new HashSet(); // Already exported objects


    NameRepository nr;

    /**
     * @see #Exporter(Collection,Collection,Collection)
     */
    public Exporter(String[] roots, String[] allow, String[] deny) {
        this(Arrays.asList(roots),Arrays.asList(allow),Arrays.asList(deny));
    }

    /**
     * Creates a new exporter.
     *
     * @param roots collection of String naming root objects to start export from.
     * @param allow collection of String naming classes to export
     * @param deny  collection of String naming classes not to export
     *
     * @see #Exporter(String[],String[],String[])
     */
    public Exporter(Collection roots, Collection allow, Collection deny) {
        nr = (NameRepository)NameRepository.get();
        this.roots = roots;
        this.allow = allow;
        Iterator i = allow.iterator();
        int count = 0;
        allowRegexps = new RE[allow.size()];
        while (i.hasNext()) {
            String s = (String)i.next();
            try {
                allowRegexps[count] = MethodPointcut.buildRegexp(s);
            } catch (Exception e) {
                logger.error("Failed to build regexp for "+s,e);
            }
        }
        this.deny = deny;
        i = deny.iterator();
        count = 0;
        denyRegexps = new RE[deny.size()];
        while (i.hasNext()) {
            String s = (String)i.next();
            try {
                denyRegexps[count] = MethodPointcut.buildRegexp(s);
            } catch (Exception e) {
                logger.error("Failed to build regexp for "+s,e);
            }
        }
    }

    /**
     * Exports all objects to a file
     */
    public void export(File file) throws IOException {
        FileOutputStream out = new FileOutputStream(file);
        try {
            export(out);
        } finally {
            out.close();
        }
    }

    /**
     * Exports all objects to a stream, using UTF-8 encoding.
     */
    public void export(OutputStream outStream) throws IOException {
        export(outStream,"UTF-8");
    }

    /**
     * Exports all objects to a stream
     */
    public void export(OutputStream outStream, String encoding) throws IOException {
        Writer out = new OutputStreamWriter(outStream,encoding);
        out.write("<?xml version=\"1.0\" encoding=\""+encoding+"\"?>\n");
        out.write("<export>\n");
        int count = 0;
        Iterator i = NameRepository.getObjects(roots).iterator();
        while (i.hasNext()) {
            Object o = i.next();
            String name = nr.getName(o);
            export(out,o,name,name);
            count++;
        }
        while (!toExport.isEmpty()) {
            i = new HashMap(toExport).entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry entry = (Map.Entry)i.next();
                Object o = entry.getKey();
                String opath = (String)entry.getValue();
                String name = nr.getName(o);
                export(out,o,name,opath);
                count++;
            }
        }
        logger.info(count+" objects exported");
        Map counters = 
            ((NamingAC)ACManager.getACM().getACFromFullName("naming")).getNameCounters();
        i = counters.entrySet().iterator();
        while(i.hasNext()) {
            Map.Entry entry = (Map.Entry)i.next();
            out.write("<nameCounter>\n");
            out.write("  <name>"+entry.getKey()+"</name>\n");
            out.write("  <counter>"+entry.getValue()+"</counter>\n");
            out.write("</nameCounter>\n");
        }
        logger.info("Name counters exported");
        out.write("</export>\n");
        out.flush();
    }

    static public String escapeChar(char c) {
        return (c == '<') ? "&lt;" :
            (c == '>') ? "&gt;" :
            (c == '\'') ? "&apos;" :
            (c == '\"') ? "&quot;" :
            (c == '&') ? "&amp;" :
            (c == ']') ? "&#93;" : 
            null;
    }

    static public String escapeString(String s) {
        StringBuffer buf = new StringBuffer(s.length()*2);
        for (int i=0; i<s.length(); i++) {
            char c = s.charAt(i);
            String escaped = escapeChar(c);
            if (escaped==null)
                buf.append(c);
            else
                buf.append(escaped);
        }
        return buf.toString();
    }

    /**
     * Tells wether instances of a class should be exported or not
     *
     * @param cli a class item
     * @return true if instances of this class should be
     * exported. Instances of a class are exported if the class is not
     * listed in the deny list and is listed in the allow list.
     */
    protected boolean allowExport(ClassItem cli) {
        for (int i=0; i<denyRegexps.length;i++) {
            RE regexp = denyRegexps[i];
            if (regexp!=null && cli.isSubClassOf(regexp))
                return false;
        }

        for (int i=0; i<allowRegexps.length;i++) {
            RE regexp = allowRegexps[i];
            if (regexp!=null && cli.isSubClassOf(regexp))
                return true;
        }

        return false;
    }

    /**
     * Exports an object to a stream
     *
     * @param out the stream to which to export
     * @param o the object to export
     * @param name name of the object to export
     */
    public void export(Writer out, Object o, String name ,String opath) throws IOException {
        ClassItem cl = ClassRepository.get().getClass(o);
        if (name==null) {
            logger.error("Skipping unamed object "+o+" "+cl.getName()+" at "+opath);
            new Exception().printStackTrace();
            toExport.remove(o);
            return;
        }
        if (exported.contains(o)) {
            logger.debug("Skipping already exported "+name+" "+cl.getName());
            toExport.remove(o);
            return;
        }
        if (!allowExport(cl)) {
            logger.debug("Skipping not allowed "+name+" "+cl.getName());
            toExport.remove(o);
            return;
        }
        logger.debug("Exporting "+name+" "+cl.getName());
        exported.add(o);
        out.write("<object name=\""+name+"\" class=\""+cl.getName()+"\">\n");
        Iterator i = cl.getAllFields().iterator();
        while (i.hasNext()) {
            FieldItem field = (FieldItem)i.next();
            if (!field.isCalculated() && !field.isTransient() && 
                !field.isStatic() && !field.isTransient()) 
            {
                String newPath = opath+"."+field.getName();
                try {
                    if (field instanceof CollectionItem) {
                        CollectionItem collection = (CollectionItem)field;
                        ClassItem componentType = collection.getComponentType();
                        if (allowExport(componentType)) {
                            out.write("  <field name=\""+field.getName()+"\">\n");
                        if (collection.isMap()) {
                            out.write("    <map>\n");
                            Iterator j = ((Map)collection.getThroughAccessor(o)).entrySet().iterator();
                            while (j.hasNext()) {
                                Map.Entry entry = (Map.Entry)j.next();
                                out.write("      <entry><key>");
                                writeValue(out,entry.getKey(),newPath+"[key]");
                                out.write("</key><value>");
                                writeValue(out,entry.getValue(),newPath+"["+entry.getKey()+"]");
                                out.write("</value></entry>\n");
                            }
                            out.write("    </map>\n");
                        } else {
                            if (collection.isList())
                                out.write("    <list>\n");
                            else if (collection.isSet())
                                out.write("    <set>\n");
                            Iterator j = ((Collection)collection.getThroughAccessor(o)).iterator();
                            int count = 0;
                            while (j.hasNext()) {
                                Object item = j.next();
                                out.write("      ");
                                writeValue(out,item,newPath+"["+count+"]");
                                out.write("\n");
                                count++;
                            }
                            if (collection.isList())
                                out.write("    </list>\n");
                            else if (collection.isSet())
                                out.write("    </set>\n");
                        }
                            out.write("  </field>\n");
                        }
                    } else {
                        if (allowExport(field.getTypeItem())) {
                            out.write("  <field name=\""+field.getName()+"\">\n");
                        out.write("    ");
                            writeValue(out,field.getThroughAccessor(o),newPath);
                        out.write("\n");
                            out.write("  </field>\n");
                        }
                    }
                } catch (Exception e) {
                    logger.error("Failed to export "+name+"."+field);
                }
            }
        }
        out.write("</object>\n");
    }

    protected void writeValue(Writer out, Object value, String opath) throws IOException {
        if (value instanceof Wrappee) {
            if (value!=null) {
                if (!exported.contains(value)) {
                    toExport.put(value,opath);
                    logger.debug("toExport "+opath+" -> "+value+" "+nr.getName(value));
                }
                out.write("<reference>"+nr.getName(value)+"</reference>");
            } else {
                out.write("<reference>null</reference>");
            }
        } else {
            if (value!=null) {
                out.write("<primitive_value>"+
                          escapeString(Strings.slashify(ValueConverter.objectToString(null,value)))+
                          "</primitive_value>");
            } else {
                out.write("<primitive_value>null</primitive_value>");
            }
        }
    }
}
