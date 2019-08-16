package com.github.hypfvieh.util;

import java.util.List;

import org.junit.Test;

import com.github.hypfvieh.AbstractBaseUtilTest;

/**
 *
 * @author hypfvieh
 */
public class StringUtilTest extends AbstractBaseUtilTest {

    public StringUtilTest() {
    }

    @Test
    public void testSmartSplit() throws Exception {

        String sampleText = "Vorbereitung Quoteverbreiterung falls TG nicht da: Felder EE und DU als TG Indikator eingefügt. "
                + "Schalter 16 eingefügt. Felder EK und EL eingefügt. EK/EL berechnet die MinPerformance Bid/Ask, "
                + "falls TG nicht als Konkurrenz und Schalter16 auf yes. In RicBid/Ask Formeln so angepasst, "
                + "dass falls TG nicht Konkurrenz, Schalter16 an und EK/EL > 0, so wird EK/EL 'Smart Performance' verwendet.";

        List<String> smartStringSplit = StringUtil.smartWordSplit(sampleText, 50);
        assertEquals("Expected 8 lines", 8, smartStringSplit.size());

        for (String line : smartStringSplit) {
            assertTrue("Each line should be no longer than 50", line.length() <= 50);
        }

    }

    @Test
    public void testSmartSplitWrongTokenLengthFix() throws Exception {

        String sampleText = "Disconnect the given session. If --force is used socket will be closed without sending logout message";

        List<String> smartStringSplit = StringUtil.smartWordSplit(sampleText, 29);
        assertEquals("Expected 4 lines", 4, smartStringSplit.size());

        for (String line : smartStringSplit) {
            assertTrue("Each line should be no longer than 29", line.length() <= 29);
        }

    }


    @Test
    public void testLowerCaseFirstChar() throws Exception {
        String tstStr = "TEST";

        assertEquals("tEST", StringUtil.lowerCaseFirstChar(tstStr));
    }

    @Test
    public void testUpperCaseFirstChar() throws Exception {
        String tstStr = "test";

        assertEquals("Test", StringUtil.upperCaseFirstChar(tstStr));
    }

    @Test
    public void testJoin() throws Exception {
        String[] toJoin = {"This", "should", "be", "joined"};

        String joined = StringUtil.join(" ", toJoin);

        assertEquals("This should be joined", joined);
    }

    @Test
    public void testAbbreviate() throws Exception {
        String tstStr = "This should be shortend";

        assertEquals("This should...", StringUtil.abbreviate(tstStr, 14));
    }

    @Test
    public void testConvertCamelToUpperCase() {
        assertNull(StringUtil.convertCamelToUpperCase(null));
        assertEquals("", StringUtil.convertCamelToUpperCase(""));
        assertEquals("  ", StringUtil.convertCamelToUpperCase("  "));
        assertEquals("QUOTE_STATUS_REPORT", StringUtil.convertCamelToUpperCase("QuoteStatusReport"));
        assertEquals("HELLO", StringUtil.convertCamelToUpperCase("hello"));
        assertEquals("MEDIA", StringUtil.convertCamelToUpperCase("MEDIA"));
        assertEquals("ACME_COMPANY", StringUtil.convertCamelToUpperCase("AcmeCompany"));
    }

    @Test
    public void testConvertUpperToCamelCase() {
        assertNull(StringUtil.convertUpperToCamelCase(null));
        assertEquals("", StringUtil.convertUpperToCamelCase(""));
        assertEquals("  ", StringUtil.convertUpperToCamelCase("  "));
        assertEquals("QuoteStatusReport", StringUtil.convertUpperToCamelCase("QUOTE_STATUS_REPORT"));
        assertEquals("Hello", StringUtil.convertUpperToCamelCase("hello"));
        assertEquals("UserResponse", StringUtil.convertUpperToCamelCase("UserResponse"));
    }
    
    @Test
    public void testRepeat() {
        assertNull(StringUtil.repeat(null, 1));
        assertNull(StringUtil.repeat(null, 0));
        assertNull(StringUtil.repeat("*", 0));
        assertNull(StringUtil.repeat("*", -1));
        
        assertEquals("***", StringUtil.repeat("*", 3));
        assertEquals("xYxY", StringUtil.repeat("xY", 2));
    }
    
    @Test
    public void testMask() {
        assertNull(StringUtil.mask(null, "x", 1, 1));
        assertNull(StringUtil.mask("test", "", 1, 1));

        assertEquals("test", StringUtil.mask("test", "x", 5, 1));
        assertEquals("txxx", StringUtil.mask("test", "x", 1, 3));
        
        assertEquals("t*st", StringUtil.mask("test", "*", 1, 1));
        assertEquals("t**t", StringUtil.mask("test", "*", 1, 2));

        assertEquals("**st", StringUtil.mask("test", "*",0, 2));
        assertEquals("***t", StringUtil.mask("test", "*",0, 3));
        
        // test mask length longer than string length
        assertEquals("t***", StringUtil.mask("test", "*",1, 6));
    }
    
    @Test
    public void testSnakeToCamelCase() {
        assertEquals("snakeCase", StringUtil.snakeToCamelCase("snake_case"));
        assertEquals("longerSnakeCase", StringUtil.snakeToCamelCase("longer_snake_case"));
        assertEquals("sneakySnakeCaseConversion", StringUtil.snakeToCamelCase("sneaky_snake_case_conversion"));
        
        assertEquals("snakeCase_1withDigit", StringUtil.snakeToCamelCase("snake_case_1with_digit"));
        assertEquals("snakeCaseWithUpperSnake", StringUtil.snakeToCamelCase("snake_case_with_Upper_snake"));
    }
    
    @Test
    public void testConcatStrings() {
        assertEquals("1, 2, 3", StringUtil.concatStrings(true, ", ", "1", "2", "3"));
        assertEquals("1, 2, 3", StringUtil.concatStrings(false, ", ", "1", "2", "3"));

        assertEquals("1, 3", StringUtil.concatStrings(true, ", ", "1", "", "3"));
        assertEquals("1, , 3", StringUtil.concatStrings(false, ", ", "1", "", "3"));
        assertEquals("1, 3", StringUtil.concatStrings(false, ", ", "1", null, "3"));

        assertEquals("2, 3", StringUtil.concatStrings(true, ", ", "", "2", "3"));
        assertEquals("2, 3", StringUtil.concatStrings(false, ", ", null, "2", "3"));
        assertEquals(", 2, 3", StringUtil.concatStrings(false, ", ", "", "2", "3"));

    }
}

