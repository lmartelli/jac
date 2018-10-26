/*
  Copyright (C) 2002 Zachary Medico

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

package org.objectweb.jac.core.parsers.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.InputStreamParser;
import org.w3c.dom.Document;

/**
 *  Adapter for org.objectweb.jac.core.Parser
 */
public class JacXmlParser implements InputStreamParser {
    static Logger logger = Logger.getLogger("xml");
    
    private DefaultDocumentInterpreter documentInterpreter;
    private XmlParser xmlParser;
    
    /**
     * The DocumentInterpreter always receives a Vector as the Object argument in interpret()
     */
    public JacXmlParser() {
        documentInterpreter = new DefaultDocumentInterpreter();
        documentInterpreter.setElementInterpreter(new ACElementInterpreter());
        xmlParser = new XmlParserJAXP();        
    }
    
    /**
     * Parse a stream.
     */
    public List parse(InputStream input, String filePath, 
                      String targetClassName, Set blockKeywords) 
        throws IOException 
    {
        Vector vector = new Vector();     
        try {
            Document document = xmlParser.parse(input, false); 
            logger.debug("XML parsed "+filePath);
            vector = documentInterpreter.interpret(
                document, 
                Class.forName(targetClassName));
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
        return vector;
    }
}
