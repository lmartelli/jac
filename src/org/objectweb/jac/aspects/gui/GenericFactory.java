/*
  Copyright (C) 2002-2003 Renaud Pawlak <renaud@aopsys.com>, 
                          Laurent Martelli <laurent@aopsys.com>
  
  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 2 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  USA */

package org.objectweb.jac.aspects.gui;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.Collaboration;
import org.objectweb.jac.core.Naming;
import org.objectweb.jac.core.ObjectRepository;
import org.objectweb.jac.core.Wrappee;
import org.objectweb.jac.core.rtti.AbstractMethodItem;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.core.rtti.CollectionItem;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.MetaItem;
import org.objectweb.jac.core.rtti.MethodItem;
import org.objectweb.jac.core.rtti.MixinMethodItem;
import org.objectweb.jac.core.rtti.RttiAC;
import org.objectweb.jac.core.rtti.VirtualClassItem;
import org.objectweb.jac.util.Classes;
import org.objectweb.jac.util.Enum;
import org.objectweb.jac.util.ExtArrays;
import org.objectweb.jac.util.ExtBoolean;
import org.objectweb.jac.util.NotInCollectionPredicate;
import org.objectweb.jac.util.Predicate;
import org.objectweb.jac.util.Stack;
import org.objectweb.jac.util.Strings;

/**
 * This class implements static methods that generically create GUI
 * items. Depending on the actual view factory, the created items are
 * for SWING, WEB or other GUI. */

public class GenericFactory {
    static Logger logger = Logger.getLogger("gui.generic");
    static Logger loggerAssoc = Logger.getLogger("associations");

    /**
     * Creates a view on an object.
     *
     * @param factory the used factory
     * @param context the display context (passed to ceated sub-item so
     * that they know displays and customized)
     * @param viewName name of the view to build
     * @param substance the object to build a view of */

    public static View createObjectView(
        ViewFactory factory,
        DisplayContext context,
        String viewName,
        Object substance) 
    {
        logger.debug("createObjectView("
                + factory + "," + Strings.hex(substance) + "," + viewName + ")");

        if (substance == null) {
            return factory.createView("null", "Empty", context);
        }

        if (substance instanceof Number
            || substance instanceof Boolean
            || substance instanceof Character
            || substance instanceof String) {
            return factory.createView(
                substance.getClass().getName(),
                "Field",
                new Object[] { substance, null, null },
                context);
        }

        Class substance_type = substance.getClass();
        ClassItem cli = ClassRepository.get().getClass(substance_type);

        CompositeView view =
            (CompositeView) factory.createCompositeView(
                cli.getName(),
                "ObjectView",
                context);

        fillObjectView(view, cli, viewName, substance);
        Collection dependencies = GuiAC.getDependentFields(cli);
        if (dependencies != null) {
            Iterator it = dependencies.iterator();
            while (it.hasNext()) {
                FieldItem field = (FieldItem) it.next();
                Utils.registerField(substance, field, EventHandler.get(), view);
            }
        }
        return view;
    }

    public static void fillObjectView(
        CompositeView view,
        ClassItem cli,
        String viewName,
        Object substance) 
    {
        ObjectView objectView = GuiAC.getView(cli, viewName);
        ViewFactory factory = view.getFactory();
        DisplayContext context = view.getContext();
        String[] categories = (String[]) cli.getAttribute(GuiAC.CATEGORIES);
        String[] icons = GuiAC.getCategoriesIcons(cli);
        String[] labels = GuiAC.getCategoriesLabels(cli);
        if (labels == null)
            labels = categories;

        view.setStyle((String) cli.getAttribute(GuiAC.STYLE));
        view.setDescription(GuiAC.getDescription(substance, "<b>", "</b>"));

        logger.debug("categories = "
                + (categories != null
                    ? Arrays.asList(categories).toString()
                    : "null"));

        if (categories == null) {
            view.addView(
                createObjectView(
                    factory,
                    context,
                    objectView,
                    substance,
                    null));
        } else {
            TabsView tabbedPane =
                (TabsView) factory.createCompositeView(
                    cli.getName(),
                    "Tabbed",
                    context);
            for (int i = 0; i < categories.length; i++) {
                logger.debug("add tab for category " + categories[i]);

                String icon;
                if ((icons != null) && (Array.getLength(icons) > i))
                    icon = icons[i];
                else
                    icon = null;

                CompositeView tab =
                    createObjectView(
                        factory,
                        context,
                        objectView,
                        substance,
                        categories[i]);
                if (!compositeViewIsEmpty(tab))
                    tabbedPane.addTab(tab, labels[i], icon);
                else
                    logger.debug("tab[" + categories[i] + "] is empty");
            }
            view.addView((CompositeView) tabbedPane);
        }
    }

    /**
     * Creates a view of an object (containing no tabs).
     * 
     * @param factory the used factory
     * @param context the display context (passed to ceated sub-item so
     * that they know displays and customized)
     * @param substance the viewed object */

    public static View createObjectViewNoTab(
        ViewFactory factory,
        DisplayContext context,
        Object substance) 
    {
        logger.debug("createObjectViewNoTab("
                + factory
                + ","
                + Strings.hex(substance)
                + ")");

        if (substance == null) {
            return factory.createView(
                substance.getClass().getName(),
                "Empty",
                context);
        } else {
            return createObjectView(
                factory,
                context,
                GuiAC.getView(
                    ClassRepository.get().getClass(substance),
                    "default"),
                substance,
                null);
        }
    }

    /**
     * Returns true is the CompositeView contains a view other than
     * CompositeView.
     */
    public static boolean compositeViewIsEmpty(CompositeView view) {
        Iterator it = view.getViews().iterator();
        while (it.hasNext()) {
            View subView = (View) it.next();
            if (!(subView instanceof CompositeView))
                return false;
            else if (!compositeViewIsEmpty((CompositeView) subView))
                return false;
        }
        return true;
    }

