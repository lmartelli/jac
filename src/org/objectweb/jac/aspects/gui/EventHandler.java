/*
  Copyright (C) 2001-2003 Renaud Pawlak <renaud@aopsys.com>, 
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.objectweb.jac.aspects.integrity.RoleWrapper;
import org.objectweb.jac.aspects.session.SessionAC;
import org.objectweb.jac.core.Collaboration;
import org.objectweb.jac.core.Interaction;
import org.objectweb.jac.core.ObjectRepository;
import org.objectweb.jac.core.Wrappee;
import org.objectweb.jac.core.Wrapping;
import org.objectweb.jac.core.rtti.AbstractMethodItem;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.core.rtti.CollectionItem;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.MemberItem;
import org.objectweb.jac.core.rtti.MethodItem;
import org.objectweb.jac.core.rtti.NamingConventions;
import org.objectweb.jac.core.rtti.RttiAC;
import org.objectweb.jac.util.ExtArrays;
import org.objectweb.jac.util.Strings;

/**
 * This handler manages events received from GUIs (web and swing for
 * now). It is especially used for lists and arrays (onSelection,
 * onView, etc ...)
 */

public class EventHandler implements FieldUpdate {
    static Logger logger = Logger.getLogger("gui.events");
    static Logger loggerDnd = Logger.getLogger("gui.dnd");
    static Logger loggerAssoc = Logger.getLogger("associations");

    static EventHandler singleton = new EventHandler();

    public static EventHandler get() {
        return singleton;
    }

    /**
     * Upcalled when a selection occurs on a field.
     *
     * @param context the context
     * @param container ???
     * @param selectedObject the selected object
     * @param field ???
     * @param extraOption */

    public void onSelection(
        DisplayContext context,
        MemberItem container,
        Object selectedObject,
        FieldItem field,
        Object extraOption) 
    {
        onSelection(
            context,
            container,
            selectedObject,
            field,
            extraOption,
            false);
    }

    /**
     * Upcalled when a view is asked on an object.
     *
     * @param context the context
     * @param container ???
     * @param selectedObject the selected object
     * @param field ???
     * @param extraOption */

    public void onView(
        DisplayContext context,
        MemberItem container,
        Object selectedObject,
        FieldItem field,
        Object extraOption) 
    {
        onSelection(
            context,
            container,
            selectedObject,
            field,
            extraOption,
            true);
    }

