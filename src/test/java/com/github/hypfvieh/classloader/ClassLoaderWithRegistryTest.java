package com.github.hypfvieh.classloader;


import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.github.hypfvieh.AbstractBaseUtilTest;

/**
 *
 * @author hypfvieh
 */
public class ClassLoaderWithRegistryTest extends AbstractBaseUtilTest {

    public ClassLoaderWithRegistryTest() {
    }


    @Test
    public void testUseClassLoaderWithRegistry() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, InstantiationException {
        String testClass = "com.github.hypfvieh.classloader.ClassLoaderWithRegistryTest$LoaderTest";
        ClassLoaderWithRegistry classLoaderWithRegistry = new ClassLoaderWithRegistry(Class.forName(testClass).getClassLoader());


        classLoaderWithRegistry.addIncludedPackageNames("com.github.hypfvieh.classloader.");


        Thread.currentThread().setContextClassLoader(classLoaderWithRegistry);

        ClassLoader cls = Thread.currentThread().getContextClassLoader();

        Class<?> c = cls.loadClass(testClass);

        Object o = c.newInstance();

        Class<?>[] paramString = new Class[1];
        paramString[0] = List.class;

        c.getMethod("run", paramString).invoke(o, new ArrayList<>(Arrays.asList(new String[] { "foo", "bar", "baz"})));

        for (Map.Entry<String, String> entry : ComponentRegistry.getInstance().getComponentsVersions().entrySet()) {
            System.out.println("    --> Loaded Class '" + entry.getKey() + "' with version: " + entry.getValue());
        }

        // Three components should be registered, ComponentRegistry itself, our ClassLoader and our LoaderTest innerclass.
        assertEquals(3, ComponentRegistry.getInstance().getComponentsVersions().size());

    }

    public static class LoaderTest {

        private static final String version = "1.0.2-BETA-4";

        public static void run(List<String> args) {
            System.out.println("    --> TestLoader Running!");
            int i = 0;
            for (String arg : args) {
                System.out.println("      --> Argument " + i +  ": " + arg);
                i++;
            }
        }

        public static String getVersion() {
            return version;
        }
    }


}
