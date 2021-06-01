package com.github.hypfvieh.config.xml;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;

import com.github.hypfvieh.AbstractBaseUtilTest;

public class XmlConfigurationTest extends AbstractBaseUtilTest {

    @Test
    public void testGetString() throws IOException {
        XmlConfiguration xmlConfiguration = new XmlConfigBuilder().setInputFile("src/test/resources/xmlConfigTest/xmlConfigTest.xml").build();

        String readValue = xmlConfiguration.getString("Config/Key2/SubKey1");
        assertEquals("SubValue1", readValue);
        readValue = xmlConfiguration.getString("Config/Key3/SubKeyList/SubListEntry");
        assertEquals("SubListEntry1", readValue);
        readValue = xmlConfiguration.getString("Not/existing");
        assertNull(readValue);
    }

    @Test
    public void testGetList() throws IOException {

        XmlConfiguration xmlConfiguration = new XmlConfigBuilder().setInputFile("src/test/resources/xmlConfigTest/xmlConfigTest.xml").build();

        List<String> readValue = xmlConfiguration.getStringList("Config/Key2/SubKey1");
        assertEquals(1, readValue.size());
        assertEquals("SubValue1", readValue.get(0));

        readValue = null;
        readValue = xmlConfiguration.getStringList("Config/Key3/SubKeyList/SubListEntry");
        assertEquals(4, readValue.size());
        assertEquals("SubListEntry2", readValue.get(1));
        assertEquals("SubListEntry3", readValue.get(3));

        readValue = null;
        readValue = xmlConfiguration.getStringList("Config/Key3/SubKeyList");
        assertEquals(4, readValue.size());
        assertEquals("SubListEntry2", readValue.get(1));
        assertEquals("SubListEntry3", readValue.get(3));

    }

    @Test
    public void testSetString() throws IOException {
        XmlConfiguration xmlConfiguration = new XmlConfigBuilder().setInputFile("src/test/resources/xmlConfigTest/xmlConfigTest.xml").build();

        String readValue = xmlConfiguration.getString("Config/Key2/SubKey1");
        assertEquals("SubValue1", readValue);

        xmlConfiguration.setString("Config/Key2/SubKey1", false, "NewValue1");

        readValue = xmlConfiguration.getString("Config/Key2/SubKey1");
        assertEquals("NewValue1", readValue);
    }

    @Test
    public void testSetValues() throws IOException {
        XmlConfiguration xmlConfiguration = new XmlConfigBuilder().setInputFile("src/test/resources/xmlConfigTest/xmlConfigTest.xml").build();

        String readValue = xmlConfiguration.getString("Config/Key2/SubKey1");
        assertEquals("SubValue1", readValue);
        readValue = xmlConfiguration.getString("Config/Key4/NotInt");
        assertEquals("A", readValue);
        readValue = xmlConfiguration.getString("Config/Key4/Int");
        assertEquals("100", readValue);

        Map<String, String> values = new HashMap<>();

        values.put("Config/Key2/SubKey1", "NewValue1");
        values.put("Config/Key4/NotInt", "B");
        values.put("Config/Key4/Int", "2000");

        xmlConfiguration.setValues(values, false);

        readValue = xmlConfiguration.getString("Config/Key2/SubKey1");
        assertEquals("NewValue1", readValue);
        readValue = xmlConfiguration.getString("Config/Key4/NotInt");
        assertEquals("B", readValue);
        readValue = xmlConfiguration.getString("Config/Key4/Int");
        assertEquals("2000", readValue);
    }

