/*
  Copyright (C) 2001-2003 Lionel Seinturier <Lionel.Seinturier@lip6.fr>

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

package org.objectweb.jac.aspects.distrans.persistence;

import org.objectweb.jac.core.NameRepository;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.util.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import org.enhydra.jdbc.standard.StandardXADataSource;

/**
 * Basic transaction-enabled persistence storage.
 *
 * Data is stored in SQL tables.
 * Each table contains object field values and owns one more attribute
 * than its associated class contains field. The additional attribute
 * stores the object name.
 * 
 * @author Lionel Seinturier <Lionel.Seinturier@lip6.fr>
 * @version 1.0
 */
public class SimpleDbPersistence implements PersistenceItf {

    /**
     * The name of the attribute storing the persistent object name
     * in SQL tables.
     */
    final static private String objectAttributeName = "_objname";

    
    /**
     * Create a SQL table to hold persistent data.
     * Implements PersistenceItf#createStorageIfNeeded()
     * 
     * The table is not created if it already exists. This enables to reuse
     * existing tables to map their content to JAC objects. However, this may
     * cause problems if the schema is different than the expected one. In
     * such a case, call createStorage instead.
     * 
     * @param className   the class name for which we want to create a table
     * @param ds          the data source
     */
    public void initStorageIfNeeded( String className, StandardXADataSource ds ) {
        try {
			_initStorageIfNeeded(className,ds);
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
    }
    
    private void _initStorageIfNeeded( String className, StandardXADataSource ds )
        throws SQLException {

        /**
         * Check whether a table exists for each element of classNames.
         * The table name is the class name where dots delimiting package
         * names are replaced by underscores.
         */
        Connection connection = XAPoolCache.getConnection(ds);
        
        String tableName = className.replace('.','_');                
        Statement st = connection.createStatement();
        try {
            st.executeQuery("SELECT * FROM "+tableName);
        }
        catch (SQLException sqle) {
            /**
             * If the table does not exist, the expected behavior is
             * to throw a SQLException. In such a case, create the table.
             */
            initStorage(className,connection);
        }
    }
    
    
    /**
     * Create a SQL table to hold persistent data.
     * If the table exists, it is deleted first. 
     * Implements PersistenceItf#createStorage()
     * 
     * @param className   the class name for which we want to create a table
     * @param ds          the data source
     */
    public void initStorage( String className, StandardXADataSource ds ) {
        try {
			_initStorage(className,ds);
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
    }
    
    private void _initStorage( String className, StandardXADataSource ds )
        throws SQLException {

        /**
         * Check whether a table exists for each element of classNames.
         */
        Connection connection = XAPoolCache.getConnection(ds);
        
        /**
         * The table name is the class name where dots delimiting package
         * names are replaced by underscores.
         */
        String tableName = className.replace('.','_');                
        Statement st = connection.createStatement();
        try {
            st.executeQuery("DROP TABLE "+tableName);
        }
        catch (SQLException sqle) {
            /**
             * If the table does not exist, the expected behavior is
             * to throw a SQLException.
             */
        }
        
        initStorage(className,connection);
    }

    
    /**
     * Create a SQL table to hold persistent data.
     * 
     * @param className   the class name
     * @param connection  the connection towards the database
     */
    private void initStorage( String className, Connection connection )
        throws SQLException {
        
        /**
         * The table name is the class name where dots delimiting package names
         * are replaced by underscores.
         */
        String tableName = className.replace('.','_');
                
        String sql =
            "CREATE TABLE "+tableName+" ( "+
            objectAttributeName+" VARCHAR PRIMARY KEY";
            
        ClassItem classItem = classes.getClass(className);
        Collection fields = classItem.getAllFields();
        for (Iterator iter = fields.iterator(); iter.hasNext();) {
            FieldItem field = (FieldItem) iter.next();
            String fieldName = field.getName();
            String fieldType = field.getType().getName();
            String sqlType = (String) SimpleDbPersistence.javaTypesToSQL.get(fieldType);
            sql += ", "+fieldName+" "+sqlType;
        }
        sql += ");";
        
        Statement st = connection.createStatement();
        st.executeUpdate(sql);
        st.close();
    }
    
    /**
     * Store mappings between Java types and SQL ones.
     * For now on, this is only a quick-and-dirty partial mapping.
     * To be completed.
     */
    private static Map javaTypesToSQL = new HashMap();
    static {
        javaTypesToSQL.put("java.lang.String","VARCHAR");
        javaTypesToSQL.put("int","INT");
        javaTypesToSQL.put("double","DOUBLE PRECISION");
    }
    
    
    /**
     * Store an object into the persistence storage.
     * Implements PersistenceItf#load()
     * 
     * @param wrappee  the object to store
     * @param name     the identifier for the object
     * @param ds       the data source
     */
    public void load( Object wrappee, String name, StandardXADataSource ds )
        throws SQLException, IllegalArgumentException, IllegalAccessException, NotSupportedException, SecurityException, IllegalStateException, RollbackException, HeuristicMixedException, HeuristicRollbackException, SystemException {
                
        ClassItem classItem = classes.getClass(wrappee);
        
        /**
         * Get a prepared statement to fetch the data.
         * It may be cached in the selectStatements map.
         */
        Connection connection = XAPoolCache.getConnection(ds);

        PreparedStatement ps =
            createSelectStatement(connection,classItem);        
        ps.setString(1,name);
        ResultSet rs = ps.executeQuery();
        
        /**
         * The object hasn't been found in the storage.
         * The most likely reason is that it has never been
         * written before. In such a case, do nothing (ie don't
         * reflect any value on the wrappee).
         */
        boolean found = rs.next();
        if (!found)  return;
        
        /**
         * Set fields with values retrieved from the ResultSet.
         * Start at index 2 to skip the wrappeeName.
         */
        Collection fields = classItem.getAllFields();
        int i=2;
        for (Iterator iter = fields.iterator(); iter.hasNext();i++) {
            FieldItem field = (FieldItem) iter.next();
            Object value = rs.getObject(i);
            field.set(wrappee,value);
        }
        rs.close();
    }

    
    /**
     * Update an object with the values retrieved from the persistent
     * storage.
     * Implements PersistenceItf#store()
     * 
     * @param wrappee  the object to update
     * @param name     the identifier for the object
     * @param ds       the data source
     */
    public void store( Object wrappee, String name, StandardXADataSource ds )
        throws Exception {

        ClassItem classItem = classes.getClass(wrappee);
        
        /**
         * Save the wrappee field values into the database.
         * Get a prepared statement to update the data.
         * It may be cached in the updateStatements map.
         */
        Connection connection = XAPoolCache.getConnection(ds);
        
        PreparedStatement ps =
            createUpdateStatement(connection,classItem);
        
        Collection fields = classItem.getAllFields();
        int i=1;
        for (Iterator iter = fields.iterator(); iter.hasNext();i++) {
            FieldItem field = (FieldItem) iter.next();
            Object value = field.get(wrappee);
            ps.setObject(i,value);
        }
        ps.setString(i,name);
        
        /**
         * Update the data in the table.
         * If 0 row have been updates, the row didn't exist yet, create it.
         */
        int rowCount = ps.executeUpdate();
        
        if ( rowCount == 0 ) {
            PreparedStatement insertps =
                createInsertStatement(
                    connection,classItem,wrappee,name
                );
            insertps.executeUpdate();
            insertps.close();
            ps.executeUpdate();
        }
    }

    /**
     * Create an INSERT SQL request to store the field values of an object.
     * Each field is mapped onto a SQL attribute.
     * The table contains one more attribute which is the name of the wrappee,
     * and which is also the primary key of the table.
     * 
     * @param connection   the connection towards the database
     * @param cl           the class
     * @param wrappee      the persistent object (ie the wrappee)
     * @param wrappeeName  the wrappee name
     * @return             the prepared statement
     */
    private static PreparedStatement createInsertStatement(
        Connection connection, ClassItem cl,
        Object wrappee, String wrappeeName ) throws SQLException {
            
        /**
         * The table name is the class name where dots delimiting package names
         * are replaced by underscores.
         */
        String tableName = cl.getName();
        tableName = tableName.replace('.','_');
        String request = "INSERT INTO "+tableName+" VALUES (?";
    
        Collection fields = cl.getAllFields();
        for (Iterator iter = fields.iterator(); iter.hasNext();) {
            iter.next();
            request += ",?";
        }
    
        request += ");";
        
        PreparedStatement ps = connection.prepareStatement(request);
    
        ps.setString(1,wrappeeName);
        int i=2;
        for (Iterator iter = fields.iterator(); iter.hasNext();i++) {
            iter.next();
            ps.setObject(i,null);
        }
        
        return ps;
    }
    
    /**
     * Create a SELECT SQL request to fetch the field values of an object.
     * Each field is mapped onto a SQL attribute.
     * The table contains one more attribute which is the name of the wrappee,
     * and which is also the primary key of the table.
     * 
     * @param connection  the connection towards the database
     * @param cl          the class
     * @return            the prepared statement
     */
    static private PreparedStatement createSelectStatement(
        Connection connection, ClassItem cl ) throws SQLException {
            
        /**
         * The table name is the class name where dots delimiting package names
         * are replaced by underscores.
         */
        String tableName = cl.getName();
        tableName = tableName.replace('.','_');
        String request =
            "SELECT * FROM "+tableName+" WHERE "+
            objectAttributeName+"=?;";

        return connection.prepareStatement(request);
    }

    /**
     * Create an UPDATE SQL request to store the field values of an object.
     * Each field is mapped onto a SQL attribute.
     * The table contains one more attribute which is the name of the wrappee,
     * and which is also the primary key of the table.
     * 
     * @param connection  the connection towards the database
     * @param cl          the class
     * @return            the prepared statement
     */
    static private PreparedStatement createUpdateStatement(
        Connection connection, ClassItem cl ) throws SQLException {
            
        /**
         * The table name is the class name where dots delimiting package names
         * are replaced by underscores.
         */
        String tableName = cl.getName();
        tableName = tableName.replace('.','_');
        String request = "UPDATE "+tableName+" SET ";
        
        Collection fields = cl.getAllFields();
        boolean first = true;
        for (Iterator iter = fields.iterator(); iter.hasNext();) {
            FieldItem field = (FieldItem) iter.next();
            String name = field.getName();
            
            if (first) first = false;
            else request += ",";
            request += name + "=?";
        }
        
        request += " WHERE "+objectAttributeName+"=?;";
        
        return connection.prepareStatement(request);
    }

    /**
     * The reference towards the RTTI class repository.
     */
    private ClassRepository classes = ClassRepository.get();

    /**
     * The reference towards the JAC name repository.
     * Needed by applyPersistence().
     */
    private Repository names = NameRepository.get();
}
