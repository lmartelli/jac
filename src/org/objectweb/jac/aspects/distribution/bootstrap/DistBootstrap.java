/*
  Copyright (C) 2001 Renaud Pawlak <renaud@aopsys.com>

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

package org.objectweb.jac.aspects.distribution.bootstrap;

import org.apache.log4j.Logger;
import org.objectweb.jac.aspects.distribution.consistency.*;
import org.objectweb.jac.core.*;
import org.objectweb.jac.core.dist.*;
import org.objectweb.jac.util.Log;

/**
 * The following class bootstraps the Jac distributed system. */

public class DistBootstrap {
    static Logger logger = Logger.getLogger("dist");

    /**
     * This method is the entry point for a Jac application launched
     * with the -D option. */

    public static void main( String[] args ) throws Throwable {
        logger.debug("bootstrapping the distributed system");

        Topology.get().bootstrapFlag = true;
        logger.debug("local container is "+Distd.getLocalContainerName());
        Topology.get().addContainer(Distd.getLocalContainerName());

        logger.debug("topology is "+Topology.get());

        // installation of a strong consistency protocol on all
        // the system objects

        logger.debug("installing consistency protocols");

        ConsistencyWrapper.wrap((Wrappee)ApplicationRepository.get(),
                                StrongPushConsistencyWrapper.class,
                                null,
                                new String[] { "addApplication",
                                               "extend",
                                               "unextend" },
                                null,
                                ".*");

        ConsistencyWrapper.wrap(
            (Wrappee)Topology.get(),
            StrongPushConsistencyWrapper.class,
            null,
            new String[] { "addContainer(org.objectweb.jac.core.dist.RemoteContainer)", 
                           "removeContainer" },
            null,
            ".*");

        Topology.get().bootstrapFlag = false;
        logger.debug("end of bootstrap");
    }

}
