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
 * rtti.acc generation plugin
 */

public class PersistencePlugin extends AbstractPlugin {   

    public void genClassConfig(Writer output, 
                               Project project, Package pkg, Class cl) 
        throws IOException 
    {
        if (cl instanceof Repository) {
            output.write("registerStatics "+
                         cl.getGenerationFullName()+" \""+
                         cl.getGenerationName().toLowerCase()+"#0\";\n");
        }

        super.genClassConfig(output,project,pkg,cl);
    }
}

