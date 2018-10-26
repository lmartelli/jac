/*
  Copyright (C) 2001-2003 Laurent Martelli <laurent@aopsys.com>

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

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.List;

/**
 * Configuration file parser interface
 */
public interface Parser {
   /**
    * Parse config file specified by its file path.
    * @param path path of file to parse
    * @param targetClass the class name of the aspect component
    * @param blockKeywords additional block keywords to factorize
    * configuration methods 
    */
   List parse(String path, String targetClass, Set blockKeywords) 
      throws IOException;
   /**
    * Parse a stream.
    * @param input the stream to parse
    * @param filePath path of file corresponding to the input stream
    * @param targetClass the class name of the aspect component
    * @param blockKeywords additional block keywords to factorize
    * configuration methods 
    */
   List parse(InputStream input, String filePath, String targetClass, 
              Set blockKeywords) 
      throws IOException;
}
