/*
  Copyright (C) 2001-2004 Renaud Pawlak <renaud@aopsys.com>
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

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import org.aopalliance.intercept.ConstructorInvocation;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.ACManager;
import org.objectweb.jac.core.AspectComponent;
import org.objectweb.jac.core.Collaboration;
import org.objectweb.jac.core.Display;
import org.objectweb.jac.core.Interaction;
import org.objectweb.jac.core.Wrappee;
import org.objectweb.jac.core.Wrapper;
import org.objectweb.jac.core.rtti.AbstractMethodItem;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.core.rtti.ConstructorItem;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.MethodItem;
import org.objectweb.jac.core.rtti.NamingConventions;
import org.objectweb.jac.util.VoidException;

/**
 * This wrapper asks the user the parameters of the invoked method if
 * the attribute <code>Gui.askForParameters</code> is defined in the
 * current collaboration.
 *
 * @see org.objectweb.jac.core.Display#showInput(Object,AbstractMethodItem,Object[])
 * @see InputSequence
 */

public class InputWrapper extends Wrapper {
    static Logger logger = Logger.getLogger("gui.input");

    public InputWrapper(AspectComponent ac) {
        super(ac);
    }

    /**
     * Returns true if all classe of an array are Wrappee classes.
     */
    boolean areAllWrappees(Class[] types) {
        for (int i=0; i<types.length; i++) {
            if (!Wrappee.class.isAssignableFrom(types[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Calls the <code>Display.showInput</code> method on the current
     * display.
     *
     * @return the value returned by the wrapped method
     * @see org.objectweb.jac.core.Display#showInput(Object,AbstractMethodItem,Object[]) 
     */
    public Object askForParameters(Interaction interaction)
        throws InputFailedException, InputCanceledException 
    {
        boolean proceed = true;

        AbstractMethodItem m =
            (AbstractMethodItem) attr(GuiAC.ASK_FOR_PARAMETERS);
        AbstractMethodItem method = (AbstractMethodItem) interaction.method;

        logger.debug("Gui.askForParameters=" + (m!=null ? m.getLongName() : "null"));
        logger.debug("current method=" + (method!=null ? method.getLongName(): "null"));

        if (m != null && method != null) {
            logger.debug("actual classes: "
                    + ((ClassItem) m.getParent()).getActualClass() + ","
                    + ((ClassItem) method.getParent()).getActualClass());
        }
        Display display = null;
        // DIRTY HACK
        if (m != null && m.getLongName().equals(method.getLongName())) {
            logger.debug("Entering parameters asking for "
                         + interaction.wrappee + " : " + method.getLongName()
                         + " askForParameters = " + m.getLongName()
                         + " autoCreate=" + attr(GuiAC.AUTO_CREATE)
                         + " view=" + attr(GuiAC.VIEW));

            AbstractMethodItem invoked = (AbstractMethodItem)attr(GuiAC.INVOKED_METHOD);
            if (invoked!=null)
                method = invoked;
            DisplayContext context =
                (DisplayContext) attr(GuiAC.DISPLAY_CONTEXT);
            display = context.getDisplay();

            attrdef(GuiAC.ASK_FOR_PARAMETERS, null);

            if (display != null) {
                MethodItem interactionHandler =
                    GuiAC.getInteractionHandler(method);
                if (interactionHandler != null) {
                    logger.debug("invoking interactionHandler " + interactionHandler);
                    proceed =
                        ((Boolean) interactionHandler
                            .invokeStatic(
                                new Object[] { interaction, context }))
                            .booleanValue();
                } else {

                    Class[] paramTypes = method.getParameterTypes();
                    // automatic created parameter handling
                    if (attr(GuiAC.NO_AUTO_CREATE) == null
                        && (GuiAC.isAutoCreateParameters(method)
                            || attr(GuiAC.AUTO_CREATE) != null
                            || (method.isAdder()
                                && GuiAC.isAutoCreate(
                                    method.getAddedCollection()))
                            || (method.isSetter() 
                                && GuiAC.isAutoCreate(
                                    method.getSetField())))
                        && areAllWrappees(paramTypes)) {
                        proceed =
                            autoCreate(
                                display,
                                context,
                                method,
                                interaction,
                                paramTypes);
                    } else {
                        proceed =
                            askingSequence(
                                display,
                                context,
                                method,
                                interaction,
                                paramTypes);
                    }
                }
            } else {
                logger.debug("No display available");
                throw new InputFailedException("No display available");
            }
            logger.debug("proceed = " + proceed);
            logger.debug("args=" + Arrays.asList(interaction.args));
        }

        if (proceed) {
            Object result = proceed(interaction);
            return result;
        } else {
            throw new InputCanceledException(method);
        }

    }

    protected boolean autoCreate(
        Display display,
        DisplayContext context,
        AbstractMethodItem method,
        Interaction interaction,
        Class[] paramTypes) 
    {
        logger.debug("in parameter autoCreation");
        if (method.isAdder()
             && GuiAC.isAutoCreate(
                 method.getAddedCollection())) {
            EventHandler.setOppositeRole(method.getAddedCollection());
        } else if (method.isSetter() 
                   && GuiAC.isAutoCreate(
                       method.getSetField())) {
            EventHandler.setOppositeRole(method.getSetField());
        }
        attrdef(GuiAC.AUTO_CREATION, Boolean.TRUE);
        //attrdef(GuiAC.EMBEDDED_EDITORS, Boolean.TRUE);
        ACManager.getACM().beforeWrappeeInit(null);
        boolean proceed = true;
        try {
            boolean ok = true;
            for (int i=0; i<paramTypes.length; i++) {

                // do not create if already filled in!
                Object arg = method.getParameter(interaction.args,i);
                if (arg != null)
                    continue;

                //if( paramTypes[i] instanceof Wrappee ) {
                if (!Modifier.isPublic(paramTypes[i].getModifiers())) {
                    logger.error(
                        "cannot instantiate non public class "
                            + paramTypes[i].getName());
                }
                Collaboration collab = Collaboration.get();
                collab.addAttribute(GuiAC.AUTOCREATE_REASON, interaction);
                try {
                    method.setParameter(interaction.args,i,newInstance(paramTypes[i], display));
                    arg = method.getParameter(interaction.args,i);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    collab.removeAttribute(GuiAC.AUTOCREATE_REASON);
                }

                if (arg != null) {
                    // Use initializer if any
                    MethodItem init = null;
                    if (method.isAdder()) {
                        init =
                            GuiAC.getInitiliazer(
                                method.getAddedCollections()[0]);
                    } else if (method.isSetter()) {
                        init = GuiAC.getInitiliazer(method.getSetField());
                    }
                    if (init != null) {
                        logger.debug("Initializing new instance of "
                                + paramTypes[i].getName()
                                + " with "
                                + init.getFullName());
                        init.invoke(
                            interaction.wrappee,
                            new Object[] { arg });
                    }

                    ok =
                        display.showModal(
                            arg,
                            "Object",
                            new String[] { GuiAC.AUTOCREATE_VIEW },
                            interaction.args.length > 1
                                ? GuiAC.getLabel(method)
                                    + " ("
                                    + NamingConventions.getShortClassName(
                                        arg.getClass())
                                    + ")"
                                : GuiAC.getLabel(method),
                            "Fill the needed information and validate.",
                            context.getWindow(),
                            true,
                            true,
                            false);
                } else {
                    ok = false;
                }
                if (!ok) {
                    logger.debug("Input cancelled for " + interaction.method);
                    proceed = false;
                    break;
                } else {
                    if (attr(GuiAC.VIEW) != null) {
                        /* What was this supposed to do ???
                           Wrapping.invokeRoleMethod(
                           ((Wrappee)interaction.args[i]),"addView",
                           new Object[] {attr(GuiAC.VIEW)});
                        */
                    }
                    // if some field must be autocreated, we ask the user
                    // to input them (except if already filled)
                    // this should be reccursive but it is not implemented
                    // for the moment
                    ClassItem parameterClass =
                        ClassRepository.get().getClass(paramTypes[i]);
                    String[] autoCreatedFields =
                        (String[]) parameterClass.getAttribute(
                            GuiAC.AUTO_CREATED_STATE);
                    if (autoCreatedFields != null) {
                        for (int j=0; j<autoCreatedFields.length; j++) {
                            FieldItem f =
                                parameterClass.getField(autoCreatedFields[j]);
                            if (f != null
                                && f.getThroughAccessor(arg) == null) {
                                Object newInstance = null;
                                try {
                                    newInstance = f.getType().newInstance();
                                    ok =
                                        display.showModal(
                                            newInstance,
                                            "Object",
                                            new String[] {
                                                 GuiAC.AUTOCREATE_VIEW },
                                            "Creation of a new "
                                                + NamingConventions
                                                    .getShortClassName(
                                                    newInstance.getClass()),
                                            "You did not fill any "
                                                + f.getName()
                                                + ". "
                                                + "Fill the needed informations and validate.",
                                            context.getWindow(),
                                            true,
                                            true,
                                            false);
                                    if (ok) {
                                        f.setThroughWriter(
                                            arg,
                                            newInstance);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        } finally {
            attrdef(GuiAC.AUTO_CREATION, null);
            //attrdef(GuiAC.EMBEDDED_EDITORS, null);
            ACManager.getACM().afterWrappeeInit(null);
        }
        return proceed;
    }

    protected boolean askingSequence(
        Display display,
        DisplayContext context,
        AbstractMethodItem method,
        Interaction interaction,
        Class[] paramTypes)
        throws InputFailedException, InputCanceledException 
    {
        boolean proceed = true;
        Object[] askingSequence =
            (Object[]) method.getAttribute(GuiAC.ASKING_SEQUENCE);
        boolean allCreated = false;
        if (paramTypes.length > 0) {
            if (askingSequence != null) {
                logger.debug("input sequence found");
                allCreated = true;
                for (int i=0; i<paramTypes.length; i++) {
                    if (askingSequence[i].equals("autoCreate")) {
                        method.setParameter(
                            interaction.args,
                            i,
                            create(
                                ClassRepository.get().getClass(paramTypes[i]),
                                display));
                    } else {
                        allCreated = false;
                    }
                }
            }
            if (!allCreated) {
                if (!display.fillParameters(method, interaction.args)) {
                    logger.debug("asking for parameters");
                    GuiAC.pushGraphicContext(method);
                    try {
                        proceed =
                            display.showInput(
                                interaction.wrappee,
                                method,
                                interaction.args);
                    } finally {
                        GuiAC.popGraphicContext();
                    }
                    logger.debug("args=" + Arrays.asList(interaction.args));
                }
            } else {
                logger.debug("skipping asking for parameters");
                proceed = true;
            }
        }
        return proceed;
    }

    /**
     * This method performs all the inputs operations for an instance creation.
     *
     * @param classItem the class to instantiate
     * @param display the display to use 
     */
    public Object create(ClassItem classItem, Display display)
        throws InputFailedException, InputCanceledException 
    {
        Object newInstance = null;
        Collection constructors = classItem.getConstructors();
        Iterator it = constructors.iterator();
        ConstructorItem c;
        c = (ConstructorItem) it.next();
        if (it.hasNext()) {
            c = (ConstructorItem) it.next();
        }
        if (c.getParameterTypes().length > 0) {
            Object[] parameters = new Object[c.getParameterTypes().length];

            String inputSequenceName =
                (String) c.getAttribute("Gui.inputSequence");
            logger.debug("input sequence name = " + inputSequenceName);
            if (inputSequenceName == null) {
                display.showInput(null, c, parameters);
            } else {
                org.objectweb.jac.aspects.gui.InputSequence inputSeq = null;
                try {
                    inputSeq =
                        (org.objectweb.jac.aspects.gui.InputSequence) Class
                            .forName(inputSequenceName)
                            .getConstructor(
                                new Class[] {
                                    Display.class,
                                    AbstractMethodItem.class,
                                    Object[].class })
                            .newInstance(
                                new Object[] { display, c, parameters });
                } catch (Exception e) {
                    throw new InputFailedException("unabled to create input sequence");
                }
                if (!inputSeq.proceedInputs()) {
                    throw new InputCanceledException(c);
                }
            }

            try {
                newInstance = c.newInstance(parameters);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            try {
                newInstance = c.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return newInstance;
    }

    /**
     * Create a new instance of a class. If the class has known
     * subclasses, the user will be given the choice of the actual
     * class to instantiate.
     * @param cl the class to instantiate
     * @param display display to use for user interaction
     * @return a new instance of the class
     */
    public static Object newInstance(Class cl, Display display)
        throws InstantiationException, IllegalAccessException 
    {
        ClassItem cli = ClassRepository.get().getClass(cl);
        Collection subClasses = cli.getChildren();
        if (!subClasses.isEmpty() && Modifier.isAbstract(cl.getModifiers())) {
            ClassChooser chooser = new ClassChooser(cli);
            ClassItem classChooserClass =
                ClassRepository.get().getClass(ClassChooser.class);
            ClassItem[] choice = new ClassItem[1];
            boolean ok =
                display.showInput(
                    chooser,
                    classChooserClass.getMethod("setChoice"),
                    choice);
            if (!ok) {
                return null;
            } else {
                return choice[0].newInstance();
            }
        } else {
            return cl.newInstance();
        }
    }

    public void catchInputCanceled(InputCanceledException e) {
        logger.debug("catching exception: " + e.toString());
        attrdef(GuiAC.AUTO_CREATE, null);
        throw new VoidException();
    }

    public Object invoke(MethodInvocation invocation) throws Throwable {
        return askForParameters((Interaction) invocation);
    }

    public Object construct(ConstructorInvocation invocation)
        throws Throwable {
        return askForParameters((Interaction) invocation);
    }
}

class InputFailedException extends Exception {
    public InputFailedException() {
    }
    public InputFailedException(String msg) {
        super(msg);
    }
}
