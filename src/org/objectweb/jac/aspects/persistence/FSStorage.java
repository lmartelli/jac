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

package org.objectweb.jac.aspects.persistence;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StreamTokenizer;
import java.io.Writer;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.objectweb.jac.aspects.naming.NameGenerator;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.CollectionItem;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.util.Files;
import org.objectweb.jac.util.Log;
import org.objectweb.jac.util.Semaphore;
import org.objectweb.jac.util.Strings;

/**
 * A FileSystem storage
 */

public class FSStorage implements Storage {
    static Logger logger = Logger.getLogger("persistence.storage");

    // name -> oid 
    private Hashtable names = new Hashtable();
    // oid -> name
    private Hashtable oids = new Hashtable();
    // oid -> classid
    private Hashtable classes = new Hashtable();
   
    private File basedir;
    private File oidsFile;
    private File classesFile;

    /* The encoding of files */
    protected String encoding;

    private long lastOID = 0;

    Semaphore semaphore = new Semaphore(1);

    NameGenerator nameGen;
    File nameCountersFile;

    /**
     * Create a new file system storage with the default encoding
     * @param basedirName name of the directory where to store data files
     */
    public FSStorage(PersistenceAC ac,
                     String basedirName) throws Exception {
        this(ac,basedirName,System.getProperty("file.encoding"));
    }

    /**
     * Create a new file system storage
     * @param basedirName name of the directory where to store data files
     * @param encoding the encoding to use for files
     */
    public FSStorage(PersistenceAC ac, 
                     String basedirName, String encoding) 
        throws Exception 
    {
        this.ac = ac;
        logger.debug("new FSStorage(basedir="+basedirName+", encoding="+encoding+")");
        this.encoding = encoding;
        basedir = new File(Files.expandFileName(basedirName));
        if (!basedir.isAbsolute()) {
            basedir = new File(org.objectweb.jac.core.Jac.getJacRoot(),basedir.toString());
        }
        if (! basedir.isDirectory()) {
            basedir.mkdirs();
        }

        nameGen = new NameGenerator();

        nameCountersFile = new File(basedir,"nameCounters");
        try {
            nameCountersFile.createNewFile();
            readNameCounters();
        } catch(Exception e) {
            e.printStackTrace();
        }

        classesFile = new File(basedir,"classes");
        try {
            classesFile.createNewFile();
            readClasses();
        } catch(Exception e) {
            e.printStackTrace();
        }

        oidsFile = new File(basedir,"oids");
        //try {
            oidsFile.createNewFile();
            readOids();
            /*
        } catch(Exception e) {
            e.printStackTrace();
        }
            */
      
        updateJacNames();
    }

    PersistenceAC ac;

