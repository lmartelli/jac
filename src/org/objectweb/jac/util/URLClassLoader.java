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

  You should have received a copy of the GNU Lesser General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA */

package org.objectweb.jac.util;

import java.net.URL;
import java.lang.ClassLoader;
import java.net.URLStreamHandlerFactory;


/**
 * An URLClassLoader which lets you add an URL after it is created.
 */
public class URLClassLoader extends java.net.URLClassLoader
{
   public URLClassLoader(URL[] urls) {
      super(urls);
   }
   public URLClassLoader(URL[] urls, ClassLoader parent) {
      super(urls,parent);
   }
   public URLClassLoader(URL[] urls, ClassLoader parent, 
                         URLStreamHandlerFactory factory) {
      super(urls,parent,factory);
   }

   public void addURL(URL url) {
      super.addURL(url);
   }
}
