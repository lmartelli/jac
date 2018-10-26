/*
  Copyright (C) 2002-2003 Renaud Pawlak <renaud@aopsys.com>

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
  USA. */

package org.objectweb.jac.ide.diagrams;

import CH.ifa.draw.figures.TextFigure;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.util.Log;

public class AttributeValueFigure extends TextFigure {

    public AttributeValueFigure(FieldItem attribute,Object substance) {
        this.attribute=attribute;
        this.substance=substance;
        DiagramView.init = true;
        Object value=attribute.getThroughAccessor(substance); 
        if(value==null)
            setText("");
        else
            setText(value.toString());
        DiagramView.init = false;
    }

    FieldItem attribute;
   
    /**
     * Get the value of attribute.
     * @return value of attribute.
     */
    public FieldItem getAttribute() {
        return attribute;
    }
   
    /**
     * Set the value of attribute.
     * @param v  Value to assign to attribute.
     */
    public void setAttribute(FieldItem  v) {
        this.attribute = v;
    }
   
    Object substance;
   
    /**
     * Get the value of substance.
     * @return value of substance.
     */
    public Object getSubstance() {
        return substance;
    }
   
    /**
     * Set the value of substance.
     * @param v  Value to assign to substance.
     */
    public void setSubstance(Object v) {
        this.substance = v;
    }
   
    public void setText(String s) {
        super.setText(s);
        if (substance != null && attribute != null && !DiagramView.init) {
            try {
                attribute.setThroughWriter(substance,s);
            } catch (Exception e) {
                Logger.getLogger("figures").error(
                    "Failed to set attribute value for "+
                    substance+"."+attribute+" to "+s);
            }
        }
    }

}
