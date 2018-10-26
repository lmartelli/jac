/*
  Copyright (C) 2001-2003 Renaud Pawlak <renaud@aopsys.com>
  
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
import java.io.IOException;
import java.io.PrintWriter;

/**
 * A nice collection view.
 * 
 * <p>It provides a choice on the elements (for the upper part) and an embedded view of the selected object
 */
public class ChoiceCollection
	extends AbstractCollection
	implements HTMLViewer 
{

	int oldSelected = -1;
	ObjectView objectView = null;

	public ChoiceCollection(
		ViewFactory factory,
		DisplayContext context,
		CollectionItem collection,
		Object substance,
		ComboBoxModel model,
		org.objectweb.jac.aspects.gui.CollectionItemView itemView) 
    {
		super(factory, context, collection, substance, model, itemView);
	}
	/* (non-Javadoc)
	 * @see org.objectweb.jac.aspects.gui.web.AbstractCollection#sort()
	 */
	public void sort() {
		// TODO Auto-generated method stub

	}

	public void genHTML(PrintWriter out) throws IOException {
		out.println("<div class=BORDER_LINE>");
		out.print(GuiAC.getLabel(collection) + " : ");
		out.print("<select name=\"index_" + getId() + "\"");
		printAttributes(out);
		out.println(">");

		for (int i = 0; i < model.getRowCount(); i++) {

			String label = GuiAC.toString(model.getObject(i));
			out.println(
				"<option"
					+ (i == selected ? " selected" : "")
					+ " value=\"" + i + "\">"
					+ label
					+ "</OPTION>");
		}
		out.println("</SELECT>");

		JacRequest request = WebDisplay.getRequest();

		if (request.isIEUserAgent()) {
			out.println(
				"<table class=\"method\"><tr><td>"
					+ iconElement(
						ResourceManager.getResource("view_icon"),
						"view")
					+ eventURL("view", "onView", "")
					+ "</td></tr></table>");
		} else {
			out.println(
				"<span class=\"method\">"
					+ iconElement(
						ResourceManager.getResource("view_icon"),
						"view")
					+ eventURL("View", "onView", "")
					+ "</span>");
		}

		genHeader(out, false);

		//		out.println(iconElement(null,"view",false)+
		//					eventURL("onView")+
		//					"\">"+"View"+"</a></td>");

		out.println("</div>");

		if (!GuiAC.isExternalChoiceView(collection)) {
			if (selected != -1) {
				if (objectView == null || oldSelected != selected) {
					Object selectedObject = model.getObject(selected);
					objectView =
						(ObjectView) getFactory().createObjectView(
							GuiAC.toString(selectedObject),
							selectedObject,
							getContext());
				}
				objectView.genHTML(out);
			}
			oldSelected = selected;
		}
	}

}
