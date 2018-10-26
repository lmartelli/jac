/*
  Copyright (C) 2002-2004 Renaud Pawlak <renaud@aopsys.com>, 
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

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.objectweb.jac.aspects.session.SessionAC;
import org.objectweb.jac.core.AspectComponent;
import org.objectweb.jac.core.Collaboration;
import org.objectweb.jac.core.Display;
import org.objectweb.jac.core.Interaction;
import org.objectweb.jac.core.Jac;
import org.objectweb.jac.core.NameRepository;
import org.objectweb.jac.core.ObjectRepository;
import org.objectweb.jac.core.Wrappee;
import org.objectweb.jac.core.Wrapping;
import org.objectweb.jac.core.dist.Distd;
import org.objectweb.jac.core.dist.RemoteContainer;
import org.objectweb.jac.core.rtti.AbstractMethodItem;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.core.rtti.CollectionItem;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.MemberItem;
import org.objectweb.jac.core.rtti.MetaItem;
import org.objectweb.jac.core.rtti.MethodItem;
import org.objectweb.jac.core.rtti.NamingConventions;
import org.objectweb.jac.core.rtti.NoSuchMethodException;
import org.objectweb.jac.core.rtti.RttiAC;
import org.objectweb.jac.core.rtti.VirtualClassItem;
import org.objectweb.jac.util.Enum;
import org.objectweb.jac.util.ExtArrays;
import org.objectweb.jac.util.ExtBoolean;
import org.objectweb.jac.util.InvalidIndexException;
import org.objectweb.jac.util.Stack;

/**
 * This aspect component implements a very simple GUI that allows the
 * user to browse the named object of the system and call methods on
 * them.
 *
 * <p>It implements a MVC design pattern where the controllers are
 * implemented by <code>ViewWrapper</code> instances.<p>
 *
 * @see ViewControlWrapper
 * @see InputWrapper
 * @see View
 *
 * @author <a href="mailto:renaud@cnam.fr">Renaud Pawlak</a>
 * @author <a href="mailto:laurent@aopsys.com">Laurent Martelli</a>
 */

public class GuiAC extends AspectComponent implements GuiConf {
    static Logger logger = Logger.getLogger("gui");
    static Logger loggerContext = Logger.getLogger("gui.context");
    static Logger loggerDisplay = Logger.getLogger("gui.display");
    static Logger loggerApp = Logger.getLogger("application");
    static Logger loggerWuni = Logger.getLogger("gui.wuni");
    static Logger loggerTable = Logger.getLogger("gui.table");
    static Logger loggerMenu = Logger.getLogger("gui.menu");
    static Logger loggerFactory = Logger.getLogger("gui.factory");

    /** Name of the default generic object view */
    public static final String DEFAULT_VIEW = "default";

    /** Name of the autocreated object view */
    public static final String AUTOCREATE_VIEW = "autocreate";

    /** Stores the displays of the GUI (displayID -> display). */
    protected static Hashtable displays = new Hashtable();

    /** Stores the cusomized GUI (class -> display). */
    protected static Hashtable cguis = new Hashtable();

    // RTTI attributes
    public static final String VIEWS = "GuiAC.VIEWS"; // Map String(view name) -> ObjectView
    public static final String DEFAULTS_ATTRIBUTES_ORDER = "GuiAC.DEFAULTS_ATTRIBUTES_ORDER";
    public static final String TREE_ATTRIBUTES_ORDER = "GuiAC.TREE_ATTRIBUTES_ORDER";
    public static final String METHODS_ORDER = "GuiAC.METHODS_ORDER";
    public static final String INTERACTION_HANDLER = "GuiAC.INTERACTION_HANDLER";
    public static final String MIME_TYPE  = "GuiAC.MIME_TYPE";    

    
    public static final String AUTO_CREATE = "GuiAC.AUTO_CREATE"; // Boolean
    
    public static final String AUTO_CREATE_INITIALIZER = "GuiAC.AUTO_CREATE_INITIALIZER"; // MethodItem
    public static final String NO_AUTO_CREATE = "GuiAC.NO_AUTO_CREATE";
    public static final String SORT_CRITERIA = "GuiAC.SORT_CRITERIA";
    public static final String TABLE_VIEW = "GuiAC.TABLE_VIEW";
    public static final String CHOICE_VIEW = "GuiAC.CHOICE_VIEW";
    public static final String SHOW_ROW_NUMBERS = "GuiAC.SHOW_ROW_NUMBERS"; // Boolean
    public static final String EDITABLE_DEFAULT_VALUES = "GuiAC.EDITABLE_DEFAULT_VALUES";
    public static final String FILTERED_COLUMNS = "GuiAC.FILTERED_COLUMNS"; // Collection of FieldItem
    public static final String TO_STRING = "GuiAC.TO_STRING";
    public static final String CONTEXTUAL_TO_STRING = "GuiAC.CONTEXTUAL_TO_STRING";
    public static final String TOOLTIP = "GuiAC.TOOLTIP";
    public static final String CONTEXTUAL_TOOLTIP = "GuiAC.CONTEXTUAL_TOOLTIP";
    //   public static final String EMBEDDED_VIEW = "GuiAC.EMBEDDED_VIEW";
    public static final String EMBEDDED_EDITOR = "GuiAC.EMBEDDED_EDITOR";
    // Boolean
    public static final String VISIBLE = "GuiAC.VISIBLE"; // Boolean
    public static final String EDITABLE = "GuiAC.EDITABLE"; // Boolean
    public static final String ADDABLE = "GuiAC.ADDABLE"; // Boolean
    public static final String REMOVABLE = "GuiAC.REMOVABLE"; // Boolean
    public static final String CREATABLE = "GuiAC.CREATABLE"; // Boolean

    public static final String CATEGORIES = "GuiAC.CATEGORIES";
    public static final String CATEGORIES_ICONS = "GuiAC.CATEGORIES_ICONS";
    public static final String CATEGORIES_LABELS = "GuiAC.CATEGORIES_LABELS";
    public static final String FIELD_CHOICE = "GuiAC.FIELD_CHOICE";
    public static final String FIELD_ENUM = "GuiAC.FIELD_ENUM";
    public static final String PARAMETER_ENUMS = "GuiAC.PARAMETER_ENUMS";
    public static final String EDITABLE_CHOICE = "GuiAC.EDITABLE_CHOICE";
    public static final String EDITABLE_CHOICES = "GuiAC.EDITABLE_CHOICES";
    public static final String PARAMETER_CHOICES = "GuiAC.PARAMETER_CHOICES";
    public static final String CLASS_CHOICES = "GuiAC.CLASS_CHOICES";
    public static final String COLLECTION_TYPE = "GuiAC.COLLECTION_TYPE";
    public static final String VIEW_ON_SELECTION = "GuiAC.VIEW_ON_SELECTION";
    public static final String SELECTION_HANDLER = "GuiAC.SELECTION_HANDLER";
    public static final String SELECTION_TARGET = "GuiAC.SELECTION_TARGET";
    public static final String PARAMETER_NAMES = "GuiAC.PARAMETER_NAMES";
    public static final String LINKED_PARAMETERS = "GuiAC.LINKED_PARAMETERS";
    public static final String PASSWORD_PARAMETERS = "GuiAC.PASSWORD_PARAMETERS";
    public static final String CREATION_ALLOWED_PARAMETERS = "GuiAC.CREATION_ALLOWED_PARAMETERS";
    public static final String DEFAULT_VALUES = "GuiAC.DEFAULT_VALUES";
    public static final String DYN_DEFAULT_VALUE = "GuiAC.DYN_DEFAULT_VALUE";
    public static final String PARAMETER_FIELDS = "GuiAC.PARAMETER_FIELDS";
    public static final String CONDITION = "GuiAC.CONDITION";
    public static final String SLOW_OPERATION = "GuiAC.SLOW_OPERATION"; // Boolean
    public static final String POST_INVOKE_HOOKS= "GuiAC.POST_INVOKE_HOOKS"; // List of AbstractMethodItems
    public static final String VIEW_HEIGHT = "GuiAC.VIEW_HEIGHT"; // Length
    public static final String VIEW_WIDTH = "GuiAC.VIEW_WIDTH"; // Length
    public static final String EDITOR_HEIGHT = "GuiAC.EDITOR_HEIGHT"; // Length
    public static final String EDITOR_WIDTH = "GuiAC.EDITOR_WIDTH"; // Length
    public static final String EDITOR_SMALL_WIDTH = "GuiAC.EDITOR_SMALL_WIDTH"; // Length
    public static final String EDITOR_SMALL_HEIGHT = "GuiAC.EDITOR_SMALL_HEIGHT"; // Length
    public static final String HIDDEN_TREE_RELATION = "GuiAC.HIDDEN_TREE_RELATION";
    public static final String BORDER = "GuiAC.BORDER";
    public static final String DESCRIPTION = "GuiAC.DESCRIPTION";
    public static final String ICON = "GuiAC.ICON";
    public static final String DYNAMIC_ICON = "GuiAC.DYNAMIC_ICON";
    public static final String MENU = "GuiAC.MENU";
    public static final String DISPLAY_LABEL = "GuiAC.DISPLAY_LABEL"; // boolean
    public static final String LABEL = "GuiAC.LABEL"; // String
    public static final String MNEMONICS = "GuiAC.MNEMONICS"; // String
    public static final String CONTEXTUAL_LABEL = "GuiAC.CONTEXTUAL_LABEL";
    public static final String STYLE = "GuiAC.STYLE"; // String
    public static final String NEW_WINDOW = "GuiAC.NEW_WINDOW";
    public static final String NUM_ROWS_PER_PAGE = "GuiAC.NUM_ROWS_PER_PAGE"; // int
    public static final String AVAILABLE_NUM_ROWS_PER_PAGE = "GuiAC.AVAILABLE_NUM_ROWS_PER_PAGE"; // int[]
    public static final String AUTO_CREATED_STATE = "GuiAC.AUTO_CREATED_STATE";
    public static final String SMALL_TARGET_CONTAINER = "GuiAC.SMALL_TARGET_CONTAINER";
    public static final String NAVBAR = "GuiAC.NAVBAR";
    public static final String FIELD_DEPENDENCIES = "GuiAC.FIELD_DEPENDENCIES";
    public static final String LINE_BREAKS = "GuiAC.LINE_BREAKS";
    public static final String DIRECT_COLLECTION_METHODS = "GuiAC.DIRECT_COLLECTION_METHODS";
    public static final String DATE_FORMAT = "GuiAC.DATE_FORMAT";
    public static final String DATEHOUR_FORMAT = "GuiAC.DATEHOUR_FORMAT";
    public static final String FLOAT_FORMAT = "GuiAC.FLOATFORMAT";
    public static final String INT_FORMAT = "GuiAC.INT_FORMAT";
    public static final String FORMAT = "GuiAC.FORMAT";

    public static final String ASKING_SEQUENCE = "GuiAC.ASKING_SEQUENCE";

    public static final String DESKTOP_VIEW = "GuiAC.DESKTOP_VIEW"; //ClassItem
    public static final String FILE_SELECTION_MODE =
    "GuiAC.FILE_SELECTION_MODE";
    // String
    public static final String FILE_EXTENSIONS = "GuiAC.FILE_EXTENSIONS";
    // String[]
    public static final String FILE_EXTENSIONS_DESCRIPTION =
    "GuiAC.FILE_EXTENSIONS_DESCRIPTION";
    // String
    public static final String FILE_CHOOSER_VIEW = "GuiAC.FILE_CHOOSER_VIEW";
    public static final String FILE_EDITOR_CONFIG = "GuiAC.FILE_EDITOR_CONFIG";

    public static final String INDEXED_FIELD_SELECTOR =
    "GuiAC.INDEXED_FIELD_SELECTOR";
    // CollectionItem
    public static final String INDEX_NOT_FOUND_HANDLER =
    "RttiAC.INDEX_NOT_FOUND_HANDLER";
    // MethodItem

    public static final String REPOSITORY_NAME = "GuiAC.REPOSITORY_NAME";

    // Context attributes

    public static final String DISPLAY_CONTEXT = "GuiAC.DISPLAY";
    /** A "concrete" method we should ask parameters for */
    public static final String ASK_FOR_PARAMETERS = "GuiAC.ASK_FOR_PARAMETERS";
    /** The "not concrete" method we should ask parameters for */
    public static final String INVOKED_METHOD = "GuiAC.INVOKED_METHOD";
    public static final String AUTO_CREATION = "GuiAC.AUTO_CREATION";
    public static final String AUTOCREATE_REASON = "GuiAC.AUTOCREATE_REASON";
    // Boolean
    /** Boolean value to force the use of embedded editors. Defaults to false. */
    //public static final String EMBEDDED_EDITORS = "GuiAC.EMBEDDED_EDITORS";
    // Boolean
    public static final String SMALL_VIEW = "GuiAC.SMALL_VIEW"; // Boolean
    public static final String OPPOSITE_ROLE = "GuiAC.OPPOSITE_ROLE";
    // FieldItem
    public static final String VIEW = "GuiAC.VIEW";
    public static final String OPEN_VIEW = "GuiAC.OPEN_VIEW"; // Boolean
    public static final String GRAPHIC_CONTEXT = "GuiAC.GRAPHIC_CONTEXT";

    /** Start index in a collection view Map (CollectionItem -> Integer) */
    public static final String START_INDEXES = "GuiAC.START_INDEXES";

    public static final int THUMB_MAX_WIDTH = 100;
    public static final int THUMB_MAX_HEIGHT = 50;
    public static final int THUMB_QUALITY = 70;

    // Map: CollectionItem -> column index
    public static final String SORT_COLUMN = "GuiAC.SORT_COLUMN"; // [+-]?<fieldname>
    public static final String DEF_SORT = "GuiAC.DEF_SORT";

    // Map: CollectionItem -> ColumnFilter
    public static final String TABLE_FILTER = "GuiAC.TABLE_FILTER";
    

    /** Disable commit in editors */
    //public static final String NO_COMMIT = "GuiAC.NO_COMMIT";

    /** Substance on which the method is invoked. Set when auto creating parameters */
    public static final String SUBSTANCE = "GuiAC.SUBSTANCE"; // Wrappee

    /** TreeNode being removed, to optimize Tree refresh */
    public static final String REMOVED_NODE = "GuiAC.REMOVED_NODE";

