/*
  Copyright (C) 2002 Julien van Malderen

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

package org.objectweb.jac.util.preparse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.Vector;

public class Preparse
{
    public static final String CFGNAME = "preparse.cfg";
    public static final String EXTENSION = ".modified";

    Vector tokenList = new Vector();

    // if in a string
    boolean isInString = false;

    // if in a quote
    boolean isInQuote = false;
   
    // if the next char is escaped
    boolean isEscaped = false;

    // number of opened parentheses
    int parenthLvl = 0;

    // initial level of parenth before function ro remove call
    int endLvl = -1;

    protected void writeToOutput(String str, Writer output)
        throws Exception
    {
        output.write(str);
    }

    /**
     * @param line line of text
     * @param i offset in line of text
     * @param output
     */
    protected int treat(String line, int i, Writer output)
        throws Exception
    {
        int lineLength = line.length();
      
        //System.out.println("treat: `"+line+"' offset="+i+", length="+lineLength);
        
        if (!isInString)
        {
            Iterator j = tokenList.iterator();
            while (j.hasNext())
            {
                Config token = (Config) j.next();
            
                int tokenLength = token.token.length();
                //System.out.println("    token="+token.token+", tokenLength="+tokenLength);
                if ((tokenLength < lineLength - i + 1)
                    && (line.substring(i, i + tokenLength)
                        .equals(token.token)))
                {
                    if (token.hasArguments)
                        endLvl = parenthLvl;
                    writeToOutput("/* a " + token.token + " was here */", output);
                    //System.out.println("    found token "+token.token);
                    return (i + tokenLength - 1);
                }
            }
        }
        /*
          System.out.println("treat: `"+line.charAt(i)+"' isEscaped="+isEscaped+
          "; isInQuote="+isInQuote);
        */
        switch (line.charAt(i))
        {
            case '\"' :
                if ((!isEscaped) && (!isInQuote)) {
                    isInString = !isInString;
                    //System.out.println("Found \" -> "+isInString);
                }
                isEscaped = false;
                break;
            case '\'':
                if ((!isInString) && (!isEscaped)) {
                    isInQuote = !isInQuote;
                    //System.out.println("Found ' -> "+isInQuote);
                }
                isEscaped = false;
                break;
            case '\\':
                if (isInString || isInQuote) {
                    isEscaped = true;
                    //System.out.println("Found \\ -> "+isEscaped);
                }
                break;
            case '(':
                if (!isInString) {
                    parenthLvl++;
                    //System.out.println("Found ( -> "+parenthLvl+"/"+endLvl);
                }
                isEscaped = false;
                break;
            case ')':
                if (!isInString)
                {
                    parenthLvl--;

                    //System.out.println("Found ) -> "+parenthLvl+"/"+endLvl);

                    if ((endLvl != -1) && (parenthLvl == endLvl))
                    {
                        endLvl = -1;
                        return i;
                    }
                }
                isEscaped = false;
                break;
            default:
                isEscaped = false;
                break;
        }

        if (endLvl == -1) {
            output.write(line.charAt(i));
            //System.out.println("print `"+line.charAt(i)+"'");
        } else {
            //System.out.println("skip  `"+line.charAt(i)+"'");
        }
        return i;
    }

    int lineNumber = 0;

    public void parse(ParseInput input, Writer output)
    {
        String line;

        lineNumber = 0;
        try {
            line = input.readLine();
            //System.out.println("read line ("+isInString+","+isInQuote+"=: "+line);
            lineNumber++;
            while (line != null)
            {
                int lineLength = line.length();

                for (int i = 0; i < lineLength ; i++)
                {
                    if ((!isInString) &&
                        ((line.charAt(i) == '/') && (i < lineLength - 1)))
                    {
                        // skipping '//' comments
                        if (line.charAt(i + 1) == '/')
                        {
                            //System.out.println("Found // comment");
                            writeToOutput(line.substring(i), output);
                            break;
                        }
                  
                        // skipping '/* */' comments
                        else if (line.charAt(i + 1) == '*')
                        {
                            //System.out.println("Found /* */ comment");
                            writeToOutput(input.skipTo(i, "*/"), output);
                            break;
                        }
                        else
                            i = treat(line, i, output);
                    }
                    else
                        i = treat(line, i, output);
                }
                if (!input.isModified())
                    output.write('\n');
                line = input.readLine();
                lineNumber++;
                //System.out.println("read line ("+isInString+"): "+line);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    protected String[] getTokens(String str)
        throws Exception
    {
        if (str == null)
            return null;

        if (str.length() == 0)
            return new String[] {};

        Vector result = new Vector();

        int begin = 0;
        int end = 0;
      
        int strLength = str.length();

        while (begin < strLength)
        {
            while ((begin < strLength)
                   && ((str.charAt(begin) == ' ') || (str.charAt(begin) == '\t')))
                begin++;

            end = begin;
            while ((end < strLength)
                   && ((str.charAt(end) != ' ') && (str.charAt(end) != '\t')))
                end++;
            result.add(str.substring(begin, end));
            begin = end + 1;
        }
        return (String[]) result.toArray(new String[] {});
    }

    public void readConfigFile(String filename)
        throws Exception
    {
        try {
            BufferedReader config = 
                new BufferedReader(
                    new FileReader(filename));

            String line = config.readLine();
            while (line != null)
            {
                line = line.trim();
                if ((line.length() > 0) && (line.charAt(0) != '#')) {
                    String[] tokens = getTokens(line);
                    tokenList.add(new Config(tokens[0],!tokens[1].equals("0")));
                }
                line = config.readLine();
            }
            config.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new Exception ("problem with config file '"
                                 + filename
                                 +"' : " + e.getMessage());
        }
    }


    public String modifName(String javaName)
    {
        int i = javaName.length() - 1;
        while ((i >= 0) && (javaName.charAt(i) != '.'))
            i--;
        if (i == 0)
            return javaName + Preparse.EXTENSION;
        return javaName.substring(0, i) + Preparse.EXTENSION;
    }
   
    /**
     * Usage: java org.objectweb.jac.util.preparse.PreParse <config_file> <dest_dir> [<file> ...]
     */
    public static void main(String[] args)
        throws Exception
    {
        Preparse parser = new Preparse();

        if (args.length == 0)
        {
            System.err.println("Error: a configuration file must be specified");
            return;
        }

        parser.readConfigFile(args[0]);
      
        String msg = new String();
        for (int i = 1; i < args.length; i++)
            msg += args[i] + " ";

        File destDir = new File(args[1]);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        for (int i=2; i<args.length; i++)
        {
            try {
                //System.out.println("Preparse: Treating " + args[i] + " ...");
                File inputFile = new File(args[i]);
                File outputFile = new File(destDir,args[i]);
                if (inputFile.lastModified()>outputFile.lastModified()) {
                    ParseInput input = 
                        new ParseInput(
                            new InputStreamReader(
                                new FileInputStream(inputFile),
                                "UTF-8"));
                    System.out.println("Preparse: "+inputFile+" -> "+outputFile);
                    File outputDir = outputFile.getParentFile();
                    if (!outputDir.exists()) {
                        outputDir.mkdirs();
                    }
                    Writer output = 
                        new BufferedWriter(
                            new OutputStreamWriter(
                                new FileOutputStream(outputFile),
                                "UTF-8"));
               
                    parser.parse(input, output);
                    input.close();
                    output.close();
                }
            }
            catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    static class Config {
        public String token;
        public boolean hasArguments;
        public Config(String token, boolean hasArguments) {
            this.token = token;
            this.hasArguments = hasArguments;
        }
    }
}
