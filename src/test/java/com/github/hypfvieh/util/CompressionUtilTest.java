package com.github.hypfvieh.util;

import java.io.File;

import org.junit.Test;

import com.github.hypfvieh.AbstractBaseUtilTest;

public class CompressionUtilTest extends AbstractBaseUtilTest {


    @Test
    public void testGzipCompressUncompress() {
        System.out.println("GzipCompressUncompress");
        File compressedGzip = CompressionUtil.compressFileGzip("src/test/resources/CompressionUtilTest/FileToCompress.txt", SystemUtil.concatFilePath(SystemUtil.getTempDir(), "SampleCompress.gz"));

        assertFileExists(compressedGzip);

        File extractedGzip = CompressionUtil.extractFileGzip(compressedGzip.getAbsolutePath(), SystemUtil.concatFilePath(SystemUtil.getTempDir(), "SampleUncompressCompress.txt"));
        assertFileExists(extractedGzip);

        String content = FileIoUtil.readFileToString(extractedGzip);

        assertContains(content, "compressed by a unit test");

        extractedGzip.delete(); // remove no longer needed file
    }


}
