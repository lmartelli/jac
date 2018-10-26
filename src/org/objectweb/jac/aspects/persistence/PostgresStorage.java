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
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Map;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.objectweb.jac.util.Log;
import org.objectweb.jac.util.Strings;


/**
 * Implements the storage to store within a PostgreSQL database.
 */

public class PostgresStorage extends SQLStorage {
    static Logger logger = Logger.getLogger("persistence.storage");

    /** Storage version */
    static final int version = 1;

    /**
     * Creates a new storage for a PostgreSQL database.
     *
     * @param database the database. It may take on eof the following form:
     * <ul>
     *   <li>database</li>
     *   <li>//host/database</li>
     *   <li>//host:port/database</li>
     * </ul>
     * @param user the user name
     * @param password the password for this user
     */
    public PostgresStorage(PersistenceAC ac, 
                           String database, String user, String password) 
        throws SQLException, Exception
    {
        super(ac);
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            logger.error("failed to load postgresql JDBC driver",e);
            return;
        }
        try {
            Connection db;
            db = DriverManager.getConnection("jdbc:postgresql:"+database,user,password);
            setConnection(db);
        } catch (SQLException e) {
            logger.error("failed to connect to the database with "+
                         "jdbc:postgresql:"+database+","+user+","+password,e);
            return;
        }

        try {
            doUpdates();
        } catch (Exception e) {
            logger.error("doUpdates failed on "+database,e);
        }
    }

    protected String[] getClassNames() throws Exception {
        ResultSet rs = executeQuery("SELECT DISTINCT classid FROM classes");
        HashSet names = new HashSet();
        while (rs.next()) {
            names.add(rs.getString("classid"));
        }
        return (String[])names.toArray(new String[0]);
    }

    protected void doUpdates() throws Exception, SQLException {
        if (!hasTable("storage")) {
            execute(
                "create table \"storage\" ("+
                "\"key\" character varying PRIMARY KEY, "+
                "\"value\" character varying )");
        }
        int currentVersion = getInt("select value from storage where key='version'",0);

        if (currentVersion<=0) {
            logger.info("Upgrading from version 0");
            // Update counter names
            String[] classes = getClassNames();
            HashSet seqs = new HashSet();
            for (int i=0; i<classes.length; i++) {
                String seq = Strings.getShortClassName(classes[i]).toLowerCase()+"_seq";
                if (seqs.contains(seq))
                    throw new RuntimeException(
                        "Cannot upgrade storage: two classes have the same seq name '"+seq+"'");
                seqs.add(seq);
            }
             
            for (int i=0; i<classes.length; i++) {
                String seq = Strings.getShortClassName(classes[i]).toLowerCase()+"_seq";
                if (hasSequence(seq)) {
                    logger.debug("Renaming sequence "+seq+" to "+classes[i]);
                    executeUpdate("ALTER TABLE "+seq+" RENAME to \""+classes[i]+"\"");
                } else {
                    logger.debug("No such sequence "+seq);
                }
            }
            
        }

        if (currentVersion==0)
            executeUpdate("insert into storage values ('version',"+version+") ");
        else
            executeUpdate("update storage set value="+version+" where key='version'");
    }

    public long getNextVal(String sequence) throws Exception {
        ensureSequenceExists(sequence);
        return getLong("select nextval('\""+sequence+"\"')");
    }

    public long getCurrVal(String sequence) throws Exception {
        ensureSequenceExists(sequence);
        return getLong("select last_value from \""+sequence+"\"");
    }

    protected boolean ensureSequenceExists(String sequence) throws Exception {
        boolean created = false;
        if (!createdSequences.contains(sequence)) {
            if (!hasSequence(sequence)) {
                execute("create sequence \""+sequence+"\"");
                created = true;
            }
            createdSequences.add(sequence);
        }
        return created;
    }

    HashSet createdSequences = new HashSet();
    public String newName(String className) throws Exception {
        String prefix = Strings.getShortClassName(className).toLowerCase();
        String seq = getClassSeqName(className);
        return prefix+"#"+(getNextVal(seq)-1);
    }

    public Map getNameCounters() throws Exception {
        Hashtable counters = new Hashtable();
        String[] classes = getClassNames();
        for (int i=0; i<classes.length; i++) {
            String seq = getClassSeqName(classes[i]);
            counters.put(classes[i],new Long(getCurrVal(seq)));
        }

        return counters;
    }

    protected String getClassSeqName(String className) {
        return className;
    }

    protected boolean hasTable(String name) throws SQLException {
        ResultSet rs = 
            executeQuery("select * from pg_tables where tablename='"+name+"'"); 
        return rs.next();
    }

    protected boolean hasSequence(String name) throws Exception {
        ResultSet rs = 
            executeQuery("select * from pg_class where relname='"+name+"' and relkind='S'"); 
        return rs.next();
    }

    public void updateNameCounters(Map counters) throws Exception {
        Iterator i = counters.entrySet().iterator();
        while (i.hasNext()) {
            Entry entry = (Entry)i.next();
            String sequence = getClassSeqName((String)entry.getKey());
            ensureSequenceExists(sequence);
            if (getCurrVal(sequence)<((Long)entry.getValue()).longValue()) 
                execute("select setval('\""+sequence+"\"',"+entry.getValue()+",true)");
        }
   }

}
