package com.github.hypfvieh.util;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.junit.Test;

import com.github.hypfvieh.AbstractBaseUtilTest;

/**
 *
 * @author hypfvieh
 */
public class FileIoUtilTest extends AbstractBaseUtilTest {

    public FileIoUtilTest() {
    }

    /**
     * Test of readPropertiesBoolean method, of class Util.
     */
    @Test
    public void testReadPropertiesBoolean() {
        System.out.println("readPropertiesBoolean");
        Properties props = new Properties();
        // should be true
        props.setProperty("bool1", "true");
        props.setProperty("bool2", "1");
        props.setProperty("bool3", "yes");
        props.setProperty("bool4", "enabled");
        props.setProperty("bool5", "on");
        props.setProperty("bool6", "yes");

        // should be false
        props.setProperty("bool7", "false");
        props.setProperty("bool8", "off");
        props.setProperty("bool9", "pingpongruebe");

        assertEquals(true, FileIoUtil.readPropertiesBoolean(props, "bool1"));
        assertEquals(true, FileIoUtil.readPropertiesBoolean(props, "bool2"));
        assertEquals(true, FileIoUtil.readPropertiesBoolean(props, "bool3"));
        assertEquals(true, FileIoUtil.readPropertiesBoolean(props, "bool4"));
        assertEquals(true, FileIoUtil.readPropertiesBoolean(props, "bool5"));
        assertEquals(true, FileIoUtil.readPropertiesBoolean(props, "bool6"));

        assertEquals(false, FileIoUtil.readPropertiesBoolean(props, "bool7"));
        assertEquals(false, FileIoUtil.readPropertiesBoolean(props, "bool8"));
        assertEquals(false, FileIoUtil.readPropertiesBoolean(props, "bool9"));

    }

    @Test
    public void testGetTextFileFromUrl() {
        System.out.println("getTextfileFromUrl");
        System.out.println("Working Directory = " + System.getProperty("user.dir"));

        String url = "file://" + System.getProperty("user.dir") + File.separator + "src/test/resources/FileIoUtilTest/getTextFileTest.txt";

        List<String> textfileFromUrl = FileIoUtil.getTextfileFromUrl(url);
        assertFalse(textfileFromUrl.size() <= 0);
    }

}