    static String parseFormatExpression(
        String formatExpr,
        Object o,
        String beforeString,
        String afterString) 
    {
        final String delim = "%";
        StringBuffer result = new StringBuffer();
        String subString = null;
        ClassItem cl = ClassRepository.get().getClass(o);
        StringTokenizer st = new StringTokenizer(formatExpr, delim, true);
        while (st.hasMoreTokens()) {
            boolean firstDelim = false;
            String cur = st.nextToken();
            // normal string
            if (!cur.equals(delim)) {
                result.append(cur);
                continue;
            }
            // delimited expression
            if (cur.equals(delim)) {
                cur = st.nextToken();
                                // double delim
                if (cur.equals(delim)) {
                    result.append(delim);
                    continue;
                }
                if (cur.indexOf("()") == -1) {
                    // field name
                    try {
                        FieldItem field = cl.getField(cur);
                        Object value = field.getThroughAccessor(o);
                        if (beforeString != null) {
                            result.append(beforeString);
                        }
                        if (field != null)
                            result.append(GuiAC.toString(field, value));
                        else
                            result.append("<bad field: " + cur + ">");
                        if (afterString != null) {
                            result.append(afterString);
                        }
                    } catch (Exception e) {
                        // bad field description
                        result.append("<bad field: " + cur + ">");
                    }
                } else {
                    // method with no args
                    try {
                        Object value =
                            ClassRepository.invokeDirect(
                                cl.getActualClass(),
                                cur.substring(0, cur.length() - 2),
                                o,
                                ExtArrays.emptyObjectArray);
                        ;
                        if (beforeString != null) {
                            result.append(beforeString);
                        }
                        result.append(GuiAC.toString(value));
                        if (afterString != null) {
                            result.append(afterString);
                        }
                    } catch (Exception e) {
                        // bad method description
                        result.append(
                            "<bad method: "
                            + cur.substring(0, cur.length() - 2)
                            + ">");
                    }
                }
                                // read closing delim
                cur = st.nextToken();
            }
        }
        return result.toString();
    }

    public static String toString(Object o) {
        return toString(o, null);
    }

    public static String toString(float value) {
        return toString(new Float(value), null);
    }

    public static String toString(double value) {
        return toString(new Double(value), null);
    }

    /**
     * Gets the string representation of an object.
     * @param o the object
     * @return a string representing the object o
     * @see #setToString(ClassItem,String)
     */
    public static String toString(Object o, Stack context) {
        if (o == null)
            return "null";
        ClassItem cl = ClassRepository.get().getClass(o);

        String formatExpr = (String) cl.getAttribute(TO_STRING);
        if (context != null) {
            formatExpr =
                (String) getContextAttribute(cl,
                                             CONTEXTUAL_TO_STRING,
                                             context,
                                             formatExpr);
        }
        if (formatExpr == null) {
            if (o instanceof java.awt.Point) {
                java.awt.Point p = (java.awt.Point) o;
                return "(" + p.x + "," + p.y + ")";
            } else if (o instanceof java.awt.Dimension) {
                java.awt.Dimension d = (java.awt.Dimension) o;
                return d.width + "x" + d.height;
            } else if (o instanceof Date) {
                return new SimpleDateFormat(getDateFormat()).format((Date) o);
            } else if (o instanceof Collection) {
                String string = "";
                Iterator it = ((Collection) o).iterator();
                while (it.hasNext()) {
                    string += toString(it.next(), context);
                    if (it.hasNext())
                        string += ", ";
                }
                return string;
            } else if (o instanceof Wrappee) {
                return NameRepository.get().getName(o);
            } else {
                return o.toString();
            }
        } else {
            return parseFormatExpression(formatExpr, o, null, null);
        }
    }

    /**
     * Gets the tooltip text of an object.
     * @param context a stack of meta items representing the context
     * @param o the object
     * @return the tooltip of the object in the given context
     * @see #setToolTipText(ClassItem,String)
     */
    public static String getToolTip(Object o, Stack context) {
        if (o == null)
            return null;
        ClassItem cl = ClassRepository.get().getClass(o);

        String formatExpr = (String) cl.getAttribute(TOOLTIP);
        if (context != null) {
            formatExpr =
                (String) getContextAttribute(cl,
                                             CONTEXTUAL_TOOLTIP,
                                             context,
                                             formatExpr);
        }
        if (formatExpr == null) {
            return null;
        } else {
            return parseFormatExpression(formatExpr, o, null, null);
        }
    }

    /**
     * @param item item holding the rules
     * @param attribute name of attribute holding the rules
     * @param defaultValue this value is returned if there is not matching context
     */
    public static Object getContextAttribute(
        MetaItem item,
        String attribute,
        Stack context,
        Object defaultValue) 
    {
        loggerContext.debug("getContextAttribute " + item + "," + attribute);
        Map rules = (Map) item.getAttribute(attribute);
        if (rules == null) {
            loggerContext.debug("No attribute " + attribute);
            return defaultValue;
        } else {
            if (context != null) {
                loggerContext.debug("rules = " + rules);
                for (int i = 0; i < context.size(); i++) {
                    MetaItem elt = (MetaItem) context.peek(i);
                    Iterator it = rules.keySet().iterator();
                    while (it.hasNext()) {
                        MetaItem selector = (MetaItem) it.next();
                        if (selector instanceof ClassItem) {
                            ClassItem cl = (ClassItem) selector;
                            if ((elt instanceof ClassItem
                                 && ((ClassItem) elt).isSubClassOf(cl))
                                || (elt instanceof MemberItem
                                    && ((MemberItem) elt)
                                    .getClassItem()
                                    .isSubClassOf(cl))) {
                                loggerContext.debug(elt + " matches " + selector
                                    + " -> " + rules.get(selector));
                                return rules.get(selector);
                            }
                        } else if (selector instanceof MemberItem) {
                            MemberItem member = (MemberItem) selector;
                            if (elt instanceof MemberItem) {
                                if (((MemberItem) elt)
                                    .getClassItem()
                                    .isSubClassOf(member.getClassItem())
                                    && member.getName().equals(elt.getName())) {
                                    loggerContext.debug(elt + " matches " + selector
                                        + " -> " + rules.get(selector));
                                    return rules.get(selector);
                                }
                            }
                        }
                    }
                }
                return defaultValue;
            } else {
                loggerContext.debug("No graphic context");
                return defaultValue;
            }
        }
    }

    public static void pushGraphicContext(MetaItem value) {
        Collaboration collab = Collaboration.get();
        Stack gc = (Stack) collab.getAttribute(GRAPHIC_CONTEXT);
        if (gc == null) {
            gc = new Stack();
            collab.addAttribute(GRAPHIC_CONTEXT, gc);
        }
        loggerContext.debug("push " + value);
        gc.push(value);
    }

    public static MetaItem popGraphicContext() {
        Stack gc = (Stack) Collaboration.get().getAttribute(GRAPHIC_CONTEXT);
        if (gc == null) {
            loggerContext.error(
                "No GRAPHIC_CONTEXT in current context, cannot popGraphicContext");
            return null;
        } else {
            loggerContext.debug("pop " + gc.peek());
            return (MetaItem) gc.pop();
        }
    }

    public static Stack getGraphicContext() {
        return (Stack) Collaboration.get().getAttribute(GRAPHIC_CONTEXT);
    }

    /**
     * Gets the string representation of the field of an object.
     * @param field the field
     * @param value the value of the field
     * @return a string representing the value of the field
     */
    public static String toString(FieldItem field, Object value) {
        if (value == null) {
            return "null";
        }
        Enum enum = getEnum(field);
        if (enum != null) {
            try {
                return value != null
                    ? enum.int2string(((Integer) value).intValue())
                    : "";
            } catch (InvalidIndexException e) {
                return "";
            }
        } else if (value instanceof Date) {
            return new SimpleDateFormat(getDateFormat()).format((Date) value);
        } else {
            return toString(value);
        }
    }

    /**
     * Gets the string representation of the field of an object.
     * @param field the field
     * @param value the value of the field
     * @return a string representing the value of the field
     */
    public static String toString(FieldItem field, int value) {
        return toString(field, new Integer(value));
    }

    /**
     * Gets a display from its ID.
     *
     * @param displayID the id of the display
     * @see Display  */

    public static final Display getDisplay(String displayID) {
        return (Display) displays.get(displayID);
    }

    /**
     * Gets a display from its ID.
     *
     * @param customizedID the id of the customized gui
     * @see Display  
     */
    public static final CustomizedGUI getCustomized(String customizedID) {
        return (CustomizedGUI) cguis.get(customizedID);
    }

    /**
     * Creates a new display and registers it.
     *
     * @param displayID the display name
     * @param displayType the display class to instantiate. It must
     * have a constructor taking a ViewFactory parameter
     * @param factory the view factory for the display
     */
    public static Display createDisplay(String displayID,
                                        Class displayType,
                                        ViewFactory factory)
        throws Exception
    {
        Display ret = null;
        loggerDisplay.debug("createDisplay(" + displayType.getName() + "," + displayType + ")");
        if ((ret = getDisplay(displayID)) != null) {
            loggerDisplay.warn("Display " + displayID + " is already registered");
            return ret;
        }
        try {
            ret =
                (Display) displayType
                .getDeclaredConstructor(new Class[] { ViewFactory.class })
                .newInstance(new Object[] { factory });

            ret.setDisplayID(displayID);
            addDisplay(displayID, ret);
        } catch (Exception e) {
            logger.error(
                "createDisplay("+displayID+","+
                displayType.getName()+","+factory+
                ") failed to instantiate display: "+e);
            throw e;
        }
        return ret;
    }

    public void createSwingDisplays(String[] customizedIDs) {
        loggerDisplay.debug("createSwingDisplays(" + Arrays.asList(customizedIDs) + ")");
        for (int i=0; i<customizedIDs.length; i++) {
            try {
                if (customizedIDs[i].equals("admin")) {
                // createDisplay("admin",org.objectweb.jac.aspects.gui.swing.ProgramView.class,null);
                } else {
                    showCustomizedSwing(customizedIDs[i], customizedIDs[i]);
                }
            } catch(Exception e) {
                logger.error("Failed to create swing display \""+customizedIDs[i]+'"',e);
            }
        }
    }

    public void createWebDisplays(String[] customizedIDs) {
        int port = 8088;
        try {
            logger.info(
                "createWebDisplays "+Arrays.asList(customizedIDs)+
                " for application " + application);
            org.objectweb.jac.aspects.gui.web.WebDisplay.startWebServer(
                application,
                customizedIDs,
                port);
        } catch (Exception e) {
            loggerDisplay.error("Failed to start webgui : " + e);
            loggerDisplay.error("Check that another server is not running on port " + port);
        }
    }

    /**
     * Adds a new display for this GUI.
     *
     * @param newDisplay the display to add
     * @see Display 
     */
    public static void addDisplay(String displayID, Display newDisplay) {
        loggerDisplay.debug("addDisplay(" + newDisplay.getClass().getName() + ")");
        displays.put(displayID, newDisplay);
    }

    /**
     * Removes a display for this GUI.
     *
     * @param display the display to remove
     * @see Display */

    public static void removeDisplay(Display display) {
        if (display.getDisplayID() != null)
            displays.remove(display.getDisplayID());
    }

    /**
     * This configuration method allows the user to register a new
     * display.
     * 
     * <p>It instantiates a new display from its class name and
     * registers its.
     * 
     * @param name the display name
     * @param displayType the display type 
     * @param guiType the type of the gui (
     */
    public static void registerDisplay(String name,
                                       String displayType,
                                       String guiType) 
        throws Exception
    {
        if (getDisplay(name) != null) {
            loggerDisplay.warn("display " + name + " is already launched");
            return;
        }
        createDisplay(
            name,
            Class.forName(displayType),
            getViewFactory(guiType));
    }

