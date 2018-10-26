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

  You should have received a copy of the GNU Lesser General Public
  License along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  USA */

package org.objectweb.jac.core.parsers.acc;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java_cup.runtime.Scanner;
import java_cup.runtime.Symbol;
import org.apache.log4j.Logger;
import org.objectweb.jac.util.PushbackReader;

public class AccScanner implements Scanner {
    static Logger logger = Logger.getLogger("acc.scanner");
    static Logger loggerParser = Logger.getLogger("acc.parser");

    PushbackReader input;
    String streamName;
    int line;
    boolean bol; // beginning of line
    boolean previousBol; // beginning of line

    AccScanner include = null;
    Set blockKeywords;

    public AccScanner(Reader input, String streamName, Set blockKeywords) {
        logger.debug("FileInputStream("+streamName+")");
        this.input = new PushbackReader(input,2);
        this.streamName = streamName;
        this.line = 1;
        this.bol = true;
        this.blockKeywords = blockKeywords;
        //System.out.println(streamName+":INIT");
    }

    public AccScanner(String streamName, Set blockKeywords) 
        throws IOException
    {
        InputStream inputStream = null;
        try {
            logger.debug("FileInputStream("+streamName+")");
            inputStream = new FileInputStream(streamName);
        } catch ( FileNotFoundException e ) {
            try {
                logger.debug("getResourceAsStream("+streamName+")");
                inputStream = getClass().getClassLoader().getResourceAsStream(streamName);
            } catch (Exception e2 ) {
                logger.debug("new URL("+streamName+")");
                try {
                    URL url = new URL(streamName);
                    inputStream = url.openStream();
                } catch (MalformedURLException e3) {
                    e3.printStackTrace();
                }
            }
        }

        if (inputStream==null)
            throw new FileNotFoundException("file not found : "+streamName);

        this.input = new PushbackReader(new InputStreamReader(inputStream),2);
        this.streamName = streamName;
        this.line = 1;
        this.bol = true;
        this.blockKeywords = blockKeywords;

        //      System.out.println(streamName+":INIT");
    }

    public void printState() {
        logger.error(getLine());
        if (include!=null)
            include.printState();
    }

    protected boolean isEof(int c) {
        return c==-1 || c==65535;
    }