    /**
     * Create a view of an object, including only the attributes of a
     * category.
     *
     * @param factory the ViewFactory
     * @param context the DisplayContext
     * @param substance the object to build the view of
     * @param category the category; if null, all fields are shown
     * @return a CompositeView representing the object
     */
    protected static CompositeView createObjectView(
        ViewFactory factory,
        DisplayContext context,
        ObjectView view,
        Object substance,
        String category) 
    {
        logger.debug("createObjectView("
                + factory + "," + Strings.hex(substance) + ","
                + category+ "," + view.getName() + ")");
        CompositeView resultPane =
            factory.createCompositeView(
                substance.getClass().getName() + "[" + category + "]",
                "Container",
                new Object[] { new Integer(Constants.VERTICAL)},
                context);

        Class substance_type = substance.getClass();
        ClassItem cli = ClassRepository.get().getClass(substance_type);

        CompositeView fieldpane =
            factory.createCompositeView(
                "fields",
                "ParameterContainer",
                new Object[] { Boolean.TRUE },
                context);
        //      EditorContainer editorContainer = (EditorContainer)fieldpane;

        // primitive fields and references

        FieldItem[] fields = null;
        fields = view.getAttributesOrder();
        if (fields == null)
            fields = cli.getFields();

        boolean embedded = view.getName().equals(GuiAC.AUTOCREATE_VIEW);
        logger.debug("attributes_order = " + Arrays.asList(fields));
        FieldItem[] groups = (FieldItem[]) cli.getAttribute(GuiAC.LINE_BREAKS);
        int curgroup = 0;

        CompositeView subFieldPane = null;
        FieldItem oppositeRole =
            (FieldItem) Collaboration.get().getAttribute(GuiAC.OPPOSITE_ROLE);
        loggerAssoc.debug("opposite role = " + oppositeRole);
        boolean first = true;
        for (int i = 0; i < fields.length; i++) {
            if (groups != null
                && curgroup < groups.length
                && groups[curgroup] == fields[i]) {
                if (subFieldPane != null) {
                    fieldpane.addView(subFieldPane);
                }
                subFieldPane =
                    factory.createCompositeView(
                        "subField[" + i + "]",
                        "Container",
                        new Object[] { new Integer(Constants.HORIZONTAL)},
                        context);
                first = true;
                curgroup++;
            }
            FieldItem field = fields[i];
            //if(field==null) continue;
            if (GuiAC.isMemberInCategory(field, category)
                && GuiAC.isVisible(substance, field)
                && !field.isStatic()
                && !field.equals(oppositeRole)) {
                CompositeView pane =
                    subFieldPane != null ? subFieldPane : fieldpane;
                try {
                    if (!first && subFieldPane != null)
                        subFieldPane.addHorizontalStrut(10);
                    pane.addView(
                        getFieldPane(
                            factory,
                            context,
                            substance,
                            view,
                            field,
                            embedded));
                    first = false;
                } catch (Exception e) {
                    logger.error(
                        "Failed to build view for field \""+field+"\"", e);
                }
            }
        }
        if (subFieldPane != null) {
            fieldpane.addView(subFieldPane);
        }

        resultPane.addView(fieldpane);

        logger.debug("getMethodsPane for " + cli);

        Collection meths = new Vector();
        //if (Collaboration.get().getAttribute(GuiAC.AUTO_CREATION) == null) {
        MethodItem[] ms = view.getMethodsOrder();
        if (ms != null) {
            meths = Arrays.asList(ms);
        }
        //}

        logger.debug("methods = " + meths);

        CompositeView methodsPane =
            getMethodsPane(factory, context, substance, meths, category, view);
        if (methodsPane != null) {
            logger.debug("adding methodPane");
            resultPane.addView(methodsPane);
        }

        return resultPane;

    }

    public static View getFieldPane(
        ViewFactory factory,
        DisplayContext context,
        Object substance,
        ObjectView objectView,
        FieldItem field,
        boolean embedded) 
    {
        GuiAC.pushGraphicContext(field);
        try {
            MemberItemView memberView = GuiAC.getView(field, objectView.getName());
            if (field.isReference()) {
                return getReferenceFieldPane(
                    factory,
                    context,
                    substance,
                    field,
                    embedded,
                    objectView,memberView);
            } else if (field.isPrimitive()
                    || factory.hasViewerFor(field.getTypeItem().getName())) {
                return getPrimitiveFieldPane(
                    factory,
                    context,
                    substance,
                    field,
                    objectView,
                    memberView,
                    embedded);
            } else if (field instanceof CollectionItem) {
                return getCollectionPane(
                    factory,
                    context,
                    substance,
                    objectView,
                    (CollectionItemView) memberView,
                    (CollectionItem) field);
            }
        } finally {
            GuiAC.popGraphicContext();
        }
        return null;
    }

    /**
     * Instantiates a viewer for a value of a field and add it to a
     * container.
     */
    protected static boolean getViewer(
        Object substance,
        FieldItem field,
        Object value,
        CompositeView container,
        ViewFactory factory,
        DisplayContext context) 
    {
        Stack types = new Stack();
        types.push(ClassRepository.get().getClass(field.getType()));
        if (value != null) {
            ClassItem actualClass = ClassRepository.get().getClass(value);
            if (types.safeTop()!=actualClass)
                types.push(actualClass);
        }

        Enum enum = GuiAC.getEnum(field);
        if (enum!=null) {
            types.push(ClassRepository.get().getVirtualClass("Enum"));
        }

        MetaItem type = RttiAC.getFieldType(field,substance);
        if (type!=null && types.safeTop()!=type)
            types.push(type);

        logger.debug("types = " + types);
        boolean foundViewer = false;
        while (!types.empty()) {
            type = (MetaItem) types.pop();
            if (factory.hasViewerFor(type.getName())) {
                container.addView(
                    factory.createView(
                        field.getName(),
                        type.getName(),
                        new Object[] { value, substance, field },
                        context));
                foundViewer = true;
                break;
            }
        }
        return foundViewer;
    }

    /**
     * Returns a view of a primitive field. It contains a label and the
     * value of the field.
     *
     * @param factory the view factory
     * @param context the display context
     * @param substance the object the field is part of
     * @param field the field item 
     * @param objectView  the object view that contains the field view
     * @param memberView  the view to build the field for
     * @param embedded use embbeded editors 
     */
    protected static View getPrimitiveFieldPane(
        ViewFactory factory,
        DisplayContext context,
        Object substance,
        FieldItem field,
        ObjectView objectView,
        MemberItemView memberView,
        boolean embedded) 
    {
        logger.debug("primitive field : " + substance + "," + field.getName());

        /*
        Boolean attrValue = (Boolean)field.getAttribute(GuiAC.BORDER);
        boolean border = attrValue!=null && attrValue.booleanValue();
        */

        CompositeView container =
            factory.createCompositeView(
                "field[" + field.getName() + "]",
                "Container",
                new Object[] { new Integer(Constants.HORIZONTAL)},
                context);
        Border border = GuiAC.getBorder(field);
        if (border != null) {
            border.setTitle(GuiAC.getLabel(field));
            container.setViewBorder(border);
        }
        container.setStyle((String) field.getAttribute(GuiAC.STYLE));
        Boolean displayLabel =
            (Boolean) field.getAttribute(GuiAC.DISPLAY_LABEL);
        boolean useEditor =
            GuiAC.isEditable(substance, field)
            && memberView.isEmbeddedEditor(embedded)
            && !objectView.isReadOnly();
        if ((border == null)
            && (displayLabel == null
                || (displayLabel != null && displayLabel.booleanValue()))) {
            container.addView(
                factory.createView(
                    GuiAC.getLabel(field) + ": ",
                    "Label",
                    context));
        }

        FieldEditor editor = null;
        if (useEditor) {
            MethodItem setter = field.getSetter();
            if (setter != null) {
                try {
                    editor =
                        getEditorComponent(
                            factory,
                            context,
                            field.getSubstance(substance),
                            setter,
                            0,
                            true,
                            null);
                    context.addEditor(editor);
                    container.addView(editor);
                } catch (Exception e) {
                    // If the editor failed, we'll try a normal view
                    logger.error("Failed to build editor component for "+
                                 substance+"."+field.getName(),e);
                }
            }
        } 
        if (editor==null) {
            Object value = field.getThroughAccessor(substance);
            if (!getViewer(substance,
                           field,
                           value,
                           container,
                           factory,
                           context)) 
            {
                Enum enum = GuiAC.getEnum(field);
                if (enum != null) {
                    container.addView(
                        factory.createView(
                            field.getName(),
                            "Enum",
                            new Object[] { value, enum, substance, field },
                            context));
                } else {
                    container.addView(
                        factory.createView(
                            field.getName(),
                            "Field",
                            new Object[] { value, substance, field },
                            context));
                }
            }

            if (GuiAC.isEditable(substance, field) && !objectView.isReadOnly()) {
                container.addView(
                    getEditButton(factory, substance, field, context));
            }
        }
        return container;
    }

