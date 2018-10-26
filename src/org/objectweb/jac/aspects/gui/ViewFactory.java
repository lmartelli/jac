/*
  Copyright (C) 2002-2003 Laurent Martelli <laurent@aopsys.com>
                          Renaud Pawlak <renaud@aopsys.com>
  
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

import java.util.Arrays;
import java.util.Hashtable;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.ACConfiguration;
import org.objectweb.jac.core.Imports;
import org.objectweb.jac.core.NameRepository;
import org.objectweb.jac.core.rtti.AbstractMethodItem;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.util.ExtArrays;

/**
 * This is a generic view factory. It can be configured to use any
 * constructor to build views of any type.
 */
public class ViewFactory {
    static Logger logger = Logger.getLogger("gui.factory");

    static Imports imports = new Imports();

    static {
        imports.add("org.objectweb.jac.aspects.gui.*");
        imports.add("org.objectweb.jac.core.rtti.*");
        imports.add("java.lang.*");        
    }

    /**
     * Init a swing factory by setting the constuctor types. 
     */ 
    public static void init(String type, ViewFactory f) {
        f.setViewConstructor("Object", "GenericFactory.createObjectView");
        f.setViewConstructor("NoTab", "GenericFactory.createObjectViewNoTab");
        f.setViewConstructor ("InputParameters", "GenericFactory.createParameters");
        //      f.setViewConstructor("java.lang.Throwable", "ExceptionViewer.createExceptionViewer");
        if (type.equals("swing")) {
            f.setViewConstructor("ObjectView","swing.ObjectView");
            f.setViewConstructor("Customized", "swing.SwingCustomized");
            f.setViewConstructor("Reference", "swing.ReferenceView(Object,Object,FieldItem)");
            f.setViewConstructor("ObjectChooser", "swing.ObjectChooser");
            f.setViewConstructor("Tree", "swing.Tree");
            f.setViewConstructor("Panel", "swing.SwingPanelView");
            f.setViewConstructor("Label", "swing.SwingLabel");
            f.setViewConstructor("Tabbed", "swing.SwingTabbedView");
            f.setViewConstructor("ParameterContainer", "swing.SwingEditorContainer");
            f.setViewConstructor("Container", "swing.SwingContainerView");
            f.setViewConstructor("SingleSlotContainer", "swing.SingleSlotContainer");
            f.setViewConstructor("Desktop", "swing.DesktopView");
            f.setViewConstructor("Method", "swing.SwingMethodView");
            f.setViewConstructor("Field", "swing.SwingFieldView");
            f.setViewConstructor("Enum", "swing.EnumViewer(Object,Object,FieldItem)");
            f.setViewConstructor("Table", "swing.SwingTableView");
            f.setViewConstructor("List", "swing.List");
            f.setViewConstructor("Dialog", "swing.Dialog");
            f.setViewConstructor("MenuBar", "swing.MenuBar()");
            f.setViewConstructor("Menu", "swing.Menu()");
            f.setViewConstructor("ToolBar", "swing.ToolBar");
            f.setViewConstructor("StatusBar", "swing.StatusBar");

            f.setViewConstructor("Empty", "swing.SwingEmptyView");
  
            f.setViewConstructor("dateHour", "swing.DateHourViewer(Object,Object,FieldItem)");
            f.setViewConstructor("timestamp", "swing.TimestampViewer(Object,Object,FieldItem)");
            f.setViewConstructor("java.util.Date", "swing.DateViewer(Object,Object,FieldItem)");
            f.setViewConstructor("imageURL", "swing.ImageURLViewer(java.net.URL,Object,FieldItem)");
            f.setViewConstructor("image", "swing.ImageViewer(byte[],Object,FieldItem)");
            f.setViewConstructor("text", "swing.TextViewer(Object,Object,FieldItem)");
            f.setViewConstructor("Float","swing.FloatViewer(Object,Object,FieldItem)");
            f.setViewConstructor("Double","swing.FloatViewer(Object,Object,FieldItem)");
            f.setViewConstructor("double","swing.FloatViewer(Object,Object,FieldItem)");
            f.setViewConstructor("float","swing.FloatViewer(Object,Object,FieldItem)");
            f.setViewConstructor("percentage","swing.PercentViewer(Object,Object,FieldItem)");
            f.setViewConstructor("editor:percentage","swing.PercentEditor(Object,FieldItem)");

            f.setViewConstructor("org.objectweb.jac.lib.Attachment", "swing.AttachmentViewer(Object,Object,FieldItem)");

            //editors
            f.setViewConstructor("editor:java.lang.Boolean", "swing.BooleanEditor");
            f.setViewConstructor("editor:boolean", "swing.BooleanEditor");
            f.setViewConstructor("editor:java.io.File", "swing.FileEditor");
            f.setViewConstructor("editor:filePath", "swing.FilePathEditor");
            f.setViewConstructor("editor:org.objectweb.jac.util.File", "swing.FileEditor");
            f.setViewConstructor("editor:directory", "swing.DirectoryEditor");
            f.setViewConstructor("editor:java.net.URL", "swing.URLEditor");
            f.setViewConstructor("editor:java.util.Date", "swing.DateEditor");
            f.setViewConstructor("editor:java.awt.Point", "swing.PointEditor");
            f.setViewConstructor("editor:dateHour", "swing.DateHourEditor");
            f.setViewConstructor("editor:text", "swing.TextEditor");
            f.setViewConstructor("editor:javaCode", "swing.JavaCodeEditor");
            f.setViewConstructor("editor:password", "swing.PasswordFieldEditor");
            f.setViewConstructor("editor:org.objectweb.jac.lib.Attachment", "swing.AttachmentEditor");
            f.setViewConstructor("editor:java.lang.Float", "swing.FloatEditor");
            f.setViewConstructor("editor:java.lang.Double", "swing.FloatEditor");
            f.setViewConstructor("editor:java.lang.Integer", "swing.PrimitiveFieldEditor(Object,FieldItem)");
            f.setViewConstructor("editor:java.lang.Long", "swing.PrimitiveFieldEditor(Object,FieldItem)");
            f.setViewConstructor("editor:java.lang.Short", "swing.PrimitiveFieldEditor(Object,FieldItem)");
            f.setViewConstructor("editor:java.lang.Character", "swing.PrimitiveFieldEditor(Object,FieldItem)");
            f.setViewConstructor("editor:java.lang.Byte", "swing.PrimitiveFieldEditor(Object,FieldItem)");
            f.setViewConstructor("editor:float", "swing.FloatEditor");
            f.setViewConstructor("editor:double", "swing.FloatEditor");
            f.setViewConstructor("editor:integer", "swing.PrimitiveFieldEditor(Object,FieldItem)");
            f.setViewConstructor("editor:long", "swing.PrimitiveFieldEditor(Object,FieldItem)");
            f.setViewConstructor("editor:short", "swing.PrimitiveFieldEditor(Object,FieldItem)");
            f.setViewConstructor("editor:char", "swing.PrimitiveFieldEditor(Object,FieldItem)");
            f.setViewConstructor("editor:java.lang.String", "swing.PrimitiveFieldEditor(Object,FieldItem)");
            f.setViewConstructor("PrimitiveTypeEditor", "swing.PrimitiveFieldEditor(Object,FieldItem,boolean)");

            f.setViewConstructor("CollectionItemView", "swing.CollectionItemView");
         
            // table cells viewers
            f.setViewConstructor("cell:java.util.Date", "swing.DateViewer()");
            f.setViewConstructor("cell:dateHour", "swing.DateHourViewer()");
            f.setViewConstructor("cell:timestamp", "swing.TimestampViewer()");
            f.setViewConstructor("cell:imageURL", "swing.ImageURLViewer()");
            f.setViewConstructor("cell:Reference", "swing.ReferenceView()");
            f.setViewConstructor("cell:Enum", "swing.EnumViewer()");
            f.setViewConstructor("cell:java.lang.Integer", "swing.IntViewer()");
            f.setViewConstructor("cell:java.lang.Long", "swing.IntViewer()");
            f.setViewConstructor("cell:int", "swing.IntViewer()");
            f.setViewConstructor("cell:long", "swing.IntViewer()");
            f.setViewConstructor("cell:java.lang.Float", "swing.FloatViewer()");
            f.setViewConstructor("cell:java.lang.Double", "swing.FloatViewer()");
            f.setViewConstructor("cell:float", "swing.FloatViewer()");
            f.setViewConstructor("cell:double", "swing.FloatViewer()");
            f.setViewConstructor("cell:percentage","swing.PercentViewer()");
            f.setViewConstructor("cell:org.objectweb.jac.lib.Attachment", "swing.AttachmentViewer()");
            f.setViewConstructor("cell:Method", "swing.SwingMethodView");
            f.setViewConstructor("cell:java.lang.Boolean", "swing.BooleanViewer()");
            f.setViewConstructor("cell:boolean", "swing.BooleanViewer()");
            //f.setViewConstructor("cell:List", "swing.()");

        } else if(type.equals("web")) {
         
            f.setViewConstructor("ObjectView", "web.ObjectView");
            f.setViewConstructor("Empty", "web.Empty");
            f.setViewConstructor("Customized", "web.Customized");
            f.setViewConstructor("Panel", "web.Panel");
            f.setViewConstructor("Container", "web.Container");
            f.setViewConstructor("SingleSlotContainer", "web.SingleSlotContainer");
            f.setViewConstructor("Label", "web.Label");
            f.setViewConstructor("Reference", "web.ReferenceView(Object,Object,FieldItem)");
            f.setViewConstructor("Enum", "web.EnumViewer(Object,Object,FieldItem)");
            f.setViewConstructor("Field", "web.PrimitiveField");
            f.setViewConstructor("Table", "web.Table");
            f.setViewConstructor("ChoiceCollection", "web.ChoiceCollection");
            f.setViewConstructor("List", "web.List");
            f.setViewConstructor("Tree", "web.Tree");
            f.setViewConstructor("Tabbed", "web.Tabs");
            f.setViewConstructor("Method", "web.Method");
            f.setViewConstructor("EmbeddedMethod", "web.EmbeddedMethod");
            f.setViewConstructor("Window", "web.Page");
            f.setViewConstructor("RefreshWindow", "web.RefreshPage");
            f.setViewConstructor("Dialog", "web.Dialog");
            f.setViewConstructor("MenuBar", "web.MenuBar");
            f.setViewConstructor("StatusBar", "web.StatusBar");
            f.setViewConstructor("ToolBar", "web.ToolBar");
            f.setViewConstructor("Menu", "web.Menu");

            f.setViewConstructor("dateHour", "web.DateHourViewer(Object,Object,FieldItem)");
            f.setViewConstructor("timestamp", "web.TimestampViewer(Object,Object,FieldItem)");
            f.setViewConstructor("java.util.Date", "web.DateViewer(Object,Object,FieldItem)");

            f.setViewConstructor("Float", "web.FloatViewer(Object,Object,FieldItem)");
            f.setViewConstructor("Integer","web.IntViewer(Object,Object,FieldItem)");
            f.setViewConstructor("Long","web.IntViewer(Object,Object,FieldItem)");
            f.setViewConstructor("int","web.IntViewer(Object,Object,FieldItem)");
            f.setViewConstructor("long","web.IntViewer(Object,Object,FieldItem)");
            f.setViewConstructor("Double", "web.FloatViewer(Object,Object,FieldItem)");
            f.setViewConstructor("float", "web.FloatViewer(Object,Object,FieldItem)");
            f.setViewConstructor("double", "web.FloatViewer(Object,Object,FieldItem)");
            f.setViewConstructor("percentage", "web.PercentViewer(Object,Object,FieldItem)");
            f.setViewConstructor("org.objectweb.jac.util.Matrix", "web.MatrixView(Object,Object,FieldItem)");

            f.setViewConstructor("imageURL", "web.ImageURLViewer(Object,Object,FieldItem)");
            f.setViewConstructor("text", "web.TextViewer");
         
            // table cells viewers
            f.setViewConstructor("cell:imageURL", "web.ImageURLViewer()");
            f.setViewConstructor("cell:java.util.Date", "web.DateViewer()");
            f.setViewConstructor("cell:dateHour", "web.DateHourViewer()");
            f.setViewConstructor("cell:timestamp", "web.TimestampViewer()");
            f.setViewConstructor("cell:java.lang.Integer", "web.IntViewer()");
            f.setViewConstructor("cell:java.lang.Long", "web.IntViewer()");
            f.setViewConstructor("cell:int", "web.IntViewer()");
            f.setViewConstructor("cell:long", "web.IntViewer()");
            f.setViewConstructor("cell:java.lang.Float", "web.FloatViewer()");
            f.setViewConstructor("cell:java.lang.Double", "web.FloatViewer()");
            f.setViewConstructor("cell:float", "web.FloatViewer()");
            f.setViewConstructor("cell:double", "web.FloatViewer()");
            f.setViewConstructor("cell:percentage", "web.PercentViewer()");
            f.setViewConstructor("cell:Reference", "web.ReferenceView()");
            f.setViewConstructor("cell:Enum", "web.EnumViewer()");
            f.setViewConstructor("cell:org.objectweb.jac.lib.Attachment", "web.AttachmentViewer()");
            f.setViewConstructor("cell:java.util.List", "web.CompactList()");

            //editors
            f.setViewConstructor("ParameterContainer", "web.EditorContainer");
            f.setViewConstructor("PrimitiveTypeEditor", "web.PrimitiveFieldEditor(Object,FieldItem,boolean)");
            f.setViewConstructor("ObjectChooser", "web.ObjectChooser");
            f.setViewConstructor("IndexSelector", "web.IndexSelector");
            f.setViewConstructor("IndicesSelector", "web.IndicesSelector");
            f.setViewConstructor("editor:java.util.Date", "web.DateEditor");
            f.setViewConstructor("editor:dateHour", "web.DateHourEditor");
            f.setViewConstructor("editor:java.net.URL", "web.URLEditor");
            f.setViewConstructor("editor:java.lang.Boolean", "web.BooleanEditor");
            f.setViewConstructor("editor:boolean", "web.BooleanEditor");
            f.setViewConstructor("editor:text", "web.TextEditor");
            f.setViewConstructor("editor:javaCode", "web.TextEditor");
            f.setViewConstructor("editor:password", "web.PasswordFieldEditor");
            f.setViewConstructor("editor:java.lang.Float", "web.FloatEditor");
            f.setViewConstructor("editor:java.lang.Double", "web.FloatEditor");
            f.setViewConstructor("editor:java.lang.Integer", "web.PrimitiveFieldEditor(Object,FieldItem)");
            f.setViewConstructor("editor:java.lang.Long", "web.PrimitiveFieldEditor(Object,FieldItem)");
            f.setViewConstructor("editor:java.lang.Short", "web.PrimitiveFieldEditor(Object,FieldItem)");
            f.setViewConstructor("editor:java.lang.Character", "web.PrimitiveFieldEditor(Object,FieldItem)");
            f.setViewConstructor("editor:float", "web.FloatEditor");
            f.setViewConstructor("editor:double", "web.FloatEditor");
            f.setViewConstructor("cell:percentage", "web.PercentEditor(Object,FieldItem)");
            f.setViewConstructor("editor:integer", "web.PrimitiveFieldEditor(Object,FieldItem)");
            f.setViewConstructor("editor:long", "web.PrimitiveFieldEditor(Object,FieldItem)");
            f.setViewConstructor("editor:short", "web.PrimitiveFieldEditor(Object,FieldItem)");
            f.setViewConstructor("editor:char", "web.PrimitiveFieldEditor(Object,FieldItem)");
            f.setViewConstructor("editor:java.lang.Byte", "web.PrimitiveFieldEditor(Object,FieldItem)");
            f.setViewConstructor("editor:java.lang.String", "web.PrimitiveFieldEditor(Object,FieldItem)");
            f.setViewConstructor("org.objectweb.jac.lib.Attachment", "web.AttachmentViewer(Object,Object,FieldItem)");
            f.setViewConstructor("editor:org.objectweb.jac.lib.Attachment", "web.AttachmentEditor");
            f.setViewConstructor("editor:java.io.Reader", "web.ReaderEditor");
            f.setViewConstructor("editor:java.io.InputStream", "web.InputStreamEditor");

            f.setViewConstructor("CollectionItemView", "web.CollectionItemView");
        }
    }

