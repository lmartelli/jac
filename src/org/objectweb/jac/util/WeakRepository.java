/*
  Copyright (C) 2001 Renaud Pawlak

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
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA */

package org.objectweb.jac.util;

/**
 * @author <a href="http://cedric.cnam.fr/~pawlak/index-english.html">Renaud Pawlak</a>
 */

/**
 * This class can be subclassed to create specific repositories.
 * 
 * <p>A repository class should be a singleton (a sole instance
 * class). Thus, the repository subclasses should define a static
 * field and a static method 'get' that returns the unique repository
 * for the class and that creates it if it does not exist. */

public class WeakRepository extends Repository {

    public WeakRepository() {
        super();
    }

    protected void init() {
        objects = new org.objectweb.jac.util.WeakHashMap();
        names = new java.util.WeakHashMap();
    }
}
