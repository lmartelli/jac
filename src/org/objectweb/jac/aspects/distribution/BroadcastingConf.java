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
 * This aspect component implements a simple broadcasting aspect.
 *
 * <p>Principles: a broadcaster, located on a given host forwards some
 * calls to a set of replicas located on remote hosts.
 *
 * @see BroadcastingAC
 * @author Renaud Pawlak
 */

public interface BroadcastingConf {

   /**
    * This configuration method allows the user to define a
    * broacaster.
    *
    * @param wrappeeName the broadcaster object and replicas names
    * @param methods a poincut expression that defines the methods
    * that will be broadcasted
    * @param broadcasterHost the broadcaster host
    * @param replicasHost the hosts of the replicas, as a regexp */

   void addBroadcaster(String wrappeeName,
                       String methods,
                       String broadcasterHost, 
                       String replicasHost);

}
