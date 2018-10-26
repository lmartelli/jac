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

import java.util.HashSet;
import java.util.Hashtable;
import org.objectweb.jac.core.rtti.CollectionItem;
import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.MemberItem;
import org.objectweb.jac.core.rtti.RttiAC;

/**
 * Defines a table collection view
 */
public class CollectionItemView extends MemberItemView {
    public CollectionItemView(CollectionItem collection, String viewName) {
        super(collection,viewName);
        this.collection = collection;
    }

    CollectionItem collection;
    CollectionItem getCollection() {
        return collection;
    }

    MemberItem[] membersOrder;
    public MemberItem[] getMembersOrder() {
        return membersOrder;
    }
    public void setMembersOrder(MemberItem[] newMembersOrder) {
        this.membersOrder = newMembersOrder;
    }

    CollectionItem multiLineCollection;
    public CollectionItem getMultiLineCollection() {
        return multiLineCollection;
    }
    public void setMultiLineCollection(CollectionItem newMultiLineCollection) {
        this.multiLineCollection = newMultiLineCollection;
    }

    FieldItem groupBy;
    public FieldItem getGroupBy() {
        return groupBy;
    }
    public void setGroupBy(FieldItem newGroupBy) {
        this.groupBy = newGroupBy;
    }

    boolean embeddedAdded;
    public void setEmbeddedAdded(boolean embedded) {
        this.embeddedAdded = embedded;
    }
    public boolean isEmbeddedAdded() {
        return embeddedAdded;
    }

    HashSet embeddedEditorColumns = new HashSet();
    boolean embeddedEditors = false;
    public void setEmbeddedEditors(boolean embeddedEditors) {
        this.embeddedEditors = embeddedEditors;
    }
    public void addEmbeddedEditorColumn(MemberItem field) {
        embeddedEditorColumns.add(field);
    }
    public boolean isEmbeddedEditors(MemberItem field) {
        return embeddedEditors || embeddedEditorColumns.contains(field);
    }
    public boolean isEmbeddedEditors() {
        return embeddedEditors;
    }

    boolean viewableItems = true;
    public boolean isViewableItems() {
        if (collection.isMap())
            return RttiAC.isIndex(collection) && viewableItems;
        else
            return viewableItems;
    }
    public void setViewableItems(boolean newViewableItems) {
        this.viewableItems = newViewableItems;
    }

    boolean enableLinks = true;
    public void setEnableLinks(boolean enable) {
        this.enableLinks = enable;
    }
    public boolean areLinksEnabled() {
        return enableLinks;
    }

    /** MemberItem -> viewType */
    Hashtable viewTypes = new Hashtable();
    public void setViewType(MemberItem member, String viewType) {
        viewTypes.put(member,viewType);
    }
    public String getViewType(MemberItem member) {
        return (String)viewTypes.get(member);
    }

    FieldItem additionalRow;
    public FieldItem getAdditionalRow() {
        return additionalRow;
    }
    public void setAdditionalRow(FieldItem newAdditionalRow) {
        this.additionalRow = newAdditionalRow;
    }
}