    public void registerCustomized(String name) {
        try {
            if (getCustomized(name) != null) {
                warning("customized GUI " + name + " is already registered");
                return;
            }
            CustomizedGUI cgui = new CustomizedGUI(name);
            // the customized gui knows the application it belongs to
            loggerApp.debug("customized gui " + name
                            + " belongs to " + Collaboration.get().getCurApp());
            cgui.setApplication(Collaboration.get().getCurApp());
            cguis.put(name, cgui);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showCustomizedSwing(String displayName,
                                    String gui,
                                    String host) 
        throws Exception
    {
        CustomizedGUI cgui = (CustomizedGUI) getCustomized(gui);
        if (cgui == null) {
            error(gui+" has not been declared with registerCustomized. "+
                  "Available customized GUI: " + cguis.keySet());
            return;
        }
        if (host == null
            || RemoteContainer.resolve(host) == null
            || RemoteContainer.resolve(host).getName().equals(
                Distd.getLocalContainerName())) 
        {
            registerDisplay(
                displayName,
                "org.objectweb.jac.aspects.gui.swing.SwingDisplay",
                "swing");

            Display swingGUI = getDisplay(displayName);
            Collaboration.get().addAttribute(
                DISPLAY_CONTEXT,
                new DisplayContext((CustomizedDisplay) swingGUI, null));
            Collaboration.get().addAttribute(
                SessionAC.SESSION_ID,
                GuiAC.getLocalSessionID());
            swingGUI.showCustomized(gui, cgui);
            //swingGUI.applicationStarted();
        } else {
            displays.remove(gui);
        }
        //      ((ACManager)ACManager.get()).afterApplicationStarted();
    }

    public void showCustomizedSwing(String displayName, String gui) 
        throws Exception
    {
        showCustomizedSwing(displayName, gui, null);
    }

    public void setWelcomeMessage(
        String gui,
        String title,
        String message,
        String icon) 
    {
        CustomizedGUI cgui = (CustomizedGUI) getCustomized(gui);
        if (cgui == null) {
            error(gui+" has not been declared with registerCustomized");
            return;
        }
        cgui.setWelcomeMessage(title, message, icon);
    }

    public void setStyle(FieldItem field, String style) {
        if (style != null) {
            field.setAttribute(STYLE, style);
        }
    }

    public void setStyle(ClassItem cli, String style) {
        if (style != null) {
            cli.setAttribute(STYLE, style);
        }
    }

    public void setOnCloseHandler(
        String gui,
        AbstractMethodItem eventHandler) 
    {
        CustomizedGUI cgui = (CustomizedGUI) getCustomized(gui);
        if (cgui == null) {
            error(gui+" has not been declared with registerCustomized");
            return;
        }
        cgui.setOnCloseHandler(eventHandler);
    }

    public void addStatusBar(String gui, MethodItem method, String position) {
        CustomizedGUI cgui = (CustomizedGUI) getCustomized(gui);
        if (cgui == null) {
            error(gui+" has not been declared with registerCustomized");
            return;
        }
        cgui.addStatusBar(method, position);
    }

    public void addStatusBar(String gui, String position) {
        CustomizedGUI cgui = (CustomizedGUI) getCustomized(gui);
        if (cgui == null) {
            error(gui+" has not been declared with registerCustomized");
            return;
        }
        cgui.addStatusBar(null, position);
    }

    public void setSubPanesGeometry(
        String gui,
        int subPanesCount,
        String geometry) 
    {
        boolean[] scrollings = new boolean[subPanesCount];
        Arrays.fill(scrollings, true);
        setSubPanesGeometry(gui, subPanesCount, geometry, scrollings);
    }

    public void setSubPanesGeometry(
        String gui,
        int subPanesCount,
        String geometry,
        boolean[] scrollings) 
    {
        if (subPanesCount != scrollings.length) {
            throw new RuntimeException("setSubPanesGeometry: subPanesCount!=scrollings.length");
        }
        CustomizedGUI cgui = (CustomizedGUI) getCustomized(gui);
        if (cgui == null) {
            logger.error(gui+" has not been declared with registerCustomized");
            return;
        }
        int geom = 0;
        if (geometry.compareToIgnoreCase("VERTICAL")==0)
            geom = CustomizedGUI.VERTICAL;
        else if (geometry.compareToIgnoreCase("VERTICAL_LEFT")==0)
            geom = CustomizedGUI.VERTICAL_LEFT;
        else if (geometry.compareToIgnoreCase("VERTICAL_RIGHT")==0)
            geom = CustomizedGUI.VERTICAL_RIGHT;
        else if (geometry.compareToIgnoreCase("HORIZONTAL")==0)
            geom = CustomizedGUI.HORIZONTAL;
        else if (geometry.compareToIgnoreCase("HORIZONTAL_UP")==0)
            geom = CustomizedGUI.HORIZONTAL_UP;
        else if (geometry.compareToIgnoreCase("HORIZONTAL_DOWN")==0)
            geom = CustomizedGUI.HORIZONTAL_DOWN;
        else if (geometry.compareToIgnoreCase("NONE")==0)
            geom = CustomizedGUI.HORIZONTAL;
        else
            warning("Unknown geometry '"+geometry+"'");
        cgui.setSubPanesGeometry(subPanesCount, geom, scrollings);
    }

    public void setPaneContent(
        String gui,
        String paneId,
        String type,
        String[] args) 
    {
        CustomizedGUI cgui = (CustomizedGUI) getCustomized(gui);
        if (cgui == null) {
            error(gui+" has not been declared with registerCustomized");
            return;
        }
        cgui.setPaneContent(paneId, type, args);
    }

    public void setPaneContainer(String gui, String paneId, String type) {
        CustomizedGUI cgui = (CustomizedGUI) getCustomized(gui);
        if (cgui == null) {
            error(gui+" has not been declared with registerCustomized");
            return;
        }
        cgui.setPaneContainer(paneId, type);
    }

    public void setInvalidPane(
        String gui,
        String changedPane,
        String invalidPane) 
    {
        CustomizedGUI cgui = (CustomizedGUI) getCustomized(gui);
        if (cgui == null) {
            error(gui+" has not been declared with registerCustomized");
            return;
        }
        cgui.setInvalidPane(changedPane, invalidPane);
    }

    public void addReferenceToPane(
        String gui,
        MemberItem field,
        String panePath) 
    {
        addReferenceToPane(
            gui,
            field,
            "Object",
            new String[] { DEFAULT_VIEW },
            Boolean.FALSE,
            panePath);
    }

    public void addReferenceToPane(
        String gui,
        MemberItem member,
        String viewType,
        String[] viewParams,
        Boolean small,
        String paneId) 
    {
        CustomizedGUI cgui = (CustomizedGUI) getCustomized(gui);
        if (cgui == null) {
            error(gui+" has not been declared with registerCustomized");
            return;
        }
        member.setAttribute(SMALL_TARGET_CONTAINER, small);
        cgui.addReferenceToPane(member, viewType, viewParams, paneId);
    }

    public void newWindow(String gui, String className, String fieldName) {

        CustomizedGUI cgui = (CustomizedGUI) getCustomized(gui);
        if (cgui == null) {
            error(gui+" has not been declared with registerCustomized");
            return;
        }
        ClassItem cli = ClassRepository.get().getClass(className);
        FieldItem field = cli.getField(fieldName);
        field.setAttribute(NEW_WINDOW, gui);
    }

    public void setCustomizedIcon(String gui, String icon) {
        CustomizedGUI cgui = (CustomizedGUI) getCustomized(gui);
        if (cgui == null) {
            error(gui+" has not been declared with registerCustomized");
            return;
        }
        cgui.setIcon(icon);
    }

    public void setPosition(
        String gui,
        int left,
        int up,
        int width,
        int height) 
    {
        CustomizedGUI cgui = (CustomizedGUI) getCustomized(gui);
        if (cgui == null) {
            error(gui+" has not been declared with registerCustomized");
            return;
        }
        cgui.setPosition(left, up, width, height);
    }

    public void addMenuItem(
        String gui,
        String menu,
        String[] menuPath,
        AbstractMethodItem callback) 
    {
        addMenuItem(gui, menu, menuPath, null, callback, null);
    }

    public void addMenuItem(
        String gui,
        String menu,
        String[] menuPath,
        String objectName,
        AbstractMethodItem callback) 
    {
        addMenuItem(gui, menu, menuPath, objectName, callback, null);
    }

    public void addMenuItem(
        String gui,
        String menu,
        String[] menuPath,
        AbstractMethodItem callback,
        String[] parameters) 
    {
        addMenuItem(gui, menu, menuPath, null, callback, parameters);
    }

    public void addMenuItem(
        String gui,
        String menu,
        String[] menuPath,
        String objectName,
        AbstractMethodItem callback,
        String[] parameters) 
    {
        CustomizedGUI cgui = (CustomizedGUI) getCustomized(gui);
        if (cgui == null) {
            error(gui+" has not been declared with registerCustomized");
            return;
        }
        cgui.addMenuItem(
            menu,
            menuPath,
            new Callback(objectName, callback, parameters));
    }

    public void addMenuSeparator(String gui, String menu, String[] menuPath) {
        CustomizedGUI cgui = (CustomizedGUI) getCustomized(gui);
        if (cgui == null) {
            error(gui+" has not been declared with registerCustomized");
            return;
        }
        cgui.addMenuSeparator(menu, menuPath);
    }

    public void setMenuPosition(String gui, String menu, String position) {
        CustomizedGUI cgui = (CustomizedGUI) getCustomized(gui);
        if (cgui == null) {
            error(gui+" has not been declared with registerCustomized");
            return;
        }
        cgui.setMenuPosition(menu, position);
    }

    public void setMenuIcon(
        String gui,
        String menu,
        String[] menuPath,
        String icon) 
    {
        CustomizedGUI cgui = (CustomizedGUI) getCustomized(gui);
        if (cgui == null) {
            error(gui+" has not been declared with registerCustomized");
            return;
        }
        cgui.setMenuIcon(menu, menuPath, ResourceManager.getResource(icon));
    }

    public void addToolbarAction(String gui, AbstractMethodItem method) {
        CustomizedGUI cgui = (CustomizedGUI) getCustomized(gui);
        if (cgui == null) {
            error(gui+" has not been declared with registerCustomized");
            return;
        }
        cgui.addToolbarAction(method);
    }

    public void addToolbarAction(
        String gui,
        String objectName,
        AbstractMethodItem method) 
    {
        CustomizedGUI cgui = (CustomizedGUI) getCustomized(gui);
        if (cgui == null) {
            error(gui+" has not been declared with registerCustomized");
            return;
        }
        cgui.addToolbarAction(objectName, method);
    }

    public void addToolbarAction(
        String gui,
        AbstractMethodItem method,
        String[] params) 
    {
        CustomizedGUI cgui = (CustomizedGUI) getCustomized(gui);
        if (cgui == null) {
            error(gui+" has not been declared with registerCustomized");
            return;
        }
        cgui.addToolbarAction(null,method,params);
    }

    public void addToolbarSeparator(String gui) {
        CustomizedGUI cgui = (CustomizedGUI) getCustomized(gui);
        if (cgui == null) {
            error(gui+" has not been declared with registerCustomized");
            return;
        }
        cgui.addToolbarSeparator();
    }

    public void setSplitterLocation(
        String gui,
        int splitterId,
        float location)
    {
        CustomizedGUI cgui = (CustomizedGUI) getCustomized(gui);
        if (cgui == null) {
            error(gui+" has not been declared with registerCustomized");
            return;
        }
        cgui.setSplitterLocation(splitterId, location);
    }

    public void setTitle(String gui, String title) {
        CustomizedGUI cgui = (CustomizedGUI) getCustomized(gui);
        if (cgui == null) {
            error(gui+" has not been declared with registerCustomized");
            return;
        }
        cgui.setTitle(title);
    }

    public void addStyleSheetURL(String gui, String url) {
        CustomizedGUI cgui = (CustomizedGUI) getCustomized(gui);
        if (cgui == null) {
            error(gui+" has not been declared with registerCustomized");
            return;
        }
        cgui.addStyleSheetURL(url);
    }

    static Vector cssURLs = new Vector();
    public void addStyleSheetURL(String url) {
        cssURLs.add(url);
    }
    static public Vector getStyleSheetURLs() {
        return cssURLs;
    }

    public void setLoggingMethod(
        String gui,
        String objects,
        String classes,
        String methods,
        int paneID) 
    {
        CustomizedGUI cgui = (CustomizedGUI) getCustomized(gui);
        if (cgui == null) {
            error(gui+" has not been declared with registerCustomized");
            return;
        }
        /*
          JPanel pane = cgui.getPane(paneID);
          JTextArea textArea = new JTextArea();
          pane.add(textArea);
          pointcut( objects, classes, methods, 
          new LoggingWrapper(textArea), 
          "logIntoConsole");
        */
    }

    /**
     * The wrapper that logs methods into a Swing console. */
    /*
      public class LoggingWrapper extends Wrapper {
      JTextArea console;
      public LoggingWrapper(JTextArea console) {
      this.console = console;
      }
      public Object logIntoConsole() {
      if( console != null ) {
      console.append( (String)arg(0) );
      console.setCaretPosition( console.getText().length() );
      }
      return proceed();
      }
      }
    */

    public void defineResource(String type, String name, String path) {
        ResourceManager.defineResource(name, path);
    }

    public void setIcon(ClassItem cli, String name) {
        cli.setAttribute(ICON, ResourceManager.getResource(name));
    }

    public void setIcon(MemberItem member, String name) {
        member.setAttribute(ICON, ResourceManager.getResource(name));
    }

    public void setDynamicIcon(MethodItem method, MethodItem iconMethod) {
        method.setAttribute(DYNAMIC_ICON, iconMethod);
    }

    public void setDynamicIcon(ClassItem cli, MethodItem iconMethod) {
        cli.setAttribute(DYNAMIC_ICON, iconMethod);
    }

    /**
     * Returns the icon associated with this item. If no icon was set
     * with one the of setIcon() configuration methods, we try to
     * return a reasonable default.
     *
     * @see #setIcon(ClassItem,String)
     * @see #setIcon(MemberItem,String) 
     */
    public static String getIcon(MetaItem item) {
        String result = (String)item.getAttribute(ICON);
        if (result == null) {
            if (item instanceof MethodItem) {
                MethodItem method = (MethodItem)item;
                if (method.isSetter()) {
                    result = ResourceManager.getResource("edit_icon");
                } else if (method.isAdder()) {
                    if (method.getParameterTypes().length > 0) {
                        result =
                            getIcon(
                                ClassRepository.get().getClass(
                                    method.getParameterTypes()[0]));
                    }
                    if (result == null)
                        result = ResourceManager.getResource("new_icon");
                } else if (method.isRemover()) {
                    result = ResourceManager.getResource("remove_icon");
                }
            }
        }
        return result;
    }

    /**
     * Gets an icon for an object
     * @param cli the class of the object
     * @param object the object
     * @return an icon's resource name
     */
    public static String getIcon(ClassItem cli, Object object) {
        MethodItem dynIcon = (MethodItem)cli.getAttribute(DYNAMIC_ICON);
        String icon = null;
        if (dynIcon!=null) {
            icon = (String)dynIcon.invokeStatic(new Object[] {object});
        }
        if (icon==null)
            icon = (String)cli.getAttribute(ICON);
        return icon;
    }

    public static String getIcon(Callback callback) {
        AbstractMethodItem method = callback.getMethod();
        String icon =
            method != null ? GuiAC.getIcon(method) : null;
        if (method != null) {
            MethodItem dynIcon = (MethodItem)method.getAttribute(DYNAMIC_ICON);
            if (dynIcon!=null) {
                icon = (String)dynIcon.invokeStatic(
                    new Object[] {
                        method,
                        callback.getObject(), 
                        callback.getParameters()});
            }
        }
        if (icon == null) {
            icon = ResourceManager.getResource("blank_icon");
        }
        return icon;
    }

    public void hideTreeRelation(FieldItem field) {
        field.setAttribute(HIDDEN_TREE_RELATION, Boolean.TRUE);
    }

    public void setMenu(ClassItem classItem, String[] menu) {
        MethodItem[] methods = new MethodItem[menu.length];
        for (int i = 0; i < menu.length; i++) {
            if (menu[i].equals(""))
                methods[i] = null;
            else
                try {
                    methods[i] = classItem.getMethod(menu[i]);
                } catch (NoSuchMethodException e) {
                    warning(e.toString());
                    methods[i] = null;
                }
        }
        classItem.setAttribute(MENU, methods);
    }

    public static MethodItem[] getMenu(ClassItem classItem) {
        Vector methods = new Vector();
        while (classItem != null) {
            MethodItem[] curmenu = (MethodItem[]) classItem.getAttribute(MENU);
            if (curmenu != null) {
                for (int i = curmenu.length - 1; i >= 0; i--) {
                    if (!methods.contains(curmenu[i]))
                        methods.add(0, curmenu[i]);
                }
            }
            classItem = classItem.getSuperclass();
        }
        MethodItem[] result = new MethodItem[methods.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = (MethodItem) methods.get(i);
        }
        return result;
    }

    /**
     * Catch this event and remove the display from the handled list if
     * exists.
     *
     * @param display the closing display */

    public void whenCloseDisplay(Display display) {
        removeDisplay(display);
    }

    public void whenReload() {
        Iterator it = displays.values().iterator();
        while (it.hasNext()) {
            Display display = (Display) it.next();
            if (display
                .getClass()
                .getName()
                .equals("org.objectweb.jac.aspects.gui.swing.SwingDisplay")) {
                loggerDisplay.debug("Refreshing display " + display);
                display.fullRefresh();
            }
        }
    }

    public void beforeReload() {
        cguis.clear();
    }

    /**
     * The default constructor for the GUI aspect component.
     *
     * <p>By default it defines the visible property to false for some
     * JAC system items.
     *
     */

    public GuiAC() {
        blockKeywords = new String[] { "menu", "window", "view" };
        setDefaults();
    }

    /**
     * Defines the visible property to false for some JAC system items.  
     */
    public void setDefaults() {
        ClassRepository cr = ClassRepository.get();
        ClassItem nameRepositoryClass =
            cr.getClass("org.objectweb.jac.core.NameRepository");
        ClassItem acManagerClass =
            cr.getClass("org.objectweb.jac.core.ACManager");
        ClassItem topologyClass =
            cr.getClass("org.objectweb.jac.core.dist.Topology");

        setVisible(nameRepositoryClass.getField("names"), false);
        setVisible(
            acManagerClass,
            new String[] { "registering", "orderedObjects", "orderedNames" },
            false);

        setVisible(topologyClass.getField("nameReps"), false);
        setVisible(
            topologyClass.getMember(
                "getContainerIndex(org.objectweb.jac.core.dist.RemoteContainer)"),
            false);
        setVisible(
            topologyClass.getMember("getContainerIndex(java.lang.String)"),
            false);
        setVisible(
            topologyClass.getMember("getContainerIndexes(gnu.regexp.RE)"),
            false);
        setVisible(
            topologyClass.getMember("getContainerIndexes(java.lang.String[])"),
            false);
        setVisible(topologyClass.getMember("countContainers"), false);
        setVisible(
            topologyClass.getMember("getContainer(java.lang.String)"),
            false);
        setVisible(topologyClass.getMember("getContainer(int)"), false);
        setVisible(topologyClass.getMember("isContainer"), false);
        setVisible(topologyClass.getMember("createNameReps"), false);

        setTableView(topologyClass.getField("containers"));

        setDirectCollectionMethods(
            topologyClass.getCollection("containers"),
            new String[] { "launchRemoteGUI" });

        ClassItem rmiStubClass =
            cr.getClass(
                "org.objectweb.jac.core.dist.rmi.RMIRemoteContainerStub");
        setVisible(rmiStubClass.getMember("copy"), false);
        setVisible(rmiStubClass.getMember("getByteCodeFor"), false);
        setVisible(rmiStubClass.getMember("instantiates"), false);

        ClassItem applicationClass =
            cr.getClass("org.objectweb.jac.core.Application");

        setVisible(applicationClass.getMember("props"), false);
        //      applicationClass.setAttribute("GuiAC.List.showItem", applicationClass.getMethod("getName"));
        ClassItem applicationManagerClass =
            cr.getClass("org.objectweb.jac.core.ApplicationRepository");

        setTableMembersOrder(applicationClass,new String[] {"name","constructorClass","instantiated"});

        setViewOnSelection(
            applicationManagerClass.getCollection("applications"));
        setAutoCreate(applicationManagerClass.getField("applications"));

        applicationManagerClass.getMethod("addApplication").setAttribute(
            ASKING_SEQUENCE,
            new Object[] { "autoCreate" });
        setVisible(applicationManagerClass.getMember("getApplication"), false);

        setCategories(
            applicationClass,
            new String[] { "Aspects", "Properties" });

        setCategory(applicationClass.getMember("name"), "Properties");
        setCategory(applicationClass.getMember("path"), "Properties");
        setCategory(applicationClass.getMember("instantiated"), "Properties");
        setCategory(
            applicationClass.getMember("constructorClass"),
            "Properties");
        setCategory(applicationClass.getMember("arguments"), "Properties");
        setCategory(applicationClass.getMember("start"), "Properties");

        setCategory(applicationClass.getMember("acs"), "Aspects");
        setCategory(applicationClass.getMember("acConfigurations"), "Aspects");

        setEmbeddedEditor(applicationClass.getField("name"));
        setEmbeddedEditor(applicationClass.getField("path"));

        setFileChooserView(
            applicationClass.getMethod("setPath"),
            new String[] { "" },
            "directories");

        setViewOnSelection(applicationClass.getCollection("acConfigurations"));
        setAutoCreate(applicationClass.getField("acConfigurations"));

        //setTableView( "org.objectweb.jac.aspects.distribution.Topology", "containers");

        applicationClass.getMethod("addAcConfiguration").setAttribute(
            ASKING_SEQUENCE,
            new Object[] { "autoCreate" });
        //applicationClass.getMethod( "weave" ).setAttribute( CATEGORY, "Aspects");
        //applicationClass.getMethod( "unweave" ).setAttribute( CATEGORY, "Aspects");
        setCategory(applicationClass.getMember("realizes"), "Aspects");

        applicationClass.getMethod("configures").setAttribute(
            VISIBLE,
            Boolean.FALSE);

        applicationClass.getCollection("acs").setAttribute(
            VISIBLE,
            Boolean.FALSE);

        //applicationClass.getCollection( "acs" ).addAddingMethod( applicationClass.getMethod( "weave" ) );
        //applicationClass.getCollection( "acs" ).addRemovingMethod( applicationClass.getMethod( "unweave" ) );

        setSystemListener(true);

        // Trace configuration
        /*
          generateDefaultParameterNames( "org.objectweb.jac.aspects.gui.WrappableMap" );
          setMethodDynamicParameterChoice("org.objectweb.jac.aspects.gui.WrappableMap","put",
          new String[] { "org.objectweb.jac.aspects.gui.WrappableMap", 
          "org.objectweb.jac.aspects.gui.WrappableMap" }, 
          new String[] { "getCategories", "getLevels" },
          new String[] { "true", "false"});
          setParameterNames("org.objectweb.jac.aspects.gui.WrappableMap","put", 
          new String[] { "trace", "level"} );
          RttiAC rttiac = (RttiAC)ACManager.get().getObject("rtti");
          rttiac.addWrittenFields("org.objectweb.jac.aspects.gui.WrappableMap","put",new String[] {"delegate"});
        */

        pointcut(
            "ALL",
            "ALL && !org.objectweb.jac.aspects.gui.*Display && !COLLECTIONS",
            "MODIFIERS",
            ViewControlWrapper.class.getName(),
            null,
            NOT_SHARED);
        //pointcut("ALL","COLLECTIONS","add.* || remove.* || clear.*",
        //         ViewControlWrapper.class.getName(),"updateView",NOT_SHARED);

    }

    public void askForParameters(String classExpr) {
        // weave InputWrapper.askForParameters on all methods with parameters
        pointcut(
            "ALL",
            classExpr,
            "!.*().* && !CONSTRUCTORS",
            InputWrapper.class.getName(),
            "catchInputCanceled",
            SHARED);
    }

    /**
     * The default behavior of this <code>BaseProgramListener</code>
     * method is to notify the display with the
     * <code>applicationStarted</code> event.
     *
     * @see org.objectweb.jac.core.Display#applicationStarted() */

    public void afterApplicationStarted() {
        if (Jac.startSwingGUI()) {
            createSwingDisplays(Jac.getStartSwingGUI());
        }
        if (Jac.startWebGUI()) {
            createWebDisplays(Jac.getStartWebGUI());
        }
    }

    /**
     * Return a session id for the local display
     */
    public static String getLocalSessionID() {
        return "Swing"
            + org.objectweb.jac.core.dist.Distd.getLocalContainerName();
    }

    /**
     * The programmer of a new GUI aspect must overload this method to
     * return the right default program name.<p>
     *
     * By default, it returns the package path name of the current GUI
     * aspect (must be right most of the time).
     *
     * @return the default program name
     */

    protected String getDefaultProgramName() {
        return getClass().getName().substring(
            0,
            getClass().getName().lastIndexOf('.'));
    }

    /** A flag to memorize that the mainWindow is beeing launched (or
        has already been launched). */
    static boolean launched = false;

    public void setVisible(MemberItem member, boolean visible) {
        logger.debug("setVisible(" + member.getLongName() + "," + visible + ")");
        member.setAttribute(VISIBLE, Boolean.valueOf("" + visible));
    }

    public void setEditable(FieldItem field, boolean editable) {
        logger.debug("setEditable(" + field + "," + editable + ")");
        if (field != null) {
            field.setAttribute(EDITABLE, Boolean.valueOf("" + editable));
        }
    }

    public void setAddable(CollectionItem collection, boolean addable) {
        logger.debug("setAddable("+collection+","+addable+")");
        collection.setAttribute(ADDABLE, ExtBoolean.valueOf(addable));
    }

    public void setRemovable(CollectionItem collection, boolean removable)
    {
        logger.debug("setRemovable("+collection+","+removable+")");
        collection.setAttribute(REMOVABLE, ExtBoolean.valueOf(removable));
    }

    public void setEditableDefaultValues(
        CollectionItem collection,
        boolean editable) {
        collection.setAttribute(
            EDITABLE_DEFAULT_VALUES,
            editable ? Boolean.TRUE : Boolean.FALSE);
    }

    public static boolean hasEditableDefaultValues(MetaItem metaItem) {
        Object value = metaItem.getAttribute(EDITABLE_DEFAULT_VALUES);
        if (value == null)
            return false;
        else
            return ((Boolean) value).booleanValue();
    }

    /**
     * Tells if an item is visible (displayed by the GUI).
     *
     * @param substance the object holding the meta item
     * @param metaItem the meta item to check 
     * @return true if visible
     *
     * @see #setVisible(MemberItem,boolean) 
     * @see #isVisible(Object,MetaItem)
     */
    public static boolean isVisible(Object substance, MetaItem metaItem) {
        Object value = metaItem.getAttribute(substance, VISIBLE);

        if (value == null)
            return true;
        else
            return ((Boolean) value).booleanValue();
    }

    /**
     * Tells if an item is visible (displayed by the GUI).
     *
     * @param metaItem the meta item to check 
     * @return true if visible
     *
     * @see #setVisible(MemberItem,boolean) 
     * @see #isVisible(MetaItem)
     */
    public static boolean isVisible(MetaItem metaItem) {
        Object value = metaItem.getAttribute(VISIBLE);

        if (value == null)
            return true;
        else
            return ((Boolean) value).booleanValue();
    }

    /**
     * Tells if a field is editable from the GUI.
     *
     * @param substance the object holding the field item
     * @param field the field item to check 
     * @return true if the field is editable
     */
    public static boolean isEditable(Object substance, FieldItem field) {
        Object value = field.getAttribute(substance, EDITABLE);
        return field.getSetter() != null
            && (value == null || ((Boolean) value).booleanValue());
    }

    /**
     * Tells if a field is editable from the GUI.
     *
     * @param field the field item to check 
     * @return true if the field is editable
     */
    public static boolean isEditable(FieldItem field) {
        Object value = field.getAttribute(EDITABLE);
        return field.getSetter() != null
            && (value == null || ((Boolean) value).booleanValue());
    }

    /**
     * Wether to show "new" buttons for a class (defaults to true)
     * @param cli the class
     */
    public static boolean isCreatable(ClassItem cli) {
        Object value = cli.getAttribute(CREATABLE);
        return value == null || ((Boolean) value).booleanValue();
    }

    /**
     * Tells wether to show an add button for a collection
     *
     * @param collection the collection item to check 
     * @return true if an add button must be displayed
     */
    public static boolean isAddable(CollectionItem collection) {
        Object value = collection.getAttribute(ADDABLE);
        MethodItem adder = collection.getAdder();
        return adder != null
            && isVisible(adder)
            && (value == null || ((Boolean) value).booleanValue());
    }

    /**
     * Tells wether to show an add button for a collection
     *
     * @param substance the object holding the field item
     * @param collection the collection item to check 
     * @return true if an add button must be displayed
     */
    public static boolean isAddable(
        Object substance,
        CollectionItem collection) 
    {
        Object value = collection.getAttribute(substance, ADDABLE);
        MethodItem adder = collection.getAdder();
        return adder != null
            && isVisible(substance, adder)
            && (value == null || ((Boolean) value).booleanValue());
    }

    /**
     * Tells wether to show a remove button for a collection
     *
     * @param collection the collection item to check 
     * @return true if a remove button must be displayed
     */
    public static boolean isRemovable(CollectionItem collection) {
        Object value = collection.getAttribute(REMOVABLE);
        MethodItem remover = collection.getRemover();
        return remover != null
            && isVisible(remover)
            && (value == null || ((Boolean) value).booleanValue());
    }

    /**
     * Tells wether to show a remove button for a collection
     *
     * @param substance the object holding the field item
     * @param collection the collection item to check 
     * @return true if a remove button must be displayed
     */
    public static boolean isRemovable(
        Object substance,
        CollectionItem collection) 
    {
        Object value = collection.getAttribute(substance, REMOVABLE);
        MethodItem remover = collection.getRemover();
        return remover != null
            && isVisible(remover)
            && (value == null || ((Boolean) value).booleanValue());
    }

    public void setVisible(ClassItem cl, String itemNames[], boolean visible) {
        for (int i = 0; i < itemNames.length; i++) {
            setVisible(cl.getMember(itemNames[i]), visible);
        }
    }

    public void setReadOnly(ClassItem cli, String viewName, boolean readOnly) {
        getView(cli,viewName).setReadOnly(readOnly);
    }

    public void setEmbeddedEditors(ClassItem cli) {
        setEmbeddedEditors(cli,DEFAULT_VIEW,true);
    }

    public void setEmbeddedEditors(ClassItem cli, String viewName, boolean embedded) {
        FieldItem[] fields = cli.getFields();
        if (fields != null) {
            for (int i = 0; i < fields.length; i++) {
                setEmbeddedEditor(fields[i],viewName,embedded);
            }
        }
    }

    public void setEmbeddedEditor(MemberItem member) {
        setEmbeddedEditor(member,DEFAULT_VIEW,true);
    }

    public void setEmbeddedEditor(MemberItem member, String viewName, boolean embedded) {
        if (member instanceof FieldItem) {
            FieldItem field = (FieldItem) member;
            if (!(field instanceof CollectionItem)) {
                if (field.getSetter() == null) {
                    //Log.warning("setEmbeddedEditor: no setter for "+cl+"."+itemName+", ignored");
                } else {
                    getView(field,viewName).setEmbeddedEditor(embedded);
                }
            }
            return;
        } else {
            getView(member,viewName).setEmbeddedEditor(embedded);
            //member.setAttribute(EMBEDDED_EDITOR, Boolean.TRUE);
        }
    }

    public void setEmbeddedEditors(
        CollectionItem collection,
        String viewName,
        boolean embedded) 
    {
        CollectionItemView itemView =
            (CollectionItemView) getView(collection, viewName);
        itemView.setEmbeddedEditors(embedded);
        loggerTable.debug("setEmbeddedEditors "+collection+"/"+viewName+"/"+embedded+
                          "->"+itemView);
    }

    public void setEmbeddedEditorColumns(
        CollectionItem collection,
        String viewName,
        MemberItem[] members) 
    {
        CollectionItemView itemView =
            (CollectionItemView) getView(collection, viewName);
        for (int i = 0; i < members.length; i++) {
            itemView.addEmbeddedEditorColumn(members[i]);
        }
    }

    public void setMultiLineCollection(
        CollectionItem collection,
        String viewName,
        CollectionItem multiLine)
    {
        CollectionItemView itemView =
            (CollectionItemView) getView(collection, viewName);
        itemView.setMultiLineCollection(multiLine);
    }

    public void setAdditionalRow(CollectionItem collection,
                                 String viewName,
                                 String row) {
        CollectionItemView itemView =
            (CollectionItemView) getView(collection, viewName);
        itemView.setAdditionalRow(collection.getClassItem().getField(row));        
    }

    public void groupBy(
        CollectionItem collection,
        String viewName,
        FieldItem groupBy)
    {
        CollectionItemView itemView =
            (CollectionItemView) getView(collection, viewName);
        itemView.setGroupBy(groupBy);
    }

    public void setViewType(
        FieldItem field,
        String viewName,
        String viewType) 
    {
        MemberItemView itemView = (MemberItemView) getView(field, viewName);
        itemView.setViewType(viewType);
    }

    /**
     * @return true is the item has the property Gui.embeddedEditor set
     * to "true"
     * @see #hasEmbeddedEditors(ClassItem)
     * @see #setEmbeddedEditor(MemberItem)
     */
    public static boolean isEmbeddedEditor(MetaItem item) {
        Boolean value = (Boolean) item.getAttribute(EMBEDDED_EDITOR);
        return value != null && value.booleanValue();
    }

    /**
     * Returns the value of the EMBEDDED_EDITORS atrribute from the
     * current collaboration. Defaults to false.
     */
    /*
      public static boolean isEmbeddedEditors() {
      Boolean value =
      (Boolean) Collaboration.get().getAttribute(GuiAC.EMBEDDED_EDITORS);
      return value != null && value.booleanValue();
      }
    */

    /**
     * @return true if at least one field of the class has the property
     * Gui.embeddedEditor set to "true" 
     * @see #isEmbeddedEditor(MetaItem)
     * @see #setEmbeddedEditor(MemberItem)
     */
    public static boolean hasEmbeddedEditors(ClassItem cli) {
        FieldItem fields[] = cli.getFields();
        String[] cats = (String[]) cli.getAttribute(CATEGORIES);
        if (fields != null) {
            for (int i = 0; i < fields.length; i++) {
                if (fields[i] instanceof CollectionItem)
                    continue;
                if (cats != null && fields[i].getAttribute(CATEGORIES) == null)
                    continue;
                                //(Arrays.asList(cats).contains(fields[i].getAttribute(CATEGORY))) 
                                //  continue;
                if (isEmbeddedEditor(fields[i]) && isVisible(fields[i])) {
                    return true;
                }
            }
        }
        return false;
    }

    //public void setBorder(ClassItem cli, String fieldName,
    public void setBorder(FieldItem field, String alignment, String style) {
        //FieldItem field = cli.getField(fieldName);
        field.setAttribute(
            BORDER,
            new Border(
                null,
                Border.a2iAlignment(alignment),
                Border.a2iStyle(style)));
    }

    //public void setBorder(ClassItem cli, String fieldName) {
    public void setBorder(FieldItem field) {
        setBorder(field, "LEFT", "LINE");
    }

    public static Border getBorder(FieldItem field) {
        return (Border) field.getAttribute(BORDER);
    }

    public void setCondition(AbstractMethodItem method, String condition) {
        ClassItem cl = method.getClassItem();
        MethodItem cond = cl.getMethod(condition);
        method.setAttribute(CONDITION, cond);
    }

    /**
     * Gets the condition method associated with a method, or null if
     * there's none.
     */
    public static MethodItem getCondition(AbstractMethodItem method) {
        return (MethodItem) method.getAttribute(CONDITION);
    }

    public void setSlowOperation(AbstractMethodItem method, boolean isSlow) {
        method.setAttribute(SLOW_OPERATION,ExtBoolean.valueOf(isSlow));
        if (isSlow) {
            pointcut(
                "ALL",
                method.getClassItem().getName(),
                method.toString(),
                WaitWrapper.class.getName(),
                null,SHARED);
        }
    }
    
    public void setMimeType(AbstractMethodItem method, String type) {
        method.setAttribute(MIME_TYPE,type);
    }

    /**
     * Tells wethers a method is slow
     */
    public static boolean isSlowOperation(AbstractMethodItem method) {
        return method.getBoolean(SLOW_OPERATION,false);
    }

    public void addPostInvoke(AbstractMethodItem method, 
                              AbstractMethodItem hook) {
        List hooks = (List)method.getAttribute(POST_INVOKE_HOOKS);
        if (hooks==null) {
            hooks = new Vector();
            method.setAttribute(POST_INVOKE_HOOKS,hooks);
        }
        hooks.add(hook);
    }


    /**
     * Tells wether a method should be enabled 
     */
    public static boolean isEnabled(
        AbstractMethodItem method,
        Object substance) 
    {
        MethodItem condition = (MethodItem) method.getAttribute(CONDITION);
        if (condition != null) {
            loggerMenu.debug("Condition for " + method.getFullName() + ": " + condition);
            return ((Boolean) condition.invoke(substance, ExtArrays.emptyObjectArray))
                .booleanValue();
        } else {
            loggerMenu.debug("No condition for " + method.getFullName());
            return true;
        }
    }

    public void setMethodParametersWidth(AbstractMethodItem method,
                                         Length[] width)
        throws Exception 
    {
        if (method.getParameterCount() != Array.getLength(width)) {
            throw new Exception(
                "setMethodParametersWidth: wrong number of parameters widths for "
                + method.getName());
        }
        method.setAttribute(EDITOR_WIDTH, width);
    }

    /**
     * Returns the parameters widths of a method or null if none was set.
     * @param method the method
     * @see #setMethodParametersWidth(AbstractMethodItem,Length[])
     */
    public static Length[] getMethodParametersWidth(AbstractMethodItem method) {
        return (Length[]) method.getAttribute(EDITOR_WIDTH);
    }

    public void setMethodParametersHeight(AbstractMethodItem method,
                                          Length[] height)
        throws Exception 
    {
        if (method.getParameterCount() != Array.getLength(height)) {
            throw new Exception(
                "setMethodParametersHeight: wrong number of parameters heights for "
                + method.getName());
        }
        method.setAttribute(EDITOR_HEIGHT, height);
    }

    /**
     * Returns the parameters heights of a method or null if none was set.
     * @param method the method
     * @see #setMethodParametersHeight(AbstractMethodItem,Length[])
     */
    public static Length[] getMethodParametersHeight(AbstractMethodItem method) {
        return (Length[]) method.getAttribute(EDITOR_HEIGHT);
    }

    public void setDefaultEditorWidth(VirtualClassItem type,
                                      Length width)
    {
        type.setAttribute(EDITOR_WIDTH, width);
    }

    public void setDefaultEditorWidth(ClassItem type, Length width) {
        type.setAttribute(EDITOR_WIDTH, width);
    }

    public static Length getEditorWidth(MetaItem type) {
        return  (Length)type.getAttribute(EDITOR_WIDTH);
    }

    public static Length getEditorHeight(MetaItem type) {
        return  (Length)type.getAttribute(EDITOR_HEIGHT);
    }

    public void setEditorWidth(FieldItem field, Length width) {
        field.setAttribute(EDITOR_WIDTH, width);
    }

    /**
     * Returns the editor width of a field or 0 if none was set.
     * @param field the field
     * @see #setEditorWidth(FieldItem,Length)
     */
    public static Length getEditorWidth(FieldItem field) {
        return (Length)field.getAttribute(EDITOR_WIDTH);
    }

    public void setSmallEditorWidth(FieldItem field, Length width) {
        field.setAttribute(EDITOR_SMALL_WIDTH, width);
    }

    public void setDefaultEditorHeight(VirtualClassItem type, Length height) 
    {
        type.setAttribute(EDITOR_HEIGHT, height);
    }

    public void setDefaultEditorHeight(ClassItem type, Length height) {
        type.setAttribute(EDITOR_HEIGHT, height);
    }

    public void setEditorHeight(FieldItem field, Length height) {
        field.setAttribute(EDITOR_HEIGHT, height);
    }

    public void setSmallEditorHeight(FieldItem field, Length height) {
        field.setAttribute(EDITOR_SMALL_HEIGHT, height);
    }

    static Map fontAttributes = new HashMap();

    public void setFontAttribute(String attribute, String value) {
        fontAttributes.put(attribute.toLowerCase(), value);
    }

    public static Map getFontAttributes() {
        return fontAttributes;
    }

    public void setCategories(ClassItem cli, String[] categories) {
        cli.setAttribute(CATEGORIES, categories);
    }

    /*
      public void setCategories(ClassItem cli, String viewName, 
      String[] categories) {
      getView(cli,viewName).setCategories(categories);
      }
    */

    public void setCategory(MemberItem member, String category) {
        setCategories(member, new String[] { category });
    }

    public void setCategories(MemberItem member, String[] categories) {
        member.setAttribute(CATEGORIES, categories);
    }

    /**
     * Get the category of a member as configured with setCategory().
     *
     * @param member the MemberItem
     * @return the category of the field or null.
     */
    public static String[] getCategories(MemberItem member) {
        return (String[]) member.getAttribute(CATEGORIES);
    }

    /**
     * Tells wether a member item belong to category
     * @return if category==null, true, otherwise if the categories of
     * the member contains the category, true, otherwise false.
     * @see #setCategory(MemberItem,String)
     * @see #setCategories(MemberItem,String[])
     */
    public static boolean isMemberInCategory(
        MemberItem member,
        String category) {
        if (category == null) {
            return true;
        }

        String[] categories = getCategories(member);
        if (categories == null)
            return false;
        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equals(category)) {
                return true;
            }
        }
        return false;
    }

