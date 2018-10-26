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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.CollectionItem;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.util.Strings;

/**
 * Implements the storage to store within an SQL compliant database system.
 *
 * @see LongOID
 */

public abstract class SQLStorage implements Storage,java.io.Serializable {
    static Logger logger = Logger.getLogger("persistence.storage");
    static Logger loggerSql = Logger.getLogger("persistence.sql");

    /**
     * The SQL connection to the database that is use by this storage. */
    protected Connection db;

    /**
     * Default constructor. */
    protected SQLStorage(PersistenceAC ac) throws SQLException {
        this. ac = ac;
    }

    /**
     * Creates a new SQL storage.<p>
     *
     * @param db the connection to the database
     */
    public SQLStorage(PersistenceAC ac, Connection db) throws SQLException {
        this. ac = ac;
        setConnection(db);
    }

    protected String id;
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public void close() {}

    /**
     * Resets the connection to the database with the given
     * connection.<p>
     * 
     * @param db the new connection */

    protected void setConnection(Connection db) throws SQLException {
        this.db = db;
        updateJacNames();
    }

    /**
     * Tells wether a table with a given name exists
     */
    protected abstract boolean hasTable(String name) throws Exception;

    /**
     * Updates jac names from <classname><count> to <classname>#<count>
     */
    protected void updateJacNames() throws SQLException {
        ResultSet rs = executeQuery(
            "SELECT roots.id, roots.name, classes.classid from roots,classes where "+
            "not roots.name like '%#%' and roots.id=classes.id");
        execute("BEGIN TRANSACTION");
        try {
            while (rs.next()) {
                String classname = Strings.getShortClassName(rs.getString("classid")).toLowerCase();
                String name = rs.getString("name");
                if (name.startsWith(classname) && 
                    name.length()>classname.length() &&
                    name.charAt(classname.length())!='#') 
                {
                    String newName = classname+"#"+name.substring(classname.length());
                    executeUpdate("update roots set name='"+newName+"' where name='"+name+"'");
                }
            }
            execute("COMMIT");
        } catch (Exception e) {
            execute("ROLLBACK");
            logger.error("Failed to update jac names");
        }
    }    

    protected int executeUpdate(String query) throws SQLException {
        try {
            loggerSql.debug(query);
            return db.createStatement().executeUpdate(query);
        } catch (SQLException e) {
            logger.error("executeUpdate query failed: "+query); 
            throw e;
        }
    }

    protected ResultSet executeQuery(String query) throws SQLException {
        try {
            loggerSql.debug(query);
            return db.createStatement().executeQuery(query);
        } catch (SQLException e) {
            logger.error("executeQuery query failed: "+query);
            throw e;
        }
    }

    protected boolean execute(String query) throws SQLException {
        try {
            loggerSql.debug(query);
            return db.createStatement().execute(query);
        } catch (SQLException e) {
            logger.error("execute query failed: "+query);
            throw e;
        }
    }

    protected boolean executeSilent(String query) throws SQLException {
        return db.createStatement().execute(query);
    }

    public void deleteObject(OID oid) throws Exception
    {
        logger.debug("deleteObject("+oid+")");
        executeUpdate("delete from objects where id="+oid.localId());
        executeUpdate("delete from roots where id="+oid.localId());
        // WE SHOULD ALSO REMOVE COLLECTIONS
    }

    public void setField(OID oid, FieldItem field, Object object) 
        throws Exception
    {
        logger.debug("setField("+oid+","+field.getName()+","+object+")");
        String value = ValueConverter.objectToString(this,object);
        String fieldID = field.getName();
        String query = "insert into objects (id,fieldID,value) values "+
            "("+oid.localId()+",'"+fieldID+"','"+addSlashes(value)+"')";
        if (executeUpdate(query)==0) {
            logger.error("setField failed : "+oid+","+fieldID+","+value);
        }
    }

    public void updateField(OID oid, FieldItem field, Object object) 
        throws Exception
    {
        logger.debug("updateField("+oid+","+field+","+object+")");
        String fieldID = field.getName();
        String value = ValueConverter.objectToString(this,object);
        String query = "update objects set value='"+addSlashes(value)+"' where "+
            "id="+oid.localId()+" and fieldID='"+fieldID+"'";
        if (executeUpdate(query)==0) {
            setField(oid,field,object);
        }
    }

