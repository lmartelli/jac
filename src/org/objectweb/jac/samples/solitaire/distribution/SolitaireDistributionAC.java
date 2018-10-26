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

package org.objectweb.jac.samples.solitaire.distribution;

import org.objectweb.jac.core.*;
import org.objectweb.jac.wrappers.*;
import org.objectweb.jac.aspects.naming.*;
import org.objectweb.jac.aspects.distribution.*;

/**
 * This aspect component handles the distrution aspect for the agenda
 * example. */

public class SolitaireDistributionAC extends DistributionAC {

   public SolitaireDistributionAC() {

      /*      addDistributionRule(
         DistributionRule.makeReplicatedStrong(
            "mainVector[0-9]*", ".*", 
            new String[] { "addAll" },
            DistributionRule.PT_PUSH ) );
      */
   }
   
}












