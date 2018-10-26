/*
  Copyright (C) 2002-2003 Renaud Pawlak, Laurent Martelli.

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 2 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  USA */

package org.objectweb.jac.aspects.gui;

import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.rtti.AbstractMethodItem;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.MemberItem;
import org.objectweb.jac.core.rtti.MethodItem;

/**
 * This interface allows the programmer to create simple customized GUIs.
 *
 * <p>The idea is to define sub-panels that can contain views on the
 * objects of the application. The geometric placement of the
 * sub-panel is defined by the <code>setSubPanesGeometry</code> method
 * that has to be called at contruction-time.
 *
 * <p>Once the geometry is chosen, you can tell a pane to contain a
 * view on a given object by using the <code>addReferenceToPane</code>
 * method.
 *
 * <p>This class must be subclassed by the porgrammer of a JAC
 * application to provide a customized GUI.
 *
 * @see GuiAC
 * @see #setSubPanesGeometry(int,int,boolean[])
 * @see #addReferenceToPane(MemberItem,String,String[],String) */

public class CustomizedGUI { 
    static Logger logger = Logger.getLogger("gui");

    //GuiAC guiAC = null;

    // paneID -> {viewType,args[]}
    Hashtable paneContents = new Hashtable();
    // paneID -> viewType
    Hashtable paneContainers = new Hashtable();
    // (Integer)splitterID -> (Integer)location
    Hashtable splitters = new Hashtable();
    // changed paneID -> invalid_paneID
    Hashtable invalidPanes = new Hashtable(); 

    // WARNING: the following order is very important
    /** The subpanes are separated by an horizontal splitter */
    public static final int HORIZONTAL = 0;
    /** The upper subpanes are separated by a vertical splitter */
    public static final int HORIZONTAL_UP = 1;
    /** The down subpanes are separated by a vertical splitter */
    public static final int HORIZONTAL_DOWN = 2;
    /** The subpanes are separated by a vertical splitter */
    public static final int VERTICAL = 3;
    /** The left subpanes are separated by an horizontal splitter */
    public static final int VERTICAL_LEFT = 4;
    /** The right subpanes are separated by an horizontal splitter */
    public static final int VERTICAL_RIGHT = 5;

    public static final String BOTTOM = "BOTTOM";
    public static final String TOP = "TOP";

    public void setInvalidPane(String changedPane, String invalidPane) {
        invalidPanes.put(changedPane,invalidPane);
    }

    public String getInvalidPane(String changedPane) {
        return (String)invalidPanes.get(changedPane);
    }

    /**
     * Gets the number of sub-panes in the main window of the GUI.
     *
     * @return the number of panels 
     */
    int subPanesCount= 1;
    public int getSubPanesCount() {
        return subPanesCount;
    }

    int geometry = 0;
    public int getGeometry() {
        return geometry;
    }

    boolean[] scrollings = new boolean[] {false};
    public boolean[] getScrollings() {
        return scrollings;
    }
   
    String application;
   
    /**
     * Get the value of application.
     * @return value of application.
     */
    public String getApplication() {
        return application;
    }
   
    /**
     * Set the value of application.
     * @param v  Value to assign to application.
     */
    public void setApplication(String  v) {
        this.application = v;
    }
   

