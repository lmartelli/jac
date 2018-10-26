/*
  Copyright (C) 2003 Laurent Martelli <laurent@aopsys.com>

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

import java.io.Reader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java_cup.runtime.Symbol;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.parsers.acc.ToolParser;

public class ToolParserWrapper extends ToolParser
{
    static Logger logger = Logger.getLogger("acc.parser");

    public ToolParserWrapper() {
        this.blockKeywords = new HashSet();
    }

    public ToolParserWrapper(Set blockKeywords) {
        this.blockKeywords = blockKeywords;
    }

    Set blockKeywords;
    public void addBlockKeywords(Collection keywords) {
        blockKeywords.addAll(blockKeywords);
    }
    public void setBlockKeywords(Set keywords) {
        blockKeywords.addAll(keywords);
    }

    /**
     * Parses some input from a reader.
     * @param input reader to parse from
     * @param streamName name of the stream to read from
     * @return a List of SyntaxElement
     */
    public NonTerminal parse(Reader input, String streamName) 
    {
        AccScanner lexer = new AccScanner(input,streamName, blockKeywords);
        setScanner(lexer);
        // Parse the input expression
        logger.debug("Parsing "+streamName+" keywords="+blockKeywords+"...");
        Vector methods = null;
        try {
            syntaxElements = (NonTerminal)parse().value;
            //          System.out.println(methods);
            logger.debug(streamName+" parsed");
        } catch (Exception e) {
            //lexer.printState();
            logger.warn("Parser error in "+streamName+" : "+e);
            //         e.printStackTrace();
        }
        return syntaxElements;
    }

    NonTerminal syntaxElements;
    public NonTerminal getSyntaxElements() {
        return syntaxElements;
    }

    public void report_error(String message, Object info) {
        logger.debug(message+" at character "+((Symbol)info).left+
                     "("+((AccScanner)getScanner()).getLine()+")");
    }   

    /**
     * Returns the Terminal at a given position, or null
     * @param position the position of the requested Terminal
     */
    public Terminal getTerminalAt(int position) {
        if (syntaxElements!=null)
            return syntaxElements.getTerminalAt(position);
        else 
            return null;
    }

    /**
     * Gets the "deepest" element at a given position, or null
     * @param position the position of the requested SyntaxElement
     */
    public SyntaxElement getSyntaxElementAt(int position) {
        if (syntaxElements!=null)
            return syntaxElements.getSyntaxElementAt(position);
        else 
            return null;
    }

    /**
     * Gets the "deepest" element at a given position with a given
     * name, or null
     * @param position the position of the requested SyntaxElement 
     * @param name searched name
     */
    public SyntaxElement getSyntaxElementAt(int position, String name) {
        if (syntaxElements!=null) {
            SyntaxElement element = syntaxElements.getSyntaxElementAt(position);
            if (element!=null)
                return element.findParent(name);
            else 
                return null;
        } else 
            return null;
    }
}
