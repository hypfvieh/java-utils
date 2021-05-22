package com.github.hypfvieh.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

/**
 * Utility methods which uses reflection techniques.
 *
 * @author David Michaelis (David.Michaelis@baaderbank.de)
 * @since v1.0.1 - 2018-02-12
 */
public final class ReflectionUtil {

    private ReflectionUtil() {

    }

    /**
     * Returns a list containing {@code Method} or {@link Field} objects reflecting all the
     * declared methods/fields of the class or interface represented by this {@code
     * Class} object, including public, protected, default (package)
     * access, and private methods/fields, also including inherited methods/fields.
     *
     * @since v1.0.2 - 2018-04-20
     *
     * @param _clazz the class object to examine
     * @return the Set of {@code Method} or {@code Field} objects representing all the
     *         declared methods of this class and superclasses - Set maybe empty but never null
     */
    @SuppressWarnings("unchecked")
    static <T extends Member> Set<T> getAllDeclared(Class<?> _clazz, Class<T> _type) {

        if (_type != Field.class && _type != Method.class) { // only fields and methods are supported
            return new LinkedHashSet<>();
        }

        Set<T> l = new LinkedHashSet<>();
        Method method;
        try {
            if (_type == Method.class) {
                method = Class.class.getDeclaredMethod("getDeclaredMethods");
            } else {
                method = Class.class.getDeclaredMethod("getDeclaredFields");
            }

            if (_clazz != null) {
                List<T> entriesInThisClass = Arrays.asList((T[]) method.invoke(_clazz));
                l.addAll(entriesInThisClass);

                if (_clazz.getSuperclass() != null) {
                    l.addAll(getAllDeclared(_clazz.getSuperclass(), _type));
                }

            }
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException _ex) {
            return new LinkedHashSet<>();
        }
        return l;
    }