    /**
     * Upcalled when a selection occurs on a field. Displays a view of
     * the selected object.
     *
     * @param context the context
     * @param container member (reference, collection, or method) that
     * references the selected object (the result in case of a method)
     * @param selectedObject the selected object
     * @param field give focus to this field in the view of the selected object
     * @param extraOption
     * @param forceView when true, a window containing the selected
     * object is opened if we could not find a place where to put the
     * new view.
     */
    public void onSelection(
        DisplayContext context,
        MemberItem container,
        Object selectedObject,
        FieldItem field,
        Object extraOption,
        boolean forceView) 
    {
        logger.debug("onSelection("
            + (container != null ? container.getLongName() : "null") + ","
            + Strings.hex(selectedObject)
            + ",field="+ (field != null ? field.getLongName() : "null")
            + ",force=" + forceView + ")");
        CustomizedDisplay display = context.getDisplay();
        ViewFactory factory = display.getFactory();
        CustomizedView customizedView = context.getCustomizedView();

        Collaboration.get().addAttribute(GuiAC.DISPLAY_CONTEXT, context);

        // in choice view collections, nothing as to be done except a refresh
        if (container instanceof CollectionItem) {
            if (GuiAC.isChoiceView((CollectionItem) container)
                && !GuiAC.isExternalChoiceView((CollectionItem) container)) {
                display.refresh();
                return;
            }
        }

        MethodItem handler = null;
        FieldItem targetField = null;
        if (container != null) {
            handler = GuiAC.getSelectionHandler(container);
            targetField =
                (FieldItem) container.getAttribute(GuiAC.SELECTION_TARGET);
        }

        if (customizedView == null && handler == null && targetField == null) {
            if (forceView) {
                logger.debug("showing in dialog mode " + "(customizedView==null)");
                display.show(selectedObject);
            } else {
                logger.debug("onSelection aborted " + "(customizedView==null)");
                display.refresh();
            }
            return;
        }

        if (handler != null) {
            try {
                logger.debug("invoking selectionHandler " + handler);
                Object result =
                    handler.invokeStatic(
                        new Object[] { context, container, selectedObject });
                if (handler.getType() != void.class && result != null) {
                    if (handler.getType() == HandlerResult.class) {
                        handleResult(context,(HandlerResult)result);
                    } else {
                        onSelection(context, null, result, null, null);
                    }
                }
            } catch (Exception e) {
                logger.error(
                    "gui: invocation of event handler " + handler
                    + " for onSelection failed with " + e);
                e.printStackTrace();
            }
        }

        if (targetField != null) {
            selectedObject = targetField.getThroughAccessor(selectedObject);
            logger.debug("selected target -> " + selectedObject);
        }

        if (customizedView == null)
            return;
        CustomizedGUI customized = customizedView.getCustomizedGUI();
        List targets = customized.getFieldTargets(container);
        logger.debug("targets=" + targets);

        if (targets != null) {
            Iterator it = targets.iterator();
            while (it.hasNext()) {
				// the target is a panelPath=
                Target target = (Target) it.next();
                logger.debug("target.path = " + target.panePath);
				/*
                                  // target.panePath must look like this <customizedId>/<paneId>
                                  int index = target.panePath.indexOf("/");
                                  if (index==-1) {
                                  throw new RuntimeException("Bad pane path : "+target.panePath);
                                  }
                                  String customizedId = target.panePath.substring(0,index);
                                  String paneId = target.panePath.substring(index+1);
                                  logger.debug("customized = "+customizedId);
				*/

                String paneId = target.panePath;
                CompositeView targetView =
                    (CompositeView) customizedView.getView(paneId);

                if (selectedObject != null) {

                    Collaboration.get().addAttribute(
                        GuiAC.SMALL_VIEW,
                        container.getAttribute(GuiAC.SMALL_TARGET_CONTAINER));
                    try {
                        // creating and adding the new view
                        View view =
                            invalidatePane(
                                factory,
                                context,
                                targetView,
                                target.viewType,
                                target.viewParams,
                                selectedObject,
                                extraOption);

                        if (field != null && view != null)
                            setFocus(view, field, extraOption);

                    } finally {
                        Collaboration.get().removeAttribute(GuiAC.SMALL_VIEW);
                    }
                }
                //targetView.validate();
                maybeInvalidatePane(factory, context, customizedView, paneId);
            }
            display.refresh();
        } else if (forceView) {
            if (container != null
                && container.getAttribute(GuiAC.NEW_WINDOW) != null) {
                display.openView(selectedObject);
            } else {
                display.show(selectedObject);
            }
        }

    }

    public void handleResult(DisplayContext context, HandlerResult hres) {
        CustomizedView customizedView = context.getCustomizedView();
        CustomizedDisplay display = context.getDisplay();
        DisplayContext newContext =
            new DisplayContext(
                display,
                hres.target != null
                ? hres.target
                : customizedView);
        if (hres.target != null)
            customizedView = hres.target;
        onSelection(
            newContext,
            (CollectionItem) hres.container,
            hres.object,
            hres.field,
            hres.extraOption);
    }

    /**
     * Removes the content of pane when another pane's content is
     * changed
     *
     * @param selectedPaneID ID of the selected pane */
    public void maybeInvalidatePane(
        ViewFactory factory,
        DisplayContext context,
        CustomizedView customizedView,
        String selectedPaneID) 
    {
        CustomizedGUI customized = customizedView.getCustomizedGUI();
        String invalidPaneID = customized.getInvalidPane(selectedPaneID);
        logger.debug("invalidPane " + selectedPaneID + " -> " + invalidPaneID);
        if (invalidPaneID != null) {
            invalidatePane(
                factory,
                context,
                (CompositeView) customizedView.getView(invalidPaneID),
                "Empty",
                ExtArrays.emptyStringArray,
                null,
                null);
        }
    }

