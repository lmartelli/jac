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

  You should have received a copy of the GNU Lesser General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA */

package org.objectweb.jac.aspects.timestamp;

import org.objectweb.jac.core.rtti.FieldItem;



public interface TimestampConf {
    /**
     * Declares which objects should have timestamps
     *
     * @param classExpr which classes should have a timestamp
     * @param wrappeeExpr which instances should have a timestamp
     * @param stampsName name of Timestamps object to store stamps into
     */
    void declareTimestampedClasses(String classExpr, String wrappeeExpr, 
                                   String stampsName);

    /**
     * Specifies that objects at the end of a link should be touched
     * when an object is touched.
     *
     * @param link the link to follow
     * @param follow wether to follow the link or not
     */
    void followLink(FieldItem link, boolean follow);

}