    protected String id;
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    protected void updateJacNames() {
        try {
            Iterator it = names.entrySet().iterator();
            Hashtable newNames = new Hashtable();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry)it.next();
                LongOID oid = (LongOID)entry.getValue();
                String name = (String)entry.getKey();
                if (name.indexOf('#')==-1) {
                    String classname = 
                        Strings.getShortClassName((String)classes.get(oid)).toLowerCase();
                    if (name.startsWith(classname) && 
                        name.length()>classname.length() &&
                        name.charAt(classname.length())!='#') 
                    {
                        String newName = classname+"#"+name.substring(classname.length());
                        it.remove();
                        newNames.put(newName,oid);
                        oids.put(oid,newName);
                    }
                }
            }
            names.putAll(newNames);
            writeOids();
        } catch (Exception e) {
            logger.error("Failed to update jac names");
        }
    }    

    /**
     * Safely close the storage. Waits for all operations to terminate.
     */
    public void close() {
        // wait for completion of deleteObject
        semaphore.acquire();
        logger.info("FSStorage shutdown hook completed");
    }

    /**
     * Read the "oids" file
     */
    void readOids() throws Exception 
    {
        oidsFile.createNewFile();
        StreamTokenizer tokens = getStreamTokenizer(oidsFile);

        boolean corruptedCounter = false;
        while (tokens.nextToken() != StreamTokenizer.TT_EOF) {
            logger.debug("token : "+tokens.sval+"/"+tokens.nval+" "+tokens.ttype);
            LongOID oid = ac.parseLongOID(tokens.sval,this);
            long oidval = oid.getOID();
            if (oidval>lastOID)
                lastOID = oidval;
            tokens.nextToken();
            logger.debug("token : "+tokens.sval+"/"+tokens.nval+" "+tokens.ttype);
            String name = tokens.sval;
            String classid = getClassID(oid);
            if (names.containsKey(name)) {
                logger.error("Corrupted storage! OIDs "+
                             names.get(name)+" and "+oid+" have the same name "+name+
                             ". Assigning new name to "+oid);
                name = newName(classid);
                logger.error("  new name is "+name);
            }
            oids.put(oid,name);
            names.put(name,oid);

            // Ensure counters integrity
            try {
                long count = nameGen.getCounterFromName(name);
                long current = nameGen.getCounter(classid);
                if (current!=-1 && current <= count) {
                    logger.error("Corrupted counter for "+classid+". Adjusting "+current+"->"+(count+1));
                    nameGen.setCounter(classid,count+1);
                    corruptedCounter = true;
                }
            } catch(Exception e) {
            }
        }
        if (corruptedCounter) {
            writeNameCounters();
        }
    }

    /**
     * Writer the "oids" file
     */ 
    void writeOids() throws Exception 
    {
        logger.debug("writeOids");
        PrintWriter writer = getPrintWriter(oidsFile,false,false);
        try {
            Iterator it = oids.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry)it.next();
                writer.println(((OID)entry.getKey()).localId()+" "+entry.getValue());         
            }
        } finally {
            writer.close();
        }
    }

    /**
     * Read the "nameCounters" file
     */
    void readNameCounters() throws IOException, FileNotFoundException
    {
        nameCountersFile.createNewFile();
        StreamTokenizer tokens = getStreamTokenizer(nameCountersFile);
        while (tokens.nextToken() != StreamTokenizer.TT_EOF) {
            String className = tokens.sval;
            tokens.nextToken();
            Long counter = new Long(tokens.sval);
            logger.debug(className+" -> "+counter);
            nameGen.put(className,counter);
        }      
    }

    FileDescriptor nameCountersFD;
    /**
     * Write the "nameCounters" file
     */
    void writeNameCounters() throws IOException {
        logger.debug("writeNameCounters");
        PrintWriter writer;
        FileOutputStream stream = new FileOutputStream(nameCountersFile.toString(),false);
        writer = getPrintWriter(stream,false);
        writeMap(nameGen,writer);
    }

    public Map getNameCounters() {
        return nameGen;
    }

    public void updateNameCounters(Map counters) throws IOException {
        nameGen.update(counters);
        writeNameCounters();
    }

    /**
     * Write the content of a Map to a File, using the toString() of
     * the keys and values. Does nothing if the map is empty.
     *
     * @param map the map
     * @param file the file 
     */
    void writeMapToFile(Map map, File file) throws IOException {
        if (map.isEmpty()) {
            file.delete();
        } else {
            PrintWriter writer = getPrintWriter(file,false,false);
            try {
                Iterator it = map.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry entry = (Map.Entry)it.next();
                    writer.println(entry.getKey().toString()+" "+entry.getValue());         
                }
            } finally {
                writer.close();
            }
        }
    }

    void writeMap(Map map, PrintWriter writer) throws IOException {
        try {
            Iterator it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry)it.next();
                writer.println(entry.getKey().toString()+" "+entry.getValue());         
            }
        } finally {
            writer.close();
        }
    }

    void readClasses() throws Exception 
    {
        logger.debug("readClasses");
        classesFile.createNewFile();
        StreamTokenizer tokens = getStreamTokenizer(classesFile);
        while (tokens.nextToken() != StreamTokenizer.TT_EOF) {
            LongOID oid = new LongOID(this,Long.parseLong(tokens.sval));
            long oidval = oid.getOID();
            if (oidval>lastOID)
                lastOID = oidval;
            tokens.nextToken();
            String classid = tokens.sval;
            logger.debug(oid.toString()+" -> "+classid);
            classes.put(oid,classid);
        }
    }

    void writeClasses() throws Exception 
    {
        logger.debug("writeClasses");
        writeMapToFile(classes,classesFile);
    }

    protected StreamTokenizer getStreamTokenizer(File file) 
        throws FileNotFoundException, IOException
    {
        StreamTokenizer tokens = 
            new StreamTokenizer(
                new BufferedReader(
                    new InputStreamReader(
                        new FileInputStream(file),
                        encoding)));
        tokens.resetSyntax();
        tokens.wordChars('\000','\377');
        tokens.whitespaceChars(' ',' ');
        tokens.whitespaceChars('\t','\t');
        tokens.whitespaceChars('\n','\n');
        tokens.whitespaceChars('\r','\r');
        return tokens;
    }

    public OID createObject(String className) throws Exception
    {
        semaphore.acquire();
        PrintWriter writer = null;
        try {
            lastOID++;
            LongOID oid = new LongOID(this,lastOID);
            classes.put(oid,className);
            writer = new PrintWriter(getWriter(classesFile,true),true);
            writer.println(oid.localId()+" "+className);
            return oid;
        } finally {
            if (writer!=null)
                writer.close();
            semaphore.release();
        }
    }

    public void deleteObject(OID oid) throws Exception
    {
        semaphore.acquire();
        try {
            logger.debug("deleteObject("+oid+")");
            String name = (String)oids.get(oid);
            if (name!=null) {
                names.remove(name);
                oids.remove(oid);
                writeOids();
            } else {
                logger.warn("FSStorage.deleteObject: oid "+
                               oid+" does not have name");
                logger.debug("names = "+classes);
            }
            if (classes.containsKey(oid)) {
                classes.remove(oid);
                writeClasses();
            } else {
                logger.warn("FSStorage.deleteObject: oid "+
                               oid+" does not have a class");
                logger.debug("classes = "+classes);
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            logger.debug("deleteObject("+oid+") DONE");
            semaphore.release();
        }
        // *** TODO: WE SHOULD ALSO REMOVE COLLECTIONS OWNED BY THE
        // ***       DELETED OBJECT
    }

    public void setField(OID oid, FieldItem field, Object value) 
        throws Exception
    {
        logger.debug("setField("+oid+","+field+","+value+")");
        Properties props = new Properties();
        File objectFile = new File(basedir,oid.localId());
        objectFile.createNewFile();
        props.load(new FileInputStream(objectFile));
        props.setProperty(
            field.getName(),
            ValueConverter.objectToString(this,value));
        props.store(new FileOutputStream(objectFile),getClassID(oid)+" "+oid.localId());
    }

    public void updateField(OID oid, FieldItem field, Object value) 
        throws Exception
    {
        logger.debug("updateField("+oid+","+field+","+value+")");
        setField(oid,field,value);
    }

    /** oid -> Properties */
    Hashtable cache = new Hashtable();

    public Object getField(OID oid, FieldItem field)
        throws Exception
    {
        logger.debug("getField("+oid+","+field+")");
        Object ret = null;
        Properties props = (Properties)cache.get(oid);
        if (props==null) {
            props = new Properties();
            File objectFile = new File(basedir,oid.localId());
            if (objectFile.exists())
                props.load(new FileInputStream(objectFile));
            cache.put(oid,props);
        }
        String value = (String)props.get(field.getName());
        if (value != null) {
            ret = ValueConverter.stringToObject(this,value);
        } else {
            if (field.isPrimitive()) {
                logger.warn("no such field in storage "+oid+","+field.getName());
            }
            ret = null;
        }
        logger.debug(" -> "+ret);
        return ret;
    }

    protected Properties getFields(OID oid) throws IOException
    {
        Properties props = new Properties();
        File f = new File(basedir,oid.localId());
        if (f.exists())
            props.load(new FileInputStream(f));
        return props;
    }

    public StorageField[] getFields(OID oid, ClassItem cl, FieldItem[] fields) 
        throws Exception
    {
        Properties props = getFields(oid);
        StorageField ret[] = new StorageField[fields.length];
        int count = 0;
        for (int i=0; i<fields.length; i++) {
            if (!fields[i].isCalculated() && !fields[i].isTransient()) {
                String value = (String)props.get(fields[i].getName());
                if (value!=null)
                    ret[i] = 
                        new StorageField(
                            cl,
                            fields[i],
                            ValueConverter.stringToObject(this,value));
                else
                    ret[i] = new StorageField(cl,fields[i],null);
            }
        }
        return ret;
    }

    // Collection methods

    protected long getCollectionSize(OID cid) throws Exception
    {
        return getList(cid).size();
    }

    public OID getCollectionID(OID oid, CollectionItem collection) 
        throws Exception
    {
        return (OID)getField(oid,collection);
    }

    // List methods

    public void clearList(OID cid) throws Exception
    {
        saveList(cid,new Vector());
    }

    public List getList(OID oid, CollectionItem collection)
        throws Exception
    {
        logger.debug("getList("+oid+","+collection+")");
        return getList(getCollectionID(oid,collection));
    }

    public List getList(OID cid)
        throws Exception
    {
        logger.debug("getList("+cid+")");
        File file = new File(basedir,cid.localId());
        Vector ret = new Vector();
        if (file.exists()) {
            StreamTokenizer tokens = getStreamTokenizer(file);
            while (tokens.nextToken() != StreamTokenizer.TT_EOF) {
                try {
                    ret.add(
                        ValueConverter.stringToObject(
                            this,
                            Strings.unslashify(tokens.sval)));
                } catch (Throwable e) {
                    logger.error("failed to list element: "+tokens.sval,e);
                }
            }
        }
        logger.debug("getList returns "+ret); 
        return ret;
    }

    public long getListSize(OID cid) throws Exception {
        logger.debug("getListSize("+cid+")");

        File file = new File(basedir,cid.localId());
        long size = 0; 
        if (file.exists()) {
            StreamTokenizer tokens = getStreamTokenizer(file);
            while (tokens.nextToken() != StreamTokenizer.TT_EOF) {
                size++;
            }
        }

        return size;
    }

    public Object getListItem(OID cid, long index)
        throws Exception, IndexOutOfBoundsException
    {
        logger.debug("getListItem("+cid+","+index+")");

        File file = new File(basedir,cid.localId());
        Vector ret = new Vector();
        long current = 0;
        if (file.exists()) {
            StreamTokenizer tokens = getStreamTokenizer(file);
            while (tokens.nextToken() != StreamTokenizer.TT_EOF) {
                if (current==index) {
                    return 
                        ValueConverter.stringToObject(
                            this,Strings.unslashify(tokens.sval));
                }
                current++;
            }
        }

        throw new IndexOutOfBoundsException(cid+"["+index+"]");
    }

    public boolean listContains(OID cid, Object value) 
        throws Exception
    {
        logger.debug("listContains("+cid+","+value+")");
        return getList(cid).contains(value);
    }

    protected void saveList(OID cid, List list) 
        throws IOException
    {
        logger.debug("saveList("+cid+","+list+")");
        File file = new File(basedir,cid.localId());
        logger.debug("file = "+file);
        if (list.isEmpty()) {
            file.delete();
        } else {
            PrintWriter writer = getPrintWriter(file,false,true);
            try {
                for (int i=0; i<list.size(); i++) {
                    String value = 
                        ValueConverter.objectToString(this,list.get(i));
                    logger.debug("  -> "+value);
                    writer.println(Strings.slashify(value));
                }
            } finally {
                writer.close();
            }
        }
    }

    public void addToList(OID cid, long position, Object value)
        throws Exception
    {
        logger.debug("addToList("+cid+","+position+","+value+")");
        List list = getList(cid);
        list.add((int)position,value);
        saveList(cid,list);
    }

    public void addToList(OID cid, Object value)
        throws Exception
    {
        logger.debug("addToList("+cid+","+value+")");

        PrintWriter writer = getPrintWriter((LongOID)cid,true,true);
        try {
            writer.println(
                Strings.slashify(ValueConverter.objectToString(this,value)));
        } finally {
            writer.close();
        }
    }

    public void setListItem(OID cid, long index, Object value)
        throws Exception
    {
        List list = getList(cid);
        list.set((int)index,value);
        saveList(cid,list);
    }

    public void removeFromList(OID cid, long position)
        throws Exception
    {
        List list = getList(cid);
        list.remove((int)position);
        saveList(cid,list);
    }

    public void removeFromList(OID cid, Object value)
        throws Exception
    {
        List list = getList(cid);
        list.remove(value);
        saveList(cid,list);
    }

    public long getIndexInList(OID cid, Object value)
        throws Exception
    {
        List list = getList(cid);
        return list.indexOf(value);
    }

    public long getLastIndexInList(OID cid, Object value)
        throws Exception
    {
        List list = getList(cid);
        return list.lastIndexOf(value);
    }


    // Set methods

    public void clearSet(OID cid) throws Exception
    {
        saveList(cid,new Vector());
    }

    public List getSet(OID oid, CollectionItem collection) 
        throws Exception 
    {
        return getSet(getCollectionID(oid,collection));
    }

    public List getSet(OID cid) 
        throws Exception 
    {
        return getList(cid);
    }

    public long getSetSize(OID cid) throws Exception {
        return getCollectionSize(cid);
    }

    public boolean addToSet(OID cid, Object value) 
        throws Exception 
    {
        logger.debug("addToSet("+cid+","+value+")");
        List list = getList(cid);
        boolean ret = list.add(value);
        saveList(cid,list);
        return ret;
    }

    public boolean removeFromSet(OID cid, Object value) 
        throws Exception 
    {
        logger.debug("addToSet("+cid+","+value+")");
        List list = getList(cid);
        boolean ret = list.remove(value);
        saveList(cid,list);
        return ret;
    }

    public boolean setContains(OID cid, Object value) 
        throws Exception
    {
        return getList(cid).contains(value);
    }

    // Map methods

    public Map getMap(OID oid, CollectionItem collection) 
        throws Exception
    {
        return getMap(getCollectionID(oid,collection));
    }

    public Map getMap(OID cid) 
        throws Exception
    {
        logger.debug("getMap("+cid+")");
        File file = new File(basedir,cid.localId());
        Hashtable ret = new Hashtable();
        if (file.exists()) {
            StreamTokenizer tokens = getStreamTokenizer(file);
            while (tokens.nextToken() != StreamTokenizer.TT_EOF) {
                Object key = 
                    ValueConverter.stringToObject(
                        this,Strings.unslashify(tokens.sval));
                tokens.nextToken();
                Object value = 
                    ValueConverter.stringToObject(
                        this,Strings.unslashify(tokens.sval));
                logger.debug("  -> "+key+","+value);
                ret.put(key,value);
            }
        }
        return ret;
    }

    public long getMapSize(OID cid) throws Exception {
        return getCollectionSize(cid);
    }

    protected void saveMap(OID cid, Map map) 
        throws IOException
    {
        logger.debug("saveMap("+cid+","+map+")");
        File file = new File(basedir,cid.localId());
        if (map.isEmpty()) {
            file.delete();
        } else {
            logger.debug("file = "+file);
            PrintWriter writer = getPrintWriter(file,false,true);
            try {
                Set entrySet = map.entrySet();
                Iterator i = entrySet.iterator();
                while (i.hasNext()) {
                    Map.Entry entry = (Map.Entry)i.next();
                    String value = 
                        ValueConverter.objectToString(this,entry.getValue());
                    String key = 
                        ValueConverter.objectToString(this,entry.getKey());
                    logger.debug("  -> "+key+","+value);
                    writer.println(Strings.slashify(key)+" "+Strings.slashify(value));
                }
            } finally {
                writer.close();
            }
        }
    }

    public void clearMap(OID cid) throws Exception
    {
        saveMap(cid,new Hashtable());
    }

    public Object putInMap(OID cid, Object key, Object value) 
        throws Exception
    {
        Map map = getMap(cid);
        Object ret = map.put(key,value);
        saveMap(cid,map);
        return ret;
    }

    public Object getFromMap(OID cid, Object key) 
        throws Exception
    {
        return getMap(cid).get(key);
    }

    public boolean mapContainsKey(OID cid, Object key)
        throws Exception
    {
        return getMap(cid).containsKey(key);
    }

    public boolean mapContainsValue(OID cid, Object value)
        throws Exception
    {
        return getMap(cid).containsValue(value);
    }

    public Object removeFromMap(OID cid, Object key)
        throws Exception
    {
        logger.debug("removeFromMap("+cid+","+key+")");
        Map map = getMap(cid);
        Object ret = map.remove(key);
        saveMap(cid,map);
        return ret;
    }

    // others...

    public void removeField(OID oid, FieldItem field, Object value) 
        throws Exception
    {}

    public synchronized String newName(String className) throws Exception {
        semaphore.acquire();
        try {
            String name = nameGen.generateName(className);
            writeNameCounters();
            return name;
        } finally {
            semaphore.release();
        }
    }

    public OID getOIDFromName(String name) throws Exception {
        return (OID)names.get(name);
    }

    public String getNameFromOID(OID oid) throws Exception {
        return (String)oids.get(oid);
    }

    public void bindOIDToName(OID oid,String name) throws Exception
    {
        semaphore.acquire();
        PrintWriter writer = null;
        try {
            names.put(name,oid);
            oids.put(oid,name);
            writer = getPrintWriter(oidsFile,true,true);
            writer.println(oid.localId()+" "+name);
        } finally {
            if (writer!=null)
                writer.close();
            semaphore.release();
        }
    }

    public void deleteName(String name) throws Exception
    {}

    public String getClassID(OID oid) throws Exception {
        logger.debug("getClassID("+oid+") -> "+(String)classes.get(oid));
        return (String)classes.get(oid);
    }

    public Collection getRootObjects() throws Exception {
        return oids.keySet();
    }

    public Collection getObjects(ClassItem cl) throws Exception {
        logger.debug("getObjects("+cl+")");
        if (cl == null) {
            return classes.keySet();
        } else {
            Vector ret = new Vector();
            getObjects(cl,ret);
            return ret;
        }
    }

    /**
     * Gets all instances of a class and its subclasses.
     * @param cl the class
     * @param objects instances of the class are added to this collection
     */
    protected void getObjects(ClassItem cl, Vector objects) throws Exception {
        logger.debug("getObjects("+cl+")");
        Set entries = classes.entrySet();
        Iterator i = entries.iterator();
        while(i.hasNext()) {
            Map.Entry entry = (Map.Entry)i.next();
            logger.debug("testing "+entry.getValue());
            if (cl==null || entry.getValue().equals(cl.getName())) {
                objects.add(entry.getKey());
            }
        }
        i = cl.getChildren().iterator();
        while(i.hasNext()) {
            ClassItem subclass = (ClassItem)i.next();
            getObjects(subclass,objects);
        }
    }


    public void startTransaction() {
    }

    public void commit() {
    }

    public void rollback() {
    }

    protected PrintWriter getPrintWriter(OID oid, boolean append, boolean autoFlush) 
        throws IOException 
    {
        return getPrintWriter(new File(basedir,oid.localId()),append,autoFlush);
    }

    protected PrintWriter getPrintWriter(File file, boolean append, boolean autoFlush) 
        throws IOException
    {
        return
            new PrintWriter(
                // The default buffer size is much too big
                new BufferedWriter(getWriter(file,append),1024), 
                autoFlush);
    }

    protected PrintWriter getPrintWriter(OutputStream stream, boolean autoFlush) 
        throws IOException
    {
        return
            new PrintWriter(
                new BufferedWriter(
                    new OutputStreamWriter(stream,encoding),
                    1024), // The default buffer size is much too big
                autoFlush);
    }

    protected Writer getWriter(File file,  boolean append) throws IOException {
        return 
            new OutputStreamWriter(
                new FileOutputStream(file.toString(),append),
                encoding);

    }
}
