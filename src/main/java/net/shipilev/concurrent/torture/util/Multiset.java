package net.shipilev.concurrent.torture.util;

import java.util.Collection;
import java.util.Collections;
import java.util.TreeMap;

public class Multiset<T> {

    private final TreeMap<T, Integer> map;

    public Multiset() {
        this.map = new TreeMap<T, Integer>();
    }

    public void add(T elem) {
        Integer v = map.get(elem);
        if (v == null) {
            v = 0;
        }
        v++;
        map.put(elem, v);
    }

    public Collection<T> keys() {
        return Collections.unmodifiableSet(map.keySet());
    }

    public int count(T elem) {
        Integer v = map.get(elem);
        return (v == null) ? 0 : v;
    }
}
