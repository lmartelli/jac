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
 * methods that handle the appearence of the classes in the GUI.
 *
 * @see GuiAC
 * @see View
 *
 * @author <a href="mailto:renaud@cnam.fr">Renaud Pawlak</a>
 * @author <a href="mailto:laurent@aopsys.com">Laurent Martelli</a> */

public interface ClassAppearenceGuiConf {

    /**
     * Defines the string representation of the instances of the class.
     *
     * @param classItem the class
     * @param formatExpression a string that contains field references
     * of the form <code>%field_name%</code>. All the field references
     * are replaced by their value when a string representation of the
     * instance is needed. When you need to print out a <code>%</code>,
     * then you must double it (<code>%%</code>). 
     *
     * @see #setToString(ClassItem,MemberItem,String)
     * @see #setToolTipText(ClassItem,MemberItem,String)
     * @see #setToolTipText(ClassItem,ClassItem,String)
     */
    void setToString(ClassItem classItem, String formatExpression);

    /**
     * Defines a contextual string representation of the instances of
     * the class that will override the default one in some cases,
     * depending on the selector parameter.
     *
     * @param classItem the class
     * @param selector tells when the formatExpression should be
     * applied. 
     * @param formatExpression a string that contains field references
     * of the form <code>%field_name%</code>. All the field references
     * are replaced by their value when a string representation of the
     * instance is needed. When you need to print out a <code>%</code>,
     * then you must double it (<code>%%</code>).  
     *
     * @see #setToString(ClassItem,String)
     * @see #setToString(ClassItem,MemberItem,String)
     * @see #setToolTipText(ClassItem,MemberItem,String)
     * @see #setToolTipText(ClassItem,ClassItem,String)
     */
    void setToString(ClassItem classItem, MemberItem selector,
                     String formatExpression);

    /**
     * Defines a contextual string representation of the instances of
     * the class that will override the default one in some cases,
     * depending on the selector parameter.
     *
     * @param classItem the class
     * @param selector tells when the formatExpression should be
     * applied. 
     * @param formatExpression a string that contains field references
     * of the form <code>%field_name%</code>. All the field references
     * are replaced by their value when a string representation of the
     * instance is needed. When you need to print out a <code>%</code>,
     * then you must double it (<code>%%</code>).  
     *
     * @see #setToString(ClassItem,String)
     * @see #setToString(ClassItem,MemberItem,String)
     * @see #setToolTipText(ClassItem,MemberItem,String)
     * @see #setToolTipText(ClassItem,ClassItem,String)
     */
    void setToString(ClassItem classItem, ClassItem selector,
                     String formatExpression);

    /**
     * This configuration method attaches an icon to a given class so
     * that the iconized instances of this class will be represented by
     * this icon (for instance in a treeview).
     *
     * @param cli the class
     * @param name the icon's resource name 
     *
     * @see #setDynamicIcon(ClassItem,MethodItem)
     */   
    void setIcon(ClassItem cli, String name);

    /**
     * Defines a method to dynamically compute the icon for a class.
     *
     * @param cli a class
     * @param iconMethod a static method which must take an object as
     * parameter and return a String or null. It will be invoked with
     * the object for which an icon must be determined.
     *
     * @see #setIcon(ClassItem,String) 
     */
    void setDynamicIcon(ClassItem cli, MethodItem iconMethod);

    /**
     * Defines a contextual string tooltip for the instances of a class.
     *    
     * @param classItem the class
     * @param formatExpression a string that contains field references
     * of the form <code>%field_name%</code>. All the field references
     * are replaced by their value when a string representation of the
     * instance is needed. When you need to print out a <code>%</code>,
     * then you must double it (<code>%%</code>).  
     *
     * @see #setToolTipText(ClassItem,ClassItem,String)
     * @see #setToolTipText(ClassItem,MemberItem,String)
     */
    void setToolTipText(ClassItem classItem, String formatExpression);

    /**
     * Defines a contextual string tooltip for the instances of the
     * class that will override the default one in some cases,
     * depending on the selector parameter.
     *
     * @param classItem the class
     * @param selector tells when the formatExpression should be
     * applied. 
     * @param formatExpression a string that contains field references
     * of the form <code>%field_name%</code>. All the field references
     * are replaced by their value when a string representation of the
     * instance is needed. When you need to print out a <code>%</code>,
     * then you must double it (<code>%%</code>).  
     *
     * @see #setToolTipText(ClassItem,String) 
     * @see #setToolTipText(ClassItem,ClassItem,String) 
     */
    void setToolTipText(ClassItem classItem, MemberItem selector, 
                        String formatExpression);


