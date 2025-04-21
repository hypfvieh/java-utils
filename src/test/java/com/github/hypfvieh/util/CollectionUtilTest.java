package com.github.hypfvieh.util;

import com.github.hypfvieh.AbstractBaseUtilTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

public class CollectionUtilTest extends AbstractBaseUtilTest {

    @Test
    void testHasValue() {
        assertTrue(CollectionUtil.hasValue(List.of(1)));
        assertTrue(CollectionUtil.hasValue(Set.of("yes")));

        assertFalse(CollectionUtil.hasValue(List.of()));
        assertFalse(CollectionUtil.hasValue(Set.of()));
        assertFalse(CollectionUtil.hasValue(null));
    }

    @Test
    void testCreateMutableList() {
        List<Integer> mutableIntList = CollectionUtil.mutableListOf(1, 2, 3);

        assertNotNull(mutableIntList);
        assertEquals(3, mutableIntList.size());
        assertEquals(List.of(1, 2, 3), mutableIntList);

        assertDoesNotThrow(() -> mutableIntList.add(4));

        assertEquals(4, mutableIntList.size());
        assertEquals(List.of(1, 2, 3, 4), mutableIntList);
    }

    @Test
    void testGetFirstOrDefault() {
        assertEquals(1, CollectionUtil.getFirstOrDefault(List.of(1, 2), null));
        assertEquals("X", CollectionUtil.getFirstOrDefault(List.of(), "X"));
        assertEquals(5, CollectionUtil.getFirstOrDefault(null, 5));
        assertNull(CollectionUtil.getFirstOrDefault(List.of(), null));
    }
}
