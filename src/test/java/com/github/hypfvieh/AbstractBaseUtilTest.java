package com.github.hypfvieh;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import com.github.hypfvieh.util.StringUtil;

/**
 *
 * @author hypfvieh
 */
public abstract class AbstractBaseUtilTest extends Assert {
    @Rule
    public TestName testName = new TestName();

    @Rule
    public TestRule watcher  = new TestWatcher() {
                                 @Override
                                 protected void starting(Description description) {
                                     System.out.println("  [TEST]: " + description.getMethodName());
                                 }

                             };

    /**
     * Retrieves class name and method name at the specified stacktrace index.
     * @param _index stacktrace index
     * @return fully qualified method name
     */
    private static String getStackTraceString(int _index) {
        StackTraceElement[] arrStackTraceElems = new Throwable().fillInStackTrace().getStackTrace();
        final int lIndex = Math.min(arrStackTraceElems.length - 1, Math.max(0, _index));
        return arrStackTraceElems[lIndex].getClassName() + "." + arrStackTraceElems[lIndex].getMethodName();
    }

    /**
    * Gets the current method name.
    * @return method name
    */
    public static String getMethodName() {
        return getStackTraceString(2);
    }

    /**
    * Gets the calling method name.
    * @return method name
    */
    public static String getCallingMethodName() {
        return getStackTraceString(3);
    }

    private static String getExceptionAsString(Throwable _ex) {
        if (_ex == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (_ex.getStackTrace() == null) {
            _ex.fillInStackTrace();
        }
        _ex.printStackTrace(new PrintStream(baos));
        return baos.toString();
    }

    public static void assertNotEquals(Object _obj1, Object _obj2) {
        Assert.assertFalse("Parameters are equal: " + _obj1 + " = " + _obj2, Objects.equals(_obj1, _obj2));
    }

    public static void assertEmpty(String _string) {
        Assert.assertTrue("String not empty.", _string != null ? _string.isEmpty() : false);
    }

    public static void assertNotEmpty(String _string) {
        Assert.assertTrue("String is empty.", _string != null ? _string.isEmpty() : true);
    }

    public static void assertBlank(String _string) {
        Assert.assertTrue("String not blank.", StringUtil.isBlank(_string));
    }

    public static void assertNotBlank(String _string) {
        Assert.assertTrue("String is blank.", !StringUtil.isBlank(_string));
    }

    public static void assertContains(String _string, String _contains) {
        if (_contains != null) {
            Assert.assertTrue("String does not contain [" + _contains + "]: " + _string, _string != null ? _string.contains(_contains) : false);
        }
    }

    public static void assertContainsNot(String _string, String _notContains) {
        if (_notContains != null) {
            Assert.assertFalse("String contains [" + _notContains + "]: " + _string, _string != null ? _string.contains(_notContains) : true);
        }
    }

    public static void assertDoubleEquals(double _expected, double _actual) {
        assertEquals(_expected, _actual, 0.000001d);
    }

    /**
     * Fails a test with the given message and optional exception.
     * @param _message message
     * @param _ex exception
     */
    public static void assertFail(String _message, Throwable _ex) {
        String message = StringUtil.defaultIfBlank(_message, "!no fail message provided by " + getCallingMethodName());
        if (_ex != null) {
            message += " " + getExceptionAsString(_ex);
        }
        Assert.fail(message);
    }

    public static void assertFail(String _message) {
        assertFail(_message, null);
    }

    /**
     * Asserts that the specified file exists.
     * @param _file file object
     * @return the file object
     */
    public static final File assertFileExists(File _file) {
        return assertFileExists(_file, true);
    }

    public static final File assertFileNotExists(File _file) {
        return assertFileExists(_file, false);
    }

    /**
     * Asserts that the specified file exists or does not exists.
     * @param _file file object
     * @param _exists true if should exist, false otherwise
     * @return the file object
     */
    private static File assertFileExists(File _file, boolean _exists) {
        assertNotNull("File object is null.", _file);
        if (_exists) {
            assertTrue("File [" + _file.getAbsolutePath() + "] does not exist.", _file.exists());
        } else {
            assertTrue("File [" + _file.getAbsolutePath() + "] exists.", !_file.exists());
        }
        return _file;
    }

    public static final File assertFileExists(String _file) {
        assertNotNull(_file);
        return assertFileExists(new File(_file));
    }

    /**
     * Asserts that the specified environment variable is set.
     * @param _name environment variable name
     * @return value of environment variable
     */
    public static final String assertEnvSet(String _name) {
        assertNotNull(_name);
        String value = System.getenv(_name);
        assertNotEmpty(value);
        return value;
    }

    /**
     * Asserts the specified map is non-null and contains all
     * of the given keys.
     * @param _map map
     * @param _keys array of keys, may be null or zero length
     * @return the map parameter
     */
    @SafeVarargs
    public static final <K, V> Map<K, V> assertMap(Map<K, V> _map, K... _keys) {
        assertNotNull("Map is null.", _map);
        if (_keys != null) {
            for (Object key : _keys) {
                assertTrue("Key [" + key + "] not found in map: " + _map, _map.containsKey(key));
            }
        }
        return _map;
    }

    /**
     * Asserts the specified collection is non-null and contains all
     * of the given values.
     * @param _coll collection
     * @param _values array of values, may be null or zero length
     * @return the collection parameter
     */
    @SafeVarargs
    public static final <V> Collection<V> assertCollection(Collection<V> _coll, V... _values) {
        assertNotNull("Collection is null.", _coll);
        Collection<V> notFound = new ArrayList<V>();
        if (_values != null) {
            for (V val : _values) {
                if (!_coll.contains(val)) {
                    notFound.add(val);
                }
            }
        }
        assertTrue("Values " + notFound + " not found in collection: " + _coll, notFound.size() == 0);
        return _coll;
    }

    public static void assertInstanceOf(Object _obj, Class<?> _class) {
        assertTrue(_obj + " is not an instance of " + _class + ".", _obj != null && _class != null && _class.isAssignableFrom(_obj.getClass()));
    }

    public static final void assertPatternFind(String _str, String _pattern) {
        assertNotNull("String may not be null.", _str);
        assertNotNull("Pattern may not be null.", _pattern);
        assertTrue("Pattern [" + _pattern + "] not found in string [" + _str + "].", Pattern.compile(_pattern).matcher(_str).find());
    }

    public static final void assertPatternMatches(String _str, String _pattern) {
        assertNotNull("String may not be null.", _str);
        assertNotNull("Pattern may not be null.", _pattern);
        assertTrue("Pattern [" + _pattern + "] does not match string [" + _str + "].", Pattern.compile(_pattern).matcher(_str).matches());
    }

}