    // viewType -> constructor
    // views added in web.acc with setViewConstructor
    Hashtable constructors = new Hashtable();

    /**
     * Create a view of a given type.
     *
     * @param label the name of the view
     * @param viewType the type of the view (for instance, "Object" or "Tree".
     * @param params the parameters to pass to the view constructor
     * @return a view of the demanded type.
     */
    public View createView(String label, String viewType, Object[] params,
                           DisplayContext context) 
        throws UnhandledViewTypeException
    {
        logger.debug("createView(\""+label+"\","+viewType+","+
                  Arrays.asList(params)+")");
        AbstractMethodItem constructor = 
            (AbstractMethodItem)constructors.get(viewType);
        if (constructor==null) {
            throw new UnhandledViewTypeException(viewType);
        }
        logger.debug("  constructor = "+constructor.getLongName());
        Object[] parameters;
        Class[] paramTypes = constructor.getParameterTypes();
        if (paramTypes.length>0 && paramTypes[0]==ViewFactory.class) {
            logger.debug("  prepending factory to params");
            if (paramTypes.length>1 && paramTypes[1]==DisplayContext.class) {
                logger.debug("  prepending context to params");
                parameters = new Object[params.length+2];
                System.arraycopy(params,0,parameters,2,params.length);
                parameters[0] = this;
                parameters[1] = context;
            } else {
                parameters = new Object[params.length+1];
                System.arraycopy(params,0,parameters,1,params.length);
                parameters[0] = this;
            }
        } else {
            parameters = params;
        } 
        logger.debug("  invoking constructor..."+Arrays.asList(parameters));
        if (constructor.getParameterCount()!=parameters.length) {
            throw new RuntimeException("Wrong number of arguments for "+
                                       constructor.getLongName()+" ("+Arrays.asList(parameters)+")");
        }
        View result = (View)constructor.invoke(null,parameters);
        // We may have problems here if the view constructor is
        // blocking (as with dialogs for instance)
        result.setLabel(label);
        result.setFactory(this);
        result.setParameters(params);
        result.setType(viewType);
        result.setContext(context);
        logger.debug("  createView("+viewType+","+label+") -> "+result);
        return result;
    }

