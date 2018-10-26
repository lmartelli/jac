/*
  Copyright (C) 2001-2003 Laurent Martelli <laurent@aopsys.com>
  
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

package org.objectweb.jac.aspects.gui.web;

import org.apache.log4j.Logger;
import org.objectweb.jac.aspects.gui.FieldUpdate; 
import org.objectweb.jac.aspects.gui.FieldView; 
import org.objectweb.jac.aspects.gui.GuiAC;
import org.objectweb.jac.aspects.gui.Utils; 
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.util.Stack;

/**
 * Base class for field views
 */
public abstract class AbstractFieldView extends AbstractView 
   implements FieldUpdate, FieldView
{
    static Logger logger = Logger.getLogger("gui.autoupdate");

    // substance and field are required so that we can register and
    // unregister ourself from fieldUpdated events on close()
    protected Object substance;
    protected FieldItem field;
    protected boolean autoUpdate = true;

    public AbstractFieldView(Object substance, FieldItem field) {
        this.substance = substance;
        this.field = field;

        if (autoUpdate)
            Utils.registerField(substance,field,this);

        if (GuiAC.getGraphicContext()!=null)
            contexts.addAll(GuiAC.getGraphicContext());
        if (field!=null)
            contexts.push(field);
    }

    Stack contexts = new Stack();

    public AbstractFieldView() {
        if (GuiAC.getGraphicContext()!=null)
            contexts.addAll(GuiAC.getGraphicContext());
    }

    public abstract void setValue(Object value);

    public void setSubstance(Object substance) {
        logger.debug(this+".setSubstance,  autoUpdate="+autoUpdate);
        if (autoUpdate)
            Utils.unregisterField(this.substance,field,this);
        this.substance = substance;
        if (autoUpdate)
            Utils.registerField(substance,field,this);
    }

    public Object getSubstance() {
        return substance;
    }    

    public void setField(FieldItem field) {
        logger.debug(this+".setField,  autoUpdate="+autoUpdate);
        if (autoUpdate)
            Utils.unregisterField(substance,this.field,this);
        this.field = field;
        if (autoUpdate)
            Utils.registerField(substance,field,this);
        if (field!=null)
            contexts.push(field);
    }

    public FieldItem getField() {
        return field;
    }

    public void setAutoUpdate(boolean autoUpdate) {
        logger.debug(this+".setAutoUpdate "+autoUpdate);
        if (this.autoUpdate!=autoUpdate) {
            if (this.autoUpdate)
                Utils.unregisterField(substance,field,this);
            this.autoUpdate = autoUpdate;
            if (autoUpdate)
                Utils.registerField(substance,field,this);            
        }
    }

    public void close(boolean validate) {
        if (autoUpdate)
            Utils.unregisterField(substance,field,this);
    }
   
    // FieldUpdate interface
    public void fieldUpdated(Object substance, FieldItem field, Object value, Object param) {
        setValue(value);
    }

}
