/*
  Copyright (C) 2002 Renaud Pawlak, Laurent Martelli
  
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

import org.objectweb.jac.core.rtti.FieldItem;
import org.objectweb.jac.core.rtti.ClassRepository;

/**
 * A handler that is assigned to a selection event (in order to change
 * its result in most cases). */

public class HandlerResult {
   public Object object;
   public FieldItem field;
   public FieldItem container;
   public Object extraOption;
   public CustomizedView target;

   /**
    * Constructs a new handler.
    *
    * @param container field the object is part of
    * @param object the object that holds the selected field
    * @param fieldname name of the field to select (may be null)
    * @param extraOption extra info (may be null) */
   public HandlerResult(CustomizedView target,
                        FieldItem container,Object object, String fieldname, 
                        Object extraOption) {
      this.target = target;
      this.container = container;
      this.object = object;
      if (fieldname!=null)
         this.field = 
            ClassRepository.get().getClass(object).getField(fieldname);
      this.extraOption = extraOption;
   }
}
