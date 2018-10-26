/*
  Copyright (C) 2004 Laurent Martelli <laurent@aopsys.com>

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

package org.objectweb.jac.core;

import java.util.Iterator;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.core.rtti.NoSuchClassException;

public class Imports {
    Logger logger = Logger.getLogger("imports");
    Vector imports = new Vector();
    public void add(String imp) {
        imports.add(imp);
    }
    public ClassItem getClass(String className) {
        logger.debug("Searching for "+className);
        Iterator i = imports.iterator();
        ClassRepository cr = ClassRepository.get();
        ClassItem cli = null;
        try {
            cli = cr.getNonPrimitiveClass(className);
            logger.debug("  Found "+className+": "+cli.getName());
        } catch (NoSuchClassException e) {
            while (i.hasNext()) {
                String imp = (String)i.next();
                try {
                    if (imp.endsWith("*")) {
                        cli = cr.getClass(
                            imp.substring(0,imp.length()-1)+className);
                    } else {
                        int index = imp.lastIndexOf('.');
                        if (imp.substring(index+1).equals(className)) {
                            cli = cr.getClass(imp);
                        }
                    }
                    if (cli!=null) {
                        logger.debug("  Found "+cli.getName());
                        break;
                    }
                } catch(NoSuchClassException e2) {
                }
            }
        }
        if (cli==null) {
            logger.debug("  Not found in "+imports);
            throw new NoSuchClassException(className);
        }
        return cli;
    }
}