    /**
     * Build a view containing a label for the name of the field, and
     * the view of the reference.
     *
     * @param factory the view factory
     * @param context the display context
     * @param substance the object the field is part of
     * @param field the field item 
     * @param embedded use embbeded editors
     */
    static protected View getReferenceFieldPane(
        ViewFactory factory,
        DisplayContext context,
        Object substance,
        FieldItem field,
        boolean embedded,
        ObjectView objectView, 
        MemberItemView memberView) 
    {
        logger.debug("reference field : " + field.getName());
        /*
        Boolean attrValue = (Boolean)field.getAttribute(GuiAC.BORDER);
        boolean border = attrValue!=null && attrValue.booleanValue();
        */
        CompositeView container =
            factory.createCompositeView(
                "field[" + field.getName() + "]",
                "Container",
                new Object[] { new Integer(Constants.HORIZONTAL)},
                context);

        Border border = GuiAC.getBorder(field);
        if (border != null) {
            border.setTitle(GuiAC.getLabel(field));
            container.setViewBorder(border);
        }

        container.setStyle((String) field.getAttribute(GuiAC.STYLE));
        Boolean displayLabel =
            (Boolean) field.getAttribute(GuiAC.DISPLAY_LABEL);

        if ((border == null)
            && (displayLabel == null
                || (displayLabel != null && displayLabel.booleanValue()))) {

            container.addView(
                factory.createView(
                    GuiAC.getLabel(field) + ": ",
                    "Label",
                    context));
        }

        if ((memberView != null && memberView.isEmbedded()) && !embedded) {
            FieldItem oppositeRole =
                (FieldItem) field.getAttribute(RttiAC.OPPOSITE_ROLE);
            if (oppositeRole instanceof CollectionItem) {
                loggerAssoc.debug("Ignoring collection oppositeRole " + oppositeRole);
                oppositeRole = null;
            }
            Collaboration.get().addAttribute(GuiAC.OPPOSITE_ROLE, oppositeRole);
            try {
                logger.debug("embedded view for " + field);
                container.addView(
                    factory.createObjectView(
                        "embbeded " + field.getName(),
                        field.getThroughAccessor(substance),
                        substance,
                        field,
                        context));
            } finally {
                Collaboration.get().addAttribute(GuiAC.OPPOSITE_ROLE, null);
            }
        } else {
            if (memberView.isEmbeddedEditor(embedded)
                && GuiAC.isEditable(substance, field) 
                && !objectView.isReadOnly())
            {
                MethodItem setter = field.getSetter();
                FieldEditor editor =
                    getEditorComponent(
                        factory,
                        context,
                        field.getSubstance(substance),
                        setter,
                        0,
                        true,
                        null);
                container.addView(editor);
                context.addEditor(editor);
            } else {
                //Object value = field.getThroughAccessor(substance);
                //if (!getViewer(substance,field,value,container,factory,context)) {
                View refView = 
                    factory.createView(
                        field.getName(),
                        "Reference",
                        new Object[] {
                            field.getThroughAccessor(substance),
                            field.getSubstance(substance),
                            field.getField()},
                        context);
                if (refView instanceof LinkGenerator)
                    ((LinkGenerator)refView).setEnableLinks(objectView.areLinksEnabled());
                container.addView(refView);

                if (GuiAC.isEditable(field.getSubstance(substance),field.getField())
                    && !objectView.isReadOnly()) {
                    container.addView(
                        getEditButton(
                            factory,
                            field.getSubstance(substance),
                            field.getField(),
                            context));
                }
                //}
            }
        }
        return container;
    }

