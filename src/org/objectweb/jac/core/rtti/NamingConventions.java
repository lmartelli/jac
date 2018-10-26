/*
  Copyright (C) 2001-2002 Renaud Pawlak <renaud@aopsys.com>
  Laurent Martelli <laurent@aopsys.com>
  
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

package org.objectweb.jac.core.rtti;

import java.lang.reflect.*;
import java.util.*;

/**
 * This class provides some useful methods to get some information
 * regarding the naming conventions.
 *
 * @author Renaud Pawlak
 * @author Laurent Martelli
 */

public class NamingConventions {

   /** Constant to represent field modifiers. */
   public static final int MODIFIER = 0;

   /** Constant to represent field getters. */
   public static final int GETTER = 1;

   /** Constant to represent field setters. */
   public static final int SETTER = 2;

   /** Constant to represent collection adders. */
   public static final int ADDER = 3;

   /** Constant to represent collection removers. */
   public static final int REMOVER = 4;

   /** Store default field getter prefixes (get...). */
   public static final String[] getterPrefixes;

   /** Store default field setter prefixes (set...). */
   public static final String[] setterPrefixes;

   /** Store default collection adder prefixes (add..., put...). */
   public static final String[] adderPrefixes;

   /** Store default collection remover prefixes (rmv..., del...,
       remove...). */
   public static final String[] removerPrefixes;

   static {
      getterPrefixes  = new String[] { "get", "is" };
      setterPrefixes  = new String[] { "set" };
      adderPrefixes   = new String[] { "add","put" };
      removerPrefixes = new String[] { "rmv", "del", "remove", "clear" };
   }

   /**
    * Returns the short name of a class.
    *
    * <p>If it is a well-known java class or a well-known JAC class,
    * truncates the packages to give only the class name. It is is an
    * array, add [] after the primitive type.
    *
    * @param cli the class item
    */
   public static String getShortClassName(ClassItem cli) {
      return getShortClassName(cli.getActualClass());
   }

   /**
    * Returns the short name of a class.
    *
    * <p>If it is a well-known java class or a well-known JAC class,
    * truncates the packages to give only the class name. It is is an
    * array, add [] after the primitive type.
    *
    * @param cl the class
    */
   public static String getShortClassName(Class cl) {

      String type = cl.getName();

      if (cl.isArray()) {
         type = cl.getComponentType().getName();
      }

      if (type.startsWith("java") || type.startsWith( "org.objectweb.jac")) {
         type = type.substring(type.lastIndexOf( '.' )+1);
      }

      if (cl.isArray()) {
         type = type + "[]";
      }

      return type;
      
   }

   public static String getStandardClassName(Class cl) {
      String type = cl.getName();
      if ( cl.isArray() ) {
         type = cl.getComponentType().getName()+"[]";
      }
      return type;      
   }

   /**
    * Returns the short name of a given constructor.
    *
    * <p>By default, the name of a constructor contains the full path
    * name of the constructor class. Thus, this method is equivalent
    * to get the short name of the constructor class.
    * 
    * @param constructor a constructor 
    * @return its short name
    * @see #getShortClassName(Class) */

   public static String getShortConstructorName(Constructor constructor) {
      String name = constructor.getName();
      if ( name.lastIndexOf( '.' ) == -1 ) {
         return name;
      }
      return name.substring(name.lastIndexOf( '.' ) + 1);
   }

   /**
    * Returns the short name of a given constructor.
    *
    * <p>By default, the name of a constructor contains the full path
    * name of the constructor class. Thus, this method is equivalent
    * to get the short name of the constructor class.
    * 
    * @param constructor a constructor 
    * @return its short name
    * @see #getShortClassName(ClassItem) */

   public static String getShortConstructorName(ConstructorItem constructor) {
      return getShortConstructorName(constructor.getActualConstructor());
   }

   /**
    * Returns the package name of the given class.
    * 
    * @param cl the class to get the package of
    * @return the name of the package where <code>cl</code> is defined
    */

   public static String getPackageName(Class cl) {
      String type = cl.getName();

      return type.substring(0,type.lastIndexOf("."));
   }

   /**
    * Returns a printable representation of the argument types of a
    * method (or a constructor if needed).
    *
    * <p>For instance, for a method that takes one object, one string,
    * and one string array, the result will look like "Object, String,
    * String[]".
    * 
    * @param method the involved method
    * @return a printable string
    * @see #getPrintableParameterTypes(AbstractMethodItem) */

