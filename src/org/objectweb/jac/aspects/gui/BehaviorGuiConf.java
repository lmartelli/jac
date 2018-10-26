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
 * methods that handle some behavioral configurations of the widgets.
 *
 * @see GuiAC
 * @see View
 *
 * @author <a href="mailto:renaud@cnam.fr">Renaud Pawlak</a>
 * @author <a href="mailto:laurent@aopsys.com">Laurent Martelli</a> */

public interface BehaviorGuiConf {

   /**
    * This configuration method tells the GUI to automatically create
    * a new instance of the field type when a setter or an adder is
    * invoke on the object that owns this field.
    *
    * <p>This features makes the GUI more usable in many cases
    * (otherwhise, a intermediate step is asked to the user for
    * creating or picking an existing instance).
    *
    * @param field the field to be auto-created */  

   void setAutoCreate(FieldItem field);

   /**
    * Defines a method to initialize new objects created by the auto-create behaviour.
    *
    * @param field the field whose autocreate behaviour is considered
    * @param initializer method used to initialize new objects. It
    * must be a method of the field's owning class that takes an
    * instance of the auto created object as parameter.
    *
    * @see #setAutoCreate(FieldItem) */
   void setAutoCreateInitializer(FieldItem field, MethodItem initializer);

   /**
    * This configuration method tells that all the class methods must
    * be in auto-creation mode.
    * @param cl the class
    */
   void setAutoCreateAll(ClassItem cl);

   /**
    * This configuration method tells which parameters of the given
    * method should be autocreated.
    *
    * <p>It does not use the constructor view but the actual modal
    * view on an empty created object that needs to be filled by the
    * user.
    *
    * @param method the method 
    */
   void setAutoCreateParameters(AbstractMethodItem method);

   /**
    * This configuration method tells that all the class methods must
    * be in auto-creation mode except the excluded ones.
    *
    * @param cl the class
    * @param excludedMethods the excluded methods names
    */
   void setAutoCreateParametersEx(ClassItem cl, String[] excludedMethods);

   /**
    * This configuration method tells which fields of the given class
    * should be auto-created.
    *
    * <p>When a wrappee of the given class is auto-created using the
    * <code>setAutoCreateParameters</code> method, all the fields
    * mentioned here are also autocreated afterwards and a modal view
    * is shown to the user so that he can fill the values. The given
    * fields should be references (on other wrappees).
    *
    * @param cl the class
    * @param fields the fields to autocreate
    * @see #setAutoCreateParameters(AbstractMethodItem) */

   void setAutoCreateState(ClassItem cl, String[] fields);

   /**
    * This configuration method allows the programmer to specify that
    * the result of a given method (should be a wrappe) opens a new
    * view on this result instead of treating it as a simple result.
    *
    * @param cl the class
    * @param methodName the method name
    * @see org.objectweb.jac.core.Display#openView(Object) */

   void setOpenViewMethod(ClassItem cl, String methodName);

   /**
    * This configuration method allows the programmer to specify that
    * the view on an object contained in a collection will be
    * automatically opened by the GUI when the user selects it.
    *
    * @param collection the collection
    *
    * @see #setOnSelection(CollectionItem,MethodItem)
    * @see #setSelectionTarget(CollectionItem,ClassItem,String)
    */

   void setViewOnSelection(CollectionItem collection);

   /**
    * Sets the event handler to be called when an item of the
    * collection is selected.
    *
    * @param collection the collection
    * @param eventHandler the event handler method. It must be static
    * method. It will be called with the following parameters : the
    * CustomizedGUI, the CollectionItem and the selected Object. If
    * the handler returns an object, it will be used as the selected
    * object.
    */
   void setOnSelection(CollectionItem collection, MethodItem eventHandler);

   /**
    * Sets the field to be displayed when an item of the collection is
    * selected, instead of the item itself.
    *
    * <p>If the user selects an object o, o.&lt;targetField&gt; will be
    * displayed instead of o.</p>
    *
    * @param collection the collection
    * @param targetClass the class owning the target field
    * @param targetField the field to display
    *
    * @see #setViewOnSelection(CollectionItem) */
   void setSelectionTarget(CollectionItem collection, 
                           ClassItem targetClass, String targetField);

   /**
    * Specify that the object views of instances of a class depend on
    * the value of a field. That is, these views must be refreshed
    * whenever the value of that field changes.
    *
    * @param cl the class
    * @param fieldName the field name
    */
   void addViewFieldDependency(ClassItem cl, String fieldName);

   /**
    * Tells if the default values of an item can be editable from the
    * GUI. */
   void setEditableDefaultValues(CollectionItem collection,boolean editable);
   
   /**
    * Sets the given method to be closing (ie, when the user press the
    * corresponding button, it performs the call and closes the
    * current object view).
    *
    * @param cl the class the contains the method
    * @param methodName the method item name. It can be of the form
    * "methodName" or "methodName(<types>)". The first syntax will use
    * the method with that name. The second syntax allow you to
    * specify parameter types (separated by commas, with no spaces). */

   void setClosingMethod(ClassItem cl, String methodName);

   /**
    * Set the commit method to use when attibutes are edited. 
    *
    * @param value If true, attribute changes are committed when the
    * widget loses focus. Otherwise, a commit and a cancel button are
    * displayed in each object view.  */

   void setCommitOnFocusLost(boolean value);

   /**
    * Tells the swing administration gui to capture System.out so that
    * it appears in a tab.  */

   void captureSystemOut();

   /**
    * Tells the swing administration gui to capture System.err so that
    * it appears in a tab.  */

   void captureSystemErr();

   /**
    * Sets a method to be used to handler user interaction when a
    * method is called, instead of the standard "ask for parameters
    * stuffs".
    * @param method
    * @param handler It should be a static method taking as arguments
    * (Interaction,DisplayContext)
    */
   void setInteractionHandler(MethodItem method, MethodItem handler);

   /**
    * Defines a method to be called when an object referred to by it's
    * indexed field is not found.
    *
    * @param cl the class of which the instance is not found
    * @param handler the method to be called. It must be a static
    * method taking 2 parameters: a ClassItem, which is the class of
    * the object which could not be found, and an Object which is the
    * value of the indexed field. And it should return an Object,
    * which if not null will used as the found object.
    *
    * @see ClassAppearenceGuiConf#selectWithIndexedField(ClassItem,CollectionItem,String) 
    */
   void setIndexNotFoundHandler(ClassItem cl, 
                                MethodItem handler);

}
