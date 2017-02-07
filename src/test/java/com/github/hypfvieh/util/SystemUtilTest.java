package com.github.hypfvieh.util;

import java.io.File;

import org.junit.Test;

import com.github.hypfvieh.AbstractBaseUtilTest;

/**
 *
 * @author hypfvieh
 */
public class SystemUtilTest extends AbstractBaseUtilTest {

    public SystemUtilTest() {
    }


    @Test
    public void testGetWorkingDirectory() {
        String result = SystemUtil.getWorkingDirectory();
        assertNotNull(result);
    }

    @Test
    public void testGetTempDir() {
        String result = SystemUtil.getTempDir();
        assertNotNull(result);
    }

    @Test
    public void testConcatFilePath() {
        String result = SystemUtil.concatFilePath("foo", "bar", "baz");
        assertNotNull(result);
        assertEquals("foo" + File.separator + "bar" + File.separator + "baz", result);
    }
}