   public static String getPrintableParameterTypes(AccessibleObject method) {
      String ret = "(";
      
      Class[] pts;
      if ( method instanceof Constructor )
         pts = ((Constructor)method).getParameterTypes();
      else if ( method instanceof Method )
         pts = ((Method)method).getParameterTypes();
      else return "";
      for ( int j = 0; j < pts.length; j++ ) {
         ret = ret + getShortClassName( pts[j] );
         if ( j < pts.length - 1 ) ret = ret + ",";
      }
      ret = ret + ")";
      return ret;
   }

   /**
    * Returns a printable representation of the argument types of a
    * method (or a constructor if needed).
    *
    * <p>Same as its homonym but uing RTTI meta item.
    * 
    * @param method the involved method
    * @return a printable string
    * @see #getPrintableParameterTypes(AccessibleObject) */

   public static String getPrintableParameterTypes(AbstractMethodItem method) {
      String ret = "(";
      
      Class[] pts = method.getParameterTypes();
      for ( int j = 0; j < pts.length; j++ ) {
         ret = ret + getShortClassName( pts[j] );
         if ( j < pts.length - 1 ) ret = ret + ",";
      }
      ret = ret + ")";
      return ret;
   }

   /**
    * Returns a declared method of a class <code>c</code> only by
    * knowing its name.  Returns null if not found (to be modified to
    * raise an exception.
    *
    * @param c     the class.
    * @param name  the name of the method to find.
    * @return the method (null if not found).  */

   public static Method getDeclaredMethodByName(Class c, String name) {

      Method[] methods = c.getDeclaredMethods();

      for ( int i=0 ; i < methods.length ; i++) {
         if ( name.equals(methods[i].getName()) ) {
            return methods[i];
         }
      }

      return null;
   }

   /**
    * Tell if the string is prefixed with one of the given prefixes.
    * 
    * <p>Returns 0 if the candidate is not prefixed by any of the
    * given prefixes and also if the candidate exactly equals one of
    * the prefixes.
    *
    * @param candidate the string to test
    * @param prefixes the tested prefixes
    * 
    * @return the length of the matching prefix, 0 if not prefixed */

   public static int isPrefixedWith(String candidate, String[] prefixes) {
      for ( int i = 0; i < prefixes.length; i++ ) {
         if ( prefixes[i].equals(candidate) ) 
            return 0;
         if ( candidate.startsWith(prefixes[i]) ) 
            return prefixes[i].length();
      }
      return 0;
   }

   /**
    * Tells if the the method name is equal to one of the given
    * prefixes.
    *
    * @return true if equals */

   public static boolean isInPrefixes(String candidate, String[] prefixes) {
      for ( int i = 0; i < prefixes.length; i++ ) {
         if ( prefixes[i].equals(candidate) ) return true;
      }
      return false;
   }   

   /**
    * Return true if the name matches a setter profile (i.e. set...).
    * 
    * @param name the string to test
    * @return true if setter profile
    */

   public static boolean isSetter(String name) {
      return isPrefixedWith(name, setterPrefixes) != 0 ;
   }  

   /**
    * Return true if the name matches a getter profile (i.e. get...).
    * 
    * @param name the string to test
    * @return true if getter profile */

   public static boolean isGetter(String name) {
      return isPrefixedWith(name, getterPrefixes) != 0 ;
   }  

   /**
    * Return true if the name matches a adder profile (i.e. add...).
    * 
    * @param name the string to test
    * @return true if adder profile */

   public static boolean isAdder(String name) {
      return isPrefixedWith(name, adderPrefixes) != 0 ;
   }

   /**
    * Return true if the name matches a remover profile (i.e. rmv...,
    * del..., remove...).
    * 
    * @param name the string to test
    * @return true if remover profile */

   public static boolean isRemover(String name) {
      return isPrefixedWith( name, removerPrefixes ) != 0 ;
   }

   /**
    * Returns true if the name matches a modifier profile
    * (i.e. setter, adder, or remover profile).
    * 
    * @param name the string to test
    * @return true if modifier profile */

   public static boolean isModifier(String name) {
      return isSetter(name) || isAdder(name) || isRemover(name) ||
         isInPrefixes(name, removerPrefixes) || isInPrefixes(name, adderPrefixes);
   }

   /**
    * Takes a string and returns a new capitalized one.
    *
    * @param str the original string
    * @return a new capitalized version of <code>str</code> */
   
   public static String capitalize(String str) {
      if ( str.length() == 0 ) 
         return str;
      StringBuffer sb = new StringBuffer(str);
      sb.setCharAt(0, Character.toUpperCase(str.charAt(0)) );
      return sb.toString();
   }

