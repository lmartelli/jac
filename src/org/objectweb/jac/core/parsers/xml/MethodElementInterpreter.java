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


import org.apache.log4j.Logger;
import org.objectweb.jac.core.ConfigMethod;
import org.w3c.dom.*;

public class MethodElementInterpreter {    
    static Logger logger = Logger.getLogger("xml");

    public MethodElementInterpreter() {        
    }    
    
    public ConfigMethod interpret(Element methodElement, Class targetClass) 
        throws Exception 
    {
        logger.debug("interpreting method "+methodElement.getAttribute("name"));
        NodeList argElements = methodElement.getElementsByTagName("arg");
        int argCount = argElements.getLength();
        //      Class[] types = new Class[argCount];
        Object[] values = new Object[argCount];
            
        for ( int argIndex=0; argIndex<argCount; argIndex++ ) {
            Element argElement = (Element)argElements.item( argIndex );
            values[argIndex] = interpretArgument(argElement);
        }         
        
        return new ConfigMethod(methodElement.getAttribute("name"), values);
    }  

    protected Object interpretArgument(Element argElement) {
        if (argElement.getAttribute("type").equals("java.lang.reflect.Array")) {
            return interpretArray(argElement);
        } else {
            return argElement.getAttribute("value");
        }
    }

    /* handle Array arguments */
    protected Object[] interpretArray(Element argElement) 
    {
        NodeList itemElements = argElement.getElementsByTagName("item");
        int itemCount = itemElements.getLength();
        Object[] array = new Object[itemCount];
        for (int i=0; i<itemCount; i++) {
            Element itemElement = (Element)itemElements.item(i);
            array[i] = interpretArgument(itemElement);
        }
        return array;
    }
}
