package com.github.hypfvieh.util;

import java.io.File;
import java.util.Map;

/**
 *
 * @author hypfvieh
 */
public class CompareUtil {

    private CompareUtil() {

    }

    /**
     * Checks if any of the passed in objects is null.
     * @param _objects array of objects, may be null
     * @return true if null found, false otherwise
     */
    public static boolean isAnyNull(Object... _objects) {
        if (_objects == null) {
            return true;
        }
        for (Object obj : _objects) {
            if (obj == null) {
                return true;
            }
        }
        return false;
    }

    public static void throwIfAnyNull(String _errMsg, Object... _objects) {
        if (isAnyNull(_objects)) {
            throw new NullPointerException(_errMsg);
        }
    }


    /**
     * Checks if any of the passed in files are non-existing.
     * @param _files array of files
     * @return the filename of the missing file, otherwise returns null.
     */
    public static String isAnyFileMissing(File... _files) {
        if (_files == null) {
            return "null";
        }
        for (File obj : _files) {
            if (obj != null && !obj.exists()) {
                return obj.toString();
            } else if (obj == null) {
                return null;
            }
        }
        return null;
    }

     /**
     * Returns the second parameter if the condition is true
     * or null if the condition is false. Returns empty string
     * instead of null for implementors of {@link CharSequence}.
     * @param _b condition
     * @param _t object
     * @return object or null
     */
    @SuppressWarnings("unchecked")
    public static <T> T ifTrue(boolean _b, T _t) {
        return _b ? _t : (_t instanceof CharSequence ? (T) "" : null);
    }

    public static <T> T ifFalse(boolean _b, T _t) {
        return ifTrue(!_b, _t);
    }


    /** Returns true if the specified object equals at least one of the specified other objects.
     * @param _obj object
     * @param _arrObj array of objects to compare to
     * @return true if equal, false otherwise or if either parameter is null
     */
   public static boolean equalsOne(Object _obj, Object... _arrObj) {
       if (_obj == null || _arrObj == null) {
           return false;
       }
       for (Object o : _arrObj) {
           if (o != null && _obj.equals(o)) {
               return true;
           }
       }
       return false;
   }

    /**
     * Checks whether a map contains all of the specified keys.
     * @param _map map
     * @param _keys one or more keys
     * @return true if all keys found in map, false otherwise
     */
    public static boolean mapContainsKeys(Map<?, ?> _map, Object... _keys) {
        if (_map == null) {
            return false;
        } else if (_keys == null) {
            return true;
        }
        for (Object key : _keys) {
            if (key != null && !_map.containsKey(key)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if given String starts with any of the other given parameters.
     *
     * @param _str string to check
     * @param _startStrings start strings to compare
     * @return true if any match, false otherwise
     */
    public static boolean startsWithAny(String _str, String... _startStrings) {
        if (_str == null || _startStrings == null || _startStrings.length == 0) {
            return false;
        }

        for (String start : _startStrings) {
            if (_str.startsWith(start)) {
                return true;
            }
        }

        return false;
    }

}
