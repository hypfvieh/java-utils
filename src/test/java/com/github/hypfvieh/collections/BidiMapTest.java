package com.github.hypfvieh.collections;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.hypfvieh.AbstractBaseUtilTest;
import com.github.hypfvieh.util.TypeUtil;

public class BidiMapTest extends AbstractBaseUtilTest {

    public void testConstructor1() {
        new BidiMap<String, String>();
    }

    public void testConstructor2() {
        new BidiMap<>(new java.util.HashMap<String, String>());
    }

    @Test
    public void testGet1() {
        assertThrows(IllegalArgumentException.class, () -> {
            BidiMap<String, String> bidiMap = new BidiMap<>();
            bidiMap.get(null);
        });
    }

    @Test
    public void testGetKey() {
        BidiMap<String, String> bidiMap = new BidiMap<>(TypeUtil.createMap("K1", "V1"));
        assertEquals("K1", bidiMap.getKey("V1"));
    }

    @Test
    public void testContainsValue() {
        BidiMap<String, String> bidiMap = new BidiMap<>(TypeUtil.createMap("K1", "V1"));
        assertTrue(bidiMap.containsValue("V1"));
    }

    @Test
    public void testKeySet() {
        BidiMap<String, String> bidiMap = new BidiMap<>();
        assertTrue(bidiMap.keySet().getClass().getName().contains("UnmodifiableSet"));
    }

    @Test
    public void testValues() {
        BidiMap<String, String> bidiMap = new BidiMap<>();
        assertTrue(bidiMap.values().getClass().getName().contains("UnmodifiableCollection"));
    }

    @Test
    public void testEntrySet() {
        BidiMap<String, String> bidiMap = new BidiMap<>();
        assertTrue(bidiMap.entrySet().getClass().getName().contains("UnmodifiableSet"));
    }

    @Test
    public void testRemove1() {
        assertThrows(RuntimeException.class, () -> {
            BidiMap<String, String> bidiMap = new BidiMap<>();
            bidiMap.remove(null);
        });
    }

    @Test
    public void testRemove2() {
        Map<String, String> map = TypeUtil.createMap("K1", "V1");
        BidiMap<String, String> bidiMap = new BidiMap<>(map);

        String removed = bidiMap.remove("K1");
        assertEquals("V1", removed);

        assertFalse(bidiMap.containsKey("K1"));
        assertFalse(bidiMap.containsValue("V1"));

        // ensure the original map was not modified
        assertTrue(map.containsKey("K1"));
        assertTrue(map.containsValue("V1"));
    }

    @Test
    public void testRemoveValue1() {
        assertThrows(RuntimeException.class, () -> {
            BidiMap<String, String> bidiMap = new BidiMap<>();
            bidiMap.removeValue(null);
        });
    }

    @Test
    public void testRemoveValue2() {
        Map<String, String> map = TypeUtil.createMap("K1", "V1");
        BidiMap<String, String> bidiMap = new BidiMap<>(map);

        bidiMap.removeValue("V1");
    }

    @Test
    public void testPutNull() {
        assertThrows(RuntimeException.class, () -> {
            BidiMap<String, String> bidiMap = new BidiMap<>();
            bidiMap.put("K1", null);
        });
    }

    @Test
    public void testPutAgain() {
        BidiMap<String, String> bidiMap = new BidiMap<>();
        bidiMap.put("K1", "V1");
        bidiMap.put("K1", "V1");
    }

    @Test
    public void testPutKeysSameValue() {
        assertThrows(RuntimeException.class, () -> {
            BidiMap<String, String> bidiMap = new BidiMap<>();
            bidiMap.put("K1", "V1");
            bidiMap.put("K2", "V1");
        });
    }

    @Test
    public void testPutValuesSameKey() {
        assertThrows(RuntimeException.class, () -> {
            BidiMap<String, String> bidiMap = new BidiMap<>();
            bidiMap.put("K1", "V1");
            bidiMap.put("K1", "V2");
        });
    }

    @Test
    public void testPutAll() {
        Map<String, String> map = TypeUtil.createMap("K1", "V1", "K2", "V2", "K3", "V3");
        BidiMap<String, String> bidiMap = new BidiMap<>();
        bidiMap.putAll(map);

        assertEquals(map.size(), bidiMap.size());
        assertEquals(map, bidiMap);
    }

    @Test
    public void testClear() {
        Map<String, String> map = TypeUtil.createMap("K1", "V1");
        BidiMap<String, String> bidiMap = new BidiMap<>(map);

        assertEquals(1, map.size());
        assertEquals(1, bidiMap.size());

        bidiMap.clear();

        assertEquals(1, map.size());
        assertEquals(0, bidiMap.size());
    }

    @Test
    public void testToString() {
        Map<String, String> map = TypeUtil.createMap("K1", "V1");
        BidiMap<String, String> bidiMap = new BidiMap<>(map);
        assertContains(bidiMap.toString(), map.toString());
    }

    @Test
    public void testCreateMap() {
        Map<?, ?> hashMap = BidiMap.createMap(new java.util.HashMap<String, String>());
        assertSame(java.util.HashMap.class, hashMap.getClass());

        Map<?, ?> hashTable = BidiMap.createMap(new java.util.Hashtable<String, String>());
        assertSame(java.util.Hashtable.class, hashTable.getClass());

        Map<?, ?> treeMap = BidiMap.createMap(new java.util.TreeMap<String, String>());
        assertSame(java.util.TreeMap.class, treeMap.getClass());
    }

    @Test
    public void testCheckParms() {
        assertThrows(RuntimeException.class, () -> BidiMap.checkParms((Object) null));
    }

}
