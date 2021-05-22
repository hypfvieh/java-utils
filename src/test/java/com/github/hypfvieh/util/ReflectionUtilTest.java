package com.github.hypfvieh.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.github.hypfvieh.AbstractBaseUtilTest;

public class ReflectionUtilTest extends AbstractBaseUtilTest {

    @Test
    public void testGetAllDeclaredFields() {
        Set<Field> allDeclaredFields = ReflectionUtil.getAllDeclaredFields(TestClass3.class);
        List<String> cll = allDeclaredFields.stream().map(f -> f.getName()).collect(Collectors.toList());
        assertCollection(cll, "field1", "field2", "field3", "staticField1");

        allDeclaredFields = ReflectionUtil.getAllDeclaredFields(TestClass2.class);
        cll = allDeclaredFields.stream().map(f -> f.getName()).collect(Collectors.toList());
        assertCollection(cll, "field1", "field2", "field3");

        allDeclaredFields = ReflectionUtil.getAllDeclaredFields(TestClass1.class);
        cll = allDeclaredFields.stream().map(f -> f.getName()).collect(Collectors.toList());
        assertCollection(cll, "field1", "field2");

        allDeclaredFields = ReflectionUtil.getAllDeclaredFields(TestClass2.class, "field2");
        cll = allDeclaredFields.stream().map(f -> f.getName()).collect(Collectors.toList());
        assertCollection(cll, "field1", "field3");
        assertFalse(cll.contains("field2"));
    }

    @Test
    public void testGetAllDeclaredStaticFields() {
        Set<Field> allDeclaredFields = ReflectionUtil.getAllDeclaredStaticFields(TestClass3.class);
        List<String> cll = allDeclaredFields.stream().map(f -> f.getName()).collect(Collectors.toList());
        assertCollection(cll, "staticField1");

        allDeclaredFields = ReflectionUtil.getAllDeclaredStaticFields(TestClass1.class, "$jacocoData");
        assertTrue(allDeclaredFields.isEmpty(), "Expected no declared static fields in " + TestClass1.class + " but got: " + allDeclaredFields);

        allDeclaredFields = ReflectionUtil.getAllDeclaredStaticFields(TestClass2.class, "$jacocoData");
        assertTrue(allDeclaredFields.isEmpty(), "Expected no declared static fields in " + TestClass2.class + " but got: " + allDeclaredFields);
    }

    @Test
    public void testGetAllDeclaredNonStaticFields() {
        Set<Field> allDeclaredFields = ReflectionUtil.getAllDeclaredNonStaticFields(TestClass3.class);
        List<String> cll = allDeclaredFields.stream().map(f -> f.getName()).collect(Collectors.toList());
        assertFalse(cll.contains("staticField1"));

        allDeclaredFields = ReflectionUtil.getAllDeclaredNonStaticFields(TestClass1.class);
        assertFalse(allDeclaredFields.isEmpty());

        allDeclaredFields = ReflectionUtil.getAllDeclaredNonStaticFields(TestClass2.class);
        assertFalse(allDeclaredFields.isEmpty());
    }

    @Test
    public void testGetAllDeclaredAnnotatedFields() {
        Set<Field> allDeclaredFields = ReflectionUtil.getAllDeclaredFieldsAnnotatedWithAny(TestClass3.class, DummyAnnotation.class);
        List<String> cll = allDeclaredFields.stream().map(f -> f.getName()).collect(Collectors.toList());
        assertCollection(cll, "field1", "field3");

        allDeclaredFields = ReflectionUtil.getAllDeclaredNonStaticFields(TestClass3.class);
        assertFalse(allDeclaredFields.isEmpty());
    }

    @Test
    public void testGetAllDeclaredNotAnnotatedFields() {
        Set<Field> allDeclaredFields = ReflectionUtil.getAllDeclaredFieldsNotAnnotatedWithAny(TestClass3.class, DummyAnnotation.class);
        List<String> cll = allDeclaredFields.stream().map(f -> f.getName()).collect(Collectors.toList());
        assertCollection(cll, "field2", "staticField1");
    }

