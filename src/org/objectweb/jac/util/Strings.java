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

package org.objectweb.jac.util;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Various often used string functions
 */
public class Strings
{
    /**
     * Replace slashed characters ("\t" -> '\t',"\r" -> '\t',
     * "\n" -> '\n' ,"\f" -> '\f' , "\_" -> ' ') 
     * @see #slashify(String)
     */
    public static String unslashify (String str) {
        StringBuffer ret = new StringBuffer(str.length());
        int i=0;
        while(i<str.length()) {
            char c = str.charAt(i++);
            if (c == '\\') {
                c = str.charAt(i++);
                switch (c) {
                    case 't': c='\t'; break;
                    case 'r': c='\r'; break;
                    case 'n': c='\n'; break;
                    case 'f': c='\f'; break;
                    case '_': c=' '; break;
                }
            } 
            ret.append(c);
        }
        return ret.toString();
    }

    /**
     * The reverse of unslashify. slashify(unslashify(str)).equals(str).
     * @see #unslashify(String)
     */ 
    public static String slashify(String str) {
        StringBuffer ret = new StringBuffer((int)(str.length()*1.2));
        for(int i=0; i<str.length(); i++) {
            char c = str.charAt(i);
            switch (c) {
                case '\\': ret.append("\\\\"); break;
                case '\t': ret.append("\\t"); break;
                case '\r': ret.append("\\r"); break;
                case '\n': ret.append("\\n"); break;
                case '\f': ret.append("\\f"); break;
                case ' ': ret.append("\\_"); break;
                default: ret.append(c); break;
            }
        }
        return ret.toString();
    }

    /**
     * Split a string into an array
     * @param source string to split
     * @param separator the separator
     * @return an array of strings
     * @see #splitToList(String,String)
     */
    public static String[] split(String source, String separator) {
        return (String[])splitToList(source,separator).toArray(ExtArrays.emptyStringArray);
    }

    /**
     * Split a string into a list of strings
     * @param source string to split
     * @param separator the separator
     * @return a list of strings
     * @see #split(String,String)
     */
    public static List splitToList(String source, String separator) {
        Vector tmp = new Vector();
        int startIndex =0;
        int index = 0;
        while ((index = source.indexOf(separator,startIndex))!=-1) {
            tmp.add(source.substring(startIndex,index));
            startIndex = index+separator.length();
        }
        if (source.length()>0 && startIndex<source.length())
            tmp.add(source.substring(startIndex));
        return tmp;
    }

    /**
     * Builds a string formed by the toString() of items from a
     * collection separated by a separator string.
     * @param items the collection. It must not contain null values
     * @param separator the separator string
     * @see #join(String[],String)
     */
    public static String join(Collection items, String separator) {
        StringBuffer result = new StringBuffer();
        Iterator it = items.iterator();
        while (it.hasNext()) {
            result.append(it.next().toString());
            if (it.hasNext())
                result.append(separator);
        }
        return result.toString();
    }

    /**
     * Builds a string formed by the toString() of items from a
     * collection separated by a separator string.
     * @param items the collection. It may contain null values.
     * @param separator the separator string
     * @see #join(String[],String)
     */
    public static String safeJoin(Collection items, String separator) {
        StringBuffer result = new StringBuffer();
        Iterator it = items.iterator();
        while (it.hasNext()) {
            Object o = it.next();
            result.append(o!=null?o.toString():"#NULL#");
            if (it.hasNext())
                result.append(separator);
        }
        return result.toString();
    }

    /**
     * @see #join(Collection,String)
     */
    public static String join(String[] items, String separator) {
        StringBuffer result = new StringBuffer();
        for (int i=0; i<items.length; i++) {
            if (i>0)
                result.append(separator);
            result.append(items[i]);
        }
        return result.toString();
    }

    /**
     * Split a list of paths separated by path.separator
     *
     * @return an array of path
     */
    public static String[] splitPath(String paths) {
        return Strings.split(paths,System.getProperty("path.separator"));
    }

