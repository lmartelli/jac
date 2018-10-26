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

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.objectweb.jac.aspects.gui.*;
import org.objectweb.jac.core.NameRepository;
import org.objectweb.jac.core.rtti.CollectionItem;

public class CompactList extends AbstractCollection
    implements HTMLViewer, CollectionListener, LinkGenerator
{
    public CompactList(ViewFactory factory, DisplayContext context,
                       CollectionItem collection, Object substance,
                       CollectionModel model,
                       org.objectweb.jac.aspects.gui.CollectionItemView itemView) {
        super(factory,context,collection,substance,model,itemView);
    }

    public CompactList() {
    }

    // LinkGenerator interface 
    boolean enableLinks = true;
    public void setEnableLinks(boolean enable) {
        this.enableLinks = enable;
    }
    public boolean areLinksEnabled() {
        return enableLinks;
    }

    public void sort() {
    }

    public void updateModel(Object substance) {
        if (model!=null)
            model.close();
        model = new ListModel(collection,substance);
    }

    // HTMLViewer interface

    public void genHTML(PrintWriter out) {
        sort();

        boolean ulOpened = false;
        for (int index=startIndex; 
             (!split || index<startIndex+rowsPerPage) && index<model.getRowCount(); 
             index++) 
        {
            String row = (String)((ListModel)model).getElementAt(index);
            if (!ulOpened) {
                out.println("<ul class=\"list\">");
                ulOpened = true;
            }
            String name = NameRepository.get().getName(model.getObject(index));
            out.print("    <li>");
            if (name!=null && enableLinks) {
                try {
                    out.print("<a href=\""+eventURL("onViewObject")+"&amp;object="+
                              URLEncoder.encode(name,GuiAC.getEncoding())+"\">"+row+"</a>");
                } catch(UnsupportedEncodingException e) {
                    logger.error(e);
                }
            } else {
                out.print(row);
            }
            if (GuiAC.isRemovable(collection) && isEditor)
                out.println(removeLink(index));
            out.print("</li>");
        }
        if (ulOpened)
            out.println("</ul>");

        if (/*GuiAC.isAddable(substance,collection) &&*/ isEditor) {
            if (GuiAC.isAutoCreate(collection) && isEditor) {
                genCollectionEvent(out,"onAddToCollection","new_icon",GuiAC.getLabelAdd());
                /*
                  genCollectionEvent(out,"onAddToCollection","new_icon","add new");
                  genCollectionEvent(out,"onAddExistingToCollection","new_icon","add existing");
                */
            } else {
                genCollectionEvent(out,"onAddToCollection","new_icon",GuiAC.getLabelAdd());
            }
        }

    }
}
