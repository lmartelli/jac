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

package org.objectweb.jac.aspects.distribution.consistency;

import java.util.*;
import org.objectweb.jac.core.*;
import org.objectweb.jac.core.rtti.*;
import org.objectweb.jac.core.dist.*;

/**
 * This class handles any type of consistency protocol on a
 * replicaction group.
 *
 * <p>A replication group is a set of objects of the same class that
 * are related through a consistency protocol.<p>
 *
 * @author <a href="http://cedric.cnam.fr/~pawlak/index-english.html">Renaud Pawlak</a>
 */

public class Consistency {

   /** The knowledge style is defined by the user (the whole knowledge
       graph has to be defined at construction time). */
   public static int KS_USER = 0;
   /** With this knowledge style, all the replicas know the same
       unique replica (to be defined at construction time). */
   public static int KS_ONE = 1;
   /** With this knowledge style, all the replicas know all the other
       replicas (none info is required at construction time). */
   public static int KS_ALL = 2;
   /** With this knowledge style, each replica knows only one replica
       which is its neighbour in the replicas list (and last knows the
       first) (none info is required at construction time). */
   public static int KS_NEXT = 3;
   /** With this knowledge style, each replica knows only one replica
       which is its father in a binary tree (the top of the tree has
       to be defined at construction time). */
   public static int KS_BTREE = 4;

   /** Use to indicate that you need all the methods. */
   public static String ALL_METHODS = "ALL";
   /** Use to indicate that you need all the modifiers. */
   public static String ALL_MODIFIERS = "ALL_MODIFIERS";
   /** Use to indicate that you need all the getters. */
   public static String ALL_GETTERS = "ALL_GETTERS";

   /** The wrapper type that implements the consistency protocol. */
   Class consistencyWrapperType;
   /** The knowledge style. */
   int knowledgeStyle = 0;
   /** The knowledge graph. */
   int[] knowledgeGraph = null;
   
   /**
    * Creates a new consistency.
    *
    * <p>The knowledge style can be one the following:
    *
    * <ul><li>KS_USER: the knowledge style is defined by the user (the
    * whole knowledge graph has to be defined in the int[] argument
    * that defines the edges of the graph)</li>
    *
    * <li>KS_ONE: with this knowledge style, all the replicas know the
    * same unique replica (the knowledge graph argument contains only
    * one element)</li>
    *
    * <li>KS_ALL: with this knowledge style, all the replicas know all
    * the other replicas (the knowledge graph is empty)</li>
    *
    * <li>KS_NEXT: with this knowledge style, each replica knows only
    * one replica which is its neighbour in the replicas list (and
    * last knows the first) (the knowledge graph is empty)</li>
    *
    * <li>KS_BTREE: with this knowledge style, each replica knows only
    * one replica which is its father in a binary tree (the knowledge
    * graph argument contains only one element wich is the top of the
    * tree).</li></ul>
    * 
    * @param consistencyWrapperType the wrapper type that actually
    * implements the consistency protocol
    * @param knowledgeStyle can be KS_USER, KS_ONE, KS_ALL, KS_NEXT,
    * or KS_BTREE
    * @param knowledgeGraph depending on the knowledge style, can be
    * empty or describing what set of replicas are known by other
    * replicas */

   public Consistency ( Class consistencyWrapperType,
                        int knowledgeStyle, int[] knowledgeGraph ) {
      this.consistencyWrapperType = consistencyWrapperType;
      this.knowledgeStyle = knowledgeStyle;
      this.knowledgeGraph = knowledgeGraph;
   }

  /** 
    * Says if a replica is deployed on the given site.
    *
    * @param name the name of the replica
    * @param container the remote container to check
    * @return true if a replica of the given name is found on the
    * given container */
   
   public static boolean isReplicatedOn ( String name, RemoteContainer container ) {
      RemoteRef rr = container.bindTo ( "JAC_name_repository" );
      Object distobj = rr.invoke ( 
         "callOrgMethod", new Object[] { "getObject", new Object[] { name } } );
      if ( distobj == null ) {
         return false;
      }
      return true;
   }

   /**
    * Construct a real methods array with an array that can contain
    * consistency specific strings (like the one that indicates that
    * we need all the modifiers).
    *
    * @param type the class to expand
    * @param methods a set of methods to expand (can contain
    * ALL_METHODS, ALL_MODIFIERS, and ALL_GETTERS keywords)
    * @return a set of methods where the keywords have been expanded
    * with the corresponding method of the type */

