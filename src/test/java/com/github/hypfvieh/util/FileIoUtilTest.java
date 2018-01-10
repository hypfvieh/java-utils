package com.github.hypfvieh.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import org.junit.Test;

import com.github.hypfvieh.AbstractBaseUtilTest;
import com.github.hypfvieh.common.SearchOrder;

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

    @Test
    public void testReadFileFrom() {
    	String path = System.getProperty("user.dir") + File.separator + "src/test/resources/FileIoUtilTest/getTextFileTest.txt";
    	List<String> readFileFrom = FileIoUtil.readFileFrom(path, Charset.defaultCharset(), SearchOrder.SYSTEM_PATH, SearchOrder.CLASS_PATH, SearchOrder.CUSTOM_PATH);

    	assertFalse(readFileFrom.isEmpty());
    }


    @Test
    public void testReadPropertiesFromStreamOk() throws FileNotFoundException {
        FileInputStream inputStream = new FileInputStream("src/test/resources/FileIoUtilTest/readProperties.properties");
        Properties readProperties = FileIoUtil.readProperties(inputStream);
        assertEquals("Content", readProperties.getProperty("Other"));
    }

    @Test
    public void testReadPropertiesFromStreamFail() {
        Properties readProperties = FileIoUtil.readProperties((InputStream) null);
        assertNull(readProperties);
    }

    @Test
    public void testReadPropertiesFromFileOk() {
        File propFile = new File("src/test/resources/FileIoUtilTest/readProperties.properties");
        Properties readProperties = FileIoUtil.readProperties(propFile);
        assertEquals("Content", readProperties.getProperty("Other"));
    }

    @Test
    public void testReadPropertiesFromFileFail() {
        File propFile = new File("src/test/resources/FileIoUtilTest/not_existing_readProperties.properties");
        Properties readProperties = FileIoUtil.readProperties(propFile);
        assertNull(readProperties);
    }

    @Test
    public void testReadPropertiesFromFileWithInputPropertiesOk() {
        Properties properties = new Properties();
        properties.setProperty("key1", "val1");
        properties.setProperty("key2", "val2");

        Properties readProperties = FileIoUtil.readPropertiesFromFile("src/test/resources/FileIoUtilTest/readProperties.properties", properties);
        assertEquals("Content", readProperties.getProperty("Other"));
        assertEquals("val1", readProperties.getProperty("key1"));
    }

    @Test
    public void testReadPropertiesFromFileWithInputPropertiesFail() {
        Properties properties = new Properties();
        properties.setProperty("key1", "val1");
        properties.setProperty("key2", "val2");

        Properties readProperties = FileIoUtil.readPropertiesFromFile("src/test/resources/FileIoUtilTest/not_existing_readProperties.properties", properties);
        assertEquals("val1", readProperties.getProperty("key1"));
    }

    @Test
    public void testWritePropertiesToFileOk() throws IOException {
        Properties properties = new Properties();
        properties.setProperty("key1", "val1");
        properties.setProperty("key2", "val2");

        File createTempFile = File.createTempFile(getClass().getSimpleName() + getMethodName(), ".properties");

        assertTrue(FileIoUtil.writeProperties(createTempFile, properties));

        Properties readProperties = FileIoUtil.readProperties(createTempFile);
        assertEquals("val1", readProperties.getProperty("key1"));
        assertEquals("val2", readProperties.getProperty("key2"));
    }

    @Test
    public void testWritePropertiesFileAsStringOk() throws IOException {
        Properties properties = new Properties();
        properties.setProperty("key1", "val1");
        properties.setProperty("key2", "val2");

        String fileName = SystemUtil.getTempDir() + getClass().getSimpleName() + getMethodName() + ".properties";
        assertTrue(FileIoUtil.writeProperties(fileName, properties));

        File file = new File(fileName);
        file.deleteOnExit();
        Properties readProperties = FileIoUtil.readProperties(file);
        assertEquals("val1", readProperties.getProperty("key1"));
        assertEquals("val2", readProperties.getProperty("key2"));

        file.delete();
    }

    @Test
    public void testReadFileToStringFromString() {
        String readFileToString = FileIoUtil.readFileToString("src/test/resources/FileIoUtilTest/getTextFileTest.txt");
        assertContains(readFileToString, "Lore Ipsum kann");
    }

    @Test
    public void testReadFileToStringFromFile() {
        String readFileToString = FileIoUtil.readFileToString(new File("src/test/resources/FileIoUtilTest/getTextFileTest.txt"));
        assertContains(readFileToString, "Lore Ipsum kann");
    }

    @Test
    public void testReadFileToListOfStringFromString() {
        List<String> readFileToString = FileIoUtil.readFileToList("src/test/resources/FileIoUtilTest/getTextFileTest.txt");
        assertEquals(2, readFileToString.size());
        assertContains(readFileToString.get(1), "Lore Ipsum kann");
    }

    @Test
    public void testReadFileToListOfStringFromFile() {
        List<String> readFileToString = FileIoUtil.readFileToList(new File("src/test/resources/FileIoUtilTest/getTextFileTest.txt"));
        assertEquals(2, readFileToString.size());
        assertContains(readFileToString.get(1), "Lore Ipsum kann");
    }

    @Test
    public void testReadStringFromResourcesOk() throws FileNotFoundException {
        FileInputStream fileInputStream = new FileInputStream("src/test/resources/FileIoUtilTest/getTextFileTest.txt");

        String readStringFromResources = FileIoUtil.readStringFromResources(fileInputStream, Charset.defaultCharset().toString());
        assertContains(readStringFromResources, "Lore Ipsum kann");
    }

    @Test
    public void testReadStringFromResourcesFail(){
        String readStringFromResources = FileIoUtil.readStringFromResources(null, Charset.defaultCharset().toString());
        assertNull(readStringFromResources);
    }

    @Test
    public void testLoadPropertiesFromClassPathOk() throws IOException{
        Properties loadPropertiesFromClasspath = FileIoUtil.loadPropertiesFromClasspath("FileIoUtilTest/readProperties.properties");
        assertEquals("More", loadPropertiesFromClasspath.getProperty("Even"));
    }

    @Test(expected = IOException.class)
    public void testLoadPropertiesFromClassPathFail() throws IOException {
        FileIoUtil.loadPropertiesFromClasspath("FileIoUtilTest/not_existing_readProperties.properties");
    }

    @Test
    public void testLoadPropertiesFromClassPathWithInputPropertiesOk() {
        Properties properties = new Properties();
        properties.setProperty("key1", "val1");
        properties.setProperty("key2", "val2");

        assertTrue(FileIoUtil.loadPropertiesFromClasspath("FileIoUtilTest/readProperties.properties", properties));
        assertEquals("More", properties.getProperty("Even"));
        assertEquals("val1", properties.getProperty("key1"));
    }

    @Test
    public void testLoadPropertiesFromClassPathWithInputPropertiesFail() {
        Properties properties = new Properties();
        properties.setProperty("key1", "val1");
        properties.setProperty("key2", "val2");

        assertFalse(FileIoUtil.loadPropertiesFromClasspath("FileIoUtilTest/not_existing_readProperties.properties", properties));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLoadPropertiesFromClassPathWithInputPropertiesIllegalInput() {
        FileIoUtil.loadPropertiesFromClasspath("FileIoUtilTest/not_existing_readProperties.properties", null);
    }

    @Test
    public void testReadFileFromClassPathOk()  {
        String readFileFromClassPath = FileIoUtil.readFileFromClassPath("FileIoUtilTest/getTextFileTest.txt");
        assertContains(readFileFromClassPath, "Lore Ipsum kann");
    }

    @Test
    public void testReadFileFromClassPathFail()  {
        assertNull(FileIoUtil.readFileFromClassPath(null));
    }

    @Test
    public void testWriteTextFileNoAppendOk() throws IOException  {
        String createTempFile = SystemUtil.getTempDir() + getClass().getSimpleName() + getMethodName() + ".txt";
        String newFileContent = "This is only a test\nWith some lines\nin it.";

        assertTrue(FileIoUtil.writeTextFile(createTempFile, newFileContent, Charset.defaultCharset(), false));

        File file = new File(createTempFile);
        file.deleteOnExit();
        List<String> readTextFileFromStream = FileIoUtil.readTextFileFromStream(new FileInputStream(file), Charset.defaultCharset(), true);

        assertEquals(3, readTextFileFromStream.size());
        assertEquals("This is only a test", readTextFileFromStream.get(0));
        assertEquals("With some lines", readTextFileFromStream.get(1));
        assertEquals("in it.", readTextFileFromStream.get(2));

        file.delete();
    }

    @Test
    public void testWriteTextFileAppendOk() throws IOException  {

        String createTempFile = SystemUtil.getTempDir() + getClass().getSimpleName() + getMethodName() + ".txt";
        File file = new File(createTempFile);

        if (file.exists()) {
            file.delete();
        }

        Files.copy(Paths.get("src/test/resources/FileIoUtilTest/getTextFileTest.txt"), Paths.get(createTempFile));

        String newFileContent = "This is only a test\nWith some lines\nin it.";

        assertTrue(FileIoUtil.writeTextFile(createTempFile, newFileContent, Charset.defaultCharset(), true));


        file.deleteOnExit();
        List<String> readTextFileFromStream = FileIoUtil.readTextFileFromStream(new FileInputStream(file), Charset.defaultCharset(), true);

        assertEquals(4, readTextFileFromStream.size());
        assertEquals("Lirum Larum Loeffelstiel", readTextFileFromStream.get(0));
        assertEquals("Lore Ipsum kann nicht vielThis is only a test", readTextFileFromStream.get(1));
        assertEquals("With some lines", readTextFileFromStream.get(2));
        assertEquals("in it.", readTextFileFromStream.get(3));

        file.delete();
    }

    @Test
    public void testGuessLineTerminatorOfFile() throws IOException  {
        String unixLineFeed = FileIoUtil.guessLineTerminatorOfFile("src/test/resources/FileIoUtilTest/guessLineTerminatorOfFileUnix.txt");
        String windowsLineFeed = FileIoUtil.guessLineTerminatorOfFile("src/test/resources/FileIoUtilTest/guessLineTerminatorOfFileWindows.txt");
        String macOs9LineFeed = FileIoUtil.guessLineTerminatorOfFile("src/test/resources/FileIoUtilTest/guessLineTerminatorOfFileMacOs9.txt");

        assertEquals("\n", unixLineFeed);
        assertEquals("\r\n", windowsLineFeed);
        assertEquals("\r", macOs9LineFeed);

    }
}
