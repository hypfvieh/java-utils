package com.github.hypfvieh.collections;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * A bidirectional map.
 * If created from an existing map, the original map is not modified.
 * Instead a new Map objects of the same type
 * are created for a copy of the original map and an inverted map.
 * Should this fail, the type defaults to {@link LinkedHashMap}.
 *
 * Combinations of key-value must be unique in order to create and
 * maintain the corresponding inverse map. In addition, null is not accepted
 * as a valid value, as it is not permitted as a map key.
 * In these cases {@link IllegalArgumentException} is thrown.
 *
 * The map requires external synchronization.
 *
 */
public class BidiMap<K, V> implements Map<K, V> {

    private final Map<K, V> map;
    private final Map<V, K> inverseMap;

    public BidiMap() {
        this(null);
    }

    @SuppressWarnings("unchecked")
    public BidiMap(Map<K, V> _map) {
        map = (Map<K, V>) createMap(_map);
        inverseMap = (Map<V, K>) createMap(map);
        if (_map != null) {
            putAll(_map);
        }
    }

    @Override
    public V get(Object _key) {
        checkParms(_key);
        return map.get(_key);
    }

    public K getKey(V _val) {
        checkParms(_val);
        return inverseMap.get(_val);
    }

    @Override
    public boolean containsKey(Object _key) {
        checkParms(_key);
        return map.containsKey(_key);
    }

    @Override
    public boolean containsValue(Object _val) {
        checkParms(_val);
        return inverseMap.containsKey(_val);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public Set<K> keySet() {
        // prevent modifications using the keySet
        return Collections.unmodifiableSet(map.keySet());
    }

    @Override
    public Collection<V> values() {
        // prevent modifications using the values collection
        return Collections.unmodifiableCollection(map.values());
    }

    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        // prevent modifications using the entry set
        return Collections.unmodifiableSet(map.entrySet());
    }

    @Override
    public V remove(Object _key) {
        checkParms(_key);
        V removedValue = map.remove(_key);
        if (removedValue != null) {
            inverseMap.remove(removedValue);
        }
        return removedValue;
    }

    /**
     * Removes the mapping for a value from this map if it is present.
     *
     * @param _val value whose mapping is to be removed from the map
     * @return the previous key associated with <tt>value</tt>.
     */
    public K removeValue(V _val) {
        checkParms(_val);
        K removedKey = inverseMap.remove(_val);
        if (removedKey != null) {
            map.remove(removedKey);
        }
        return removedKey;
    }

    @Override
    public V put(K _key, V _val) {
        checkParms(_key, _val);

        K invKey = inverseMap.get(_val);
        if (invKey == null) { // value does not exist in map
            if (containsKey(_key)) { // prevent storage of different values under same key
                throw new IllegalArgumentException("Key [" + _key + "] not unique in bidirectional map, cannot put(" + _key + ", " + _val + ").");
            }
        } else { // value exists in map
            if (!_key.equals(invKey)) { // prevent storage of same value under different key
                throw new IllegalArgumentException("Value [" + _val + "] not unique in bidirectional map, cannot put(" + _key + ", " + _val + ").");
            }
        }

        V prevVal = map.put(_key, _val);
        inverseMap.put(_val, _key);
        return prevVal;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> _map) {
        for (java.util.Map.Entry<? extends K, ? extends V> entry : _map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        map.clear();
        inverseMap.clear();
    }

    @Override
    public final String toString() {
        return getClass().getName() + "[" + map + "]";
    }

    static Map<?, ?> createMap(Map<?, ?> _map) {
        Map<?, ?> newMap;
        try {
            newMap = _map.getClass().getDeclaredConstructor().newInstance();
        } catch (Exception _ex) {
            newMap = new LinkedHashMap<>();
        }
        return newMap;
    }

    static void checkParms(Object... _objs) {
        if (_objs != null) {
            for (Object obj : _objs) {
                if (obj == null) {
                    throw new IllegalArgumentException("Null parameter not allowed.");
                }
            }
        }
    }

}
