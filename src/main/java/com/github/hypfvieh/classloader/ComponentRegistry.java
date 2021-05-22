package com.github.hypfvieh.classloader;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Singleton which holds a list of registered classes.
 *
 * @author hypfvieh
 */
public final class ComponentRegistry {

    private static ComponentRegistry instance = null;

    // we need a specified logger here, otherwise we cannot register SimpleLogger to ComponentRegistry!
    private final Logger logger = System.getLogger(ComponentRegistry.class.getName());

    private final Map<String, String> componentVersions = new TreeMap<>();

    private static final String version = "0.2.3";

    private final List<String> includes = new ArrayList<>();

    // Constructor is package protected for unit tests!
    ComponentRegistry() {
        this.registerComponent(this.getClass());
    }

    public static synchronized ComponentRegistry getInstance() {
        if (instance == null) {
            instance = new ComponentRegistry();
        }

        return instance;
    }

    public static String getVersion() {
        return version;
    }

    /**
     * Packages to allow to register (package name or FQCN).
     *
     * @param _str package name
     */
    public synchronized void addPackageToIncludeList(String _str) {
        if (_str == null) {
            return;
        }

        includes.add(_str);
    }

    public synchronized void clearPackageIncludeList() {
        includes.clear();
    }

    /**
     * Returns a list with all registered classes (FQCN).
     *
     * @return list (maybe empty), never null
     */
    public synchronized List<String> getComponents() {
        List<String> compos = new ArrayList<>();
        for (Map.Entry<String, String> entry : componentVersions.entrySet()) {
            compos.add(entry.getKey());
        }
        return compos;
    }

    /**
     * Returns the list of registered Components and Versions as Map.
     *
     * @return Map, never null
     */
    public synchronized Map<String, String> getComponentsVersions() {
        SortedMap<String, String> keys = new TreeMap<>(componentVersions);
        return keys;
    }

    /**
     * Return version for given Class. Returns null if class not in list.
     *
     * @param _clazz class
     * @return string or null
     */
    public synchronized String getVersionForComponent(Class<?> _clazz) {
        return componentVersions.get(_clazz.getName());
    }

    /**
     * Register a class with version.
     *
     * @param _clazz class
     * @param _version version
     */
    public synchronized void registerComponent(Class<?> _clazz, String _version) {
        componentVersions.put(_clazz.getName(), _version);
    }

    /**
     * Remove a registered class from list.
     *
     * @param _clazz class
     * @return true if component could be removed, false otherwise
     */
    public synchronized boolean unregisterComponent(Class<?> _clazz) {
        return null != componentVersions.remove(_clazz.getName());
    }

    /**
     * Remove a registered class from list.
     *
     * @param _className fqcn string
     * @return true if component could be removed, false otherwise
     */
    public synchronized boolean unregisterComponent(String _className) {
        return null != componentVersions.remove(_className);
    }

    /**
     * Check if the given FQCN matches to any given include pattern.
     * @param _clazzName class
     * @return true if included, false otherwise
     */
    private synchronized boolean isIncluded(String _clazzName) {
        if (includes.size() > 0) {
            for (String include : includes) {
                if (_clazzName.startsWith(include)) {
                    return true;

                }
            }
        } // if we dont have a list, we include nothing (to prevent circular classloader dependencies)
        return false;
    }

    /**
     * Register a class using the Class-Object.
     * @param _clazz class
     */
    public synchronized void registerComponent(Class<?> _clazz) {
        if (_clazz == null) {
            return;
        }

        if (isIncluded(_clazz.getName()) || _clazz.getName().equals(this.getClass().getName())) {
            String classVersion = getVersionWithReflection(_clazz);
            if (classVersion != null) {
                componentVersions.put(_clazz.getName(), classVersion);
            }
        }
    }


    /**
     * Register a component with FQCN only. This method will try to get the class version using reflections!
     *
     * @param _string fqcn
     */
    public synchronized void registerComponent(String _string) {

        if (isIncluded(_string)) {
            Class<?> dummy;
            try {
                dummy = Class.forName(_string);
                String classVersion = getVersionWithReflection(dummy);
                if (classVersion != null) {
                    componentVersions.put(_string, classVersion);
                }
            } catch (ClassNotFoundException ex) {
                logger.log(Level.TRACE, "Unable to call getVersion on " + _string);
            }
        }
    }

    /**
     * Helper which tries to find (and call) the method 'getVersion()' using reflection.
     *
     * @param dummy whatever
     * @return value of getVersion() or null
     */
    private String getVersionWithReflection(Class<?> dummy) {
        Method meth = null;
        try {
            Method[] declaredMethods = dummy.getDeclaredMethods();
            if (declaredMethods.length == 0) {
                return null;
            }

            for (Method method : declaredMethods) {
                if (method.getName().equals("getVersion")) {
                    meth = method;
                    break;
                }
            }

            if (meth != null) {
                try {
                    meth.setAccessible(true);
                    if (Modifier.isStatic(meth.getModifiers())) {

                        Object value = meth.invoke(null);
                        if (value instanceof String) {
                            return (String) value;
                        }
                    }
                    return null;
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    logger.log(Level.TRACE, "Unable to call getVersion on " + dummy.getName());
                }
            }
        } catch (SecurityException _ex) {
            return null;
        }

        return null;
    }

}
