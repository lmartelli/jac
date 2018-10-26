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

/**
 * This is the conficuration interface of the remote access aspect.
 *
 * @author <a href="http://cedric.cnam.fr/~pawlak/index-english.html">Renaud Pawlak</a>
 *
 * @see RemoteAccessAC
 * @see DeploymentRule 
 */

public interface RemoteAccessConf {

   /**
    * This conficuration method creates remote-access stubs for the
    * objects matching names and classes on a server host.
    *
    * @param nameExpr an object pointcut expression
    * @param classExpr an class pointcut expression
    * @param serverHost the server where are located the server
    * objects */

   void createRemoteAccess( String nameExpr, 
                            String classExpr, 
                            String methodExpr,
                            String serverHost );

}
