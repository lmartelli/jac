/**
 * This class delegates to java.util.Vector
 * This file was automatically generated by JAC (-g option)
 * DO NOT MODIFY
 * Author: Renaud Pawlak (pawlak@cnam.fr)
 */

package org.objectweb.jac.lib.java.util;

import java.io.Serializable;
import java.lang.Cloneable;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import org.objectweb.jac.util.Strings;


public class Vector extends java.util.Vector {

    //private java.util.Vector delegate = new java.util.Vector();

    public Object clone() {
        System.out.println("Vector.clone");
        Object result = null;
        try { 
           result = super.clone(); 
        } catch(Exception e) {
        }
        //((Vector)result).delegate = (java.util.Vector)delegate.clone();
        return result;
    }

    public Object elementAt(int index) {
        return super.elementAt(index);
    }

    public int indexOf(Object obj) {
        return super.indexOf(obj);
    }

    public int indexOf(Object obj, int index) {
        return super.indexOf(obj, index);
    }

    public int lastIndexOf(Object obj) {
        return super.lastIndexOf(obj);
    }

    public int lastIndexOf(Object obj, int index) {
        return super.lastIndexOf(obj, index);
    }

    public boolean addAll(Collection c) {
        return super.addAll(c);
    }

    public boolean addAll(int index, Collection c) {
        return super.addAll(index, c);
    }

    public boolean add(Object obj) {
        return super.add(obj);
    }

    public void add(int index, Object obj) {
        super.add(index, obj);
    }

    public Object get(int index) {
        return super.get(index);
    }

    public boolean contains(Object obj) {
        return super.contains(obj);
    }

    public int size() {
        return super.size();
    }

    public Object[] toArray() {
        return super.toArray();
    }

    public Object[] toArray(Object[] array) {
        return super.toArray(array);
    }

    public boolean remove(Object obj) {
        return super.remove(obj);
    }

    public Object remove(int index) {
        return super.remove(index);
    }

    public void removeRange(int from, int to) {
        super.removeRange(from,to);
    }

    public void addElement(Object obj) {
        super.addElement(obj);
    }

    public Enumeration elements() {
        return super.elements();
    }

    public void copyInto(Object[] array) {
        super.copyInto(array);
    }

    public void clear() {
        super.clear();
    }

    public boolean isEmpty() {
        return super.isEmpty();
    }

    public Object set(int p0, Object p1) {
        return super.set(p0, p1);
    }

    public void trimToSize() {
        super.trimToSize();
    }

    public void ensureCapacity(int p0) {
        super.ensureCapacity(p0);
    }

    public void setSize(int p0) {
        super.setSize(p0);
    }

    public int capacity() {
        return super.capacity();
    }

    public Object firstElement() {
        return super.firstElement();
    }

    public Object lastElement() {
        return super.lastElement();
    }

    public void setElementAt(Object p0, int p1) {
        super.setElementAt(p0, p1);
    }

    public void removeElementAt(int p0) {
        super.removeElementAt(p0);
    }

    public void insertElementAt(Object p0, int p1) {
        super.insertElementAt(p0, p1);
    }

    public boolean removeElement(Object p0) {
        return super.removeElement(p0);
    }

    public void removeAllElements() {
        super.removeAllElements();
    }

    public boolean containsAll(Collection p0) {
        return super.containsAll(p0);
    }

    public boolean removeAll(Collection p0) {
        return super.removeAll(p0);
    }

    public boolean retainAll(Collection p0) {
        return super.retainAll(p0);
    }

    public List subList(int p0, int p1) {
        return super.subList(p0, p1);
    }

    public Iterator iterator() {
        return super.iterator();
    }

    public ListIterator listIterator() {
        return super.listIterator();
    }

    public ListIterator listIterator(int p0) {
        return super.listIterator(p0);
    }

    public boolean equals(Object o) {
        return this == o;
    }

    public int hashCode() {
        return System.identityHashCode(this);
    }

    public String toString() {
        return getClass().getName()+"@"+System.identityHashCode(this);
    }
}