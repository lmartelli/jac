/*
  Copyright (C) 2001-2003 Renaud Pawlak <renaud@aopsys.com>

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

package org.objectweb.jac.aspects.tracing;

import java.util.*;
import org.objectweb.jac.core.*;
import org.objectweb.jac.util.Log;
import org.objectweb.jac.wrappers.*;

/**
 * This sample aspect component traces the calls on all the objects of
 * the program.
 *
 * <p>To be active this aspect component must be configured with the
 * <code>addTrace</code> method.
 *
 * @see org.objectweb.jac.wrappers.VerboseWrapper */

public class TracingAC extends AspectComponent implements TracingConf  {

    VerboseWrapper timedWrapper;
    VerboseWrapper namedWrapper;
    VerboseWrapper stackWrapper;
    VerboseWrapper wrappersWrapper;

    public TracingAC () {
        timedWrapper = new VerboseWrapper(this,VerboseWrapper.TIMED);
        namedWrapper = new VerboseWrapper(this,VerboseWrapper.NAMED);
        stackWrapper = new VerboseWrapper(this,VerboseWrapper.STACK);
        wrappersWrapper = new VerboseWrapper(this,VerboseWrapper.WRAPPERS);
    }
    
    public void addTrace(String wrappeeExpr, 
                         String wrappeeClassExpr, 
                         String wrappeeMethodExpr) {
        pointcut(wrappeeExpr, wrappeeClassExpr, wrappeeMethodExpr,
                 timedWrapper, null);
    }

    // TODO: Handle other types of traces

    public void addNamedTrace(String wrappeeExpr, 
                              String wrappeeClassExpr, 
                              String wrappeeMethodExpr) {
        pointcut(wrappeeExpr, wrappeeClassExpr, wrappeeMethodExpr,
                 namedWrapper, null);
    }

    public void addStackTrace(String wrappeeExpr, 
                              String wrappeeClassExpr, 
                              String wrappeeMethodExpr) {

        pointcut(wrappeeExpr, wrappeeClassExpr, wrappeeMethodExpr,
                 stackWrapper, null);
    }

    public void addWrappersTrace(String wrappeeExpr, 
                                 String wrappeeClassExpr, 
                                 String wrappeeMethodExpr) {
        pointcut(wrappeeExpr, wrappeeClassExpr, wrappeeMethodExpr,
                 wrappersWrapper,null);
    }

    public void addRecording(String wrappeeExpr, 
                             String wrappeeClassExpr, 
                             String wrappeeMethodExpr) {

        pointcut(wrappeeExpr+" && !recorder0",
                 wrappeeClassExpr,
                 wrappeeMethodExpr,
                 RecordingWrapper.class.getName(), null, false);
    }

    Hashtable counters = new Hashtable();

    public void addCounter(String name,
                           String wrappeeExpr,
                           String wrappeeClassExpr, 
                           String wrappeeMethodExpr) {

        Counter c = (Counter) counters.get(name);
        if (c == null) {
            c = new Counter();
            counters.put(name, c);
        }
        pointcut(wrappeeExpr, wrappeeClassExpr, wrappeeMethodExpr,
                 new SimpleCountingWrapper(this,c), null);
    }

    public void addOptimizedCounter(String name,
                                    String wrappeeExpr, 
                                    String wrappeeClassExpr, 
                                    String wrappeeMethodExpr,
                                    String fieldName,
                                    String argNumber) {
      
        Counter c = (Counter) counters.get(name);
        if (c == null) {
            c = new Counter();
            counters.put(name,c);
        }
        if (!fieldName.equals("")) {
            pointcut(wrappeeExpr, wrappeeClassExpr, 
                     wrappeeMethodExpr, 
                     new OptimizedCountingWrapper(this,c,fieldName), 
                     null);
        }
        if (!argNumber.equals("")) {
            pointcut(wrappeeExpr, wrappeeClassExpr, 
                     wrappeeMethodExpr, 
                     new OptimizedCountingWrapper(
                         this, c, (new Integer(argNumber)).intValue()), 
                     null );
        }
    }

    /**
     * Skips the counting wrapper if this call is part of a global
     * conting optimization performed by an
     * <code>OptimizedCountingWrapper</code>
     *
     * @param wrapper the wrapper 
     * @param wrappingMethod the wrapping method that is about to be
     * run
     * @see OptimizedCountingWrapper */

    public boolean beforeRunningWrapper (Wrapper wrapper, 
                                         String wrappingMethod) {
        if (attr("tracing.globalIncr")!=null) 
            return false;
        return true;
    }


}
