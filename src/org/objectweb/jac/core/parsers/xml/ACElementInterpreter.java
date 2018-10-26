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

import java.util.Vector;
import org.apache.log4j.Logger;
import org.w3c.dom.*;

public class ACElementInterpreter implements ElementInterpreter {
    static Logger logger = Logger.getLogger("xml");
    
    public ACElementInterpreter() {        
    }
    
    public Vector interpret(Element element, Class targetClass) throws Exception 
    {
        NodeList childNodes = element.getChildNodes();
        int length = childNodes.getLength();
        Node childNode;
        String tagName;
        Vector methods = new Vector();
        MethodElementInterpreter meInterpreter = new MethodElementInterpreter();
        for (int i=0; i<length; i++) {
            childNode = childNodes.item(i);
            if ( childNode instanceof Element ) {
                Element childElement = (Element)childNode; 
                tagName = childElement.getTagName().intern();
                if (tagName.equals("method")) {
                    methods.add(meInterpreter.interpret( childElement, targetClass ));
                } else {
                    logger.error("Expecting <method> element, found <"+tagName+">");
                }
            }
        }
        return methods;
    }
}
