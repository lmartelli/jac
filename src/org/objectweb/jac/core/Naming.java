/*
  Copyright (C) 2001-2003 Renaud Pawlak <renaud@aopsys.com>

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

package org.objectweb.jac.core;

/**
 * This class implements useful static methods to manipulate JAC objects
 * and names.
 *
 * @see NameRepository
 * @see org.objectweb.jac.aspects.naming.NamingAC
 */

public class Naming {

    public static String FORCE_NAME="Naming.FORCE_NAME";

    /**
     * Sets the name of the next created object within the current
     * thread. */
    public static void setName(String name) {
        Collaboration.get().addAttribute(FORCE_NAME, name);
    }

    /**
     * Returns the JAC name of a JAC object.
     *
     * <p>Equivalent to <code>NameRepository.get().getName(object)</code>.
     *
     * @see NameRepository#getName(Object) */
    public static String getName(Object object) {
        return NameRepository.get().getName(object);
    }

    /**
     * Returns the JAC object named name.
     *
     * <p>Equivalent to <code>NameRepository.get().getObject(name)</code>.
     *
     * @see NameRepository#getObject(String) */
    public static Object getObject(String name) {
        return NameRepository.get().getObject(name);
    }

    /** Name of configuration file parser */
    public static final String PARSER_NAME = "parserimpl#0";
}
