package com.github.hypfvieh.util;

import org.junit.Test;

import com.github.hypfvieh.AbstractBaseUtilTest;

public class ConverterUtilTest extends AbstractBaseUtilTest {


    /**
     * Test of readPropertiesBoolean method, of class Util.
     */
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
}
