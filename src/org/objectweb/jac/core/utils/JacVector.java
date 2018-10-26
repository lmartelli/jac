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


package org.objectweb.jac.core.utils;

import java.util.Vector;

public class JacVector extends Vector {

    public JacVector() { super(); }
    public JacVector( int initialCapacity ) { super(initialCapacity); }
    
    public JacVector( int initialCapacity, int capacityIncrement ) {
       super( initialCapacity, capacityIncrement );
    }

    public JacVector( Object obj ) { super(); }
}
