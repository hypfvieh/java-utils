package com.github.hypfvieh.util;

import java.io.File;

import org.junit.jupiter.api.Test;

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
    public void testConcatFilePath() {
        String result = SystemUtil.concatFilePath("foo", "bar", "baz");
        assertNotNull(result);
        assertEquals("foo" + File.separator + "bar" + File.separator + "baz", result);
    }

    @Test
    public void testGetTempDir() {
        String tempDir = SystemUtil.getTempDir();

        assertNotNull(tempDir);
        assertTrue(tempDir.endsWith(System.getProperty("file.separator")), "tmpdir " + tempDir + " does not end with file separator");
        assertTrue(new File(tempDir).exists(), "tmpdir " + tempDir + " does not exist");
        assertTrue(new File(tempDir).isDirectory(), "tmpdir " + tempDir + " not a directory");
        assertEquals(new File(tempDir), new File(System.getProperty("java.io.tmpdir")));
    }

    @Test
    public void testNormalizePath() {
        assertTrue(SystemUtil.normalizePath(null) == null);
        assertTrue(SystemUtil.normalizePath("/home").endsWith(System.getProperty("file.separator")));
    }
}
