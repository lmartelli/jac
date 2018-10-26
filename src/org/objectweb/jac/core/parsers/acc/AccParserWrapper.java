/*
  Copyright (C) 2001-2002 Laurent Martelli <laurent@aopsys.com>

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

import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java_cup.runtime.Symbol;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.InputStreamParser;

public class AccParserWrapper implements InputStreamParser 
{
    static Logger logger = Logger.getLogger("acc.parser");

    public AccParserWrapper() {
    }

    public List parse(InputStream inputStream, String streamName, 
                      String targetClass, Set blockKeywords) 
    {
        AccScanner lexer = new AccScanner(new InputStreamReader(inputStream), 
                                          streamName, blockKeywords);
        AccParser parser = new AccParser(lexer);
        // Parse the input expression
        logger.debug("Parsing "+streamName+" ...");
        Vector methods = null;
        try {
            methods = (Vector)parser.parse().value;
            //          System.out.println(methods);
        } catch (Exception e) {
            lexer.printState();
            logger.error("Parser error in "+streamName,e);
        }
        logger.debug(streamName+" parsed");
        return methods;
    }
    /*
    public void report_error(String message, Object info) {
        logger.debug(message+" at character "+((Symbol)info).left+
                     "("+((AccScanner)getScanner()).getLine()+")");
    }   
    */
    /**
     * Parse a file. 
     * @param args arg[0] is the path of the file to parse
     */
    public static void main(String[] args) 
    {
        AccScanner scanner = null;
        try {
            scanner = new AccScanner(new FileReader(args[0]),args[0],
                                     new HashSet());
            AccParser myParser = new AccParser(scanner);
            Vector methods = (Vector)myParser.parse().value;
            System.out.println("Methods = "+methods);
        } catch (Exception e) {
            if (scanner!=null)
                System.err.println(args[0]+":"+scanner.getLine());
            e.printStackTrace();
        }
    }
}