    /**
     * Defines a contextual string tooltip for the instances of the
     * class that will override the default one in some cases,
     * depending on the selector parameter.
     *
     * @param classItem the class
     * @param selector tells when the formatExpression should be
     * applied. 
     * @param formatExpression a string that contains field references
     * of the form <code>%field_name%</code>. All the field references
     * are replaced by their value when a string representation of the
     * instance is needed. When you need to print out a <code>%</code>,
     * then you must double it (<code>%%</code>).  
     *
     * @see #setToolTipText(ClassItem,String) 
     * @see #setToolTipText(ClassItem,MemberItem,String) 
     */
    void setToolTipText(ClassItem classItem, ClassItem selector, 
                        String formatExpression);

    /**
     * This configuration method attaches a contextual menu to a given
     * class so that the instances of this class will show the menu
     * when the user performs a right click on it (by default the menu
     * shows all the methods).
     *
     * @param classItem the class
     * @param menu an array containing the names of the methods that
     * form the menu (must be declared in the class item), if an
     * element is an empty string, then a menu item separator is added
     */   
    void setMenu(ClassItem classItem, String[] menu);

    /**
     * This configuration method sets some categories for a class.
     *
     * <p>The GUI will interpret this categories to split the views
     * of this class instances to be separated into several subviews.
     * For instance, the Swing GUI will show the object in
     * several parts placed into several tabs.
     *
     * <p>When this method has been called, each meta-item of the class
     * must be categorized with one of the categories by using the
     * <code>setCategory</code> method.
     *
     * @param cl the class item
     * @param categories the categories for this class
     * @see FieldAppearenceGuiConf#setCategory(MemberItem,String) 
     */
    void setCategories(ClassItem cl, String[] categories);


    /**
     * This configuration method sets categories icons for a class.
     *
     * Each icon is associated with a category, so there must be
     * the right number of icons (one by category)
     *
     * @param cli the class
     * @param icons the icons for the categories
     * @see ClassAppearenceGuiConf#setCategories(ClassItem,String[]) 
     */
    void setCategoriesIcons(ClassItem cli, String[] icons);

    /**
     * This configuration method sets categories labels for a class.
     *
     * Each label is associated with a category, so there must be
     * the right number of labels (one by category)
     *
     * @param cli the class 
     * @param labels the labels for the categories
     * @see ClassAppearenceGuiConf#setCategories(ClassItem,String[]) 
     */
    void setCategoriesLabels(ClassItem cli, String[] labels);

    /**
     * This configuration method allows the programmer to tell that the
     * given class should be viewed with a given customized view when
     * it is opened in a desktop panel.
     *
     * @param classItem the class of the viewed objects
     * @param type the class of the view component 
     */
    void setDesktopCustomizedView(ClassItem classItem, ClassItem type);

    /**
     * Sets the order in which the attributes of a class are to be
     * rendered.
     *
     * <p>Attributes not listed here will not be rendered. This order
     * is also used for tableView rendered collections if the
     * <code>setTableMembersOrder</code> method is not used.
     *
     * @param cl the class 
     * @param attributeNames the name of the attributes in the
     * rendering order
     *
     * @see #setAttributesOrder(ClassItem,String,String[])
     * @see #setTableMembersOrder(ClassItem,String[]) 
     * @see #setMethodsOrder(ClassItem,String[]) 
     * @see FieldAppearenceGuiConf#setMembersOrder(CollectionItem,ClassItem,String[])
     */
    void setAttributesOrder(ClassItem cl, String[] attributeNames);

    /**
     * Sets the order in which the attributes of a class are to be
     * rendered for a given view.
     *
     * <p>Attributes not listed here will not be rendered. This order
     * is also used for tableView rendered collections if the
     * <code>setTableMembersOrder</code> method is not used.
     *
     * @param cl the class 
     * @param viewName the name of the view
     * @param attributeNames the name of the attributes in the
     * rendering order
     *
     * @see #setAttributesOrder(ClassItem,String[])
     * @see #setTableMembersOrder(ClassItem,String[]) 
     * @see FieldAppearenceGuiConf#setMembersOrder(CollectionItem,ClassItem,String[])
     */
    void setAttributesOrder(ClassItem cl, String viewName, String[] attributeNames);

    /**
     * Sets the attributes to be displayed for editable default values in tables
     *
     * @param cl the class
     * @param attributeNames the name of the attributes
     * @see BehaviorGuiConf#setEditableDefaultValues(CollectionItem,boolean)
     */
    void setDefaultsAttributesOrder(ClassItem cl, String[] attributeNames);

    /**
     * Defines which fields should start on a new line. By default,
     * every fields start on a new line.
     *
     * @param cli the class
     * @param fields the names fields which should start on a
     * new line.  
     */
    void setLineBreaks(ClassItem cli, String[] fields);

    /**
     * Sets the order in which the methods of a class are to be
     * rendered for the default view.
     *
     * <p>Methods not listed here will not be rendered.
     *
     * @param cl the class name 
     * @param methodNames the name of the methods in the
     * rendering order 
     * 
     * @see #setMethodsOrder(ClassItem,String,String[])
     */
    void setMethodsOrder(ClassItem cl, String[] methodNames);