    /**
     * Adds choices within a container containing a combobox and sort
     * them.
     *
     * @param choice combo box model to fill
     * @param type type of objects to fill the model with
     * @param field associated field item
     * @param nullAllowed boolean telling wether the add null to themodel
     * @param nullLabel if nullAllowed==true, the label to use for the null value (if not null)
     * @param predicate if not null, only add objects which match this
     * predicate 
     */
    static protected void addChoices(
        ComboBoxModel choice,
        ClassItem type,
        Enum enum,
        Object substance,
        FieldItem field,
        boolean nullAllowed,
        String nullLabel,
        Predicate predicate) 
    {
        // use JacObjects.getObjects(type) instead
        Collection all_objects = null;
        logger.debug("ObjectChooser.type = " + type
                + "; predicate=" + predicate
                + "; field=" + field
                + "; nullAllowed=" + nullAllowed);
        if (field != null) {
            Object fieldChoice = field.getAttribute(GuiAC.FIELD_CHOICE);
            logger.debug("  fieldChoice = "+fieldChoice);            
            if (fieldChoice instanceof MethodItem) {
                try {
                    all_objects =
                        (Collection) (((MethodItem) fieldChoice)
                            .invoke(null, ExtArrays.emptyObjectArray));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (fieldChoice instanceof CollectionItem) {
                all_objects = ((CollectionItem)fieldChoice).getActualCollectionThroughAccessor(substance);
            }
            choice.setType(type);
        }

        if (enum!=null) {
            Iterator it = enum.getValues().iterator();
            while (it.hasNext()) {
                String str = (String) it.next();
                choice.addObject(new Integer(enum.string2int(str)), str);
            }
            choice.setType(type);
            if (nullAllowed) {
                choice.setNullLabel(nullLabel);
                choice.addObject(null);
            }
        } else {

            if (all_objects == null) {
                all_objects = ObjectRepository.getObjects(type);
                choice.setType(type);
            }
            if (nullAllowed) {
                choice.setNullLabel(nullLabel);
                choice.addObject(null);
            }
            
            Iterator i = all_objects.iterator();
            while (i.hasNext()) {
                Object object = i.next();
                if (predicate == null || predicate.apply(object)) {
                    choice.addObject(object);
                }
            }
            choice.sort();
        }
    }

    /**
     * Constructs an edit button for reference views. 
     */
    static protected View getEditButton(
        ViewFactory factory,
        Object substance,
        FieldItem field,
        DisplayContext context)
    {
        MethodView methodView =
            (MethodView) factory.createView(
                "edit " + field.getName(),
                "Method",
                new Object[] { substance, field.getSetter()},
                context);
        methodView.setIcon(ResourceManager.getResource("edit_icon"));
        methodView.setOnlyIcon(true);
        return methodView;
    }

    /**
     * Builds a view that will display a given collection field of an
     * object.
     *
     * @param factory the view factory to use to build other inner views
     * @param context the display context
     * @param substance the object that contains the collection
     * @param collection the collection to show
     * @return the associated view
     * @see TableModel 
     */
    static public View getCollectionPane(
        ViewFactory factory,
        DisplayContext context,
        Object substance,
        ObjectView objectView,
        CollectionItemView memberView,
        CollectionItem collection) throws ViewFactory.UnhandledViewTypeException 
    {
        logger.debug("collection : " + collection.getName());
        String label = "collection[" + collection.getName() + "]";
        /*
        View newPane;
        if (collection.isMap() && collection.getAttribute("Gui.tableView")==null) {
           newPane = new MapView(parent,this,substance,collection);
        } else {
           newPane = new CollectionView(parent,this,substance,collection);
        }
        newPane.setName( collection.getName() );
        }
        */

        CompositeView container =
            factory.createCompositeView(
                label,
                "Container",
                new Object[] { new Integer(Constants.VERTICAL)},
                context);

        Boolean displayLabel =
            (Boolean) collection.getAttribute(GuiAC.DISPLAY_LABEL);
        Border border = GuiAC.getBorder(collection);
        logger.debug("  border="+border);
        if (displayLabel == null
            || (displayLabel != null && displayLabel.booleanValue())) {

            if (border != null) {
                border.setTitle(GuiAC.getLabel(collection));
            } else {
                if (GuiAC.isChoiceView(collection)) {
                    border = new Border(null, Border.LEFT, Border.LINE);
                } else {
                    border =
                        new Border(
                            GuiAC.getLabel(collection),
                            Border.LEFT,
                            Border.LINE);
                }
            }
            container.setViewBorder(border);
        }

        container.setStyle((String) collection.getAttribute(GuiAC.STYLE));

        View view = null;
        Collaboration.get().addAttribute(
            GuiAC.OPPOSITE_ROLE,
            collection.getAttribute(RttiAC.OPPOSITE_ROLE));
        GuiAC.pushGraphicContext(collection);
        try {

            if (memberView.getViewType() != null) {

                view =
                    factory.createView(
                        label,
                        memberView.getViewType(),
                        new Object[] { collection, substance, memberView },
                        context);
                container.addView(view);

            } else if (memberView != null && memberView.isEmbedded()) {
                Collection values =
                    collection.getActualCollectionThroughAccessor(substance);
                Iterator it = values.iterator();
                while (it.hasNext()) {
                    Object value = it.next();
                    container.addView(
                        factory.createView(
                            "embbeded " + collection.getName(),
                            "Object",
                            new Object[] { value },
                            context));
                }

            } else {
                if (GuiAC.isTableView(collection)) {

                    // TABLE
                    logger.debug("  isTableView");
                    ExtendedTableModel model =
                        new TableModel(
                            collection,
                            substance,
                            objectView != null
                                ? objectView.getName()
                                : GuiAC.DEFAULT_VIEW,
                            factory);

                    Collection filteredColumns = 
                        (Collection)collection.getAttribute(GuiAC.FILTERED_COLUMNS);
                    if (filteredColumns!=null) {
                        model = new TableFilter(model,filteredColumns);
                    }

                    model = new TableSorter(model);

                    view =
                        factory.createView(
                            label,
                            "Table",
                            new Object[] {
                                collection,
                                substance,
                                model,
                                memberView },
                            context);

                } else if (GuiAC.isChoiceView(collection)) {

                    // CHOICE VIEW
                    logger.debug("  isChoiView");
                    ComboBoxModel model =
                        new ComboBoxModel(collection, substance);
                    view =
                        factory.createView(
                            label,
                            "ChoiceCollection",
                            new Object[] {
                                collection,
                                substance,
                                model,
                                memberView },
                            context);

                } else {

                    // LIST
                    logger.debug("  isListView");
                    ListModel model = new ListModel(collection, substance);
                    view =
                        factory.createView(
                            label,
                            "List",
                            new Object[] {
                                collection,
                                substance,
                                model,
                                memberView },
                            context);
                }

                Length height =
                    (Length) collection.getAttribute(GuiAC.VIEW_HEIGHT);
                Length width =
                    (Length) collection.getAttribute(GuiAC.VIEW_WIDTH);
                /*
                if (height == null)
                    height = new Length("70px");
                if (width == null)
                    width = new Length("500px");
                */
                view.setSize(width,height);
                container.addView(view);
            }
        } finally {
            Collaboration.get().addAttribute(GuiAC.OPPOSITE_ROLE, null);
            GuiAC.popGraphicContext();
        }

        return container;
    }

    /**
     * Gets a composite panel containing a set of methods that are held
     * by the substance object. */

    static protected CompositeView getMethodsPane(
        ViewFactory factory,
        DisplayContext context,
        Object substance,
        Collection methods,
        String category,
        ObjectView objectView) throws ViewFactory.UnhandledViewTypeException 
    {
        CompositeView methodpane =
            factory.createCompositeView(
                "methods",
                "Container",
                new Object[] { new Integer(Constants.VERTICAL)},
                context);
        methodpane.setStyle("methods");

        CompositeView subMP =
            factory.createCompositeView(
                "methods0",
                "Container",
                new Object[] { new Integer(Constants.HORIZONTAL)},
                context);

        Iterator it = methods.iterator();
        int i = 0;
        int nbMethods = 0;

        while (it.hasNext()) {

            MethodItem curmeth = (MethodItem) it.next();
            MemberItemView memberView =
                GuiAC.getView(curmeth, objectView.getName());

            logger.debug("handling method " + curmeth);

            if (i == 3) {
                methodpane.addView(subMP);
                subMP =
                    factory.createCompositeView(
                        "methods" + i,
                        "Container",
                        new Object[] { new Integer(Constants.HORIZONTAL)},
                        context);
                i = 0;
            }

            if (curmeth == null)
                continue;

            if (!GuiAC.isMemberInCategory(curmeth, category)) {
                logger.debug("category, skipping " + curmeth);
                continue;
            }

            if (!GuiAC.isVisible(substance, curmeth)) {
                logger.debug("invisible, skipping " + curmeth);
                continue;
            }

            subMP.addView(
                getMethodView(
                    curmeth,
                    substance,
                    context,
                    factory,
                    memberView));
            i++;
            nbMethods++;
        }

        if (nbMethods > 0) {
            methodpane.addView(subMP);
            return methodpane;
        } else {
            return null;
        }
    }

    /**
     * Build view for a method
     * @param method the method item to build a view for
     * @param substance the object the method shall be invoked on
     * @param context display context
     * @param factory a view factory
     * @return a MethodView
     */
    public static MethodView getMethodView(
        AbstractMethodItem method,
        Object substance,
        DisplayContext context,
        ViewFactory factory,
        MemberItemView memberView) 
    {
        String text = GuiAC.getLabel(method);
        if (method.getParameterTypes().length > 0) {
            text += "...";
        }
        MethodView methodView = null;
        if (memberView != null && memberView.isEmbedded()) {
            EditorContainer inputView =
                (EditorContainer) factory.createView(
                    "parameters[" + method.getName() + "]",
                    "InputParameters",
                    new Object[] { method, substance, null },
                    context);
            methodView =
                (MethodView) factory.createView(
                    text,
                    "EmbeddedMethod",
                    new Object[] { substance, method, inputView },
                    context);
        } else {
            methodView =
                (MethodView) factory.createView(
                    text,
                    "Method",
                    new Object[] { substance, method },
                    context);
        }
        methodView.setIcon(GuiAC.getIcon(method));
        return methodView;
    }

    /**
     * <p>Create a view containing editor components for the parameters of
     * a method.</p>
     *
     * <p>Parameters of type
     * <code>org.objectweb.jac.aspects.gui.DisplayContext</code> are
     * not displayed.</p>
     *
     * <p>If method is a MixinMethodItem, the first parameter is at
     * index 1 of the parameters array.</p>
     *
     * @param factory the ViewFactory
     * @param context the DisplayContext
     * @param method the method whose parameters you the view of
     * @param substance the object on which the method will be
     * called. It used to get a default value if the method is a
     * setter.  
     * @param parameters 
     * @return a CompositeView containing an editor component for each
     * parameter of the method. The returned View implements the
     * EditorContainer interface 
     */
    static public View createParameters(
        ViewFactory factory,
        DisplayContext context,
        final AbstractMethodItem method,
        Object substance,
        Object[] parameters) 
    {
        logger.debug("createParameters(" + method.getLongName() + ")");

        CompositeView panel =
            factory.createCompositeView(
                method.toString(),
                "ParameterContainer",
                new Object[] { Boolean.FALSE },
                context);

        Class[] paramTypes = method.getParameterTypes();
        String[] paramNames = GuiAC.getParameterNames(method);

        for (int i=0; i<paramTypes.length; i++) 
        {

            if (paramTypes[i]==DisplayContext.class) 
                continue;

            String paramName = null;
            if (paramNames != null && paramNames.length>i && paramNames[i] != null) {
                paramName = paramNames[i];
            } else {
                paramName = "arg" + i + " (" + paramTypes[i] + ")";
            }

            CompositeView container =
                factory.createCompositeView(
                    method.toString(),
                    "Container",
                    new Object[] { new Integer(Constants.HORIZONTAL)},
                    context);
            container.addView(
                factory.createView(
                    paramName + " : ",
                    "Label",
                    ExtArrays.emptyObjectArray,
                    context));
            //         ((Component)ve).setName( "arg" + i );

            try {
                FieldEditor editor =
                    getEditorComponent(
                        factory,
                        context,
                        substance,
                        method,
                        i,
                        false,
                        parameters != null ? method.getParameter(parameters,i) : null);
                ((EditorContainer) panel).addEditor(editor);
                container.addView(editor);
            } catch (Exception e) {
                logger.error(
                    "Failed to build editor component for "
                        + method + "[" + i + "]",e);
            }

            panel.addView(container);
        }
        return panel;
    }

    /**
     * Returns a ValueEditor suitable for editing the i-th parameter of
     * a method.
     *
     * @param factory the view factory
     * @param substance the substance object
     * @param method the method that contains the parameter
     * @param i the parameter index 
     * @param embedded wether the editor is an embedded field
     * editor. If true, the component will commit changes when it
     * looses the focus (only works for swing).
     * @param value the initial edited value. Used only if non null. 
     */
    public static FieldEditor getEditorComponent(
        ViewFactory factory,
        DisplayContext context,
        Object substance,
        AbstractMethodItem method,
        int i,
        boolean embedded,
        Object value) 
    {
        logger.debug("getEditorComponent " + method + "[" + i + "]");
        Class[] paramTypes = method.getParameterTypes();
        ClassRepository cr = ClassRepository.get();
        ClassItem paramType = cr.getClass(paramTypes[i]);
        String[] passwordParams =
            (String[]) method.getAttribute(GuiAC.PASSWORD_PARAMETERS);
        Object[] defaultValues =
            (Object[]) method.getAttribute(GuiAC.DEFAULT_VALUES);
        FieldItem[] parameterFields =
            (FieldItem[]) method.getAttribute(RttiAC.PARAMETERS_FIELDS);
        Object[] parameterChoices =
            (Object[]) method.getAttribute(GuiAC.PARAMETER_CHOICES);
        boolean[] editableChoices =
            (boolean[]) method.getAttribute(GuiAC.EDITABLE_CHOICES);
        MetaItem[] parametersType =
            (MetaItem[]) method.getAttribute(RttiAC.PARAMETER_TYPES);
        Enum[] enums = (Enum[]) method.getAttribute(GuiAC.PARAMETER_ENUMS);
        CollectionItem[] linkedColls =
            (CollectionItem[]) method.getAttribute(GuiAC.LINKED_PARAMETERS);

        FieldItem editedField = null;
        FieldItem parameterField = null;

        Object defaultValue = null;
        boolean defaultValueSet = false;
        if (defaultValues != null) {
            defaultValue = defaultValues[i];
            defaultValueSet = true;
        }
        if (value != null) {
            defaultValue = value;
            defaultValueSet = true;
        }
        Object choiceObject = null;
        Object[] choice = null;
        Enum enum = null;
        boolean editableChoice = false;
        // the most specific type of the edited value
        MetaItem type = null;
        // a stack of type of the edited value (most specific on top)
        Stack types = new Stack();

        if (parameterFields != null) {
            parameterField = parameterFields[i];
        }
        // if it's a set-method, get the default value from the field
        // as well as the editableChoice attribute
        editedField = ((MethodItem) method).getSetField();
        if (editedField != null) {
            editedField = cr.getActualField(substance,editedField);
            logger.debug("edited field = " + editedField);
            if (substance != null) {
                defaultValue = editedField.getThroughAccessor(substance);
                defaultValueSet = true;
            }

            Object[] def_value =
                ((Object[]) editedField.getAttribute(GuiAC.DYN_DEFAULT_VALUE));
            if ((!RttiAC.isNullAllowed(editedField))
                && (defaultValue == null)
                && (def_value != null))
                defaultValue =
                    ((MethodItem) def_value[0]).invokeStatic(
                        new Object[] { editedField, def_value[1] });

            choiceObject = editedField.getAttribute(GuiAC.FIELD_CHOICE);
            editableChoice = GuiAC.isEditableChoice(editedField);
            enum = (Enum) editedField.getAttribute(GuiAC.FIELD_ENUM);
            logger.debug(
                "choiceObject = "+ choiceObject
                + (editableChoice ? " editable" : ""));
            type = RttiAC.getFieldType(editedField,substance);
            if (type != null) {
                logger.debug("adding rtti type = " + type);
                if (!types.contains(type))
                    types.add(0, type);
            }
            if (type == null)
                type = editedField.getTypeItem();
            if (editedField.getTypeItem() != null) {
                logger.debug("adding type item = " + editedField.getTypeItem());
                if (!types.contains(editedField.getTypeItem()))
                    types.add(0, editedField.getTypeItem());
            }
        }

        if (parameterField != null) {
            logger.debug("parameter field = " + parameterField);

            Object[] def_value =
                ((Object[]) parameterField
                    .getAttribute(GuiAC.DYN_DEFAULT_VALUE));
            if ((!RttiAC.isNullAllowed(parameterField))
                && (defaultValue == null)
                && (def_value != null))
                defaultValue =
                    ((MethodItem) def_value[0]).invokeStatic(
                        new Object[] { parameterField, def_value[1] });

            choiceObject = parameterField.getAttribute(GuiAC.FIELD_CHOICE);
            editableChoice = GuiAC.isEditableChoice(parameterField);
            enum = (Enum) parameterField.getAttribute(GuiAC.FIELD_ENUM);
            logger.debug("choiceObject = "
                    + choiceObject
                    + (editableChoice ? " editable" : ""));
            type = RttiAC.getFieldType(parameterField,substance);
            if (type != null) {
                logger.debug("adding rtti type = " + type);
                if (!types.contains(type))
                    types.add(0, type);
            }
            if (type == null)
                type = parameterField.getTypeItem();
            if (parameterField.getTypeItem() != null) {
                logger.debug("adding type item = " + parameterField.getTypeItem());
                if (!types.contains(parameterField.getTypeItem()))
                    types.add(0, parameterField.getTypeItem());
            }
        }

        if (defaultValueSet && defaultValue != null) {
            if (type == null)
                type = cr.getClass(defaultValue);
            if (!types.contains(cr.getClass(defaultValue)))
                types.add(0, cr.getClass(defaultValue));
            logger.debug("adding default value type = "
                    + cr.getClass(defaultValue));
        }
        if (parametersType != null) {
            if (type == null && parametersType[i] instanceof ClassItem)
                type = (ClassItem) parametersType[i];
            if (!types.contains(parametersType[i]))
                types.add(0, parametersType[i]);
            logger.debug("adding rtti parameter type = " + parametersType[i]);
        }
        if (type == null)
            type = paramType;
        if (!types.contains(paramType))
            types.add(0, paramType);
        logger.debug("adding parameter type = "
                     + paramType);

        if (parameterField != null) {
            choiceObject = parameterField.getAttribute(GuiAC.FIELD_CHOICE);
        }

        if (enum == null && enums != null)
            enum = enums[i];

        logger.debug("types: " + types);
        logger.debug("type=" + type);

        if (parameterChoices != null) {
            choiceObject = parameterChoices[i];
        }

        boolean classChoice = false;
        if (choiceObject==null) {
            choiceObject = type.getAttribute(GuiAC.CLASS_CHOICES);
            logger.debug("classChoices="+choiceObject);
            classChoice = choiceObject!=null;
        }

        if (choiceObject != null) {
            if (choiceObject instanceof MethodItem) {
                MethodItem choiceMethod = (MethodItem) choiceObject;
                logger.debug("choiceMethod="+choiceMethod);
                try {
                    if (classChoice) {
                        choiceObject = 
                            choiceMethod.invokeStatic(new Object[] {type});
                    } else {
                        if (choiceMethod.isStatic()) {
                            choiceObject =
                                choiceMethod.invokeStatic(
                                    new Object[] { substance });
                        } else {
                            choiceObject =
                                choiceMethod.invoke(
                                    substance, 
                                    ExtArrays.emptyObjectArray);
                        }
                    }
                    logger.debug("choiceMethod returned "+choiceObject);
                } catch (Exception e) {
                    logger.error(
                        "Invocation of choice "+choiceMethod+" method failed",e);
                }
            }
            if (choiceObject instanceof Object[]) {
                choice = (Object[]) choiceObject;
            } else if (
                choiceObject.getClass().isArray()
                    && choiceObject.getClass().getComponentType().isPrimitive()) {
                choice = Classes.convertPrimitiveArray(choiceObject);
            } else if (choiceObject instanceof Collection) {
                choice = ((Collection) choiceObject).toArray();
            }
        }

        FieldEditor editor = null;
        String editorName =
            "editor " + Naming.getName(substance) + "." + 
            method.getName() + "[" + i + "]";

        boolean nullAllowed = false;
        if (editedField == null) {
            nullAllowed = RttiAC.isNullAllowedParameter(method, i);
        } else {
            nullAllowed = RttiAC.isNullAllowed(editedField);
        }

        if (linkedColls != null && linkedColls[i] != null) {

            ComboBoxModel model = new ComboBoxModel(linkedColls[i], substance);

            editor =
                factory.createEditor(
                    editorName,
                    "ObjectChooser",
                    new Object[] {
                        substance,
                        editedField,
                        model,
                        Boolean.FALSE },
                    context);

        } else if (choice != null) {
            if (editableChoices != null) {
                editableChoice = editableChoices[i];
            }
            ComboBoxModel model = new ComboBoxModel();
            for (int j = 0; j < choice.length; j++) {
                if (enum != null)
                    model.addObject(
                        choice[j],
                        enum.int2string(((Integer) choice[j]).intValue()));
                else
                    model.addObject(choice[j]);
            }
            if (nullAllowed)
                model.addObject(null);
            if (type instanceof ClassItem)
                model.setType((ClassItem) type);
            model.sort();

            editor =
                factory.createEditor(
                    editorName,
                    "ObjectChooser",
                    new Object[] {
                        substance,
                        editedField,
                        model,
                        ExtBoolean.valueOf(editableChoice)},
                    context);
        } else if (enum != null) {
            ComboBoxModel model = new ComboBoxModel();
            Iterator it = enum.getValues().iterator();
            while (it.hasNext()) {
                String str = (String) it.next();
                model.addObject(new Integer(enum.string2int(str)), str);
            }

            editor =
                factory.createEditor(
                    editorName,
                    "ObjectChooser",
                    new Object[] {
                        substance,
                        editedField,
                        model,
                        Boolean.FALSE },
                    context);

        } else if (
            !factory.hasViewerFor(
                "editor:" + ((MetaItem) types.peek()).getName())
                && (Wrappee.class.isAssignableFrom(paramTypes[i])
                    || paramTypes[i] == Object.class)) {
            // jac objects
            ClassItem collectionType = null;
            if (types.size() > 0)
                collectionType = (ClassItem) types.peek();
            else
                collectionType = paramType;
            Predicate predicate = null;
            if (method instanceof MethodItem) {
                CollectionItem[] collections =
                    ((MethodItem) method).getAddedCollections();
                if (collections != null && collections.length > 0) {
                    predicate =
                        new NotInCollectionPredicate(
                            (Collection) collections[0].getActualCollectionThroughAccessor(
                                substance));
                }
            }
            editor =
                createReferenceEditor(
                    factory,
                    context,
                    substance,
                    editedField,
                    editorName,
                    collectionType,
                    predicate,
                    nullAllowed,null,
                    GuiAC.isCreationAllowedParameter(method, i));
            /*
        } else if (paramTypes[i].isArray()
                   || Collection.class.isAssignableFrom(paramTypes[i])) {
            */
            // array and collections
            //         editor = new ArrayEditor(paramTypes[i]);
        } else {

            boolean isPassword =
                passwordParams != null && passwordParams[i].equals("true");

            Vector tried_types = new Vector();
            Stack viewerTypes = new Stack();
            while (!types.empty() && editor == null) {
                String typeName = ((MetaItem)types.pop()).getName();
                viewerTypes.add(0, typeName);
                typeName = "editor:" + typeName;
                tried_types.add(typeName);
                logger.debug("Trying " + typeName);
                if (factory.hasViewerFor(typeName) && !isPassword) {
                    logger.debug("Factory has viewer for " + typeName);
                    editor =
                        (FieldEditor) factory.createView(
                            editorName,
                            typeName,
                            new Object[] { substance, editedField },
                            context);
                }
            }
            if (editor == null) {
                if (!isPassword)
                    logger.error(
                        "Could not find an editor component for any of the type "
                        + tried_types);
                editor =
                    factory.createEditor(
                        editorName,
                        "PrimitiveTypeEditor",
                        new Object[] {
                            substance,
                            editedField,
                            ExtBoolean.valueOf(isPassword)},
                        context);
            }
        }

        //if( fieldEditor != null && ve!=null ) {
        //   System.out.println("added focus listener for "+ve);
        //   System.out.println("--- Listener is: "+fieldEditor);
        //   ve.getValueComponent().addFocusListener(fieldEditor);
        //((Component)ve).addFocusListener(fieldEditor);
        //   }

        if (defaultValueSet) {
            logger.debug("setting default value for "
                    + method.getName() + " : " + defaultValue);
            editor.setValue(defaultValue);
        }

        editor.setEmbedded(embedded);

        logger.debug("type = " + type);
        logger.debug("types = " + types);

        if (parameterField == null) {
            parameterField = editedField;
        }
        Length width = null;
        Length[] cols = GuiAC.getMethodParametersWidth(method);
        if (cols != null) {
            width = cols[i];
        }
        if (width == null && parameterField != null) {
            width = GuiAC.getEditorWidth(parameterField);
        }
        if (width == null)
            width = GuiAC.getEditorWidth(type);

        Length height = null;
        Length[] rows = GuiAC.getMethodParametersHeight(method);
        if (rows != null) {
            height = rows[i];
        }
        if (height == null && parameterField != null) {
            height = GuiAC.getEditorHeight(parameterField);
        }
        if (height == null)
            height = GuiAC.getEditorHeight(type);

        editor.setSize(width,height);

        logger.debug("editedType = "+type);
        if (type instanceof ClassItem) {
            editor.setEditedType((ClassItem)type);
        } else if (type instanceof VirtualClassItem) {
            if (editedField!=null) 
                editor.setEditedType(editedField.getTypeItem());
            else
                editor.setEditedType(paramType);
        }
        return editor;

    }

    /**
     * Initialize the panels of a customized gui.
     *
     * @param factory the view factory
     * @param context the display context
     * @param internalView the CompositeView which holds the panels
     * @param customized the CustomizedGUI
     * @param panels if not null, overrides the content of panels
     * (panelID -> PanelContent)
     */
    public static void initCustomized(
        ViewFactory factory,
        DisplayContext context,
        CompositeView internalView,
        CustomizedGUI customized,
        Map panels) 
    {
        logger.debug("initCustomized...");
        // open the views on the objects in the right subpanes
        if (panels == null)
            panels = customized.getPaneContents();
        for (Iterator it = panels.keySet().iterator(); it.hasNext();) {
            String paneId = (String) it.next();
            PanelContent panel = (PanelContent) panels.get(paneId);
            logger.debug("init pane " + paneId + " -> " + panel);
            try {
                View view =
                    factory.createView(
                        paneId,
                        panel.getType(),
                        panel.getArgs(),
                        context);
                internalView.addView(view, paneId);
            } catch (Exception e) {
                logger.error(
                    "Failed to build content for pane \""
                        + paneId + "\"",e);
            }
            logger.debug("init pane " + paneId + " DONE");
        }
        logger.debug("initCustomized DONE");
    }

    /**
     * Sets a status bar to a customized view.
     *
     * @param factory the used factory
     * @param context the passed context
     * @param view the customized that will hold the status bar
     * @param statusBar the method item that defines the text to print within the status bar
     * @param position the position
     * (<code>Constants.TOP||Constants.BOTTOM</code>) */

    public static void setStatusBar(
        ViewFactory factory,
        DisplayContext context,
        CustomizedView view,
        MethodItem statusBar,
        String position) 
    {
        logger.debug("setStatusbar(" + statusBar + ")");
        StatusView statusView =
            (StatusView) factory.createView(
                "StatusBar",
                "StatusBar",
                new Object[] { statusBar },
                context);
        view.setStatusBar(statusView, position);
    }

    /**
     * Build the menu bar of a customized gui.
     *
     * @param factory the view factory
     * @param context the display context
     * @param view the CustomizedView where to put the menu bar
     * @param menuBars the menuBars
     */
    public static void setMenuBars(
        ViewFactory factory,
        DisplayContext context,
        CustomizedView view,
        Hashtable menuBars) 
    {
        logger.debug("setMenuBars(" + menuBars + ")");
        Menu menus;
        Iterator it = menuBars.values().iterator();
        while (it.hasNext()) {
            menus = (Menu) it.next();
            MenuView menuBar =
                (MenuView) factory.createView("MenuBar", "MenuBar", context);
            menuBar.setPosition(menus.getPosition());
            Iterator i = menus.getKeys().iterator();
            while (i.hasNext()) {
                String key = (String) i.next();
                logger.debug("createMenu " + key);
                Object item = menus.get(key);
                if (item instanceof Menu) {
                    Menu subMenu = (Menu) item;
                    menuBar.addSubMenu(
                        key,
                        subMenu.getIcon(),
                        createMenu(factory, context, key, subMenu));
                } else {
                    if (key == null) {
                        menuBar.addSeparator();
                    } else {
                        Callback callback = (Callback) item;
                        if (GuiAC.isVisible(callback.getMethod())) {
                            menuBar.addAction(
                                key,
                                callback.getMethod() != null
                                    ? GuiAC.getIcon(callback.getMethod())
                                    : null,
                                callback);
                        }
                    }
                }
            }
            view.setMenuBar(menuBar, menus.getPosition());
        }
    }

    /**
     * Creates a menu in a a customized gui.
     *
     * @param factory the view factory
     * @param context the display context
     * @param content the content of the menu view
     */

    public static MenuView createMenu(
        ViewFactory factory,
        DisplayContext context,
        String label,
        Menu content) 
    {
        MenuView menu = (MenuView) factory.createView(label, "Menu", context);
        Iterator i = content.getKeys().iterator();
        while (i.hasNext()) {
            String key = (String) i.next();
            if (key == null) {
                menu.addSeparator();
            } else {
                Object value = content.get(key);
                if (value instanceof Menu) {
                    // submenu
                    MenuView subMenu =
                        createMenu(factory, context, key, (Menu)value);
                    menu.addSubMenu(key, ((Menu) value).getIcon(), subMenu);
                } else {
                    // action or separator
                    logger.debug("menu action: " + key + "->" + value);
                    if (key == null) {
                        menu.addSeparator();
                    } else {
                        Callback callback = (Callback) value;
                        if (callback.getMethod() == null
                            || GuiAC.isVisible(callback.getMethod())) 
                        {
                            menu.addAction(
                                key, 
                                GuiAC.getIcon(callback), 
                                callback);
                        }
                    }
                }
            }
        }
        return menu;
    }

    /**
     * Build the toolbar of a customized gui.
     *
     * @param factory the view factory
     * @param context the display context
     * @param view the CustomizedView where to put the menu bar
     * @param toolbar the toolbar definition
     */
    public static void setToolBar(
        ViewFactory factory,
        DisplayContext context,
        CustomizedView view,
        Collection toolbar) 
    {
        logger.debug("setToolbar(" + toolbar + ")");
        MenuView toolbarView =
            (MenuView) factory.createView("ToolBar", "ToolBar", context);
        Iterator i = toolbar.iterator();
        while (i.hasNext()) {
            Callback callback = (Callback) i.next();
            logger.debug("createButton " + callback);
            if (callback != null) {
                toolbarView.addAction(
                    GuiAC.getLabel(callback.method),
                    GuiAC.getIcon(callback.method),
                    callback);
            } else {
                toolbarView.addSeparator();
            }
        }
        view.setToolBar(toolbarView);
    }

    /**
     * Builds a dialog box to enter the parameters of method
     *
     * @param substance object the method will be invoked on
     * @param method the method
     * @param parameters an array where to store the value enteres by the user
     * @param context a display context
     *
     * @see #createParameters(ViewFactory,DisplayContext,AbstractMethodItem,Object,Object[])
     */
    public static DialogView createInputDialog(
        Object substance,
        AbstractMethodItem method,
        Object[] parameters,
        DisplayContext context) 
    {
        ViewFactory factory = context.getDisplay().getFactory();
        EditorContainer inputView =
            (EditorContainer) factory.createView(
                "parameters",
                "InputParameters",
                new Object[] { method, substance, parameters },
                context);
        String description = (String) method.getAttribute(GuiAC.DESCRIPTION);
        String title = GuiAC.getLabel(method);
        DialogView dialog =
            (DialogView) factory.createView(
                method.getName(),
                "Dialog",
                new Object[] { inputView, null, title, description },
                context);
        dialog.setLabel(GuiAC.getLabel(method));
        return dialog;
    }

    /**
     * A generic view builder for a reference editor. Returns a combox
     * box or a text input depending on the field's configuration
     *
     * @param factory
     * @param context
     * @param substance the object holding the field
     * @param field the field to build an editor for (may be null)
     * @param editorName the name of the editor to build
     * @param type the type of the objects to choose from
     * @param predicate a predicate used to filter proposed objects in
     * the case of a combobox.
     * @param nullAllowed wether the user is authorised to select the null value
     * @param isCreationAllowed wether the user is authorised to create
     * new instances of type
     *
     * @see org.objectweb.jac.aspects.gui.ClassAppearenceGuiConf#selectWithIndexedField(ClassItem,CollectionItem,String) 
     */
    public static FieldEditor createReferenceEditor(
        ViewFactory factory,
        DisplayContext context,
        Object substance,
        FieldItem field,
        String editorName,
        ClassItem type,
        Predicate predicate,
        boolean nullAllowed,
        String nullLabel,
        boolean isCreationAllowed) 
    {
        logger.debug("createReferenceEditor isCreationAllowed="+isCreationAllowed);
        CollectionItem index =
            (CollectionItem) type.getAttribute(GuiAC.INDEXED_FIELD_SELECTOR);
        if (index != null) {
            return factory.createEditor(
                editorName,
                "IndexSelector",
                new Object[] {
                    substance,
                    field,
                    index,
                    GuiAC.getRepository(type),
                    ExtBoolean.valueOf(isCreationAllowed),
                    type.getAttribute(GuiAC.INDEX_NOT_FOUND_HANDLER)},
                context);
        } else {
            ComboBoxModel model = new ComboBoxModel();
            addChoices(model, type, null, substance, field, 
                       nullAllowed, nullLabel, predicate);
            return factory.createEditor(
                editorName,
                "ObjectChooser",
                new Object[] {
                    substance,
                    field,
                    model,
                    ExtBoolean.valueOf(isCreationAllowed)},
                context);
        }
    }

    public static FieldEditor createEnumEditor(
        ViewFactory factory,
        DisplayContext context,
        Object substance,
        FieldItem field,
        String editorName,
        Enum enum,
        boolean nullAllowed,
        String nullLabel) 
    {
        ComboBoxModel model = new ComboBoxModel();
        ClassItem type = ClassRepository.get().getClass("java.lang.Integer");
        addChoices(model, type, enum, substance, field, 
                   nullAllowed, nullLabel, null);
        return factory.createEditor(
            editorName,
            "ObjectChooser",
            new Object[] {
                substance,
                field,
                model,
                Boolean.FALSE},
            context);
    }
}
