/*
  Copyright (C) 2003 Renaud Pawlak <renaud@aopsys.com>
  
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

package org.objectweb.jac.aspects.gui.swing;

import org.objectweb.jac.core.rtti.FieldItem;

/**
 * A Java source code editor
 */
public class JavaCodeEditor extends AbstractCodeEditor
{

   public JavaCodeEditor(Object substance, FieldItem field) {
      super(substance,field);
   }  

   public void init() {
      editor.addKeywords(
         new String[] {"for","while","return","if","else","switch","case",
                       "break","continue","try","catch","finally",
                       "class","interface","goto","this","super",
                       "package","extends","throws","abstract", "instanceof",
                       "implements","import","default","do","new"});
      editor.addModifiers(
         new String[] {"public","protected","private","final","const","abstract",
                       "static","transient","native","volatile"});
      editor.addTypes(
         new String[] {"void","int","long","float","double","boolean",
                       "char","short","byte"});
   }

}

