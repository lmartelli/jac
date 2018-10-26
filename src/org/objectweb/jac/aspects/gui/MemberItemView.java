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

package org.objectweb.jac.aspects.gui;

import org.objectweb.jac.core.rtti.MemberItem;


/**
 * Defines a generic member view (is embedded, ...)
 */
public abstract class MemberItemView {
    MemberItem member;
    String name;

    public MemberItemView(MemberItem member, String name) {
        this.member = member;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /** Wether to use an embedded view or not (for a reference) */
    boolean embedded;
    public void setEmbedded(boolean embedded) {
        this.embedded = embedded;
    }
    public boolean isEmbedded() {
        return embedded;
    }

    Boolean embeddedEditor = null;
    public void setEmbeddedEditor(boolean embeddedEditor) {
        this.embeddedEditor = embeddedEditor ? Boolean.TRUE : Boolean.FALSE;
    }
    public boolean isEmbeddedEditor(boolean defaultValue) {
        return embeddedEditor!=null ? embeddedEditor.booleanValue() : defaultValue;
    }

    String viewType;
    public void setViewType(String viewType) {
        this.viewType = viewType;
    }
    public String getViewType() {
        return viewType;
    }
}
