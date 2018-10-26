/*
  Renaud Pawlak, pawlak@cnam.fr, CEDRIC Laboratory, Paris, France.
  Lionel Seinturier, Lionel.Seinturier@lip6.fr, LIP6, Paris, France.

  JAC-Core is free software. You can redistribute it and/or modify it
  under the terms of the GNU Library General Public License as
  published by the Free Software Foundation.
  
  JAC-Core is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

  This work uses the Javassist system - Copyright (c) 1999-2000
  Shigeru Chiba, University of Tsukuba, Japan.  All Rights Reserved.  */

package org.objectweb.jac.core.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;


/**
 * Lib is a container class for various utility method used by org.objectweb.jac.
 *
 * None of these methods is attached to any particular jac files but
 * should be, in an ideal world, provided by the JDK.
 */
 
 
public class Lib {

   /**
    * Execute a command in a process and dump its standard output and error.
    *
    * @param command  the command to execute
    */
   
   public static void exec(String command) {
   
       Runtime runtime = Runtime.getRuntime();
       
       try {
	  
	  Process p = runtime.exec(command);
	  
	  byte[] buf = new byte[1024];
	  int len;
	  
	  
	  /** Dump the output stream of the process */
	  
	  InputStream in = new BufferedInputStream( p.getInputStream() );
	  for ( len=in.read(buf) ; len != -1 ; len=in.read(buf) )
	     System.out.write( buf, 0, len );
	  

	  /** Dump the error stream of the process */
	  
	  in = new BufferedInputStream( p.getErrorStream() );
	  for ( len=in.read(buf) ; len != -1 ; len=in.read(buf) )
	     System.err.write( buf, 0, len );
	  
	  
	  /** Wait for the end of the process */
	  
	  p.waitFor();
	  
	}
	catch ( Exception e ) {
	   e.printStackTrace();
	   System.exit(1);
	}
   }
   
   
   /**
    * Transform strings stored as a enumeration object into
    * a space-separated string.
    *
    * @param stringsEnum  the enumeration object containing the strings
    * @return             a space-separated string composed of
    *			  the string contained in <I>stringsEnum</I>
    */
   
   public static String stringsEnumToString(Enumeration stringsEnum) {
   
      String str = "";
      
      while ( stringsEnum.hasMoreElements() ) {
         str += (String) stringsEnum.nextElement() + " ";
      }
      
      return str;
   }


   /**
    * Transform strings stored as a enumeration object into
    * an array of strings.
    *
    * @param stringsEnum  the enumeration object containing the strings
    * @return             an array of strings composed of
    *			  the string contained in <I>stringsEnum</I>
    */
   
   public static String[] stringsEnumToStringArray(Enumeration stringsEnum) {
   
      String str = stringsEnumToString(stringsEnum);     
      StringTokenizer strTok = new StringTokenizer(str);
      int len = strTok.countTokens();
      
      String[] strArray = new String[len];
      
      for (int i=0; i<len ; i++) {
         strArray[i] = new String(strTok.nextToken());
      }
      
      return strArray;
   }


   /**
    * Transform object stored as a enumeration object into
    * an array of object.
    *
    * @param enum  the enumeration object containing the objects
    * @return      an array of objects composed of
    *		   the objects contained in <I>enum</I>
    */
   
   public static Object[] enumToArray(Enumeration enum) {
   
      Vector elements = new Vector();
      
      while ( enum.hasMoreElements() )
         elements.add( enum.nextElement() );
   
      return elements.toArray();
   }


   /**
    * Transform a string composed of substrings separated by spaces into
    * an array composed of the substrings.
    *
    * @param str the string composed of substrings separated by spaces 
    * @return an array composed of the substrings
    */
   
   public static String[] stringToStringArray(String str) {
   
      StringTokenizer strTok = new StringTokenizer(str);
      int len = strTok.countTokens();
      
      String[] strArray = new String[len];
      
      for (int i=0; i<len ; i++) {
         strArray[i] = new String(strTok.nextToken());
      }
      
      return strArray;
   }


   /**
    * Store a string array into a hashtable.
    *
    * @param strs  the string array
    * @return      the hashtable
    */
   
   public static Hashtable stringArrayToHashtable(String[] strs) {
   
      Hashtable ret = new Hashtable();
      
      for (int i=0; i<strs.length; i++) {
         ret.put(strs[i], "");
      }
      
      return ret;
   }


   /**
    * Return the byte code contained in file
    * <I>dirName</I>.<I>fileName</I>.class
    *
    * @param dirName   the directory where the file is stored
    * @param fileName  the file name
    * @return          the byte code
    */
   
   public static byte[] loadByteCodeFromFile(String dirName, String fileName)
   {
   
      byte[] byteCode = null;
   
      try {
      
         String fullFileName =
	    dirName + new String(fileName).replace('.','/') + ".class";

	 File file = new File( fullFileName );
	 long fileSize = file.length();
	 if ( fileSize == 0 )  return null;
	 
	 byteCode = new byte[ (int) fileSize ];
	 InputStream in = new BufferedInputStream( new FileInputStream(file) );
	 if ( in.read(byteCode) != fileSize )  return null;

      }
      catch( Exception e ) { e.printStackTrace(); }

      return byteCode;
   }

   
   /**
    * Serialize an object into an array of bytes.
    *
    * @param src  the object to serialize
    * @return     an array of bytes
    */
    
   public static byte[] serialize(Object src) {
   
      return serialize(src, ObjectOutputStream.class);
   }
   
   
   /**
    * Serialize an object into an array of bytes.
    *
    * @param src       the object to serialize
    * @param oosClass  the subclass of ObjectOutputStream
    *                  to use for serializing src
    * @return          an array of bytes
    */
    
