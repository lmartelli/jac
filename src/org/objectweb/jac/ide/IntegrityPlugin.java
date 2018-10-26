/*
  Copyright (C) 2003-2004 Laurent Martelli <laurent@aopsys.com>

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

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

/**
 * intergrity.acc generation plugin
 */

public class IntegrityPlugin extends AbstractPlugin {

   public void genClassConfig(Writer output, 
                              Project project, Package pkg, Class cl)
      throws IOException
   {
       /*
      Log.trace("ide.plugin","Integrity on "+cl.getName());
      Iterator roles = cl.getLinks().iterator();
      while (roles.hasNext()) {
         Role role = (Role)roles.next();
         Log.trace("ide.plugin","Integrity on "+cl.getName()+"."+role);
         if (role instanceof RelationRole) {
            RelationRole relRole = (RelationRole)role;
            if (((RelationLink)relRole.getLink()).getOrientation()
                   == RelationLink.ORIENTATION_BOTH 
                && relRole.getLink().getStartRole()==relRole) {
               RelationRole opposite = (RelationRole)relRole.oppositeRole();
               state.openClass(cl);
               output.write("    declareAssociation "+
                            relRole.getGenerationName()+" "+
                            opposite.getStart().getGenerationFullName()+" "+
                            opposite.getGenerationName()+";\n");
            }
         }
      }
      state.closeClass();
       */
   }
}
