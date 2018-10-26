/*
  Copyright (C) 2003 Laurent Martelli <laurent@aopsys.com>

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

import org.objectweb.jac.core.rtti.NamingConventions;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

/**
 * gui.acc generation plugin
 */

public class GuiPlugin extends AbstractPlugin {   

    public void genConfig(Writer output, Project project) 
        throws IOException
    {
        // Generate enumerated types
        Iterator enums = Projects.types.getEnumeratedTypes().iterator();
        while (enums.hasNext()) {
            EnumeratedType enum = (EnumeratedType)enums.next();
            output.write("defineEnum "+enum.getName()+" { ");
            Iterator names = enum.getNames().iterator();
            while (names.hasNext()) {
                String name = (String)names.next();
                output.write("\""+name+"\"");
                if (names.hasNext())
                    output.write(", ");
            }
            output.write("} "+enum.getStartValue()+" "+enum.getStep()+";\n");
        }
        super.genConfig(output,project);
    }

    public void genPackageConfig(Writer output, 
                                 Project project, Package pkg) 
        throws IOException
    {
        // Generate askForParameters
        output.write("askForParameters "+pkg.getPPath()+".*;\n\n");
        super.genPackageConfig(output,project,pkg);
    }

    public void genClassConfig(Writer output, 
                               Project project, Package pkg, Class cl)
        throws IOException
    {
        // Generate default attributes order
        state.openClass(cl);
        if (!cl.getName().equals(cl.getGenerationName()))
            state.write("setLabel \""+NamingConventions.textForName(cl.getName())+"\";\n");
        state.write("setAttributesOrder {");
        int count = 0;
        Iterator fields = cl.getAllFields().iterator();
        while (fields.hasNext()) {
            Field field = (Field)fields.next();
            if(field.isStatic()) continue;
            if (count>0) {
                output.write(",");
            }
            output.write(field.getGenerationName());
            count++;
        }

        Iterator roles = cl.getAllNavigableRoles().iterator();
        while (roles.hasNext()) {
            RelationRole role = (RelationRole)roles.next();
            if (count>0) {
                output.write(",");
            }
            output.write(role.getGenerationName());
            count++;
        }
        output.write("};\n");

        super.genClassConfig(output,project,pkg,cl);
      
    }

    public void genRoleConfig(Writer output, Project project, 
                              Package pkg, Class cl, RelationRole role) 
        throws IOException 
    {
        if (role.isNavigable()) {
            Class targetClass = (Class)role.getEnd();
            if (!role.getName().equals(role.getGenerationName())) {
                state.openRole(cl,role);
                state.write("setLabel \""+
                            NamingConventions.textForName(role.getRoleName())+"\";\n");
            }
            // Generate autoCreate for aggregations
            if (role.isAggregation()) {
                state.openRole(cl,role);
                state.write("setAutoCreate;\n");
            }
        }
    }

    public void genFieldConfig(Writer output, Project project, 
                               Package pkg, Class cl, Field field) 
        throws IOException
    {
        if (!field.getName().equals(field.getGenerationName())) {
            state.openField(cl,field);
            state.write("setLabel \""+
                        NamingConventions.textForName(field.getName())+"\";\n");
        }
        if (field.getType() instanceof EnumeratedType) {
            state.openField(cl,field);
            state.write("setFieldEnum "+
                        ((EnumeratedType)field.getType()).getName()+";\n");         
        }
        state.closeMember();
    }

    public void genMethodConfig(Writer output, Project project, 
                                Package pkg, Class cl, Method method) 
        throws IOException 
    {
        if (method.getVisibility()!=Visibility.PUBLIC)
            return;
        if (!method.getName().equals(method.getGenerationName())) {
            state.openMethod(cl,method);
            state.write("setLabel \""+
                        NamingConventions.textForName(method.getName())+"\";\n");
        }
        // Generate method parameters names
        if (method.getParameters().size()>0) {
            state.openMethod(cl,method);
            state.write("setParameterNames {");
            Iterator it = method.getParameters().iterator();
            while (it.hasNext()) {
                Parameter param = (Parameter)it.next();
                output.write("\""+param.getName()+"\"");
                if (it.hasNext()) {
                    output.write(",");
                }
            }
            output.write("};\n");
        }
        state.closeMember();
    }

}
