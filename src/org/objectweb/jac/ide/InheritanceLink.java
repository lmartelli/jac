/*
  Copyright (C) 2002-2003 Renaud Pawlak <renaud@aopsys.com>

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

public class InheritanceLink extends Link {
    public InheritanceLink() {
    }
    
    public void initRoles() {
        startRole = new Role(this);
        endRole = new Role(this);
    }

    /**
     * Creates an inheritance link where cl extends superClass
     * @param cl the subclass
     * @param superClass the super class
     */ 
    public InheritanceLink(Class cl, Class superClass) {
        initRoles();
        setStart(cl);
        setEnd(superClass);
    }
}