    public void checkCategories(ClassItem cli, int length) {
        //String[] categories = getView(cli,DEFAULT_VIEW).getCategories();
        String[] categories = (String[]) cli.getAttribute(CATEGORIES);
        if (categories == null || categories.length != length) {
            throw new RuntimeException(
                "Wrong number of icons for categories of class " + cli);
        }
    }

    public void setCategoriesIcons(ClassItem cli, String[] icons) {
        //      checkCategories(cli,icons.length);
        for (int i = 0; i < Array.getLength(icons); i++) {
            String str = ResourceManager.getResource(icons[i]);
            if (str == null)
                warning("setCategoriesIcons: icon not found " + icons[i]);
            icons[i] = str;
        }

        cli.setAttribute(CATEGORIES_ICONS, icons);
    }

    /**
     * Get the categories icons (icons displayed in tabs).
     *
     * @param cl the class.
     * @return the list of icons.
     */
    public static String[] getCategoriesIcons(ClassItem cl) {
        return (String[]) cl.getAttribute(CATEGORIES_ICONS);
    }

    public void setCategoriesLabels(ClassItem cli, String[] labels) {
        //      checkCategories(cli,labels.length);
        cli.setAttribute(CATEGORIES_LABELS, labels);
    }

    /**
     * Get the categories labels (text displayed in tabs).
     *
     * @param cl the class
     * @return the list of labels.
     */
    public static String[] getCategoriesLabels(ClassItem cl) {
        return (String[]) cl.getAttribute(CATEGORIES_LABELS);
    }