    public Object getField(OID oid, FieldItem field) 
        throws Exception
    {
        logger.debug("getField("+oid+","+field.getName()+")");
        checkStorage();
        String fieldID = field.getName();
        ResultSet rs = executeQuery("select value from objects where id="+oid.localId()+
                                    " and fieldID='"+fieldID+"'");
        if (rs.next()) {
            return ValueConverter.stringToObject(this,rs.getString("value"));
        } else {
            if (field.isPrimitive()) {
                logger.warn("no such field in storage "+oid+","+fieldID);
            }
            return null;
        }
    }

    public StorageField[] getFields(OID oid, ClassItem cl, FieldItem[] fields) 
        throws Exception
    {
        logger.debug("getFields "+oid+","+cl+","+Arrays.asList(fields));
        // compute stringified list of fields for SQL query
        if (fields.length == 0) {
            return new StorageField[0];
        }
        String fieldlist = "(";
        boolean first = true;
        for (int i=0; i<fields.length; i++) {
            if (!fields[i].isCalculated() && !fields[i].isTransient()) {
                if (!first)
                    fieldlist += " or ";
                fieldlist += "fieldID='"+fields[i].getName()+"'";
                first = false;
            }
        }
        fieldlist += ")";
      
        StorageField fieldValues[] = new StorageField[fields.length];
        String query = "select * from objects where id="+oid.localId()+" and "+fieldlist;
        ResultSet rs = executeQuery(query);

        int i=0;
        while (rs.next()) {
            FieldItem field = cl.getField(rs.getString("fieldID"));
            fieldValues[i] = new StorageField(
                cl,field,
                ValueConverter.stringToObject(this,rs.getString("value")));
            i++;
        }
        return fieldValues;
    }

    public void removeField(OID oid, FieldItem field, Object value) 
        throws Exception
    {
        logger.debug("removeField("+oid+","+field+","+value+")");
        String fieldID = field.getName();
        executeUpdate("delete from objects where id="+oid.localId()+
                      " and fieldID='"+fieldID+"'");      
    }

    public Collection getRootObjects() throws Exception {
        logger.debug("getRootObjects");
        String sql = "select id from roots";
        ResultSet rs = executeQuery(sql);
        Vector result = new Vector();
        while (rs.next()) {
            result.add(new LongOID(this,rs.getLong("value")));
        }
        logger.debug("getRootObjects returns " + result);
        return result;
    }

    // Collection methods

    public OID getCollectionID(OID oid, CollectionItem collection) 
        throws Exception 
    {
        return getOID("select value from objects where "+
                      "id="+oid.localId()+" and fieldID='"+collection.getName()+"'");
    }

    public List getCollectionValues(OID oid, CollectionItem collection, 
                                    String table, String orderBy) 
        throws Exception
    {
        logger.debug("getCollectionValues("+oid+","+collection+")");
        String fieldID = collection.getName();

        String sql = "select "+table+".value from "+table+",objects where "+
            "objects.id="+oid.localId()+
            " and objects.fieldID='"+fieldID+"'"+
            " and objects.value="+table+".id";
        if (orderBy!=null) {
            sql += " order by " + orderBy;
        }
      
        ResultSet rs = executeQuery(sql);
        Vector result = new Vector();
        while (rs.next()) {
            result.add(ValueConverter.stringToObject(this,rs.getString("value")));
        }
        logger.debug("getCollectionValues returns " + result);
        return result;
    }

    public boolean collectionContains(String table, OID cid, Object value) 
        throws Exception 
    {
        ResultSet res = executeQuery(
            "select id from "+table+" where "+
            "id="+cid.localId()+" and value='"+
            addSlashes(ValueConverter.objectToString(this,value))+"'");
        return res.next();
    }

    // List methods

    public void clearList(OID cid) 
        throws Exception 
    {
        logger.debug("clearList("+cid+")");
        executeUpdate("delete from lists where id="+cid.localId());
    }

    public List getList(OID oid, CollectionItem collection) 
        throws Exception
    {
        return getList(getCollectionID(oid,collection));
    }

    public List getList(OID cid)
        throws Exception
    {
        logger.debug("getList("+cid+")");
        ResultSet rs = executeQuery("select value from lists "+
                                    "where id="+cid.localId()+" order by index");
        Vector result = new Vector();
        while (rs.next()) {
            result.add(
                ValueConverter.stringToObject(this,rs.getString("value")));
        }
        logger.debug("getList returns " + result);
        return result;
    }