    /**
     * Gives focus to the view which is a field editor for a given field.
     * @param top view to start searching from. All subviews of this
     * view will be recursively inspected.
     * @param field the field whose editor to search for
     * @param option an option that will be passed when calling
     * <code>onSetFocus()</code> on the FieldEditor.
     * @see FieldEditor#onSetFocus(Object)
     */
    public void setFocus(View top, FieldItem field, Object option) {
        logger.debug("setFocus " + top + "," + field.getLongName() + "," + option);
        if (top instanceof CompositeView) {
            Iterator it = ((CompositeView) top).getViews().iterator();
            while (it.hasNext()) {
                View view = (View) it.next();
                if (view instanceof TabsView) {
                    String[] categories = GuiAC.getCategories(field);
                    if (categories != null && categories.length > 0) {
                        ((TabsView) view).select(categories[0]);
                        setFocus(
                            ((CompositeView) view).getView(categories[0]),
                            field,
                            option);
                    }
                } else {
                    setFocus(view, field, option);
                }
            }
        } else if (top instanceof FieldEditor) {
            FieldEditor editor = (FieldEditor) top;
            if (editor.getField().equals(field)) {
                editor.getContext().getCustomizedView().requestFocus();
                editor.onSetFocus(option);
            }
        }
    }

    /**
     * Used only to test if a CompositeView contains the view that we
     * are about to build.
     */
    /*
      static class DummyView {
      String type;
      public void setType(String type) {
      this.type = type;
      }
      public String getType() {
      return type;
      }

      Object[] parameters;
      public void setParameters(Object[] parameters) {
      this.parameters = parameters;
      }
      public Object[] getParameters() {
      return parameters;
      }
      public boolean equals(Object o) {
      return o instanceof ViewIdentity
      && ((ViewIdentity) o).getType().equals(type)
      && Arrays.equals(((ViewIdentity) o).getParameters(), parameters);
      }
      public int hashCode() {
      return type.hashCode() ^ parameters.hashCode();
      }

      public DummyView(String type, Object[] parameters) {
      this.type = type;
      this.parameters = parameters;
      }
      }
    */

    /**
     * Create a view for an object and display it if it is not already
     * displayed.
     *
     * @return the new view or the old one
     */
    View invalidatePane(
        ViewFactory factory,
        DisplayContext context,
        CompositeView panel,
        String viewType,
        String[] viewParams,
        Object selectedObject,
        Object extraInfo) 
    {
        logger.debug("invalidatePane("+panel+","
            +viewType+Arrays.asList(viewParams)+ ","+selectedObject+")");
        Collection comps = panel.getViews();
        View view = null;
        // close and remove the currently opened view in the panel
        Object[] parameters;
        if (selectedObject != null) {
            parameters = ExtArrays.add(selectedObject, viewParams);
        } else {
            parameters = viewParams;
        }
        if (!panel.containsView(viewType, parameters)) {
            logger.debug("new view " + viewType + ", extraInfo=" + extraInfo);
            view =
                factory.createView(
                    "target[?]",
                    viewType,
                    parameters,
                    context);
            if (extraInfo instanceof CollectionPosition &&
                GuiAC.hasSetNavBar(
                    context.getCustomizedView().getCustomizedGUI(),
                    ((CollectionPosition)extraInfo).getCollection())) 
            {
                view.setParentView(
                    view =
                    factory.createView(
                        "collectionItemView",
                        "CollectionItemView",
                        new Object[] {
                            view,
                            extraInfo,
                            viewType,
                            viewParams,
                            null },
                        context));
            }
            logger.debug("new view CREATED");
            panel.addView(view, GuiAC.toString(selectedObject));

        } else {
            Iterator i = comps.iterator();
            while (i.hasNext()) {
                view = (View) i.next();
                if (view.equalsView(viewType,parameters)) {
                    return view;
                }
            }
            return null;
        }

        return view;
    }