   protected String[] expandMethods( Class type, String[] methods ) {
      if ( methods == null ) return null;
      Vector newVM = new Vector();
      for ( int i = 0; i < methods.length; i++ ) {
         if ( methods[i].equals(ALL_METHODS) ) {
            newVM.addAll( Arrays.asList(
               ClassRepository.getMethodsName(type) ) );
         }
         else if ( methods[i].equals(ALL_MODIFIERS) ) {
            newVM.addAll( Arrays.asList( 
               ClassRepository.getModifiersNames(type) ) );
         }
         else if ( methods[i].equals(ALL_GETTERS) ) {
            newVM.addAll( Arrays.asList( 
               ClassRepository.getGettersNames(type) ) );
         }
         else {
            newVM.add( methods[i] );
         }
      }
      String[] newMethods = new String[newVM.size()];
      for ( int i = 0; i < newMethods.length; i++ ) {
         newMethods[i] = (String) newVM.get(i);
      }
      return newMethods;
   }

 
   /**
    * Deploy the consistency on a set of remote objects.
    *
    * <p>PRE: the objects must have been previously deployed.
    * 
    * @param members the references on the replicated members
    * @param type the class of these members
    * @param readMethods the names of the methods of the type that
    * read the objects states
    * @param writeMethods the names of the methods of the type that
    * write the objects states
    * @param callMethods the names of the methods of the type that
    * neither read or write the objects states
    *
    * @see Deployment#deployStruct(Object[])
    * @see Deployment#deploy(Object[])
    * @see Deployment#replicateStruct(Object)
    * @see Deployment#replicate(Object)
    */

   public void deploy ( RemoteRef[] members, Class type, 
                        String[] readMethods,
                        String[] writeMethods,
                        String[] callMethods )
      throws WrongConsistencyDefinitionException {

      ConsistencyWrapper curwrapper;
      RemoteRef referee = null;
      boolean ok = false;

      readMethods = expandMethods( type, readMethods );
      writeMethods = expandMethods( type, writeMethods );
      callMethods = expandMethods( type, callMethods );

      try {

         String kstag = "";   

         if (knowledgeStyle == KS_ONE) {
            
            referee = members[knowledgeGraph[0]];
            for (int i = 0; i < members.length; i++) {
               wrapMember ( members[i],
                            new String[] {"whenRead", "whenWrite", "whenCall"},
                            new String[][] {readMethods, writeMethods, callMethods},
                            new RemoteRef[] { referee }, kstag );
            }
            ok = true;
         }
         
         if (knowledgeStyle == KS_ALL) {
            
            for (int i = 0; i < members.length; i++) {
               wrapMember ( members[i],
                            new String[] {"whenRead", "whenWrite", "whenCall"},
                            new String[][] {readMethods, writeMethods, callMethods},
                            members, kstag );
            }
            ok = true;
         }
         
         if (knowledgeStyle == KS_NEXT) {
            
            for (int i = 0; i < members.length; i++) {
               if ( i == members.length - 1 ) {
                  referee = members[0];
               } else {
                  referee = members[i + 1];
               }
               wrapMember ( members[i],
                            new String[] {"whenRead", "whenWrite", "whenCall"},
                            new String[][] {readMethods, writeMethods, callMethods},
                            new RemoteRef[] { referee }, kstag );
            }
            ok = true;
         }
      } catch ( Exception e ) { e.printStackTrace(); }

      if (!ok) {
         throw new WrongConsistencyDefinitionException();
      }
   }

   /** 
    * Internally used to wrap a remote member with a consistency
    * wrapper.
    *
    * @param member the member to wrap
    * @param wrappingMethods the methods of the consistency wrapper
    * that will wrap this member
    * @param wrappedMethods for each wrapping method, the set of
    * methods of the member that will be actually wrapped
    * @param knowledge the set of other members known by the newly
    * wrapped member
    * @param kstag the knowledge style tag */

   protected void wrapMember ( RemoteRef member, String[] wrappingMethods,
                               String[][] wrappedMethods, RemoteRef[] knowledge,
                               String kstag  )
   throws InstantiationException {

      if ( wrappedMethods != null && wrappingMethods != null ) {

         Class wrapper_type = consistencyWrapperType;
         ConsistencyWrapper wrapper = null;
         
         try {

            wrapper = (ConsistencyWrapper) wrapper_type.newInstance();
            
            if (wrapper == null) {
               throw new InstantiationException();
            }
            
            //wrapper.setKnownReplicas( knowledge );
            //wrapper.knowledgeStyle = knowledgeStyle;
            //wrapper.kstag = kstag;
            wrapper.setReadMethods ( wrappedMethods[0] );
            wrapper.setWriteMethods ( wrappedMethods[1] );
            wrapper.setCallMethods ( wrappedMethods[2] );
         
         } catch ( Exception e ) {
            throw new InstantiationException();
         }

         member.invoke(
            "wrap",
            new Object[] {
               wrapper,
               wrappingMethods,
               wrappedMethods
            }
         );
      }
   }

   /**
    * This method binds a new object to the group of replicas of this
    * consistency.
    *
    * @param name the name of the replica to bind to
    * @param tobind the object to be put in consistency with the group
    */
   public static void bindToDistObj(String name, Wrappee tobind) {
      Topology topology = Topology.get();
      if (topology.countContainers() < 1) 
         return;
      RemoteContainer rc = topology.getContainer(0);
      RemoteRef rr = rc.bindTo(name);
      rr.invokeRoleMethod (
         "addMember", 
         new Object[] { RemoteRef.create(NameRepository.get().getName(tobind), tobind) } );
  }   
                     
}

class WrongConsistencyDefinitionException extends Exception {}








