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
 * methods that handle the appearence of the fields in objects views.
 *
 * @see GuiAC
 * @see View
 *
 * @author <a href="mailto:renaud@cnam.fr">Renaud Pawlak</a>
 * @author <a href="mailto:laurent@aopsys.com">Laurent Martelli</a> */

public interface FieldAppearenceGuiConf {

    /**
     * Sets an item to be visible or not.
     *
     * <p>If a field of method item is not visible, it will not be
     * displayed by the object views. If a class item is not visible,
     * then it will not be possible to open a view on objects of this
     * class.
     *
     * <p>By default, the RTTI does not define this property and every
     * item is visble. Thus, the GUI aspect component programmer should
     * overload the default contructor of this class to set some
     * visible attributes to false.<p>
     * 
     * @param member the member item (may be a method or a field name);
     * it can be null to set the whole class visible or not
     * @param visible whether the member must be visible
     *
     * @see GuiAC#isVisible(MetaItem) 
     */
    void setVisible(MemberItem member, boolean visible);

    /**
     * Tells the GUI to use embedded editors in the default view for
     * all fields of a class, whenever possible.
     * @param cl the class item
     * @see FieldEditor 
     */
    void setEmbeddedEditors(ClassItem cl);

    /**
     * Tells the GUI wether to use embedded editors in a view for all
     * fields of a class.
     * @param cl the class item
     * @param viewName the view name
     * @param embedded wether to use embedded editors by default
     * @see FieldEditor 
     */
    void setEmbeddedEditors(ClassItem cl, String viewName, boolean embedded);

    /**
     * Tells wether to use embedded editors for the cells of a table
     * in a given view.
     *
     * @param collection the collection whose table view to configure
     * @param viewName the view name for which to configure
     * @param embedded wether to use embedded editors or not in the cells of the table
     *
     * @see #setEmbeddedEditorColumns(CollectionItem,String,MemberItem[]) */
    void setEmbeddedEditors(
        CollectionItem collection,
        String viewName,
        boolean embedded);

    /**
     * Tells to use embedded editors for the cells of some columns in
     * a given view
     *
     * @param collection the collection whose table view to configure
     * @param viewName the view name for which to configure
     * @param members wether to use embedded editors or not in the cells of the table
     * 
     * @see #setEmbeddedEditors(CollectionItem,String,boolean) 
     */
    void setEmbeddedEditorColumns(
        CollectionItem collection,
        String viewName,
        MemberItem[] members);

    /**
     * This configuration method allows the programmer to set a class
     * member to be internally edited for the default view.
     *
     * @param member the member item to be embedded (may be a
     * field or a method)
     * @see FieldEditor 
     * @see #setEmbeddedEditor(MemberItem,String,boolean)
     */
    void setEmbeddedEditor(MemberItem member);

    /**
     * This configuration method allows the programmer to set a class
     * member to be internally edited.
     *
     * <p>By default, each member of a class is editable with an "edit"
     * button or link that opens a popup that allows the user to edit
     * its value (see the <code>FieldEditor</code> interface). If this
     * configuration method is called, then the editor will be embedded
     * into the object's view the member belongs to.
     *
     * @param member the member item to be embedded (may be a
     * field or a method)
     * @param viewName the view for which to use an embedded
     * editor. "default", "autocreate" or one of your own.
     * @param embedded wether to use an editor or not
     *
     * @see FieldEditor 
     * @see #setEmbeddedEditor(MemberItem,String,boolean) 
     */
    void setEmbeddedEditor(MemberItem member, String viewName, boolean embedded);

    /**
     * Sets a border to the field in an object view.
     *
     * @param field the field
     * @param alignment LEFT: title is on the left, RIGHT:
     * title is on the right, CENTER: title is centered 
     * @param style LINE: the border is a line, ETCHED: the border is a
     * 3D line, LOWERED: the border is a 3D effect that makes the
     * bordered element lowered, RAISED: the border is a 3D effect that
     * makes the bordered element raised.  
     * @see #setBorder(FieldItem)
     */
    void setBorder(FieldItem field, String alignment, String style);

