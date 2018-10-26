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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.objectweb.jac.aspects.authentication.AuthenticationAC;
import org.objectweb.jac.aspects.session.SessionAC;
import org.objectweb.jac.core.ACManager;
import org.objectweb.jac.core.Collaboration;
import org.objectweb.jac.core.Display;
import org.objectweb.jac.core.NameRepository;
import org.objectweb.jac.core.rtti.AbstractMethodItem;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.core.rtti.MetaItem;
import org.objectweb.jac.core.rtti.MethodItem;
import org.objectweb.jac.util.MimeTypes;
import java.io.File;

/**
 * This class is a container of commonly used GUI actions static
 * methods.
 *
 * <p>Usually, a GUI action takes in parameter the display
 * context. Using the context, is can then know the customized window
 * to use to interact with the users. When an action does not take any
 * display context, it means that it does not need any interaction
 * with the GUI or that the display context is retrieved throught the
 * collaboration. */

public class Actions {
    static Logger logger = Logger.getLogger("gui");

    /**
     * Quit the application by shutting down the server.
     *
     * @param context the display context for this action
     */
    public static void exit(DisplayContext context) {
        Display display = context.getDisplay();
        if (display!=null)
            display.close();
        System.exit(0);
    }
   
    /**
     * Logout from an application by clearing the current user. If an
     * authentication aspect is woven, the user is asked again.
     *
     * @param context the display context for this action */

    public static void logout(DisplayContext context) {
        SessionAC sac = ((SessionAC)ACManager.getACM().getAC("session"));
        if (sac==null) {
            logger.error("No session aspect found: logout cannot work");
            return;
        }
        sac.clearCurrentSessionAttribute(AuthenticationAC.USER);
        Collaboration.get().removeAttribute(SessionAC.INITIALIZED);
        context.getDisplay().fullRefresh();
    }

    /**
     * Shows a message on the status bar of the current customized. If
     * no display context available, then <code>println</code> is
     * used.
     * 
     * @param message the message to show */

    public static void showStatus(String message) {
        CustomizedView cview = ((DisplayContext)Collaboration.get()
                                .getAttribute(GuiAC.DISPLAY_CONTEXT)).getCustomizedView();
        if(cview!=null) {
            cview.showStatus(message);
        }
    }

    /**
     * Show a customized window that has been declared and configured
     * within the GUI aspect.
     *
     * @param context the display context for this action
     * @param id the customized ID */

    public static void showWindow(DisplayContext context,String id) {
        ((GuiAC)ACManager.getACM().getAC("gui"))
            .createSwingDisplays(new String[] {id});
    }
  
    /**
     * Invokes a method on an object. */

    public static void invoke(Object object, String methodName) {
        AbstractMethodItem method = ClassRepository.get()
            .getClass(object).getMethod(methodName);
        EventHandler.get().onInvoke(
            (DisplayContext)Collaboration.get().getAttribute(GuiAC.DISPLAY_CONTEXT),
            new InvokeEvent(null,object,method));
    }

    /**
     * Display an object in a panel.
     *
     * @param context the DisplayContext of the custmoized view
     * @param objectName the name of the object to display
     * @param panelID the panel ID where to display the object
     *
     * @see #viewObject(DisplayContext,String,String)
     * @see #openView(DisplayContext,String)
     */
    public static void viewObject2(DisplayContext context, 
                                   String objectName, String viewName, 
                                   String panelID) 
    {
        CustomizedView custom = context.getCustomizedView();
        if (custom!=null) {
            ViewFactory factory = context.getDisplay().getFactory();
            Object object = NameRepository.get().getObject(objectName);
            if (object!=null)
                custom.getPanelView().addView(
                    factory.createView(
                        objectName,"Object",
                        new Object[] {viewName,object},
                        context),
                    panelID
                );
            else 
                custom.getPanelView().addView(
                    factory.createView("No such object "+objectName,"Label",context),
                    panelID
                );

            EventHandler.get().maybeInvalidatePane(
                context.getDisplay().getFactory(),context,
                custom,panelID);
        }
    }

