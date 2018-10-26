/*

  Copyright (C) 2001 Lionel Seinturier

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
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA */

package org.objectweb.jac.core.dist.rmi;

import org.objectweb.jac.core.dist.RemoteContainer;
import org.objectweb.jac.core.dist.Distd;

import java.rmi.Naming;


/**
 * RMINaming wraps the RMI naming registry.
 * The class method resolve()
 * is dynamically called by org.objectweb.jac.dist.RemoteContainer.resolve(String)
 * and statically by RMIRemoteRef.resolve(String).
 *
 * @author <a href="http://www-src.lip6.fr/homepages/Lionel.Seinturier/index-eng.html">Lionel Seinturier</a>
 */
 
public class RMINaming {

   /**
    * This method resolves a container from a container name.
    *
    * @param contName  the name of the container
    * @return the container reference, null if not resolved */
   
   public static RemoteContainer resolve( String contName ) {
   
	RMIRemoteContainerInterf stub = null;

      contName = Distd.getFullHostName( contName );
      
      try { 
         stub = (RMIRemoteContainerInterf) Naming.lookup(contName); 
         return new RMIRemoteContainerStub( stub, contName );
      } catch( Exception e ) { 
         return null;
      }

   }

}