    public void setEmbeddedView(MemberItem member) {
        setEmbeddedView(member, GuiAC.DEFAULT_VIEW, true);
    }

    public void setEmbeddedView(MemberItem member, boolean embedded) {
        setEmbeddedView(member, GuiAC.DEFAULT_VIEW, embedded);
    }

    public void setEmbeddedView(
        MemberItem member,
        String viewName,
        boolean embedded) 
    {
        getView(member, viewName).setEmbedded(embedded);
    }

    public void setEmbeddedAdder(CollectionItem collection, boolean embedded) {
        setEmbeddedView(collection.getAdder(), embedded);
    }

    /**
     * Tells if a field item's value must be displayed embbeded.
     * @return wether to display the field's values embbeded, default
     * to false.
     */
    /*
      public static boolean isEmbbededView(MemberItem member) {
      Boolean value = (Boolean)member.getAttribute(EMBEDDED_VIEW);
      return value!=null && value.booleanValue();
      }
    */

    public void setDesktopCustomizedView(ClassItem classItem, ClassItem type) {
        classItem.setAttribute(DESKTOP_VIEW, type);
    }

    public void setTableView(FieldItem field) {
        field.setAttribute(TABLE_VIEW, Boolean.TRUE);
    }

    public void setChoiceView(CollectionItem field, boolean external) {
        field.setAttribute(CHOICE_VIEW, ExtBoolean.valueOf(external));
    }

    public void showRowNumbers(CollectionItem collection, boolean value) {
        collection.setAttribute(
            SHOW_ROW_NUMBERS,
            value ? Boolean.TRUE : Boolean.FALSE);
    }

    protected static boolean defaultShowRowNumbers = false;

    public void setDefaultShowRowNumbers(boolean value) {
        defaultShowRowNumbers = value;
    }

    public static boolean isShowRowNumbers(CollectionItem collection) {
        return collection.getBoolean(SHOW_ROW_NUMBERS, defaultShowRowNumbers);
    }

    public void setToString(ClassItem classItem, String formatExpression) {
        classItem.setAttribute(TO_STRING, formatExpression);
    }

    public void setToString(
        ClassItem classItem,
        MemberItem selector,
        String formatExpression) 
    {
        Map rules = (Map) classItem.getAttributeAlways(CONTEXTUAL_TO_STRING);
        if (rules == null) {
            rules = new HashMap();
            classItem.setAttribute(CONTEXTUAL_TO_STRING, rules);
        }
        rules.put(selector, formatExpression);
    }

