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

import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.MethodItem;

import java.util.List;
import java.util.Vector;
import java.util.Collection;
import java.util.Iterator;

/**
 * this is a class that represent the call of an aspect Method by a ModelElement
 * @author gregoire Waymel
 */
public class ConfigItem {
    /** the Aspect */
    private AspectConfiguration aspectConfiguration;

    /** Method of the aspect */
    private MethodItem method;

    /** Element (1st param of method) which call the method */
    private ModelElement modelElement;

    /** the Params of the method */
    private List param = new Vector();

    /**
     * default constructor
     */
    public ConfigItem() {
    }

    public AspectConfiguration getAspectConfiguration() {
        return aspectConfiguration;
    }

    public ModelElement getModelElement() {
        return modelElement;
    }

    public List getParam() {
        return param;
    }

    public MethodItem getMethod() {
        return method;
    }

    /**
     * @param aspectConfiguration the new Aspect for this ConfigItem
     */
    public void setAspectConfiguration(AspectConfiguration aspectConfiguration) {
        this.aspectConfiguration = aspectConfiguration;
    }
    public void addParam(String param) {
        this.param.add(param);
    }

    public void removeParam(String param) {
        this.param.remove(param);
    }

    public void setModelElement(ModelElement modelElement) {
        this.modelElement = modelElement;
    }

    public void setMethod(MethodItem newMethod) {
        this.method = newMethod;
    }

    public String toString() {
        return "ConfigItem "+modelElement+" "+method+" "+aspectConfiguration;
    }

    /**
     * Gets available aspect configurations 
     */
    public static Collection getAvailableAspects(ConfigItem item) {
        ModelElement element = item.getModelElement();
        Vector configs = new Vector();
        if (element instanceof Class) {
            Iterator i = ((Class)element).getProject().getApplications().iterator();
            while (i.hasNext()) {
                Application app = (Application)i.next();
                configs.addAll(app.getAspectConfigurations());
            }
        }
        return configs;
    }

    /**
     * search the aspect method that can be call by the ModelElement.
     * @param item the ConfigItem that should have a valid ModelElement and a valid AspectConfiguration.
     * @return the method name that can be call by the item ModelElement.
     */
    public final static Collection getValidMethods(ConfigItem item) throws Exception {
        Vector list = new Vector();
        AspectConfiguration aspect = item.getAspectConfiguration();
        //If the model element or the AspectConfiguration is missing, we cannot determine the valid Method.
        ModelElement element = item.getModelElement();
        if ((aspect==null)||(element==null)) {
            return list;
        }

        //Iterate all the config method of the aspect
        Iterator iteMethods = aspect.getConfigurationMethods().iterator();
        while(iteMethods.hasNext()) {
            MethodItem method = (MethodItem)iteMethods.next();
            //Searching the first param
            java.lang.Class param;
            //If the method has no param it's a Project method.
            if (method.getParameterCount()==0) {
                param = Project.class;
            }else{
                param = method.getParameterTypes()[0];
            }

            //test whether the method is a class method.
            if ((param==ClassItem.class)&&(element.getClass()==Class.class)) {
                list.add(method);
                continue;
            }
            //test whether the method is a Field method.
            if ((param==FieldItem.class)&&(element.getClass()==Field.class)) {
                list.add(method);
                continue;
            }
            //test whether the method is a Method method.
            if ((param==MethodItem.class)&&(element.getClass()==Method.class)) {
                list.add(method);
                continue;
            }
            //if it's not this then add the method in the project
            if (element.getClass()==Project.class) {
                list.add(method);
            }
            //Else Error
            //We just continue...
        }
        return list;
    }
}