    /**
     * Sets the geometric arrangement of the panes.
     *
     * <p><ul><li><code>subPanesCount == 4 && geometry == VERTICAL</code>:
     * <pre>
     *    +-------+
     *    | 0 | 2 |
     *    +---|---+
     *    | 1 | 3 |
     *    +-------+
     * </pre>
     * </li>
     * <li><code>subPanesCount == 4 && geometry == HORIZONTAL</code>:
     * <pre>
     *    +-------+
     *    | 0 | 1 |
     *    +-------+
     *    | 2 | 3 |
     *    +-------+
     * </pre>
     * </li>
     * <li><code>subPanesCount == 3 && geometry == HORIZONTAL_UP</code>:
     * <pre>
     *    +-------+
     *    | 0 | 1 |
     *    +-------+
     *    |   2   |
     *    +-------+
     * </pre>
     * </li>
     * </li>
     * <li><code>subPanesCount == 3 && geometry == HORIZONTAL_DOWN</code>:
     * <pre>
     *    +-------+
     *    |   2   |
     *    +-------+
     *    | 0 | 1 |
     *    +-------+
     * </pre>
     * </li>
     * <li><code>subPanesCount == 3 && geometry == VERTICAL_LEFT</code>:
     * <pre>
     *    +---+---+
     *    | 0 |   |
     *    +---| 2 |
     *    | 1 |   |
     *    +---+---+
     * </pre>
     * </li>
     * <li><code>subPanesCount == 3 && geometry == VERTICAL_RIGHT</code>:
     * <pre>
     *    +---+---+
     *    |   | 0 |
     *    | 2 |---+
     *    |   | 1 |
     *    +---+---+
     * </pre>
     * </li>
     * <li><code>subPanesCount == 2 && geometry == VERTICAL</code>:
     * <pre>
     *    +---+---+
     *    |   |   |
     *    | 0 | 1 |
     *    |   |   |
     *    +---+---+
     * </pre>
     * </li>
     * <li><code>subPanesCount == 2 && geometry == HORIZONTAL</code>:
     * <pre>
     *    +-------+
     *    |   0   |
     *    +-------+
     *    |   1   |
     *    +-------+
     * </pre>
     * </li>
     *
     * @param subPanesCount the number of subpanes in the window
     * @param geometry the geometry = <code>VERTICAL || HORIZONTAL ||
     * VERTICAL_LEFT || VERTICAL_RIGHT || HORIZONTAL_UP ||
     * HORIZONTAL_DOWN</code> (see above)
     * @param scrollings a set of string that tells if the sub-panes
     * must be srollable or not 
     */
    public void setSubPanesGeometry(int subPanesCount, int geometry, 
                                    boolean[] scrollings) {
        this.subPanesCount = subPanesCount;
        this.geometry = geometry;
        this.scrollings = scrollings;
    }

    /**
     * Sets the object that should be opened in the given panel id.
     *
     * @param paneId the panel id (see the geometry to know its
     * placement)
     * @param viewType of the type of the view
     * @param args parameters to pass to the view type constructor
     *
     * @see #setSubPanesGeometry(int,int,boolean[]) 
     */
    public void setPaneContent(String paneId, 
                               String viewType, String[] args) {      
        paneContents.put(paneId,new PanelContent(viewType,args));
    }

    /**
     * Returns the type of the object to be displayed in a pane
     */
    public String getPaneContentType(String paneID) {
        return (String)((Object[])paneContents.get(paneID))[0];
    }

    public String[] getPaneContentArgs(String paneID) {
        return (String[])((Object[])paneContents.get(paneID))[1];
    }

    public Map getPaneContents() {
        return paneContents;
    }

    public void setPaneContainer(String paneId, String containerType) {
        paneContainers.put(paneId,containerType);
    }

    public String getPaneContainer(String paneId) {
        return (String)paneContainers.get(paneId);
    }

    public Map getPaneContainers() {
        return paneContainers;
    }

    Hashtable subPaneContents = new Hashtable();
   
    public Map getSubPaneContents() {
        return subPaneContents;
    }

    // PaneID -> Vector of FieldItem
    HashMap targetContainers = new HashMap();

    /**
     * Tells a referenced object to open in a given panel when a view
     * is asked by the user (instead of opening in a popup).
     *
     * @param reference the reference (can be a method's result)
     * @param viewType the type of the view that must be opened
     * @param paneId the panel id where the view must be opened */

    public void addReferenceToPane(MemberItem reference, 
                                   String viewType, String[] viewParams,
                                   String paneId) {
        Vector containers = (Vector)targetContainers.get(reference);
        if (containers==null) {
            containers = new Vector();
            targetContainers.put(reference,containers);
        }
        containers.add(new Target(paneId,viewType,viewParams));
    }

