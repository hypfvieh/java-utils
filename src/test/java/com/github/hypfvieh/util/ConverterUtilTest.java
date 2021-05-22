package com.github.hypfvieh.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.jupiter.api.Test;

import com.github.hypfvieh.AbstractBaseUtilTest;

public class ConverterUtilTest extends AbstractBaseUtilTest {

    @Test
    public void testStrToBool() {
        System.out.println("strToBool");

        String bool0 = null;
        String bool1 = "yes";
        String bool2 = "true";
        String bool3 = "y";
        String bool4 = "1";
        String bool5 = "active";
        String bool6 = "enabled";
        String bool7 = "j";

        String bool8 = "no";
        String bool9 = "false";
        String bool10 = "epic fail";

        assertEquals(true, ConverterUtil.strToBool(bool1));
        assertEquals(true, ConverterUtil.strToBool(bool2));
        assertEquals(true, ConverterUtil.strToBool(bool3));
        assertEquals(true, ConverterUtil.strToBool(bool4));
        assertEquals(true, ConverterUtil.strToBool(bool5));
        assertEquals(true, ConverterUtil.strToBool(bool6));
        assertEquals(true, ConverterUtil.strToBool(bool7));

        assertEquals(false, ConverterUtil.strToBool(bool0));
        assertEquals(false, ConverterUtil.strToBool(bool8));
        assertEquals(false, ConverterUtil.strToBool(bool9));
        assertEquals(false, ConverterUtil.strToBool(bool10));

    }

    @Test
    public void testStrToInt() {
        assertEquals(Integer.valueOf(1), ConverterUtil.strToInt("1"));
        assertEquals(Integer.valueOf(-1), ConverterUtil.strToInt("-1"));

        assertEquals(Integer.valueOf(4711), ConverterUtil.strToInt("ab2000", 4711));
        assertEquals(Integer.valueOf(4711), ConverterUtil.strToInt("4711", 815));
    }

    @Test
    public void testToProperties() {
        Map<Object, Object> someMap = new HashMap<>();
        someMap.put("foo", "bar");
        someMap.put(123, "456");
        someMap.put("str", 555);
        someMap.put(999, 888);

        Properties properties = ConverterUtil.toProperties(someMap);

        assertEquals("bar", properties.get("foo"));
        assertEquals("456", properties.get(123));

        assertEquals(888, properties.get(999));
        assertEquals(555, properties.get("str"));

    }
}
