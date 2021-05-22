package com.github.hypfvieh.util;

import org.junit.jupiter.api.Test;

import com.github.hypfvieh.AbstractBaseUtilTest;

/**
 *
 * @author hypfvieh
 */
public class FixUtilTest extends AbstractBaseUtilTest {

    private String sampleFixMsg = "8=FIX.4.2|9=65|35=0|49=TRIOMM|56=BAADER_UAT_AUC_DC|34=1992|52=20140714-21:29:29|10=149|";

    public FixUtilTest() {
    }

    /**
     * Test of getDelimiterFromFixMsgStr method, of class FixUtil.
     */
    @Test
    public void testGetDelimiterFromFixMsgStr() {
        System.out.println("getDelimiterFromFixMsgStr");
        String expResult = "|";
        String result = FixUtil.getDelimiterFromFixMsgStr(sampleFixMsg);
        assertEquals(expResult, result);
    }

    /**
     * Test of setFixTagOnMsgStr method, of class FixUtil.
     */
    @Test
    public void testSetFixTagOnMsgStr() {
        System.out.println("setFixTagOnMsgStr");
        String expResult = "8=FIX.4.2|9=65|35=0|49=TRIOMM|56=BAADER_UAT_AUC_DC|34=1992|52=20140714-21:29:29|4711=FOOBAR|10=149|";
        String result = FixUtil.setFixTagOnMsgStr(sampleFixMsg, 4711, "FOOBAR");
        assertEquals(expResult, result);
    }

    /**
     * Test of calculateFixBodyLength method, of class FixUtil.
     */
    @Test
    public void testCalculateFixBodyLength() {
        System.out.println("calculateFixBodyLength");
        String brokenMsg = sampleFixMsg;
        FixUtil.setFixTagOnMsgStr(brokenMsg, 10, "815");
        int expectValue = 65;
        int calcValue = FixUtil.calculateFixBodyLength(brokenMsg);
        assertEquals(expectValue, calcValue);
    }

    /**
     * Test of calculateFixCheckSum method, of class FixUtil.
     */
    @Test
    public void testCalculateFixCheckSum() {
        System.out.println("calculateFixCheckSum");
        String expResult = "149";
        String result = FixUtil.calculateFixCheckSum(sampleFixMsg, "|".charAt(0));
        assertEquals(expResult, result);
    }

    /**
     * Test of looksLikeFixMsg method, of class FixUtil.
     */
    @Test
    public void testLooksLikeFixMsg() {
        System.out.println("looksLikeFixMsg");
        boolean expResult = true;
        boolean result = FixUtil.looksLikeFixMsg(sampleFixMsg);
        assertEquals(expResult, result);
    }

    @Test
    public void testSetValueCorrectOrder() {
        System.out.println("setValue and correct order");
        String testMsg = "9=65|49=TRIOMM|56=BAADER_UAT_AUC_DC|8=FIX.4.2|34=1992|52=20140714-21:29:29|35=0|4711=FOO|10=149|";
        String expResult = "8=FIX.4.2|9=65|35=0|49=TRIOMM|56=BAADER_UAT_AUC_DC|34=1992|52=20140714-21:29:29|4711=FOOBAR|10=149|";

        assertEquals(expResult, FixUtil.setFixTagOnMsgStr(testMsg, 4711, "FOOBAR"));
    }

}