   /**
    * Returns the normalized name for a string.
    * 
    * <p>A normalized string is a blank-free word where each relevant
    * substring starts with an upcase character.<br>
    *
    * <p>For instance:
    * 
    * <ul><pre>
    * - one string --> OneString
    * - one_string --> OneString
    * - oneString  --> OneString
    * - one.s-tring --> OneSTring
    * </pre></ul>
    *
    * @param string the string to normalize
    * @return the normalized string */

   public static String getNormalizedString(String string) {
      if ( string.length() == 0 ) return string;
      StringBuffer sb = new StringBuffer( string.length() );
      sb.append ( Character.toUpperCase( string.charAt( 0 ) ) );
      boolean wordSep = false;
      for ( int i = 1; i < string.length(); i++ ) {
         char c = string.charAt( i );
         if ( c == '_' || c == '.' || c == ' ' || c == '-' ) {
            wordSep = true;
         } else {
            if ( wordSep ) {
               wordSep = false;
               sb.append ( Character.toUpperCase( c ) );
            } else {
               sb.append ( c );
            }
         }
      }
      return new String( sb );
   }

   /**
    * Returns the underscored name for a normalized string.
    *
    * <p>A normalized string is a blank-free word where each relevant
    * substring starts with an upcase character.<br>
    *
    * <p>For instance:
    * 
    * <ul><pre>
    * - OneString --> one_string
    * </pre></ul>
    *
    * @param string a normalized string
    * @return the underscored string
    */

   public static String getUnderscoredString(String string) {
      if (string.length() == 0) 
         return string;
      StringBuffer sb = new StringBuffer(string.length());
      sb.append(Character.toLowerCase(string.charAt(0)));
      for (int i=1; i<string.length(); i++) {
         char c = string.charAt(i);
         if (Character.isUpperCase(c)) {
            sb.append('_');
            sb.append(Character.toLowerCase(c));
         } else {
            sb.append(c);
         }
      }
      return new String(sb);
   }

   /**
    * Lower case the first character of a string.
    *
    * @param string the string to transform
    * @return the same string but with the first character lowered 
    */
   public static String lowerFirst(String string) {
      if (string.equals("")) 
         return string;
      char[] chs = string.toCharArray();
      chs[0] = Character.toLowerCase(chs[0]);
      return String.copyValueOf(chs);      
   }

   /**
    * Lower case the first character of a string unless it starts with
    * at least two upper case letters.
    *
    * @param string the string to transform
    * @return the same string but with the first character lowered */
   public static String maybeLowerFirst(String string) {
      if (!(string.length()>1 && 
            Character.isUpperCase(string.charAt(0)) && 
            Character.isUpperCase(string.charAt(1))))
         return lowerFirst(string);
      else 
         return string;
   }

   /**
    * Get an unprefixed string from a prefixed string.<br>
    *
    * <p><b>NOTE:</b> this method is not semantically indentical to
    * <code>removePrefixFrom</code>.
    *
    * <p>For instance:
    * 
    * <ul><pre>
    * - getName --> Name
    * - addName --> Names
    * - removeName --> Names
    * </pre></ul>
    *
    * @param string a prefixed string
    * @return the corresponding unprefixed string
    * @see #removePrefixFrom(String) */

   public static String getUnprefixedString(String string) {
      String ret = string;
      int ps = 0; /* prefix size */
      
      if( (ps = isPrefixedWith( string, removerPrefixes )) != 0 ) {
         ret = getPlural(string.substring(ps));
      } else if( (ps = isPrefixedWith( string, adderPrefixes )) != 0 ) {
         ret = getPlural(string.substring(ps));
      } else if( (ps = isPrefixedWith( string, setterPrefixes )) != 0 ) {
         ret = string.substring(ps);
      } else if( (ps = isPrefixedWith( string, getterPrefixes )) != 0 ) {
         ret = string.substring(ps);
      }

      return ret;
   }

   /**
    * Returns the plural of a name
    *
    * <p></p>
    *
    * @param name the name
    * @return the plural of name
    */
   public static String getPlural(String name) {
      if (!name.endsWith("s")) {
         if (name.endsWith("y")) {
            return name.substring( 0, name.length()-1 )+"ie";
         } else {
            return name + "s";
         }
      } else {
         return name + "es";
      }
   }

   /**
    * Return the singular of a name. Handles english -ies plurals,
    * and -s. If no plural is recognized, returns name.
    */
   public static String getSingular(String name) {
      if (name.endsWith("ies")) {
         return name.substring(0, name.length()-3)+"y";
      } else if (name.endsWith("s")) {
         return name.substring(0, name.length()-1);
      } else {
         return name;
      }
   }