    /**
     * Initialize an autocreated object by setting 
     */
    public static void initAutocreatedObject(
        Object created,
        Object substance,
        FieldItem role) 
    {
        FieldItem oppositeRole =
            (FieldItem) role.getAttribute(RttiAC.OPPOSITE_ROLE);
        logger.debug("oppositeRole = " + oppositeRole);
        if (oppositeRole != null) {
            RoleWrapper.disableRoleUpdate(oppositeRole);
            try {
                if (oppositeRole instanceof CollectionItem) {
                    ((CollectionItem) oppositeRole).addThroughAdder(
                        created,
                        substance);
                } else {
                    oppositeRole.setThroughWriter(created, substance);
                }
            } catch (Exception e) {
                logger.error(
                    "initAutocreatedObject(created=" + created
                    + ",substance=" + substance
                    + "role=" + role
                    + "): " + e);
            } finally {
                RoleWrapper.enableRoleUpdate(oppositeRole);
            }
        }
    }

    /**
     * Upcalled when a tree node is selected.
     *
     * @param context the display context
     * @param node the selected tree node
     * @param forceView if true, the subtance of the node is opened in
     * a new window 
     */
    public void onNodeSelection(
        DisplayContext context,
        AbstractNode node,
        boolean forceView) 
    {
        logger.debug("onNodeSelection " + node);
        AbstractNode parentNode = (AbstractNode) node.getParent();
        if (parentNode != null) {
            // recursively send the event for the parent nodes
            onNodeSelection(context, parentNode, false);
        }
        if (node instanceof ObjectNode) {
            onSelection(
                context,
                ((ObjectNode) node).getRelation(),
                node.getUserObject(),
                null,
                null,
                forceView);
        } else if (node instanceof RootNode) {
            onSelection(
                context,
                null,
                node.getUserObject(),
                null,
                null,
                forceView);
        }
    }

    /**
     * Upcalled when a direct invocation is performed on an object (no
     * parameters will be asked by the GUI).
     *
     * @param context the display context
     * @param substance the object that holds the method
     * @param method the method to invoke
     * @param parameters the parameters of the method 
     */
    public void onInvokeDirect(
        DisplayContext context,
        Object substance,
        AbstractMethodItem method,
        Object[] parameters) 
    {
        Collaboration.get().addAttribute(GuiAC.DISPLAY_CONTEXT, context);
        method.invoke(substance, parameters);
    }

    /**
     * Upcalled when an invocation is performed on an object.
     *
     * @param context the display context
     * @param invoke
     * @return the thread the method was invoked in 
     *
     * @see #onInvoke(DisplayContext,InvokeEvent,String[],Object[])
     * @see #onInvoke(DisplayContext,InvokeEvent,boolean,String[],Object[])
     */
    public InvokeThread onInvoke(
        DisplayContext context,
        InvokeEvent invoke)
    {
        return onInvoke(context, invoke, null, null);
    }

    /**
     * Upcalled when an invocation is performed on an object. 
     *
     * @param context the display context
     * @param invoke 
     * @param attrNames the contextual attributes names to pass
     * @param attrValues the contextual attributes values 
     * @return the thread the method was invoked in 
     *
     * @see #onInvoke(DisplayContext,InvokeEvent)
     * @see #onInvoke(DisplayContext,InvokeEvent,boolean,String[],Object[])
     */
    public InvokeThread onInvoke(
        DisplayContext context,
        InvokeEvent invoke,
        String[] attrNames,
        Object[] attrValues)
    {
        return onInvoke(context, invoke, true, attrNames, attrValues);
    }

