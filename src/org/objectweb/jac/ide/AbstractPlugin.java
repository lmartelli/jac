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

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

/**
 * Abstract base class for aspect config file plugins
 */

public abstract class AbstractPlugin implements AspectPlugin {   
    AccGenState state;

    public void genConfig(Writer output, Project project) 
        throws IOException
    {
        Iterator it = project.getPackages().iterator();
        while (it.hasNext()) {
            Package pkg = (Package)it.next();
            genPackageConfig(output,project,pkg);
        }
    }

    /**
     * Generate default rtti config code for a package
     */
    public void genPackageConfig(Writer output, 
                                 Project project, Package pkg) 
        throws IOException
    {
        Iterator it = pkg.getSubPackages().iterator();
        while (it.hasNext()) {
            Package subPkg = (Package)it.next();
            genPackageConfig(output,project,subPkg);
        }
        it = pkg.getClasses().iterator();
        while (it.hasNext()) {
            Class cl = (Class)it.next();
            if (!(cl instanceof Interface)) {
                state = new AccGenState(output);
                genClassConfig(output,project,pkg,cl);
                state.closeClass();
            }
        }
    }

    /**
     * Generate default rtti config code for a class
     */
    public void genClassConfig(Writer output, 
                               Project project, Package pkg, Class cl) 
        throws IOException
    {
        Iterator it = cl.getFields().iterator();
        while (it.hasNext()) {
            Field field = (Field)it.next();
            genFieldConfig(output,project,pkg,cl,field);
            state.closeMember();
        }

        it = cl.getMethods().iterator();
        while (it.hasNext()) {
            Method method = (Method)it.next();
            genMethodConfig(output,project,pkg,cl,method);
            state.closeMember();
        }

        it = cl.getRelationRoles().iterator();
        while (it.hasNext()) {
            RelationRole role = (RelationRole)it.next();
            genRoleConfig(output,project,pkg,cl,role);
            state.closeMember();
        }
    }

    /**
     * Generate default rtti config code for a field
     */
    public void genFieldConfig(Writer output, Project project, 
                               Package pkg, Class cl, Field field) 
        throws IOException 
    {}

    /**
     * Generate default rtti config code for a method
     */
    public void genMethodConfig(Writer output, Project project, 
                                Package pkg, Class cl, Method method) 
        throws IOException 
    {}


    /**
     * Generate default rtti config code for a relation role
     */
    public void genRoleConfig(Writer output, Project project, 
                              Package pkg, Class cl, RelationRole role) 
        throws IOException 
    {}
}
