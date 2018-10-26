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

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

/**
 * The <code>Log</code> class provides a means to send some
 * informational, debugging or error messages like
 * <code>System.out.println</code> but traces can be enabled or
 * disabled at runtime.
 *
 * <p>JAC supports a -V launching option that allows the user to
 * enable a given trace category (for instance, by using <code>-V
 * jac</code>, the user can see what happens in the JAC core system.
 *
 * @author <a href="mailto:laurent@aopsys.com">Laurent Martelli</a> */

public final class Log{

    // enabled traces
    static Hashtable levels = new Hashtable();

    static PrintStream out = System.out;

    static String logHeader = "";

    static Date date = new Date();
    static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS ");

    /**
     * Sets the logging file.
     *
     * @param name the file name if null=>log to the standard output
     * @param header a string to prepend to each line logged.
     */
    public static void setFileName(String name, String header) {
        try {
            logHeader = header;
            out = new PrintStream(new FileOutputStream(name));
        } catch(Exception e) {
            out = System.out;
            e.printStackTrace();
        }
    }

    /**
     * Traces with a level lower than <code>levels[category]</code> are
     * discarded.
     *
     * @param category a string representing the category to trace
     * @param level the level of the trace (1 is always printed)
     * @param message the message to print out */

    public static void trace(String category, int level, String message) {
        if (!(category!=null && levels.containsKey(category))) {
            levels.put(category, new Integer(0));
        }
        if (level<=((Integer)levels.get(category)).intValue()) {
            date.setTime(System.currentTimeMillis());
            out.print(dateFormat.format(date));
            out.print(logHeader);
            out.print(category);
            out.print(": ");
            out.println(message);
        }
    }

    /**
     * Print the stack trace of an exception with a level lower than
     * <code>levels[category]</code> are discarded.
     *
     * @param category a string representing the category to trace
     * @param level the level of the trace (1 is always printed)
     * @param exception the exception */

    public static void trace(String category, int level, Throwable exception) {
        if (category!=null && levels.containsKey(category)) {
            if (level<=((Integer)levels.get(category)).intValue()) {
                out.println(logHeader+category+": StackTrace");
                exception.printStackTrace(out);
            }
        } else {
            levels.put(category, new Integer(0));
        }
    }


    /**
     * Prints a stack trace.
     *
     * @param category a string representing the category to trace
     * @param level the level of the trace (1 is always printed)
     */
    public static void stack(String category, int level) {
        if (category!=null && levels.containsKey(category)) {
            if (level<=((Integer)levels.get(category)).intValue()) {
                out.println(logHeader+category+": StackTrace");
                new Exception().printStackTrace(out);
            }
        } else {
            levels.put(category, new Integer(0));
        }
    }

    public static void stack(String category) {
        stack(category,1);
    }

    /**
     * Traces with a level equals to 1.
     *
     * @param category a string representing the category to trace
     * @param message the message to print out
     * @see #trace(String,int,String) */

    public static void trace(String category, String message) {
        Log.trace(category, 1, message);
    }

    /**
     * Print a stack trace with a level equals to 1.
     *
     * @param category a string representing the category to trace
     * @param exception the message to print out
     * @see #trace(String,int,String) */

    public static void trace(String category, Throwable exception) {
        Log.trace(category, 1, exception);
    }

    /**
     * Traces an error into the <code>System.err</code> stream.
     *
     * @param message the error message to print out */

    public static void error(String message) {
        out.println(logHeader+"ERROR: "+message);
    }

    /**
     * Traces a warning into the <code>System.err</code> stream.
     *
     * @param message the warning message to print out
     * @param level warning level (0=important, 1=normal, 2=low, ...)
     */
    public static void warning(String message, int level) {
        if (level<=1) {
            out.println(logHeader+"WARNING: "+message);
        }
    }

    /**
     * Traces a warning into the <code>System.err</code> stream.
     *
     * @param message the warning message to print out
     */
    public static void warning(String message) {
        warning(message, 1);
    }

    /**
     * Traces a warning into the <code>System.err</code> stream only if
     * the given category is enable to trace.
     *
     * @param message the warning message to print out
     */
    public static void warning(String category, String message) {
        warning(category+": "+message, 1);
    }

    /**
     * Traces a warning into the <code>System.err</code> stream only if
     * the given category is enable to trace.
     *
     * @param message the warning message to print out
     */
    public static void warning(String category, int level, String message) {
        warning(category+": "+message, level);
    }

    /**
     * Sets the verbose level of a given category.
     *
     * <p>The higher, the more traces are printed out.
     *
     * @param category the category to set the level of
     * @param level the category verbose level */

    public static void setLevel(String category, int level) {
        levels.put(category, new Integer(level));
    }

    /**
     * Returns a Map category -> enabled saying which traces are enables
     */
    public static Map getLevels() {
        return levels;
    }

    public static Set getCategories(Object substance) {
        return levels.keySet();
    }

    public static String dump() {
        return Strings.hex(levels)+" : "+levels;
    }
}