    /**
     * Sets a border to the field in an object view. Uses a default
     * style (LINE) and alignement(LEFT)
     *
     * @param field the field
     * @see #setBorder(FieldItem,String,String)
     */
    void setBorder(FieldItem field);

    /**
     * Sets the width of the field's embedded editor when exist.
     *
     * @param field the field
     * @param width the editor width
     * @see #setEmbeddedEditor(MemberItem) 
     * @see #setEditorHeight(FieldItem,Length)
     * @see #setDefaultEditorWidth(ClassItem,Length)
     */
    void setEditorWidth(FieldItem field, Length width);

    /**
     * Sets the default editor width for value of a given type.
     *
     * @param type the type 
     * @param width the editor width
     *
     * @see #setDefaultEditorWidth(ClassItem,Length)
     * @see #setDefaultEditorHeight(VirtualClassItem,Length)
     * @see #setEditorWidth(FieldItem,Length)
     */
    void setDefaultEditorWidth(VirtualClassItem type, Length width);

    /**
     * Sets the default editor width for value of a given type.
     *
     * @param type the type 
     * @param width the editor width
     *
     * @see #setDefaultEditorWidth(VirtualClassItem,Length)
     * @see #setDefaultEditorHeight(VirtualClassItem,Length)
     * @see #setEditorWidth(FieldItem,Length)
     */
    void setDefaultEditorWidth(ClassItem type, Length width);

    /**
     * Set a field to be editable.
     *
     * @param field the field
     * @param editable the flag (true is default) 
     */
    void setEditable(FieldItem field, boolean editable);

    /**
     * Sets the height of a field's editor. It does not affect single
     * line editors (used by primitive types)
     *
     * @param field the field 
     * @param height the editor height 
     *
     * @see #setEmbeddedEditor(MemberItem) 
     * @see #setEditorWidth(FieldItem,Length)
     * @see #setDefaultEditorHeight(ClassItem,Length) 
     */
    void setEditorHeight(FieldItem field, Length height);

    /**
     * Sets the default editor height for value of a given type.
     *
     * @param type the type 
     * @param height the editor height
     *
     * @see #setDefaultEditorHeight(ClassItem,Length)
     * @see #setDefaultEditorWidth(VirtualClassItem,Length)
     * @see #setEditorHeight(FieldItem,Length)
     */
    void setDefaultEditorHeight(VirtualClassItem type, Length height);

    /**
     * Sets the default editor height for value of a given type.
     *
     * @param type the type 
     * @param height the editor height
     *
     * @see #setDefaultEditorHeight(VirtualClassItem,Length)
     * @see #setDefaultEditorWidth(VirtualClassItem,Length)
     * @see #setEditorHeight(FieldItem,Length)
     */
    void setDefaultEditorHeight(ClassItem type, Length height);

    /**
     * Sets the category of an item of a class.
     *
     * <p>The category must correspond to one of these defined on the
     * class with the <code>setCategories</code> configuration method.
     * 
     * @param member the member to categorize (a method or a field)
     * @param category the existing category name
     * @see #setCategories(MemberItem,String[]) 
     * @see ClassAppearenceGuiConf#setCategories(ClassItem,String[]) 
     */
    void setCategory(MemberItem member, String category);

    /**
     * Sets the categories of an item of a class.
     *
     * <p>The category must correspond to one of these defined on the
     * class with the <code>setCategories</code> configuration method.
     * 
     * @param member the member to categorize (a method or a field)
     * @param categories the existing category names
     * @see #setCategory(MemberItem,String)
     * @see ClassAppearenceGuiConf#setCategories(ClassItem,String[]) 
     */
    void setCategories(MemberItem member, String[] categories);

    /**
     * Tells the GUI to insert a referenced object to be displayed as an
     * embedded view in its container object view.
     *
     * @param member the member (reference field or method) that must
     * be embedded 
     *
     * @see #setEmbeddedView(MemberItem,String,boolean)
     */
    void setEmbeddedView(MemberItem member);

