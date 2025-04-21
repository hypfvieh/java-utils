package com.github.hypfvieh.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public final class CollectionUtil {
    private CollectionUtil() {

    }

    /**
     * Verifies if given collection has any value.
     * @param _coll collection
     * @return true if given collection is not {@code null} and contains any value
     */
    public static boolean hasValue(Collection<?> _coll) {
        return _coll != null && !_coll.isEmpty();
    }

    /**
     * Creates a mutable List of items.
     *
     * @param _items Items to add
     *
     * @param <T> Type
     *
     * @return ArrayList
     */
    @SafeVarargs
    public static <T> List<T> mutableListOf(T... _items) {
        if (_items == null) {
            return new ArrayList<>();
        }

        return Arrays.stream(_items)
            .collect(Collectors.toList());
    }

    /**
     * Returns the first element in the collection or the default value if collection is {@code null} or empty.
     * @param <T> type
     * @param _items collection with elements
     * @param _default default if collection is empty/{@code null}
     * @return first element of collection or default
     */
    public static <T> T getFirstOrDefault(Collection<T> _items, T _default) {
        if (_items == null || _items.isEmpty()) {
            return _default;
        }

        return _items.iterator().next();
    }
}
