/*
  Copyright (C) 2003 Laurent Martelli <laurent@aopsys.com>
  
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
import java.util.List;
import java.util.Vector;
import org.objectweb.jac.core.NameRepository;

/**
 * Selects objects from a collection with check boxes
 */
public class CollectionSelector extends AbstractView 
    implements JacRequestReader
{
    Vector selection = new Vector();
    /**
     * @param name the name for generated HTML <input> elements
     */
    public CollectionSelector(String name) {
        this.label = name;
    }

    NameRepository nameRepository = (NameRepository)NameRepository.get();

    /**
     * Generate a checkbox for an item of the collection
     */
    public void genHTML(PrintWriter out, Object object) {
        out.print("<input type=\"checkbox\" name=\""+label+"\" "+
                  "value=\""+nameRepository.getName(object)+"\"");
        printAttributes(out);
        out.println(">");
    }

    public void readValue(JacRequest request) {
        selection.clear();
        String[] names = (String[])request.getParameters(label);
        if (names!=null) {
            for (int i=0; i<names.length; i++) {
                selection.add(nameRepository.getObject(names[i]));
            }
        }
    }

    public List getSelection() {
        return selection;
    }
}

