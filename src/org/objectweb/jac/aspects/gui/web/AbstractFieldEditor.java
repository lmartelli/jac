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

import java.lang.IllegalAccessException;
import org.apache.log4j.Logger;
import org.objectweb.jac.aspects.gui.FieldEditor; 
import org.objectweb.jac.aspects.gui.FieldUpdate; 
import org.objectweb.jac.aspects.gui.GuiAC; 
import org.objectweb.jac.aspects.gui.Length;
import org.objectweb.jac.aspects.gui.Utils;
import org.objectweb.jac.core.Collaboration;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.MethodItem;
import org.objectweb.jac.aspects.gui.Unit;

/**
 * Base class for field editors
 */
public abstract class AbstractFieldEditor extends AbstractView
    implements FieldEditor, FieldUpdate
{
    static Logger logger = Logger.getLogger("gui.editor");

    // substance and field are required so that we can register and
    // unregister ourself from fieldUpdated events on close()
    protected Object substance;
    protected FieldItem field;
    protected ClassItem type;
    protected MethodItem setter;
    protected Object value;
    Object oldValue;
    protected boolean isEmbedded;
    boolean isValueSet = false;

    public AbstractFieldEditor(Object substance, FieldItem field) {
        this.substance = substance;
        setField(field);
        width = new Length("10ex");
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        logger.debug(this+".setValue "+oldValue+"->"+value);
        this.value = value;
        if (!isValueSet) {
            oldValue = value;
            isValueSet = true;
        }
    }

    public void setSubstance(Object substance) {
        this.substance = substance;
    }

    public Object getSubstance() {
        return substance;
    }

    public void setEditedType(ClassItem type) {
        this.type = type;
    }

    public void setField(FieldItem field) {
        Utils.unregisterField(substance,this.field,this);
        this.field = field;
        if (field!=null)
            this.setter = field.getSetter();
        Utils.registerField(substance,field,this);
    }

    public FieldItem getField() {
        return field;
    }

    public void setAutoUpdate(boolean autoUpdate) {
    }

    public void setEmbedded(boolean isEmbedded) {
        this.isEmbedded = isEmbedded;
    }

    public void close(boolean validate) {
        if (validate) {
            Object paramValue = 
                WebDisplay.getRequest().getParameter(getLabel());
            logger.debug("reading value for "+getLabel()+": "+paramValue);
            readValue(paramValue);
            if (isEmbedded)
                commit();
        }
        Utils.unregisterField(substance,field,this);
        context.removeEditor(this);
    }

    public void onSetFocus(Object param) {
    }

    public boolean readValue(Object parameter) {
        if (disabled)
            return false;
        else 
            return doReadValue(parameter);
    }
   
    protected abstract boolean doReadValue(Object parameter);

    /**
     * Commit editing by calling the setter method.
     */
    public void commit() {
        if (setter!=null && valueHasChanged()) {
            logger.debug(this+": "+field.getName()+
                         "'s value changed: "+getValue());
            try {
                field.setThroughWriter(substance,getValue());
            } catch (IllegalAccessException e) {
                logger.error("Failed to commit value "+getValue()+
                             " for field "+substance+"."+field,e);
            }
            oldValue = this.value;
        }
    }

    /**
    * Tells wether the value in the editor was changed
    */
    boolean valueHasChanged() {
        boolean ret;
        if( oldValue == null &&  value != null ) {
            ret = true;
        } else if( oldValue == null ) {
            ret = false;
        } else {
            ret = ! oldValue.equals(value);
        }
        logger.debug("valueHasChanged("+field.getName()+") "+
                  oldValue+" / "+value+" -> "+ret);
        return ret;
    }

    public void fieldUpdated(Object substance, FieldItem field, 
                             Object value, Object param) {
        setValue(value);
    }

    // When disabled == true, readValue() does nothing
    boolean disabled = false;
    public boolean isEnabled() {
        return !disabled;
    }
    public void setEnabled(boolean enabled) {
        logger.debug((enabled?"enable ":"disable ")+this);
        this.disabled = !enabled;
    }

    protected String sizeSpec() {
        String size = "";
        if (width!=null && width.unit==Unit.EX) 
            size += " size=\""+width+"\"";
        size += " style=\"width:"+width+"\"";
        return size;
     }
}