    public void setToString(
        ClassItem classItem,
        ClassItem selector,
        String formatExpression) 
    {
        Map rules = (Map) classItem.getAttributeAlways(CONTEXTUAL_TO_STRING);
        if (rules == null) {
            rules = new HashMap();
            classItem.setAttribute(CONTEXTUAL_TO_STRING, rules);
        }
        rules.put(selector, formatExpression);
    }

    public void setToolTipText(ClassItem classItem, String formatExpression) {
        classItem.setAttribute(TOOLTIP, formatExpression);
    }

    public void setToolTipText(
        ClassItem classItem,
        MemberItem selector,
        String formatExpression) 
    {
        Map rules = (Map) classItem.getAttributeAlways(CONTEXTUAL_TOOLTIP);
        if (rules == null) {
            rules = new HashMap();
            classItem.setAttribute(CONTEXTUAL_TOOLTIP, rules);
        }
        rules.put(selector, formatExpression);
    }

    public void setToolTipText(
        ClassItem classItem,
        ClassItem selector,
        String formatExpression) 
    {
        Map rules = (Map) classItem.getAttributeAlways(CONTEXTUAL_TOOLTIP);
        if (rules == null) {
            rules = new HashMap();
            classItem.setAttribute(CONTEXTUAL_TOOLTIP, rules);
        }
        rules.put(selector, formatExpression);
    }

    public void showColumnFilters(CollectionItem collection,                                  
                                  String[] columnNames) {
        ClassItem type = collection.getComponentType();
        Vector columns = new Vector();
        for (int i=0; i< columnNames.length; i++) {
            FieldItem field = type.getFieldNoError(columnNames[i]);
            if (field!=null) {
                columns.add(field);
            } else {
                warning("gui.showColumnFilters: no such field "+columnNames[i]+" in "+type.getName());
            }
        }
        collection.setAttribute(FILTERED_COLUMNS,columns);
    }

    public void setDefaultSortedColumn(
        CollectionItem collection,
        String column) 
    {
        collection.setAttribute(DEF_SORT, column);
    }

    public void setDefaultSortedColumn(ClassItem cl, String column) {
        cl.setAttribute(DEF_SORT, column);
    }

    public static String getDefaultSortedColumn(CollectionItem collection) {
        String column = (String) collection.getAttribute(GuiAC.DEF_SORT);
        if (column == null) {
            ClassItem type = collection.getComponentType();
            if (type != null)
                column = (String) type.getAttribute(GuiAC.DEF_SORT);
        }
        return column;
    }

    public void setAutoCreate(FieldItem field) {
        field.setAttribute(AUTO_CREATE, Boolean.TRUE);
    }

    public static boolean isAutoCreate(FieldItem field) {
        Boolean bool = (Boolean) field.getAttribute(AUTO_CREATE);
        if (bool != null)
            return bool.booleanValue();
        else if (field instanceof CollectionItem) {
            CollectionItem collection = (CollectionItem) field;
            MethodItem adder = collection.getAdder();
            if (adder != null)
                return GuiAC.isAutoCreateParameters(adder);
        } else {
            MethodItem setter = field.getSetter();
            if (setter != null)
                return GuiAC.isAutoCreateParameters(setter);
        }
        return false;
    }

    public void setAutoCreateInitializer(
        FieldItem field,
        MethodItem initializer) 
    {
        field.setAttribute(AUTO_CREATE_INITIALIZER, initializer);
    }

    /**
     * Returns the initializer method of a field, if any, or null.
     * @param field the field
     * @see #setAutoCreateInitializer(FieldItem,MethodItem)
     */
    public static MethodItem getInitiliazer(FieldItem field) {
        return (MethodItem) field.getAttribute(AUTO_CREATE_INITIALIZER);
    }

    public void setAutoCreateParameters(AbstractMethodItem method) {
        method.setAttribute(AUTO_CREATE, Boolean.TRUE);
    }

    public static boolean isAutoCreateParameters(AbstractMethodItem method) {
        return method.getBoolean(AUTO_CREATE, false);
    }

    public void setAutoCreateAll(ClassItem cl) {
        setAutoCreateParametersEx(cl, ExtArrays.emptyStringArray);
    }