    public Map getTargetContainers() {
        return targetContainers;
    }

    public List getFieldTargets(MemberItem reference) {
        List result = null;
        while (result==null && reference!=null) {
            result = (List)targetContainers.get(reference);
            ClassItem superClass = reference.getClassItem().getSuperclass();
            if (superClass!=null)
                reference = superClass.getFieldNoError(reference.getName());
            else 
                reference = null;
        }
        return result;
    }

    int left;
    int up;
    int width;
    int height;

    public int getLeft() {
        return left;
    }

    public int getUp() {
        return up;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    boolean geometrySet = false;

    /**
     * Returns true if the geometry of this GUI was set with
     * <code>setPosition</code>.
     *
     * @see #setPosition(int,int,int,int) 
     */
    public boolean isGeometrySet() {
        return geometrySet;
    }

    /**
     * Sets the dimensions and position of the window regarding to the
     * main screen.
     *
     * @param left left-border pixel
     * @param up upper-border pixel
     * @param width in percentage regarding the screen
     * @param height in percentage regarding the screen 
     * @see #isGeometrySet() 
     */

    public void setPosition(int left, int up, int width, int height) {
        this.up = up;
        this.left = left;
        this.width = width;
        this.height = height;
        geometrySet = true;
    }

    /**
     * Sets a splitter location.
     *
     * <p>The splitter is referenced by its index going from the
     * front-end splitter to the back-end splitters. For instance, in
     * the case of a 3 sub-panel window, the 0 index references the
     * splitter that splits the main window in two, the 1 index, the
     * one that splits the half-window in two other smaller parts.
     *
     * @param splitterId the splitter's index
     * @param location the position in pixel, regarding to the top/left
     * component, a negative value means that the splitter should be
     * set at the preferred sized of the inner components */

    public void setSplitterLocation(int splitterId, float location) {
        splitters.put(new Integer(splitterId), new Float(location));
    }

    public Map getSplitters() {
        return splitters;
    }

    boolean hasStatusBar=false;
    MethodItem statusBarMethod=null;
    String statusPosition = BOTTOM;

    public boolean hasStatusBar() {
        return hasStatusBar;
    }

    public String getStatusPosition() {
        return statusPosition;
    }

    public MethodItem getStatusBarMethod() {
        return statusBarMethod;
    }

    /**
     * Adds a status bar to the GUI.
     */

    public void addStatusBar(MethodItem method,String position) {
        hasStatusBar=true;
        statusBarMethod = method;
        statusPosition=position;
    }
    
    /*
      public void showStatus(String message) {
      if( statusBar == null ) {
      System.out.println(message);
      } else {
      ((JLabel)statusBar.getComponent(0)).setText(message);
      }
      }
    */

    /**
     * Creates a new customized GUI.
     *
     * <p>When subclassing, a typical implementation of the constructor is:
     *
     * <pre class=code>
     * super()
     * // initialization calls such as setSubPanesGeometry
     * // addReferenceToPane, setPosition
     * ...
     * show();
     * </pre>
     *
     * @see #setSubPanesGeometry(int,int,boolean[])
     * @see #addReferenceToPane(MemberItem,String,String[],String)
     * @see #setPosition(int,int,int,int)
     * @see #setSplitterLocation(int,float) 
     */   
    public CustomizedGUI(String id) {
        this.id = id;
    }

    String id;
    public String getId() {
        return id;
    }

    Hashtable menus = new Hashtable();

    public Hashtable getMenus() {
        return menus;
    }

    public Menu getMenus(String name) {
        Menu menu=(Menu)menus.get(name);
        if(menu==null) {
            menu=new Menu();
            menus.put(name,menu);
        }
        return menu;
    }

    public boolean hasMenuBar() {
        return menus.size()>0;
    }

    /**
     * Add an item in a menu
     *
     * @param menuPath the path of the menu where to add an item
     * @param callback the method to call when this item is selected
     *
     * @see #addMenuSeparator(String,String[])
     */
    public void addMenuItem(String menuName,
                            String[] menuPath, 
                            Callback callback) {
        String[] path = new String[menuPath.length-1];
        System.arraycopy(menuPath,0,path,0,menuPath.length-1);
        getMenu(menuName,path).put(
            menuPath[menuPath.length-1],callback);
    }

    /**
     * Add a separator in a menu
     *
     * @param menuName the name of the menu
     * @param menuPath the path of the menu where to add a separator
     *
     * @see #addMenuItem(String,String[],Callback)
     */
    public void addMenuSeparator(String menuName,String[] menuPath) {
        getMenu(menuName,menuPath).addSeparator();
    }

    Vector toolbar = new Vector();

    /**
     * Add a button in the toolbar
     *
     * @param method the static method to call when the button is pressed
     *
     * @see #addToolbarSeparator()
     * @see #addToolbarAction(String,AbstractMethodItem)
     */
    public void addToolbarAction(AbstractMethodItem method) {
        toolbar.add(new Callback(null,method, new Object [0]));
    }

    /**
     * Add a button in the toolbar
     *
     * @param objectName name of the object on which to invoke the method
     * @param method the method to call when the button is pressed
     *
     * @see #addToolbarSeparator()
     * @see #addToolbarAction(AbstractMethodItem)
     */
    public void addToolbarAction(String objectName, AbstractMethodItem method) {
        addToolbarAction(objectName,method,new Object[0]);
    }

    public void addToolbarAction(String objectName, AbstractMethodItem method, Object[] params) {
        toolbar.add(new Callback(objectName,method,params));
    }

    /**
     * Add a separator in the toolbar
     *
     * @see #addToolbarAction(AbstractMethodItem)
     */
    public void addToolbarSeparator() {
        toolbar.add(null);
    }

    /**
     * Returns the toolbar of the customized GUI.
     *
     * @return a collection representing the tool. It contains
     * AbstractMethodItem objects for buttons, and null for a
     * separator.
     */
    public Collection getToolbar() {
        return toolbar;
    }

    public boolean hasToolBar() {
        return toolbar.size()>0;
    }

    /**
     * Find a menu defined by its path
     */
    protected Menu getMenu(String menuName,String[] menuPath) {
        Menu menu = getMenus(menuName);
        for (int i=0; i<menuPath.length; i++) {
            if (menu.containsKey(menuPath[i])) {
                Object current = menu.get(menuPath[i]);
                if (current instanceof Menu) {
                    menu = (Menu)current;
                } else {
                    logger.warn("overriding menu item "+current);
                    Menu old = menu;
                    menu = new Menu();
                    old.put(menuPath[i],menu);
                }
            } else {
                Menu old = menu;
                menu = new Menu();
                old.put(menuPath[i],menu);
            }
        }
        return menu;
    }

    public void setMenuIcon(String menuName,
                            String[] menuPath, String icon) {
        getMenu(menuName,menuPath).setIcon(icon);
    }
   
    public void setMenuPosition(String menuName,String position) {
        getMenus(menuName).setPosition(position);
    }
   
    String welcomeTitle = "Welcome";
    String welcomeMessage = null;
    String welcomeIcon = null;

    public void setWelcomeMessage(String title,String message,String icon) {
        this.welcomeTitle = title;
        this.welcomeMessage = message;
        this.welcomeIcon = icon;
    }

    String title;
    public void setTitle(String title) {
        this.title=title;
    }
    public String getTitle() {
        return title;
    }

    Vector cssURLs=new Vector();
    public void addStyleSheetURL(String url) {
        cssURLs.add(url);
    }

    public Vector getStyleSheetURLs() {
        return cssURLs; 
    }

    AbstractMethodItem onCloseHandler;
    public void setOnCloseHandler(AbstractMethodItem handler) {
        onCloseHandler = handler;
    }
    public AbstractMethodItem getOnCloseHandler() {
        return onCloseHandler;
    }

    String icon;
    public String getIcon() {
        return icon;
    }
    public void setIcon(String icon) {
        this.icon = icon;
    }
}
