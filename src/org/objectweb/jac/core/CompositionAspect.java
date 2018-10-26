/*
  Copyright (C) 2001-2002 Renaud Pawlak.

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

import java.util.Vector;
import org.aopalliance.intercept.Interceptor;

/**
 * This special aspect component is used by the system to solve
 * inter-aspect composition issues.
 *
 * <p>It is typically used to order the different wrappers at runtime
 * (see <code>getWeaveTimeRank()</code>) or to check if two aspect
 * components are incompatible of dependent (see
 * <code>addIncompatibleACPairs()</code> and
 * <code>addDependentACPair()</code>).
 *
 * @author <a href="mailto:pawlak@cnam.fr">Renaud Pawlak</a>
 */

public class CompositionAspect extends AspectComponent {

	/** The name of the wrapping order property in the prop file. */
	//protected static String wrappingOrderProp = "org.objectweb.jac.comp.wrappingOrder";
	/** The name of the incompatible property in the prop file. */
	//protected static String incompatibleACsProp = "org.objectweb.jac.comp.imcompatibleACs";
	/** The name of the dependent property in the prop file. */
	//protected static String dependentACsProp = "org.objectweb.jac.comp.dependentACs";

	/** Store the default wrapping order. */
	protected Vector wrappingOrder = JacPropLoader.wrappingOrder;

	/** Store the exclusive aspect component pairs. */
	protected Vector incompatibleACs = JacPropLoader.incompatibleACs;

	/** Store the dependent aspect component pairs. */
	protected Vector dependentACs = JacPropLoader.dependentACs;

	/**
	 * The default contructor (reads the jac.prop file to initialize
	 * the composition aspect). */

	public CompositionAspect() {
	}

	/**
	 * When a wrappee method is beeing wrapped by a wrapper, this
	 * method is upcalled by the system to get the rank of the wrapper
	 * within the wrapping chain (the set of wrappers that allready
	 * wrap the wrappee method).
	 *
	 * @param wrappingChain the set of wrapping methods that allready
	 * wraps the wrappee method
	 * @param wrapper the wrapper that is going be added to the
	 * wrapping chain
     *
	 * @see Wrapping#wrap(Wrappee,Wrapper,AbstractMethodItem) 
     */
	public int getWeaveTimeRank(WrappingChain wrappingChain, Wrapper wrapper) {
		int i = 0;
		Interceptor[] chain = wrappingChain.chain;
		int wrapperRank = wrappingOrder.indexOf(wrapper.getClass().getName());
		for (; i < chain.length; i++) {
			if (wrapperRank
				<= wrappingOrder.indexOf(chain[i].getClass().getName())) {
				return i;
			}
		}
		/*Log.trace("composition",
		          "getting weave time rank for "+wrapper+"."+wrappingMethod+ 
		          "==>" + i + "/" + wrappingChain.size());*/
		return i;
	}

	/**
	 * Returns true if wrapperType1 has to be run before
	 * wrapperType2. This method is used by
	 * <code>getWeaveTimeRank()</code>.
	 *
	 * @param wrapperType1 the first type to check
	 * @param wrapperType2 the second type to check
	 * @return true if (wrapperType1 < wrapperType2 )
	 * @see #getWeaveTimeRank(WrappingChain,Wrapper)
	 */

	public final boolean areCorrectlyOrdered(
		String wrapperType1,
		String wrapperType2) {
		/*
		Log.trace("composition","areCorrectlyOrdered("+
		          wrapperType1+"("+i1+"),"+wrapperType2+"("+i1+"))");
		*/
		return (
			wrappingOrder.indexOf(wrapperType1)
				<= wrappingOrder.indexOf(wrapperType2));
		/*
		Log.trace("composition","areCorrectlyOrdered("+
		          wrapperType1+","+wrapperType2+") -> false");
		*/
	}

	/**
	 * The getter for the wrapping types order.
	 *
	 * @return a vector that contains the ordered wrapper classes */

	public final Vector getWrappingTypesOrder() {
		return wrappingOrder;
	}

	/**
	 * Add a new exlusive aspect component pair.
	 * 
	 * <p>If ac1 and ac2 are incompatible, then ac1 cannot be
	 * registered in the Aspect Component Manager if ac2 is already
	 * registered (and reverse).
	 * 
	 * <p>NOTE: this is a reflexive relation.
	 *
	 * @param ac1 the aspect component that is incompatible with ac2
	 * @param ac2 the aspect component that is incompatible with ac1
	 * @see ACManager#register(String,Object) */

	public final void addIncompatibleACPair(
		AspectComponent ac1,
		AspectComponent ac2) {
		incompatibleACs.add(ac1);
		incompatibleACs.add(ac2);
	}

	/**
	 * Add a new dependent aspect component pair.
	 * 
	 * <p>If ac1 depends on ac2, then ac1 cannot be registered in the
	 * Aspect Component Manager if ac2 is not already registered.
	 * 
	 * <p>NOTE: this is a transitive relation.
	 *
	 * @param ac1 the aspect component that depends on ac2
	 * @param ac2 the aspect component on which ac1 depends
	 * @see ACManager#register(String,Object) */

	public final void addDependentACPair(
		AspectComponent ac1,
		AspectComponent ac2) {
		dependentACs.add(ac1);
		dependentACs.add(ac2);
	}

	/**
	 * Returns true if the aspect components are incompatible.
	 *
	 * <p>NOTE: <code>areIncompatible(ac1,ac2)<code> equals
	 * <code>areIncompatible(ac2,ac1)<code>
	 *
	 * @param ac1 the first aspect component to check
	 * @param ac2 the second aspect component to check
	 * @return true if ac1 is incompatible with ac2
	 * @see #addIncompatibleACPair(AspectComponent,AspectComponent) */

	public final boolean areIncompatible(
		AspectComponent ac1,
		AspectComponent ac2) {
		for (int i = 0; i < incompatibleACs.size(); i += 2) {
			if ((ac1.equals(incompatibleACs.get(i))
				&& ac2.equals(incompatibleACs.get(i + 1)))
				|| (ac2.equals(incompatibleACs.get(i))
					&& ac1.equals(incompatibleACs.get(i + 1)))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if the aspect components are dependent.
	 *
	 * <p>NOTE: <code>areDependent(ac1,ac2)<code> not equals
	 * <code>areDependent(ac2,ac1)<code>
	 *
	 * @param ac1 the first aspect component to check
	 * @param ac2 the second aspect component to check
	 * @return true if ac1 depends on ac2
	 * @see #addDependentACPair(AspectComponent,AspectComponent) */

	public final boolean areDependent(
		AspectComponent ac1,
		AspectComponent ac2) {
		for (int i = 0; i < dependentACs.size(); i += 2) {
			if ((ac1.equals(dependentACs.get(i))
				&& ac2.equals(dependentACs.get(i + 1)))
				|| (ac2.equals(dependentACs.get(i))
					&& ac1.equals(dependentACs.get(i + 1)))) {
				return true;
			}
		}
		return false;
	}

}
