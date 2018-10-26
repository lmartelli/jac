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

package org.objectweb.jac.aspects.tracing;

import org.aopalliance.intercept.ConstructorInvocation;
import org.aopalliance.intercept.MethodInvocation;
import org.objectweb.jac.core.*;

public class RecordingWrapper extends Wrapper {
    public RecordingWrapper(AspectComponent ac) {
        super(ac);
    }

    public Object record(Interaction interaction) {
        Object ret = proceed(interaction);
        if(Recorder.get().isRecording()) {
            Recorder.get().recordMethodCall(interaction.wrappee, 
                                            interaction.method.getName(), 
                                            interaction.args);
        }
        return ret;
    }

    public Object invoke(MethodInvocation invocation) throws Throwable {
        return record((Interaction)invocation);
    }
    public Object construct(ConstructorInvocation invocation) throws Throwable {
        return record((Interaction)invocation);
    }
}
