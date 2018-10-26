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
 * rtti.acc generation plugin
 */

public class RttiPlugin extends AbstractPlugin {   

    public void genConfig(Writer output, Project project) 
        throws IOException
    {
        // Generate "extended" types
        Iterator enums = Projects.types.getExtendedTypes().iterator();
        while (enums.hasNext()) {
            ExtendedType extended = (ExtendedType)enums.next();
            output.write("newVirtualClass "+extended.getName()+" "+
                         extended.getRealType().getGenerationFullName()+";\n");
        }

        super.genConfig(output,project);
    }

    public void genFieldConfig(Writer output, Project project, 
                               Package pkg, Class cl, Field field) 
        throws IOException 
    {
        if (field.isCalculated()) {
            Getter getter = field.getGetter();
            if (getter!=null) {
                state.openField(cl,field);
                output.write("        declareCalculatedField "+
                             getter.getGenerationName()+";\n");
            } else {
                System.err.println("Calculated field "+field.getFullName()+
                                   " does not have a getter");
            }
            Setter setter = field.getSetter();
            if (setter!=null) {
                state.openField(cl,field);
                output.write("        setSetter "+
                             setter.getGenerationName()+";\n");
            }         
        }
        if (field.getType() instanceof ExtendedType) {
            state.openField(cl,field);
            output.write("        setFieldType "+
                         field.getType().getName()+";\n");
        }
    }   

    public void genRoleConfig(Writer output, Project project, 
                              Package pkg, Class cl, RelationRole role)
        throws IOException 
    {
        // Repositories
        String name = role.getGenerationName();
        if (role.getEnd() instanceof Repository) {
            Repository rep = (Repository)role.getEnd();
            RelationRole itemsRole = (RelationRole)role.oppositeRole();
            if (itemsRole!=null) {
                state.openClass(cl);
                output.write(
                    "    defineRepository \""+rep.getGenerationName().toLowerCase()+"#0\" "+
                    itemsRole.getGenerationFullName()+";\n");
            }
            
        }

        // associations
        if (role instanceof RelationRole) {
            RelationRole relRole = (RelationRole)role;
            if (((RelationLink)relRole.getLink()).getOrientation()
                == RelationLink.ORIENTATION_BOTH 
                && relRole.getLink().getStartRole()==relRole) 
            {
                RelationRole opposite = (RelationRole)relRole.oppositeRole();
                state.openRole(cl,role);
                output.write("        declareAssociation "+
                             opposite.getGenerationFullName()+";\n");
            }
        }

        if (role.isNavigable()) {
            RelationLink link = (RelationLink)role.getLink();
            if (link.isCalculated()) {
                Method getter = role.getGetter();
                if (getter!=null) {
                    state.openRole(cl,role);
                    output.write("        declareCalculatedField "+
                                 role.getGetter().getGenerationName()+";\n");
                } else {
                    System.err.println("Calculated role "+role.getFullName()+
                                       " does not have a getter");
                }
                state.openRole(cl,role);
                output.write("        setComponentType "+role.getEnd().getGenerationFullName()+";\n");
            }
            state.openRole(cl,role);
            output.write("        setAggregation "+link.isAggregation()+";\n");
            if (!role.isMultiple() && role.getCardinality().startsWith("0")) {
                state.openRole(cl,role);
                output.write("        setNullAllowed;\n");
            }
            Typed index = role.getPrimaryKey();
            if (index!=null) {
                state.openRole(cl,role);
                output.write("        setIndexedField "+
                             index.getGenerationFullName()+";\n");
                output.write("        setRemover "+role.getRemoverName()+";\n");
            }
        }
    }

}
