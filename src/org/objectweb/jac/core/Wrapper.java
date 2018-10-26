/*
  Copyright (C) 2001-2002 Renaud Pawlak <renaud@aopsys.com>

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 2 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  USA */

package org.objectweb.jac.core;


import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Vector;
import org.aopalliance.intercept.ConstructorInterceptor;
import org.aopalliance.intercept.ConstructorInvocation;
import org.aopalliance.intercept.Invocation;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.objectweb.jac.core.rtti.ClassRepository;

/**
 * This class is the root class for all the wrappers. A wrapper is
 * part of a collaboration point when one or several of its methods
 * wrap a base program object (a wrappee). The set of wrappers
 * (methods) that wrap a wrappee (method) it called a wrapping
 * (method) chain.
 * 
 * <p>When a method call occurs on a wrappee, the system creates a new
 * collaboration point in the current collaboration and all the
 * wrappers are sequentially called.
 *
 * <p>A given wrapper passes the hand to the next wrapper (or to the
 * wrappee if last) by explicitly calling the <code>proceed()</code>
 * method.
 *
 * <p>For futher details about wrapping semantics, see the
 * <code>Wrapper.proceed()</code> method and the
 * <code>Wrappee.wrap()</code> methods.
 *
 * @author <a href="http://cedric.cnam.fr/~pawlak/index-english.html">Renaud Pawlak</a> */

