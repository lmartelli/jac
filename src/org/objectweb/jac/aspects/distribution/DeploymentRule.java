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
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA */

package org.objectweb.jac.aspects.distribution;

import java.io.Serializable;

import org.objectweb.jac.core.*;
import org.objectweb.jac.core.dist.*;
import gnu.regexp.*;
import java.util.*;

/**
 * A deployment rule parametrizes the deployment scheme of a set
 * of objects identified by a regular expression.
 *
 * <p>Each object that is named by the naming aspect so that it
 * matches the regular expression will be deployed regarding the
 * deployment rule.<p>
 *
 * The deployment aspect component uses a set of deployment rules
 * to know how to handle a newly used object.<p>
 *
 * @see DeploymentAC
 * @see Deployment
 * @see Topology
 *
 * @author <a href="http://cedric.cnam.fr/~pawlak/index-english.html">Renaud Pawlak</a>
 */

public class DeploymentRule implements Serializable {

   /** Stores the readable type of the rule. */
   String type = "";

   /** Stores the regular expression that represents the objects
       to be affected by this distribution rule. */
   RE nameRegexp;

   /** Stores the regular expression that identifies the remote
       containers where the deployment rule will be applied. */
   RE contRegexp;

   boolean state=false;

   /** Stores the knowledge style of the replication group. */
   int knowledgeStyle;

   /** Stores the knowledge graph if needed. */
   String[] knowledgeGraph;

   /** The objects that have allready been treated. */
   transient Hashtable treated = new Hashtable();

   AspectComponent ac;

   /**
    * Creates a new deployment rule in the general case.
    *
    * @param type a string that contains a readable representation of
    * what the rule is doing 
    * @param nameRegexp a regular expression that filters the objects
    * to which this rule will be applied 
    * @param contRegexp a regular expression that defines a set of
    * remote container where the objects of the rule will be
    * deployed 
    * @param state if true, the states of the objects are replicated,
    * else, it only deploys empty objects */

   public DeploymentRule (AspectComponent ac,
                          String type,
                          String nameRegexp,
                          String contRegexp,
                          boolean state ) {
      this.ac = ac;
      this.type = type;
      try {
         this.nameRegexp = new RE(nameRegexp);
         this.contRegexp = new RE(contRegexp);
      } catch (REException e) {
         System.out.println("Regexp construction failed : "+e);
      }
      this.state = state;
   }                           

   /**
    * Returns true if the deployment rule must by applied on a given
    * object.<p>
    *
    * @param candidate the tested object
    * @return true if the candiate matches the rule */

   public boolean isApplicableTo( Object candidate ) {
      String name = NameRepository.get().getName( candidate );
      if ( name == null ) return false;
      if ( nameRegexp.isMatch(name) ) {
         return true;
      }
      return false;
   }

   /**
    * Tells if the rule is already applied to a given object.<p>
    *
    * @param object the object to test
    * @return true if the rule is applied to the object */

   public boolean isAppliedTo( Object object ) {
      if (treated == null) treated = new Hashtable();
      if (treated.get( object ) != null) return true;
      if( type.equals( "dynamic client-server" ) ) {
         return true;
      }
      /*if ( ((Wrappee)object).isExtendedBy( consistencyType ) ) {
         return true;
      } else {
         return false;
         }*/
      return false;
   }

   /**
    * Applies the rule to the given object.<p>
    *
    * @param object the object on which the deployment rule will be
    * applied to */

   public void applyTo( Object object ) {
      if (treated == null) treated = new Hashtable();
      treated.put( object, "" );
      Topology topology = Topology.getPartialTopology( contRegexp );

      Deployment dep = new Deployment(ac,topology);
      
      if( state ) {
         dep.replicate( object );
      } else {
         dep.replicateStruct( object );
      }
   }

   /**
    * Returns a readable string representation of the type of this
    * distribution rule.
    *
    * @return the type */

   public String getType() {
      return type;
   }

}













