    /**
     * Invoke a method in the general case. Sets the necessary
     * attributes in the context. The method is invoked in a new
     * thread.
     *
     * @param context the display context
     * @param invoke
     * @param attrNames the contextual attributes names to pass
     * @param attrValues the contextual attributes values 
     * @return the thread the method was invoked in 
     *
     * @see #onInvoke(DisplayContext,InvokeEvent)
     * @see #onInvoke(DisplayContext,InvokeEvent,String[],Object[])
     */
    public InvokeThread onInvoke(
        DisplayContext context,
        InvokeEvent invoke,
        boolean askFormParameters,
        String[] attrNames,
        Object[] attrValues)
    {
        logger.debug("onInvoke(" + context + "," + invoke + ")");
        CustomizedDisplay display = context.getDisplay();
        if (attrNames == null)
            attrNames = ExtArrays.emptyStringArray;
        if (attrValues == null)
            attrValues = ExtArrays.emptyStringArray;
        Class[] parameterTypes = invoke.getMethod().getParameterTypes();
        int parametersLeft = parameterTypes.length;
        Object[] parameters = invoke.getParameters();
        if (parameters == null) {
            parameters = new Object[parameterTypes.length];
        } else if (parameters.length < parameterTypes.length) {
            // If there are not enough parameters,
            // we assume the first ones are missing
            parametersLeft -= parameters.length;
            Object[] tmp = new Object[parameterTypes.length];
            System.arraycopy(
                parameters,
                0,
                tmp,
                parameterTypes.length - parameters.length,
                parameters.length);
            parameters = tmp;
        }
        if (parameters.length > 0
            && parameterTypes[0] == DisplayContext.class) {
            parametersLeft--;
            parameters[0] = context;
        }
        invoke.setParameters(parameters);
        // Get the session id from the current context
        Object sid = Collaboration.get().getAttribute(SessionAC.SESSION_ID);
        if (parametersLeft == 0) {
            logger.debug("Invoking " + invoke);
            String[] names = new String[2 + attrNames.length];
            Object[] values = new Object[2 + attrNames.length];
            names[0] = GuiAC.DISPLAY_CONTEXT;
            values[0] = context;
            names[1] = SessionAC.SESSION_ID;
            values[1] = sid;
            System.arraycopy(attrNames, 0, names, 2, attrNames.length);
            System.arraycopy(attrValues, 0, values, 2, attrNames.length);
            return 
                InvokeThread.run(
                    invoke,
                    null,
                    null,
                    names,
                    values);
        } else {
            //new CallingBox( this, substance, method );
            logger.debug("Invoking " + invoke +
                " (ask for parameters is on, "
                + parametersLeft + " parameters left)");

            String[] names = new String[4 + attrNames.length];
            Object[] values = new Object[4 + attrNames.length];
            names[0] = GuiAC.DISPLAY_CONTEXT;
            values[0] = context;
            names[1] = SessionAC.SESSION_ID;
            values[1] = sid;
            names[2] = GuiAC.ASK_FOR_PARAMETERS;
            names[3] = GuiAC.INVOKED_METHOD;
            // Do not ask for parameters if all values are not null
            if (ExtArrays.indexOf(parameters, null) != -1 && askFormParameters) {
                values[2] = invoke.getMethod().getConcreteMethod();
                values[3] = invoke.getMethod();
            }

            System.arraycopy(attrNames, 0, names, 4, attrNames.length);
            System.arraycopy(attrValues, 0, values, 4, attrNames.length);
            return 
                InvokeThread.run(
                    invoke,
                    null,
                    null,
                    names,
                    values);
        }
    }

    /**
     * Invoke a method and waits for the result (and returns it).
     *
     * @param context the display context
     * @param invoke the method invocation to perform
     */
    public Object onInvokeSynchronous(
        DisplayContext context,
        InvokeEvent invoke)
    {
        logger.debug("onInvokeSynchronous(" + context + "," + invoke + ")");
        CustomizedDisplay display = context.getDisplay();
        Class[] parameterTypes = invoke.getMethod().getParameterTypes();
        int parametersLeft = parameterTypes.length;
        Object[] parameters = invoke.getParameters();
        if (parameters == null) {
            parameters = new Object[parameterTypes.length];
        } else if (parameters.length < parameterTypes.length) {
            // If there are not enough parameters,
            // we assume the first ones are missing
            parametersLeft -= parameters.length;
            Object[] tmp = new Object[parameterTypes.length];
            System.arraycopy(
                parameters,
                0,
                tmp,
                parameterTypes.length - parameters.length,
                parameters.length);
            parameters = tmp;
        }
        if (parameters.length > 0
            && parameterTypes[0] == DisplayContext.class) {
            parametersLeft--;
            parameters[0] = context;
        }

        String[] names;
        Object[] values;

        // Get the session id from the current context
        Object sid = Collaboration.get().getAttribute(SessionAC.SESSION_ID);
        if (parametersLeft == 0) {
            logger.debug("Invoking " + invoke);
            names =
                new String[] { GuiAC.DISPLAY_CONTEXT, SessionAC.SESSION_ID };
            values = new Object[] { context, sid };
        } else {
            //new CallingBox( this, substance, method );
            logger.debug("Invoking " + invoke
                + " (ask for parameters is on, "
                + parametersLeft + " parameters left)");
            names =
                new String[] {
                    GuiAC.DISPLAY_CONTEXT,
                    SessionAC.SESSION_ID,
                    GuiAC.ASK_FOR_PARAMETERS };
            values = new Object[] { context, sid, invoke.getMethod() };
        }

        invoke.setParameters(parameters);
        InvokeThread thread =
            new InvokeThread(
                invoke,
                null,
                null,
                names,
                values);
        thread.start();
        return thread.getReturnValue();
    }

