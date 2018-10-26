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

import org.objectweb.jac.aspects.gui.*;
import org.objectweb.jac.core.rtti.CollectionItem;
import java.io.PrintWriter;

public class List extends AbstractCollection
   implements HTMLViewer, CollectionListener
{
   public List(ViewFactory factory, DisplayContext context,
               CollectionItem collection, Object substance,
               CollectionModel model, org.objectweb.jac.aspects.gui.CollectionItemView itemView) {
      super(factory,context,collection,substance,model,itemView);
   }

   public void sort() {
   }

   // HTMLViewer interface

   public void genHTML(PrintWriter out) {
      sort();

      genHeader(out);
      out.println("<table class=\"list\">");
      out.println("  <tbody>");

      for (int index=startIndex; 
           (!split || index<startIndex+rowsPerPage) && index<model.getRowCount(); 
           index++) 
      {
         String row = (String)((ListModel)model).getElementAt(index);
         out.println("    <tr"+(selected==index?" class=\"selected\"":"")+">");
         out.println("      <td><a href=\""+
                     eventURL("onView")+
                     "&amp;index="+index+"\">"+row+"</a></td>");
         if (GuiAC.isRemovable(collection) && isEditor)
            out.println("      <td>"+removeLink(index)+"</td>");
         out.println("    </tr>");
      }
      out.println("  </tbody>");

      out.println("</table>");
   }
}
