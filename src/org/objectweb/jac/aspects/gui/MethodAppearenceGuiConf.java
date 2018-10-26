/*
  Copyright (C) 2001-2003 Renaud Pawlak <renaud@aopsys.com>, 
                          Laurent Martelli <laurent@aopsys.com>

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 2 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA */

package org.objectweb.jac.aspects.gui;

import org.objectweb.jac.core.rtti.*;

/**
 * This configuration interface of the Gui aspect defines all the
 * methods that handle the appearence of the methods in views.
 *
 * @see GuiAC
 * @see View
 *
 * @author <a href="mailto:renaud@cnam.fr">Renaud Pawlak</a>
 * @author <a href="mailto:laurent@aopsys.com">Laurent Martelli</a> */

public interface MethodAppearenceGuiConf {

    /**
     * Same as <code>setDynamicFieldChoice</code> but for method parameters
     *
     * <p>The values are dynamically defined at runtime by the
     * invocation of a static target method. This target method must
     * return an array of objects that contains the possible new values
     * for the fields.
     *
     * @param method the method
     * @param targetClasses the array of classes that contains the
     * target method for each parameter (elements of this array may be null)
     * @param targetMethods the static methods within the target
     * classes that returns the values to choose from (elements of this
     * array may be null). The prototype is
     * <code>callback(Object)</code> where the given object is the
     * substance.
     * @param editable tells if the values can be edited (new values
     * can be manually entered) 
     *
     * @see #setMethodDynamicParameterChoice(MethodItem,MethodItem[],boolean[])
     */
    void setMethodDynamicParameterChoice(MethodItem method, 
                                         ClassItem[] targetClasses, 
                                         String[] targetMethods,
                                         boolean[] editable); 

    /**
     * Same as <code>setDynamicFieldChoice</code> but for method parameters
     *
     * <p>The values are dynamically defined at runtime by the
     * invocation of a static target method. This target method must
     * return an array of objects that contains the possible new values
     * for the fields.
     *
     * @param method the method
     * @param targetMethods the static methods 
     * that returns the values to choose from (elements of this
     * array may be null). The prototype is
     * <code>callback(Object)</code> where the given object is the
     * substance.
     * @param editable tells if the values can be edited (new values
     * can be manually entered) 
     *
     * @see #setMethodDynamicParameterChoice(MethodItem,ClassItem[],String[],boolean[])
     */
    void setMethodDynamicParameterChoice(MethodItem method, 
                                         MethodItem[] targetMethods, 
                                         boolean[] editable); 

    /**
     * Sets the argument names of a given method item so that they can
     * be used by GUI aspect components.
     *
     * <p>The GUI aspect can automatically fill some default parameter
     * names for all the setters to lighten the GUI aspect programmer
     * work (however, these automatically generated names can be
     * overloaded if needed).
     *
     * @param method the method item. It can be of the form
     * "methodName" or "methodName(<types>)". The first syntax will use
     * the method with that name. The second syntax allow you to
     * specify parameter types (separated by commas, with no spaces).
     * @param parameterNames the parameter names
     */
    void setParameterNames(AbstractMethodItem method,
                           String[] parameterNames);

    /**
     * Declares some parameters of method as enums. 
     * @param method the method
     * @param enumNames the name of enums, for each parameter of the
     * method. Use null to leave a parameter unaffected.
     */
    void setParameterEnums(AbstractMethodItem method, 
                           String[] enumNames) throws Exception;

    /**
     * Declares some reference parameters (non-primitive object-typed
     * parameters) of the method to be linked with a collection that
     * gives the choices of the object in the GUI combobox.
     * @param method the method
     * @param collections the entire names of the collections that
     * should be linked to the corresponding parameters (empty string
     * if unlinked) */
    void setLinkedParameters(AbstractMethodItem method, 
                             String[] collections) throws Exception;

    /**
     * Tells if JAC object-typed (references) arguments of a method can be
     * created while the method's invocation or if they should be
     * choosen in existing instances list.
     *
     * @param method the method item. It can be of the form
     * "methodName" or "methodName(<types>)". The first syntax will use
     * the method with that name. The second syntax allow you to
     * specify parameter types (separated by commas, with no spaces).
     * @param create a flags arrays that tells for each parameters
     * whether it can be created (true) or not (false). It has no
     * effect if the parameter is not a JAC object (a reference) 
     * 
     * @see #setCreationAllowed(FieldItem,boolean)
     */
    void setCreationAllowedParameters(AbstractMethodItem method,
                                      boolean[] create);