    public long getListSize(OID cid) 
        throws Exception
    {
        return getLong("select count(*) from lists where id="+cid.localId());
    }

    public boolean listContains(OID cid, Object value) 
        throws Exception 
    {
        return collectionContains("lists",cid,value);
    }

    public Object getListItem(OID cid, long index)
        throws Exception
    {
        ResultSet rs = 
            executeQuery("select value from lists where "+
                         "id="+cid.localId()+" order by index limit 1 offset "+index);
        if (rs.next()) {
            return ValueConverter.stringToObject(this,rs.getString("value"));
        } else {
            return null;      
        }
    }

    public long getIndexInList(OID cid, Object value)
        throws Exception
    {
        ResultSet rs = executeQuery(
            "select min(index) as index from lists where "+
            "id="+cid.localId()+" and value='"+
            addSlashes(ValueConverter.objectToString(this,value))+"'");
        if (rs.next()) {
            long index = rs.getLong(1);
            return getLong("select count(*) from lists where id="+cid.localId()+
                           " and index<="+index)-1;
        } else {
            return -1;
        }
    }

    protected long getInternalIndexInList(OID cid, Object value)
        throws Exception
    {
        ResultSet rs = executeQuery(
            "select min(index) as index from lists where "+
            "id="+cid.localId()+" and value='"+
            addSlashes(ValueConverter.objectToString(this,value))+"'");
        if (rs.next()) {
            long result = rs.getLong(1);
            if (rs.wasNull())
                return -1;
            else
                return result;
        } else {
            return -1;
        }
    }

    public long getLastIndexInList(OID cid, Object value)
        throws Exception
    {
        ResultSet rs = executeQuery(
            "select max(index) from lists where "+
            "id="+cid.localId()+" and value='"+
            addSlashes(ValueConverter.objectToString(this,value))+"'");
        if (rs.next()) {
            long index = rs.getLong(1);
            return getLong("select count(*) from lists where id="+cid.localId()+
                           " and index<="+index)-1;
        } else {
            return -1;      
        }
    }

    public void addToList(OID cid, long position, Object value)
        throws Exception
    {
        logger.debug("addToList("+cid+","+position+","+value+")");
        executeUpdate("update lists set index=index+1 where id="+cid.localId()+
                      " and index>="+position);
        executeUpdate(
            "insert into lists (id,index,value) values "+
            "("+cid.localId()+","+position+",'"+
            addSlashes(ValueConverter.objectToString(this,value))+"')");
    }

    public void addToList(OID cid, Object value)
        throws Exception
    {
        logger.debug("addToList("+cid+","+value+")");
        long size = getListSize(cid);
        String indexExpr;
        if (size==0)
            indexExpr = "0";
        else
            indexExpr = "select max(index)+1 from lists where id="+cid.localId();
        executeUpdate(
            "insert into lists (id,index,value) values "+
            "("+cid.localId()+",("+indexExpr+"),'"+
            addSlashes(ValueConverter.objectToString(this,value))+"')");
    }

    public void setListItem(OID cid, long index, Object value)
        throws Exception
    {
        logger.debug("setListItem("+cid+","+index+","+value+")");
        executeUpdate("update lists set value='"+
                      addSlashes(ValueConverter.objectToString(this,value))+
                      " where id="+cid.localId()+" and index="+index);
    }

    public void removeFromList(OID cid, long position)
        throws Exception
    {
        logger.debug("removeFromList("+cid+","+position+")");
        // First, get the index for the position
        ResultSet rs = executeQuery("select index from lists where "+
                                    "id="+cid.localId()+" order by index limit 1,"+position);
        long index = rs.getLong("index");
        executeUpdate("delete from lists where "+"id="+cid.localId()+" and index="+index);
    }

    public void removeFromList(OID cid, Object value)
        throws Exception
    {
        logger.debug("removeFromList("+cid+","+value+")");
        long index = getInternalIndexInList(cid,value);
        executeUpdate("delete from lists where "+"id="+cid.localId()+" and index="+index);
    }

    // Set methods

    public void clearSet(OID cid) 
        throws Exception 
    {
        logger.debug("clearSet("+cid+")");
        executeUpdate("delete from sets where id="+cid.localId());
    }

    public List getSet(OID oid, CollectionItem collection) throws Exception {
        return getSet(getCollectionID(oid,collection));
    }   