    /**
     * Display an object in a panel.
     *
     * @param context the DisplayContext of the custmoized view
     * @param objectName the name of the object to display
     * @param panelID the panel ID where to display the object
     *
     * @see #viewObject2(DisplayContext,String,String,String)
     * @see #openView(DisplayContext,String)
     */
    public static void viewObject(DisplayContext context, 
                                  String objectName, String panelID) 
    {
        viewObject2(context,objectName,GuiAC.DEFAULT_VIEW,panelID);
    }

    /**
     * Display an object in a new window
     *
     * @param context the DisplayContext of the custmoized view
     * @param objectName the name of the object to display
     *
     * @see #viewObject2(DisplayContext,String,String,String)
     * @see #viewObject(DisplayContext,String,String)
     */
    public static void openView(DisplayContext context, 
                                String objectName) 
    {
        Object object = NameRepository.get().getObject(objectName);
        if (object!=null)
            context.getDisplay().show(object);
        else
            context.getDisplay().showError("Error","No such object "+objectName);
    }

    public static String getOpenViewIcon(MethodItem method, 
                                         Object object, Object[] parameters) {
        Object target = NameRepository.get().getObject((String)parameters[0]);
        if (target!=null)
            return GuiAC.getIcon(ClassRepository.get().getClass(target),target);
        else
            return null;
    }

    public static String getFileIcon(File file) {
        if (file.isDirectory())
            return ResourceManager.getResource("open_icon");
        else 
            return ResourceManager.getResource("doc_icon");
    }

    /**
     * Changes a trace for the current application
     * @param loggerName category of the trace
     * @param level level of the trace
     */
    public static void setTrace(String loggerName, Level level) {
        Logger.getLogger(loggerName).setLevel(level);
    }

    /**
     * Returns all known logger names
     */
    public static Collection getLoggerNames(Object substance) {
        Enumeration enum = LogManager.getCurrentLoggers();
        LinkedList loggers = new LinkedList();
        while (enum.hasMoreElements()) {
            loggers.add(((Logger)enum.nextElement()).getName());
        }
        return loggers;
    }

    /**
     * Returns all known loggers
     */
    public static Collection getLoggers(ClassItem cli) {
        Enumeration enum = LogManager.getCurrentLoggers();
        LinkedList loggers = new LinkedList();
        while (enum.hasMoreElements()) {
            loggers.add(enum.nextElement());
        }
        return loggers;
    }

    /**
     * Returns all known log levels
     */
    public static Collection getLogLevels(ClassItem cli) {
        ArrayList levels = new ArrayList(7);
        levels.add(Level.ALL);
        levels.add(Level.OFF);
        levels.add(Level.DEBUG);
        levels.add(Level.WARN);
        levels.add(Level.ERROR);
        levels.add(Level.INFO);
        levels.add(Level.FATAL);
        return levels;
    }

    static MimeTypes mimeTypes = new MimeTypes();
    public static Collection getMimeTypes(MetaItem cli) {
        return mimeTypes.getMimeTypes();
    }
    static {
        mimeTypes.readDefaults();
    }

    /**
     * Reloads an aspect for the current application
     */
    public static void reloadAspect(String aspect) throws Exception {
        ACManager.getACM().reloadAspect(aspect);
    }

    /**
     * Enable EJP profiling
     */
    /*
      public static void enableProfiling() throws Throwable {
      if (!ejp.tracer.TracerAPI.enableTracing()) {
      throw ejp.tracer.TracerAPI.getInitializationError();
      }
      }

      public static void disableProfiling() throws Throwable {
      if (!ejp.tracer.TracerAPI.disableTracing()) {
      throw ejp.tracer.TracerAPI.getInitializationError();
      }
      }
    */
}
