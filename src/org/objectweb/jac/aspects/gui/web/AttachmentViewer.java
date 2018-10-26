/*
  Copyright (C) 2002 Laurent Martelli <laurent@aopsys.com>
  
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

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.mortbay.html.Tag;
import org.objectweb.jac.aspects.gui.GuiAC;
import org.objectweb.jac.aspects.gui.TableCellViewer;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.lib.Attachment;
import org.objectweb.jac.util.Strings;
import org.objectweb.jac.util.Thumbnail;

public class AttachmentViewer extends AbstractFieldView 
    implements HTMLViewer, AttachmentListener, TableCellViewer
{
    static Logger logger = Logger.getLogger("web.attachment");

    Attachment value;

    public AttachmentViewer(Object value, Object substance, FieldItem field) {
        super(substance,field);
        setValue(value);
    }

    public AttachmentViewer() {
        isCellViewer = true;
    }

    public void setValue(Object value) {
        this.value = (Attachment)value;
    }

    public void genHTML(PrintWriter out) throws IOException {
        if (value!=null) {
            if (value.getMimeType()==null)
                out.print("<a href=\""+eventURL("onLoadAttachment")+"\">"+
                          value.getName()+"</a>");
            else if (value.getMimeType().startsWith("image/")) {
                Tag img = new Tag("img");
                if (isCellViewer)
                    img.attribute("src",eventURL("onLoadAttachment")+"&amp;thumb=1");
                else
                    img.attribute("src",eventURL("onLoadAttachment"));
                img.write(out);
            } else
                out.print("<a href=\""+eventURL("onLoadAttachment")+"\">"+
                          value.getName()+"</a>");
        }
    }

    // AttachmentListener interface

    public void onLoadAttachment() {
        WebDisplay display = (WebDisplay)context.getDisplay();
        try {
            if (value!=null) {
                JacRequest request = WebDisplay.getRequest();
                HttpServletResponse response = WebDisplay.getResponse();
                response.setContentType(value.getMimeType());
                if (request.getParameter("thumb")!=null) {
                    try {
                        byte[] thumb =
                            Thumbnail.createThumbArray(
                                value.getData(),
                                GuiAC.THUMB_MAX_WIDTH,GuiAC.THUMB_MAX_HEIGHT,
                                GuiAC.THUMB_QUALITY);
                        logger.debug(this+"Writing attachment "+value+
                                     " on "+Strings.hex(response));
                        response.getOutputStream().write(thumb);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    response.getOutputStream().write(value.getData());
                }
            }
        } catch (IOException e) {
            logger.error("Failed to output stream",e);
        } finally {
            (WebDisplay.getRequest()).setResponse();
        }
    }
}