public abstract class Wrapper
	implements Serializable, MethodInterceptor, ConstructorInterceptor 
{

    protected static final ClassRepository cr =  ClassRepository.get();

	/**
	 * The constructor sets the aspect component that owns the wrapper
	 * from the context. */

	public Wrapper(AspectComponent ac) {
		//      this.ac = ACManager.get().getName(ac);
		this.ac = ac;
		if (ac != null) {
			//Log.trace("wrapper","new wrapper for "+ac);
			ac.addWrapper(this);
			//Log.trace("wrapper","wrappers= "+ac.getWrappers());         
		}
	}

	/** The AC of the wrapper. */
	//   protected transient String acName;
	protected final transient AspectComponent ac;

	/**
	 * Returns true if the method is defined in the wrapper class.
	 *
	 * @param methodName the method to test
	 * @return true if wrapper method
	 */

	public static boolean defines(String methodName) {
		if (ClassRepository.getDirectMethodAccess(Wrapper.class, methodName)[0]
			!= null)
			return true;
		return false;
	}

	/**
	 * Returns the exception handlers of the given wrapper class. Are
	 * considered as exception handlers all methods whose first
	 * argument is an Exception.
	 *
	 * @param wrapperClass the wrapper class
	 * @return a vector containing the methods names */

	public static Vector getExceptionHandlers(Class wrapperClass) {
		Method[] methods = wrapperClass.getMethods();
		Vector ret = new Vector();
		for (int i = 0; i < methods.length; i++) {
			Class[] pts = methods[i].getParameterTypes();
			if (pts.length > 0) {
				Class cl = pts[0];
				boolean ok = false;
				if (cl != null) {
					while (cl != null
						&& (!cl.isPrimitive())
						&& cl != Object.class) {
						if (cl == Exception.class) {
							ok = true;
						}
						cl = cl.getSuperclass();
					}
				}
				if (ok) {
					ret.add(methods[i].getName());
				}
			}
		}
		return ret;
	}

	/**
	 * Returns the Aspect Component the wrapper belongs to by resolving
	 * the name.
	 * 
	 * @see #getAspectComponentName() */

	public AspectComponent getAspectComponent() {
		return ac;
		/*
		if (ac==null) return null;
		return (AspectComponent)ACManager.get().getObject(ac);
		*/
	}

	/**
	 * Returns the Aspect Component name the wrapper belongs to. By
	 * default, this name is set by the system when an aspect component
	 * wraps a given wrappee.
	 *
	 * @see Wrappee */

	public String getAspectComponentName() {
		return ACManager.get().getName(ac);
	}

	/**
	 * Set the aspect component name that owns the wrapper. The name of
	 * the aspect component is the one registered in
	 * <code>ACManager</code> (see <code>ACManager.get()</code>). This
	 * method should not be called directly since the aspect component
	 * is automatically set by the system when the wrap method is
	 * called on a wrappee.
	 * 
	 * @param ac the name of the aspect component
	 * @see Wrappee 
	 */
    /*
	public void setAspectComponent(AspectComponent ac) {
		this.ac = ac;
	}
    */
	/**
	 * String representation of a wrapper (eg <code>{wrapper
	 * className owned by aspectComponentName}</code>). */

	public String toString() {
		if (ac == null) {
			return super.toString() + "(nobody)";
		}
		return super.toString() + "(" + getAspectComponentName() + ")";
	}

	/**
	 * Run the next wrapper of the collaboration point.
	 *
	 * <p>A new collaboration point is initiated by the system when a
	 * method call occurs on a base program object (a wrappee). If the
	 * called method is wrapped by one or several wrapping methods
	 * (wrappers methods - see <code>Wrappee.wrap()</code>), then the
	 * system first upcalls all the wrapping methods.
	 *
	 * <p>A wrapping method is a regular Java method that takes no
	 * argument and returns an object. It can access the currently
	 * called method runtime caracteristics by using the
	 * <code>CollaborationParticipant</code> interface methods
	 * implemented by the wrapper (see <code>method()</code> or
	 * <code>args</code>).
	 *
	 * <p>The set of wrapping methods that wraps a wrappee method is
	 * called a wrapping method chain. The wrapping methods of a
	 * wrapping chain are sequentially called. A given wrapping method
	 * of the wrapping chain passes the hand to the next wrapping
	 * method of this chain by calling <code>proceed()</code>.
	 * 
	 * <p>A wrapper should ALWAYS call the proceed method except if it
	 * delibaratly wants to replace all the upcoming wrappers and base
	 * method functionalities (e.g. the
	 * <code>org.objectweb.jac.aspects.binding.ForwardingWrapper</code>).
	 *
	 * <p>The wrapping methods are called in a well-defined order. A
	 * wrapper cannot change this order by himself since it is the
	 * composition aspect that is responsible for setting this order
	 * (see <code>Wrappee.wrap()</code> and
	 * <code>CompositionAspect.getWeaveTimeRank()</code>).
	 * 
	 * <p>Within a clean aspect-oriented design, the wrapper can be
	 * shortcutted by its aspect component if needed (see
	 * <code>AspectComponent.beforeRunningWrapper()</code>).
	 *
	 * <p>In a wrapping method, proceed returns the value returned by
	 * the wrappee method. A wrapper can modify this return value if
	 * needed. The wrapping method code that is placed before the
	 * <code>proceed()</code> call is called <i>before</i> code (since
	 * it is executed before the wrapped method execution), and the
	 * code that is placed after the proceed call is called
	 * <i>after</i> code (since it is executed after).
	 *
	 * <p>A typical wrapping method looks like the following:
	 *
	 * <ul><pre>
	 * public class MyWrapper extends Wrapper {
	 *   public MyWrapper(AspectComponent ac) { super(ac); }
	 *   public Object invoke(MethodInvocation mi) {
	 *     // before code (can access the called method infos)
	 *     ...
	 *     Object result = proceed(mi);
	 *     // after code (can modify the returned value)
	 *     ...
	 *     // must return a value (most of the time the proceed() value)
	 *     return result;
	 *   }
	 *   public Object construct(ConstructorInvocation ci) {
	 *     return proceed(ci);
	 *   }
	 * } 
	 * </pre></ul>
	 *
	 * @return the final value returned by the current method call
	 * @see Wrapping#wrap(Wrappee,Wrapper,AbstractMethodItem)
	 * @see CollaborationParticipant
	 * @see CompositionAspect#getWeaveTimeRank(WrappingChain,Wrapper)
	 * @see AspectComponent#beforeRunningWrapper(Wrapper,String) 
     */
	public final Object proceed(Invocation invocation) {
		// The AOP Alliance Invocation interface is implemented by the Interaction class 
		Interaction interaction = (Interaction) invocation;
		interaction.rank += 1;
		return Wrapping.nextWrapper(interaction);
	}

	// following methods implement the CollaborationParticipant interface

	public final void attrdef(String name, Object value) {
		Collaboration.get().addAttribute(name, value);
	}

	public final Object attr(String name) {
		return Collaboration.get().getAttribute(name);
	}

	public Object invoke(MethodInvocation invocation) throws Throwable {
        throw new Exception("Wrapper "+this+" does not support method interception.");
	}
    
	public Object construct(ConstructorInvocation invocation) throws Throwable 
    {
        throw new Exception("Wrapper "+this+" does not support construction interception.");
    }

}
