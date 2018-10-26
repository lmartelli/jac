/*
  Copyright (C) 2002-2003 Laurent Martelli <laurent@aopsys.com>

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

package org.objectweb.jac.ide;

import java.util.List;
import java.util.Map;
import java.util.Iterator;

import org.objectweb.jac.aspects.gui.HandlerResult;

public class Errors {
    List errors;

    public Errors(List errors) {
        this.errors = errors;
    }

    public List getErrors() {
        return errors;
    }

    /*
     * GUI methods
     */

   
    public static HandlerResult selectionHandler(
        org.objectweb.jac.aspects.gui.DisplayContext context,
        org.objectweb.jac.core.rtti.CollectionItem collection,
        Error error)
    {
        Class cl = error.getCl();
        if (cl==null)
            return null;
        return cl.gotoLine(context,error.getLine());
    }
}