    /**
     * Convert Strings into the type expected by the view constructor.
     */
    public View createView(String label, String viewType, String[] params,
                           DisplayContext context) 
        throws UnhandledViewTypeException
    {
        logger.debug("createView(\""+label+"\","+viewType+","+
                  Arrays.asList(params)+")");
        AbstractMethodItem constructor = 
            (AbstractMethodItem)constructors.get(viewType);
        if (constructor==null) {
            throw new UnhandledViewTypeException(viewType);
        }
        logger.debug("  constructor = "+constructor);
        Class[] paramTypes = constructor.getParameterTypes();
        Object[] args = new Object[params.length];
        int offset = 0;
        if (paramTypes.length>0 && paramTypes[0]==ViewFactory.class)
            offset += 1;
        if (paramTypes.length>1 && paramTypes[1]==DisplayContext.class)
            offset += 1;

        for (int i=0; i<params.length;i++) {
            if (String.class.isAssignableFrom(paramTypes[i+offset])) {
                // String: leave as is
                args[i] = params[i];
            } else if (Boolean.class.isAssignableFrom(paramTypes[i+offset]) || 
                       boolean.class.isAssignableFrom(paramTypes[i+offset]) ) {
                args[i] = Boolean.valueOf(params[i]);
            } else if (Object.class.isAssignableFrom(paramTypes[i+offset])) {
                // Object: object name
                args[i] = NameRepository.get().getObject(params[i]);
                logger.debug(params[i]+" -> "+args[i]);
                if (args[i]==null) {
                    logger.warn("Could not find object named "+params[i]);
                }
            }
        }
        return createView(label,viewType,args,context);
    }

