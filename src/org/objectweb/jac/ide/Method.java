/*
  Copyright (C) 2002-2003 Renaud Pawlak <renaud@aopsys.com>

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

import org.objectweb.jac.util.Strings;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class Method extends Member {
  
   public String getPrototype() {
      String prototype = 
         ((type==null)?"void":type.getGenerationFullName())+
         " "+getGenerationName()+"("+getParametersString()+")";
      if (!exceptions.isEmpty()) {
         prototype += " throws "+Strings.join(exceptions,",");
      }
      return prototype;
   }

   public String getModifiers() {
      if ((parent instanceof Interface)) {
         return "";
      } else {
         String modifiers = super.getModifiers();
         if (isAbstract)
            modifiers += " abstract";
         if (isSynchronized)
            modifiers += " synchronized";
         return modifiers;
      }
   }

   /**
    * Returns the prototypes (type and name) of the parameters, separated by a comma
    */
   public String getParametersString() {
      String result="";
      if (parameters == null) 
         return result;
      Iterator it = parameters.iterator();
      while (it.hasNext()) {
         Parameter cur = (Parameter)it.next();
         result = result+cur.getPrototype();
         if (it.hasNext()) {
            result = result+", ";
         }
      }
      return result;
   }

   Vector parameters=new Vector();
   
   /**
    * Get the value of parameters.
    * @return value of parameters.
    */
   public List getParameters() {
      return parameters;
   }

   public void addParameter(Parameter p) {
      parameters.add(p);
   }

   public void removeParameter(Parameter p) {
      parameters.remove(p);
   }

   public void clearParameters() {
      parameters.clear();
   }

   /**
    * Returns an array containing the types of the parameters
    */
   public Type[] getParameterTypes() {
      Type[] types = new Type[parameters.size()];
      int i=0;
      Iterator it = parameters.iterator();
      while (it.hasNext()) {
         types[i] = ((Parameter)it.next()).getType();
         i++;
      }
      return types;
   }

   String body;
   
   /**
    * Get the value of body.
    * @return value of body.
    */
   public String getBody() {
      return body;
   }
   
   /**
    * Set the value of body.
    * @param v  Value to assign to body.
    */
   public void setBody(String  v) {
      this.body = v;
   }   

   boolean isAbstract = false;
   public boolean isAbstract() {
      return isAbstract;
   }
   public void setAbstract(boolean value) {
      isAbstract = value;
   }

   boolean isSynchronized = false;
   public boolean isSynchronized() {
      return isSynchronized;
   }
   public void setSynchronized(boolean value) {
      isSynchronized = value;
   }

   /**
    * Returns the name with the type of the parameters between
    * parenthesis.
    */
   public String getUniqueName() {
      StringBuffer result = new StringBuffer();
      result.append(getGenerationName());
      result.append('(');
      Iterator it = parameters.iterator();
      while (it.hasNext()) {
         Parameter param = (Parameter)it.next();
         result.append(param.getTypeName());
         if (it.hasNext())
            result.append(',');
      }
      result.append(')');
      return result.toString();
   }

   /**
    * Returns the names of all parameters, separated by a comma
    */
   public String getParameterNames() {
      StringBuffer text = new StringBuffer();
      Iterator it = getParameters().iterator();
      while (it.hasNext()) {
         Parameter parameter = (Parameter)it.next();
         text.append(parameter.getName());
         if (it.hasNext())
            text.append(",");
      }
      return text.toString();
   }

   Vector exceptions = new Vector();
   public List getExceptions() {
      return exceptions;
   }
   public void addException(String exception) {
      exceptions.add(exception);
   }
   public void removeException(String exception) {
      exceptions.remove(exception);
   }

   /**
    * Clone this method
    * @return a new method with the same name, return type and same parameters
    */
   public Method cloneMethod() {
      Method method = new Method();
      method.setName(getName());
      method.setType(getType());
      method.setArray(isArray());
      Iterator params = getParameters().iterator();
      while (params.hasNext()) {
         Parameter param = (Parameter)params.next();
         Parameter newParam = new Parameter();
         newParam.setName(param.getName());
         newParam.setType(param.getType());
         newParam.setArray(param.isArray());
         method.addParameter(newParam);
      }
      Iterator exceptions = getExceptions().iterator();
      while (exceptions.hasNext()) {
         String exception = (String)exceptions.next();
         method.addException(exception);
      }
      return method;
   }
}
