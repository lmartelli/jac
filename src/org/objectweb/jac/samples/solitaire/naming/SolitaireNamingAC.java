/*
  JAC-Core version 0.5.1

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


package org.objectweb.jac.samples.solitaire.naming;
import org.objectweb.jac.aspects.naming.*;
import org.objectweb.jac.lib.java.util.*;

import org.objectweb.jac.core.*;

/**
 *
 * @version 0.5.1
 * @author <a href="http://cedric.cnam.fr/~pawlak/index-english.html">Renaud Pawlak</a>
 * @author <a href="http://www-src.lip6.fr/homepages/Lionel.Seinturier/index-eng.html">Lionel Seinturier</a>
 */

/**
 * Handles the naming aspect within the JAC system.
 *
 * <p>This aspect component automatically registers all the created
 * JAC object into the sole instance of <code>NameRepository</code>.
 *
 * @see NameRepository */

public class SolitaireNamingAC extends NamingAC {
   int count = 0;
   /**
    * Generate default JAC object names.
    *  
    * <p>The programmer should overload this method to generically
    * generate a name for the given JAC object. By default, the
    * generated name is the name of the class concatenated with the
    * index of the JAC object in the array of all the current
    * instances of its class.
    *
    * <p>This method is used by this aspect component to name the JAC
    * objects.
    * 
    * @param object the object to give a name for
    * @return the proposed name
    *
    * @see whenNewInstance(Wrappee) */

   public String generateName ( Object object ) {
      if (object instanceof Vector) {
         if (method().equals("createChildren")) {
            return super.generateName(object);
         } else {
            System.out.println("dans GenerateName " + count + " : " + method());
            return "mainVector" + count++;
         }
      } else {  
         return super.generateName(object);
      }
   }
}