    /**
     * Creates a view for an object, using the most specific view
     * constructor available for the type of the object.
     *
     * @param label the label of the view to create
     * @param object the object for which to create a view
     * @param context the display context
     */
    public View createObjectView(String label, Object object,
                                 DisplayContext context) {
        if (object==null) {
            return createView(label,"Object",new Object[] {GuiAC.DEFAULT_VIEW,object}, 
                              context);
        }
        ClassItem cl = ClassRepository.get().getClass(object);
        while (cl!=null) {
            if (hasViewerFor(cl.getName())) {
                return createView(label,cl.getName(),
                                  new Object[] {object}, context);
            }
            cl = cl.getSuperclass();
        }
        return createView(label,"Object",new Object[] {"default",object}, context);
    }

    /**
     * Creates a view for an object, using the most specific view
     * constructor available for the type of the object.
     *
     * @param label the label of the view to create
     * @param object the object for which to create a view
     * @param substance
     * @param field 
     * @param context the display context
     */
    public View createObjectView(String label, Object object,
                                 Object substance, FieldItem field,
                                 DisplayContext context) {
        if (object==null) {
            return createView(label,"Object",new Object[] {GuiAC.DEFAULT_VIEW,object}, 
                              context);
        }
        ClassItem cl = ClassRepository.get().getClass(object);
        while (cl!=null) {
            if (hasViewerFor(cl.getName())) {
                return createView(label,cl.getName(),
                                  new Object[] {object,substance,field}, context);
            }
            cl = cl.getSuperclass();
        }
        return createView(label,"Object",new Object[] {"default",object}, context);
    }

