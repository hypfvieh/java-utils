package com.github.hypfvieh.classloader;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.hypfvieh.AbstractBaseUtilTest;

/**
 *
 * @author hypfvieh
 */
public class ComponentRegistryTest extends AbstractBaseUtilTest {

    public ComponentRegistryTest() {
    }

    /**
     * Test getInstance().
     */
    @Test
    public void testGetInstance() {
        ComponentRegistry result = new ComponentRegistry();

        assertNotNull(result);
        assertEquals(ComponentRegistry.class.getName(), result.getClass().getName());
    }

    /**
     * Test of clearPackageIncludeList method.
     * First add something to list, then remove it, then try to register component with missing include entry.
     */
    @Test
    public void testClearPackageIncludeList() {
        String testClass = "com.github.hypfvieh.classloader.ComponentRegistryTest$LoaderTest";
        ComponentRegistry instance = new ComponentRegistry();
        instance.addPackageToIncludeList(testClass);
        instance.clearPackageIncludeList();
        instance.registerComponent(testClass);

        assertEquals(1, instance.getComponents().size());
    }

    /**
     * Test getComponentsVersions() method.
     */
    @Test
    public void testGetComponentsVersions() {
        ComponentRegistry instance = new ComponentRegistry();
        Map<String, String> result = instance.getComponentsVersions();

        // one class should be registered
        assertEquals(1, result.size());
    }

    /**
     * Test getComponents() method.
     */
    @Test
    public void testGetComponents() {
        ComponentRegistry instance = new ComponentRegistry();
        List<String> result = instance.getComponents();

        // one class should be registered (ComponentRegistry itself)
        assertEquals(1, result.size());
    }

    /**
     * Test registerComponent with package inclusion.
     */
    @Test
    public void testAddPackageToIncludeList() {
        String testClass = "com.github.hypfvieh.classloader.ComponentRegistryTest$LoaderTest";
        ComponentRegistry instance = new ComponentRegistry();
        instance.addPackageToIncludeList(testClass);
        instance.registerComponent(testClass);

        assertEquals(2, instance.getComponents().size());
    }

    /**
     * Test of unregisterComponent method, of class ComponentRegistry.
     */
    @Test
    public void testUnregisterComponent() {
        String testClass = "com.github.hypfvieh.classloader.ComponentRegistryTest$LoaderTest";
        ComponentRegistry instance = new ComponentRegistry();
        instance.addPackageToIncludeList(testClass);
        instance.registerComponent(testClass);

        assertEquals(2 , instance.getComponents().size());
        assertEquals(2 , instance.getComponentsVersions().size());

        instance.unregisterComponent(testClass);

        assertEquals(1 , instance.getComponents().size());
        assertEquals(1 , instance.getComponentsVersions().size());

    }


    public static class LoaderTest {

        private static final String version = "1.0.2-BETA-4";

        public static void run(List<String> args) {
            System.out.println("    --> Running!");
            int i = 0;
            for (String arg : args) {
                System.out.println("Argument " + i +  ": " + arg);
                i++;
            }
        }

        public static String getVersion() {
            return version;
        }
    }
}
