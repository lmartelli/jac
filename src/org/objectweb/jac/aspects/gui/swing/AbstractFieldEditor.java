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

package org.objectweb.jac.aspects.gui.swing;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.lang.IllegalAccessException;
import org.apache.log4j.Logger;
import org.objectweb.jac.aspects.gui.CommitException; 
import org.objectweb.jac.aspects.gui.FieldEditor; 
import org.objectweb.jac.aspects.gui.FieldUpdate; 
import org.objectweb.jac.aspects.gui.GuiAC;
import org.objectweb.jac.aspects.gui.Utils; 
import org.objectweb.jac.core.Collaboration;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.MethodItem;

public abstract class AbstractFieldEditor extends AbstractView 
    implements FocusListener, FieldEditor, FieldUpdate
{
    static Logger logger = Logger.getLogger("gui.editor");
    static Logger loggerFocus = Logger.getLogger("gui.focus");

    // substance and field are required so that we can register and
    // unregister ourself from fieldUpdated events on close()
    Object substance;
    FieldItem field;
    MethodItem setter;
    Object value;
    boolean isEmbedded;
    protected ClassItem type;

    public AbstractFieldEditor(Object substance, FieldItem field) {
        this.substance = substance;
        setField(field);
        addFocusListener(this);
    }

    public void fieldUpdated(Object object, FieldItem field, 
                             Object value, Object param) {
        setValue(value);
    }

    public abstract Object getValue();

    public void setValue(Object value) {
        this.value = value;
    }

    public void setField(FieldItem field) {
        Utils.unregisterField(substance,this.field,this);
        this.field = field;
        Utils.registerField(substance,this.field,this);
        if (field!=null)
            this.setter = field.getSetter();
    }

    public FieldItem getField() {
        return field;
    }

    public void setSubstance(Object substance) {
        logger.debug("setSubstance("+substance+")");
        this.substance = substance;
    }

    /**
    * Returns the object that holds the field, if any
    */
    public Object getSubstance() {
        return substance;
    }

    public void setEditedType(ClassItem type) {
        this.type = type;
    }

    public void setEmbedded(boolean isEmbedded) {
        this.isEmbedded = isEmbedded;
        if (isEmbedded)
            Utils.registerField(substance,field,this);
        else
            Utils.unregisterField(substance,field,this);
    }

    public void setAutoUpdate(boolean autoUpdate) {
        // TODO
    }

    public void close(boolean validate) {
        Utils.unregisterField(substance,field,this);
        if (validate)
            commit();
        substance = null;
        super.close(validate);
    }

    public void onSetFocus(Object extraOption) {}

    /**
    * Commit editing by calling the setter method.
    */
    public void commit() {
        if (setter!=null && valueHasChanged()) {
            logger.debug("value changed for "+substance+"."+field.getName());
            try {
                field.setThroughWriter(substance,getValue());
            } catch (IllegalAccessException e) {
                logger.error("Failed to commit value "+getValue()+
                             " for field "+substance+"."+field,e);
            } catch (Exception e) {
                throw new CommitException(e,substance,field);
            }
            value = getValue();
        }
    }

    /**
    * Tells wether the value in the editor was changed
    */
    boolean valueHasChanged() {
        Object newValue = getValue();
        boolean ret;
        if (value == null &&  newValue != null) {
            ret = true;
        } else if (value == null) {
            ret = false;
        } else {
            ret = ! value.equals(newValue);
        }
        logger.debug("valueHasChanged("+field.getName()+") "+
                     value+" / "+newValue+" -> "+ret);
        return ret;
    }

    // FocusListener interface

    public void focusGained(FocusEvent e) {
        /** do nothing */
        loggerFocus.debug("focus gained on "+getClass().getName());
    }

    public void focusLost(FocusEvent e) {
        /** Update the object's field if needed. */
        loggerFocus.debug("focus lost on "+getClass().getName());
        // We must ignore the event if closed==true because susbtance
        // is set to null
        if (field!=null && isEmbedded && !closed) {
            invokeInContext(this,"commit", new Object[]{});
        } else {
            loggerFocus.debug("ignoring focusLost event");
        }
    }

}
