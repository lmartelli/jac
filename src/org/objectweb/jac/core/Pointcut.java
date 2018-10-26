/*
  Copyright (C) 2001-2002 Renaud Pawlak <renaud@aopsys.com>

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 2 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  USA */

package org.objectweb.jac.core;


import java.io.Serializable;
import java.util.*;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.rtti.*;
import org.objectweb.jac.util.*;

/**
 * This abstract class is the definition of a pointcut in org.objectweb.jac.
 *
 * @author <a mailto:renaud@aopsys.com>Renaud Pawlak</a>
 * @see AspectComponent
 * @see MethodPointcut */

public abstract class Pointcut implements Serializable {
    static Logger logger = Logger.getLogger("pointcut");
    static Logger loggerTags = Logger.getLogger("tags");
    static Logger loggerKeywords = Logger.getLogger("pointcut.keywords");

    /**
     * Applies this pointcut to the given wrappee.
     *
     * @param wrappee the component the current pointcut is applied to 
     */
    public abstract void applyTo(Wrappee wrappee, ClassItem cl);

    /**
     * Parses a keyword expression and returns its actual value as a
     * regular expression regarding the context. 
     */
    protected abstract String parseKeyword(Wrappee wrappee, 
                                           ClassItem cl, 
                                           String keywordExpr, 
                                           List parameters);

    /**
     * Replace elements of parameters "<attribute_name>" with members
     * who have this attribute, and "member_name" with the member
     * having that name.
     * @param parameters Strings to replace
     * @param cli replace with members of this class
     * @return substituted list of MemberItem 
     */
    protected List replaceTags(List parameters,ClassItem cli) {
        if (cli==null || parameters==null)
            return null;
        Vector result = new Vector();
        loggerTags.debug("check "+parameters+" on "+cli.getName());
        Iterator it = parameters.iterator();
        while (it.hasNext()) {
            String param = (String)it.next();
            if (param.startsWith("<") && param.endsWith(">")) {
                Collection taggedMembers;
                if (param.charAt(1)=='!')
                    taggedMembers = cli.getTaggedFields(
                        param.substring(2,param.length()-1),true);
                else
                    taggedMembers = cli.getTaggedFields(
                        param.substring(1,param.length()-1),false);
                result.addAll(taggedMembers);
                /*
                  if (taggedMembers.size()==0) {
                  result.add("#NONE#");
                  } else {
                  result.addAll(taggedMembers);
                  }
                */
            } else if (param.startsWith("{") && param.endsWith("}")) {
                result.addAll(cli.filterFields(param.substring(1,param.length()-1)));
            } else {
                result.add(cli.getMember(param));
            }
        }
        loggerTags.debug("  result="+result);
        return result;
    }

    /**
     * A generic method that parses a pointcut expression and stores
     * the result within a vector.
     *
     * @param descr a humain readable desciption of the pointcut
     * expression type (used to make logs clearer)
     * @param expr a pointcut expression
     * @param result the parsing result 
     * @param inv filled with Boolean, one per element in result
     */
    protected void parseExpr(String descr, Wrappee wrappee, ClassItem cl, 
                             String expr, String[] keywords, 
                             Vector result, Vector inv) {

        result.clear();
        inv.clear();
        expr = Strings.replace(expr, " || ", "\\|");
        int pos = skipSpaces(expr,0);
        boolean end = false;

        if (expr.charAt(0) == '!') {
            pos = skipSpaces(expr,pos+1);
            inv.add(Boolean.FALSE);
        } else {
            inv.add(Boolean.TRUE);
        }
        if (pos==-1)
            throw new RuntimeException("Invalid expression: \""+expr+"\"");
        while (!end) {
            int newpos = expr.indexOf("&&", pos);
            try {
                if (newpos != -1) {
                    result.add(replaceKeywords(
                        wrappee,cl,expr.substring(pos,newpos).trim(),keywords));
                    pos = skipSpaces(expr,newpos + 2);
                    if (pos==-1)
                        throw new RuntimeException("Invalid expression: \""+expr+"\"");
                    if (expr.charAt(pos) == '!') {
                        pos = skipSpaces(expr,pos+1);
                        if (pos==-1)
                            throw new RuntimeException("Invalid expression: \""+expr+"\"");
                        inv.add(Boolean.FALSE);
                    } else {
                        inv.add(Boolean.TRUE);
                    }
                } else {
                    result.add(
                        replaceKeywords(wrappee,cl,expr.substring(pos).trim(),keywords));
                    end = true;
                }
            } catch (Exception e) {
                logger.error("Invalid pointcut definition, "+descr+
                             " construction failed at position "+pos+": "+e);
            }
        }
    }                           

    /**
     * Skips spaces in a string.
     * @param str a string
     * @param pos a position in the string
     * @return the next position in str which >= pos whose character
     * is not a white space, or -1.
     */
    static int skipSpaces(String str, int pos) {
        while (pos<str.length()) {
            if (!Character.isWhitespace(str.charAt(pos)))
                return pos;
            pos++;
        }
        return -1;
    }

    /** Replaces the keywords within an expression. */
    String replaceKeywords(Wrappee wrappee, ClassItem cl,
                           String expr, String[] keywords) {
        String newExpr = expr; 
        for(int i=0; i<keywords.length; i++) {
            newExpr = replaceKeyword(wrappee, cl, newExpr, keywords[i]); 
        }
        return newExpr;
    }

    /** 
     * Parses the parameters of a keyword 
     * @param params a string representing the parameters. Should start
     * with '(' and end with ')'.
     * @return a list of member items
     */
    List parseParameters(String params, ClassItem cli) {
        if (params.equals("") || params.charAt(0) != '(')
            return null;
        /*
          StringTokenizer st = new StringTokenizer( params, "," );
          while (st.hasMoreTokens()) {
          result.add(st.nextToken());
          }
        */
        return replaceTags(      
            Strings.splitToList(params.substring(1, params.indexOf(')',0)),",")
            ,cli);
    }

    int parametersLength(String params) {
        if (params.equals( "" ))
            return 0;
        if (params.charAt(0) != '(')
            return 0;
        return params.indexOf(')',0)+1;
    }

    /** Replace a keyword within an expression. */
    String replaceKeyword(Wrappee wrappee, ClassItem cl, 
                          String expr, String keyword) {

        int pos = 0;
        boolean end = false;
        StringBuffer newExpr = new StringBuffer();
        int keyLen = keyword.length();

        while (!end) {
            int newpos = expr.indexOf(keyword, pos);
            try {
                if (newpos != -1) {
                    loggerKeywords.debug("replacing keyword '"+keyword+
                                         "' in expr :"+expr);
                    newExpr.append(expr.substring(pos, newpos - pos));
                    newExpr.append(
                        parseKeyword( 
                            wrappee, cl, keyword, 
                            parseParameters(
                                expr.substring(newpos+keyword.length()),cl)));
                    pos = newpos + keyLen + 
                        parametersLength(expr.substring(newpos+keyLen));
                } else {
                    newExpr.append(expr.substring(pos));
                    end = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                //Log.error("Invalid keyword '"+keyword+"'");
            }
        }
        return newExpr.toString();
    }

}
