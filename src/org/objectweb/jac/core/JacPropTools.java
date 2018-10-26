/*
  Copyright (C) 2002 Fabrice Legond-Aubry.

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

package org.objectweb.jac.core;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import org.apache.log4j.Logger;

/**
 * This class provides useful primitives to parse Java property
 * files. */

public abstract class JacPropTools {
    static Logger logger = Logger.getLogger("props");

   /**
    * Extract a specific vector (ordered) property from a property list.
    * Then parse it and split it into tokens that will be
    * added to the "vector".
    *
    * @param vector the vetor where all extracted tokens will be
    * stored
    * @param pList the list of all properties from where we will
    * search the property
    * @param propertyName the name of the property to parse */
   public static void fillListStringProps(List vector,
                                          Properties pList,
                                          String propertyName,
                                          boolean force)
   {
      String prop = pList.getProperty(propertyName);
      if (prop == null) {
         logger.warn("\tNo property '"+propertyName +"' found.");
      }
      else {
         logger.debug("\tVECTOR Property '"+propertyName+"' found."); 
         StringTokenizer st = new StringTokenizer( prop );
         logger.debug("\tProperty elements added:");
         String tmp;
         while ( st.hasMoreElements() ) {
            try {
               tmp = (String)st.nextElement() ;
               logger.debug("\t\tElement: " + tmp);
               vector.add ( tmp );
            }
            catch (Exception e) {
               logger.debug("\t\tCan not get a reference for a class."); 
               e.printStackTrace(); 
            }				   

         }
      }
   }

   /**
    * Extract a specific hash set property from a property list. Then
    * parse it and split it into tokens that will be added to the
    * "hashSet".
    *
    * @param set the set where all extracted tokens will be stored
    * @param pList the list of all properties from where we will
    * search the property
    * @param propertyName the name of the property to parse 
    * @param trim wether to trim ending ".*"
    */
   public static void fillSetProps(Set set, 
                                   Properties pList, 
                                   String propertyName,
                                   boolean trim)
   { 
      String prop = pList.getProperty(propertyName);
      if (prop == null) {
         logger.debug("\t-- WARNING: no property '"+ 
                   propertyName +"' found.");
      } else {
         logger.debug("\tSET Property '"+
                   propertyName+"' found.");
         StringTokenizer st = new StringTokenizer( prop );
         logger.debug("\tProperty tokens added:");
         while ( st.hasMoreElements() ) {
            String element = (String)st.nextElement();
            String tmp;
	    // I remove this to implement a more general wildcard
            // policy (RP)
	    //if (element.endsWith(".*") && trim) tmp =
            //element.substring(0,element.length()-2); else
	    tmp = element;
            tmp = tmp.trim();
            set.add(tmp);
            logger.debug("\t\tToken: " + tmp);
         }
      }
   }
	

   /**
    * Extract a specific hashTable property from a property list.
    * Then parse it and split it into tokens that will be added to the
    * "hashTable".
    *
    * @param hashTable the hash table where all extracted tokens will
    * be stored
    * @param pList the list of all properties from where we will
    * search the property
    * @param propertyName the name of the property to parse
    * @param nElements the number of elements attached to a key (the
    * key is the first element). If nElements==0, the number of
    * elements attached to the key is variant and must be ending
    * with a '.'. */
   public static void fillMapProps(Map hashTable,
                                   Properties pList,
                                   String propertyName,
                                   int nElements,
                                   boolean force)
   { 
      String prop = pList.getProperty(propertyName);
      if (prop == null) {
         logger.debug("\t-- WARNING: no property '"+propertyName+"' found.");
      }
      else {
         logger.debug("\tHASHTABLE Property '"+propertyName+"' found.");
         StringTokenizer st = new StringTokenizer( prop );
         logger.debug("\tProperty couple added:");
         while ( st.hasMoreElements() ) {
            String key = (String) st.nextElement();
            Vector vvalue=new Vector();
            String value=null;
            if (nElements>1) {
               for(int i=0;i<nElements;i++) {
                  vvalue.add(((String)st.nextElement()).trim());
               }
            } else if(nElements==0) {
               String tmpvalue="";
               while(st.hasMoreElements()) {
                  tmpvalue=((String)st.nextElement()).trim();
                  if(tmpvalue.equals(".")) break;
                  vvalue.add(tmpvalue);
               }
            } else {
               value = ((String)st.nextElement()).trim();
            }
            if (force) {
               hashTable.put(key.trim(),value==null?(Object)vvalue:(Object)value);
               logger.debug("\t\t(key,value): ("+
                         key+","+(value==null?vvalue.toString():value)+")");
            } else if (!hashTable.containsKey(key)) {
               hashTable.put(key.trim(),value==null?(Object)vvalue:(Object)value);
               logger.debug("\t\t(key,value): ("+
                         key+","+(value==null?vvalue.toString():value)+")");
            }
         }
      }
   }
        

   /**
    * Extracts a specific String property from a property list.
    *
    * @param pList the list of all properties from where we will search the property
    * @param propertyName the name of the property to parse
    */
   public static String fillStringProp(Properties pList, String propertyName)
   {
      String tmp = pList.getProperty(propertyName);
      if ( tmp == null )
         logger.debug("\t-- WARNING: no property '"+propertyName+"' found.");
      else 
         logger.debug("\tSTRING Property '"+propertyName+"' found.");
      if ( tmp == null)
         return null;
      logger.debug("\tValue is "+tmp);
      return tmp.trim();
   }

   /**
    * Try to load the property file (propFileName) from the specified directory.
    *
    * @param directory the directory where we should, in theory, found the property file
    * @param name the name of the property file
    * @return true if the file was found and loaded, false otherwise
    */
   public static Properties getPropsFrom(String directory, String name)
   {
      Properties pList = new Properties();
      try {
         FileInputStream fis = new FileInputStream(directory + name);
         pList.load(fis);
         logger.debug("Properties file '"+name+
                   "' found in '"+directory +"'.");
         return pList;
      }
      catch (FileNotFoundException e) {
         logger.warn("No property file '"+
                   name+"' found in the directory: '"+directory+"'.");
         return null;
      }
      catch (IOException e) {
         logger.warn("Can not load file '"+
                     name+"' found in the directory: '" +
                   directory +"'.");
         e.printStackTrace();
         return null;
      }
   }


}
