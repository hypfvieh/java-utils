package com.github.hypfvieh.util;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.github.hypfvieh.AbstractBaseUtilTest;

public class CompareUtilTest extends AbstractBaseUtilTest {

    @Test
    public void testIsAnyNull() {
        assertFalse(CompareUtil.isAnyNull());
        assertTrue(CompareUtil.isAnyNull((Object[]) null));
        assertTrue(CompareUtil.isAnyNull("", null));
        assertTrue(CompareUtil.isAnyNull(null, null));
    }

    @Test
    public void testMapContainsKeys() {
        Map<Object, Object> map = new HashMap<>();
        map.put("key1", "val1");
        map.put("key2", "val2");

        assertFalse(CompareUtil.mapContainsKeys(null, (Object) null));
        assertTrue(CompareUtil.mapContainsKeys(map, (Object) null));
        assertTrue(CompareUtil.mapContainsKeys(map, "key1"));
        assertTrue(CompareUtil.mapContainsKeys(map, "key1", "key2"));
        assertTrue(CompareUtil.mapContainsKeys(map, null, "key2", null));
        assertFalse(CompareUtil.mapContainsKeys(map, "not", "a", "key", "in", "this", "map"));
    }
}
