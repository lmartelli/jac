package org.objectweb.jac.util;

import java.lang.Cloneable;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;


public class WeakHashSet extends AbstractSet
    implements Set
{
    private transient WeakHashMap map;

    public WeakHashSet() {
        map = new WeakHashMap();
    }
    public WeakHashSet(Collection c) {
        map = new WeakHashMap(Math.max((int) (c.size()/.75f) + 1, 16));
        addAll(c);
    }
    public WeakHashSet(int initialCapacity, float loadFactor) {
        map = new WeakHashMap(initialCapacity, loadFactor);
    }
    public WeakHashSet(int initialCapacity) {
        map = new WeakHashMap(initialCapacity);
    }

    public Iterator iterator() {
        return map.keySet().iterator();
    }

    public int size() {
        return map.size();
    }
    public boolean isEmpty() {
        return map.isEmpty();
    }

    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    public boolean add(Object o) {
        return map.put(o, Boolean.TRUE)==null;
    }
    public boolean remove(Object o) {
        return map.remove(o)==Boolean.TRUE;
    }
    public void clear() {
        map.clear();
    }
}