    @Test
    public void testSaveToFile() throws IOException {
        File tempFile = File.createTempFile(getClass().getSimpleName() + getMethodName(), ".xml");
        XmlConfiguration xmlConfiguration = new XmlConfigBuilder()
            .setInputFile("src/test/resources/xmlConfigTest/xmlConfigTest.xml")
            .setOutputFile(tempFile).build();

        String readValue = xmlConfiguration.getString("Config/Key2/SubKey1");
        assertEquals("SubValue1", readValue);

        xmlConfiguration.setString("Config/Key2/SubKey1", false, "NewValue1");

        readValue = xmlConfiguration.getString("Config/Key2/SubKey1");
        assertEquals("NewValue1", readValue);

        xmlConfiguration.save();
        xmlConfiguration = null;

        xmlConfiguration = new XmlConfigBuilder()
                .setInputFile(tempFile).build();

        readValue = xmlConfiguration.getString("Config/Key2/SubKey1");
        assertEquals("NewValue1", readValue);

        tempFile.delete();
    }

    @Test
    public void testGetStringProperty() {
        XmlConfiguration xmlConfiguration = new XmlConfigBuilder()
                .setInputFile("src/test/resources/xmlConfigTest/xmlConfigTest.xml")
                .setSkipRoot(true)
                .setKeyDelimiter(".")
                .setAllowKeyOverrideFromEnvironment(true)
                .build();

        // correct value found
        assertEquals("Value1", xmlConfiguration.getString("Key1", "Key2"));

        // key found, but wrong value
        assertNotEquals("fail", xmlConfiguration.getString("Key1", "fail"));

        // wrong key, wrong value
        assertNotEquals("fail", xmlConfiguration.getString("foo", "bar"));

        // wrong key, correct default
        assertEquals("fail", xmlConfiguration.getString("foo", "fail"));

        // correct key, overridden by environment
        System.getProperties().setProperty("Key2.SubKey1", "Jambalaja");
        assertEquals("Jambalaja", xmlConfiguration.getString("Key2.SubKey1", "default"));
    }

    @Test
    public void testGetStringListProperty() {
        XmlConfiguration xmlConfiguration = new XmlConfigBuilder()
                .setInputFile("src/test/resources/xmlConfigTest/xmlConfigTest.xml")
                .setSkipRoot(true)
                .setKeyDelimiter(".")
                .build();

        List<String> stringList = xmlConfiguration.getStringList("Key3.SubKeyList.SubListEntry");

        assertFalse(stringList.isEmpty());
        assertTrue(stringList.size() == 4);
        assertEquals("SubListEntry1", stringList.get(0));
    }

    @Test
    public void testGetStringSetProperty() {
        XmlConfiguration xmlConfiguration = new XmlConfigBuilder()
                .setInputFile("src/test/resources/xmlConfigTest/xmlConfigTest.xml")
                .setKeyDelimiter(".")
                .setSkipRoot(true)
                .build();

        Set<String> stringList = xmlConfiguration.getStringSet("Key3.SubKeyList.SubListEntry", TreeSet.class);

        assertFalse(stringList.isEmpty());
        assertTrue(stringList.size() == 3);
    }

    @Test
    public void testGetIntProperty() {
        XmlConfiguration xmlConfiguration = new XmlConfigBuilder()
                .setInputFile("src/test/resources/xmlConfigTest/xmlConfigTest.xml")
                .setSkipRoot(true)
                .setKeyDelimiter(".")
                .build();

        // key found, but not integer
        assertEquals(-1, xmlConfiguration.getInt("Key4.NotInt", -1));

        // key found and is integer
        assertEquals(100, xmlConfiguration.getInt("Key4.Int", -1));
    }

    @Test
    public void testGetBooleanProperty() {
        XmlConfiguration xmlConfiguration = new XmlConfigBuilder()
                .setInputFile("src/test/resources/xmlConfigTest/xmlConfigTest.xml")
                .setSkipRoot(true)
                .setKeyDelimiter(".")
                .build();

        // key found, but not bool, expect default
        assertEquals(false, xmlConfiguration.getBoolean("Key5.NotBool", false));

        // key found and is boolean
        assertEquals(true, xmlConfiguration.getBoolean("Key5.Bool", false));
    }
}
