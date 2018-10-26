/*
  Copyright (C) 2002-2004 Renaud Pawlak <renaud@aopsys.com>

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 2 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  USA */

package org.objectweb.jac.aspects.gui;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.Collaboration;
import org.objectweb.jac.core.rtti.AbstractMethodItem;
import org.objectweb.jac.core.rtti.ConstructorItem;
import org.objectweb.jac.core.rtti.MethodItem;
import org.objectweb.jac.util.Exceptions;

/**
 * This class allows the programmer to invoke a given method in a new
 * thread.
 *
 * <p>JAC programmers should use JAC to pass some attibutes of the
 * current thread to the new thread.
 *
 * <p>Typical use:
 * <ul><pre>
 * InvokeThread.run( myObject, "myMethodName",
 *                   new Object[] { arg0, ...},
 *                   new String[] { attrName0, ... },
 *                   new Object[] { attrValue0, ... },
 *                   new String[] { lattrName0, ... },
 *                   new Object[] { lattrValue0, ... } );
 * </pre></ul> */

public class InvokeThread extends Thread {
    static Logger logger = Logger.getLogger("gui.threads");
    static Logger loggerInput = Logger.getLogger("gui.input");

    InvokeEvent invoke;
    Object returnValue;
    String[] attrNames;
    Object[] attrValues; 
    String[] lattrNames;
    Object[] lattrValues;
    Collaboration parentCollaboration;
    boolean showResult = true;

    /**
     * Runs a method in a new thread and sets a display for this
     * thread.
     *
     * @param invoke the invocation to perform
     * @param attrNames the attribute names to set into the new thread
     * collaboration
     * @param attrValues the values of these attributes
     * @param lattrNames the local attribute names to set into the new
     * thread collaboration
     * @param lattrValues the values of these local attributes 
     */
    public static InvokeThread run(InvokeEvent invoke,                                   
                                   String[] attrNames, Object[] attrValues,
                                   String[] lattrNames, Object[] lattrValues) {
        InvokeThread ret = new InvokeThread(invoke);
        ret.parentCollaboration = Collaboration.get();
        ret.attrNames = attrNames;
        ret.attrValues = attrValues;
        ret.lattrNames = lattrNames;
        ret.lattrValues = lattrValues;
        ret.start();
        return ret;
    }

    /**
     * Runs a method in a new thread and sets a display for this
     * thread. Do not show any results on the display.
     *
     * @param invoke the invocation to perform
     * @param attrNames the attribute names to set into the new thread
     * collaboration
     * @param attrValues the values of these attributes
     * @param lattrNames the local attribute names to set into the new
     * thread collaboration
     * @param lattrValues the values of these local attributes 
     */
    public static 
    InvokeThread quietRun(InvokeEvent invoke,
                          String[] attrNames, Object[] attrValues,
                          String[] lattrNames, Object[] lattrValues) 
    {
        InvokeThread ret = new InvokeThread(invoke);
        ret.parentCollaboration = Collaboration.get();
        ret.attrNames = attrNames;
        ret.attrValues = attrValues;
        ret.lattrNames = lattrNames;
        ret.lattrValues = lattrValues;
        //        ret.showResult = false;
        ret.start();
        return ret;
    }

    /**
     * Runs a method in a new thread.
     *
     * @param invoke the invocation to perform
     */
    public static InvokeThread run(InvokeEvent invoke) {
        InvokeThread ret = new InvokeThread(invoke);
        ret.start();
        return ret;
    }

    /**
     * Creates a new thread that will invoke a method when started.
     *
     * <p>The programmer should use the static <code>run</code>
     * methods.
     *
     * @param invoke the invocation to perform
     */
    public InvokeThread(InvokeEvent invoke) {
        this.invoke = invoke;
    }

    /**
     * Creates a new thread that will invoke a method when started.
     *
     * <p>The programmer should use the static <code>run</code>
     * methods.
     *
     * @param invoke the invocation to perform
     * @param attrNames name of attributes to add to the context
     * before invoking the method (may be null)
     * @param attrValues values of the attributes (may be null)
     * @param lattrNames name of local attributes to add to the context (may be null)
     * before invoking the method (may be null)
     * @param lattrValues values of the local attributes (may be null)
     */
    public InvokeThread(InvokeEvent invoke,
                        String[] attrNames, Object[] attrValues,
                        String[] lattrNames, Object[] lattrValues) {
        this.invoke = invoke;
        this.parentCollaboration = Collaboration.get();
        this.attrNames = attrNames;
        this.attrValues = attrValues;
        this.lattrNames = lattrNames;
        this.lattrValues = lattrValues;
        this.showResult = false;
    }