    /**
     * Extract all {@link Field}s found in the given class recursively.
     * This means, all {@link Field}s are retrieved, even fields which only exists in superclasses.<br>
     * <br>
     * <b>NOTE:</b> Accessibility of {@link Field}s returned in the {@link Set} have not been changed
     * (setAccessable(true) is NOT called explicitly)!
     *
     * @param _class class to analyze
     * @param _fieldsToIgnore fields to skip
     *
     * @return null if _class was null, Set otherwise
     */
    public static Set<Field> getAllDeclaredFields(Class<?> _class, String... _fieldsToIgnore) {
        if (_class == null) {
            return null;
        }

        List<String> fieldsToIgnore = Arrays.asList(_fieldsToIgnore);

        Set<Field> allDeclared = getAllDeclared(_class, Field.class);
        return allDeclared.stream()
            .filter(f -> !fieldsToIgnore.contains(f.getName()))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Extract all {@link Field}s which are static and do not match any of the given ignore names.
     *
     * @param _class class to analyze
     * @param _fieldsToIgnore fields to skip
     *
     * @return null if _class was null, Set otherwise
     */
    public static Set<Field> getAllDeclaredStaticFields(Class<?> _class, String... _fieldsToIgnore) {
        return getAllDeclaredStatic(ReflectionType.FIELD, _class, _fieldsToIgnore);
    }

    /**
     * Extract all {@link Field}s which are <b>not</b> static and do not match any of the given ignore names.
     *
     * @param _class class to analyze
     * @param _fieldsToIgnore fields to skip
     *
     * @return null if _class was null, Set otherwise
     */
    public static Set<Field> getAllDeclaredNonStaticFields(Class<?> _class, String... _fieldsToIgnore) {
        return getAllDeclaredNonStatic(ReflectionType.FIELD, _class, _fieldsToIgnore);
    }

    /**
     * Extract all {@link Field}s with any of the given annotations found in the given class recursively.
     * This means, all {@link Field}s are retrieved, even fields which only exists in superclasses.<br>
     * <br>
     * <b>NOTE:</b> Accessibility of {@link Field}s returned in the {@link Set} have not been changed
     * (setAccessable(true) is NOT called explicitly)!
     *
     * @param _class class to analyze
     * @param _annotations annotations to check for
     *
     * @return null if _class was null, Set otherwise
     */
    @SafeVarargs
    public static Set<Field> getAllDeclaredFieldsAnnotatedWithAny(Class<?> _class, Class<? extends Annotation>... _annotations) {
        return getAllDeclaredWithAnnotationAction(ReflectionType.FIELD, _class, (f, a) -> f.isAnnotationPresent(a), _annotations);
    }

    /**
     * Extract all {@link Field}s without any of the given annotations found in the given class recursively.
     * This means, all {@link Field}s are retrieved, even fields which only exists in superclasses.<br>
     * <br>
     * <b>NOTE:</b> Accessibility of {@link Field}s returned in the {@link Set} have not been changed
     * (setAccessable(true) is NOT called explicitly)!
     *
     * @param _class class to analyze
     * @param _annotations annotations to check for
     *
     * @return null if _class was null, Set otherwise
     */
    @SafeVarargs
    public static Set<Field> getAllDeclaredFieldsNotAnnotatedWithAny(Class<?> _class, Class<? extends Annotation>... _annotations) {
        return getAllDeclaredWithAnnotationAction(ReflectionType.FIELD, _class, (f, a) -> !f.isAnnotationPresent(a), _annotations);
    }

    /**
     * Extract all {@link Method}s found in the given class recursively.
     * This means, all {@link Method}s are retrieved, even methods which only exists in superclasses.<br>
     * <br>
     * <b>NOTE:</b> Accessibility of {@link Method}s returned in the {@link Set} have not been changed
     * (setAccessable(true) is NOT called explicitly)!
     *
     * @since v1.0.2 - 2018-04-20
     *
     * @param _class class to analyze
     * @param _methodsToIgnore methods to skip
     *
     * @return null if _class was null, Set otherwise
     */
    public static Set<Method> getAllDeclaredMethods(Class<?> _class, String... _methodsToIgnore) {
        if (_class == null) {
            return null;
        }

        List<String> methodsToIgnore = Arrays.asList(_methodsToIgnore);

        Set<Method> allDeclared = getAllDeclared(_class, Method.class);
        return allDeclared.stream()
            .filter(f -> !methodsToIgnore.contains(f.getName()))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Extract all {@link Method}s which are static and do not match any of the given ignore names.
     *
     * @since v1.0.2 - 2018-04-20
     *
     * @param _class class to analyze
     * @param _methodNamesToIgnore methods to skip
     *
     * @return null if _class was null, Set otherwise
     */
    public static Set<Method> getAllDeclaredStaticMethods(Class<?> _class, String... _methodNamesToIgnore) {
        return getAllDeclaredStatic(ReflectionType.METHOD, _class, _methodNamesToIgnore);
    }

    /**
     * Extract all {@link Method}s which are <b>not</b> static and do not match any of the given ignore names.
     *
     * @since v1.0.2 - 2018-04-20
     *
     * @param _class class to analyze
     * @param _methodNamesToIgnore methods to skip
     *
     * @return null if _class was null, Set otherwise
     */
    public static Set<Method> getAllDeclaredNonStaticMethods(Class<?> _class, String... _methodNamesToIgnore) {
        return getAllDeclaredNonStatic(ReflectionType.METHOD, _class, _methodNamesToIgnore);
    }


    /**
     * Extract all {@link Method}s with any of the given annotations found in the given class recursively.
     * This means, all {@link Method}s are retrieved, even fields which only exists in superclasses.<br>
     * <br>
     * <b>NOTE:</b> Accessibility of {@link Method}s returned in the {@link Set} have not been changed
     * (setAccessable(true) is NOT called explicitly)!
     *
     * @since v1.0.2 - 2018-04-20
     *
     * @param _class class to analyze
     * @param _annotations annotations to check for
     *
     * @return null if _class was null, Set otherwise
     */
    @SafeVarargs
    public static Set<Method> getAllDeclaredMethodsAnnotatedWithAny(Class<?> _class, Class<? extends Annotation>... _annotations) {
        return getAllDeclaredWithAnnotationAction(ReflectionType.METHOD, _class, (f, a) -> f.isAnnotationPresent(a), _annotations);
    }

    /**
     * Extract all {@link Method}s without any of the given annotations found in the given class recursively.
     * This means, all {@link Method}s are retrieved, even fields which only exists in superclasses.<br>
     * <br>
     * <b>NOTE:</b> Accessibility of {@link Method}s returned in the {@link Set} have not been changed
     * (setAccessable(true) is NOT called explicitly)!
     *
     * @since v1.0.2 - 2018-04-20
     *
     * @param _class class to analyze
     * @param _annotations annotations to check for
     *
     * @return null if _class was null, Set otherwise
     */
    @SafeVarargs
    public static Set<Method> getAllDeclaredMethodsNotAnnotatedWithAny(Class<?> _class, Class<? extends Annotation>... _annotations) {
        return getAllDeclaredWithAnnotationAction(ReflectionType.METHOD, _class, (f, a) -> !f.isAnnotationPresent(a), _annotations);
    }

    /**
     * Extract all {@link Field}s or {@link Method}s which are static and do not match any of the given ignore names.
     *
     * @since v1.0.2 - 2018-04-20
     *
     * @param _type ReflectionType
     * @param _class class to analyze
     * @param _ignoreNames member names to ignore
     * @return
     */
    private static <T extends Member> Set<T> getAllDeclaredStatic(ReflectionType _type, Class<?> _class, String... _ignoreNames) {
        @SuppressWarnings("unchecked")
        Set<T> allDeclaredFields = (Set<T>) getAllDeclared(_class, _type.getReflectionClass());
        List<String> ignoreNames = Arrays.asList(_ignoreNames);


        return allDeclaredFields.stream()
                .filter(f -> !ignoreNames.contains(f.getName()))
                .filter(f -> Modifier.isStatic(f.getModifiers()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Extract all {@link Field}s or {@link Method}s which are <b>not</b> static and do not match any of the given ignore names.
     *
     * @since v1.0.2 - 2018-04-20
     *
     * @param _type ReflectionType
     * @param _class class to analyze
     * @param _ignoreNames member names to ignore
     * @return
     */
    private static <T extends Member> Set<T> getAllDeclaredNonStatic(ReflectionType _type, Class<?> _class, String... _ignoreNames) {
        @SuppressWarnings("unchecked")
        Set<T> allDeclaredFields = (Set<T>) getAllDeclared(_class, _type.getReflectionClass());
        List<String> ignoreNames = Arrays.asList(_ignoreNames);


        return allDeclaredFields.stream()
                .filter(f -> !ignoreNames.contains(f.getName()))
                .filter(f -> !Modifier.isStatic(f.getModifiers()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Extract all {@link Field}s or {@link Method}s and validates each field/method against the given {@link BiConsumer} lambda expression.
     * Result will depend on lambda. All positive lambda results (test returns true), will be added to the result Set.
     *
     * @since v1.0.2 - 2018-04-20
     * @param _class class to analyze
     * @param _annotationCheckLambda lambda to use to check every annotation
     * @param _annotations annotations to check for
     * @return null if class was null, Set otherwise
     */
    @SafeVarargs
    private static <T> Set<T> getAllDeclaredWithAnnotationAction(ReflectionType _type, Class<?> _class, BiPredicate<T, Class<? extends Annotation>> _annotationCheckLambda, Class<? extends Annotation>... _annotations) {
        if (_class == null) {
            return null;
        }

        if (_annotations == null || _annotations.length == 0) {
            return new LinkedHashSet<>();
        }
        @SuppressWarnings("unchecked")
        Set<T> allDeclaredFields =  (Set<T>) getAllDeclared(_class, _type.getReflectionClass());

        Set<T> result = new LinkedHashSet<>();
        for (T field : allDeclaredFields) {
            for (Class<? extends Annotation> annot : _annotations) {
                if (_annotationCheckLambda.test(field, annot)) {
                    result.add(field);
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Enum which defines which information is retrieved by reflection.
     *
     * @author hypfvieh
     * @since v1.0.2 - 2018-04-20
     */
    public static enum ReflectionType {
        FIELD(Field.class), METHOD(Method.class);

        private final Class<? extends Member> reflectionClass;

        private ReflectionType(Class<? extends Member> _class) {
            reflectionClass = _class;
        }

        public Class<? extends Member> getReflectionClass() {
            return reflectionClass;
        }
    }
}
