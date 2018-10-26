/*
  Copyright (C) 2002 Renaud Pawlak <renaud@aopsys.com>

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 2 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  USA */

package org.objectweb.jac.ide;

import java.util.List;
import java.util.Vector;

/**
 * This is the root class of all the model elements. */

public abstract class ModelElement{

    /**
     * Builds an unamed model element. */
    public ModelElement(){
    }

    /**
     * Builds a named model element. */
    public ModelElement(String name){
        this.name = name;
    }

    String name = "";

    /** Sets the model element name. */
    public void setName(String name){
        this.name = name;
    }
   
    /**
     * Defines a redefinable method to get the full name. Here it is
     * equivalent to the <code>getName()</code> method. */

    public String getFullName(){
        return getName();
    }

    /** Gets the model element name. */
    public String getName(){
        return name;
    }

    /**
     * Gets name to use for code generation. Defaults to name.
     */
    public String getGenerationName() {
        return getName();
    }

    /**
     * Gets full name to use for code generation. Defaults to fullName.
     */
    public String getGenerationFullName() {
        return getFullName();
    }

    /**
     * Get the type of the model element.
     *
     * @return the void type (by default, element are not typed) */

    public Type getType(){
        return Projects.types.resolveType("void", "");
    }

    List endingLinks = new Vector();
    /**
     * Gets the list of the links that end on this model element.
     * @return value of endingLinks.
     * @see Link
     */
    public List getEndingLinks(){
        return endingLinks;
    }
    /** Sets the ending links list. */
    public void setEndingLinks(List l) {
        endingLinks = l;
    }   
    /** Adds a link that ends on this element. */
    public void addEndingLink(Role l) {
        endingLinks.add(l);
    }
    /** Removes an ending link. */
    public void removeEndingLink(Role l) {
        endingLinks.remove(l);
    }

    List links = new Vector();
    /**
     * Gets the list of the links that start from this model element.
     * @return value of links.
     * @see Link
     */
    public List getLinks() {
        return links;
    }
    /** Sets the ending links list. */
    public void setLinks(List l) {
        links = l;
    }
    /** Adds a link that ends on this element. */
    public void addLink(Role l) {
        links.add(l);
    }
    /** Removes an ending link. */
    public void removeLink(Role l) {
        links.remove(l);
    }

    String description;

    /**
     * Gets the description of this element. All the model elements
     * have a description for documentation.
     * @return value of description.  */
    public String getDescription(){
        return description;
    }

    /**
     * Set the value of description.
     * @param v  Value to assign to description.
     */
    public void setDescription(String v){
        this.description=v;

    }

    private List configItems = new Vector();

    /**
     * add a new ConfigItem on this Element
     * @param config the new ConfigItem
     */
    public void addConfigItem(ConfigItem config){
        configItems.add(config);
    }

    /**
     * remove an ConfigItem
     * @param config the ConfigItem
     */
    public void remove(ConfigItem config){
        configItems.remove(config);
    }

    public List getConfigItems(){
        return configItems;
    }
}
