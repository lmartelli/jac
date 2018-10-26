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
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA */

package org.objectweb.jac.aspects.export;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.apache.xerces.parsers.SAXParser;
import org.objectweb.jac.aspects.integrity.IntegrityAC;
import org.objectweb.jac.aspects.persistence.ValueConverter;
import org.objectweb.jac.core.ACManager;
import org.objectweb.jac.core.NameRepository;
import org.objectweb.jac.core.Naming;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.core.rtti.CollectionItem;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.util.Files;
import org.objectweb.jac.util.Strings;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @see Exporter
 */
public class Importer extends DefaultHandler implements ContentHandler {
    static Logger logger = Logger.getLogger("import");

    NameRepository nr;
    ClassRepository cr;
    XMLReader xmlReader;

    boolean firstPass = true;

    public Importer() {
        nr = (NameRepository)NameRepository.get();
        cr = ClassRepository.get();
        xmlReader = new SAXParser();
        xmlReader.setContentHandler(this);
    }

    Hashtable counters = new Hashtable();

    /**
     * Imports objects from a file created by the exporter, using
     * UTF-8 encoding. All fields must have proper setters, or their
     * values won't be properly restored regarding a persistence
     * aspect.
     *
     * @param file the file to read the objects data from. It can be compressed.  
     * @see #importObjects(File,String)
     */
    public void importObjects(File file) throws IOException, SAXException 
    {
        importObjects(file,ExportAC.DEFAULT_ENCODING);
    }

    /**
     * Imports objects from a file created by the exporter. All fields
     * must have proper setters, or their values won't be properly
     * restored regarding a persistence aspect.
     *
     * @param file the file to read the objects data from. It can be compressed.  
     * @param encoding charset encoding of the file
     *
     * @see #importObjects(File)
     */
    public void importObjects(File file, String encoding) throws IOException, SAXException {
        // First pass: create all objects
        try {
            firstPass = true;
            Reader reader = Files.autoDecompressReader(file, encoding);
            IntegrityAC.disableRoleUpdates();
            try {
                xmlReader.parse(new InputSource(reader));
                ACManager.getACM().updateNameCounters(counters);
            } finally {
                reader.close();
            }

            // Second pass: initialize field of objects created during first pass
            firstPass = false;
            reader = Files.autoDecompressReader(file, encoding);
            try {
                xmlReader.parse(new InputSource(reader));
            } finally {
                reader.close();
            }
        } finally {
            IntegrityAC.enableRoleUpdates();
            newObjects.clear();
        }
    }

    public void startDocument() {
        if (firstPass)
            logger.info("FIRST PASS");
        else
            logger.info("SECOND PASS");
    }

    public void endDocument() {
        if (firstPass)
            logger.info("FIRST PASS DONE.");
        else
            logger.info("SECOND PASS DONE");
    }

    // Current object
    Object current = null;
    // Name of current object
    String currentName;
    // Class of current object
    ClassItem cl = null;
    FieldItem field = null;
    CollectionItem collection = null;
    boolean reference = false;
    boolean key = false;
    boolean value = false;
    boolean nameCounter = false;
    // Map.Entry key
    Object keyObject = null;
    // Map.Entry value
    Object valueObject = null;
    // Name of counter
    String name = null;
    // value of counter
    Long counter = null;

    Set currentSet = new HashSet();
    Map currentMap = new Hashtable();
    List currentList = new Vector();