    /**
     * Tells the GUI to insert a referenced object to be displayed as an
     * embedded view in its container object view.
     *
     * @param member the member (reference field or method) that must
     * be embedded 
     * @param viewName the view for which the member must be embedded
     * @param embedded wether to embedded the member or not
     *
     * @see #setEmbeddedView(MemberItem)
     */
    void setEmbeddedView(MemberItem member,
                         String viewName,
                         boolean embedded);

    /**
     * Tells the GUI wether to use an embedded view for the adder of a
     * collection.  
     * @param collection the collection
     * @param embedded wether to use an embedded view for the adder
     */
    void setEmbeddedAdder(CollectionItem collection, boolean embedded);

    /**
     * Sets the render of a given field (more precisely a collection)
     * to be rendered by a table.
     *
     * <p>In a table view, each item of the displayed collection fills
     * a table line. Each column represents one field of the objects
     * whithin the collection (the item within the collection whould be
     * of the same class --- or at least share a common superclass).
     *
     * @param field the field that contains the collection 
     */
    void setTableView(FieldItem field);

    /** 
     * Sets the render of a collection to be rendered by a choice and an 
     * embedded view on the selected object.
     * 
     * @param collection the collection 
     * @param external tell if the object is embedded in the current view or if is is opened in an external panel (given by {@link GuiConf#addReferenceToPane(String,MemberItem,String)} 
     */
    void setChoiceView(CollectionItem collection, boolean external);

    /**
     * Tells the GUI wether to show row numbers for tables and lists.
     * @param collection the affected collection
     * @param value wether to show row numbers
     *
     * @see #setDefaultShowRowNumbers(boolean)
     */
    void showRowNumbers(CollectionItem collection, boolean value);

    /**
     * Tells the GUI wether to show row numbers for tables and lists by
     * default.
     *
     * @param value wether to show row numbers
     *
     * @see #showRowNumbers(CollectionItem,boolean) 
     */
    void setDefaultShowRowNumbers(boolean value);

    /**
     * Sets a default sorted column for a collection.
     *
     * <p>By default, collections are not sorted. You can precise a
     * column to use to sort the collection by default. It will be used
     * at the construction of the collection.</p>
     *
     * @param collection the collection
     * @param column the column used to sort (it is a fieldItem, watch
     * out for case). You may preprend a '-' to use the reverse order
     * of that column.
     */
    void setDefaultSortedColumn(CollectionItem collection, String column);

    /**
     * Sets the default order in which the attributes of the elements
     * of a collection are to be rendered.
     *
     * @param collection the collection
     * @param targetClass the class of attributes to render
     * @param memberNames the name of the members in the
     * rendering order
     *
     * @see #setMembersOrder(CollectionItem,String,ClassItem,String[])
     * @see ClassAppearenceGuiConf#setTableMembersOrder(ClassItem,String[])
     * @see ClassAppearenceGuiConf#setAttributesOrder(ClassItem,String[]) */
    void setMembersOrder(
        CollectionItem collection,
        ClassItem targetClass,
        String[] memberNames);

    /**
     * Sets the order in which the attributes of the elements of a
     * collection are to be rendered for a given view.
     *
     * @param collection the collection
     * @param viewName the type for which to set the members order
     * @param targetClass the class of attributes to render
     * @param memberNames the name of the members in the
     * rendering order
     * 
     * @see #setMembersOrder(CollectionItem,ClassItem,String[]) */
    void setMembersOrder(
        CollectionItem collection,
        String viewName,
        ClassItem targetClass,
        String[] memberNames);

    /**
     * This configuration method allows the programmer to make a set of
     * object to be proposed to the final user when an edition of this
     * field value is performed.
     *
     * <p>Most of the GUI will propose the choice within a ComboBox.
     *
     * <p>When the choices values can not be defined at programming
     * time but must be dynamically created, then the programmer can
     * use the <code>setDynamicFieldChoice</code> method.
     *
     * @param field the field
     * @param choice the values the user will have to choose from when
     * a edition of the field is performed
     *
     * @see #setDynamicFieldChoice(FieldItem,Boolean,ClassItem,String) 
     * @see #setFieldChoice(FieldItem,Boolean,String[])
     * @see #setFieldEnum(FieldItem,String)
     * @see GuiConf#defineEnum(String,String[],int,int)
     */
    void setFieldChoice(FieldItem field, Boolean editable, String[] choice);

