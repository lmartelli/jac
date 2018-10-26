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

  You should have received a copy of the GNU Lesser Generaly Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA */

package org.objectweb.jac.aspects.distribution;

/**
 * This aspect component provides some consistency protocols that can
 * be settled on a set of replicas.
 *
 * @see ConsistencyAC
 * @author Renaud Pawlak
 */

public interface ConsistencyConf {

   /**
    * Adds a strong-push consistency protocol on a set of replicas
    * called <code>wrappeeName</code>.
    *
    * <p>The classical use of this consistency protocol is that any
    * replica forwards all the writing calls to all the replicas
    * located on the hosts defined by the consistency.
    *
    * <p>It is called "push" since the replica pushes the data to the
    * other replicas. Despite this strategy is the most curently used,
    * other strong or weak consistency strategies can be implemented by
    * other consistency protocols.
    *
    * @param wrappeeName the name of the object to be consistent
    * @param methods a pointcut expression that defines the methods
    * that will be pushed to the other replicas (generally the state
    * modifiers -- use the MODIFIERS keyword in your expression)
    * @param hosts the location of the replicas as a pointcut
    * expression */

   void addStrongPushConsistency( String wrappeeName, String methods, String hosts );

   /**
    * Adds a strong-pull consistency protocol on a set of replicas
    * called <code>wrappeeName</code>.
    *
    * <p>On contrary to the push consistency, this protocol pulls the
    * data from the other replicas. Indeed, each time a data is read
    * and is not locally available, it is fetched from the known
    * replicas.
    *
    * @param wrappeeName the name of the object to be consistent
    * @param methods a pointcut expression that defines the methods
    * that will be pulled from the other replicas (generally the state
    * readers -- use the keyword ACCESSORS in your expression)
    * @param hosts the location of the replicas as a pointcut
    * expression */

   void addStrongPullConsistency( String wrappeeName, String methods, String hosts );

}