    public List getSet(OID cid) throws Exception {
        logger.debug("getSet("+cid+")");
        ResultSet rs = executeQuery("select value from sets "+
                                    "where id="+cid.localId());
        Vector result = new Vector();
        while (rs.next()) {
            result.add(
                ValueConverter.stringToObject(this,rs.getString("value")));
        }
        logger.debug("getSet returns " + result);
        return result;
    }   

    public long getSetSize(OID cid)
        throws Exception
    {
        return getLong("select count(*) from sets where id="+cid.localId());
    }

    public boolean setContains(OID cid, Object value) 
        throws Exception 
    {
        return collectionContains("sets",cid,value);
    }

    public boolean addToSet(OID cid, Object value) 
        throws Exception 
    {
        logger.debug("addToSet("+cid+","+value+")");
        if (!collectionContains("sets",cid,value)) {
            executeUpdate(
                "insert into sets (id,value) values "+"("+cid.localId()+",'"+
                addSlashes(ValueConverter.objectToString(this,value))+"')");
            return true;
        } else {
            return false;
        }
    }

    public boolean removeFromSet(OID cid, Object value) 
        throws Exception 
    {
        logger.debug("removeFromSet("+cid+","+value+")");
        boolean result = collectionContains("set",cid,value);
        if (result)
            executeUpdate(
                "delete from sets where "+
                "id="+cid.localId()+" and value='"+
                addSlashes(ValueConverter.objectToString(this,value))+"'");
        return result;
    }

    // Map functions

    public void clearMap(OID cid) 
        throws Exception 
    {
        logger.debug("clearMap("+cid+")");
        executeUpdate("delete from maps where id="+cid.localId());
    }

    public Map getMap(OID oid, CollectionItem collection) 
        throws Exception
    {
        return getMap(getCollectionID(oid,collection));
    }

    public Map getMap(OID cid) throws Exception
    {
        logger.debug("getMap("+cid+")");
        ResultSet rs =
            executeQuery("select value,key from maps where id="+cid.localId());
        Map result = new HashMap();
        while (rs.next()) {
            result.put(
                ValueConverter.stringToObject(this,rs.getString("key")),
                ValueConverter.stringToObject(this,rs.getString("value")));
        }
        logger.debug("getMap returns " + result);
        return result;
    }

    public long getMapSize(OID cid)
        throws Exception
    {
        return getLong("select count(*) from maps where id="+cid.localId());
    }

    public Object putInMap(OID cid, Object key, Object value) 
        throws Exception
    {
        logger.debug("putInMap("+cid+","+key+"->"+value+")");
        if (mapContainsKey(cid,key)) {
            Object old = getFromMap(cid,key);
            executeUpdate(
                "update maps set "+
                "key='"+addSlashes(ValueConverter.objectToString(this,key))+"',"+
                "value='"+addSlashes(ValueConverter.objectToString(this,value))+"' "+
                "where id="+cid.localId()+
                " and key='"+addSlashes(ValueConverter.objectToString(this,key))+"'");
            return old;
        } else {
            executeUpdate(
                "insert into maps (id,key,value) values "+
                "("+cid.localId()+",'"+
                addSlashes(ValueConverter.objectToString(this,key))+
                "','"+addSlashes(ValueConverter.objectToString(this,value))+"')");
            return null;
        }
    }

    public Object getFromMap(OID cid, Object key) 
        throws Exception
    {
        logger.debug("getFromMap("+cid+","+key+")");
        ResultSet res = executeQuery(
            "select value from maps where "+
            "id="+cid.localId()+" and key='"+
            addSlashes(ValueConverter.objectToString(this,key))+"'");
        if (res.next()) {
            return ValueConverter.stringToObject(this,res.getString("value"));
        } else {
            return null;
        }
    }

    public boolean mapContainsKey(OID cid, Object key) 
        throws Exception
    {
        logger.debug("mapContainsKey("+cid+","+key+")");
        ResultSet res = executeQuery(
            "select value from maps where "+
            "id="+cid.localId()+" and key='"+
            addSlashes(ValueConverter.objectToString(this,key))+"'");
        return res.next();
    }

    public boolean mapContainsValue(OID cid, Object value) 
        throws Exception 
    {
        return collectionContains("maps",cid,value);
    }

    public Object removeFromMap(OID cid, Object key)
        throws Exception