    /**
     * This method is upcalled when an object is added to a collection.
     *
     * @param context the display context
     * @param add the add that triggered the event
     */
    public void onAddToCollection(
        DisplayContext context,
        AddEvent add)
    {
        onAddToCollection(context, add, false);
    }

    /**
     * This method is upcalled when an object is added to a collection.
     *
     * @param context the display context
     * @param add the add that triggered the event
     * @param noAutoCreate if true, does not auto create the object to
     * add, whatever the configuration for the collection.
     */
    public void onAddToCollection(
        DisplayContext context,
        AddEvent add,
        boolean noAutoCreate)
    {
        logger.debug("onAddToCollection "+add
                     + "; noAutoCreate=" + noAutoCreate);
        CollectionItem collection = add.getCollection();
        MethodItem addMethod = collection.getAdder();
        Collaboration collab = Collaboration.get();
        FieldItem oppositeRole =
            (FieldItem) collection.getAttribute(RttiAC.OPPOSITE_ROLE);
        loggerAssoc.debug("opposite_role = " + oppositeRole);
        if (oppositeRole instanceof CollectionItem) {
            loggerAssoc.debug("Ignoring collection oppositeRole " + oppositeRole);
            oppositeRole = null;
        }

        try {
            if (addMethod != null) {
                logger.debug("Invoking add method " + addMethod.getName()
                    + " on collection owner");

                if (noAutoCreate) {
                    ClassItem type =
                        ClassRepository.get().getClass(
                            addMethod.getParameterTypes()[0]);
                    onInvoke(
                        context,
                        new InvokeEvent(
                            add.getSource(),
                            add.getSubstance(),
                            addMethod),
                        new String[] { GuiAC.NO_AUTO_CREATE, GuiAC.VIEW },
                        new Object[] { type, this });
                } else {
                    onInvoke(
                        context,
                        new InvokeEvent(
                            add.getSource(),
                            add.getSubstance(),
                            addMethod),
                        new String[] { GuiAC.VIEW, GuiAC.OPPOSITE_ROLE },
                        new Object[] { this, oppositeRole });
                }
            } else {
                logger.debug("No adder for " + collection);
                context.getDisplay().refresh();
				/*
				  getMethod = collection.getGetter();
				  if (getMethod!=null)
				  MethodItem method = null;
				  Object c;
				  try {
				  c = getMethod.invoke(substance,ExtArrays.emptyObjectArray);
				  } catch (Exception e) {
				  logger.error("Getting collection with "+getMethod.getName()+
				  "failed : "+e);
				  return;
				  }
				  ClassItem cl = ClassRepository.get().getClass(c.getClass());
				  if (collection.isMap())
				  method = cl.getMethod("put(Object,Object)");
				  else
				  method = cl.getMethod("add(Object)");
				  Log.trace("gui","Invoking "+method.getName()+" on collection itself");
				  GuiAC.invoke((Display)parent,method,c,collection);
				  }
				*/
            }
        } finally {
            collab.removeAttribute(GuiAC.OPPOSITE_ROLE);
        }
    }

