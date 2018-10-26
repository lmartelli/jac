/*
  Copyright (C) 2002-2003 Laurent Martelli <laurent@aopsys.com>
  
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

import org.objectweb.jac.aspects.gui.*;
import org.objectweb.jac.core.rtti.FieldItem;
import java.io.PrintWriter;

/**
 * This class defines a component view for references in
 * objects.
 *
 * <p>By default this view constructs an embedded <code>JLabel</code>
 * containing the string representation of the referenced object. However,
 * the field can be attributed to be displayed with a customized
 * rendering by the GUI aspect component.
 *
 * @see GuiAC
 * @see FieldView 
 */

public class ReferenceView extends AbstractFieldView 
    implements FieldView, FieldUpdate, ObjectUpdate, 
              HTMLViewer, SelectionListener, LinkGenerator 
{
    protected Object object;
    protected String text;
    protected String eventURL;

    /**
     * Constructs a new reference view.
     *
     * @param substance the object the viewed field belongs to */

    public ReferenceView(Object value, Object substance, FieldItem reference) {
        super(substance,reference);
        this.object = value;

        if (autoUpdate)
            Utils.registerField(substance,reference,this);

        refreshView();
    }

    public ReferenceView() {
    }

    boolean enableLinks = true;
    public void setEnableLinks(boolean enable) {
        this.enableLinks = enable;
    }
    public boolean areLinksEnabled() {
        return enableLinks;
    }

    public void refreshView() {
        if (autoUpdate)
            Utils.registerObject(object,this);
        if (object!=null) {
            text = GuiAC.toString(object,contexts);
        } else {
            text = "";
        }
    }

    /**
     * Set the URL to link to.
     */
    public void setEventURL(String eventURL) {
        this.eventURL = eventURL;
    }

    // FieldView interface

    public void setValue(Object value) {
        if (autoUpdate)
            Utils.unregisterObject(object,this);
        this.object = value;
        refreshView();
    }

    public void close(boolean validate) {
        super.close(validate);
        if (autoUpdate)
            Utils.unregisterObject(object,this);
    }

    // FieldUpdate interface

    public void fieldUpdated(Object substance, FieldItem field, Object value, Object param) {
        setValue(value);
    }

    // ObjectUpdate interface

    public void objectUpdated(Object object, Object param) {
        refreshView();
    }

    // HTMLViewer interface
    public void genHTML(PrintWriter out) {
        if (enableLinks)
            out.print("<a href=\""+
                      (eventURL!=null?eventURL:eventURL("onSelection"))+"\">"+text+"</a>");
        else
            out.print(text);
    }

    // SelectionListener interface

    public void onSelection() {
        EventHandler.get().onSelection(context,field,object,null,null,true);
    }

}