    {
        logger.debug("removeFromMap("+cid+","+key+")");
        if (!mapContainsKey(cid,key)) {
            return null;
        } else {
            Object result = getFromMap(cid,key);
            executeUpdate(
                "delete from maps where "+
                "id="+cid.localId()+" and key='"+
                addSlashes(ValueConverter.objectToString(this,key))+"'");
            return result;
        }
    }

    public abstract String newName(String className) throws Exception;

    public abstract Map getNameCounters() throws Exception;

    public abstract void updateNameCounters(Map counters) throws Exception;

    public OID getOIDFromName(String name) 
        throws Exception
    {
        ResultSet rs = executeQuery(
            "select id from roots where name='"+name+"'");
        if (rs.next()) {
            return new LongOID(this,rs.getLong("id"));
        } else {
            return null;
        }
    }
   
    public String getNameFromOID(OID oid) throws Exception
    {
        ResultSet rs = executeQuery(
            "select name from roots where id="+oid.localId());
        if (rs.next()) {
            return rs.getString("name");
        } else {
            return null;
        }
    }

    public void bindOIDToName(OID oid, String name) throws Exception
    {
        logger.debug("bindOIDToName "+oid+" -> "+name);
        executeUpdate("insert into roots (id,name) values ("+
                      oid.localId()+",'"+name+"')");
    }

    public void deleteName(String name) throws Exception
    {
        logger.debug("deleteName("+name+")");
        executeUpdate("delete from roots where name='"+name+"'");
    }

    public String getClassID(OID oid) throws Exception
    {
        ResultSet rs = executeQuery("select classid from classes where id="+oid.localId());
        if (rs.next()) {
            String classID = rs.getString("classid");
            logger.debug("getClassID("+oid+") -> "+classID);
            return classID;
        } else {
            throw new NoSuchOIDError(oid);
        }
    }

    public Collection getObjects(ClassItem cl) throws Exception
    {
        logger.debug("getObjects("+cl.getName()+")");
        Vector result = new Vector();
        getObjects(cl,result);
        return result;
    }

    protected void getObjects(ClassItem cl, Vector objects) throws SQLException {
        String query = "select id from classes";
        if (cl != null) {
            query += " where classes.classid='"+cl.getName()+"'";
        }
               
        ResultSet rs = executeQuery(query);
        while (rs.next()) {
            objects.add(new LongOID(this,rs.getLong("id")));
        }

        Iterator i = cl.getChildren().iterator();
        while(i.hasNext()) {
            ClassItem subclass = (ClassItem)i.next();
            getObjects(subclass,objects);
        }
    }

    public void startTransaction() throws SQLException {
        execute("BEGIN TRANSACTION");
    }

    public void commit() throws SQLException {
        execute("COMMIT");
    }

    public void rollback() throws SQLException {
        execute("ROLLBACK");
    }

    /**
     * Throw an exception if storage is null or invalid
     *
     */
    protected void checkStorage() {
        if (db==null) {
            logger.error("connection is NULL");
            throw new InvalidStorageException("connection is NULL");
        }
    }

    /**
     * Creates a new object using a PostgreSQL sequance.<p>
     *
     * @return the new object OID
     */
    public OID createObject(String className) throws Exception {
        LongOID res = new LongOID(this,getNextVal("object_id"));
        executeUpdate("insert into classes (id,classid) values ("+
                      res.localId()+",'"+className+"')");
        return res;
    }

    /**
     * Returns the next value of a sequence
     */
    public abstract long getNextVal(String sequence) throws Exception;

    public long getLong(String query) throws Exception {
        ResultSet rs = executeQuery(query);
        rs.next();
        return rs.getLong(1);
    }

    public int getInt(String query, int defaultValue) throws Exception {
        ResultSet rs = executeQuery(query);
        if (rs.next())
            return rs.getInt(1);
        else
            return defaultValue;
    }

    public OID getOID(String query) throws Exception {
        return new LongOID(this,getLong(query));
    }

    public static class InvalidStorageException extends RuntimeException {
        public InvalidStorageException(String msg) { 
            super(msg); 
        }
    }

    /* add '\' before ''' and '\' */
    public static String addSlashes(String str) {
        StringBuffer res = new StringBuffer(str.length());
        for (int i=0; i<str.length();i++) {
            if (str.charAt(i)=='\'' || str.charAt(i)=='\\') {
                res.append('\\');
            }
            res.append(str.charAt(i));
        }
        return res.toString();
    }

    PersistenceAC ac;
}