    public Symbol next_token() throws java.lang.Exception
    {
        if (include!=null) {
            Symbol token = include.next_token();
            if (token.sym != -1) {
                return token;
            }
            include = null;
        }

        boolean quoted = false;
        int c = input.read();
        while (true) {
            // skip white spaces and CR
            while (c==' ' || c=='\t' || c=='\n' || c=='\r') {
                //System.out.println(streamName+":"+line+":WHITESPACE");
                if (c=='\n' || c=='\r') {
                    //System.out.println(streamName+":"+line+":NEWLINE1");
                    line++;
                    bol = true;
                    if (c=='\r') {
                        c = input.read();
                        if (c != '\n')
                            input.unread(c);
                    }
                }
                c = input.read();
            }
            // skip comments
            if (c=='/') {
                int ahead = input.read();
                if (ahead=='/') {
                    // c++-style comments
                    //System.out.println(streamName+":"+line+":C++ COMMENT");
                    c = input.read();
                    while (c!='\n' && c!='\r' && c!=-1) {
                        c = input.read();
                    }
                    continue;
                } if (ahead=='*') {
                    // c-style comments
                    //System.out.println(streamName+":"+line+":C COMMENT");
                    int prev = c;
                    c = input.read();
                    boolean state = false;
                    while (!(state && c=='/') && c!=-1) {
                        if ((c=='\n' && prev!='\r') || c=='\r') line++;
                        state = (c=='*');
                        prev = c;
                        c = input.read();
                    }
                    if (c==-1) {
                        logger.error("unclosed comment");
                        printState();
                        break;
                    }
                    c = input.read();
                    continue;
               
                } else {
                    input.unread(ahead);
                    break;
                }
            }
            break;
        }

        int start = input.getPosition()-1;
        Symbol token = null;/*new Symbol(AccSymbols.error);*/

        //System.out.println("Testing char `"+c+"' = `"+(char)c+"'");
        switch (c) {
            case -1: 
            case 65535:
                return null;
                //token = new Symbol(AccSymbols.EOF);
                //System.out.println(streamName+":TOKEN "+token.sym+"["+token.value+"]");
                //break;
            case '{':
                token = new Symbol(AccSymbols.LBRACE,start,start,"{");
                break;
            case '}':
                token = new Symbol(AccSymbols.RBRACE,start,start,"}");
                break;
            case ',':
                token = new Symbol(AccSymbols.COMMA,start,start,",");
                break;
                // when -1 is unread(), it becomes \uFFFF
            case ';':
                token = new Symbol(AccSymbols.EOL,start,start,";");
                break;
            case '/':
                token = new Symbol(AccSymbols.EOL,start,start,"/");
                break;
            case '"':
                // quoted string
                {
                    quoted = true;
                    StringWriter value = new StringWriter();
                    c = input.read();
                    boolean escaped = false;
                    if (c=='\\') {
                        escaped = true;
                        c=readEscaped(input.read());
                    }
                    while ((c!='"' || escaped) && !isEof(c)) {
                        value.write(c);
                        c = input.read();
                        if (c=='\n')
                            line++;
                        escaped = false;
                        if (c=='\\') {
                            c=readEscaped(input.read());
                            escaped = true;
                        }
                    }
                    //System.out.println("value="+value);
                    String str = value.toString();
                    token = new Symbol(AccSymbols.ATOMIC_VALUE,
                                       start,start+str.length()-1,str);
                }
                break;
            default:
                {
                    StringWriter value = new StringWriter();
                    String delim="/{},; \t\n\r";
                    while (delim.indexOf(c)==-1 && c!=-1) {
                        value.write(c);
                        c = input.read();
                    }
                    input.unread(c);
                    //System.out.println("value="+value);
                    String str = value.toString();
                    token = new Symbol(AccSymbols.ATOMIC_VALUE,
                                       start,start+str.length()-1,str);
                }
        }

        previousBol = bol;

        if (bol && token.sym == AccSymbols.ATOMIC_VALUE && token.value.equals("include")) {
            bol = false;
            String filename = (String)next_token().value;
            //System.out.println(streamName+":INCLUDE("+filename+")");
            include = new AccScanner(filename,blockKeywords);
            return next_token();
        }
        if (!quoted && token.sym == AccSymbols.ATOMIC_VALUE && 
            ( token.value.equals("class") || 
              token.value.equals("member") || 
              token.value.equals("method") || 
              token.value.equals("attribute") || 
              token.value.equals("block") ||
              blockKeywords.contains(token.value) ) )
        {
            token.sym = AccSymbols.CLASS;
            //System.out.println(streamName+":TOKEN block");
        }
        if (!quoted && token.sym == AccSymbols.ATOMIC_VALUE && 
            token.value.equals("import")) {
            token.sym = AccSymbols.IMPORT;
        }
        if (!quoted && token.sym == AccSymbols.ATOMIC_VALUE && 
            token.value.equals("null")) {
            token.value = null;
        }
        token.left = start;
        if (token.value instanceof String)
            token.right = start + ((String)token.value).length() - 1;
        if (quoted)
            token.right += 2;

        //System.out.println(streamName+":TOKEN "+token.sym+"["+token.value+"]");
        loggerParser.debug("read token "+token+"  = "+token.value);
        bol = false;
        return token;
    }

    /**
     * Read a char from input, possibly escaped by '\'
     */
    int readEscaped(int c) throws IOException 
    {
        switch (c) {
            case 'n':
                return '\n';
            case 'r':
                return '\r';
            case 't':
                return '\t';
            default:
                return c;
        }
    }

    /**
     * Return the current line number
     */
    public String getLine() {
        if (include!=null)
            return include.getLine();
        else
            return streamName+":"+(previousBol?line-1:line);
    }

}
