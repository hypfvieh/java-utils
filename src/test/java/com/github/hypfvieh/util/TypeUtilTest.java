package com.github.hypfvieh.util;

import java.util.Map;
import java.util.Properties;

import org.junit.Test;

import com.github.hypfvieh.AbstractBaseUtilTest;

/**
 *
 * @author hypfvieh
 */
public class TypeUtilTest extends AbstractBaseUtilTest {


    @Test
    public void testIsAnyNull() {
        assertFalse(TypeUtil.isAnyNull());
        assertTrue(TypeUtil.isAnyNull((Object[]) null));
        assertTrue(TypeUtil.isAnyNull("", null));
        assertTrue(TypeUtil.isAnyNull(null, null));
    }

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
}
