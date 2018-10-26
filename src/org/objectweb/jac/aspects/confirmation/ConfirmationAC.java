/*
  Copyright (C) 2001-2003 Renaud Pawlak <renaud@aopsys.com>

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
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA */

package org.objectweb.jac.aspects.confirmation;

import org.aopalliance.intercept.ConstructorInvocation;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.objectweb.jac.aspects.gui.DisplayContext;
import org.objectweb.jac.aspects.gui.GuiAC;
import org.objectweb.jac.core.AspectComponent;
import org.objectweb.jac.core.Display;
import org.objectweb.jac.core.Interaction;
import org.objectweb.jac.core.Wrapper;
import org.objectweb.jac.core.rtti.AbstractMethodItem;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.core.rtti.NamingConventions;

/**
 * The confirmation aspect implementation (allows the user to add
 * confirmation popups before committing. */

public class ConfirmationAC
    extends AspectComponent
    implements ConfirmationConf 
{
    static final Logger logger = Logger.getLogger("confirmation");

    static String cancellationMessage = "invocation was cancelled by the user";
    public void confirm(String classes, String methods, String objects) {
        pointcut(
            objects,
            classes,
            methods,
            new ConfirmationWrapper(this),
            "catchCancellation");
    }

    public void confirm(String classes, String methods, String objects, String message) {
        pointcut(
            objects,
            classes,
            methods,
            new ConfirmationWrapper(this,message),
            "catchCancellation");
    }

    /**
     * A confirmation wrapper that wraps methods to show a confirmation
     * message box before actually performing the call. */

    public class ConfirmationWrapper extends Wrapper {

        String message;

        public ConfirmationWrapper(AspectComponent ac, String message) {
            super(ac);
            this.message = message;
        }

        public ConfirmationWrapper(AspectComponent ac) {
            super(ac);
            this.message = null;
        }

        public Object invoke(MethodInvocation invocation) throws Throwable {
            return confirm((Interaction) invocation);
        }

        public Object construct(ConstructorInvocation invocation)
            throws Throwable {
            throw new Exception("This wrapper does not support constructor wrapping");
        }

        /** The wrapping method. */
        public Object confirm(Interaction interaction)
            throws OperationCanceledException 
        {
            logger.debug("confirm " + interaction);
            DisplayContext context =
                (DisplayContext) this.attr(GuiAC.DISPLAY_CONTEXT);
            logger.debug("  context=" + context);
            AbstractMethodItem method = interaction.method;
            if (context != null) {
                Display display = context.getDisplay();
                String actualMessage = message;
                if (actualMessage == null) {
                    if (method.isRemover()) {
                        actualMessage =
                            "Do you really want to remove "
                            + NamingConventions.textForName(
                                ClassRepository.get().getClass(interaction.args[0]).getShortName())
                            + " '"
                            + GuiAC.toString(interaction.args[0])
                            + "' from "
                            + NamingConventions.textForName(
                                method.getRemovedCollections()[0].getName())
                            + " of "
                            + NamingConventions.textForName(
                                interaction.getClassItem().getShortName())
                            + " '"
                            + GuiAC.toString(interaction.wrappee)
                            + "' ?";
                    } else {
                        actualMessage =
                            "Do you really want to "
                            + NamingConventions.textForName(
                                interaction.method.getName())
                            + (interaction.method.isStatic()
                               ? ""
                               : (" for '" + GuiAC.toString(interaction.wrappee)) + "'")
                            + " ?";
                    }
                }

                if (!display.showMessage(actualMessage, "Confirmation", true, true, false)) {
                    throw new OperationCanceledException(interaction);
                }
            }
            return interaction.proceed();
        }

        /** The exception handler. */
        public void catchCancellation(OperationCanceledException e) {
            System.out.println("Catching exception: " + e);
        }

    }

}

class OperationCanceledException extends Exception {
    public OperationCanceledException(Interaction interaction) {
        super("Operation cancelled: " + interaction.method);
    }
}