    /**
     * Sets the order in which the methods of a class are to be
     * rendered for a view.
     *
     * <p>Methods not listed here will not be rendered.
     *
     * @param cl the class name 
     * @param viewName the view for which to set the methods order
     * @param methodNames the name of the methods in the
     * rendering order 
     * 
     * @see #setMethodsOrder(ClassItem,String[])
     * @see #setAttributesOrder(ClassItem,String,String[])
     */
    void setMethodsOrder(ClassItem cl, String viewName, String[] methodNames);

    /**
     * Sets the order in which the attributes of a class are to be
     * rendered when an instance is created.
     *
     * @param cl the class name 
     * @param attributeNames the name of the attributes in the
     * rendering order
     * @see #setAttributesOrder(ClassItem,String[]) */

    void setCreationAttributesOrder(ClassItem cl, String[] attributeNames);

    /**
     * Sets the order in which the attributes of a class are to be
     * rendered when their instance are placed within a table of the a
     * default view.
     *
     * <p>Attributes not listed here will not be rendered.
     *
     * @param cl the class name
     * @param memberNames the name of the member in the
     * rendering order
     *
     * @see #setTableMembersOrder(ClassItem,String,String[])
     * @see #setAttributesOrder(ClassItem,String[])
     * @see FieldAppearenceGuiConf#setMembersOrder(CollectionItem,ClassItem,String[]) 
     */
    void setTableMembersOrder(ClassItem cl, String[] memberNames);

    /**
     * Sets the order in which the attributes of a class are to be
     * rendered when their instance are placed within a table of a
     * given view.
     *
     * <p>Attributes not listed here will not be rendered.
     *
     * @param cl the class name
     * @param viewName the name of the view
     * @param memberNames the name of the member in the
     * rendering order
     *
     * @see #setTableMembersOrder(ClassItem,String[])
     * @see FieldAppearenceGuiConf#setMembersOrder(CollectionItem,ClassItem,String[]) 
     */
    void setTableMembersOrder(ClassItem cl, String viewName, String[] memberNames);

    /**
     * Sets the order in which the attributes of a class are to be
     * rendered when their instance are placed within a tree.
     *
     * <p>Attributes not listed here will not be rendered.
     *
     * @param cl the class name 
     * @param attributeNames the name of the attributes in the
     * rendering order
     * @see #setAttributesOrder(ClassItem,String[])
     * @see FieldAppearenceGuiConf#setMembersOrder(CollectionItem,ClassItem,String[])
     */
    void setTreeAttributesOrder(ClassItem cl, String[] attributeNames);

    /**
     * Sets a default sorted column for a class.
     *
     * @param cl the class
     * @param column the column used to sort (it is a fieldItem, watch
     * out for case). You may preprend a '-' to use the reverse order
     * of that column.
     */
    void setDefaultSortedColumn(ClassItem cl, String column);

    /**
     * Sets the description of a class.
     * @param cl the class
     * @param description the description of the class
     */
    void setDescription(ClassItem cl, String description);

    /**
     * Sets the label of a class.
     * @param cl the class
     * @param label the label of the class
     */
    void setLabel(ClassItem cl, String label);

    /**
     * Wether to display a label containing the name of the field in views.
     *
     * @param virtualClass the name of the class
     * @param value boolean indicating wether to display the label or not
     */
    void setDisplayLabel(String virtualClass, boolean value);

    /**
     * Sets the display format of a class.
     */
    void setFormat(String className, String format);

    /**
     * Tells the gui to use a primary key field to select instances of
     * a class, instead of a combobox. This is usefull when there are
     * two many instances of the class.
     *
     * @param cl the class whose instance to select
     * @param collection the collection whose indexed field to use 
     * @param repositoryName name of the object holding the collection
     *
     * @see org.objectweb.jac.core.rtti.RttiAC#setIndexedField(CollectionItem,FieldItem) 
     */
    void selectWithIndexedField(ClassItem cl, CollectionItem collection, String repositoryName);

    /**
     * Set the style for a class
     *
     * @param cli the class
     * @param style the CSS style
     *
     * @see FieldAppearenceGuiConf#setStyle(FieldItem,String)
     * @see GuiConf#addStyleSheetURL(String)
     * @see GuiConf#addStyleSheetURL(String,String)
     */
    void setStyle(ClassItem cli, String style);

    void setReadOnly(ClassItem cli, String viewName, boolean readOnly);

    /**
     * Defines a method that returns instances of a class that should
     * be displayed in comboboxes.
     *
     * @param className the class to configure (ClassItem or VirtualClassItem)
     * @param targetMethod the static method that returns instances of
     * the class. It should take a ClassItem as a parameter and return
     * a Collection
     *
     * @see FieldAppearenceGuiConf#setDynamicFieldChoice(FieldItem,Boolean,MethodItem) 
     */
    void setDynamicClassChoice(String className, MethodItem targetMethod);

    /**
     * Defines preferred mnemonics for class.
     * @param cli
     * @param mnemonics the mnemonics
     */
    void setMnemonics(ClassItem cli, String mnemonics);
}
