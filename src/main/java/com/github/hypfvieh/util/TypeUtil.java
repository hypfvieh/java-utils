package com.github.hypfvieh.util;

import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormatSymbols;
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
     * @param _str string to validate
     * @return true if string represents a true (like 'yes','ja',1) value, false otherwise
     * @deprecated use {@link ConverterUtil#strToBool(String)}
     */
    @Deprecated
    public static boolean strToBool(String _str) {
        return ConverterUtil.strToBool(_str);
    }

    /**
     * Checks if the given string is a valid double (including negative values).
     * Separator is based on the used locale.
     * @param _str string to check
     * @return true if valid double, false otherwise
     */
    public static boolean isDouble(String _str) {
        return isDouble(_str, true);
    }

    /**
     * Checks if the given string is a valid double (including negative values) using the given separator.
     * @param _str string to check
     * @param _separator separator to use
     * @return true if valid double, false otherwise
     */
    public static boolean isDouble(String _str, char _separator) {
        return isDouble(_str, _separator, true);
    }

    /**
     * Checks if the given string is a valid double.
     * The used separator is based on the used system locale
     * @param _str string to validate
     * @param _allowNegative set to true if negative double should be allowed
     * @return true if given string is double, false otherwise
     */
    public static boolean isDouble(String _str, boolean _allowNegative) {
        return isDouble(_str, DecimalFormatSymbols.getInstance().getDecimalSeparator(), _allowNegative);
    }

    /**
     * Checks if the given string is a valid double.
     * @param _str string to validate
     * @param _separator seperator used (e.g. "." (dot) or "," (comma))
     * @param _allowNegative set to true if negative double should be allowed
     * @return true if given string is double, false otherwise
     */
    public static boolean isDouble(String _str, char _separator, boolean _allowNegative) {
        String pattern = "\\d+XXX\\d+$";
        if (_separator == '.') {
            pattern = pattern.replace("XXX", "\\.?");
        } else {
            pattern = pattern.replace("XXX", _separator + "?");
        }

        if (_allowNegative) {
            pattern = "^-?" + pattern;
        } else {
            pattern = "^" + pattern;
        }

        if (_str != null) {
            return _str.matches(pattern) || isInteger(_str, _allowNegative);
        }
        return false;
    }

    /**
     * Check if string is integer (including negative integers).
     *
     * @param _str string to check
     * @return true if integer (positive or negative), false otherwise
     */
    public static boolean isInteger(String _str) {
        return isInteger(_str, true);
    }

    /**
     * Check if string is an either positive or negative integer.
     *
     * @param _str string to validate
     * @param _allowNegative negative integer allowed
     * @return true if integer, false otherwise
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
     * @param _port 'port' to check
     * @param _allowWellKnown allow ports below 1024 (aka reserved well known ports)
     * @return true if int is a valid network port, false otherwise
     */
    public static boolean isValidNetworkPort(int _port, boolean _allowWellKnown) {
        if (_allowWellKnown) {
            return _port > 0 && _port < 65536;
        }

        return _port > 1024 && _port < 65536;
    }

    /**
     * @see #isValidNetworkPort(int, boolean)
     * @param _str string to check
     * @param _allowWellKnown allow well known port
     * @return true if valid port, false otherwise
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
     * @param _regExStr regex string to check
     * @return true if given string is valid regex, false otherwise
     */
    public static boolean isValidRegex(String _regExStr) {
        return createRegExPatternIfValid(_regExStr) != null;
    }

    /**
     * Creates a RegEx Pattern object, if given String is a valid regular expression.
     *
     * @param _regExStr regex string to check
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
     * @param <T> list type
     * @return list
     */
    @SafeVarargs
    public static <T> List<T> createList(T... _entries) {
        List<T> l = new ArrayList<>();
        if (_entries != null) {
            l.addAll(Arrays.asList(_entries));
        }
        return l;
    }

    /**
     * Creates a map from the even-sized parameter array.
     * @param <T> map type
     * @param _args parameter array, any type
     * @return map of parameter type
     */
    @SafeVarargs
    public static <T> Map<T, T> createMap(T... _args) {
        Map<T, T> map = new HashMap<>();
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
     * @deprecated Use {@link CompareUtil#isAnyNull(Object...)}
     */
    @Deprecated
    public static boolean isAnyNull(Object... _objects) {
        return CompareUtil.isAnyNull(_objects);
    }

    /**
     * @param _errMsg error message
     * @param _objects objects to check
     * @see CompareUtil#throwIfAnyNull(String, Object...)
     * @deprecated Use {@link CompareUtil#throwIfAnyNull(String, Object...)}
     */
    @Deprecated
    public static void throwIfAnyNull(String _errMsg, Object... _objects) {
        if (isAnyNull(_objects)) {
            throw new NullPointerException(_errMsg);
        }
    }

    /** Returns true if the specified object equals at least one of the specified other objects.
     * @param _obj object
     * @param _arrObj array of objects to compare to
     * @return true if equal, false otherwise or if either parameter is null
     *
     * @deprecated Use {@link CompareUtil#equalsOne(Object, Object...)}
     */
   @Deprecated
   public static boolean equalsOne(Object _obj, Object... _arrObj) {
       return CompareUtil.equalsOne(_obj, _arrObj);
   }

    /**
    * Splits _map to a list of maps where each map has _nbElements.
    * Last map in list maybe shorter if _map.size() is not divideable by _nElements.
    *
    * @param _map map to split
    * @param _nbElements elements per map
    * @param <K> key type
    * @param <V> value type
    * @return List of Maps
    * @throws IllegalAccessException on error
    * @throws InstantiationException on error
    */
   @SuppressWarnings("unchecked")
   public static <K, V> List<Map<K, V>> splitMap(Map<K, V> _map, int _nbElements) throws InstantiationException, IllegalAccessException  {
       List<Map<K, V>> lofm = new ArrayList<>();
       try {
        lofm.add(_map.getClass().getDeclaredConstructor().newInstance());
           for (Entry<K, V> e : _map.entrySet()) {
               Map<K, V> lastSubMap = lofm.get(lofm.size() - 1);
               if (lastSubMap.size() == _nbElements) {
                   lofm.add(_map.getClass().getDeclaredConstructor().newInstance());
                   lastSubMap = lofm.get(lofm.size() - 1);
               }
               lastSubMap.put(e.getKey(), e.getValue());
           }
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
            | NoSuchMethodException | SecurityException _ex) {
        throw new InstantiationException(_ex.getMessage());
    }
       return lofm;
   }

   /**
    * Split a List into equal parts.
    * Last list could be shorter than _elements.
    *
    * @param _list list to split
    * @param _elements elements per list
    * @param <T> type
    * @return list of lists
    */
   public static <T> List<List<T>> splitList(List<T> _list, int _elements) {
       List<List<T>> partitions = new ArrayList<>();
       for (int i = 0; i < _list.size(); i += _elements) {
           partitions.add(_list.subList(i,
                   Math.min(i + _elements, _list.size())));
       }

       return partitions;
   }


   /**
    * Splits a given list to _listCount subLists.
    * Last list maybe shorter or longer than all previous list (depending on _list size).
    *
    * @param _list list to split
    * @param _listCount sublists to create
    * @return List of Lists, null if _list was null, empty list if _list was empty
    */
   public static <T> List<List<T>> splitListToSubLists(List<T> _list, int _listCount) {
       if (_list == null) {
           return null;
       }

       List<List<T>> results = new ArrayList<>();
       if (_list.isEmpty()) {
           results.add(_list);
           return results;
       }

       int partPerList = _list.size() / _listCount;
       int remainder = _list.size() % _listCount;

       for (int i = 0; i < _list.size() -remainder; i+=partPerList) {
           if (i+partPerList > _list.size()) {
               i = _list.size();
           }
           results.add(new ArrayList<>(_list.subList(i, i+partPerList)));
       }

       for (int i = 0; i < remainder; i++) {
           results.get(_listCount-1).add(_list.get(_list.size()-1-i));
       }

       return results;
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
    * Returns integer converted from string or default if string could not be converted to int.
    *
    * @param _possibleInt string to convert
    * @param _default default to use if string cannot be converted
    * @return int
    */
   public static int defaultIfNotInteger(String _possibleInt, int _default) {
       if (isInteger(_possibleInt)) {
           return Integer.parseInt(_possibleInt);
       }
       return _default;
   }
}