    /**
     * Create a path string, using the appropriate path separator
     * @param paths a collection of File
     * @return the filenames of paths, separated by the appropriate path separator
     */
    public static String createPathString(Collection paths) {
        String separator = System.getProperty("path.separator");
        String pathString = null;
        Iterator it = paths.iterator();
        while (it.hasNext()) {
            File path = (File)it.next();
            if (pathString==null)
                pathString = path.toString();
            else
                pathString += separator+path.toString();
        }
        return pathString;
    }

    /**
     * Build a String representation of an object of the form
     * &lt;classname&gt;@&lt;hashcode&gt; 
     * @param o the object to stringify
     * @return a String representation of the object
     */
    public static String hex(Object o) {
        return o==null?"null":o.getClass().getName()+"@"+Integer.toHexString(o.hashCode());
    }

    public static String hash(Object o) {
        return o==null?"null":"@"+Integer.toHexString(o.hashCode());
    }
   
    /**
     * Build a String representation of a vector in the way as
     * Vector.toString(), but without brackets.  
     * @param list the vector to stringify
     * @return a String representation of the vector
     */
    public static String toString(Collection list) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("[");
        
        Iterator i = list.iterator();
        while (i.hasNext()) {
            Object item = i.next();
            buffer.append(item==null ? "null" : item.toString());
            if (i.hasNext())
                buffer.append(", ");
        }