    /**
     * <p>Declare a field as an enumeration.</p>
     *
     * @param field the field
     * @param enum the name of the enumeration
     *
     * @see GuiConf#defineEnum(String,String[],int,int)
     * @see #setFieldChoice(FieldItem,Boolean,String[])
     * @see #setDynamicFieldChoice(FieldItem,Boolean,ClassItem,String) 
     */
    void setFieldEnum(FieldItem field, String enum);

    /**
     * Same as <code>setFieldChoice</code> but with dynamically
     * defined values.
     *
     * <p>The values are dynamically defined at runtime by the
     * invocation of a target method. This target method must return a
     * collection of objects or an array of objects that contains the
     * possible new values for the fields.</p>
     *
     * <p>If the target method is static, it will be called with the
     * object as the only parameter. If it's not static, it will called
     * <em>on</em> the object with no parameters.</p>
     *
     * @param field the field
     * @param targetClass the class that contains the target method
     * @param targetMethod name of a static method within the target
     * class that returns the values to choose from. It must take an
     * Object as parameter which will be the instance to which the
     * field belongs to.  
     *
     * @see #setDynamicFieldChoice(FieldItem,Boolean,MethodItem) 
     * @see ClassAppearenceGuiConf#setDynamicClassChoice(String,MethodItem)
     */
    void setDynamicFieldChoice(
        FieldItem field,
        Boolean editable,
        ClassItem targetClass,
        String targetMethod);

    /**
     * Same as <code>setFieldChoice</code> but with dynamically
     * defined values.
     *
     * @param field the field
     * @param targetMethod a static method that returns the values to
     * choose from. It must take an Object as parameter which will be
     * the instance to which the field belongs to.
     *
     * @see #setDynamicFieldChoice(FieldItem,Boolean,ClassItem,String) 
     * @see ClassAppearenceGuiConf#setDynamicClassChoice(String,MethodItem)
     */
    void setDynamicFieldChoice(
        FieldItem field,
        Boolean editable,
        MethodItem targetMethod);

    /**
     * Use objects from a collection as the available choices to edit
     * a reference field.
     *
     * @param field the edited reference field
     * @param targetCollection the collection. It belong to same class
     * as the field.  
     */
    void setDynamicFieldChoice(
        FieldItem field,
        CollectionItem targetCollection);

    /**
     * Set the type of the objects of a collection.
     *
     * <p>If this method is not used, the collection type can be
     * dynamically found out by the GUI from the adder's argument
     * types.
     *
     * @param collection the collection within this class 
     * @param type the type of this collection (an exiting class name) 
     */
    void setCollectionType(CollectionItem collection, String type);

    /**
     * Tells the preferred height a table or list view of a collection
     * should take, if possible.
     *
     * @param collection the collection
     * @param height the preferred height 
     *
     * @see #setPreferredWidth(CollectionItem,Length)
     */
    void setPreferredHeight(CollectionItem collection, Length height);

    /**
     * Tells the preferred width a table or list view of a collection
     * should take, if possible.
     *
     * @param collection the collection
     * @param width the preferred width 
     *
     * @see #setPreferredHeight(CollectionItem,Length)
     */
    void setPreferredWidth(CollectionItem collection, Length width);

    /**
     * Sets the number of rows to display simultaneously for a
     * collection. 
     *
     * <p>This is only used by the web GUI so that generated
     * web pages are not too big. If the number of elements in the
     * collection is bigger than numRows, a "previous" and a "next"
     * button are displayed so that the user can see the rest of the
     * collection. The default is 10. Use 0 to display all rows.</p>
     *
     * @param collection the collection
     * @param numRows the number of rows per page 
     *
     * @see #setAvailableNumRowsPerPage(CollectionItem,int[])
     */
    void setNumRowsPerPage(CollectionItem collection, int numRows);

