package com.github.hypfvieh.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

class NumberUtilTest {

    @Test
    void convertNumberToBigDecimal() {
        Double v = 11.111d;
        BigDecimal bigDecimal = NumberUtil.convertNumberToBigDecimal(v, 3);

        assertEquals(v, bigDecimal.doubleValue());
        assertEquals(3, bigDecimal.scale());
    }

    @Test
    void testConvertNumberToBigDecimal() {
        BigInteger bigInteger = BigInteger.valueOf(100L);
        BigDecimal bigDecimal = NumberUtil.convertNumberToBigDecimal(bigInteger);

        assertEquals(bigDecimal.toBigInteger(), bigInteger);
    }

    @Test
    void isLess() {
        assertTrue(NumberUtil.isLess(BigDecimal.ZERO, BigDecimal.ONE));
    }

    @Test
    void isGreater() {
        assertTrue(NumberUtil.isGreater(BigDecimal.ONE, BigDecimal.ZERO));
    }

    @Test
    void isEqual() {
        assertTrue(NumberUtil.isEqual(BigDecimal.ONE, BigDecimal.valueOf(1L)));
        assertTrue(NumberUtil.isEqual(new BigDecimal("1.11000"), new BigDecimal("1.11")));
    }

    @Test
    void isZero() {
        assertTrue(NumberUtil.isZero(BigDecimal.ZERO));
        assertFalse(NumberUtil.isZero(BigDecimal.ONE));
    }

    @Test
    void isGreaterOrEqual() {
        assertTrue(NumberUtil.isGreaterOrEqual(BigDecimal.ONE, BigDecimal.ZERO));
        assertTrue(NumberUtil.isGreaterOrEqual(BigDecimal.ONE, BigDecimal.ONE));
    }

    @Test
    void isLessOrEqual() {
        assertTrue(NumberUtil.isLessOrEqual(BigDecimal.ZERO, BigDecimal.ONE));
        assertTrue(NumberUtil.isLessOrEqual(BigDecimal.ONE, BigDecimal.ONE));
    }

    @Test
    void isNegative() {
        assertTrue(NumberUtil.isNegative(BigDecimal.valueOf(-1L)));
    }

    @Test
    void invert() {
        assertEquals(BigDecimal.ONE, NumberUtil.invert(BigDecimal.valueOf(-1L)));
    }

    @Test
    void sumBigDecimals() {
        assertEquals(NumberUtil.sumBigDecimals(BigDecimal.ONE, BigDecimal.ONE), BigDecimal.valueOf(2L));
    }

    @Test
    void testSumBigDecimals() {
        BigDecimal expected = BigDecimal.ONE.add(BigDecimal.TEN);
        assertEquals(expected, NumberUtil.sumBigDecimals(BigDecimal.ONE, BigDecimal.TEN));
    }

}
