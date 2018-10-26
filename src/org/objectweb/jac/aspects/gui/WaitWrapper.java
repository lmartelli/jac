/*
  Copyright (C) 2003 Laurent Martelli <laurent@aopsys.com>

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

import org.aopalliance.intercept.ConstructorInvocation;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.AspectComponent;
import org.objectweb.jac.core.Display;
import org.objectweb.jac.core.Interaction;
import org.objectweb.jac.core.Wrapper;

/**
 * This wrapper displays a "Please wait message" for slow operations.
 * @see org.objectweb.jac.aspects.gui.GuiAC#setSlowOperation(AbstractMethodItem,boolean)
 */
public class WaitWrapper extends Wrapper {
    static Logger logger = Logger.getLogger("gui.wait");

	public WaitWrapper(AspectComponent ac) {
		super(ac);
	}

    public Object pleaseWait(Interaction interaction) {
        DialogView page = null;
        CustomizedDisplay display = null;
        Object result = null;
        try {
            if (GuiAC.isSlowOperation(interaction.method)) {
                logger.debug("pleaseWait "+interaction);
                DisplayContext context =
                    (DisplayContext)attr(GuiAC.DISPLAY_CONTEXT);
                if (context!=null) {
                    display = context.getDisplay();
                    page = 
                        (DialogView)display.showRefreshMessage(
                            "Please wait",
                            "Please wait: "+interaction.method.getName()+" is running...");
                } else {
                    logger.error("pleaseWait: no display context available for "+
                                 interaction.method);
                }            
            }
            result = interaction.proceed();
        } finally {
            if (page!=null)
                page.waitForClose();
        }
        return result;
    }

	public Object invoke(MethodInvocation invocation) throws Throwable {
		return pleaseWait((Interaction)invocation);
	}

	public Object construct(ConstructorInvocation invocation) throws Throwable {
		return pleaseWait((Interaction)invocation);
	}
}
