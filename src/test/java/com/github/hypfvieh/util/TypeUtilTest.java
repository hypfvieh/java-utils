package com.github.hypfvieh.util;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.jupiter.api.Test;

import com.github.hypfvieh.AbstractBaseUtilTest;

/**
 *
 * @author hypfvieh
 */
public class TypeUtilTest extends AbstractBaseUtilTest {

    @Test
    public void testCreateProperties() {
        Properties props;
        props = TypeUtil.createProperties((String[]) null);
        assertNotNull(props);
        assertEquals(0, props.size());

        props = TypeUtil.createProperties("k1", "v1", "k2", "v2");
        assertNotNull(props);
        assertEquals(2, props.size());

        // odd number of parameters should get exception
        Exception ex = null;
        try {
            props = TypeUtil.createProperties("k1");
        } catch (Exception _ex) {
            ex = _ex;
        }
        assertNotNull(ex);
    }

    @Test
    public void testIsInteger() {
        System.out.println("isInteger");

        String int1 = "0";
        String int2 = "-148832";
        String int3 = "4711";
        String int4 = "0.815";
        String int5 = "no int";
        String int6 = null;

        assertEquals(true, TypeUtil.isInteger(int1));
        assertEquals(false, TypeUtil.isInteger(int2, false));
        assertEquals(true, TypeUtil.isInteger(int2));
        assertEquals(true, TypeUtil.isInteger(int3));
        assertEquals(false, TypeUtil.isInteger(int4));
        assertEquals(false, TypeUtil.isInteger(int5));
        assertEquals(false, TypeUtil.isInteger(int6));

    }

    @Test
    public void testIsDouble() {
        System.out.println("isDouble");

        String int1 = "0";
        String int2 = "-0.45";
        String int3 = "4.711";
        String int4 = "0,815";
        String int5 = "no double";
        String int6 = null;

        assertEquals(true, TypeUtil.isDouble(int1, '.'));
        assertEquals(false, TypeUtil.isDouble(int2, '.', false));
        assertEquals(true, TypeUtil.isDouble(int2, '.'));
        assertEquals(true, TypeUtil.isDouble(int3, '.'));
        assertEquals(false, TypeUtil.isDouble(int4, '.'));
        assertEquals(false, TypeUtil.isDouble(int5, '.'));
        assertEquals(false, TypeUtil.isDouble(int6, '.'));

    }
    @Test
    public void testIsValidNetworkPort() {
        System.out.println("isValidNetworkPort");

        int int1 = 4711;
        int int2 = 100000;
        int int3 = -2003;
        int int4 = 113;
        String port1 = "19393";
        String port2 = "100032";
        String port3 = "-19432";
        String port4 = "80";

        assertEquals(true, TypeUtil.isValidNetworkPort(int1, true));
        assertEquals(false, TypeUtil.isValidNetworkPort(int2, true));
        assertEquals(false, TypeUtil.isValidNetworkPort(int3, true));
        assertEquals(true, TypeUtil.isValidNetworkPort(int4, true));
        assertEquals(false, TypeUtil.isValidNetworkPort(int4, false));

        assertEquals(true, TypeUtil.isValidNetworkPort(port1, true));
        assertEquals(false, TypeUtil.isValidNetworkPort(port2, true));
        assertEquals(false, TypeUtil.isValidNetworkPort(port3, true));
        assertEquals(true, TypeUtil.isValidNetworkPort(port4, true));
        assertEquals(false, TypeUtil.isValidNetworkPort(port4, false));
    }

    @Test
    public void testCreateMap() throws Exception {
        Map<String, String> map = TypeUtil.createMap("1", "a", "2", "b", "3", "c", "4", "d");
        assertMap(map, "1", "2", "3", "4");
        assertEquals(4, map.size());
        assertEquals("a", map.get("1"));
        assertTrue(map.containsKey("1"));
        assertTrue(map.containsValue("a"));
    }

    @Test
    public void testCreateList() throws Exception {
        List<String> list = TypeUtil.createList("u", "r", "gr8");
        assertCollection(list, "u", "r", "gr8");

        assertNotNull(TypeUtil.createList());
        assertNotNull(TypeUtil.createList((Object[]) null));
    }

    @Test
    public void testSplitListToSubLists() throws Exception {
        List<Integer> list = TypeUtil.createList(1,2,3,4,5,6,7,8,9,10);

        List<List<Integer>> listOf2 = TypeUtil.splitListToSubLists(list, 2);
        assertNotNull(listOf2);
        assertEquals(2, listOf2.size());

        assertCollection(listOf2.get(0), 1,2,3,4,5);
        assertCollection(listOf2.get(1), 6,7,8,9,10);

        List<List<Integer>> listOf5 = TypeUtil.splitListToSubLists(list, 5);
        assertNotNull(listOf5);
        assertEquals(5, listOf5.size());

        assertCollection(listOf5.get(0), 1,2);
        assertCollection(listOf5.get(1), 3,4);
        assertCollection(listOf5.get(2), 5,6);
        assertCollection(listOf5.get(3), 7,8);
        assertCollection(listOf5.get(4), 9,10);

        List<List<Integer>> listOf3 = TypeUtil.splitListToSubLists(list, 3);
        assertNotNull(listOf3);
        assertEquals(3, listOf3.size());

        assertCollection(listOf3.get(0), 1,2,3);
        assertCollection(listOf3.get(1), 4,5,6);
        assertCollection(listOf3.get(2), 7,8,9,10);

        list = TypeUtil.createList(1,2,3,4,5,6,7,8,9);

        List<List<Integer>> list2Of9 = TypeUtil.splitListToSubLists(list, 2);
        assertNotNull(list2Of9);
        assertEquals(2, list2Of9.size());

        assertCollection(list2Of9.get(0), 1,2,3,4);
        assertCollection(list2Of9.get(1), 5,6,7,8,9);

    }


}