    /**
     * Runs the thread (and invoke the method that was given to the
     * constructor with the right display in the collaboration).
     *
     * <p>Do not call this method directly. 
     */
    public void run() {
        Collaboration collab = Collaboration.get();
        Object[] parameters = invoke.getParameters();
        AbstractMethodItem method = invoke.getMethod();
        Object substance = invoke.getSubstance();

        logger.debug("invokeThread "+this+": "+invoke);
        if (parentCollaboration!=null) {
            Iterator it = parentCollaboration.attributeNames().iterator();
            while (it.hasNext()) {
                String attrName = (String)it.next();
                collab.addAttribute(
                    attrName,parentCollaboration.getAttribute(attrName));
            }         
            logger.debug("application = "+parentCollaboration.getCurApp());
            collab.setCurApp(parentCollaboration.getCurApp());
        }
        if (attrNames != null) {
            for (int i=0; i<attrNames.length; i++) {
                logger.debug("setting attribute " + attrNames[i] +
                          " to " + attrValues[i]);
                collab.addAttribute( 
                    attrNames[i], attrValues[i]);
            }
        }
        if (lattrNames != null) {
            for (int i=0; i<lattrNames.length; i++) {
                logger.debug("setting local attribute " + 
                          lattrNames[i] + " to " + lattrValues[i]);
                collab.addAttribute( 
                    lattrNames[i], lattrValues[i]);
            }
        }
        DisplayContext context = (DisplayContext)collab
            .getAttribute(GuiAC.DISPLAY_CONTEXT);

        CustomizedDisplay display = context.getDisplay();
        try {
            logger.debug("InvokeThread " + invoke);
         
            Class[] paramTypes = method.getParameterTypes();
            if (paramTypes.length != invoke.getParameters().length) 
                throw new RuntimeException("Wrong number of parameters ("+
                                           parameters.length+") for "+method);
            for (int i=0; i<parameters.length; i++) {
                if (parameters[i]==null) {
                    if (paramTypes[i] == float.class) {
                        method.setParameter(parameters,i,new Float(0.0));
                    } else if (paramTypes[i] == long.class) {
                        method.setParameter(parameters,i,new Long(0));
                    } else if (paramTypes[i] == double.class) {
                        method.setParameter(parameters,i,new Double(0.0));
                    } else if (paramTypes[i] == byte.class) {
                        method.setParameter(parameters,i,new Byte((byte)0));
                    } else if (paramTypes[i] == char.class) {
                        method.setParameter(parameters,i,new Character(' '));
                    } else if (paramTypes[i] == short.class) {
                        method.setParameter(parameters,i,new Short((short)0));
                    } else if (paramTypes[i] == int.class) {
                        method.setParameter(parameters,i,new Integer(0));
                    } else if (paramTypes[i] == boolean.class) {
                        method.setParameter(parameters,i,Boolean.FALSE);
                    }
                }
            }

            if (method instanceof ConstructorItem) {
                returnValue = ((ConstructorItem)method).newInstance(parameters);
            } else {
                returnValue = ((MethodItem)method).invoke(substance, parameters);
            }

            if (display != null) {
                display.onInvocationReturn(substance,method);
            }
            List hooks = (List)method.getAttribute(GuiAC.POST_INVOKE_HOOKS);
            if (hooks!=null) {
                Iterator i = hooks.iterator();
                while (i.hasNext()) {
                    AbstractMethodItem hook = (AbstractMethodItem)i.next();
                    try {
                        loggerInput.debug("Invoking post hook "+hook.getName());
                        hook.invoke(
                            null,
                            new Object[] {invoke});
                    } catch (Exception e) {
                        loggerInput.error("Post invoke hook for "+
                                          substance+"."+
                                          method.getFullName()+" failed",e);
                    }
                }
            }

            if (method.getType() != void.class) {
                if (display != null && showResult) {

                    if (collab.getAttribute(GuiAC.OPEN_VIEW)!=null) {
                        display.openView(returnValue);
                    } else {
                        if(method.getAttribute(GuiAC.SMALL_TARGET_CONTAINER)!=null) {
                            EventHandler.get().onSelection(context,method,returnValue,null,null,false);
                        } else if (returnValue instanceof HandlerResult) {
                            EventHandler.get().handleResult(context,(HandlerResult)returnValue);
                        } else {
                            display.show(returnValue);
                        }
                    }
                }
            } else {
                if (display != null && showResult) 
                    display.refresh();
            }
        } catch (Exception e) {
            Throwable te = Exceptions.getTargetException(e);
            logger.debug(this+" TargetException is "+te);
            if (te instanceof TimeoutException) {
                logger.debug(this+" Timeout");
                DialogView dialog = ((TimeoutException)te).getDialog();
                display.closeWindow(dialog,false);
                display.addTimedoutDialog(dialog);
            } else if (display != null && showResult) {
                if (!(te instanceof org.objectweb.jac.util.VoidException))
                    display.showModal(te,"Error","",context.getWindow(),false,false,true);
                else
                    display.refresh();
            }
        }
        logger.debug("invokeThread done "+this+": "+
                     substance+"."+method+Arrays.asList(parameters));
    }

    Object getReturnValue() {
        return returnValue;
    }
}

class NotAvailableException extends Exception {}
