/*
  Copyright (C) 2002-2003 Laurent Martelli <laurent@aopsys.com>

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 2 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  USA */

package org.objectweb.jac.core;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.aopalliance.intercept.ConstructorInvocation;
import org.aopalliance.intercept.Interceptor;
import org.aopalliance.intercept.Invocation;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.objectweb.jac.core.rtti.AbstractMethodItem;
import org.objectweb.jac.core.rtti.ClassItem;
import org.objectweb.jac.core.rtti.ClassRepository;
import org.objectweb.jac.core.rtti.ConstructorItem;
import org.objectweb.jac.core.rtti.MethodItem;
import org.objectweb.jac.util.ExtArrays;
import org.objectweb.jac.util.Strings;


public class Interaction implements MethodInvocation, ConstructorInvocation {
    static final Logger logger = Logger.getLogger("interaction");

    public final Wrappee wrappee;
    public final AbstractMethodItem method;
    public final Object[] args;
    public int rank;
    public String cur_AC;
    public Interceptor[] wrappingChain;

    public Interaction(WrappingChain wrappingChain, Wrappee wrappee, 
                       AbstractMethodItem method, Object[] args) 
    {
        logger.debug("new Interaction(wrappee="+wrappee+", method="+method+Strings.hash(method)+
                     ", wrappingChain="+wrappingChain+
                     (wrappingChain!=null ? Strings.hash(wrappingChain) : "")+
                     ")");
        this.wrappingChain = 
            wrappingChain==null ? ExtArrays.emptyInterceptorArray : wrappingChain.chain;
        this.wrappee = wrappee;
        this.method = method;
        this.args = args;
        this.rank = 0;
    }

    public final Object proceed() {
        rank += 1;
        return Wrapping.nextWrapper(this);
    }

    public final Object invoke(Object substance) {
        return method.invoke(substance,args);
    }

    public final Class getActualClass() {
        if (wrappee!=null)
            return wrappee.getClass();
        else
            return method.getClassItem().getActualClass();
    }

    ClassItem cli;
    public final ClassItem getClassItem() {
        if (cli==null) {
            if (wrappee!=null) {
                cli = ClassRepository.get().getClass(wrappee);
            } else {
                cli = method.getClassItem();
            }
        }
        return cli;
    }

    public String toString() {
        return wrappee+"."+method+(args!=null?("("+Arrays.asList(args)+")"):"")+" rank="+rank;
    }


	// AOP Alliance interfaces implementations
    public Constructor getConstructor() {
        return ((ConstructorItem)method).getActualConstructor();
    }
	

    public Method getMethod() {
        return ((MethodItem)method).getActualMethod();
    }

    // TODO implement these methods?
    public Object getArgument(int index) {
        return args[index];
    }

    public void setArgument(int index, Object argument) {
        args[index]=argument;
    }

    public int getArgumentCount() {
        return args.length;
    }

    public Object[] getArguments() {
        return args;
    }

    public Object getThis() {
        return wrappee;
    }

    public AccessibleObject getStaticPart() {
        if(method instanceof ConstructorItem) {
            return getConstructor();
        } else {
            return getMethod();
        }
    }
}