    /**
     * Causes the view of a collection to let the user selects the
     * number of rows to display simultaneously at runtime.
     *
     * @param collection the collection
     * @param numRows the numbers of rows per page the user can choose from.
     *
     * @see #setNumRowsPerPage(CollectionItem,int) 
     */
    void setAvailableNumRowsPerPage(CollectionItem collection, int[] numRows);

    /**
     * Enables the user to filter a table by retaining only rows whose
     * columns have a given value. This feature is only available on
     * the web GUI for the moment.
     * @param collection the collection to configure
     * @param columnNames the field names (from the collection's
     * component type) that can be filtered 
     */
    void showColumnFilters(CollectionItem collection,                                  
                           String[] columnNames);
    /**
     * Sets the view of a given setter's calling box to be a file
     * chooser.<p>
     *
     * As logically expected, the type of the set field must be a
     * String or an URL.<p>
     *
     * @param method the method item. It can be of the form
     * "methodName" or "methodName(<types>)". The first syntax will use
     * the method with that name. The second syntax allow you to
     * specify parameter types (separated by commas, with no spaces).
     * @param fileExtensions allowed file extensions to choose from
     * @param fileDescription
     *
     * @see java.net.URL
     * @see GuiAC#isFileChooserView(MethodItem) 
     */
    void setFileChooserView(
        MethodItem method,
        String[] fileExtensions,
        String fileDescription);

    /**
     * Add some allowed file extensions for File field.
     *
     * @param field the field
     * @param fileExtensions a list of allowed file extensions to
     * choose from (for instance {"html","xhtml"})
     * @param fileDescription a description for those file extensions
     * (for instance "HTML documents")
     */
    /*
    void addAllowedFileExtensions(
        FieldItem field,
        String[] fileExtensions,
        String fileDescription);
    */

    /**
     * This configuration method attaches an icon to a given field so
     * that the iconized instances of this field will be represented by
     * this icon (for instance in a treeview).
     *
     * @param member the member (field or method)
     * @param name the icon's resource name 
     */
    void setIcon(MemberItem member, String name);

    /**
     * This configuration method allows not to use a node to represent
     * a given relation (collection) in a treeview, even if the show
     * relations mode is on .
     *
     * @param field the field 
     */
    void hideTreeRelation(FieldItem field);

    /**
     * Set the default value for a choice.
     *
     * <p>The default value is dynamically calculated by the
     * <code>method</code> parameter from the string value (result can
     * be an object). A default implementation for method is provided
     * by <code>GuiAC</code>.
     *
     * @param field the field
     * @param method a static method that returns the default value
     * (prototype: Object m(FieldItem,String))
     * @param value the string representation of the default value
     * @see GuiAC#getDefaultValue(FieldItem,String) */
    void setDefaultValue(FieldItem field, MethodItem method, String value);

    /**
     * Set the default value for a choice.
     *
     * <p>Same as setDefaultValue(ClassItem, String, MethodItem, String)
     * using default MethodItem <code>GuiAC.getDefaultValue</code>.
     *
     * @param field the field
     * @param value the string representation of the default value
     * @see GuiAC#getDefaultValue(FieldItem,String)
     * @see #setDefaultValue(FieldItem,MethodItem,String) 
     */
    void setDefaultValue(FieldItem field, String value);

    /**
     * Sets the display format of a float or double field.
     *
     * @param field the field
     * @param format the display format of the field
     *
     * @see java.text.DecimalFormat
     */
    void setFloatFormat(FieldItem field, String format);

    /**
     * Sets the description of a class member (field of method).
     *
     * @param member the member
     * @param description the description of the class member
     */
    void setDescription(MemberItem member, String description);

    /**
     * Sets the label of a class member (field of method).
     *
     * @param member the member
     * @param label the label of the class member
     * @see #setLabel(MemberItem,MemberItem,String)
     */
    void setLabel(MemberItem member, String label);