   public static byte[] serialize(Object src, Class oosClass) {

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      
      ObjectOutputStream oos = null;
      byte[] ret = null;
      
      try {
      
	 oos = 
	    (ObjectOutputStream)
	       oosClass.
	       getConstructor( new Class[]{OutputStream.class} ).
	       newInstance( new Object[]{baos} );

         oos.writeObject( src );
         oos.close();
      
         ret = baos.toByteArray();
         baos.close();
      
      }
      catch( Exception e ) { e.printStackTrace(); }
      
      return ret;
   }
   
   
   /**
    * Deserialize an object from an array of bytes.
    *
    * @param buf  the array of bytes
    * @return     the object or null if a error has been encountered
    */
    
   public static Object deserialize(byte[] buf) {
   
      return deserialize( buf, ObjectInputStream.class );
   }
   
   
   /**
    * Deserialize an object from an array of bytes.
    *
    * @param data      the array of bytes
    * @param oisClass  the subclass of ObjectInputStream
    *                  to use for deserializing src
    * @return          the object or null if a error has been encountered
    */
    
   public static Object deserialize( byte[] data, Class oisClass ) {
   
      ByteArrayInputStream bais = new ByteArrayInputStream(data);
      
      ObjectInputStream ois = null;
      Object ret = null;
      
      try {
      
	 ois = 
	    (ObjectInputStream)
	       oisClass.
	       getConstructor( new Class[]{InputStream.class} ).
	       newInstance( new Object[]{bais} );

         ret = ois.readObject();
         ois.close();
         bais.close();
      
      }
      catch( Exception e ) { e.printStackTrace(); }
      
      return ret;
   }


   /**
    * Get fields name.
    *
    * @param src  the source object containing the fields
    * @return     the fields name as an array of strings
    */
   
   public static String[] getFieldsName( Object src ) {
   
      String[] fieldsName = null;
      
      try {
      
         Field[] fields = src.getClass().getFields();
	 fieldsName = new String[ fields.length ];
            
         for ( int i=0 ; i < fields.length ; i++ ) {
            fieldsName[i] = fields[i].getName();
         }

      }
      catch( Exception e ) { e.printStackTrace(); }
      
      return fieldsName;
   }
      

   /**
    * Get fields value.
    *
    * @param src  the source object containing the fields
    * @return     the fields value as an array of objects
    */
   
   public static Object[] getFieldsValue( Object src ) {
   
      Object[] fieldsValue = null;
      
      try {
      
         Field[] fields = src.getClass().getFields();
	 fieldsValue = new Object[ fields.length ];
            
         for ( int i=0 ; i < fields.length ; i++ ) {
            fieldsValue[i] = fields[i].get( src );
         }

      }
      catch( Exception e ) { e.printStackTrace(); }
      
      return fieldsValue;
   }
   
   
   /**
    * Get fields value.
    *
    * @param src         the source object containing the fields
    * @param fieldsName  the fields name
    * @return            the fields value as an array of objects
    */
   
   public static Object[] getFieldsValue( Object src, String[] fieldsName ) {
   
      Object[] fieldsValue = null;
      
      try {
      
         Class cl = src.getClass();
	 fieldsValue = new Object[ fieldsName.length ];
            
         for ( int i=0 ; i < fieldsName.length ; i++ ) {
            fieldsValue[i] = cl.getField(fieldsName[i]).get( src );
         }

      }
      catch( Exception e ) { e.printStackTrace(); }
      
      return fieldsValue;
   }
   
   
   /**
    * Set fields value.
    *
    * @param src the source object
    * @param fieldsName the fields name
    * @param fieldsValue the fields value
    */
    
   public static void setFieldsValue(Object src, String[] fieldsName, Object[] fieldsValue) {
      try {
         Class cl = src.getClass();
         for (int i=0; i<fieldsName.length; i++) {
            cl.getField(fieldsName[i]).set(src, fieldsValue[i]);
         }
      } catch (Exception e) { 
         e.printStackTrace(); 
      }
   }
   
   /**
    * Get classes.
    *
    * @param objs  the objects as an array
    * @return      the array of classes where each element is the class
    *		   of the corresponding object
    */
   
   public static Class[] getClasses( Object[] objs ) {
   
      Class cl;
      Field fieldTYPE;
      Class[] cls = new Class[ objs.length ];
      
      for ( int i=0 ; i < objs.length ; i++ ) {
      
         if( objs[i] != null ) {
            cl = objs[i].getClass();

            /**
             * Check whether the class is a wrapper for a primitive type.
             * Heuristic used: search for a field called TYPE.
             */
	    
            try {
               fieldTYPE = cl.getField("TYPE");
               cls[i] = (Class) fieldTYPE.get( objs[i] );
            }
            catch( NoSuchFieldException e )   { cls[i] = cl; }
            catch( IllegalAccessException e ) { cls[i] = cl; }
         } else {
            cls[i] = Object.class;
         }
      }
      return cls;
   }


   /**
    * Recursively pretty prints an array.
    *
    * @param o  the array
    */
   
   public static void printArray( Object o ) {

      if ( o == null ) {
         System.out.print ( " <null> " );
         return;
      }

      if ( o.getClass().isArray() ) {

         System.out.print ( "[ " );

         for ( int i = 0; i < ((Object[])o).length; i++ ) {
            printArray( ((Object[])o)[i] );
         }

         System.out.print ( "]" );

      } else {
         System.out.print ( o + " " );
      }
  
   }    
      
}
