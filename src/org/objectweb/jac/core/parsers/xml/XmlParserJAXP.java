/*
  Copyright (C) 2001 Zachary Medico

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

import java.io.*;
import javax.xml.parsers.*;

import org.xml.sax.*;
import org.w3c.dom.*;

import org.objectweb.jac.util.*;

public class XmlParserJAXP implements XmlParser{
    
    private DocumentBuilderFactory documentBuilderFactory;

    /**
     *
     *  @param fileLocation
     *  @param validating
     *
     */
    public Document parse( String fileLocation, boolean validating ) throws Exception {
        
        if ( documentBuilderFactory==null )
            documentBuilderFactory = DocumentBuilderFactory.newInstance();
        
        documentBuilderFactory.setValidating( validating );
        
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        
        if (validating)
            documentBuilder.setErrorHandler(new SaxParserErrorHandler());
        
        return parse( fileLocation, documentBuilder );
        
    }
    
    public Document parse(InputStream input, boolean validating) throws Exception {
        if ( documentBuilderFactory==null )
            documentBuilderFactory = DocumentBuilderFactory.newInstance();
        
        documentBuilderFactory.setValidating( validating );
        
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        
        if (validating)
            documentBuilder.setErrorHandler(new SaxParserErrorHandler());
        
        return parse( input, documentBuilder );
    }
    
    
    private static Document parse( String fileLocation, DocumentBuilder documentBuilder  ) throws Exception {                

        InputStream is = new URLInputStream(fileLocation).getInputStream();
        return documentBuilder.parse(is);        
    }
    
    private static Document parse( InputStream input, DocumentBuilder documentBuilder  ) throws Exception {                              

        return documentBuilder.parse(input);        
    }    
    

    
    private static class SaxParserErrorHandler implements ErrorHandler{
        
        public void error(SAXParseException e) throws SAXException {
            System.err.println("error: "+ e.getMessage());
        }
        
        public void fatalError(SAXParseException e) throws SAXException {
            System.err.println("fatal error: "+ e.getMessage());
        }
        
        public void warning(SAXParseException e) throws SAXException {
            System.err.println("warning: "+ e.getMessage());
        }
        
    }
    
}