    /**
     * Sets the label of a class member (field of method) for a given
     * context
     *
     * @param member the member
     * @param selector use the label when inside the view of his member item
     * @param label the label of the class member
     * @see #setLabel(MemberItem,String) 
     */
    void setLabel(MemberItem member, MemberItem selector, String label);

    /**
     * Wether to display a label containing the name of the field in views.
     *
     * @param member the member
     * @param value boolean indicating wether to display the label
     */
    void setDisplayLabel(MemberItem member, boolean value);

    /**
     * Set the style of a field
     *
     * @param field the field
     * @param style the CSS style
     *
     * @see ClassAppearenceGuiConf#setStyle(ClassItem,String)
     * @see GuiConf#addStyleSheetURL(String)
     * @see GuiConf#addStyleSheetURL(String,String)
     */
    void setStyle(FieldItem field, String style);

    /**
     * Tells wether to show an add button for a collection
     *
     * @param collection the collection to configure
     * @param addable wether to show a button or not
     *
     * @see #setRemovable(CollectionItem,boolean)
     */
    void setAddable(CollectionItem collection, boolean addable);

    /**
     * Tells wether to show remove buttons for items of a collection
     *
     * @param collection the collection to configure
     * @param removable wether to show buttons or not
     *
     * @see #setAddable(CollectionItem,boolean)
     */
    void setRemovable(CollectionItem collection, boolean removable);

    /**
     * Sets the view type of a field instead of using the default
     * one ("List" or "Table"). It allows you to define a custom view
     * constructor for that type in order to handle complex tables.
     * The view constructor must take 3 arguments: a CollectionItem, an
     * Object (the susbtance), and a CollectionItemView)
     */
    void setViewType(FieldItem field, String viewName, String viewType);

    /**
     * Tells wether items in the default view of a collection have a
     * "view" button to open a view of the item.
     *
     * @param collection the collection
     * @param viewable wether items are viewable
     * 
     * @see #setViewableItems(CollectionItem,String,boolean) 
     */
    void setViewableItems(CollectionItem collection, boolean viewable);

    /**
     * Tells wether items in a given default view of a collection have
     * a "view" button to open a view of the item.
     *
     * @param collection the collection
     * @param viewName the view to configure
     * @param viewable wether items have a view button
     *
     * @see #setViewableItems(CollectionItem,boolean) 
     */
    void setViewableItems(CollectionItem collection, String viewName, boolean viewable);
    
    /**
     * Enables or disables links for references in cells of a table
     */
    void setEnableLinks(CollectionItem collection, String viewName, boolean enable);

    /**
     * Groups cells of table.
     *
     * @param collection the collection whose table cells to group
     * @param viewName the view to configure
     * @param groupBy group adjacent cells with same values of columns
     * whose field start with this field
     *
     * @see #setMultiLineCollection(CollectionItem,String,CollectionItem)
     */
    void groupBy(CollectionItem collection,
                 String viewName,
                 FieldItem groupBy);

    /**
     * Creates subrows in some cells of a table.
     *
     * @param collection the collection whose table cells to subdivide
     * @param viewName the view to configure
     * @param multiLine subdivide cells of columns whose field start
     * with this field
     *
     * @see #groupBy(CollectionItem,String,FieldItem)
     */
    void setMultiLineCollection(CollectionItem collection,
                                String viewName,
                                CollectionItem multiLine);

    /**
     * Use a field as a row to be added at the end of a table
     */
    void setAdditionalRow(CollectionItem collection,String viewName,
                          String row);

    /**
     * Sets the view type to use for cells of a column of table,
     * instead of the default one.
     * 
     * @param collection the collection to configure
     * @param viewName the view to configure
     * @param column the column to configure
     * @param viewType the view type to use for that column
     *
     * @see GuiConf#setViewConstructor(String,String,AbstractMethodItem)
     */
    void setCellViewType(CollectionItem collection, String viewName, 
                         FieldItem column, String viewType);

    /**
     * Defines preferred mnemonics for field of method.
     * @param method 
     * @param mnemonics the mnemonics
     */
    void setMnemonics(MemberItem method, String mnemonics);
}