    /**
     * Tells wether should be allowed to create a new instance when
     * editing the value of a field.
     *
     * @param field the field
     * @param allow wether to allow or not
     * 
     * @see #setCreationAllowedParameters(AbstractMethodItem,boolean[]) 
     */
    void setCreationAllowed(FieldItem field, boolean allow);

    /**
     * Specify that a parameter of a method corresponds to a field
     * within the invoked object.
     *
     * @param method the method 
     * @param parameterFields an array that contains the corresponding
     * field for each parameter of the method 
     */
    void setParameterFields(AbstractMethodItem method, 
                            String[] parameterFields);
   
    /**
     * Sets the width for all parameters of one method.
     *
     * @param method the method
     * @param width width of all parameters
     *
     * @see #setMethodParametersHeight(AbstractMethodItem,Length[])
     */
    void setMethodParametersWidth(AbstractMethodItem method, Length[] width)
        throws Exception;

    /**
     * Sets the height for all parameters of one method.
     *
     * @param method the method
     * @param height height of all parameters
     *
     * @see #setMethodParametersWidth(AbstractMethodItem,Length[])
     */
    void setMethodParametersHeight(AbstractMethodItem method, Length[] height)
        throws Exception;

    /**
     * Specify that a parameter of a method is a pasword method.
     *
     * <p>A password parameter will be rendered with stars instead of
     * the actual characters.
     *
     * @param method the method 
     * @param parameterFields an array that must contains "true" items
     * if the corresponding parameter is a password, "false" or ""
     * otherwhise. 
     */
    void setPasswordParameters(AbstractMethodItem method, String[] parameterFields);

    /**
     * This configuration method allows the programmer to tell the GUI
     * to add some buttons to the collection that will invoke the
     * corresponding methods on the currently selected collection
     * element.
     *
     * @param collection the collection 
     * @param methods the names of the methods that can be invoked on
     * the selected element 
     */
    void setDirectCollectionMethods(CollectionItem collection, 
                                    String[] methods);

    /**
     * Sets the default value for argument names of a given method item
     * so that they can be used by GUI aspect components.
     *
     * @param method the method item. It can be of the form
     * "methodName" or "methodName(<types>)". The first syntax will use
     * the method with that name. The second syntax allow you to
     * specify parameter types (separated by commas, with no spaces).
     * @param values the default parameter values */

    void setDefaultValues(AbstractMethodItem method, Object[] values);

    /**
     * Sets a condition on a method. If the condition evaluates to
     * false, the method is diabled in the GUI.
     *
     * @param method the method to put a condition on
     * @param condition the condition. It must be the name of method of
     * the same class as method which returns a boolean.
     */
    void setCondition(AbstractMethodItem method, String condition);

    /**
     * If an operation is expected to take a long time to complete,
     * use this method to tell the GUI that it should display a
     * message asking the user to wait for a while.
     *
     * @param method the method
     * @param isSlow wether the method is slow or not
     */
    void setSlowOperation(AbstractMethodItem method, boolean isSlow);

    /**
     * <p>Sets the mime-type of the file written to an OutputStream or
     * Writer.</p>
     *
     * <p>If a method takes an OutputStream or Writer as a parameter,
     * a display (such as the web display or instance) may redirect
     * the stream, to the browser. So that the browser can correctly
     * interpret the data, it may be necessary to specify its
     * mime-type.</p>
     *
     * @param method
     * @param type the mime-type (such as "application/pdf") 
     */
    void setMimeType(AbstractMethodItem method, String type);

    /**
     * Defines a method to dynamically compute the icon for a menu's
     * method.
     *
     * @param method method
     * @param iconMethod a static method which must take 3 parameters
     * (a MethodItem, an Object and an array of objects) and return a
     * String or null. It will be invoked with the parameters array
     * given to <code>addMenuItem</code>
     *
     * @see GuiConf#addMenuItem(String,String,String[],String,AbstractMethodItem,String[]) 
     * @see FieldAppearenceGuiConf#setIcon(MemberItem,String)
     */
    void setDynamicIcon(MethodItem method, MethodItem iconMethod);

    /**
     * Specifies a method to be invoked after a method is invoked from
     * the GUI.
     *
     * @param method 
     * @param hook the method to be invoked after "method" is
     * invoked. It must be static and take an InvokeEvent parameter
     * @see InvokeEvent
     */
    void addPostInvoke(AbstractMethodItem method, AbstractMethodItem hook);
}