    @Test
    public void testGetAllDeclaredMethods() {
        Set<Method> allDeclaredFields = ReflectionUtil.getAllDeclared(TestClass3.class, Method.class);
        List<String> cll = allDeclaredFields.stream().map(f -> f.getName()).collect(Collectors.toList());
        assertCollection(cll, "testMethod1", "testMethod2", "testMethod3");

        allDeclaredFields = ReflectionUtil.getAllDeclared(TestClass2.class, Method.class);
        cll = allDeclaredFields.stream().map(f -> f.getName()).collect(Collectors.toList());
        assertCollection(cll, "testMethod1", "testMethod2");

        allDeclaredFields = ReflectionUtil.getAllDeclared(TestClass1.class, Method.class);
        cll = allDeclaredFields.stream().map(f -> f.getName()).collect(Collectors.toList());
        assertCollection(cll, "testMethod1");
    }

    @Test
    public void testGetAllDeclaredStaticMethods() {
        Set<Method> allDeclaredFields = ReflectionUtil.getAllDeclaredStaticMethods(TestClass3.class);
        List<String> cll = allDeclaredFields.stream().map(f -> f.getName()).collect(Collectors.toList());
        assertCollection(cll, "testMethod3");
        assertFalse(cll.contains("testMethod1"));

        allDeclaredFields = ReflectionUtil.getAllDeclaredStaticMethods(TestClass1.class, "$jacocoData", "registerNatives");
        assertTrue(allDeclaredFields.isEmpty(), "Expected no declared static methods in " + TestClass1.class + " but got: " + allDeclaredFields);

        allDeclaredFields = ReflectionUtil.getAllDeclaredStaticMethods(TestClass2.class, "$jacocoData", "registerNatives");
        assertTrue(allDeclaredFields.isEmpty(), "Expected no declared static methods in " + TestClass2.class + " but got: " + allDeclaredFields);
    }

    @Test
    public void testGetAllDeclaredNonStaticMethods() {

        Set<Method> allDeclaredFields = ReflectionUtil.getAllDeclaredNonStaticMethods(TestClass3.class);

        List<String> cll = allDeclaredFields.stream().map(f -> f.getName()).collect(Collectors.toList());
        assertFalse(cll.contains("testMethod3"));
        assertTrue(cll.contains("testMethod1"));

        allDeclaredFields = ReflectionUtil.getAllDeclaredNonStaticMethods(TestClass1.class);
        assertFalse(allDeclaredFields.isEmpty());

        allDeclaredFields = ReflectionUtil.getAllDeclaredNonStaticMethods(TestClass2.class);
        assertFalse(allDeclaredFields.isEmpty());

    }

    @Test
    public void testGetAllDeclaredAnnotatedMethods() {
        Set<Method> allDeclaredFields = ReflectionUtil.getAllDeclaredMethodsAnnotatedWithAny(TestClass3.class, DummyAnnotation.class);
        List<String> cll = allDeclaredFields.stream().map(f -> f.getName()).collect(Collectors.toList());
        assertCollection(cll, "testMethod2");
    }

    @Test
    public void testGetAllDeclaredNotAnnotatedMethods() {
        Set<Method> allDeclaredFields = ReflectionUtil.getAllDeclaredMethodsNotAnnotatedWithAny(TestClass3.class, DummyAnnotation.class);
        List<String> cll = allDeclaredFields.stream().map(f -> f.getName()).collect(Collectors.toList());
        assertCollection(cll, "testMethod1", "testMethod3");
    }

    @SuppressWarnings("unused")
    static class TestClass1 {
        @DummyAnnotation
        private int field1;
        private int field2;

        private void testMethod1() {

        }

    }

    static class TestClass2 extends TestClass1 {
        @DummyAnnotation
        private int field3;

        @DummyAnnotation
        private void testMethod2() {

        }

    }

    @SuppressWarnings("unused")
    static class TestClass3 extends TestClass2 {
        private static int staticField1;

        private static void testMethod3() {

        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @java.lang.annotation.Target(value = {
            ElementType.FIELD,
            ElementType.METHOD
    })
    public @interface DummyAnnotation {
    }

}
