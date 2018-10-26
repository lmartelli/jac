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

package org.objectweb.jac.aspects.gui.web;

import java.io.PrintWriter;
import org.apache.log4j.Logger;
import org.objectweb.jac.aspects.gui.*;
import org.objectweb.jac.core.Collaboration;
import org.objectweb.jac.core.rtti.FieldItem;

/**
 * This is a special value editor that allows the user to choose a
 * value within a set of object of a given type. */

public class ObjectChooser extends AbstractFieldEditor
    implements HTMLEditor, ChoiceListener, ReferenceEditor
{
    static Logger logger = Logger.getLogger("gui.chooser");

    /**
     * Constructs a new object chooser.
     *
     * @param substance
     * @param reference the subtance reference that is affected by this
     * chooser (can be null) 
     * @param isEditable should the user be allowed to enter a new value
     */

    public ObjectChooser(Object substance, FieldItem reference, 
                         ComboBoxModel model,
                         boolean isEditable) 
    {
        super(substance,reference);
        this.model = model;
        this.isEditable = isEditable;
    }

    ComboBoxModel model;
    /**
     * Gets the model containing the list of items the user can choose
     * from.
     */
    public ComboBoxModel getModel() {
        return model;
    }

    // FieldEditor interface

    public void setValue(Object value) {
        super.setValue(value);
        model.setSelectedObject(value);
    }

    public Object getValue() {
        logger.debug("selectedItem = "+model.getSelectedObject());
        return model.getSelectedObject();
    }

    public void close(boolean validate) {
        super.close(validate);
        model.close();
    }

    boolean isEditable = false;
    public boolean isEditable() {
        return isEditable;
    }
    public void setEditable(boolean editable) {
        this.isEditable = editable;
    }

    // HTMLEditor
    public void genHTML(PrintWriter out) {
        String selected = (String)model.getSelectedItem();
        logger.debug("ObjectChooser(field="+field+
                     ", selected="+selected+
                     ", type="+model.getType()+")");

        out.print("<select name=\""+label+"\"");
        printAttributes(out);
        out.println(">");

        for (int i=0; i<model.getSize(); i++) {
            String label = (String)model.getElementAt(i);
            out.println("<option"+
                        (label.equals(selected)?" selected":"")+
                        " value=\""+label+"\""+
                        ">"+
                        label+"</option>");
        }
        out.println("</select>");

        // display a "new" button
        if (isEditable && model.getType()!=null &&
            GuiAC.isCreatable(model.getType())) 
        {
            showButton(out,"new_icon",GuiAC.getLabelNew(),"onCreateObject");
        }
    }

    protected boolean doReadValue(Object parameter) {
        if (parameter!=null) {
            String string = (String)parameter;
            model.setSelectedItem(string);
            Object value = model.getSelectedObject();
            super.setValue(value);
            return true;
        } else {
            return false;
        }
    }

    // ChoiceListener interface
    public void onCreateObject() {
        Thread createThread = new CreateThread();
        createThread.start();
    }

    class CreateThread extends Thread {
        Collaboration parentCollaboration;
        public CreateThread() {
            parentCollaboration = Collaboration.get();
        }
        public void run() {
            Collaboration.set(new Collaboration(parentCollaboration));
            Collaboration.get().addAttribute(GuiAC.AUTO_CREATION,"true");
            Object instance = 
                EventHandler.get().onCreateObject(context,model.getType(),substance,field);
            if (instance!=null) {
                model.addObject(instance);
                value = instance;
                model.setSelectedObject(value);
                // do not use setValue(), because it would then fail to
                // commit on close
            }
            context.getDisplay().refresh();
        }
    }
} 

