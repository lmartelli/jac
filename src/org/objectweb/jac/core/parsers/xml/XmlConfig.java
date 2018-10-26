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

import java.io.InputStream;

import org.w3c.dom.*;

import org.objectweb.jac.util.URLInputStream;

public class XmlConfig {
    
   //public static final String methodElementTagName = new String("method").intern();
   public static final String methodElementTagName = "method";
    
   /**
    *  @param fileLocation the xml file
    *  @param targetClass the object will be operated on
    */
   public XmlConfig(InputStream inputStream, 
                    String fileLocation, 
                    Class targetClass)
      throws Exception 
   {
      XmlParserJAXP xmlParser = new XmlParserJAXP();
      Document document = xmlParser.parse( inputStream, false );

      // create an interpreter for the root element
      ACElementInterpreter acElementInterpreter = new ACElementInterpreter();
        
      // create an interpreter for the XML document
      DefaultDocumentInterpreter documentInterpreter = new DefaultDocumentInterpreter();
        
      // add the interpreter for the root element
      documentInterpreter.setElementInterpreter( acElementInterpreter );
        
      // interpret xml and do method invocations        
      documentInterpreter.interpret(document, targetClass);
   }
    
   /**
    *  Do some invocations on System.out
    *
    */
   public static void main(String[] args) throws Exception {
      if ( args.length != 2 )
         System.err.println("usage: java XmlConfig <target_class> <xml_document>");
      else
         new XmlConfig( new URLInputStream(args[0]).getInputStream(), 
                        args[0],
                        Class.forName(args[1]) );
        
      System.exit(0);        
   }    
    
}
