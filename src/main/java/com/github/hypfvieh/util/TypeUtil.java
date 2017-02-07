package com.github.hypfvieh.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Class providing a set of methods for converting and/or checking of various data types.
 *
 * @author hypfvieh
 */
public final class TypeUtil {


    private TypeUtil() {

    }

    /**
     * Returns true if string matches certain boolean values.
     *
     * @param _str
     * @return
     * @deprecated use {@link ConverterUtilTest#strToBool(String)}
     */
    @Deprecated
    public static boolean strToBool(String _str) {
        return ConverterUtil.strToBool(_str);
    }

    /**
     * Check if string is integer (including negative integers).
     *
     * @param _str
     * @return
     */
    public static boolean isInteger(String _str) {
        return isInteger(_str, true);
    }

    /**
     * Check if string is an either positive or negative integer.
     *
     * @param _str
     * @param _allowNegative negative integer allowed
     * @return
     */
    public static boolean isInteger(String _str, boolean _allowNegative) {
        if (_str == null) {
            return false;
        }

        String regex = "[0-9]+$";
        if (_allowNegative) {
            regex = "^-?" + regex;
        } else {
            regex = "^" + regex;
        }
        return _str.matches(regex);
    }

    /**
     * Check if the given value is a valid network port (1 - 65535).
     * @param _port
     * @param _allowWellKnown allow ports below 1024 (aka reserved well known ports)
     * @return
     */
    public static boolean isValidNetworkPort(int _port, boolean _allowWellKnown) {
        if (_allowWellKnown) {
            return _port > 0 && _port < 65536;
        }

        return _port > 1024 && _port < 65536;
    }

    /**
     * @see #isValidNetworkPort(int, boolean)
     * @param _str
     * @param _allowWellKnown
     * @return
     */
    public static boolean isValidNetworkPort(String _str, boolean _allowWellKnown) {
        if (isInteger(_str, false)) {
            return isValidNetworkPort(Integer.parseInt(_str), _allowWellKnown);
        }
        return false;
    }

    /**
     * Checks if given String is a valid regular expression.
     *
     * @param _regExStr
     * @return true if given string is valid regex, false otherwise
     */
    public static boolean isValidRegex(String _regExStr) {
        return createRegExPatternIfValid(_regExStr) != null ? true : false;
    }

    /**
     * Creates a RegEx Pattern object, if given String is a valid regular expression.
     *
     * @param _regExStr
     * @return Pattern-Object or null if given String is no valid RegEx
     */
    public static Pattern createRegExPatternIfValid(String _regExStr) {
        if (StringUtil.isBlank(_regExStr)) {
            return null;
        }

        Pattern pattern;
        try {
            pattern = Pattern.compile(_regExStr);
        } catch (PatternSyntaxException _ex) {
            return null;
        }

        return pattern;
    }

    /**
     * Creates a list from a varargs parameter array.
     * The generic list is created with the same type as the parameters.
     * @param _entries list entries
     * @return list
     */
    @SafeVarargs
    public static <T> List<T> createList(T... _entries) {
        List<T> l = new ArrayList<T>();
        if (_entries != null) {
            l.addAll(Arrays.asList(_entries));
        }
        return l;
    }

    /**
     * Creates a map from the even-sized parameter array.
     * @param _args parameter array, any type
     * @return map of parameter type
     */
    @SafeVarargs
    public static <T> Map<T, T> createMap(T... _args) {
        Map<T, T> map = new HashMap<T, T>();
        if (_args != null) {
            if (_args.length % 2 != 0) {
                throw new IllegalArgumentException("Even number of parameters required to create map: " + Arrays.toString(_args));
            }
            for (int i = 0; i < _args.length;) {
                map.put(_args[i], _args[i + 1]);
                i += 2;
            }
        }
        return map;
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
    * Splits _map to a list of maps where each map has _nbElements.
    * Last map in list maybe shorter if _map.size() is not divideable by _nElements.
    *
    * @param _map
    * @param _nbElements
    * @return List of Maps
    * @throws IllegalAccessException
    * @throws InstantiationException
    */
   @SuppressWarnings("unchecked")
   public static <K, V> List<Map<K, V>> splitMap(Map<K, V> _map, int _nbElements) throws InstantiationException, IllegalAccessException  {
       List<Map<K, V>> lofm = new ArrayList<>();
       lofm.add((Map<K, V>) _map.getClass().newInstance());
       for (Entry<K, V> e : _map.entrySet()) {
           Map<K, V> lastSubMap = lofm.get(lofm.size() - 1);
           if (lastSubMap.size() == _nbElements) {
               lofm.add((Map<K, V>) _map.getClass().newInstance());
               lastSubMap = lofm.get(lofm.size() - 1);
           }
           lastSubMap.put(e.getKey(), e.getValue());
       }
       return lofm;
   }

   /**
    * Split a List into equal parts.
    * Last list could be shorter than _elements.
    *
    * @param _list
    * @param _elements
    * @return
    */
   public static <T> List<List<T>> splitList(List<T> _list, int _elements) {
       List<List<T>> partitions = new ArrayList<List<T>>();
       for (int i = 0; i < _list.size(); i += _elements) {
           partitions.add(_list.subList(i,
                   Math.min(i + _elements, _list.size())));
       }

       return partitions;
   }


   /**
    * Factory method for {@link Properties} from an even-sized String array.
    * @param _keysAndVals String array of keys and values, may be null or even-numbered String array
    * @return new Properties object
    */
   public static Properties createProperties(String... _keysAndVals) {
       if (_keysAndVals != null && _keysAndVals.length % 2 != 0) {
           throw new IllegalArgumentException("Even number of String parameters required.");
       }
       Properties props = new Properties();
       if (_keysAndVals != null) {
           for (int i = 0; i < _keysAndVals.length; i+=2) {
               props.setProperty(_keysAndVals[i], _keysAndVals[i + 1]);
           }
       }
       return props;
   }

   /**
    * Returns integer converted from string or default if not string was not a integer type.
    *
    * @param _possibleInt
    * @param _default
    * @return
    */
   public static int defaultIfNotInteger(String _possibleInt, int _default) {
       if (isInteger(_possibleInt)) {
           return Integer.parseInt(_possibleInt);
       }
       return _default;
   }
}
