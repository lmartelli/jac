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

package org.objectweb.jac.aspects.synchronization;

/**
 * This is the configuration interface of the Synchronization aspect.
 *
 * @see SynchronizationAC
 */

public interface SynchronizationConf {

   /**
    * This configuration method sets methods to be synchronized.
    *
    * <p>Two synchronized methods cannot execute at the same time on
    * the same object.</p>
    *
    * @param classes the classes the methods belong to
    * @param methods the methods to synchronize
    * @param objects the instances of the classes to be synchronized */

   void synchronize(String classes,String methods,String objects);

}
