/*
  Copyright (C) 2001 Laurent Martelli <laurent@aopsys.com>
  
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

package org.objectweb.jac.aspects.gui.web.html;

import org.mortbay.util.UrlEncoded;

public class Button extends Block
{
    /** Input types */
    public final static String BUTTON="button";
    public final static String SUBMIT="submit";
    public final static String RESET="reset";

    public Button(String type,String name) {
        super("button");
        attribute("type",type);
        attribute("name",name);
    }

    public Button(String type,String name, String value) {
        this(type,name);
        attribute("value",value);
    }

    public Button check() {
        attribute("checked");
        return this;
    }

    public Button setSize(int size) {
        size(size);
        return this;
    }

    public Button setMaxSize(int size) {
        attribute("maxlength",size);
        return this;
    }

    public Button fixed() {
        setMaxSize(size());
        return this;
    }

   public void encodeAttribute(String attribute) {
      attributeMap.put(attributeMap,
                       UrlEncoded.encodeString((String)attributeMap.get(attribute)));
   }
}
