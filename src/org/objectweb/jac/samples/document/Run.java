/*
  Copyright (C) AOPSYS (http://www.aopsys.com)

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
*/

package org.objectweb.jac.samples.document;

public class Run {

    public static void main(String[] args) {
        
        ds = new DocumentServer();
        
        Document doc = 
            new Document(
                new String[] {
                    "This document is just a sample",
                    "to demonstrate the capability of",
                    "the tracing aspect to count its",
                    "lines in a simple way and an",
                    "optimized way"} );
        
        ds.addDocument(doc);
    }
    static DocumentServer ds;
}