    public void setAutoCreateParametersEx(
        ClassItem cli,
        String[] excludedMethods) 
    {
        Class clazz = cli.getActualClass();
        boolean error = false;
        try {
            Iterator it = cli.getAllMethods().iterator();
            List l = Arrays.asList(excludedMethods);
            while (it.hasNext()) {
                MethodItem cur = (MethodItem) it.next();
                if ((!l.contains(cur.getName()))
                    && (!cur.isRemover())
                    && cur.getActualMethod().getDeclaringClass() == clazz) {
                    try {
                        setAutoCreateParameters(cur);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setAutoCreateState(ClassItem cli, String[] fields) {
        cli.setAttribute(AUTO_CREATED_STATE, fields);
    }

    public void setOpenViewMethod(ClassItem cli, String methodName) {
        MetaItem mi = cli.getMethod(methodName);
        mi.setAttribute(OPEN_VIEW, Boolean.TRUE);
    }

    public void declareView(
        ClassItem cli,
        String viewName,
        String parentViewName) 
    {
        ObjectView view =
            new ObjectView(cli, viewName, getView(cli, parentViewName));
        Map views = (Map) cli.getAttributeAlways(VIEWS);
        if (views == null) {
            views = new HashMap();
            cli.setAttribute(VIEWS, views);
        }
        views.put(viewName, view);
    }

    public void declareView(ClassItem cli, String viewName) {
        declareView(cli, viewName, DEFAULT_VIEW);
    }

    /**
     * Gets an object view by name. Creates one if it does not exist
     */
    public static ObjectView getView(ClassItem cli, String viewName) {
        Map views = (Map) cli.getAttributeAlways(VIEWS);
        if (views == null) {
            views = new HashMap();
            cli.setAttribute(VIEWS, views);
        }
        ObjectView view = (ObjectView) views.get(viewName);
        if (view == null) {
            if (viewName.equals(DEFAULT_VIEW))
                view = new ObjectView(cli, viewName);
            else
                view = new ObjectView(cli, viewName, getView(cli,DEFAULT_VIEW));
        }
        views.put(viewName, view);
        return view;
    }

    /**
     * Gets an object view by name. Creates one if it does not exist
     */
    public static MemberItemView getView(MemberItem member, String viewName) {
        Map views = (Map) member.getAttributeAlways(VIEWS);
        if (views == null) {
            views = new HashMap();
            member.setAttribute(VIEWS, views);
        }
        MemberItemView view = (MemberItemView) views.get(viewName);
        if (view == null) {
            if (member instanceof CollectionItem)
                view =
                    new CollectionItemView((CollectionItem) member, viewName);
            else if (member instanceof FieldItem)
                view = new FieldItemView((FieldItem) member, viewName);
            else if (member instanceof MethodItem)
                view = new MethodItemView((MethodItem) member, viewName);
        }
        views.put(viewName, view);
        return view;
    }

    /**
     * Gets an object view by name. Creates one if it does not exist
     */
    public static CollectionItemView getView(
        CollectionItem collection,
        String viewName) 
    {
        Map views = (Map) collection.getAttributeAlways(VIEWS);
        if (views == null) {
            views = new HashMap();
            collection.setAttribute(VIEWS, views);
        }
        CollectionItemView view = (CollectionItemView) views.get(viewName);
        if (view == null) {
            view = new CollectionItemView(collection, viewName);
        }
        views.put(viewName, view);
        return view;
    }

    public void setAttributesOrder(ClassItem cli, String[] attributeNames) {
        setAttributesOrder(cli, DEFAULT_VIEW, attributeNames);
    }

    public void setAttributesOrder(
        ClassItem cli,
        String viewName,
        String[] attributeNames) 
    {
        getView(cli, viewName).setAttributesOrder(
            cli.getFields(attributeNames));
    }

    public void setLineBreaks(ClassItem cli, String[] fields) {
        FieldItem[] lineBreaks = new FieldItem[fields.length];
        for (int i = 0; i < fields.length; i++) {
            lineBreaks[i] = cli.getField(fields[i]);
        }
        cli.setAttribute(LINE_BREAKS, lineBreaks);
    }

    public void setMethodsOrder(ClassItem cli, String[] methodNames) {
        setMethodsOrder(cli, DEFAULT_VIEW, methodNames);
    }

    public void setMethodsOrder(
        ClassItem cli,
        String viewName,
        String[] methodNames) 
    {
        getView(cli, viewName).setMethodsOrder(cli.getMethods(methodNames));
    }

    public void setCreationAttributesOrder(
        ClassItem cli,
        String[] attributeNames) 
    {
        setAttributesOrder(cli, AUTOCREATE_VIEW, attributeNames);
    }

    /**
     * Gets the field items to display when creation a new instance a class
     */
    public static FieldItem[] getCreationAttributesOrder(ClassItem cl) {
        FieldItem[] fields = getView(cl,AUTOCREATE_VIEW).getAttributesOrder();
        if (fields == null) {
            fields = getView(cl,DEFAULT_VIEW).getAttributesOrder();
            if (fields == null) {
                fields = cl.getFields();
            }
        }
        return fields;
    }

    public static boolean isCreationAttribute(FieldItem field) {
        ClassItem cl = field.getClassItem();
        FieldItem[] attributesOrder = getCreationAttributesOrder(cl);
        if (attributesOrder == null)
            return true;
        return ExtArrays.contains(attributesOrder, field);
    }

    public void setMembersOrder(
        CollectionItem collection,
        ClassItem targetClass,
        String[] memberNames) 
    {
        setMembersOrder(collection, DEFAULT_VIEW, targetClass, memberNames);
    }

    public void setMembersOrder(
        CollectionItem collection,
        String viewName,
        ClassItem targetClass,
        String[] memberNames) 
    {
        CollectionItemView itemView = getView(collection,viewName);
        itemView.setMembersOrder(targetClass.getMembers(memberNames));
    }

    public static MemberItem[] getMembersOrder(
        CollectionItem collection,
        String viewName) 
    {
        CollectionItemView itemView = getView(collection,viewName);
        return itemView.getMembersOrder();
    }

    public void setTableMembersOrder(ClassItem cli, String[] memberNames) {
        setTableMembersOrder(cli,DEFAULT_VIEW,memberNames);
    }

    public void setTableMembersOrder(ClassItem cli, String viewName, 
                                     String[] memberNames) {
        ObjectView view = getView(cli,viewName);
        view.setTableMembersOrder(cli.getMembers(memberNames));
    }

    public void setTreeAttributesOrder(
        ClassItem cli,
        String[] attributeNames) 
    {
        cli.setAttribute(TREE_ATTRIBUTES_ORDER, cli.getFields(attributeNames));
    }

    public void setFieldChoice(
        FieldItem field,
        Boolean editable,
        String[] choice) 
    {
        Object[] choice2 = choice;
        choice2 = new Object[choice.length];
        try {
            logger.debug("setFieldChoice " + field.getType());
            for (int i = 0; i < choice.length; i++) {
                choice2[i] =
                    ClassRepository.instantiate(field.getType(), choice[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        field.setAttribute(FIELD_CHOICE, choice2);
        field.setAttribute(EDITABLE_CHOICE, editable);
    }

    public void setFieldEnum(FieldItem field, String enumName) {
        Enum enum = (Enum) enums.get(enumName);
        if (enum == null) {
            throw new RuntimeException("Unknown enumeration: " + enumName);
        }
        field.setAttribute(FIELD_ENUM, enum);
    }

    public static Enum getEnum(FieldItem field) {
        return (Enum)field.getAttribute(FIELD_ENUM);
    }

    public void setParameterEnums(
        AbstractMethodItem method,
        String[] enumNames)
        throws Exception 
    {
        if (enumNames.length != method.getParameterCount())
            throw new Exception(
                method.getName()
                + " expects "
                + method.getParameterCount()
                + " parameters");
        Enum[] enums = new Enum[enumNames.length];
        for (int i = 0; i < enumNames.length; i++) {
            if (enumNames[i] != null) {
                enums[i] = (Enum) this.enums.get(enumNames[i]);
                if (enums[i] == null) {
                    warning(
                        "setParameterEnums(" + method.getName()
                        + "): Unknown enumeration: " + enumNames[i]);
                }
            }
        }
        method.setAttribute(PARAMETER_ENUMS, enums);
    }

    public void setLinkedParameters(
        AbstractMethodItem method,
        String[] collections)
        throws Exception 
    {
        if (collections.length != method.getParameterCount())
            throw new Exception(
                method.getName()
                + " expects "
                + method.getParameterCount()
                + " parameters");
        CollectionItem[] colls = new CollectionItem[collections.length];
        for (int i = 0; i < collections.length; i++) {
            if (!collections[i].equals("")) {
                colls[i] =
                    (CollectionItem) MemberItem.getMemberFromFullName(
                        collections[i]);
            } else {
                colls[i] = null;
            }
        }
        method.setAttribute(LINKED_PARAMETERS, colls);
    }

    public void setDefaultValue(
        FieldItem field,
        MethodItem method,
        String value) 
    {
        field.setAttribute(DYN_DEFAULT_VALUE, new Object[] { method, value });
    }

    public void setDefaultValue(FieldItem field, String value) {
        setDefaultValue(
            field,
            ClassRepository.get().getClass(GuiAC.class).getMethod(
                "getDefaultValue"),
            value);
    }

    public static boolean hasDefaultValue(FieldItem field) {
        return field.getAttribute(DYN_DEFAULT_VALUE) != null;
    }

    public static boolean isEditableChoice(FieldItem field) {
        return field.getBoolean(EDITABLE_CHOICE, false);
    }

    public void setDynamicFieldChoice(
        FieldItem field,
        Boolean editable,
        ClassItem targetClass,
        String targetMethod) 
    {
        setDynamicFieldChoice(
            field,
            editable,
            targetClass.getMethod(targetMethod));
    }

    public void setDynamicFieldChoice(
        FieldItem field,
        CollectionItem targetField) 
    {
        field.setAttribute(FIELD_CHOICE, targetField);
        field.setAttribute(EDITABLE_CHOICE, Boolean.TRUE);
    }

    public void setDynamicFieldChoice(
        FieldItem field,
        Boolean editable,
        MethodItem targetMethod) 
    {
        field.setAttribute(FIELD_CHOICE, targetMethod);
        field.setAttribute(EDITABLE_CHOICE, editable);
    }

    public void setMethodDynamicParameterChoice(
        MethodItem method,
        ClassItem[] targetClasses,
        String[] targetMethods,
        boolean[] editable) 
    {
        MethodItem[] methods =
            new MethodItem[method.getParameterTypes().length];
        for (int i = 0; i < methods.length; i++) {
            if (targetClasses[i] != null && !targetClasses[i].equals("")) {
                methods[i] = targetClasses[i].getMethod(targetMethods[i]);
            } else {
                methods[i] = null;
            }
        }
        setMethodDynamicParameterChoice(method,methods,editable);
    }

    public void setMethodDynamicParameterChoice(
        MethodItem method,
        MethodItem[] targetMethods,
        boolean[] editable) 
    {
        method.setAttribute(PARAMETER_CHOICES, targetMethods);
        method.setAttribute(EDITABLE_CHOICES, editable);
    }

    public void setDynamicClassChoice(String className, MethodItem targetMethod) {
        MetaItem cli = ClassRepository.get().getVirtualClass(className);
        cli.setAttribute(CLASS_CHOICES,targetMethod);
    }

    public void setCollectionType(CollectionItem collection, String type) {
        collection.setAttribute(
            COLLECTION_TYPE,
            ClassRepository.get().getClass(type));
    }

    /**
     * Sets the selection mode for an URL.
     *
     * @param field the field (of URL type) 
     * @param mode the selection mode (DIRECTORIES_ONLY ||
     * FILES_AND_DIRECTORIES), default is FILES_ONLY
     * @param extensions the selection extensions (gif, ps, etc.)
     * @param extensionsDescription the description of the extensions
     * (can be null) 
     */
    public void setURLSelectionMode(
        FieldItem field,
        String mode,
        String[] extensions,
        String extensionsDescription) 
    {
        field.setAttribute(FILE_SELECTION_MODE, mode);
        field.setAttribute(FILE_EXTENSIONS, extensions);
        field.setAttribute(FILE_EXTENSIONS_DESCRIPTION, extensionsDescription);
    }

    /*
      public void addAllowedFileExtensions(
      FieldItem field,
      String[] fileExtensions,
      String fileDescription) 
      {
      FileEditorConfig config = (FileEditorConfig)field.getAttribute(FILE_EDITOR_CONFIG);
      if (config==null) {
      config = new FileEditorConfig();
      field.setAttribute(FILE_EDITOR_CONFIG,config);
      }
      }
    */

    public void setViewOnSelection(CollectionItem collection) {
        collection.setAttribute(VIEW_ON_SELECTION, Boolean.TRUE);
    }

    /**
     * Tells wether VIEW_ON_SELECTION is set for a given field. Defaults to true.
     * @param field the field
     * @return true if VIEW_ON_SELECTION is set for the field
     * @see #setViewOnSelection(CollectionItem)
     */
    public static boolean getViewOnSelection(FieldItem field) {
        return field.getBoolean(VIEW_ON_SELECTION, true);
    }

    public void setOnSelection(
        CollectionItem collection,
        MethodItem eventHandler) 
    {
        collection.setAttribute(SELECTION_HANDLER, eventHandler);
    }

    public void setSelectionTarget(
        CollectionItem collection,
        ClassItem targetClass,
        String targetField) 
    {
        collection.setAttribute(
            SELECTION_TARGET,
            targetClass.getField(targetField));
    }

    public static MethodItem getSelectionHandler(MemberItem member) {
        Object handler = member.getAttribute(SELECTION_HANDLER);
        return (handler instanceof MethodItem) ? (MethodItem) handler : null;
    }

    public void setPreferredHeight(CollectionItem collection, Length height) {
        collection.setAttribute(VIEW_HEIGHT, height);
    }

    public void setPreferredWidth(CollectionItem collection, Length width) {
        collection.setAttribute(VIEW_WIDTH, width);
    }

    public void setNumRowsPerPage(CollectionItem collection, int numRows) {
        collection.setAttribute(NUM_ROWS_PER_PAGE, new Integer(numRows));
    }

    public void setAvailableNumRowsPerPage(CollectionItem collection, int[] numRows) {
        collection.setAttribute(AVAILABLE_NUM_ROWS_PER_PAGE, numRows);
    }

    public static int[] getAvailableNumRowsPerPage(CollectionItem collection) {
        return (int[])collection.getAttribute(AVAILABLE_NUM_ROWS_PER_PAGE);
    }

    static int defaultNumRowsPerPage = 10;

    public static int getNumRowsPerPage(CollectionItem collection) {
        Object numRows = collection.getAttribute(NUM_ROWS_PER_PAGE);
        if (numRows == null)
            return defaultNumRowsPerPage;
        else
            return ((Integer) numRows).intValue();
    }

    /**
     * Gets start index for collection view from the context
     * @param collection the collection to get the start index of
     */
    public static int getStartIndex(CollectionItem collection) {
        Map startIndexes =
            (Map) Collaboration.get().getAttribute(START_INDEXES);
        if (startIndexes == null) {
            return 0;
        } else {
            Object value = startIndexes.get(collection);
            if (value == null)
                return 0;
            else
                return ((Integer) value).intValue();
        }
    }

    /**
     * Sets the start index for a collection view in the context
     * @param collection the collection
     * @param index the start index
     */
    public static void setStartIndex(CollectionItem collection, int index) {
        Map startIndexes =
            (Map) Collaboration.get().getAttribute(START_INDEXES);
        if (startIndexes == null) {
            startIndexes = new HashMap();
            Collaboration.get().addAttribute(START_INDEXES, startIndexes);
        }

        startIndexes.put(collection, new Integer(index));
    }

    public static void removeStartIndex(CollectionItem collection) {
        Map startIndexes =
            (Map) Collaboration.get().getAttribute(START_INDEXES);
        if (startIndexes != null) {
            startIndexes.remove(collection);
        }
    }

    public void setFileChooserView(
        MethodItem method,
        String[] fileExtensions,
        String fileDescription) 
    {
        method.setAttribute(
            FILE_CHOOSER_VIEW,
            new Object[] { fileExtensions, fileDescription });
        return;
    }

    /**
     * Tells if the given method call by the GUI opens a file chooser
     * dialog.<p>
     *
     * @param setter the setter for the field
     * @return true if a file chooser is oppened
     * @see #getFileExtensions(MethodItem)
     * @see #getFileDescription(MethodItem) */

    public boolean isFileChooserView(MethodItem setter) {
        return setter.getAttribute(FILE_CHOOSER_VIEW) != null;
    }

    /**
     * Gets the file extensions for a given field that is view as a
     * file chooser (more precisely, the field that is set by the given
     * setter).<p>
     *
     * For instance, the file extensions can be a strings array like
     * {"gif", "jpg", "png"} or {"txt", "doc"}.<p>
     *
     * @param setter the setter for the field
     * @return the description of the files that are associated to the
     * field
     * @see #isFileChooserView(MethodItem) */

    public String[] getFileExtensions(MethodItem setter) {
        Object attr = setter.getAttribute(FILE_CHOOSER_VIEW);
        if (attr != null) {
            return (String[]) ((Object[]) attr)[0];
        }
        return null;
    }

    /**
     * Gets the file description for a given field that is view as a
     * file chooser (more precisely, the field that is set by the given
     * setter).<p>
     *
     * For instance, the file description can be a string like "Image
     * files" or "Text files".<p>
     *
     * @param setter the setter for the field
     * @return the description of the files that are associated to the
     * field
     * @see #isFileChooserView(MethodItem) */

    public String getFileDescription(MethodItem setter) {
        Object attr = setter.getAttribute(FILE_CHOOSER_VIEW);
        if (attr != null) {
            return (String) ((Object[]) attr)[1];
        }
        return null;
    }

    /**
     * Returns true if the given item must be displayed by the GUI as a
     * table.<p>
     *
     * @param collection the collection to check 
     */
    public static boolean isTableView(CollectionItem collection) {
        return (collection.getBoolean(TABLE_VIEW, false))
            || (collection.isMap() && !RttiAC.isIndex(collection));
    }

    public static boolean isChoiceView(CollectionItem collection) {
        return collection.getBoolean(CHOICE_VIEW,false);
    }

    public static boolean isExternalChoiceView(CollectionItem collection) {
        return (collection.getBoolean(CHOICE_VIEW, false));
    }

    public void setCreationAllowedParameters(
        AbstractMethodItem method,
        boolean[] create) {
        method.setAttribute(CREATION_ALLOWED_PARAMETERS, create);
    }

    public void setCreationAllowed(FieldItem field, boolean allow) {
        field.getSetter().setAttribute(
            CREATION_ALLOWED_PARAMETERS,
            new boolean[] { allow });
    }

    /**
     * Tells wether to display a "new" button for a parameter of a method
     *
     * @param method the method
     * @param i index of the parameter
     * @return a boolean
     *
     * @see #setCreationAllowedParameters(AbstractMethodItem,boolean[])
     */
    public static boolean isCreationAllowedParameter(
        AbstractMethodItem method,
        int i) {
        boolean[] cs =
            (boolean[]) method.getAttribute(CREATION_ALLOWED_PARAMETERS);
        if (cs == null) {
            return Wrappee.class.isAssignableFrom(
                method.getParameterTypes()[i]);
            /*
              if (i==0 && ((MethodItem)method).getSetField()!=null) {
              return true;
              } else {
              return false;
              }
            */
        }
        return cs[i];
    }

    public void setParameterNames(
        AbstractMethodItem method,
        String[] parameterNames) 
    {
        method.setAttribute(PARAMETER_NAMES, parameterNames);
        int count = method.getParameterCount();
        if (parameterNames.length!=count) {
            warning("setParameterNames: expecting "+count+" parameterNames for "+
                    method+", got "+parameterNames.length);
        }
    }

    /**
     * Returns the parameter names of a method. If none were with
     * <code>setParameterNames</code>, try some naming convention
     * heuristic.
     * @param method the method 
     * @return the parameter names of the method
     * @see #setParameterNames(AbstractMethodItem,String[])
     * @see #setParameterNames(AbstractMethodItem,String[])
     */
    public static String[] getParameterNames(AbstractMethodItem method) {
        String[] parameterNames =
            (String[]) method.getAttribute(PARAMETER_NAMES);
        if (parameterNames == null && method instanceof MethodItem) {
            MethodItem m = (MethodItem) method;
            if (m.isSetter()) {
                parameterNames = new String[] { getLabel(m.getSetField())};
            } else if (m.isRemover()) {
                parameterNames =
                    new String[] { getLabel(m.getRemovedCollections()[0])};
            } else if (m.isAdder()) {
                String collName = getLabel(m.getAddedCollections()[0]);
                if (method.getParameterCount() == 1) {
                    parameterNames = new String[] { NamingConventions.getSingular(collName) };
                } else if (method.getParameterCount() == 2) {
                    parameterNames = new String[] { "key", collName };
                }
            }
        }
        return parameterNames;
    }

    public void setParameterFields(
        AbstractMethodItem method,
        String[] parameterFields) 
    {
        method.setAttribute(PARAMETER_FIELDS, parameterFields);
    }

    public void setPasswordParameters(
        AbstractMethodItem method,
        String[] params) 
    {
        method.setAttribute(PASSWORD_PARAMETERS, params);
    }

    public void setDirectCollectionMethods(
        CollectionItem collection,
        String[] methods) 
    {
        collection.setAttribute(
            DIRECT_COLLECTION_METHODS, 
            collection.getComponentType().getMethods(methods));
    }

    public void setDefaultValues(AbstractMethodItem method, Object[] values) {
        method.setAttribute(DEFAULT_VALUES, values);
    }

    public void setDefaultsAttributesOrder(
        ClassItem cl,
        String[] attributeNames) 
    {
        cl.setAttribute(
            DEFAULTS_ATTRIBUTES_ORDER,
            cl.getFields(attributeNames));
    }

    public static FieldItem[] getDefaultsAttributesOrder(ClassItem cl) {
        return (FieldItem[]) cl.getAttribute(DEFAULTS_ATTRIBUTES_ORDER);
    }

    public void setClosingMethod(ClassItem classItem, String methodName) {
        AbstractMethodItem method = classItem.getAbstractMethod(methodName);
        method.setAttribute("GuiAC.closingMethod", Boolean.TRUE);
    }

    public void setModifyingBoxes(
        ClassItem classItem,
        String methodName,
        String[] modifyingBoxes) 
    {
        AbstractMethodItem method = classItem.getAbstractMethod(methodName);
        method.setAttribute("GuiAC.modifyingBoxes", modifyingBoxes);
    }

    /**
     * Get the modifying boxes for the given abstract method item (a
     * method or a constructor).<p>
     *
     * @return the mofifying boxes names for each parameter */

    public String[] getModifyingBoxes(AbstractMethodItem method) {
        return (String[]) method.getAttribute("GuiAC.modifyingBoxes");
    }

    /**
     * Tells the swing gui to capture System.out so that it appears a
     * in tab.
     */

    public void captureSystemOut() {
        /*
          ProgramView pv = (ProgramView)getDisplay("admin");
          if (pv != null)
          pv.captureSystemOut();
          else
          Log.warning("gui","captureSystemOut(): no ProgramView found");
        */
    }

    public void captureSystemErr() {
        /*
          ProgramView pv = (ProgramView)getDisplay("admin");
          if (pv != null)
          pv.captureSystemErr();
          else
          Log.warning("gui","captureSystemErr(): no ProgramView found");
        */
    }

    String defaultCurrency;

    public void setDefaultCurrency(String currencyName, int precision) {
        MetaItem classItem =
            (MetaItem) ClassRepository.get().getVirtualClass("currency");
        classItem.setAttribute("gui.defaultCurrency", currencyName);
        declareCurrency(currencyName, precision, 1);
    }

    /**
     * Returns the default currency
     */
    public static String getDefaultCurrency() {
        MetaItem classItem =
            (MetaItem) ClassRepository.get().getVirtualClass("currency");
        return (String) classItem.getAttribute("gui.defaultCurrency");
    }

    public void declareCurrency(
        String currencyName,
        int precision,
        double rate) 
    {
        MetaItem classItem =
            (MetaItem) ClassRepository.get().getVirtualClass("currency");
        logger.debug("currency classItem =  " + classItem);
        Hashtable currencies =
            (Hashtable) classItem.getAttribute("gui.currencies");
        if (currencies == null) {
            logger.debug("building currencies");
            currencies = new Hashtable();
            classItem.setAttribute("gui.currencies", currencies);
        }
        currencies.put(
            currencyName,
            new Currency(currencyName, precision, rate));
        logger.debug("declareCurrency " + currencyName + " " + rate);
    }
    /**
     * Returns an enumeration of all declared currencies, including the
     * default currency. 
     * 
     * @return the declared currencies as an Enumeration of
     * Currency.
     */
    public static Enumeration getCurrencies() {
        MetaItem classItem =
            (MetaItem) ClassRepository.get().getVirtualClass("currency");
        Hashtable currencies =
            (Hashtable) classItem.getAttribute("gui.currencies");
        logger.debug(currencies.size() + " currencies");
        return currencies.keys();
    }
    /**
     * Returns the Currency object associated with a currency name.
     */
    public static Currency getCurrency(String currencyName) {
        MetaItem classItem =
            (MetaItem) ClassRepository.get().getVirtualClass("currency");
        Hashtable currencies =
            (Hashtable) classItem.getAttribute("gui.currencies");
        return (Currency) currencies.get(currencyName);
    }

    // guiType -> ViewFactories
    static Hashtable viewFactories = new Hashtable();

    /**
     * Returns a ViewFactory for a given guiType
     *
     * @param guiType the type of the gui ("swing","web",...)
     */
    public static ViewFactory getViewFactory(String guiType) {
        ViewFactory result = (ViewFactory) viewFactories.get(guiType);
        if (result == null) {
            loggerFactory.debug(
                "Creating a ViewFactory for the \"" + guiType + "\" gui");
            result = new ViewFactory();
            ViewFactory.init(guiType, result);
            viewFactories.put(guiType, result);
        }
        return result;
    }

    /**
     * Set the view constructor for a given gui type.
     * @param guiType the type of the gui ("swing","web",...)
     * @param viewType the type of the view
     * @param constructor the view constructor for this gui type and view type
     */
    public void setViewConstructor(
        String guiType,
        String viewType,
        AbstractMethodItem constructor) 
    {
        getViewFactory(guiType).setViewConstructor(viewType, constructor);
    }

    // Dates

    public void setDateFormat(String dateFormat) {
        ClassRepository.get().getClass("java.util.Date").setAttribute(
            "DATE_FORMAT",
            dateFormat);
    }

    /**
     * Returns the date format as set by setDateFormat.
     *
     * @return the date format
     */
    public static String getDateFormat() {
        String format =
            (String) ClassRepository.get().getClass(
                "java.util.Date").getAttribute(
                    "DATE_FORMAT");
        if (format == null)
            format = "dd/MM/yyyy";
        return format;
    }

    public void setDateHourFormat(String dateFormat) {
        ClassRepository.get().getClass("java.util.Date").setAttribute(
            DATEHOUR_FORMAT,
            dateFormat);
    }

    /**
     * Returns the date format for DateHours as set by setDateHourFormat.
     *
     * @return the date format
     */
    public static String getDateHourFormat() {
        String format =
            (String) ClassRepository.get().getClass(
                "java.util.Date").getAttribute(
                    DATEHOUR_FORMAT);
        if (format == null)
            format = "dd/MM/yyyy HH:mm";
        return format;
    }

    /**
     * Returns the default display format of floats.
     */
    public static String getFloatFormat() {
        String format =
            (String) ClassRepository.get().getClass(
                "java.lang.Float").getAttribute(
                    FLOAT_FORMAT);
        if (format == null)
            format = "0.00";
        return format;
    }

    public void setFloatFormat(FieldItem field, String format) {
        field.setAttribute(FLOAT_FORMAT, format);
    }

    /**
     * Returns the default display format of integers.
     */
    public static String getIntFormat() {
        String format =
            (String) ClassRepository.get().getClass(
                "java.lang.Integer").getAttribute(FORMAT);
        if (format == null)
            format = "0";
        return format;
    }

    public void setIntFormat(FieldItem field, String format) {
        field.setAttribute(INT_FORMAT, format);
    }

    public void setFormat(String className, String format) {
        MetaItem metaItem = ClassRepository.get().getVirtualClass(className);
        metaItem.setAttribute(FORMAT, format);
    }

    /**
     * Returns the format of a ClassItem or VirtualClassItem.
     * @see #setFormat(String,String)
     */
    public static String getFormat(MetaItem item) {
        return (String) item.getAttribute(FORMAT);
    }

    /**
     * Returns the display format of an item.
     */
    public static String getFloatFormat(MetaItem item) {
        String format = (String) item.getAttribute(FLOAT_FORMAT);
        if (format == null)
            format = getFloatFormat();
        return format;
    }

    /**
     * Returns the display format of an item.
     */
    public static String getIntFormat(MetaItem item) {
        String format = (String) item.getAttribute(INT_FORMAT);
        if (format == null)
            format = getIntFormat();
        return format;
    }

    public void setCommitOnFocusLost(boolean value) {
        /*
          ClassRepository.get().getClass("org.objectweb.jac.aspects.gui.SwingObjectView").setAttribute(
          "gui.commitOnFocusLost",
          ExtBoolean.valueOf(value)
          );
        */
    }

    public static boolean getCommitOnFocusLost() {
        return true;
        /*
          Boolean val = (Boolean)ClassRepository.get().getClass("org.objectweb.jac.aspects.gui.SwingObjectView").
          getAttribute("gui.commitOnFocusLost");
          if (val == null)
          return true;
          else 
          return val.booleanValue();
        */
    }

    public void setDescription(ClassItem classItem, String description) {
        classItem.setAttribute(DESCRIPTION, description);
    }

    public void setDescription(MemberItem member, String description) {
        member.setAttribute(DESCRIPTION, description);
    }

    /**
     * Returns the description of an object if it has been set.
     */

    public static String getDescription(Object object) {
        return getDescription(object, null, null);
    }

    public static String getDescription(
        Object object,
        String beforeString,
        String afterString) 
    {
        String expr =
            (String) ClassRepository.get().getClass(object).getAttribute(
                DESCRIPTION);
        if (expr == null)
            return null;
        else
            return parseFormatExpression(
                expr,
                object,
                beforeString,
                afterString);
    }

    public void setLabel(ClassItem classItem, String label) {
        classItem.setAttribute(LABEL, label);
    }

    public void setLabel(MemberItem member, String label) {
        member.setAttribute(LABEL, label);
    }

    public void setLabel(
        MemberItem member,
        MemberItem selector,
        String label) 
    {
        Map rules = (Map) member.getAttributeAlways(CONTEXTUAL_LABEL);
        if (rules == null) {
            rules = new HashMap();
            member.setAttribute(CONTEXTUAL_LABEL, rules);
        }
        rules.put(selector, label);
    }

    public static String getLabel(MetaItem item) {
        return getLabel(item, null);
    }

    /**
     * Returns the label of an item, or an automatically computed
     * string if none was set.
     * @param item the MetaItem
     * @return the label of the MetaItem
     */
    public static String getLabel(MetaItem item, Stack context) {
        String label = (String) item.getAttribute(LABEL);
        if (context != null) {
            label =
                (String) getContextAttribute(item,
                                             CONTEXTUAL_LABEL,
                                             context,
                                             label);
        }
        if (label == null) {
            label = NamingConventions.textForName(item.getName());
        }
        if (label==null)
            logger.warn("No label for "+item);
        return label;
    }

    public void setMnemonics(MemberItem method, String mnemonics) {
        method.setAttribute(MNEMONICS,mnemonics);
    }

    public void setMnemonics(ClassItem cli, String mnemonics) {
        cli.setAttribute(MNEMONICS,mnemonics);
    }

    public static String getMnemonics(MetaItem item) {
        String mnemonics = (String)item.getAttribute(MNEMONICS);
        return mnemonics!=null ? mnemonics : "";
    }

    public static String getMnemonics(MethodItem method) {
        String mnemonics = (String)method.getAttribute(MNEMONICS);
        if (mnemonics==null) {
            if (method.isSetter()) {
                mnemonics = getMnemonics(method.getSetField());
            } else if (method.isGetter()) {
                mnemonics = getMnemonics(method.getReturnedField());
            } else if (method.isAdder()) {
                mnemonics = getMnemonics(method.getAddedCollection());
            } else if (method.isRemover()) {
                mnemonics = getMnemonics(method.getRemovedCollection());
            }
        }
        return mnemonics!=null ? mnemonics : "";
    }

    Hashtable enums = new Hashtable();

    public void defineEnum(String name, String[] values, int start, int step) {
        enums.put(name, new Enum(values, start, step));
    }

    public void setDisplayLabel(MemberItem member, boolean displayLabel) {
        member.setAttribute(DISPLAY_LABEL, ExtBoolean.valueOf(displayLabel));
    }

    public void setDisplayLabel(String itemName, boolean displayLabel) {
        MetaItem metaItem = ClassRepository.get().getVirtualClass(itemName);
        metaItem.setAttribute(DISPLAY_LABEL, ExtBoolean.valueOf(displayLabel));
    }

    public void setViewableItems(CollectionItem collection, boolean viewable) {
        setViewableItems(collection,DEFAULT_VIEW,viewable);
    }

    public void setViewableItems(CollectionItem collection, String viewName, boolean viewable) {
        getView(collection,viewName).setViewableItems(viewable);
    }

    public void setEnableLinks(CollectionItem collection, String viewName, boolean enable) {
        getView(collection,viewName).setEnableLinks(enable);
    }

    public void setEnableLinks(ClassItem cli, String viewName, boolean enable) {
        getView(cli,viewName).setEnableLinks(enable);
    }

    public void setCellViewType(CollectionItem collection, String viewName, 
                                FieldItem field, String viewType) {
        getView(collection,viewName).setViewType(field,viewType);
    }

    /**
     * Converts the a String into an object for the value of a field
     * @param field the field for which to convert the value
     * @param value the string to convert
     * @return an object of the type of the field whose string
     * representation is <em>value</em>, or null.
     */
    public static Object getDefaultValue(FieldItem field, String value) {
        //System.out.println("GET_DEFAULT_VALUE: "+field+ ", " + field.getType());
        Collection objects =
            ObjectRepository.getObjects(
                ClassRepository.get().getClass(field.getType()));
        Iterator i = objects.iterator();
        while (i.hasNext()) {
            Object obj = i.next();
            //System.out.println("TESTING: "+GuiAC.toString(obj));
            if (GuiAC.toString(obj).equals(value)) {
                                //System.out.println("RETURN: "+obj);
                return obj;
            }
        }
        return null;
    }

    public String[] getDefaultConfigs() {
        return new String[] {
            "org/objectweb/jac/aspects/gui/gui.acc",
            /*"org/objectweb/jac/aspects/gui/swing/gui.acc", */
            "org/objectweb/jac/aspects/gui/web/gui.acc",
            "org/objectweb/jac/aspects/authentication/gui.acc",
            "org/objectweb/jac/aspects/user/gui.acc" };
    }

    public void setNavBar(String gui, CollectionItem collection) {
        CustomizedGUI cgui = (CustomizedGUI) getCustomized(gui);
        if (cgui == null) {
            error(gui+" has not been declared with registerCustomized");
            return;
        }

        Vector vect = (Vector) collection.getAttribute(NAVBAR);

        if (vect == null) {
            vect = new Vector();
            collection.setAttribute(NAVBAR, vect);
        }
        if (!vect.contains(cgui)) {
            vect.add(cgui);
        }
    }

    public static boolean hasSetNavBar(
        CustomizedGUI gui,
        CollectionItem coll) 
    {
        Vector vect = (Vector) coll.getAttribute(NAVBAR);
        return vect != null && vect.contains(gui);
    }

    public void addViewFieldDependency(ClassItem cl, String fieldName) {
        Collection fields =
            (Collection) cl.getAttributeAlways(FIELD_DEPENDENCIES);
        if (fields == null) {
            fields = new Vector();
            cl.setAttribute(FIELD_DEPENDENCIES, fields);
        }
        fields.add(cl.getField(fieldName));
    }

    public void setInteractionHandler(MethodItem method, MethodItem handler) {
        if (!handler.isStatic())
            error("handler method must be static");
        else
            method.setAttribute(INTERACTION_HANDLER, handler);
    }

    public static MethodItem getInteractionHandler(AbstractMethodItem method) {
        return (MethodItem) method.getAttribute(INTERACTION_HANDLER);
    }

    /**
     * Returns the fields the views a class depend on.
     * @param cli the class
     * @return a collection of FieldItem
     * @see #addViewFieldDependency(ClassItem,String)
     */
    public static Collection getDependentFields(ClassItem cli) {
        return (Collection) cli.getAttribute(FIELD_DEPENDENCIES);
    }

    public static long dialogTimeout = 1000 * 60 * 20; // 20 minutes
    public void setDialogTimeout(long timeout) {
        dialogTimeout = timeout;
    }

    static String labelAdd = "add";
    public void setLabelAdd(String label) {
        labelAdd = label;
    }
    public static String getLabelAdd() {
        return labelAdd;
    }

    static String labelNew = "new";
    public void setLabelNew(String label) {
        labelNew = label;
    }
    public static String getLabelNew() {
        return labelNew;
    }

    static String labelCancel = "Cancel";
    public void setLabelCancel(String label) {
        labelCancel = label;
    }
    public static String getLabelCancel() {
        return labelCancel;
    }

    static String labelOK = "OK";
    public void setLabelOK(String label) {
        labelOK = label;
    }
    public static String getLabelOK() {
        return labelOK;
    }

    static String labelClose = "Close";
    public void setLabelClose(String label) {
        labelClose = label;
    }
    public static String getLabelClose() {
        return labelClose;
    }

    static String labelNone = "None";
    public void setLabelNone(String label) {
        labelNone = label;
    }
    public static String getLabelNone() {
        return labelNone;
    }

    static String labelAll = "All";
    public void setLabelAll(String label) {
        labelAll = label;
    }
    public static String getLabelAll() {
        return labelAll;
    }

    public void selectWithIndexedField(
        ClassItem cl,
        CollectionItem collection,
        String repositoryName) 
    {
        cl.setAttribute(INDEXED_FIELD_SELECTOR, collection);
        cl.setAttribute(REPOSITORY_NAME, repositoryName);
    }

    public void setIndexNotFoundHandler(ClassItem cl, MethodItem handler) {
        cl.setAttribute(INDEX_NOT_FOUND_HANDLER, handler);
    }

    /**
     * Gets the repository object of a class
     * @param cl the class
     * @see #selectWithIndexedField(ClassItem,CollectionItem,String)
     */
    public static Object getRepository(ClassItem cl) {
        String repositoryName = (String) cl.getAttribute(REPOSITORY_NAME);
        if (repositoryName != null) {
            return NameRepository.get().getObject(repositoryName);
        } else {
            return null;
        }
    }

    static String encoding = "UTF-8";
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
    public static String getEncoding() {
        return encoding;
    }
}
