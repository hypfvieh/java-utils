package com.github.hypfvieh.classloader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Custom classloader to register certain classes to ComponentRegistry.
 *
 * @author hypfvieh
 */
public class ClassLoaderWithRegistry extends java.lang.ClassLoader {

    private static final String version = "0.0.3";

    /**
     * List of package names which are loaded with this classloader (has to end with a dot).
     */
    private final Set<String> includePackageNames
                              = new HashSet<>(Arrays.asList(
                            "com.github.hypfvieh.",
                            "classloadertest."
                    ));
    /**
     * List of classes which this classloader should not load (could be classes in packages which are included).
     */
    private final Set<String> excludeClassNames
                              = new HashSet<>(Arrays.asList(
                            "com.github.hypfvieh.classloader.ComponentRegistry"
                    ));

    public ClassLoaderWithRegistry(ClassLoader _parent) {
        super(_parent);
        // Register classloader to ComponentRegistry
        ComponentRegistry.getInstance().registerComponent(ClassLoaderWithRegistry.class, getVersion());
    }

    public ClassLoaderWithRegistry() {
        super();
        // Register classloader to ComponentRegistry
        ComponentRegistry.getInstance().registerComponent(ClassLoaderWithRegistry.class, getVersion());
    }

    /**
     * Add a package name which should be loaded with this classloader.
     *
     * @param _packageName name of the package, has to end with '.'
     */
    public void addIncludedPackageNames(String _packageName) {
        if (_packageName.endsWith(".")) {
            includePackageNames.add(_packageName);
            ComponentRegistry.getInstance().addPackageToIncludeList(_packageName);
        }
    }

    /**
     * Add a class which should not be loaded with this classloader even if it is in 'includedPackages' List.
     *
     * @param _excludedClassName
     */
    public void addExcludedClassName(String _excludedClassName) {
        if (!_excludedClassName.endsWith(".")) {
            excludeClassNames.add(_excludedClassName);
        }
    }

    /**
     * Check if a certain class is included.
     * @param _fqcn
     * @return
     */
    private boolean isIncluded(String _fqcn) {
        if (includePackageNames.contains(_fqcn)) {
            return true;
        }

        String packageName = _fqcn.substring(0, _fqcn.lastIndexOf('.') + 1);
        for (String str : includePackageNames) {
            if (packageName.startsWith(str)) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve)
            throws ClassNotFoundException {

        // only take classes in includePackageNames except the class is listet in excludeClassNames
        if (!excludeClassNames.contains(name) && isIncluded(name)) {

            // see if we have already loaded the class.
            Class<?> c = findLoadedClass(name);
            if (c != null) {
                return c;
            }

            // the class is not loaded yet.  Since the parent class loader has all of the
            // definitions that we need, we can use it as our source for classes.
            InputStream in = null;
            try {
                // get the input stream, throwing ClassNotFound if there is no resource.
                in = getParent().getResourceAsStream(name.replaceAll("\\.", "/") + ".class");
                if (in == null) {
                    throw new ClassNotFoundException("Could not find " + name);
                }

                // read all of the bytes and define the class.
                byte[] cBytes = toByteArray(in);
                c = defineClass(name, cBytes, 0, cBytes.length);
                if (resolve) {
                    resolveClass(c);
                }
                ComponentRegistry.getInstance().registerComponent(c.getName());

                return c;
            } catch (IOException e) {
                throw new ClassNotFoundException("Could not load " + name, e);
            } finally {

                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ioe) {
                    // ignore
                }
            }
        } else {
            return super.loadClass(name, resolve);
        }

    }

    public static String getVersion() {
        return version;
    }

    private static byte[] toByteArray(InputStream _is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[1024 * 4];

        while ((nRead = _is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();

        return buffer.toByteArray();
    }
}
