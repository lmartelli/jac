/*
  Copyright (C) 2002 Renaud Pawlak <renaud@aopsys.com>

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
package org.objectweb.jac.ide;

import org.apache.log4j.Logger;
import org.objectweb.jac.aspects.persistence.StringConverter;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.core.rtti.MethodItem;

/**
 * Persistance for a MethodItem Class
 */
public class MethodItemConverter implements StringConverter {
    static Logger logger = Logger.getLogger("ide");

    /**
     * Persistance storage : translate the object into String
     * @param obj the object to translate
     * @return a string representing the object ClassItem:MethodItem
     */
    public String objectToString(Object obj){
        String result=null;
        try{
            MethodItem method=(MethodItem)obj;
            ClassItem cl=method.getClassItem();
            result=cl.getName()+":"+method.getFullName();
        }catch(Exception e){
            result=null;
        }
        return result;
    }

    /**
     * Trying to convert a String into a MethodItem: the string must
     * be like ClassItem:MethodItem (fully qualified)
     *
     * @param str the input string  ClassItem:MethodItem
     * @return a MethodItem 
     */
    public Object stringToObject(String str){
        //The position of ':' in the string
        int pos = str.indexOf(":");
        //If not found error
        if (pos==-1) {
            logger.warn("Malformed method string (must be <classname>:<methodname>)"+str);
        }
        ClassItem classItem = ClassRepository.get().getClass(str.substring(0,pos));
        //if classItem doesn't exist
        if (classItem==null) {
            logger.warn("The class "+str.substring(0,pos)+" couldn't be found in the Repository");
            return null;
        }
        return classItem.getMethod(str.substring(pos+1));
    }
}