        buffer.append("]");
        return buffer.toString();
    }

    public static String toString(Map map) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("{");
        
        Iterator i = map.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry entry = (Map.Entry)i.next();
            buffer.append(String.valueOf(entry.getKey()));
            buffer.append("=");
            buffer.append(String.valueOf(entry.getValue()));
            if (i.hasNext())
                buffer.append(", ");
        }

        buffer.append("}");
        return buffer.toString();
    }

    /**
     * Build a string with a given length and all the characters
     * equals.
     * @param c the character to fill the string with
     * @param length the length of the string
     * @return a string with the required length where
     * string.charAt(i)==c for all i between 0 and lenght-1.
     */
    public static String newString(char c, int length) {
        char[] array = new char[length];
        for (length--;length>=0;length--) {
            array[length] = c;
        }
        return new String(array);
    }

    /**
     * A useful method that replaces all the occurences of a string.
     *
     * @param orgString the original string
     * @param oldString the string to replace (if found) in the
     * original string
     * @param newString the string that replaces all the occurences of
     * old string
     * @return a new string with the occurences replaced 
     */
    public static String replace(String orgString, 
                                 String oldString, String newString) {

        int pos = 0;
        boolean end = false;

        StringBuffer result = new StringBuffer(orgString.length()*2);

        int oldLen = oldString.length();
        while (!end) {
            int newpos = orgString.indexOf(oldString,pos);
            if (newpos != -1) {
                result.append(orgString.substring(pos, newpos));
                result.append(newString);
            } else {
                result.append(orgString.substring(pos));
                end = true;
            }
            pos = newpos + oldLen;
        }
        return result.toString();
    }

    /**
     * Replaces all occurences of some characters by a character
     * @param oldChars the characters that should be replaced
     * @param newChar the character by which to replace
     * @param s the string buffer whose's characters must be replaced
     */
    public static void replace(String oldChars, char newChar, StringBuffer s) {
        for (int i=0; i<s.length(); i++) {
            if (oldChars.indexOf(s.charAt(i))!=-1)
                s.setCharAt(i,newChar);
        }
    }

    /**
     * Replaces all occurences of some characters by a character
     * @param oldChars the characters that should be replaced
     * @param newChar the character by which to replace
     * @param s the string whose's characters must be replaced
     */
    public static String replace(String oldChars, char newChar, String s) {
        StringBuffer result = new StringBuffer(s);
        replace(oldChars,newChar,result);
        return result.toString();
    }

    /**
     * Delete occurences of characters from a StringBuffer
     * @param delChars the characters to delete
     * @param s the StringBuffer to remlove the characters from
     */
    public static void deleteChars(String delChars, StringBuffer s) {
        int length = s.length();
        int current = 0;
        for (int i=0; i<length; i++) {
            char c = s.charAt(i);
            if (delChars.indexOf(c)==-1) {
                s.setCharAt(current,c);
                current++;
            }
        }
        s.setLength(current);
    }

    public static String getShortClassName(Class cl) {
        String type = cl.getName();
        if (cl.isArray()) {
            type = cl.getComponentType().getName();
        }
        type = type.substring(type.lastIndexOf( '.' )+1);
        if (cl.isArray()) {
            type = type + "[]";
        }
        return type;
    }   

    public static String getShortClassName(String className) {
        return className.substring(className.lastIndexOf( '.' )+1);
    }

    public static String toUSAscii(String s) {
        StringBuffer result = new StringBuffer(s);
        toUSAscii(result);
        return result.toString();
    }

    /**
     * Lowers all characters of a StringBuffer
     */
    public static void toLowerCase(StringBuffer s) {
        for (int i=0; i<s.length(); i++) {
            s.setCharAt(i,Character.toLowerCase(s.charAt(i)));
        }
    }

    /**
     * Uppers all characters of a StringBuffer
     */
    public static void toUpperCase(StringBuffer s) {
        for (int i=0; i<s.length(); i++) {
            s.setCharAt(i,Character.toUpperCase(s.charAt(i)));
        }
    }

    /**
     * Replace accented chars with their non-accented value. For
     * instance, 'é' becomes 'e'.
     * @param s string to convert
     * @return converted string
     */
    public static void toUSAscii(StringBuffer s) {
        for (int i=s.length()-1; i>=0; i--) {
            switch (s.charAt(i)) {
                case 'é':
                case 'è':
                case 'ê':
                case 'ë':
                    s.setCharAt(i, 'e');
                    break;
                case 'É':
                case 'È':
                case 'Ê':
                case 'Ë':
                    s.setCharAt(i, 'E');
                    break;
                case 'ï':
                case 'î':
                case 'ì':
                case 'í':
                    s.setCharAt(i, 'i');
                    break;
                case 'Ï':
                case 'Î':
                case 'Ì':
                case 'Í':
                    s.setCharAt(i, 'I');
                    break;
                case 'à':
                case 'â':
                case 'ä':
                case 'ã':
                case 'å':
                    s.setCharAt(i, 'a');
                    break;
                case 'À':
                case 'Â':
                case 'Ä':
                case 'Ã':
                case 'Å':
                    s.setCharAt(i, 'A');
                    break;
                case 'ù':
                case 'ú':
                case 'ü':
                case 'û':
                    s.setCharAt(i, 'u');
                    break;
                case 'Ù':
                case 'Ú':
                case 'Ü':
                case 'Û':
                    s.setCharAt(i, 'U');
                    break;
                case 'ö':
                case 'ô':
                case 'ó':
                case 'ò':
                case 'õ':
                    s.setCharAt(i, 'o');
                    break;
                case 'Ö':
                case 'Ô':
                case 'Ó':
                case 'Ò':
                case 'Õ':
                    s.setCharAt(i, 'O');
                    break;
                case 'ç':
                    s.setCharAt(i, 'c');
                    break;
                case 'Ç':
                    s.setCharAt(i, 'C');
                    break;
                case 'ÿ':
                case 'ý':
                    s.setCharAt(i, 'y');
                    break;
                case 'Ý':
                    s.setCharAt(i, 'Y');
                    break;
                case 'ñ':
                    s.setCharAt(i, 'n');
                    break;
                case 'Ñ':
                    s.setCharAt(i, 'N');
                    break;
                default:
            }
        }
    }

    /**
     * Compares the USAscii representation of two strings in a case insensitive manner. 
     * @param a first string to compare
     * @param b second string to compare
     * @return true if a and b are equals
     */
    public static boolean equalsUSAsciiNoCase(String a, String b) {
        if (a==null)
            return b==null;
        else if (b==null)
            return a==null;
        else 
            return toUSAscii(a).toLowerCase().equals(toUSAscii(b).toLowerCase());
    }

    /**
     * Tells if a string is empty (is null, has a zero length, or
     * contains only whitespaces)
     * @param str string to test
     * @return true if str is null, str.length()==0 or str.trim().length()==0
     */
    public static boolean isEmpty(String str) {
        return str==null || str.length()==0 || str.trim().length()==0;
    }

    /**
     * Removes all whitespace and CR/LF characters at the beginning or
     * at the end of a string.
     * @param str the string to trim
     */
    public static String trimWSAndCRLF(String str) {
        if (str==null || str.length()==0)
            return str;

        // Trim at the beginning
        int start = 0;
        char c = str.charAt(start);
        while (Character.isWhitespace(c) || c=='\n' || c=='\r') {
            start++;
            if (start>=str.length())
                break;
            c = str.charAt(start);
        }

        // Trim at the end
        int end = str.length()-1;
        c = str.charAt(end);
        while (end>=start && (Character.isWhitespace(c) || c=='\n' || c=='\r')) {
            end--;
            c = str.charAt(end);
        }
        if (end<start)
            return "";
        else
            return str.substring(start,end+1);
    }


    /**
     * Convert a String to a string with onlu iso-8859-1
     * characters. Non iso-8859-1 characters are encoded with \\u<char_value>
     * @param s strng to encode
     * @see #fromISO8859_1(String)
     */
    public static String toISO8599_1(String s) {
        int len = s.length();
        StringBuffer res = new StringBuffer(len*2);

        for(int i=0; i<len; i++) {
            char c = s.charAt(i);
            if (c=='\\') 
                res.append("\\\\");
            else if ((c < 0x0020) || (c > 0x007e)) {
                switch (c) {
                    case 'é': case 'è': case 'ê': case 'ë':
                    case 'É': case 'È': case 'Ê': case 'Ë':
                    case 'ï': case 'î': case 'ì': case 'í':
                    case 'Ï': case 'Î': case 'Ì': case 'Í':
                    case 'à': case 'â': case 'ä': case 'ã': case 'å':
                    case 'À': case 'Â': case 'Ä': case 'Ã': case 'Å':
                    case 'ù': case 'ú': case 'ü': case 'û':
                    case 'Ù': case 'Ú': case 'Ü': case 'Û':
                    case 'ö': case 'ô': case 'ó': case 'ò': case 'õ':
                    case 'Ö': case 'Ô': case 'Ó': case 'Ò': case 'Õ':
                    case 'ç': case 'Ç':
                    case 'ÿ': case 'ý': case 'Ý':
                    case 'ñ': case 'Ñ':
                    case '\n': case '\t': case '\r':
                        res.append(c);
                        break;
                    default:
                        res.append("\\u");
                        res.append(hexDigit[(c >> 12) & 0xF]);
                        res.append(hexDigit[(c >> 8) & 0xF]);
                        res.append(hexDigit[(c >> 4) & 0xF]);
                        res.append(hexDigit[c & 0xF]);
                }
            } else {
                res.append(c);
            }
        }
        return res.toString();
    }

    /**
     * Convert a String to a string with onlu iso-8859-1
     * characters. Non iso-8859-1 characters are encoded with \\u<char_value>
     * @param s string to decode
     * @see #fromISO8859_1(String)
     */
    public static String fromISO8859_1(String s) {
        char c;
        int len = s.length();
        StringBuffer res = new StringBuffer(len);

        int i=0;
        while (i<len) {
            c = s.charAt(i++);
            if (c == '\\') {
                c = s.charAt(i++);
                if (c == 'u') {
                    // Read the xxxx
                    int value=0;
                    for (int j=0; j<4; j++) {
                        c = s.charAt(i++);
                        switch (c) {
                            case '0': case '1': case '2': case '3': case '4':
                            case '5': case '6': case '7': case '8': case '9':
                                value = (value << 4) + c - '0';
                                break;
                            case 'a': case 'b': case 'c':
                            case 'd': case 'e': case 'f':
                                value = (value << 4) + 10 + c - 'a';
                                break;
                            case 'A': case 'B': case 'C':
                            case 'D': case 'E': case 'F':
                                value = (value << 4) + 10 + c - 'A';
                                break;
                            default:
                                throw new IllegalArgumentException(
                                    "Malformed \\uxxxx encoding.");
                        }
                    }
                    res.append((char)value);
                } else {
                    res.append(c);
                }
            } else {
                res.append(c);
            }
        }
        return res.toString();
    }

    private static final char[] hexDigit = {
        '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
    };

}