    /**
     * This method is upcalled when an object is removed from a collection.
     *
     * @param context the display context
     * @param remove
     * @param askFormParameters wether to to display an input box for
     * the parameters of the remover method
     */
    public void onRemoveFromCollection(
        DisplayContext context,
        RemoveEvent remove,
        boolean askFormParameters)
    {
        CollectionItem collection = remove.getCollection();
        Object selected = remove.getRemoved();
        MethodItem remover = collection.getRemover();
        if (remover != null) {
            logger.debug("Invoking remove method "
                + remover.getName()
                + " on collection owner");

            if (collection.isMap() && selected instanceof Map.Entry) {
				// We don't want to call the remover with an entry as the
				// parameter
                selected = ((Map.Entry) selected).getKey();
            }

            try {
                if (selected != null)
                    onInvoke(
                        context,
                        new InvokeEvent(
                            remove.getSource(),
                            remove.getSubstance(),
                            remover,
                            new Object[] {selected}),
                        false,
                        ExtArrays.emptyStringArray,
                        ExtArrays.emptyObjectArray);
                else
                    onInvoke(
                        context,
                        new InvokeEvent(
                            remove.getSource(),
                            remove.getSubstance(),
                            remover,
                            new Object[] {selected}),
                        askFormParameters,
                        ExtArrays.emptyStringArray,
                        ExtArrays.emptyObjectArray);

				/**
				 * Close all views where the removed element is displayed
				 */
                CustomizedView customizedView = context.getCustomizedView();
                if (customizedView != null) {
                    CustomizedGUI customized =
                        customizedView.getCustomizedGUI();
                    List targets = customized.getFieldTargets(collection);

                    if (targets != null) {
                        Iterator it = targets.iterator();
                        while (it.hasNext()) {
                            Target target = (Target) it.next();
                            CompositeView targetView =
                                (CompositeView) customizedView.getView(
                                    target.panePath);
                            if (targetView == null)
                                continue;

                            Object[] parameters = new Object[] {selected};
                            Iterator it2 = targetView.getViews().iterator();
                            while (it2.hasNext()) {
                                View view = (View) it2.next();

                                View view2 = null;
                                if (view
                                    instanceof AbstractCollectionItemView) {
                                    view2 = view;
                                    view =
                                        ((AbstractCollectionItemView) view)
                                        .getView();
                                }

                                if (view.equalsView(view.getType(),parameters)) {
                                    if (view2 != null) {
                                        view.close(true);
                                        view = view2;
                                    }
                                    ((CompositeView)view.getParentView())
                                        .removeView(view,true);
                                    // Should removeView() do the close() ???
                                    view.close(true);

                                    it2 = targetView.getViews().iterator();
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.error(
                    "failed to remove "
                    + ((selected != null) ? selected : "element")
                    + " from collection");
                e.printStackTrace();
            }
        } else {
            context.getDisplay().showError(
                "Cannot Remove",
                "No remover method available for collection " + collection);
            // TODO: handling of arrays
        }
    }

    /**
     * This method is upcalled when an object has to be created in an
     * object chooser.
     *
     * @param context the display context
     * @param cli the class of the object to create
     * @param substance
     * @param field a field to which the created object will be "added". May be null.
     */
    public Object onCreateObject(
        DisplayContext context,
        ClassItem cli,
        Object substance,
        FieldItem field)
    {
        logger.debug("onCreateObject(" + cli.getName() + "," + field + ")");

        Vector classes = new Vector(cli.getChildren());
        classes.add(cli);

        Object newInstance = null;
        try {
            
            Collaboration collab = Collaboration.get();

            Object fieldChoice = field!=null ? field.getAttribute(GuiAC.FIELD_CHOICE) : null;
            if (fieldChoice instanceof CollectionItem) {
                CollectionItem choice = (CollectionItem)fieldChoice;
                collab.addAttribute(GuiAC.AUTOCREATE_REASON, 
                                    new Interaction(null,
                                                    (Wrappee)choice.getSubstance(substance),
                                                    choice.getAdder(),new Object[1]));
                try {
                    newInstance = cli.newInstance();
                } finally {
                    collab.removeAttribute(GuiAC.AUTOCREATE_REASON);
                }
                onInvokeDirect(
                    context,
                    choice.getSubstance(substance),
                    choice.getAdder(),
                    new Object[] {newInstance});
            } else {
                newInstance = cli.newInstance();
            }

            if (substance != null && field != null) {
                MethodItem init = GuiAC.getInitiliazer(field);
                if (init != null) {
                    init.invoke(substance, new Object[] { newInstance });
                }
            }

            FieldItem oppositeRole = setOppositeRole(field);
            //collab.addAttribute(GuiAC.EMBEDDED_EDITORS, Boolean.TRUE);
            collab.addAttribute(GuiAC.AUTO_CREATION, Boolean.TRUE);
            try {
                boolean ok =
                    context.getDisplay().showModal(
                        newInstance,
                        "Object",
                        new String[] {GuiAC.AUTOCREATE_VIEW},
                        "New " + NamingConventions.getShortClassName(cli),
                        "Fill the needed information and validate.",
                        context.getWindow(),
                        true,
                        true,
                        false);

                if (!ok) {
                    logger.debug("Creation of " + cli.getName() + " cancelled");
                    ObjectRepository.delete((Wrappee) newInstance);
                    newInstance = null;
                }
            } finally {
                if (oppositeRole != null)
                    collab.removeAttribute(GuiAC.OPPOSITE_ROLE);
				//collab.removeAttribute(GuiAC.EMBEDDED_EDITORS);
                collab.removeAttribute(GuiAC.AUTO_CREATION);
            }
        } catch (Exception e) {
            logger.error("onCreateObject("+cli.getName()+","+
                         substance+","+field+") failed",e);
        }
        return newInstance;
    }

    public void onDropObject(
        DisplayContext context,
        Object target,
        Object droppedObject,
        Object source,
        boolean copy)
    {
        logger.debug("onDropObject(" + target + "," + droppedObject + ")");
        ClassItem cli = ClassRepository.get().getClass(target);
        Class objectType = droppedObject.getClass();
        CollectionItem[] cols = cli.getCollections();
        for (int i = 0; i < cols.length; i++) {
            CollectionItem c = cols[i];
            loggerDnd.debug("checking collection " + c);
            MethodItem adder = c.getAdder();
            if (adder != null) {
                loggerDnd.debug("types(" + adder.getParameterTypes()[0] + "," + objectType + ")");
                if (adder.getParameterTypes()[0] == objectType) {
                    if (!copy && GuiAC.isRemovable(c) && GuiAC.isAddable(c)) {
                        loggerDnd.debug("moving object...");
                        c.removeThroughRemover(source, droppedObject);
                        c.addThroughAdder(target, droppedObject);
                    } else if (
                        copy && GuiAC.isRemovable(c) && GuiAC.isAddable(c)) {
                        loggerDnd.debug("copying object...");
                        Wrapping.clone(droppedObject);
                        c.addThroughAdder(target, droppedObject);
                    }
                }
            }
        }
    }

    public void fieldUpdated(
        Object substance,
        FieldItem field,
        Object value,
        Object param)
    {
        CompositeView view = (CompositeView) param;
        logger.debug("rebuilding view " + view);
        view.removeAllViews(false);
        GenericFactory.fillObjectView(
            view,
            ClassRepository.get().getClass(substance),
            "default",
            substance);
    }

    public static FieldItem setOppositeRole(FieldItem field) {
        if (field!=null) {
            FieldItem oppositeRole =
                (FieldItem)field.getAttribute(RttiAC.OPPOSITE_ROLE);
            if (oppositeRole instanceof CollectionItem) {
                loggerAssoc.debug("Ignoring collection oppositeRole of "+
                                  field.getLongName() + ": "+oppositeRole);
                return null;
            } else {
                loggerAssoc.debug("opposite_role of "+field.getLongName()+" = " + oppositeRole);
                Collaboration.get().addAttribute(GuiAC.OPPOSITE_ROLE, oppositeRole);
                return oppositeRole;
            }
        } else {
            return null;
        }
    }
}
