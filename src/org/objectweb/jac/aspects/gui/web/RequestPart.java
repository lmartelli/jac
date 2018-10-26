/*
  Copyright (C) 2001 Laurent Martelli <laurent@aopsys.com>

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

package org.objectweb.jac.aspects.gui.web;

import java.io.InputStream;
import java.util.Map;


/**
 * This class represents a file part of multi-part HttpRequest.
 */
public class RequestPart {
   String name;
   String filename;
   InputStream data;
   Map headers;
   public RequestPart(String name, String filename, InputStream data, Map headers) {
      this.name = name;
      this.filename = filename;
      this.data = data;
      this.headers = headers;
   }

   public String getName() {
      return name;
   }

   public String getFilename() {
      return filename;
   }

   public InputStream getData() {
      return data;
   }

   public Map getHeaders() {
      return headers;
   }

   public String toString() {
      return "RequestPart(name="+name+",filename="+filename+")";
   }
}
