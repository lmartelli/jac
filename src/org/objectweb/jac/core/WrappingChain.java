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

import org.aopalliance.intercept.Interceptor;
import org.objectweb.jac.util.ExtArrays;

public class WrappingChain {
    public Interceptor[] chain;

    public WrappingChain(Interceptor[] chain) {
        this.chain = chain;
    }
    public WrappingChain() {
        this.chain = ExtArrays.emptyInterceptorArray;
    }
    public void add(int rank,Interceptor interceptor) {
        Interceptor[] newChain = new Interceptor[chain.length+1];
        System.arraycopy(chain,0,newChain,0,rank);
        System.arraycopy(chain,rank,newChain,rank+1,chain.length-rank);
        newChain[rank] = interceptor;
        chain = newChain;
    }

    protected void ensureCapacity(int n) {
    }

    public boolean contains(Interceptor interceptor) {
        for (int i=chain.length-1; i>=0; i--) {
            if (chain[i]==interceptor)
                return true;
        }
        return false;
    }
    public void remove(int rank) {
        Interceptor[] newChain = new Interceptor[chain.length-1];
        System.arraycopy(chain,0,newChain,0,rank);
        System.arraycopy(chain,rank+1,newChain,rank,chain.length-rank-1);
        chain = newChain;      
    }
    public int size() {
        return chain.length;
    }
    public Interceptor get(int i) {
        return chain[i];
    }
    public String toString() {
        String result = "[";
        for (int i=0; i<chain.length;i++) {
            if (i!=0) {
                result += ",";
            }
            result += chain[i].toString();
        } 
      
        result += "]";
        return result;
    }
}