    public CompositeView createCompositeView(String label, String viewType, 
                                             Object[] params, 
                                             DisplayContext context) 
        throws UnhandledViewTypeException
    {
        return (CompositeView)createView(label,viewType,params,context);
    }
   
    public View createView(String label, String viewType, DisplayContext context) 
        throws UnhandledViewTypeException
    {
        return createView(label,viewType,ExtArrays.emptyObjectArray,context);
    }

    public CompositeView createCompositeView(String label, String viewType, 
                                             DisplayContext context) 
        throws UnhandledViewTypeException
    {
        return (CompositeView)createView(label,viewType,context);
    }

    public FieldEditor createEditor(String label, String viewType, 
                                    Object[] params, 
                                    DisplayContext context)
        throws UnhandledViewTypeException
    {
        return (FieldEditor)createView(label,viewType,params,context);
    }
   
    /**
     * Set the constructor for a type of view
     *
     * @param viewType the type of the view
     * @param constructor the constructor to be used to build view of
     * that type. It should take a ViewFactory as the first parameter
     */
    public void setViewConstructor(String viewType, AbstractMethodItem constructor) {
        constructors.put(viewType,constructor);
    }

    public void setViewConstructor(String viewType, String constructor) 
    {
        try {
            setViewConstructor(
                viewType,
                (AbstractMethodItem)ACConfiguration.convertValue(
                    constructor,
                    AbstractMethodItem.class,
                    imports));
        } catch (Exception e) {
            logger.error("setViewConstructor("+viewType+","+constructor+") failed",e);
        }
    }

    /**
     * Tells wether a view factory is able to build a view for given type.
     * @param viewType the type of the view
     * @see #setViewConstructor(String,AbstractMethodItem)
     */
    public boolean hasViewerFor(String viewType) {
        return constructors.containsKey(viewType);
    }

    /**
     * Thrown to indicate that a ViewFactory is unable to build a view
     * of the given type because no constructor is associated to this
     * type of view.
     * @see #setViewConstructor(String,AbstractMethodItem)
     */
    public static class UnhandledViewTypeException extends RuntimeException {
        /**
         * @param viewType the type of view that the factory is unable to build.
         */
        public UnhandledViewTypeException(String viewType) {
            super("Unable to build a view of type \""+viewType+"\"");
        }
    }

}
