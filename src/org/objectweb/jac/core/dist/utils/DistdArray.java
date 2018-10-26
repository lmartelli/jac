/*
  Renaud Pawlak, pawlak@cnam.fr, CEDRIC Laboratory, Paris, France.
  Lionel Seinturier, Lionel.Seinturier@lip6.fr, LIP6, Paris, France.

  JAC-Core is free software. You can redistribute it and/or modify it
  under the terms of the GNU Library General Public License as
  published by the Free Software Foundation.
  
  JAC-Core is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

  This work uses the Javassist system - Copyright (c) 1999-2000
  Shigeru Chiba, University of Tsukuba, Japan.  All Rights Reserved.  */

package org.objectweb.jac.core.dist.utils;

import java.io.Serializable;

/**
 * DistdArray is an utility class used to marshall/unmarshall array objects
 * in remote invocations.
 *
 * @author <a href="http://cedric.cnam.fr/~pawlak/index-english.html">Renaud Pawlak</a>
 * @author <a href="http://www-src.lip6.fr/homepages/Lionel.Seinturier/index-eng.html">Lionel Seinturier</a>
 */

public class DistdArray implements Serializable {

      /** The class name of the array elements. */
      String componentTypeName;
   
      /** The length of the array. */
      int length;
   
      public DistdArray( String componentTypeName, int length ) {
         this.componentTypeName = componentTypeName;
         this.length = length;
      }

      public String getComponentTypeName() { return componentTypeName; }
      public int getLength() { return length; }
}