    // We must keep a reference on new objects so that they are not
    // collected by the GC.
    List newObjects = new LinkedList();

    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        if (localName.equals("object")) {
            String name = attributes.getValue("name");
            currentName = name;
            cl = cr.getClass(attributes.getValue("class"));
            logger.info("<object name=\""+name+"\" class=\""+cl.getName()+"\">");
            current = nr.getObject(name);
            if (current==null) {
                if (firstPass) {
                    logger.debug("  No such object "+name);
                    try {
                        Naming.setName(name);
                        current = cl.newInstance();
                        logger.debug("  instanciated "+Strings.hex(current));
                        if (!nr.getName(current).equals(name))
                            logger.error("Instanciated object is named "+
                                         nr.getName(current)+" instead of "+name);
                        newObjects.add(current);
                    } catch (Exception e) {
                        logger.error("Instanciation of "+cl.getName()+" failed:"+e);
                    }
                } else {
                    logger.error("Object "+name+" was not instanciated during first pass?");
                }
            }
        } else if (localName.equals("field")) {
            if (!firstPass) {
                String name = attributes.getValue("name");
                logger.debug("  <field name=\""+name+"\">");
                if (current==null || cl==null) {
                    logger.error("No current object or class for <field class=\""+name+"\"> element");
                } else {
                    field = cl.getField(name);
                    if (field instanceof CollectionItem) {
                        collection = (CollectionItem)field;
                        //((CollectionItem)field).clear(current);
                    }
                }
            }
        } else if (localName.equals("reference")) {
            if (!firstPass) {
                logger.debug("    <reference>");
                if (field!=null) {
                    reference = true;
                    buf.setLength(0);
                }
            }
        } else if (localName.equals("primitive_value")) {
            if (!firstPass) {
                logger.debug("    <primitive_value>");
                if (field!=null) {
                    buf.setLength(0);
                }
            }
        } else if (localName.equals("list")) {
            if (!firstPass) {
                if (collection!=null) {
                    currentList.clear();
                } else {
                    logger.error("No current collection field for <list>");
                }
            }
        } else if (localName.equals("set")) {
            if (!firstPass) {
                if (collection!=null) {
                    currentSet.clear();
                } else {
                    logger.error("No current collection field for <set>");
                }
            }
        } else if (localName.equals("map")) {
            if (!firstPass) {
                if (collection!=null) {
                    currentMap.clear();
                } else {
                    logger.error("No current collection field for <map>");
                }
            }
        } else if (localName.equals("entry")) {
        } else if (localName.equals("value")) {
            value = true;
        } else if (localName.equals("key")) {
            key = true;
        } else if (localName.equals("nameCounter")) {
            nameCounter = true;
        } else if (localName.equals("name")) {
            if (firstPass) {
                logger.debug("    <name>");
                buf.setLength(0);
            }
        } else if (localName.equals("counter")) {
            if (firstPass) {
                logger.debug("    <counter>");
                buf.setLength(0);
            }
        }
    }

    public void endElement(String uri, String localName, String qName) {
        try {
            if (localName.equals("object")) {
                current = null;
                currentName = null;
                cl = null;
            } else if (localName.equals("field")) {
                field = null;
                collection = null;
            } else if (localName.equals("reference")) {
                if (!firstPass) {
                    String name = Strings.unslashify(buf.toString());
                    Object ref = name.equals("null") ? null : nr.getObject(name);
                    logger.debug("  read reference "+name+" -> "+Strings.hex(ref));
                    if (key) {
                        keyObject = ref;
                    } else if (value) {
                        valueObject = ref;
                    } else if (field!=null) {
                        if (collection!=null) {
                            if (collection.isSet())
                                currentSet.add(ref);
                            else
                                currentList.add(ref);
                        } else {
                            if (field.getThroughAccessor(current)!=ref) {
                                logger.info("  Updating "+currentName+"."+field.getName());
                                field.setThroughWriter(current,ref);
                            }
                        }
                    }
                    reference = false;
                }
            } else if (localName.equals("primitive_value")) {
                if (!firstPass) {
                    Object val = 
                        ValueConverter.stringToObject(
                            null,Strings.unslashify(buf.toString()));
                    if (key) {
                        keyObject = val;
                    } else if (value) {
                        valueObject = val;
                    } else if (field!=null) {
                        if (collection!=null) {
                            if (collection.isSet())
                                currentSet.add(val);
                            else
                                currentList.add(val);
                        } else {
                            Object currentValue = field.getThroughAccessor(current);
                            if ((currentValue==null && val!=null) || 
                                (currentValue!=null && !currentValue.equals(val))) {
                                logger.info("  Updating "+currentName+"."+field.getName());
                                field.setThroughWriter(current,val);
                            }
                        }
                    }
                }
            } else if (localName.equals("list")) {
                if (!firstPass) {
                    List list = (List)collection.getThroughAccessor(current);
                    if (!currentList.equals(list)) {
                        logger.info("  Updating list "+currentName+"."+field.getName());
                        list.clear();
                        list.addAll(currentList);
                    } else {
                        logger.debug("    List has not changed");
                    }
                    currentList.clear();
                }
            } else if (localName.equals("set")) {
                if (!firstPass) {
                    Set set = (Set)collection.getThroughAccessor(current);
                    if (!currentSet.equals(set)) {
                        logger.info("  Updating set "+currentName+"."+field.getName());
                        set.clear();
                        set.addAll(currentSet);
                    } else {
                        logger.debug("    Set has not changed");
                    }
                    currentSet.clear();
                }
            } else if (localName.equals("map")) {
                if (!firstPass) {
                    Map map = (Map)collection.getThroughAccessor(current);
                    if (!currentMap.equals(map)) {
                        logger.info("  Updating map "+currentName+"."+field.getName());
                        map.clear();
                        map.putAll(currentMap);
                    } else {
                        logger.debug("    Map has not changed");
                    }
                    currentMap.clear();
                }
            } else if (localName.equals("entry")) {
                if (!firstPass) {
                    if (collection!=null) {
                        if (collection.isMap())
                            currentMap.put(keyObject,valueObject);
                        else
                            logger.error("Field is not a Map: "+field);
                    } else {
                        logger.error("Field is not a collection: "+field);
                    }
                }
            } else if (localName.equals("value")) {
                value = false;
            } else if (localName.equals("key")) {
                key = false;
            } else if (localName.equals("nameCounter")) {
                if (firstPass) {
                    counters.put(name,counter);
                    logger.info("Namecounter "+name+" -> "+counter);
                    nameCounter = false;
                }
            } else if (localName.equals("name")) {
                if (firstPass) {
                    name = buf.toString();
                }
            } else if (localName.equals("counter")) {
                if (firstPass) {
                    counter = new Long(buf.toString());
                }
            }
        } catch (Exception e) {
            logger.error("endElement("+uri+", "+localName+", "+qName+") failed");
            logger.error("  class="+cl);
            logger.error("  field="+field);
            logger.error("  current="+current);
            logger.error("  buf="+buf,e);
        }
    }

    StringBuffer buf = new StringBuffer();
    public void characters(char chars[], int start, int length) {
        if (buf!=null) {
            buf.append(chars,start,length);
        } else {
            //throw new RuntimeException("Unexpected characters: "+new String(chars));
        }
    }

    public void ignorableWhitespace(char chars[], int start, int length) {
        characters(chars,start,length);
    }

    protected Object readValue(String s) throws IOException {
        int colon = s.indexOf(':');
        if (colon==-1) {
            return nr.getObject(s);
        } else {
            return ValueConverter.stringToObject(null,s);
        }
    }
}