   /**
    * Removes the prefix from a prefixed string.
    *
    * <p><b>NOTE:</b> this method is not semantically indentical to
    * <code>getUnprefixedString</code>.
    *
    * <p>For instance:
    * 
    * <ul><pre>
    * - getName --> Name
    * - addName --> Name
    * - removeName --> Name
    * </pre></ul>
    *
    * @param string a prefixed string
    * @return the corresponding unprefixed string
    * @see #getUnprefixedString(String) */

   public static String removePrefixFrom(String string) {
      String ret = string;
      int ps = 0; /* prefix size */
      
      if( (ps = isPrefixedWith( string, removerPrefixes )) != 0 ) {
         ret = string.substring( ps );
      } else if ( (ps = isPrefixedWith( string, adderPrefixes )) != 0 ) {
         ret = string.substring( ps );
      } else if ( (ps = isPrefixedWith( string, setterPrefixes )) != 0 ) {
         ret = string.substring( ps );
      } else if ( (ps = isPrefixedWith( string, getterPrefixes )) != 0 ) {
         ret = string.substring( ps );
      }

      return ret;
   }

   /**
    * Returns the field name for a given method (modifier or getter).
    *
    * @param cl the class where the method is supposed to be
    * @param method the name of the method
    * @return the field for this method if exist */

   public static String fieldForMethod(Class cl, String method) {
      if ( (!isGetter(method)) && (!isModifier(method)) ) 
         return null;
      String fieldName1 = getUnderscoredString(
         getUnprefixedString( method ) );
      Hashtable fields = ClassRepository.getDirectFieldAccess(cl);
      if (fields.containsKey(fieldName1)) 
         return fieldName1;
      String fieldName2 = lowerFirst(getUnprefixedString(method));
      if (fields.containsKey(fieldName2))
         return fieldName2;
      return null;
   }

   /**
    * Returns the printable textual representation of a field or
    * method name.
    *
    * @param name the field or method name
    * @return a "natural-language-like" textual representation */

   public static String textForName(String name) {
      if (name.length()==0) 
	 return name;
      StringBuffer sb = new StringBuffer(name.length());
      sb.append(Character.toUpperCase(name.charAt(0)) );
      for (int i = 1; i<name.length(); i++) {
         char c = name.charAt(i);
         if ( Character.isUpperCase(c) && 
              ( (i>0) && Character.isLowerCase(name.charAt(i-1)) ) ) {
            sb.append ( ' ' );
	    if ( (i<name.length()-1) && 
                 Character.isUpperCase(name.charAt(i+1)) ) {
	       sb.append(c);
	    } else {
	       sb.append(Character.toLowerCase(c));
	    }
         } else {
            if ( c == '_' || c == '.' || c == '-' ) {
               sb.append(' ');
            } else {
               sb.append(c);
            }
         }
      }
      return new String(sb);
   }

   /**
    * Returns the normalized name of an aspect regarding its class
    * name.<p>
    *
    * E.g.:
    *
    * <ul>
    * <li>AgendaPersistenceAC -> persistence
    *
    * @param name the aspect name to normalize
    * @return the normalized name */

   public static String getNormalizedAspectName(String name) {
      if ( name == null ) return null;
      if ( name.length() == 0 ) 
         return name;
      String result = null;
      StringBuffer sb = new StringBuffer( name.length() );
      for ( int i = 1; i < name.length(); i++ ) {
         char c = name.charAt( i );
         if ( Character.isUpperCase( c ) ) { 
            result = name.substring( i );
            break;
         }
      }
      if ( result == null ) return null;
      result = result.substring( 0, result.length() - 2 );
      return result.toLowerCase();
   }

   /**
    * Returns the normalized class name of an aspect regarding its
    * normalized name and the program it belongs to.<p>
    *
    * <ul>
    * <li>org.objectweb.jac.samples.agenda, persistence -> AgendaPersistenceAC
    *
    * @param programName name of the application the aspect belongs to
    * @param aspectName the aspect name to normalize
    * @return the normalized name */

   public static String getNormalizedAspectClassName(String programName, 
                                                     String aspectName) {
      if (aspectName == null || aspectName.length() == 0) 
         return null;
      String result = null;
      String shortProgramName = programName.substring( programName.lastIndexOf( '.' ) );
      StringBuffer sb1 = new StringBuffer( shortProgramName );
      sb1.setCharAt( 1, Character.toUpperCase( sb1.charAt( 1 ) ) );
      StringBuffer sb2 = new StringBuffer( aspectName );
      sb2.setCharAt( 0, Character.toUpperCase( sb2.charAt( 0 ) ) );
      result = programName + new String( sb1 ) + new String( sb2 ) + "AC";
      return result;
   }

}
