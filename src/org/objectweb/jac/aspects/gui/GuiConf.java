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
 * This is the configuration interface of the Gui aspect. 
 *
 * @see GuiAC
 * @see View
 *
 * @author <a href="mailto:renaud@cnam.fr">Renaud Pawlak</a>
 * @author <a href="mailto:laurent@aopsys.com">Laurent Martelli</a>
 */

public interface GuiConf 
    extends ClassAppearenceGuiConf, FieldAppearenceGuiConf, 
MethodAppearenceGuiConf, BehaviorGuiConf {

    /**
     * Essential method for GUI, which weaves
     * <code>InputWrapper.askForParameters</code> on all methods with
     * parameters.
     *
     * <p>It permits to dynamically call methods, and so display
     * something when user clicks on buttons.</p>
     *
     * @param classExpr the classes
     */
    void askForParameters(String classExpr);

    /**
     * This configuration method allows the user to create and register
     * a new customized GUI.
     * 
     * <p>A configuration file should first use this method, then
     * configure the customized GUI, and then call the
     * <code>showCustomized</code> method when the GUI configuration is
     * finished.
     *
     * @param name the new customized GUI name
     */
    void registerCustomized(String name);

    /**
     * Sets the icon of the window
     * @param gui the gui ID
     * @param icon the name of the icon
     */
    void setCustomizedIcon(String gui, String icon);

    /**
     * Set an event handler which is called when the window is closed.
     * @param gui the customized gui ID
     * @param eventHandler the event handler 
     */
    void setOnCloseHandler(String gui, AbstractMethodItem eventHandler);

    /**
     * Sets a welcome message (in a popup) when a customized GUI is
     * started.
     *
     * @param gui the gui ID
     * @param title the popup's title
     * @param message the welcome message
     * @param icon an icon (defined as a resource) placed before the
     * message (can be null) */

    void setWelcomeMessage(String gui,String title,
                           String message,String icon);

    /**
     * This configuration method allows the programmer to actually show
     * a status bar in a declared personal UI.
     *
     * @param gui the name of the customized GUI
     * @param method the method that returns the text
     * @param position UP||BOTTOM */

    void addStatusBar( String gui, MethodItem method, String position );

    /**
     * This configuration method allows the programmer to actually show
     * an empty status bar in a declared personal UI. The text should
     * be set with the <code>showStatus</code> method.
     *
     * @param gui the name of the customized GUI
     * @param position UP||BOTTOM
     * 
     * @see #addStatusBar(String,MethodItem,String) 
     */
    void addStatusBar( String gui, String position );

    /**
     * This configuration method delegates to
     * <code>setSubPanesGeometry(int,int,String[])</code> with all the
     * panes scrollable.
     *
     * <p>The GUI must have been declared.
     *
     * @param gui the GUI name
     * @param subPanesCount the number of subpanes in the window
     * @param geometry the geometry = <code>VERTICAL || HORIZONTAL ||
     * VERTICAL_LEFT || VERTICAL_RIGHT || HORIZONTAL_UP ||
     * HORIZONTAL_DOWN || NONE</code>
     *
     * @see #registerCustomized(String)
     * @see #setSubPanesGeometry(String,int,String,boolean[]) 
     * @see #setPaneContent(String,String,String,String[])
     */
    void setSubPanesGeometry(String gui, int subPanesCount, String geometry);

    /**
     * This configuration method delegates to the corresponding
     * customized GUI.
     *
     * <p>The GUI must have been declared.
     *
     * @param gui the GUI name
     * @param subPanesCount the number of subpanes in the window
     * @param geometry the geometry = <code>VERTICAL || HORIZONTAL ||
     * VERTICAL_LEFT || VERTICAL_RIGHT || HORIZONTAL_UP ||
     * HORIZONTAL_DOWN</code>
     * @param scrollings a set of string that tells if the sub-panes
     * must be srollable or not (use setSubPanesGeometry(String,int,String) to
     * make all the sub-panes scrollable
     *
     * @see #registerCustomized(String)
     * @see #setSubPanesGeometry(String,int,String) 
     * @see #setPaneContent(String,String,String,String[])
     */
    void setSubPanesGeometry(String gui, int subPanesCount, 
                             String geometry,
                             boolean[] scrollings);

    /**
     * Defines the initial content of a pane.
     *
     * <p>The GUI must have been declared.
     *
     * @param gui the GUI name
     * @param paneId the panel id (see the geometry to know its placement)
     * @param type the type of the view
     * @param args arguments to pass to the constructor of the view
     * 
     * @see #registerCustomized(String)
     * @see #setViewConstructor(String,String,AbstractMethodItem)
     * @see #setSubPanesGeometry(String,int,String)
     * @see #setSubPanesGeometry(String,int,String,boolean[])
     */ 
    void setPaneContent(String gui, String paneId, String type, String[] args); 

    /**
     * Set the container type to use for a pane. The default is
     * "SingleSlotContainer".
     *
     * @param gui the GUI name
     * @param paneId the panel id (see the geometry to know its
     * placement)
     * @param type the type of the container
     *
     * @see #registerCustomized(String)
     * @see #setViewConstructor(String,String,AbstractMethodItem) 
     */
    void setPaneContainer(String gui, String paneId, String type);

    /**
     * Set a pane to be invalidated (reload) when a given pane content
     * changes.
     *
     * @param gui the GUI name
     * @param changedPane the pane to watch
     * @param invalidPane the pane to invalidate when the watched
     * pane's content changes */

    void setInvalidPane(String gui, String changedPane, String invalidPane);

    /**
     * Specify in which pane to open the view of a reference,
     * collection, or a method's result. The default view for an object
     * will be used.
     *
     * <p>Note that, for a method member, a displayabe object should be
     * returned by the method. Otherwise, a runtime error will happen.
     * 
     * @param gui the GUI name
     * @param member the member (reference, collection, or method)
     * @param paneId the ID of the pane where the view must be opened
     *
     * @see #registerCustomized(String)
     * @see #addReferenceToPane(String,MemberItem,,String,String[],Boolean,String) 
     */
    void addReferenceToPane(String gui,
                            MemberItem member,
                            String paneId);

    /**
     * This configuration method delegates to the corresponding
     * customized GUI.
     *
     * <p>The GUI must have been declared.
     *
     * @param gui the GUI name
     * @param field the field (reference of collection)
     * @param viewType the type of the view to open
     * @param small tell if the viewed object in this pane should be
     * small
     * @param panePath the path of the panel where the view must be
     * opened (<customizedID>/<paneID>)
     *
     * @see #registerCustomized(String)
     * @see #addReferenceToPane(String,MemberItem,String)
     * @see CustomizedGUI#addReferenceToPane(MemberItem,String,String[],String) 
     */
    void addReferenceToPane(String gui,
                            MemberItem field, 
                            String viewType, String[] viewParams,
                            Boolean small, 
                            String panePath);

    /**
     * This configuration method delegates to the corresponding
     * customized GUI.
     *
     * <p>The GUI must have been declared.
     *
     * @param gui the GUI name
     * @param left left-border pixel
     * @param up upper-border pixel
     * @param width in percentage regarding the screen
     * @param height in percentage regarding the screen
     *
     * @see #registerCustomized(String)
     * @see CustomizedGUI#setPosition(int,int,int,int) */ 

    void setPosition(String gui, int left, int up, 
                     int width, int height);

    /**
     * Sets the position of a menu
     * @param gui the customized GUI
     * @param menu the menu name
     * @param position the position of the menu (TOP, BOTTOM, LEFT or RIGHT)
     */
    void setMenuPosition(String gui, String menu, String position);

    /**
     * Add a menu item to a menu bar.
     *
     * @param gui the GUI name
     * @param menu the menu name
     * @param menuPath the path of the menu item.
     * @param method the callback method for that menu item. It must be
     * a static method with no arguments.
     *
     * @see #registerCustomized(String)
     * @see #addMenuItem(String,String,String[],String,AbstractMethodItem)
     * @see #addMenuItem(String,String,String[],String,AbstractMethodItem,String[])
     */
    void addMenuItem(String gui, String menu, String[] menuPath,
                     AbstractMethodItem method);

    /**
     * Add a menu item to a menu bar.
     *
     * @param gui the GUI name
     * @param menu the menu name
     * @param menuPath the path of the menu item.
     * @param objectName the name of the object on which to invoke the method
     * @param method the callback method for that menu item. It must be
     * an instance method with no arguments.
     *
     * @see #registerCustomized(String)
     * @see #addMenuItem(String,String,String[],AbstractMethodItem)
     * @see #addMenuItem(String,String,String[],AbstractMethodItem,String[])
     */
    void addMenuItem(String gui, String menu, String[] menuPath,
                     String objectName, AbstractMethodItem method);

    /**
     * Add a menu item to a menu bar.
     *
     * @param gui the GUI name
     * @param menu the menu name
     * @param menuPath the path of the menu item.
     * @param method the callback method for that menu item. It must be
     * a static method.
     * @param parameters the arguments to pass to the callback method
     * when it is called
     *
     * @see #registerCustomized(String) 
     * @see #addMenuItem(String,String,String[],AbstractMethodItem)
     * @see #addMenuItem(String,String,String[],String,AbstractMethodItem)
     * @see #addMenuItem(String,String,String[],String,AbstractMethodItem,String[])
     */
    void addMenuItem(String gui, String menu, String[] menuPath,
                     AbstractMethodItem method, String[] parameters);

    /**
     * Add a menu item to a menu bar.
     *
     * @param gui the GUI name
     * @param menu the menu name
     * @param menuPath the path of the menu item.
     * @param objectName the name of the object on which to invoke the method
     * @param method the callback method for that menu item. It must be
     * an instance method.
     * @param parameters the arguments to pass to the callback method
     * when it is called
     *
     * @see #registerCustomized(String) 
     * @see #addMenuItem(String,String,String[],AbstractMethodItem)
     * @see #addMenuItem(String,String,String[],AbstractMethodItem,String[])
     * @see #addMenuItem(String,String,String[],String,AbstractMethodItem)
     */
    void addMenuItem(String gui, String menu, String[] menuPath,
                     String objectName, AbstractMethodItem method, 
                     String[] parameters);

    /**
     * Add a separator in a menu.
     *
     * @param gui the GUI name
     * @param menuPath the path of the menu item separator.
     *
     * @see #registerCustomized(String)
     */
    void addMenuSeparator(String gui, String menu, String[] menuPath);

    /**
     * Set the icon for a menu.
     * @param gui the GUI name
     * @param menuPath the path of the menu
     * @param icon the name of the icon
     *
     * @see #registerCustomized(String)
     */
    void setMenuIcon(String gui, String menu, String[] menuPath, String icon);

    /**
     * Add a button in the toolbar
     *
     * @param gui the GUI name
     * @param method the callback method for that button. It must be a
     * static method with no arguments.
     *
     * @see #addToolbarAction(String,String,AbstractMethodItem)
     * @see #addToolbarAction(String,AbstractMethodItem,String[])
     * @see #registerCustomized(String)
     */
    void addToolbarAction(String gui, AbstractMethodItem method);

    /**
     * Add a button in the toolbar
     *
     * @param gui the GUI name
     * @param objectName name of the object on which to invoke the method
     * @param method the callback method for that button. It must be a
     * static method with no arguments.
     *
     * @see #addToolbarAction(String,AbstractMethodItem)
     * @see #addToolbarAction(String,AbstractMethodItem,String[])
     * @see #registerCustomized(String)
     */
    void addToolbarAction(String gui,String objectName,AbstractMethodItem method);

    /**
     * Add a button in the toolbar
     * 
     * @param gui the GUI name 
     * @param method a static method to invoke when the button is clicked
     * @param params some parameters to pass the method
     *
     * @see #addToolbarAction(String,String,AbstractMethodItem)
     * @see #addToolbarAction(String,AbstractMethodItem)
     * @see #registerCustomized(String)
     */
    void addToolbarAction(String gui,
                          AbstractMethodItem method,
                          String[] params);

    /**
     * Add a separator in the toolbar
     *
     * @param gui the GUI name
     *
     * @see #registerCustomized(String)
     */
    void addToolbarSeparator(String gui);

    /**
     * This configuration method delegates to the corresponding
     * customized GUI.
     *
     * <p>The GUI must have been declared.
     *
     * @param gui the GUI name
     * @param splitterId the splitter's index
     * @param location the position as a percentage between 0 and 1,
     * regarding to the top/left component, a negative value means that
     * the splitter should be set at the preferred sized of the inner
     * components
     * @see #registerCustomized(String)
     * @see CustomizedGUI#setSplitterLocation(int,float) 
     */ 
    void setSplitterLocation( String gui, int splitterId, float location );

    /**
     * This configuration method sets the title of the GUI main window.
     *
     * <p>The GUI must have been declared.
     *
     * @param gui the GUI name
     * @param title the window title
     * @see #registerCustomized(String) 
     */
    void setTitle(String gui, String title);

    /**
     * Sets the given methods to be logging.
     *
     * <p>When it is called, the argument --- that must be a string ---
     * is written into a text area added at the end of a subpanel
     * defined by a customized GUI.
     *
     * @param gui the customized gui (must be registered and
     * configured with at least <code>setSubPanesGeometry</code>)
     * @param objects the objects that contain the method (pointcut expression)
     * @param classes the class the contains the method (pointcut expression)
     * @param methods the method item names (pointcut expression)
     * @param paneId the subpanel id
     * @see #registerCustomized(String)
     * @see #setSubPanesGeometry(String,int,String) 
     */
    void setLoggingMethod(String gui, 
                          String objects, String classes, String methods, 
                          int paneId);

    /**
     * This configuration method allows the user to define new
     * resources that can be used by the GUI later-on.
     *
     * @param type the resource type (ICON) -- other types should be
     * supported soon
     * @param name the identifier of the resource (should be unique)
     * @param path the path where the resource is located (can be
     * classpath relative)
     * @see #setIcon(ClassItem,String) 
     */
    void defineResource(String type, String name, String path );
   
    /**
     * This configuration method allows the programmer to define
     * attributes for the default font. It currently is only used by
     * the swing display.
     *
     * <p>The specified font will be used by all the UI components.
     *
     * <p>Note: this method sets a global font that will be active for
     * all the configured GUIs. It is not possible to have different
     * font configurations when running several GUIs on the same
     * container.
     *
     * <p>Configurable font attributes are:</p>
     * <dl>
     *  <dt>family</dt><dd>serif,sans-serif or monospace</dd>
     *  <dt>weight</dt><dd>normal or bold</dd>
     *  <dt>style</dt><dd>normal or italic</dd>
     *  <dt>size</dt><dd>the size of the font</dd>
     * </dl>
     *
     * @param attribute the name of the attribute
     * @param value the value for the attribute
     */
    void setFontAttribute( String attribute, String value );

    /**
     * Adds a style-sheet URL for the generated html pages (for WEB
     * GUIs).
     *
     * <p>By default, the style-sheets are the one defined in the
     * org/objectweb/jac/aspects/gui/web directory but the user can override some of
     * their characteristics by adding customized ones (last added
     * overrides)
     *
     * @param gui the customized GUI the style sheets applies to
     * @param url the URL string 
     * @see #addStyleSheetURL(String)
     */
    void addStyleSheetURL(String gui, String url);
   
    /**
     * Adds a style-sheet URL for the generated html pages (for WEB
     * GUIs).
     *
     * @param url the URL string 
     * @see #addStyleSheetURL(String,String)
     */
    void addStyleSheetURL(String url);

    /**
     * Sets the delay before a dialog times out. Dialogs only time out
     * on the web GUI, to avoid locking threads on the server for ever.
     * @param timeout the timeout delay in milliseconds
     */
    void setDialogTimeout(long timeout);

    /**
     * Set the default currency
     *
     * @param currencyName the name of the default currency
     * @param precision number of decimals to display
     */
    void setDefaultCurrency(String currencyName, int precision);

    /** 
     * Declare a currency and it's change rate with the default currency
     *
     * @param currencyName the name of currency
     * @param precision number of decimals to display for the currency
     * @param rate the change rate for the currency
     */ 
    void declareCurrency( String currencyName, int precision, double rate);


    /**
     * Set the view constructor for a given gui type.
     * @param guiType the type of the gui ("swing","web",...)
     * @param viewType the type of the view
     * @param constructor the view constructor for this gui type and view type
     */
    void setViewConstructor(String guiType, 
                            String viewType, 
                            AbstractMethodItem constructor);
   

    /**
     * Set the default date format used by date components. It must be
     * a valid format as defined by java.text.SimpleDateFormat
     *
     * @param dateFormat the date format
     * @see java.text.SimpleDateFormat
     */
    void setDateFormat(String dateFormat);

    /**
     * Define an enumeration. It associates integer values with strings.
     *
     * @param name the name of enumeration to define
     * @param values the labels of the values
     * @param start the integer value of the first item.
     * @param step 
     *
     * @see FieldAppearenceGuiConf#setFieldEnum(FieldItem,String)
     * @see FieldAppearenceGuiConf#setFieldChoice(FieldItem,Boolean,String[])
     */
    void defineEnum(String name, String[] values, int start, int step);

    /**
     * Enables navigation bar for a collection. <p> Navigation bar is a
     * set of buttons (previous element, next element, remove element)
     * used to navigate in a collection
     *
     * @param gui the gui
     * @param collection the collection
     */
    void setNavBar(String gui, CollectionItem collection);

    void setLabelOK(String label);
    void setLabelCancel(String label);
    void setLabelNew(String label);
    void setLabelAdd(String label);
    void setLabelNone(String label);
    void setLabelAll(String label);

    /**
     * Sets the default charset encoding to use when interaction with
     * the user's terminal. Defaults to "UTF-8". Used by the WEB
     * diplay.
     *
     * @param encoding the charset encoding
     *
     * @see java.nio.charset.Charset
     */
    void setEncoding(String encoding);
}

