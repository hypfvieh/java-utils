package com.github.hypfvieh.collections;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Properties;

/**
 * Subclass of Properties that returns keys in insertion order by
 * keeping an internal insertion order set of keys updated on each
 * write operation (put, putAll, clear, remove). DOES NOT CONTROL
 * the usage of {@link java.util.Hashtable#entrySet()} which allows
 * write operations as well.
 */
@SuppressWarnings("serial")
public class LinkedProperties extends Properties {

    private final LinkedHashSet<Object> insertionOrderKeys = new LinkedHashSet<Object>();

    public LinkedProperties() {
        this((Properties) null);
    }

    public LinkedProperties(Properties _defaults) {
        super(_defaults);
    }

    /**
     * Constructor that reads a property list (key and element pairs)
     * from the input byte stream.
     * @param _inputStream the input stream
     * @throws IOException if an error occurred when reading from the input stream
     * @see #load(InputStream)
     */
    public LinkedProperties(InputStream _inputStream) throws IOException {
        load(_inputStream);
    }

    @Override
    public final synchronized Object put(Object _key, Object _value) {
        Object prevValue = super.put(_key, _value);
        insertionOrderKeys.add(_key);
        return prevValue;
    }

    /**
     * Overrides keys() in order to return an insertion order enumeration.
     * As this method is called by the various store methods in Properties,
     * output to file etc. will be sorted accordingly.
     */
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public final synchronized Enumeration keys() {
        return Collections.enumeration(insertionOrderKeys);
    }

    @Override
    public final synchronized Object remove(Object _key) {
        Object val = super.remove(_key);
        insertionOrderKeys.remove(_key);
        return val;
    }

    @Override
    public final synchronized void clear() {
        super.clear();
        insertionOrderKeys.clear();
    }

}
